package com.solux31th.audiya.domain.guardian.service;

import com.solux31th.audiya.domain.guardian.dto.GuardianAlertResponse;
import com.solux31th.audiya.domain.guardian.dto.GuardianSituationResponse;
import com.solux31th.audiya.domain.guardian.entity.GuardianAccessToken;

public interface GuardianQuerySource {
    GuardianAlertResponse getCurrentAlert(GuardianAccessToken token);
    GuardianSituationResponse getSituation(GuardianAccessToken token, String cursor, int limit);
}
