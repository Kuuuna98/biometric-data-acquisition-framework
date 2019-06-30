package kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.e4.skin_temperature;

import kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.commons.SubjectTimestampId;
import kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.smartphone.gyroscope.SmartphoneGyroscope;
import kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.subject.Subject;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
public class E4SkinTemperature {

    @EmbeddedId
    private SubjectTimestampId id;

    private double value;

    E4SkinTemperature() {

    }

    public E4SkinTemperature(Subject subject, Long timestamp, double value) {
        this.id = new SubjectTimestampId(subject, timestamp);
        this.value = value;
    }


    public SubjectTimestampId getId() {
        return id;
    }

    public double getValue() {
        return value;
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
