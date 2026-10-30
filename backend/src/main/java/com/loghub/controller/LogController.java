package com.loghub.controller;

import com.loghub.domain.AccessContext;
import com.loghub.domain.LogDomain;
import com.loghub.dto.LogRequest;
import com.loghub.dto.LogResponse;
import com.loghub.dto.LogSummary;
import com.loghub.dto.ServiceStats;
import com.loghub.proxy.LogServiceRegistry;
import com.loghub.service.LogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Client of the Proxy pattern: depends on the LogService interface only. */
@RestController
@RequestMapping("/api/v1/logs/{domain}")
public class LogController {

    private final LogServiceRegistry registry;

    public LogController(LogServiceRegistry registry) {
        this.registry = registry;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LogResponse create(@PathVariable String domain,
                              @Valid @RequestBody LogRequest body,
                              @RequestHeader(value = RequestContextResolver.ROLE_HEADER, required = false) String role,
                              @RequestHeader(value = RequestContextResolver.CLIENT_HEADER, required = false) String clientId,
                              HttpServletRequest http) {
        AccessContext context = RequestContextResolver.resolve(role, clientId, http);
        return serviceFor(domain).save(body.withSourceIpIfAbsent(context.clientIp()), context);
    }

    @GetMapping
    public List<LogSummary> recent(@PathVariable String domain,
                                   @RequestParam(defaultValue = "20") int limit,
                                   @RequestHeader(value = RequestContextResolver.ROLE_HEADER, required = false) String role,
                                   @RequestHeader(value = RequestContextResolver.CLIENT_HEADER, required = false) String clientId,
                                   HttpServletRequest http) {
        return serviceFor(domain).findRecent(limit, RequestContextResolver.resolve(role, clientId, http));
    }

    @GetMapping("/stats")
    public ServiceStats stats(@PathVariable String domain,
                              @RequestHeader(value = RequestContextResolver.ROLE_HEADER, required = false) String role,
                              @RequestHeader(value = RequestContextResolver.CLIENT_HEADER, required = false) String clientId,
                              HttpServletRequest http) {
        return serviceFor(domain).stats(RequestContextResolver.resolve(role, clientId, http));
    }

    @GetMapping("/{id}")
    public LogResponse detail(@PathVariable String domain,
                              @PathVariable Long id,
                              @RequestHeader(value = RequestContextResolver.ROLE_HEADER, required = false) String role,
                              @RequestHeader(value = RequestContextResolver.CLIENT_HEADER, required = false) String clientId,
                              HttpServletRequest http) {
        return serviceFor(domain).findById(id, RequestContextResolver.resolve(role, clientId, http));
    }

    private LogService serviceFor(String domain) {
        return registry.serviceFor(LogDomain.from(domain));
    }
}
