package com.medallia.word2vec;

import com.google.common.base.Preconditions;
import com.medallia.word2vec.util.AC;
import com.medallia.word2vec.util.ProfilingTimer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Word2VecBuilder {
    private final static long ONE_GB = 1024 * 1024 * 1024;

    public static Word2VecModel fromBinFile(File file)
            throws IOException {
        ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;
        ProfilingTimer timer = ProfilingTimer.NONE;
        try (
                final FileInputStream fis = new FileInputStream(file);
                final AC ac = timer.start("Loading vectors from bin file")
        ) {
            final FileChannel channel = fis.getChannel();
            timer.start("Reading gigabyte #1");
            MappedByteBuffer buffer =
                    channel.map(
                            FileChannel.MapMode.READ_ONLY,
                            0,
                            Math.min(channel.size(), Integer.MAX_VALUE));
            buffer.order(byteOrder);
            int bufferCount = 1;
            // Java's NIO only allows memory-mapping up to 2GB. To work around this problem, we re-map
            // every gigabyte. To calculate offsets correctly, we have to keep track how many gigabytes
            // we've already skipped. That's what this is for.

            StringBuilder sb = new StringBuilder();
            char c = (char) buffer.get();
            while (c != '\n') {
                sb.append(c);
                c = (char) buffer.get();
            }
            String firstLine = sb.toString();
            int index = firstLine.indexOf(' ');
            Preconditions.checkState(index != -1,
                    "Expected a space in the first line of file '%s': '%s'",
                    file.getAbsolutePath(), firstLine);

            final int vocabSize = Integer.parseInt(firstLine.substring(0, index));
            final int layerSize = Integer.parseInt(firstLine.substring(index + 1));
            timer.appendToLog(String.format(
                    "Loading %d vectors with dimensionality %d",
                    vocabSize,
                    layerSize));

            List<String> vocabs = new ArrayList<String>(vocabSize);
            DoubleBuffer vectors = ByteBuffer.allocateDirect(vocabSize * layerSize * 8).asDoubleBuffer();

            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            long lastLogMessage = System.currentTimeMillis();
            final float[] floats = new float[layerSize];
            for (int lineno = 0; lineno < vocabSize; lineno++) {
                // read vocab
                sb.setLength(0);

                /*
                c = (char) buffer.get();
                while (c != ' ') {
                    // ignore newlines in front of words (some binary files have newline,
                    // some don't)
                    if (c != '\n') {
                        sb.append(c);
                    }
                    c = (char) buffer.get();
                }*/

                stream.reset();
                byte b = buffer.get();
                while (b != ' ') {
                    // ignore newlines in front of words (some binary files have newline,
                    // some don't)
                    if (b != '\n') {
                        stream.write(b);
                    }
                    b = buffer.get();
                }

                vocabs.add(new String(stream.toByteArray(), StandardCharsets.UTF_8));

                // read vector
                final FloatBuffer floatBuffer = buffer.asFloatBuffer();
                floatBuffer.get(floats);
                for (int i = 0; i < floats.length; ++i) {
                    vectors.put(lineno * layerSize + i, floats[i]);
                }
                buffer.position(buffer.position() + 4 * layerSize);

                // print log
                final long now = System.currentTimeMillis();
                if (now - lastLogMessage > 1000) {
                    final double percentage = ((double) (lineno + 1) / (double) vocabSize) * 100.0;
                    timer.appendToLog(
                            String.format("Loaded %d/%d vectors (%f%%)", lineno + 1, vocabSize, percentage));
                    lastLogMessage = now;
                }

                // remap file
                if (buffer.position() > ONE_GB) {
                    final int newPosition = (int) (buffer.position() - ONE_GB);
                    final long size = Math.min(channel.size() - ONE_GB * bufferCount, Integer.MAX_VALUE);
                    timer.endAndStart(
                            "Reading gigabyte #%d. Start: %d, size: %d",
                            bufferCount,
                            ONE_GB * bufferCount,
                            size);
                    buffer = channel.map(
                            FileChannel.MapMode.READ_ONLY,
                            ONE_GB * bufferCount,
                            size);
                    buffer.order(byteOrder);
                    buffer.position(newPosition);
                    bufferCount += 1;
                }
            }
            timer.end();

            return new Word2VecModel(vocabs, layerSize, vectors);
        }
    }
}
