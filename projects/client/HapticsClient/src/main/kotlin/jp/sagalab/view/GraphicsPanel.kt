package jp.sagalab.view

import jp.sagalab.model.AppModel
import jp.sagalab.model.ViewUpdater
import java.awt.GridLayout
import javax.swing.JPanel

/**
 * XY, YZ, ZX, 3Dをまとめて表示するパネル.
 */
class GraphicsPanel(model: AppModel) : JPanel(GridLayout(2, 2)) {
    private val front = StateView2D(model, StateView2D.Type.XY, "Front")
    private val side = StateView2D(model, StateView2D.Type.YZ, "Side")
    private val top = StateView2D(model, StateView2D.Type.ZX, "Top")
    private val threeD = StateView3D(model)

    val updaters: Array<ViewUpdater> by lazy {
        arrayOf(front, side, top, threeD)
    }

    init {
        add(top.view)
        add(threeD.view)
        add(front.view)
        add(side.view)
    }
}