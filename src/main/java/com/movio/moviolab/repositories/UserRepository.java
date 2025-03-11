package com.movio.moviolab.repositories;

import com.movio.moviolab.models.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

    List<User> findByNameIgnoreCase(String name);

    List<User> findByEmailIgnoreCase(String email);

    List<User> findByNameIgnoreCaseAndEmailIgnoreCase(String name, String email);
}
