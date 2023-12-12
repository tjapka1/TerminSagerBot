package com.tjapka.TerminSagerBot.Repository;

import com.tjapka.TerminSagerBot.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.jar.JarEntry;

public interface UserRepository extends JpaRepository<User, Long> {

}
