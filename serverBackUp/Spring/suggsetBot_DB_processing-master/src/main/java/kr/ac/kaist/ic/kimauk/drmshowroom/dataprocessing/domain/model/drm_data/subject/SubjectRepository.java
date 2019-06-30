package kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.subject;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubjectRepository extends CrudRepository<Subject, Long> {
}
