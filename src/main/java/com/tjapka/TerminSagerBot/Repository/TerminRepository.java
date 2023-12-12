package com.tjapka.TerminSagerBot.Repository;

import com.tjapka.TerminSagerBot.Entity.Termin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TerminRepository extends JpaRepository<Termin, Long> {

List<Termin>findByUserId(long userId);

}
