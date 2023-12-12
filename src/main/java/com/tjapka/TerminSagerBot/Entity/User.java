package com.tjapka.TerminSagerBot.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.util.List;

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
    @Column(name = "registeredAt")
    private Timestamp registeredAt;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Termin> termins;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Birthday> birthdays;



}
