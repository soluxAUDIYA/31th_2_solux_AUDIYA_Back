package com.solux31th.audiya.domain.contact.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.solux31th.audiya.domain.auth.entity.User;
import com.solux31th.audiya.global.entity.BaseTimeEntity;

@Getter
@Entity
@Table(name = "emergency_contact")

public class Contact extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "emergency_contact_id")
  private Long id;

  @Column(name = "name", nullable = false, length = 50)
  private String name;

  @Column(name = "phone_number", nullable = false, length = 15)
  private String phoneNumber;

  @Column(name = "priority", nullable = false, columnDefinition = "TINYINT")
  private Integer priority;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Column(name = "sms_enabled", nullable = false)
  private Boolean smsEnabled = true;

  @Column(name = "is_verified", nullable = false)
  private Boolean isVerified = false;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  protected Contact() {} // JPA 기본 생성자
  
  public Contact(User user, String name, String phoneNumber, Integer priority) {
    this.user = user;
    this.name = name;
    this.phoneNumber = phoneNumber;
    this.priority = priority;
  }
  
  // 연락처 수정
  public void updateContact(String name, String phoneNumber) {
    if (!this.phoneNumber.equals(phoneNumber)) {
        this.isVerified = false;
    }
    this.name = name;
    this.phoneNumber = phoneNumber;
  }

  // 연락 우선순위 변경
  public void changePriority(Integer priority) {
    this.priority = priority;
  }

  // 문자 알림 수신 여부 변경
  public void changeSmsEnabled(boolean smsEnabled) {
    this.smsEnabled = smsEnabled;
  }

  // 인증번호 확인에 성공한 뒤 호출
  public void markVerified() {
    this.isVerified = true;
  }

  // DB 행을 지우지 않고 삭제 시각 기록
  public void delete() {
    this.deletedAt = LocalDateTime.now();
  }
}