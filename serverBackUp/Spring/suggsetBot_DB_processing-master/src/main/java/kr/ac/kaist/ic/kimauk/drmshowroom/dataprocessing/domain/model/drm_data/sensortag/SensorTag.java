package kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.sensortag;

import kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.commons.SubjectTimestampId;
import kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.commons.XYZ;
import kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.subject.Subject;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.io.Serializable;

@Entity
public class SensorTag implements Serializable {

    @EmbeddedId
    private SubjectTimestampId id;

    @Embedded
    @AttributeOverrides( {
            @AttributeOverride(name="x", column = @Column(name="accelerometer_x") ),
            @AttributeOverride(name="y", column = @Column(name="accelerometer_y") ),
            @AttributeOverride(name="z", column = @Column(name="accelerometer_z") )
    } )
    private XYZ accelerometer;

    @Embedded
    @AttributeOverrides( {
            @AttributeOverride(name="x", column = @Column(name="gyroscope_x") ),
            @AttributeOverride(name="y", column = @Column(name="gyroscope_y") ),
            @AttributeOverride(name="z", column = @Column(name="gyroscope_z") )
    } )
    private XYZ gyroscope;

    @Embedded
    @AttributeOverrides( {
            @AttributeOverride(name="x", column = @Column(name="magnitude_x") ),
            @AttributeOverride(name="y", column = @Column(name="magnitude_y") ),
            @AttributeOverride(name="z", column = @Column(name="magnitude_z") )
    } )
    private XYZ magnitude;

    SensorTag() {

    }

    public SensorTag(Subject subject, Long timestamp, XYZ accelerometer, XYZ gyroscope, XYZ magnitude) {
        this.id = new SubjectTimestampId(subject, timestamp);
        this.accelerometer = accelerometer;
        this.gyroscope = gyroscope;
        this.magnitude = magnitude;
    }

    public XYZ getAccelerometer() {
        return accelerometer;
    }

    public XYZ getGyroscope() {
        return gyroscope;
    }

    public XYZ getMagnitude() {
        return magnitude;
    }

    public SubjectTimestampId getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if(o == null || this.getClass() != o.getClass())
            return false;

        if (this == o)
            return true;

        SensorTag other = (SensorTag) o;
        return getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
