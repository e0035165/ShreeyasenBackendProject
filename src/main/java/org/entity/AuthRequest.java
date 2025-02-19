package org.entity;

import java.util.Objects;

public record AuthRequest(String username, String password, String email) {
    public AuthRequest {
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);
        Objects.requireNonNull(email);
    }
}
