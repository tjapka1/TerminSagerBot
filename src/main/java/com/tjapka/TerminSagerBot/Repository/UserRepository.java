package com.tjapka.TerminSagerBot.Repository;

import com.tjapka.TerminSagerBot.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}
