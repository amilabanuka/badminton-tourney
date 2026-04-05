package nl.amila.badminton.manager.repository.apl;

import nl.amila.badminton.manager.entity.apl.AplTournamentSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AplTournamentSettingsRepository extends JpaRepository<AplTournamentSettings, Long> {
    Optional<AplTournamentSettings> findByTournamentId(Long tournamentId);
}
