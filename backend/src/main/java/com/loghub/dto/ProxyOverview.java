package com.loghub.dto;

import com.loghub.domain.LogDomain;

import java.util.Map;

public record ProxyOverview(LogDomain domain, String proxyName, String proxyKind,
                            String responsibility, Map<String, Long> metrics) { }
