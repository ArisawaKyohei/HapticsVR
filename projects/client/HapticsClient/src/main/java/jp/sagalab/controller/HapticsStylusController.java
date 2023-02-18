package jp.sagalab.controller;

import jp.sagalab.haptics.EventListener;
import jp.sagalab.haptics.HapticsClient;
import jp.sagalab.jftk.Matrix;
import jp.sagalab.jftk.Vector;
import jp.sagalab.jftk.transform.TransformMatrix;
import jp.sagalab.jftk.transform.Transformable;
import jp.sagalab.model.ForceUpdater;
import jp.sagalab.model.AppModel;

public class HapticsStylusController implements EventListener, ForceUpdater, Transformable<Matrix> {
    private final AppModel m_model;
    private final HapticsClient m_client;

    private Matrix position = Matrix.create(new double[][]{
            {0.0},
            {0.0},
            {0.0},
            {1.0}
    });

    public HapticsStylusController(AppModel model, HapticsClient client) {
        m_model = model;
        m_client = client;
        m_model.addUpdater(this);
    }

    @Override
    public void onPressed(int buttonId, long time) {
        switch (buttonId) {
            case 0:
                m_model.setHapticsButton1Pressed(true);
                break;
            case 1:
                m_model.setHapticsButton2Pressed(true);
                break;
            default:
        }
    }

    @Override
    public void onReleased(int buttonId, long time) {
        switch (buttonId) {
            case 0:
                m_model.setHapticsButton1Pressed(false);
                break;
            case 1:
                m_model.setHapticsButton2Pressed(false);
                break;
            default:
        }
    }

    @Override
    public void position(double x, double y, double z, long time /* ns */) {
//        System.out.printf("\rz:%.3f, y:%3f, z:%3f\033[0K", x, y, z);
        position = Matrix.create(new double[][]{
                {x},
                {y},
                {z},
                {1}
        });

        Matrix worldPosition = transform(m_client.getGeo2WorldMatrix());
        m_model.setHapticsPosition(worldPosition.get(0,0), worldPosition.get(1,0), worldPosition.get(2,0), time * 1e-9);
//        System.out.printf("\rx = " + x + ", y = " + y + ", z = " + z +
//                           " -> x = " +worldPosition.get(0,0)+ ", y = "+worldPosition.get(1,0)+", z = "+worldPosition.get(2,0));
    }

    @Override
    public void force(double fx, double fy, double fz, long time) {
        m_model.setHapticsForce(fx, fy, fz);
    }

    @Override
    public void requestUpdate(Vector force) {
        Matrix geoForce = HapticsClient.getWorld2GeoMatrix().product(Matrix.create(new double[][]{
                {force.x()},
                {force.y()},
                {force.z()}
        }));
        //System.out.println(geoForce.get(0,0)+", "+geoForce.get(1,0)+", "+geoForce.get(2,0));
//        System.out.printf("\r"+force + " -> x = "+geoForce.get(0,0)+" y = " +geoForce.get(1,0)+" z = " +geoForce.get(2,0));
        m_client.sendForceEvent(geoForce.get(0,0), geoForce.get(1,0), geoForce.get(2,0));
//        m_client.sendForceEvent(0,0, 0);
    }


    @Override
    public Matrix transform(TransformMatrix _matrix) {
        double x = _matrix.get(0,0) * position.get(0,0)
                 + _matrix.get(0,1) * position.get(1,0)
                 + _matrix.get(0,2) * position.get(2,0)
                 + _matrix.get(0,3) * position.get(3,0);
        double y = _matrix.get(1,0) * position.get(0,0)
                + _matrix.get(1,1) * position.get(1,0)
                + _matrix.get(1,2) * position.get(2,0)
                + _matrix.get(1,3) * position.get(3,0);
        double z = _matrix.get(2,0) * position.get(0,0)
                + _matrix.get(2,1) * position.get(1,0)
                + _matrix.get(2,2) * position.get(2,0)
                + _matrix.get(2,3) * position.get(3,0);
        double w = _matrix.get(3,0) * position.get(0,0)
                + _matrix.get(3,1) * position.get(1,0)
                + _matrix.get(3,2) * position.get(2,0)
                + _matrix.get(3,3) * position.get(3,0);
        return Matrix.create(new double[][]{
                {x},
                {y},
                {z},
                {1}
        });
    }
}
