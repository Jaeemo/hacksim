package com.capstone.backend;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostureReportRepository extends JpaRepository<PostureReport, Long> {

    List<PostureReport> findTop20ByOrderByReportedAtDesc();
}
