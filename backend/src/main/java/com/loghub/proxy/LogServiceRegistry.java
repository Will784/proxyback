package com.loghub.proxy;

import com.loghub.domain.LogDomain;
import com.loghub.service.LogService;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Lookup of the proxy that fronts each domain. Controllers never see the real services. */
public final class LogServiceRegistry {

    private final Map<LogDomain, AbstractLogProxy> proxies = new EnumMap<>(LogDomain.class);

    public LogServiceRegistry(List<AbstractLogProxy> registered) {
        registered.forEach(proxy -> proxies.put(proxy.domain(), proxy));
    }

    public LogService serviceFor(LogDomain domain) {
        return proxies.get(domain);
    }

    public Collection<AbstractLogProxy> all() {
        return Collections.unmodifiableCollection(proxies.values());
    }
}
