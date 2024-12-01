package com.vocacional.prestamoinso.Repository;

import com.vocacional.prestamoinso.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
