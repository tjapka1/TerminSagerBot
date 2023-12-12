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
    @Table(name = "birthday")
    public class Birthday {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "birthday_id", unique = true)
        private Long id;
        @Column(name = "birthday_firstname")
        private String birthdayFirstName;
        @Column(name = "birthday_lastname")
        private String birthdayLarstName;
        @Column(name = "birthday_date")
        private String birthdayDate;
        @Column(name = "created_at")
        private Timestamp createdAt;
        @ManyToOne()
        @JoinColumn(name = "user_id" )
        private User user;
}
