package org.services;

import org.entity.UserLoginHistory;
import org.repositories.UserLoginHistoryRepo;
import org.springframework.beans.factory.annotation.Autowired;

public class UserLoginHistoryService {
    @Autowired
    private UserLoginHistoryRepo repo;

    public void persistHistory(UserLoginHistory hist) {
        repo.save(hist);
    }

    public void persistHistory(String email) {
        UserLoginHistory history = UserLoginHistory.builder()
                                    .email(email)
                                    .timestamp(new java.sql.Timestamp(System.currentTimeMillis()))
                                    .build();
        this.persistHistory(history);
    }
}
