package com.loghub.proxy;

import com.loghub.domain.AccessContext;
import com.loghub.domain.LogDomain;
import com.loghub.domain.Role;
import com.loghub.domain.Verdict;
import com.loghub.dto.LogRequest;
import com.loghub.dto.LogResponse;
import com.loghub.dto.LogSummary;
import com.loghub.dto.ServiceStats;
import com.loghub.exception.LogAccessDeniedException;
import com.loghub.service.LogService;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** Protection proxy: role-based access control, field masking and an audit entry for every decision. */
public final class SecurityLogProxy extends AbstractLogProxy {

    private static final Set<Role> WRITERS = EnumSet.of(Role.ADMIN, Role.SERVICE);
    private static final Set<Role> READERS = EnumSet.of(Role.ADMIN, Role.ANALYST);
    private static final String MASKED_PAYLOAD = "[PROTEGIDO: solo administradores]";

    public SecurityLogProxy(LogService delegate, AuditTrail auditTrail) {
        super(LogDomain.SECURITY, delegate, new ProxyMetrics("calls", "granted", "denied", "masked"), auditTrail);
    }

    @Override
    public String proxyKind() {
        return "Proxy de protección";
    }

    @Override
    public String responsibility() {
        return "Solo servicios y administradores escriben; solo analistas y administradores leen. "
                + "Los analistas ven la IP y el payload enmascarados.";
    }

    @Override
    protected LogResponse onSave(LogRequest request, AccessContext context) {
        authorize(WRITERS, "ESCRIBIR", context);
        return delegate.save(request, context);
    }

    @Override
    protected List<LogSummary> onFindRecent(int limit, AccessContext context) {
        authorize(READERS, "LISTAR", context);
        return delegate.findRecent(limit, context);
    }

    @Override
    protected LogResponse onFindById(Long id, AccessContext context) {
        authorize(READERS, "LEER", context);
        LogResponse response = delegate.findById(id, context);
        if (context.role() == Role.ADMIN) {
            return response;
        }
        metrics.increment("masked");
        return mask(response);
    }

    @Override
    protected ServiceStats onStats(AccessContext context) {
        authorize(READERS, "ESTADÍSTICAS", context);
        return delegate.stats(context);
    }

    private void authorize(Set<Role> allowed, String action, AccessContext context) {
        if (allowed.contains(context.role())) {
            metrics.increment("granted");
            audit(context, action, Verdict.ALLOWED, "Acceso concedido");
            return;
        }
        metrics.increment("denied");
        audit(context, action, Verdict.DENIED, "El rol " + context.role() + " no tiene permiso");
        throw new LogAccessDeniedException(
                "Tu rol (" + context.role() + ") no puede realizar esta operación en logs de seguridad.");
    }

    private LogResponse mask(LogResponse r) {
        return new LogResponse(r.id(), r.domain(), r.level(), r.source(), r.message(),
                r.payload() == null ? null : MASKED_PAYLOAD, maskIp(r.sourceIp()), r.createdAt());
    }

    static String maskIp(String ip) {
        if (ip == null || ip.isBlank()) {
            return ip;
        }
        return ip.matches("^\\d{1,3}(\\.\\d{1,3}){3}$") ? ip.replaceAll("^(\\d{1,3}\\.\\d{1,3})\\..*$", "$1.*.*") : "***";
    }
}
