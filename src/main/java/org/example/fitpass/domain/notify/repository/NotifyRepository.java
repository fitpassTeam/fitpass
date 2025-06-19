package org.example.fitpass.domain.notify.repository;

import org.example.fitpass.domain.notify.entity.Notify;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotifyRepository extends JpaRepository<Notify, Long> {

}
