package com.tjapka.TerminSagerBot.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
@Builder
@Entity
@Table(name = "reminder")
public class Reminder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reminder_id", unique = true)
    private Long id;
    @Column(name = "reminder_tittle")
    private String reminderTittle;
    @Column(name = "reminder_time")
    private String reminderTime;
    @Column(name = "reminder_days")
    private String reminderDays;
    @Column(name = "reminder_min_minuts")
    private String reminderMinusMin;

    @Column(name = "created_at")
    private Timestamp createdAt;
        @ManyToOne()
    @JoinColumn(name = "user_id" )
    private User user;

}
