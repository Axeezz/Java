package com.movio.moviolab.repositories;

import com.movio.moviolab.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
}
