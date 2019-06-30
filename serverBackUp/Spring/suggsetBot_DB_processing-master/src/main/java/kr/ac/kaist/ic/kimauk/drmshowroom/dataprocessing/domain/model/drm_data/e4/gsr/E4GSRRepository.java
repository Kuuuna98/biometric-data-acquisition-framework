package kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.e4.gsr;

import kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.commons.SubjectTimestampId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface E4GSRRepository extends CrudRepository<E4GSR, SubjectTimestampId> {
}
