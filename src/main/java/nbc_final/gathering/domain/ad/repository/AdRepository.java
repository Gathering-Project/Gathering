package nbc_final.gathering.domain.ad.repository;

import nbc_final.gathering.domain.ad.entity.Ad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdRepository extends JpaRepository<Ad, Long>, AdQueryRepository {

}
