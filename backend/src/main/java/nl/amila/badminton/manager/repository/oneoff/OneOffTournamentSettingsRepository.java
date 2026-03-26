package nl.amila.badminton.manager.repository.oneoff;

import nl.amila.badminton.manager.entity.oneoff.OneOffTournamentSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OneOffTournamentSettingsRepository extends JpaRepository<OneOffTournamentSettings, Long> {
    Optional<OneOffTournamentSettings> findByTournamentId(Long tournamentId);
}
