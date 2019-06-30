package kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.guide;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuideRepository extends CrudRepository<Guide, GuideId> {
}
