package kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.main.logs;

import kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.main.uploads.UploadFile;
import org.springframework.data.repository.CrudRepository;

public interface LogsRepository extends CrudRepository<Logs, Long> {
}
