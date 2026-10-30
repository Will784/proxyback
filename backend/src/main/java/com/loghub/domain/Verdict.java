package com.loghub.domain;

/** Decision taken by a proxy before (or instead of) reaching the real service. */
public enum Verdict { ALLOWED, DENIED, THROTTLED, REJECTED, DUPLICATE }
