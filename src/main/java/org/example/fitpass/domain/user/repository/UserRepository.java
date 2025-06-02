package org.example.fitpass.domain.user.repository;

import java.util.Optional;
import org.example.fitpass.domain.user.UserRole;
import org.example.fitpass.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findById(Long userId);
    Optional<User> findByEmail(String email);

    default User findByIdOrElseThrow(Long userId) {
        User user = findById(userId).orElseThrow(
            () -> new RuntimeException("존재하지 않는 id입니다."));

        return user;
    }

}
