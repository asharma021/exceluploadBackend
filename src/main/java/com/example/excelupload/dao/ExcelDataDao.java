package com.example.excelupload.dao;


import com.example.excelupload.model.ExcelData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ExcelDataDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<ExcelData> rowMapper = (rs, rowNum) -> {
        ExcelData data = new ExcelData();
        data.setId(rs.getLong("id"));
        data.setRequestId(rs.getString("request_id"));
        data.setHeader1(rs.getString("header1"));
        data.setHeader2(rs.getString("header2"));
        data.setHeader3(rs.getString("header3"));
        // Set more headers as needed
        return data;
    };

    // Save Excel data
    public int save(ExcelData data) {
        String sql = "INSERT INTO excel_data (request_id, header1, header2, header3) VALUES (?, ?, ?, ?)";
        return jdbcTemplate.update(sql, data.getRequestId(), data.getHeader1(), data.getHeader2(), data.getHeader3());
    }

    // Retrieve data by request ID
    public List<ExcelData> findByRequestId(String requestId) {
        String sql = "SELECT * FROM excel_data WHERE request_id = ?";
        return jdbcTemplate.query(sql, new Object[]{requestId}, rowMapper);
    }
}
