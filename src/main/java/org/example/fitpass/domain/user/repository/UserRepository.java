package org.example.fitpass.domain.user.repository;

import java.util.List;
import java.util.Optional;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.domain.user.enums.UserRole;
import org.example.fitpass.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findById(Long userId);

    default User findByIdOrElseThrow(Long userId) {
        User user = findById(userId).orElseThrow(
            () -> new BaseException(ExceptionCode.USER_NOT_FOUND));
        return user;
    }

    Optional<User> findByEmail(String email);

    default User findByEmailOrElseThrow(String email) {
        User user = findByEmail(email).orElseThrow(
            () -> new BaseException(ExceptionCode.USER_NOT_FOUND));
        return user;
    }
    List<User> findByUserRole(UserRole userRole);

}
