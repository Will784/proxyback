package com.loghub.controller;

import com.loghub.dto.AuditEvent;
import com.loghub.dto.ProxyOverview;
import com.loghub.proxy.AbstractLogProxy;
import com.loghub.proxy.AuditTrail;
import com.loghub.proxy.LogServiceRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Read-only visibility into what each proxy is doing (metrics and decision history). */
@RestController
@RequestMapping("/api/v1/proxies")
public class ProxyInsightController {

    private final LogServiceRegistry registry;
    private final AuditTrail auditTrail;

    public ProxyInsightController(LogServiceRegistry registry, AuditTrail auditTrail) {
        this.registry = registry;
        this.auditTrail = auditTrail;
    }

    @GetMapping
    public List<ProxyOverview> overview() {
        return registry.all().stream().map(AbstractLogProxy::overview).toList();
    }

    @GetMapping("/audit")
    public List<AuditEvent> audit(@RequestParam(defaultValue = "30") int limit) {
        return auditTrail.latest(Math.min(limit, 100));
    }
}
