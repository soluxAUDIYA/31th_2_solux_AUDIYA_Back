package com.solux31th.audiya.domain.contact.repository;

import com.solux31th.audiya.domain.contact.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import com.solux31th.audiya.domain.auth.entity.User;

public interface ContactRepository extends JpaRepository<Contact, Long> {

  long countByUserAndDeletedAtIsNull(User user);
  List<Contact> findAllByUserAndDeletedAtIsNullOrderByPriorityAsc(
    User user
  );

  Optional<Contact> findByIdAndUserAndDeletedAtIsNull(
    Long id,
    User user
  );
}
