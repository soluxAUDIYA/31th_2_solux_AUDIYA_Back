package com.solux31th.audiya.domain.contact.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.solux31th.audiya.domain.auth.entity.User;
import com.solux31th.audiya.global.entity.BaseTimeEntity;

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
  
}