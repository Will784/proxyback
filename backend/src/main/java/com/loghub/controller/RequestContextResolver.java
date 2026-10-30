package com.loghub.controller;

import com.loghub.domain.AccessContext;
import com.loghub.domain.Role;
import jakarta.servlet.http.HttpServletRequest;

/** Builds the caller identity from headers. A real deployment would derive it from a verified JWT instead. */
final class RequestContextResolver {

    static final String ROLE_HEADER = "X-Role";
    static final String CLIENT_HEADER = "X-Client-Id";
    private static final String ANONYMOUS = "anonymous";

    private RequestContextResolver() { }

    static AccessContext resolve(String roleHeader, String clientHeader, HttpServletRequest request) {
        String clientId = (clientHeader == null || clientHeader.isBlank()) ? ANONYMOUS : clientHeader.trim();
        return new AccessContext(Role.from(roleHeader), clientId, request.getRemoteAddr());
    }
}
