package org.example.fitpass.domain.gym.repository;

import org.example.fitpass.domain.gym.entity.Gym;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GymRepositoryQuery {
    Page<Gym> searchGym(String keyword, String city, String district, Pageable pageable);
}
