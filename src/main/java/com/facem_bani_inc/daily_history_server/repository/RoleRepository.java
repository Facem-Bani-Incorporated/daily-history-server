package com.facem_bani_inc.daily_history_server.repository;

import com.facem_bani_inc.daily_history_server.entity.Role;
import com.facem_bani_inc.daily_history_server.model.ERole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByName(ERole name);
}
