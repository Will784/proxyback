package com.loghub.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "log_entries",
        indexes = @Index(name = "idx_log_domain_created", columnList = "log_domain, created_at"))
public class LogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "log_domain", nullable = false, length = 20)
    private LogDomain domain;

    @Enumerated(EnumType.STRING)
    @Column(name = "log_level", nullable = false, length = 20)
    private LogLevel level;

    @Column(nullable = false, length = 80)
    private String source;

    @Column(nullable = false, length = 500)
    private String message;

    /** Heavy field: never loaded by list queries (see summary projection in the repository). */
    @Column(length = 20000)
    private String payload;

    @Column(name = "source_ip", length = 45)
    private String sourceIp;

    @Column(nullable = false, length = 64)
    private String fingerprint;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected LogEntry() { }

    public LogEntry(LogDomain domain, LogLevel level, String source, String message,
                    String payload, String sourceIp, String fingerprint, Instant createdAt) {
        this.domain = domain;
        this.level = level;
        this.source = source;
        this.message = message;
        this.payload = payload;
        this.sourceIp = sourceIp;
        this.fingerprint = fingerprint;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public LogDomain getDomain() { return domain; }
    public LogLevel getLevel() { return level; }
    public String getSource() { return source; }
    public String getMessage() { return message; }
    public String getPayload() { return payload; }
    public String getSourceIp() { return sourceIp; }
    public String getFingerprint() { return fingerprint; }
    public Instant getCreatedAt() { return createdAt; }
}
