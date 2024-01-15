package com.tjapka.TerminSagerBot.Repository;

import com.tjapka.TerminSagerBot.Entity.Birthday;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BirthdayRepository extends JpaRepository<Birthday, Long> {
    List<Birthday> findByUserId(long userId);
}
