-- Table to store upload requests
CREATE TABLE requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id VARCHAR(50) NOT NULL UNIQUE,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    file_name VARCHAR(255) NOT NULL
);

-- Table to store Excel data
CREATE TABLE excel_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id VARCHAR(50) NOT NULL,
    header1 VARCHAR(255),
    header2 VARCHAR(255),
    header3 VARCHAR(255),
    -- Add more columns as per Excel headers
    FOREIGN KEY (request_id) REFERENCES requests(request_id) ON DELETE CASCADE
);
