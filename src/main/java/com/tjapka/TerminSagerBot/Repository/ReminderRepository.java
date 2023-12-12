package com.tjapka.TerminSagerBot.Repository;

import com.tjapka.TerminSagerBot.Entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    List<Reminder> findByUserId(long userId);
}
