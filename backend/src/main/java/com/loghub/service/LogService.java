package com.loghub.service;

import com.loghub.domain.AccessContext;
import com.loghub.dto.LogRequest;
import com.loghub.dto.LogResponse;
import com.loghub.dto.LogSummary;
import com.loghub.dto.ServiceStats;

import java.util.List;

/**
 * Subject of the Proxy pattern. The controller (Client) only knows this interface,
 * so it cannot tell whether it talks to a real service or to the proxy guarding it.
 */
public interface LogService {

    LogResponse save(LogRequest request, AccessContext context);

    List<LogSummary> findRecent(int limit, AccessContext context);

    LogResponse findById(Long id, AccessContext context);

    ServiceStats stats(AccessContext context);
}
