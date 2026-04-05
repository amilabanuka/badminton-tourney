package nl.amila.badminton.manager.repository.apl;

import nl.amila.badminton.manager.entity.apl.AplGameDayGroupMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AplGameDayGroupMatchRepository extends JpaRepository<AplGameDayGroupMatch, Long> {
}
