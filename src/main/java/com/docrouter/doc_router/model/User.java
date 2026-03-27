package com.docrouter.doc_router.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// @Entity marks this class as a JPA entity — Hibernate will map it to a database table
@Entity
// @Table specifies the table name — "users" instead of the default "user",
// which is a reserved keyword in PostgreSQL
@Table(name = "users")
public class User {

    // @Id marks this field as the primary key
    @Id
    // @GeneratedValue with IDENTITY lets PostgreSQL auto-increment the id via SERIAL
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Column(unique = true) adds a unique constraint — prevents duplicate usernames at the DB level
    @Column(unique = true, nullable = false)
    private String username;

    // Stores the BCrypt hash, never the plaintext password
    @Column(nullable = false)
    private String passwordHash;

    // JPA requires a no-arg constructor for entity instantiation via reflection
    protected User() {}

    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
