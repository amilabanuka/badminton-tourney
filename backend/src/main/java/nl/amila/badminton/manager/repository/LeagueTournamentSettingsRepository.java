package nl.amila.badminton.manager.repository;

import nl.amila.badminton.manager.entity.LeagueTournamentSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeagueTournamentSettingsRepository extends JpaRepository<LeagueTournamentSettings, Long> {
    Optional<LeagueTournamentSettings> findByTournamentId(Long tournamentId);
}
