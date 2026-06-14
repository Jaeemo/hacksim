package com.capstone.backend;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetonationRunRepository extends JpaRepository<DetonationRun, Long> {

    List<DetonationRun> findTop50ByOrderByRequestedAtDesc();
}
