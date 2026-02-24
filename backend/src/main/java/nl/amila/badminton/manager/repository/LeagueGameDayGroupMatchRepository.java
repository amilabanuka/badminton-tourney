package nl.amila.badminton.manager.repository;

import nl.amila.badminton.manager.entity.LeagueGameDayGroupMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeagueGameDayGroupMatchRepository extends JpaRepository<LeagueGameDayGroupMatch, Long> {
}

