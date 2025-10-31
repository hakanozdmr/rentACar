-- Audit Logging System Migration Script
-- Creates audit_logs table for comprehensive system auditing

-- Create audit_logs table
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    entity_name VARCHAR(100) NOT NULL,
    entity_id BIGINT,
    action_type VARCHAR(50) NOT NULL,
    user_id BIGINT,
    username VARCHAR(100),
    user_email VARCHAR(255),
    ip_address VARCHAR(45),
    user_agent VARCHAR(1000),
    request_method VARCHAR(10),
    request_url VARCHAR(1000),
    old_values TEXT,
    new_values TEXT,
    changed_fields VARCHAR(2000),
    session_id VARCHAR(255),
    operation_result VARCHAR(50),
    error_message VARCHAR(2000),
    execution_time_ms BIGINT,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    additional_info TEXT,
    created_by VARCHAR(255),
    created_date TIMESTAMP,
    update_by VARCHAR(255),
    update_date TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_audit_logs_entity_name ON audit_logs(entity_name);
CREATE INDEX IF NOT EXISTS idx_audit_logs_entity_id ON audit_logs(entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_action_type ON audit_logs(action_type);
CREATE INDEX IF NOT EXISTS idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_username ON audit_logs(username);
CREATE INDEX IF NOT EXISTS idx_audit_logs_timestamp ON audit_logs(timestamp);
CREATE INDEX IF NOT EXISTS idx_audit_logs_operation_result ON audit_logs(operation_result);
CREATE INDEX IF NOT EXISTS idx_audit_logs_ip_address ON audit_logs(ip_address);

-- Create composite indexes for common queries
CREATE INDEX IF NOT EXISTS idx_audit_logs_entity_name_id ON audit_logs(entity_name, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_timestamp_entity ON audit_logs(timestamp DESC, entity_name);
CREATE INDEX IF NOT EXISTS idx_audit_logs_user_timestamp ON audit_logs(user_id, timestamp DESC);

-- Add comments for documentation
COMMENT ON TABLE audit_logs IS 'Sistem audit logları - Tüm önemli işlemlerin takibi için';
COMMENT ON COLUMN audit_logs.entity_name IS 'İşlem yapılan varlık türü (Car, Customer, Reservation vb.)';
COMMENT ON COLUMN audit_logs.entity_id IS 'İşlem yapılan varlığın ID si';
COMMENT ON COLUMN audit_logs.action_type IS 'Yapılan işlem türü (CREATE, UPDATE, DELETE, READ vb.)';
COMMENT ON COLUMN audit_logs.user_id IS 'İşlemi yapan kullanıcının ID si';
COMMENT ON COLUMN audit_logs.username IS 'İşlemi yapan kullanıcının adı';
COMMENT ON COLUMN audit_logs.ip_address IS 'İşlemi yapan kullanıcının IP adresi';
COMMENT ON COLUMN audit_logs.old_values IS 'Güncelleme öncesi değerler (JSON format)';
COMMENT ON COLUMN audit_logs.new_values IS 'Güncelleme sonrası değerler (JSON format)';
COMMENT ON COLUMN audit_logs.changed_fields IS 'Değişen alanların listesi';
COMMENT ON COLUMN audit_logs.operation_result IS 'İşlem sonucu (SUCCESS, FAILURE, ERROR)';
COMMENT ON COLUMN audit_logs.execution_time_ms IS 'İşlem süresi (milisaniye)';
COMMENT ON COLUMN audit_logs.timestamp IS 'İşlem zamanı';

-- Grant permissions (adjust based on your user)
-- GRANT SELECT, INSERT, UPDATE, DELETE ON audit_logs TO your_app_user;
-- GRANT USAGE, SELECT ON SEQUENCE audit_logs_id_seq TO your_app_user;


