package com.example.excelupload.controller;


import com.example.excelupload.model.ExcelData;
import com.example.excelupload.model.Request;
import com.example.excelupload.service.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RequestController {

    @Autowired
    private RequestService requestService;

    // Endpoint to upload Excel file
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String requestId = requestService.uploadFile(file);
            return ResponseEntity.ok().body("File uploaded successfully. Request ID: " + requestId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to upload file: " + e.getMessage());
        }
    }

    // Endpoint to get all requests
    @GetMapping("/requests")
    public ResponseEntity<List<Request>> getAllRequests() {
        List<Request> requests = requestService.getAllRequests();
        return ResponseEntity.ok(requests);
    }

    // Endpoint to get data by request ID
    @GetMapping("/requests/{requestId}")
    public ResponseEntity<List<ExcelData>> getRequestData(@PathVariable String requestId) {
        List<ExcelData> data = requestService.getDataByRequestId(requestId);
        return ResponseEntity.ok(data);
    }

    // Endpoint to download uploaded file
    @GetMapping("/requests/{requestId}/download")
    public ResponseEntity<?> downloadFile(@PathVariable String requestId) {
        try {
            Request request = requestService.getRequestByRequestId(requestId);
            String fileName = request.getFileName();
            InputStreamResource resource = requestService.downloadFile(requestId);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to download file: " + e.getMessage());
        }
    }
}
