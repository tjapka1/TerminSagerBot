package com.tjapka.TerminSagerBot.Entity;

import java.sql.Timestamp;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
@Builder
@Entity
@Table(name = "user")
public class User {
    @Id
    @Column(name = "user_id", unique = true)
    private Long id;
    @Column(name = "firstName")
    private String firstName;
    @Column(name = "lastName")
    private String lastName;
    @Column(name = "userName")
    private String userName;
    @Column(name = "language")
    private String language;
    @Column(name = "region")
    private String region;
    @Column(name = "birthday_fordays_time")
    private String birthdayFordaysTime;
    @Column(name = "birthday_nowday_time")
    private String birthdayNowDayTime;
    @Column(name = "termin_fordays_time")
    private String terminFordaysTime;
    @Column(name = "registeredAt")
    private Timestamp registeredAt;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Termin> termins;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Birthday> birthdays;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Reminder> reminders;



}
