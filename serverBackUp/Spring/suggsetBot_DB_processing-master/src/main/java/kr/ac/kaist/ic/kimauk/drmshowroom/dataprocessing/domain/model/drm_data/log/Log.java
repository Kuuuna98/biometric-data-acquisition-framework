package kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.log;

import javax.persistence.*;

@Entity
@Table(indexes = {
        @Index(name = "index_timestamp",  columnList="timestamp", unique = false),
        @Index(name = "index_filename", columnList="fileName",     unique = false),
        @Index(name = "index_type", columnList="type", unique = false),
        @Index(name = "index_json", columnList="json", unique = false),
        @Index(name = "index_phoneNumber",  columnList="phoneNumber", unique = false),
        @Index(name = "index_phoneNumber_type",  columnList="phoneNumber, type", unique = false),
        @Index(name = "index_timestamp_filename",  columnList="timestamp, fileName", unique = false),
        @Index(name = "index_timestamp_type",  columnList="timestamp, type", unique = false)
})
public class Log implements Comparable<Log>{

    @EmbeddedId
    private LogId id;

    private String fileName;

    private String phoneNumber;

    private long sqliteId;

    public String getFileName() {
        return fileName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public long getSqliteId() {
        return sqliteId;
    }

    Log(){}

    public Log(String fileName, String phoneNumber, long sqliteId, String type, String json, long timestamp) {
        this.fileName = fileName;
        this.phoneNumber = phoneNumber;
        this.sqliteId = sqliteId;
        this.id = new LogId(timestamp, json, type);
    }

    public LogId getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Log)) return false;
        Log other = (Log) o;
        return other.getId().equals(getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public int compareTo(Log o) {
        return getId().compareTo(o.getId());
    }
}
