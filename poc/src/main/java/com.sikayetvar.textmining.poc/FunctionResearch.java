package com.sikayetvar.textmining.poc;

import com.sikayetvar.textmining.api.datalayer.DataOperatorFactory;
import com.sikayetvar.textmining.api.entity.Complaint;
import com.sikayetvar.textmining.api.entity.Hashtag;
import com.sikayetvar.textmining.api.middle.ServiceOperator;
import com.sikayetvar.textmining.api.nlp.NgramBuilder;
import com.sikayetvar.textmining.api.nlp.WordParse;
import com.sikayetvar.textmining.api.nlp.ZemberekStemmer;
import com.sikayetvar.textmining.api.nlp.ZemberekStemmerBuilder;
import com.sikayetvar.textmining.api.scoring.*;
import com.sikayetvar.textmining.api.util.Configuration;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;
import java.util.stream.Collectors;

public class FunctionResearch {

    public static final int HASHTAG_LIMIT = 20;
    public static final int MAX_NGRAM_COUNT = 4;

    private static final Integer[] complaintSet1 = new Integer[]{3131818, 2939566, 2454087, 2038093, 1808804, 1845534, 3170985, 4525032, 4591425, 3152159, 1944102, 3180105, 3354919, 2084350,
            1725575, 4643322, 2078503, 2713544, 2297528, 2169995, 4085844, 2982973, 2450570, 2560190, 3255024, 3177359, 5206507, 4091625, 2728714, 2110356, 4372056, 3021181,
            4428765, 3026255, 5158588, 2587816, 2928586, 3313575, 1879601, 4426539, 2181544, 6285793, 2217101, 1972498, 2182162, 2361426, 3080373, 4616406, 5798209, 2494629,
            3753147, 3469318, 2539064, 3314985, 6370336, 5635990, 4510521, 3083704, 6498001, 2374487, 1945349, 3791403, 2105413, 1939784, 1666208, 2871120, 1981025, 3320443,
            3367233, 4087698, 1924593, 2844026, 2114894, 3133684, 4433337, 3270438, 3425214, 5229211, 5112523, 2763934, 2236099, 4555773, 2664387, 2076686, 2779664, 4137825,
            3217316, 3391704, 2689295, 2777299, 4993923, 5689942, 3163426, 2281269, 3092133, 2816909, 6079315, 4515072, 2427566, 2854811};

    private static final Integer[] complaintSet2 = new Integer[]{7311884, 7311860, 7311854, 7311851, 7311848, 7311845, 7311833, 7311830, 7311827};

