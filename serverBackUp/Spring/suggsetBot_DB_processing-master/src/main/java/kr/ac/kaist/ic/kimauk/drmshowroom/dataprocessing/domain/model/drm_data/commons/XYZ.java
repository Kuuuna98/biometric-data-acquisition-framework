package kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.commons;

import javax.persistence.Embeddable;

@Embeddable
public class XYZ {
    private double x;
    private double y;
    private double z;

    XYZ(){}

    public XYZ(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }
}
