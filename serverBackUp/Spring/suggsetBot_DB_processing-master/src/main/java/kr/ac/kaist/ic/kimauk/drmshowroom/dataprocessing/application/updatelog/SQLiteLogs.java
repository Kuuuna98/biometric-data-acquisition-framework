package kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.application.updatelog;

import kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.history.History;
import kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.history.HistoryType;
import kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.log.Log;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class SQLiteLogs{
    private final File zipFile;

    private final List<Log> logs;

    public SQLiteLogs(File zipFile, List<Log> logs) {
        this.zipFile = zipFile;
        this.logs = logs;
    }

    public File getZipFile() {
        return zipFile;
    }

    public List<Log> getLogs() {
        if(logs == null)
            return logs;
        return Collections.unmodifiableList(logs);
    }

    @Override
    public boolean equals(Object o) {
        if(o==null || o.getClass() != getClass())
            return false;
        if (this == o) return true;
        SQLiteLogs other = (SQLiteLogs) o;
        return other.getZipFile().equals(getZipFile());
    }

    @Override
    public int hashCode() {
        return getZipFile().hashCode();
    }

    History getHistory(){
        return new History(logs == null ? HistoryType.UPLOAD_FILE_IMPORTING_FAILED : HistoryType.UPLOAD_FILE_IMPORTING_DONE, zipFile.toString());
    }
}