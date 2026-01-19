-- OpenOLAT Forum Enhancements Migration Script
-- Adds support for Q&A thread type with best answers and abuse reporting

-- 1. Add best answer support to forum messages
ALTER TABLE o_message ADD COLUMN is_best_answer BOOLEAN NOT NULL DEFAULT FALSE;

-- Add index for performance when querying best answers in threads
CREATE INDEX idx_msg_best_answer ON o_message(topthread_id, is_best_answer) WHERE is_best_answer = TRUE;

-- 2. Create abuse report table
CREATE TABLE o_fo_abuse_report (
    id BIGINT NOT NULL,
    creationdate TIMESTAMP NOT NULL,
    message_id BIGINT NOT NULL,
    reporter_id BIGINT NOT NULL,
    reason VARCHAR(4000) NOT NULL,
    status VARCHAR(32) NOT NULL,
    resolution_date TIMESTAMP NULL,
    resolved_by_id BIGINT NULL,
    PRIMARY KEY (id)
);

-- Add foreign keys
ALTER TABLE o_fo_abuse_report ADD CONSTRAINT fk_abuse_msg FOREIGN KEY (message_id) REFERENCES o_message(message_id);
ALTER TABLE o_fo_abuse_report ADD CONSTRAINT fk_abuse_reporter FOREIGN KEY (reporter_id) REFERENCES o_bs_identity(id);
ALTER TABLE o_fo_abuse_report ADD CONSTRAINT fk_abuse_resolver FOREIGN KEY (resolved_by_id) REFERENCES o_bs_identity(id);

-- Add indexes for performance
CREATE INDEX idx_abuse_report_msg ON o_fo_abuse_report(message_id);
CREATE INDEX idx_abuse_report_status ON o_fo_abuse_report(status, creationdate);
CREATE INDEX idx_abuse_report_reporter ON o_fo_abuse_report(reporter_id, message_id);

-- Comments explaining the schema
COMMENT ON COLUMN o_message.is_best_answer IS 'Indicates if this message is marked as the best answer in a Q&A thread';
COMMENT ON TABLE o_fo_abuse_report IS 'Stores abuse reports for forum messages to streamline moderation';
COMMENT ON COLUMN o_fo_abuse_report.status IS 'Status values: PENDING, REVIEWED, DISMISSED, ACTION_TAKEN';
