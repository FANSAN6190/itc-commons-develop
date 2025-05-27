//package com.itc.commons.core.services.impl;
//
//import com.amazonaws.HttpMethod;
//import com.amazonaws.auth.AWSStaticCredentialsProvider;
//import com.amazonaws.auth.BasicAWSCredentials;
//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.AmazonS3ClientBuilder;
//import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
//import com.amazonaws.services.s3.model.S3Object;
//import com.google.gson.JsonObject;
//import com.itc.commons.core.services.ItcS3BucketConfig;
//import com.itc.commons.core.services.S3FileUploadService;
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.URL;
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.List;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.usermodel.CellType;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.xssf.usermodel.XSSFSheet;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.osgi.service.component.annotations.Activate;
//import org.osgi.service.component.annotations.Component;
//import org.osgi.service.component.annotations.Modified;
//import org.osgi.service.metatype.annotations.Designate;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//
//@Component(service = S3FileUploadService.class, immediate = true)
//@Designate(ocd = ItcS3BucketConfig.class)
//public class S3FileUploadServiceImpl implements S3FileUploadService {
//
//    private static final Logger logger = LoggerFactory.getLogger(S3FileUploadServiceImpl.class);
//
//    private String s3BucketName;
//    private String s3Region;
//    private String awsAccessKey;
//    private String awsSecretKey;
//
//    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
//
//    @Activate
//    @Modified
//    protected void activate(ItcS3BucketConfig config) {
//        logger.info("Activating or modifying S3FileUploadServiceImpl with configuration.");
//        s3BucketName = config.s3BucketName();
//        s3Region = config.s3Region();
//        awsAccessKey = config.awsAccessKey();
//        awsSecretKey = config.awsSecretKey();
//        logger.debug("Configuration loaded - Bucket: {}, Region: {}, AccessKey: {}, SecretKey: {}", s3BucketName, s3Region, awsAccessKey, awsSecretKey);
//    }
//
//    @Override
//    public String updateFileInS3(JsonObject jsonObject, String brandName, String fileName, List<String> fieldNames) throws IOException {
//        logger.info("Starting to update file in S3. Brand: {}, File: {}", brandName, fileName);
//        AmazonS3 s3Client = createS3Client();
//        String filePath = StringUtils.EMPTY;
//
//        filePath = brandName.concat("/").concat(fileName);
//        logger.debug("Constructed file path: {}", filePath);
//
//        boolean fileExists = s3Client.doesObjectExist(s3BucketName, filePath);
//        logger.info("File exists in S3: {}", fileExists);
//
//        InputStream inputStream;
//        if (fileExists) {
//            logger.info("Fetching existing file from S3.");
//            S3Object s3Object = s3Client.getObject(s3BucketName, filePath);
//            inputStream = s3Object.getObjectContent();
//        } else {
//            logger.info("Creating new Excel file as it does not exist in S3.");
//            XSSFWorkbook newWorkbook = createNewExcelFile(fieldNames, fileName);
//            ByteArrayOutputStream bos = new ByteArrayOutputStream();
//            newWorkbook.write(bos);
//            newWorkbook.close();
//            inputStream = new ByteArrayInputStream(bos.toByteArray());
//        }
//
//        logger.info("Adding content to Excel workbook.");
//        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
//        InputStream is = addContentToExcel(workbook, jsonObject);
//        logger.info("Uploading updated file to S3.");
//        s3Client.putObject(s3BucketName, filePath, is, null);
//        URL signedUrl = generateSignedUrl(s3Client, filePath);
//        logger.info("Generated signed URL for the file: {}", signedUrl);
//        return signedUrl.toString();
//    }
//
//    private URL generateSignedUrl(AmazonS3 s3Client, String filePath) {
//        logger.info("Generating signed URL for file: {}", filePath);
//        Calendar expiration = Calendar.getInstance();
//        expiration.add(Calendar.MINUTE, 5);
//        GeneratePresignedUrlRequest generatePresignedUrlRequest =
//                new GeneratePresignedUrlRequest(s3BucketName, filePath)
//                        .withMethod(HttpMethod.GET)
//                        .withExpiration(expiration.getTime());
//        return s3Client.generatePresignedUrl(generatePresignedUrlRequest);
//    }
//
//    private XSSFWorkbook createNewExcelFile(List<String> fieldNames, String fileName) {
//        logger.info("Creating new Excel file with field names: {}", fieldNames);
//        XSSFWorkbook workbook = new XSSFWorkbook();
//        XSSFSheet sheet = workbook.createSheet(fileName);
//        Row headerRow = sheet.createRow(0);
//
//        headerRow.createCell(0).setCellValue("S.No");
//        for (int i = 0; i < fieldNames.size(); i++) {
//            headerRow.createCell(i + 1).setCellValue(fieldNames.get(i));
//        }
//        sheet.setColumnWidth(0, 3000);
//        for (int i = 0; i < fieldNames.size(); i++) {
//            sheet.setColumnWidth(i + 1, 6000);
//        }
//
//        return workbook;
//    }
//
//    private InputStream addContentToExcel(XSSFWorkbook workbook, JsonObject jsonObject) throws IOException {
//        logger.info("Adding content to Excel workbook.");
//        Sheet sheet = workbook.getSheetAt(0);
//        if (sheet == null) {
//            logger.error("Workbook does not contain any sheets.");
//            throw new IllegalArgumentException("Workbook does not contain any sheets.");
//        }
//
//        Row headerRow = sheet.getRow(0);
//        if (headerRow == null) {
//            logger.error("Sheet does not contain a header row.");
//            throw new IllegalArgumentException("Sheet does not contain a header row.");
//        }
//
//        Row row = createRowInExcelSheet(workbook);
//
//        for (int cellIndex = 0; cellIndex < headerRow.getLastCellNum(); cellIndex++) {
//            Cell headerCell = headerRow.getCell(cellIndex);
//            if (headerCell != null && headerCell.getCellType() == CellType.STRING) {
//                String headerName = headerCell.getStringCellValue().trim();
//                if (jsonObject.has(headerName)) {
//                    Cell cell = row.createCell(cellIndex);
//                    cell.setCellValue(jsonObject.get(headerName).getAsString());
//                }
//            }
//        }
//
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        workbook.write(bos);
//        workbook.close();
//        logger.info("Content added to Excel workbook successfully.");
//        return new ByteArrayInputStream(bos.toByteArray());
//    }
//
//    private Row createRowInExcelSheet(XSSFWorkbook workbook) {
//        logger.info("Creating new row in Excel sheet.");
//        Date currentDate = Calendar.getInstance().getTime();
//        String formattedDate = simpleDateFormat.format(currentDate);
//        XSSFSheet sheet = workbook.getSheetAt(0);
//        String serialNumber = "1";
//        int lastRowNum = sheet.getLastRowNum();
//        if (lastRowNum > 0) {
//            Row lastRow = sheet.getRow(lastRowNum);
//            String lastSerialNumber = lastRow.getCell(0).getStringCellValue();
//            serialNumber = String.valueOf(Integer.parseInt(lastSerialNumber) + 1);
//        }
//        Row row = sheet.createRow(lastRowNum + 1);
//        Cell sNoCell = row.createCell(0);
//        sNoCell.setCellValue(serialNumber);
//        Cell dateTimeCell = row.createCell(1);
//        dateTimeCell.setCellValue(formattedDate);
//        logger.debug("Created row with S.No: {} and Date: {}", serialNumber, formattedDate);
//        return row;
//    }
//
//    private AmazonS3 createS3Client() {
//        logger.info("Creating S3 client.");
//        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
//        return AmazonS3ClientBuilder.standard()
//                .withRegion(s3Region)
//                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
//                .build();
//    }
//}