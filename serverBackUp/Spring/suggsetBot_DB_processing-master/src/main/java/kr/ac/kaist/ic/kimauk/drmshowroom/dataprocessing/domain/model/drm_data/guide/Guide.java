package kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.guide;

import kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.smartphone.gyroscope.SmartphoneGyroscope;
import kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.subject.Subject;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
public class Guide {

    @EmbeddedId
    private GuideId id;

    private long startTime;

    private long endTime;

    Guide() {

    }

    public Guide(Subject subject, int productNo, boolean isIntro, long startTime, long endTime) {
        this.id = new GuideId(subject, productNo, isIntro);
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public GuideId getId() {
        return id;
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
