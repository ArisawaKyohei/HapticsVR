package jp.sagalab.controller;

import jp.sagalab.model.AppModel;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class KeyController extends KeyAdapter {
    private final AppModel m_model;

    public KeyController(AppModel model) {
        m_model = model;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
                System.exit(0);
            case KeyEvent.VK_F:
                m_model.setEnableForceCalculation(!m_model.isEnableForceCalculation());
                break;
            case KeyEvent.VK_C:
                m_model.clearPoints();
                break;
            case KeyEvent.VK_S:
                m_model.savePoints();
                break;
            case KeyEvent.VK_R:
                //m_model.randomCircle();
                m_model.randomline();
            default:
        }
    }
}
