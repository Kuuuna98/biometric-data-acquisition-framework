package kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.smartphone.gyroscope;

import kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.commons.XYZ;
import kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.commons.SubjectTimestampId;
import kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.subject.Subject;

import javax.persistence.*;

@Entity
public class SmartphoneGyroscope{

    @EmbeddedId
    private SubjectTimestampId id;

    @Embedded
    private XYZ values;

    SmartphoneGyroscope() {

    }

    public SmartphoneGyroscope(Subject subject, Long timestamp, XYZ values) {
        this.id = new SubjectTimestampId(subject, timestamp);
        this.values = values;
    }


    public SubjectTimestampId getId() {
        return id;
    }

    public XYZ getValues() {
        return values;
    }

    @Override
    public boolean equals(Object o) {
        if(o == null || this.getClass() != o.getClass())
            return false;

        if (this == o)
            return true;

        SmartphoneGyroscope other = (SmartphoneGyroscope) o;
        return getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
