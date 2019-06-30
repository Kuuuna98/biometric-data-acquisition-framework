package kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.main.uploads;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;

@Entity(name = "uploads")
public class UploadFile implements Serializable{
    @Id
    @GeneratedValue
    private Long id;
    private String udid;
    @Column(name = "fileName")
    private String fileName;
    private String info;
    @Column(name = "reg")
    private long timestamp;

    public UploadFile() {

    }

    public String getUdid() {
        return udid;
    }

    public String getFileName() {
        return fileName;
    }

    public String getInfo() {
        return info;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if(o == null || o.getClass() != getClass())
            return false;

        if (this == o) return true;

        UploadFile others = (UploadFile) o;
        return getFileName().equals(others.getFileName());
    }

    @Override
    public int hashCode() {
        return getFileName().hashCode();
    }
}
