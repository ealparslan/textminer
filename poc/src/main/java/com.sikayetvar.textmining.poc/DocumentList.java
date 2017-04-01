package com.sikayetvar.textmining.poc;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

public class DocumentList implements Iterable<List<String>> {
    private final BlockingQueue<List<String>> queue;
    private final DocumentListIterator iterator;

    public DocumentList() {
        queue = new LinkedBlockingQueue<>();
        iterator = new DocumentListIterator(queue);
    }

    public void addDocument(List<String> document) {
        try {
            queue.put(document);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void finishProcessing() {
        iterator.finishProcessing();
    }

    @Override
    public DocumentListIterator iterator() {
        return iterator;
    }

    private class DocumentListIterator implements Iterator<List<String>> {
        public static final String END_OF_DOCUMENTS = "END_OF_DOCUMENTS_THAT'S_ALL";
        private final BlockingQueue<List<String>> queue;
        private final ReentrantLock lock;

        public DocumentListIterator(BlockingQueue<List<String>> queue) {
            this.queue = queue;
            lock = new ReentrantLock();
        }

        public void finishProcessing() {
            try {
                queue.put(Arrays.asList(END_OF_DOCUMENTS));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public boolean hasNext() {
            try {
                lock.lockInterruptibly();
                List<String> document = queue.peek();
                // processing is finished only iff END_OF_DOCUMENTS is in the head of queue
                if (document != null && !document.isEmpty() && END_OF_DOCUMENTS.equals(document.get(0)))
                    return false;
                else
                    return true;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        }

        @Override
        public List<String> next() {
            try {
                lock.lockInterruptibly();
                List<String> document = queue.take();
                if (!document.isEmpty() && END_OF_DOCUMENTS.equals(document.get(0))) {
                    queue.put(Arrays.asList(END_OF_DOCUMENTS));
                    return Arrays.asList();
                }
                return document;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        }
    }
}
