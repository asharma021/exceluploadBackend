package com.example.excelupload.service;


import com.example.excelupload.dao.ExcelDataDao;
import com.example.excelupload.dao.RequestDao;
import com.example.excelupload.model.ExcelData;
import com.example.excelupload.model.Request;
import jakarta.annotation.PostConstruct;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@Service
public class RequestService {

    @Autowired
    private RequestDao requestDao;

    @Autowired
    private ExcelDataDao excelDataDao;

    @Value("${file.upload-dir}")
    private String uploadDir;

    // Upload and process Excel file
    public String uploadFile(MultipartFile file) throws Exception {
        // Validate Excel file
        if (!isExcelFile(file)) {
            throw new Exception("Invalid file type. Please upload an Excel file.");
        }

        // Parse Excel file
        List<Map<String, String>> data = parseExcel(file);

        // Generate unique request ID
        String requestId = UUID.randomUUID().toString();

        // Save request metadata
        Request request = new Request();
        request.setRequestId(requestId);
        request.setFileName(file.getOriginalFilename());
        requestDao.save(request);

        // Save Excel data to DB
        for (Map<String, String> row : data) {
            ExcelData excelData = new ExcelData();
            excelData.setRequestId(requestId);
            excelData.setHeader1(row.get("Header1"));
            excelData.setHeader2(row.get("Header2"));
            excelData.setHeader3(row.get("Header3"));
            // Set more headers as needed
            excelDataDao.save(excelData);
        }

        // Store the uploaded file on the server
        storeFile(file, requestId);

        return requestId;
    }

    // Check if the file is an Excel file
    private boolean isExcelFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType.equals("application/vnd.ms-excel") ||
                contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    // Parse Excel file and return data as list of maps
    private List<Map<String, String>> parseExcel(MultipartFile file) throws Exception {
        List<Map<String, String>> list = new ArrayList<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            if (!rows.hasNext()) {
                throw new Exception("Excel file is empty.");
            }

            // Get headers
            Row headerRow = rows.next();
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(cell.getStringCellValue());
            }

            // Validate headers if necessary

            // Iterate through each row
            while (rows.hasNext()) {
                Row row = rows.next();
                Map<String, String> map = new HashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    cell.setCellType(CellType.STRING);
                    map.put(headers.get(i), cell.getStringCellValue());
                }
                list.add(map);
            }
        } catch (Exception e) {
            throw new Exception("Error parsing Excel file: " + e.getMessage());
        }

        return list;
    }

    // Store the uploaded file on the server
    private void storeFile(MultipartFile file, String requestId) throws Exception {
        try {
            Path path = Paths.get(uploadDir, requestId + "_" + file.getOriginalFilename());
            Files.createDirectories(path.getParent());
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new Exception("Failed to store file: " + e.getMessage());
        }
    }

    // Retrieve all requests
    public List<Request> getAllRequests() {
        return requestDao.findAll();
    }

    // Retrieve data by request ID
    public List<ExcelData> getDataByRequestId(String requestId) {
        return excelDataDao.findByRequestId(requestId);
    }

    // Retrieve request metadata by request ID
    public Request getRequestByRequestId(String requestId) {
        return requestDao.findByRequestId(requestId);
    }

    // Download file as InputStreamResource
    public InputStreamResource downloadFile(String requestId) throws Exception {
        Request request = getRequestByRequestId(requestId);
        String fileName = request.getFileName();
        Path path = Paths.get(uploadDir, requestId + "_" + fileName);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("File not found");
        }
        return new InputStreamResource(new FileInputStream(path.toFile()));
    }
    @PostConstruct
    public void init() throws IOException {
        Path path = Paths.get(uploadDir);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }
}
