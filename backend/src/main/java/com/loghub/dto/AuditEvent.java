package com.loghub.dto;

import com.loghub.domain.LogDomain;
import com.loghub.domain.Role;
import com.loghub.domain.Verdict;

import java.time.Instant;

public record AuditEvent(Instant at, LogDomain domain, String clientId, Role role,
                         String action, Verdict verdict, String detail) { }
