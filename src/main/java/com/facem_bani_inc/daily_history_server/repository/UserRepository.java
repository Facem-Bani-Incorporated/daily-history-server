package com.facem_bani_inc.daily_history_server.repository;

import com.facem_bani_inc.daily_history_server.entity.User;
import com.facem_bani_inc.daily_history_server.model.EAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByAuthProviderAndProviderUserId(EAuthProvider authProvider, String providerUserId);


    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);
}
