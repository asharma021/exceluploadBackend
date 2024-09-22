package com.example.excelupload.dao;


import com.example.excelupload.model.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RequestDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Request> rowMapper = (rs, rowNum) -> {
        Request req = new Request();
        req.setId(rs.getLong("id"));
        req.setRequestId(rs.getString("request_id"));
        req.setCreationDate(rs.getTimestamp("creation_date").toLocalDateTime());
        req.setFileName(rs.getString("file_name"));
        return req;
    };

    // Save a new request
    public int save(Request request) {
        String sql = "INSERT INTO requests (request_id, file_name) VALUES (?, ?)";
        return jdbcTemplate.update(sql, request.getRequestId(), request.getFileName());
    }

    // Retrieve all requests
    public List<Request> findAll() {
        String sql = "SELECT * FROM requests ORDER BY creation_date DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    // Find a request by request ID
    public Request findByRequestId(String requestId) {
        String sql = "SELECT * FROM requests WHERE request_id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{requestId}, rowMapper);
    }
}
