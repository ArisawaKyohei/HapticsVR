package jp.sagalab.view

import jp.sagalab.jftk.Vector
import jp.sagalab.jftk.Point as tkPoint
import jp.sagalab.jftk.force.surface.FrictionSurface
import jp.sagalab.jftk.force.surface.Plane
import jp.sagalab.jftk.force.surface.Sphere
import jp.sagalab.model.AppModel
import jp.sagalab.model.ViewUpdater
import java.awt.*
import javax.swing.JPanel
import kotlin.math.round

/**
 * ３次元座標をXY, YZ, ZX平面で表示するView. (awt)
 */
class StateView2D(val model: AppModel, val type: Type = Type.XY, w: Int = 400, h: Int = 400, var label: String?) :
    JPanel(null), ViewUpdater {
    constructor(model: AppModel, type: Type, label: String?) : this(model, type, 400, 400, label)

    override fun getView(): Component = this

    // 中心座標
    private val center: Point
        get() = Point(width / 2, height / 2)

    // 画面縮尺 (±100mm)
    private val distance = 100.0

    // 画面縮尺比
    private val ratio: Double
        get() = height / (2.0 * distance)

    // 力のベクトルを見やすくしたいので適当に倍率かけて大きく表示
    private val forceScale = 30.0

    override fun requestRepaint() {
        super.repaint()
    }

    override fun paint(g: Graphics): Unit = with(g as Graphics2D) {
        // 背景塗り潰し
        color = Color(0.25f, 0.25f, 0.25f, 1f)
        fillRect(0, 0, width, height)
        // 境界線描画
        color = Color.LIGHT_GRAY
        drawRect(0, 0, width, height)
        // 軸描画
        drawAxisAndLabel(this)
        // 立体描画
        model.surfaces?.forEach { drawSurface(this, it) }

        // 点列の描画
        color = Color.YELLOW
        for (list in model.offForcePointsList) {
            list.map(this@StateView2D::transform)
                .zipWithNext { a, b -> drawLine(a.x, a.y, b.x, b.y) }
        }
        if (!model.isEnableForceCalculation) {
            model.hapticsPoints.map(this@StateView2D::transform)
                .zipWithNext { a, b -> drawLine(a.x, a.y, b.x, b.y) }
        }

        color = Color.MAGENTA
        for (list in model.onForcePointsList) {
            list.map(this@StateView2D::transform)
                .zipWithNext { a, b -> drawLine(a.x, a.y, b.x, b.y) }
        }
        if (model.isEnableForceCalculation) {
            model.hapticsPoints.map(this@StateView2D::transform)
                .zipWithNext { a, b -> drawLine(a.x, a.y, b.x, b.y) }
        }
        val hapticsPosition = transform(model.hapticsPosition)
        val forcePosition = transform(model.hapticsForce.magnify(forceScale))
        // 座標描画
        if (!model.isEnableForceCalculation) {
            color = Color.YELLOW
        } else {
            color = Color.MAGENTA
        }
        fillOval(hapticsPosition.x - 5, hapticsPosition.y - 5, 10, 10)

        // 力描画
        color = Color.CYAN
        stroke = BasicStroke(3f)
        drawLine(
            hapticsPosition.x, hapticsPosition.y,
            hapticsPosition.x + forcePosition.x, hapticsPosition.y + forcePosition.y
        )

        stroke = BasicStroke(1f)
        color = Color.MAGENTA
        model.fbcLastPoint?.also {
            val screenPoint = transform(it)
            drawOval(
                round(screenPoint.x - it.fuzziness()).toInt(), round(screenPoint.y - it.fuzziness()).toInt(),
                round(2 * it.fuzziness()).toInt(), round(2 * it.fuzziness()).toInt()
            )
        }
    }

    // 各平面への変換を行う
    private fun transform(v: Vector): Point =
        transform(v.x(), v.y(), v.z()).let { Point(it.x - center.x, it.y - center.y) }

    private fun transform(p: tkPoint): Point = transform(p.x(), p.y(), p.z())
    private fun transform(x: Int, y: Int, z: Int): Point = transform(x.toDouble(), y.toDouble(), z.toDouble())
    private fun transform(x: Double, y: Double, z: Double): Point = when (type) {
        Type.XY -> Point(center.x + (x * ratio).toInt(), center.y - (y * ratio).toInt())
        Type.YZ -> Point(center.x - (z * ratio).toInt(), center.y - (y * ratio).toInt())
        Type.ZX -> Point(center.x + (x * ratio).toInt(), center.y + (z * ratio).toInt())
    }

    // 軸描画
    private fun drawAxisAndLabel(g: Graphics2D) {
        val stroke = g.stroke
        g.stroke = BasicStroke(3f)
        val font = g.font

        label?.run {
            g.font = font.deriveFont(24f)
            g.color = Color.WHITE
            g.drawString(this, 10, 24)
        }

        g.font = font.deriveFont(40f)
        val center = transform(0, 0, 0)
        val x = transform(distance / 2, 0.0, 0.0)
        val y = transform(0.0, distance / 2, 0.0)
        val z = transform(0.0, 0.0, distance / 2)
        when (type) {
            Type.XY -> {
                g.color = Color.RED
                g.drawLine(center.x, center.y, x.x, x.y)
                g.drawString("x", x.x + 10, x.y + 10)
                g.color = Color.GREEN
                g.drawLine(center.x, center.y, y.x, y.y)
                g.drawString("y", y.x - 10, y.y - 20)
                g.color = Color.BLUE
                g.drawLine(center.x, center.y, z.x, z.y)
            }
            Type.YZ -> {
                g.color = Color.GREEN
                g.drawLine(center.x, center.y, y.x, y.y)
                g.drawString("y", y.x - 10, y.y - 20)
                g.color = Color.BLUE
                g.drawLine(center.x, center.y, z.x, z.y)
                g.drawString("z", z.x - 30, z.y + 10)
                g.color = Color.RED
                g.drawLine(center.x, center.y, x.x, x.y)
            }
            Type.ZX -> {
                g.color = Color.BLUE
                g.drawLine(center.x, center.y, z.x, z.y)
                g.drawString("z", z.x - 10, z.y + 30)
                g.color = Color.RED
                g.drawLine(center.x, center.y, x.x, x.y)
                g.drawString("x", x.x + 10, x.y + 10)
                g.color = Color.GREEN
                g.drawLine(center.x, center.y, y.x, y.y)
            }
        }
        g.stroke = stroke
        g.font = font
    }

    private fun drawSurface(g: Graphics2D, surface: FrictionSurface) {
        g.color = Color(0f, 1f, 1f, 0.2f)
        when (surface) {
            is Plane -> {
                // 未実装
            }
            is Sphere -> {
                val radius = (surface.radius() * ratio).toInt()
                val base = transform(surface.base())
                g.fillOval(base.x - radius, base.y - radius, 2 * radius, 2 * radius)
            }
        }
    }

    init {
        preferredSize = Dimension(w, h)
    }

    // 平面タイプ
    enum class Type {
        XY,
        YZ,
        ZX,
    }
}