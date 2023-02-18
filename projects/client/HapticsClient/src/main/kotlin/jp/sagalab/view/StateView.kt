package jp.sagalab.view

import jp.sagalab.jftk.Point
import jp.sagalab.jftk.Vector
import jp.sagalab.model.AppModel
import jp.sagalab.model.ViewUpdater
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.JLabel
import javax.swing.JPanel

/**
 * 各種値をラベル表示するView.
 */
class StateView(private val model: AppModel, width: Int, height: Int) : JPanel(GridLayout(3, 4)), ViewUpdater {
    constructor(model: AppModel) : this(model, 400, 80)

    private val x: JLabel
    private val y: JLabel
    private val z: JLabel
    private val fx: JLabel
    private val fy: JLabel
    private val fz: JLabel

    private val px: JLabel
    private val py: JLabel
    private val pz: JLabel
    init {
        preferredSize = Dimension(width, height)
        isFocusable = false
        add(JLabel("Axis", JLabel.CENTER))
        x = add(JLabel("X", JLabel.CENTER)) as JLabel
        y = add(JLabel("Y", JLabel.CENTER)) as JLabel
        z = add(JLabel("Z", JLabel.CENTER)) as JLabel
        add(JLabel("Force Value", JLabel.CENTER))
        fx = add(JLabel("fx", JLabel.CENTER)) as JLabel
        fy = add(JLabel("fy", JLabel.CENTER)) as JLabel
        fz = add(JLabel("fz", JLabel.CENTER)) as JLabel
        fx.background = Color.PINK
        fy.background = Color.PINK
        fz.background = Color.PINK

        add(JLabel("Point", JLabel.CENTER))
        px = add(JLabel("px", JLabel.CENTER)) as JLabel
        py = add(JLabel("py", JLabel.CENTER)) as JLabel
        pz = add(JLabel("pz", JLabel.CENTER)) as JLabel

    }

    override fun getView(): Component = this

    override fun requestRepaint() {
        updateForce(model.hapticsForce, model.forceFlags, model.hapticsPosition)
    }

    private fun updateForce(v: Vector, flags: BooleanArray, p: Point) {
        this.x.isOpaque = flags[0]
        this.fx.isOpaque = flags[0]
        this.fx.foreground = if (flags[0]) Color.RED else Color.BLACK
        this.fx.text = String.format("%.4f", v.x())
        this.px.text = String.format("%.4f", p.x())
        this.y.isOpaque = flags[1]
        this.fy.isOpaque = flags[1]
        this.fy.foreground = if (flags[1]) Color.RED else Color.BLACK
        this.fy.text = String.format("%.4f", v.y())
        this.py.text = String.format("%.4f", p.y())
        this.z.isOpaque = flags[2]
        this.fz.isOpaque = flags[2]
        this.fz.foreground = if (flags[2]) Color.RED else Color.BLACK
        this.fz.text = String.format("%.4f", v.z())
        this.pz.text = String.format("%.4f", p.z())
    }
}