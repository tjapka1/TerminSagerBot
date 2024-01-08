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
@Table(name = "termin")
public class Termin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "termin_id", unique = true)
    private Long id;
    @Column(name = "termin_name")
    private String terminName;
    @Column(name = "termin_date")
    private String terminDate;
    @Column(name = "termin_time")
    private String terminTime;
    @Column(name = "termin_min_minuts")
    private String terminMinusMin;
    @Column(name = "termin_min_day")
    private String terminMinusDay;
    @Column(name = "created_at")
    private Timestamp createdAt;
    @ManyToOne( )
    @JoinColumn(name = "user_id" )
    private User user;


}