    public void generateExcelReport() {
        try {
            List<Complaint> complaints = DataOperatorFactory.getDataOperator(Configuration.DATABASE).getComplaintsById(complaintSet2);

            ZemberekStemmer stemmer = ZemberekStemmerBuilder.getInstance().getStemmer();

            ServiceOperator serviceOperator = new ServiceOperator();

            XSSFWorkbook workbook = new XSSFWorkbook();
            CellStyle scoreCellStyle = workbook.createCellStyle();
            scoreCellStyle.setDataFormat(
                    workbook.getCreationHelper().createDataFormat().getFormat("##0.000"));

            List<String> categories = complaints.stream().map(Complaint::getCategory).distinct().sorted().collect(Collectors.toList());

            List<QueryScoreFunction> queryScoreFunctions = Arrays.asList(
                    new QueryScoreFunctionTfIdf(),
                    new QueryScoreFunctionLogTfIdf(),
                    new QueryScoreFunctionTf(),
                    new QueryScoreFunctionIdf()
            );

            for (String category : categories) {
                XSSFSheet sheet = workbook.createSheet(category);
                sheet.setColumnWidth(1, 12800);
                sheet.setColumnWidth(2, 25600);

                //Create a new row in current sheet
                Row row = sheet.createRow(0);

                for (int columnIndex = 0; columnIndex < queryScoreFunctions.size(); columnIndex++) {
                    QueryScoreFunction queryScoreFunction = queryScoreFunctions.get(columnIndex);
                    int start = 3 + columnIndex * 5;
                    int end = start + 4;
                    sheet.addMergedRegion(new CellRangeAddress(0, 0, start, end));
                    Cell cell = row.createCell(start);
                    cell.setCellValue(queryScoreFunction.getClass().getSimpleName());
                    CellUtil.setAlignment(cell, HorizontalAlignment.CENTER);
                }

                // generate special FIS function header
                {
                    int fisColumnIndex = queryScoreFunctions.size();
                    int start = 3 + fisColumnIndex * 5;
                    int end = start + 4;
                    sheet.addMergedRegion(new CellRangeAddress(0, 0, start, end));
                    Cell cell = row.createCell(start);
                    cell.setCellValue("FIS");
                    CellUtil.setAlignment(cell, HorizontalAlignment.CENTER);
                }

                row = sheet.createRow(1);
                Cell cell = row.createCell(0);
                cell.setCellValue("Id");
                cell = row.createCell(1);
                cell.setCellValue("Subject");
                cell = row.createCell(2);
                cell.setCellValue("Content");

                for (int i = 0; i < queryScoreFunctions.size() + 1; i++) { // one extra for FIS
                    int ci = 3 + i * 5;
                    cell = row.createCell(ci++);
                    cell.setCellValue("HashTag");
                    cell = row.createCell(ci++);
                    cell.setCellValue("TF");
                    cell = row.createCell(ci++);
                    cell.setCellValue("IDF");
                    cell = row.createCell(ci++);
                    cell.setCellValue("Score");
                    cell = row.createCell(ci++);
                    cell.setCellValue("NrmScore");
                }

                for (Cell c : row) {
                    CellUtil.setAlignment(c, HorizontalAlignment.CENTER);
                }

                // foreach complaint
                List<Complaint> complaintList = complaints.stream().filter(complaint1 -> complaint1.getCategory().equals(category)).
                        sorted(Comparator.comparingInt(Complaint::getId)).collect(Collectors.toList());
                for (int complaintIndex = 0; complaintIndex < complaintList.size(); complaintIndex++) {
                    int rowStartIndex = 2 + complaintIndex * HASHTAG_LIMIT;
                    int rowEndIndex = rowStartIndex + HASHTAG_LIMIT;
                    Complaint complaint = complaintList.get(complaintIndex);
                    row = sheet.createRow(rowStartIndex);
                    sheet.addMergedRegion(CellRangeAddress.valueOf("A" + (rowStartIndex + 1) + ":A" + (rowEndIndex)));
                    row.createCell(0).setCellValue(complaint.getId());
                    sheet.addMergedRegion(CellRangeAddress.valueOf("B" + (rowStartIndex + 1) + ":B" + (rowEndIndex)));
                    row.createCell(1).setCellValue(complaint.getSubject());
                    sheet.addMergedRegion(CellRangeAddress.valueOf("C" + (rowStartIndex + 1) + ":C" + (rowEndIndex)));
                    Cell rowCell = row.createCell(2);
                    rowCell.setCellValue(complaint.getContent());
                    XSSFCellStyle cellStyle = workbook.createCellStyle();
                    cellStyle.setWrapText(true);
                    rowCell.setCellStyle(cellStyle);
                    for (Cell c : row) {
                        CellUtil.setVerticalAlignment(c, VerticalAlignment.CENTER);
                    }

                    // get tfidfHashtags
                    String sentence = complaint.getSubject() + " . " + complaint.getContent();
                    List<WordParse> wordParses = stemmer.stemSentenceThenFilter(sentence);
                    Set<String> terms = wordParses.stream().map(WordParse::getRoot).distinct().collect(Collectors.toSet());

                    // generate ngrams
                    List<String> ngrams = new ArrayList<>();
                    for (int i = 2; i <= MAX_NGRAM_COUNT; i++) {
                        ngrams.addAll(NgramBuilder.buildNgramsOfParses(wordParses, i));
                    }
                    // add ngrams to terms
                    terms.addAll(ngrams);

                    List<String> termsWithNgrams = new ArrayList<>(terms);
                    termsWithNgrams.addAll(ngrams);

                    // TFIDF functions
                    // get results from db
                    List<Hashtag> tfidfHashtags = DataOperatorFactory.getDataOperator(Configuration.DATABASE).getHashtags(complaint.getCategory(), terms, 10000);
                    // FIS functions
                    // TODO: Fis hashtags
                    List<Hashtag> fisHashtags = new ArrayList<>();
                    // List<Hashtag> fisHashtags = DataOperatorFactory.getDataOperator(Configuration.DATABASE).getHashtags(Hashtag.Methods.FIS, category, termsWithNgrams).stream().filter(fis -> fis.getNormScore() > 0f).limit(HASHTAG_LIMIT).collect(Collectors.toList());

                    int functionIndex = 0;
                    // generate TFIDF results
                    for (; functionIndex < queryScoreFunctions.size(); functionIndex++) {
                        QueryScoreFunction queryScoreFunction = queryScoreFunctions.get(functionIndex);
                        QueryScoreModel queryScoreModel = new QueryScoreModel(tfidfHashtags, queryScoreFunction);
                        queryScoreModel.calculateScores();
                        List<Hashtag> modelHashtags = queryScoreModel.getHashtags().stream().filter(tfidf -> tfidf.getScore() > 0f).limit(HASHTAG_LIMIT).collect(Collectors.toList());
                        createDataColumn(sheet, scoreCellStyle, rowStartIndex, functionIndex, modelHashtags);
                    }

                    // generate FIS result
                    createDataColumn(sheet, scoreCellStyle, rowStartIndex, functionIndex, fisHashtags);
                }
            }

            FileOutputStream out = new FileOutputStream(new File("out/test.xlsx"));
            workbook.write(out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

    private void createDataColumn(XSSFSheet sheet, CellStyle scoreCellStyle, int rowStartIndex, int functionIndex, Collection<Hashtag> modelHashtags) {
        Row row;
        int columnIndex = 3 + functionIndex * 5;
        sheet.setColumnWidth(columnIndex, 9000);
        sheet.setColumnWidth(columnIndex + 3, 3000);
        sheet.setColumnHidden(columnIndex + 1, true);
        sheet.setColumnHidden(columnIndex + 2, true);
        sheet.setColumnHidden(columnIndex + 4, true);
        int rowLocalIndex = rowStartIndex;

        // foreach hashtag
        for (Hashtag hashtag : modelHashtags) {
            row = sheet.getRow(rowLocalIndex);
            if (row == null)
                row = sheet.createRow(rowLocalIndex);

            columnIndex = 3 + functionIndex * 5;
            row.createCell(columnIndex++).setCellValue(hashtag.getTerm());

            Cell tfCell = row.createCell(columnIndex++);
            tfCell.setCellValue(hashtag.getTf());
            tfCell.setCellStyle(scoreCellStyle);

            Cell idfCell = row.createCell(columnIndex++);
            idfCell.setCellValue(hashtag.getIdf());
            idfCell.setCellStyle(scoreCellStyle);

            Cell scoreCell = row.createCell(columnIndex++);
            scoreCell.setCellValue(hashtag.getScore());
            scoreCell.setCellStyle(scoreCellStyle);

            Cell normScoreCell = row.createCell(columnIndex++);
            normScoreCell.setCellValue(hashtag.getScore());
            normScoreCell.setCellStyle(scoreCellStyle);

            rowLocalIndex++;
        }
    }

    public static void main(String[] args) {
        FunctionResearch functionResearch = new FunctionResearch();

        functionResearch.generateExcelReport();
    }
}
