package com.solux31th.audiya.domain.guardian.service;
import com.solux31th.audiya.domain.guardian.entity.GuardianAccessToken;
public interface GuardianAuthorization {
    boolean isAuthorized(GuardianAccessToken token);
}
