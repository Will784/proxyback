package com.loghub.domain;

/** Who is calling: resolved from request headers, never trusted blindly by the real services. */
public record AccessContext(Role role, String clientId, String clientIp) { }
