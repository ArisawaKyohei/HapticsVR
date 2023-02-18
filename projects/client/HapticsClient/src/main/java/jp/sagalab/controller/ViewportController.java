package jp.sagalab.controller;

import jp.sagalab.haptics.EventListener;
import jp.sagalab.haptics.HapticsClient;
import jp.sagalab.jftk.Matrix;
import jp.sagalab.jftk.Vector;
import jp.sagalab.jftk.transform.TransformMatrix;
import jp.sagalab.jftk.transform.Transformable;
import jp.sagalab.model.AppModel;
import jp.sagalab.model.ForceUpdater;

public class ViewportController implements EventListener, Transformable<Matrix> {
    private final AppModel m_model;
    private final HapticsClient m_client;

    private Matrix position = Matrix.create(new double[][]{
            {0.0},
            {0.0},
            {0.0},
            {1.0}
    });

    public ViewportController(AppModel model, HapticsClient client) {
        m_model = model;
        m_client = client;
    }

    @Override
    public void position(double x, double y, double z, long time /* ns */) {
        m_model.setViewpointPosition(x, y, z);
//        position = Matrix.create(new double[][]{
//                {x},
//                {y},
//                {z},
//                {1}
//        });
//
//        Matrix worldPosition = transform(HapticsClient.getLib2WorldMatrix());
//        m_model.setViewpointPosition(worldPosition.get(0,0)*100, worldPosition.get(1,0)*100, worldPosition.get(2,0)*100); // m -> cm
//        System.out.printf("\rx = " + x + ", y = " + y + ", z = " + z +
//                " -> x = " +worldPosition.get(0,0)+ ", y = "+worldPosition.get(1,0)+", z = "+worldPosition.get(2,0));


    }

    @Override
    public void posture(double px, double py, double pz, long time) {
        m_model.setViewpointPosture(px, py, pz);
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
