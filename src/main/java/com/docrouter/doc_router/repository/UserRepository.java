package com.docrouter.doc_router.repository;

import com.docrouter.doc_router.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// JpaRepository<User, Long> provides CRUD operations for the User entity
// with Long as the primary key type — no implementation needed, Spring generates it
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data derives the query from the method name:
    // SELECT * FROM users WHERE username = ?
    Optional<User> findByUsername(String username);

    // Derived query for existence check — more efficient than findByUsername().isPresent()
    // because it uses SELECT COUNT(*) instead of loading the full entity
    boolean existsByUsername(String username);
}
