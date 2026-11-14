package com.solux31th.audiya.global.sms;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// package-private: SMS 모듈 밖에서는 사용할 수 없다. 다른 도메인은 SmsService만 호출한다.
interface SmsLogRepository extends JpaRepository<SmsLog, Long> {

    Optional<SmsLog> findByRequestKey(String requestKey);
}