package jp.sagalab.view

import com.jogamp.opengl.*
import com.jogamp.opengl.awt.GLCanvas
import com.jogamp.opengl.glu.GLU
import com.jogamp.opengl.util.awt.TextRenderer
import com.jogamp.opengl.util.gl2.GLUT
import jp.sagalab.jftk.Vector
import jp.sagalab.jftk.force.surface.FrictionSurface
import jp.sagalab.jftk.force.surface.Plane
import jp.sagalab.jftk.force.surface.Sphere
import jp.sagalab.model.AppModel
import jp.sagalab.model.ViewUpdater
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import kotlin.math.*

/**
 * 3次元座標を3D表示するView. (JOGL)
 */
class StateView3D(val model: AppModel, width: Int = 400, height: Int = 400) : GLEventListener, ViewUpdater {
    private val canvas: GLCanvas
    private val textRenderer = TextRenderer(Font(Font.SANS_SERIF, Font.PLAIN, 24))
    private val glu = GLU()
    private val glut = GLUT()
    private val fovy = 30.0 // (度)
    private val distance = 100.0 // (mm)

    // 力のベクトルを見やすくしたいので適当に倍率かけて大きく表示
    private val forceScale = 30.0

    override fun requestRepaint() {
        canvas.repaint()
    }

    override fun getView(): Component = canvas

    override fun init(drawable: GLAutoDrawable) {
        val gl = drawable.gl.gL2 ?: return
        gl.glClearColor(0f, 0f, 0f, 1f)
        // 深度設定
        gl.glEnable(GL.GL_DEPTH_TEST)
        // 光源設定
        gl.glEnable(GL2.GL_LIGHTING)
        gl.glEnable(GL2.GL_LIGHT0)
        // 画面外の上方から照明を当てる
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, floatArrayOf(0f, (distance * sqrt(2.0)).toFloat(), 0f, 1f), 0)
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, floatArrayOf(0.3f, 0.3f, 0.3f, 1f), 0)
        // 透過設定
        gl.glEnable(GL.GL_BLEND)
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA)
    }

    override fun display(drawable: GLAutoDrawable) {
        val gl = drawable.gl.gL2 ?: return
        // 色と深度をクリア
        gl.glClear(GL.GL_COLOR_BUFFER_BIT or GL.GL_DEPTH_BUFFER_BIT)
        // モデル空間を指定（この空間は右手系）
        gl.glMatrixMode(GL2.GL_MODELVIEW)
        // 空間の初期化
        gl.glLoadIdentity()
        // カメラの位置を指定
        gl.glRotated(model.rz, 0.0, 0.0, 1.0)
        gl.glRotated(model.ry, 0.0, 1.0, 0.0)
        gl.glRotated(model.rx, 1.0, 0.0, 0.0)

        // オブジェクトの描画
        // 目安として±distance(mm)の境界線表示
        gl.glLineWidth(1f)
        val cubeColor = floatArrayOf(1f, 1f, 1f, 1f)
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, cubeColor, 0)
        glut.glutWireCube(2 * distance.toFloat())
        // 軸の表示
        drawAxis(gl)

        // ペン先の座標表示
        val stylusSphereRadius = 5.0 * distance / 80.0
        val hapticsPosition = model.hapticsPosition
        val force = model.hapticsForce.magnify(forceScale)

        if (model.isEnableForceCalculation) {
            model.hapticsPoints.zipWithNext { a, b ->
                drawLine(gl, a.x(), a.y(), a.z(), b.x(), b.y(), b.z(), 1f, 1f, 0f, 1f)
            }
            solidSphere(
                gl,
                hapticsPosition.x(),
                hapticsPosition.y(),
                hapticsPosition.z(),
                stylusSphereRadius,
                1f,
                0f,
                1f
            )
        } else {
            model.hapticsPoints.zipWithNext { a, b ->
                drawLine(gl, a.x(), a.y(), a.z(), b.x(), b.y(), b.z(), 1f, 1f, 1f, 0f)
            }
            solidSphere(
                gl,
                hapticsPosition.x(),
                hapticsPosition.y(),
                hapticsPosition.z(),
                stylusSphereRadius,
                1f,
                1f,
                0f
            )
        }
        model.onForcePointsList.forEach {
            it.zipWithNext { a, b ->
                drawLine(gl, a.x(), a.y(), a.z(), b.x(), b.y(), b.z(), 1f, 1f, 0f, 1f)
            }
        }
        model.offForcePointsList.forEach {
            it.zipWithNext { a, b ->
                drawLine(gl, a.x(), a.y(), a.z(), b.x(), b.y(), b.z(), 1f, 1f, 1f, 0f)
            }
        }

        // 力のベクトル表示
        drawLine(
            gl, hapticsPosition.x(), hapticsPosition.y(), hapticsPosition.z(),
            hapticsPosition.x() + force.x(),
            hapticsPosition.y() + force.y(),
            hapticsPosition.z() + force.z(),
            3f, 1f, 1f, 1f
        )
        // x 方向の力の表示
        drawLine(
            gl, hapticsPosition.x(), hapticsPosition.y(), hapticsPosition.z(),
            hapticsPosition.x() + force.x(), hapticsPosition.y(), hapticsPosition.z(),
            2f, r = 1f
        )
        // y 方向の力の表示
        drawLine(
            gl, hapticsPosition.x(), hapticsPosition.y(), hapticsPosition.z(),
            hapticsPosition.x(), hapticsPosition.y() + force.y(), hapticsPosition.z(),
            2f, g = 1f
        )
        // z 方向の力の表示
        drawLine(
            gl, hapticsPosition.x(), hapticsPosition.y(), hapticsPosition.z(),
            hapticsPosition.x(), hapticsPosition.y(), hapticsPosition.z() + force.z(),
            2f, b = 1f
        )

        // 表面の描画
        model.surfaces?.forEach { drawSurface(gl, it) }

        // ラベルを表示
        textRenderer.setColor(Color.WHITE)
        textRenderer.beginRendering(canvas.width, canvas.height)
        textRenderer.draw("3D", 10, canvas.height - 24)
        textRenderer.endRendering()
    }

    override fun dispose(drawable: GLAutoDrawable) {
    }

    override fun reshape(drawable: GLAutoDrawable, x: Int, y: Int, w: Int, h: Int) {
        val gl = drawable.gl.gL2 ?: return
        gl.glViewport(x, y, w, h)
        // クリップ空間を指定（この空間は左手系）
        gl.glMatrixMode(GL2.GL_PROJECTION)
        // 空間を初期化
        gl.glLoadIdentity()
        // x:±150, y:±150, z:±150 の立方体が画面に最大限描画できることを想定してパースを設定
        // view ------ near -- org -- far
        val view2near = distance / tan(0.5 * fovy * (PI / 180))
        val view2org = ceil(view2near + distance)
        val diagonal = ceil(distance * sqrt(3.0))
        glu.gluPerspective(fovy, w / h.toDouble(), view2org - diagonal, view2org + diagonal)
        gl.glTranslated(0.0, 0.0, -view2org)
    }

    fun drawLine(
        gl: GL2,
        fromX: Double, fromY: Double, fromZ: Double,
        toX: Double, toY: Double, toZ: Double,
        width: Float = 1f,
        r: Float = 0f, g: Float = 0f, b: Float = 0f
    ) {
        // 線の太さを設定
        gl.glLineWidth(width)
        // 色を設定
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, floatArrayOf(r, g, b, 1f), 0)
        // 線の描画
        gl.glBegin(GL.GL_LINES)
        gl.glVertex3d(fromX, fromY, fromZ)
        gl.glVertex3d(toX, toY, toZ)
        gl.glEnd()
    }

    fun solidSphere(
        gl: GL2,
        x: Double, y: Double, z: Double, radius: Double,
        r: Float = 0f, g: Float = 0f, b: Float = 0f
    ) {
        // 空間を保存
        gl.glPushMatrix()
        // 表示する座標に移動
        gl.glTranslated(x, y, z)
        // 色を設定
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, floatArrayOf(r, g, b, 1f), 0)
        // 表示
        glut.glutSolidSphere(radius, 10, 10)
        // 空間の復元
        gl.glPopMatrix()
    }

    private fun drawAxis(gl: GL2) {
        // x軸の表示
        drawLine(gl, 0.0, 0.0, 0.0, distance * 0.5, 0.0, 0.0, width = 4f, r = 1f)
        // y軸の表示
        drawLine(gl, 0.0, 0.0, 0.0, 0.0, distance * 0.5, 0.0, width = 4f, g = 1f)
        // z軸の表示
        drawLine(gl, 0.0, 0.0, 0.0, 0.0, 0.0, distance * 0.5, width = 4f, b = 1f)
    }

    private fun drawSurface(gl: GL2, surface: FrictionSurface) {
        gl.glPushMatrix()
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, floatArrayOf(0f, 1f, 1f, 0.3f), 0)
        if (model.surfaceFlags?.get(surface) == true) {
            gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, floatArrayOf(1f, 1f, 0f, 0.3f), 0)
        }

        when (surface) {
            is Plane -> {
                val norm = surface.normal(null)
                val zAxis = Vector.createXYZ(0.0, 0.0, 1.0)
                val rotateAxis = zAxis.cross(norm).normalize()
                val degree = 180 * zAxis.angle(norm) / PI
                gl.glTranslated(surface.base().x(), surface.base().y(), surface.base().z())
                gl.glRotated(degree, rotateAxis.x(), rotateAxis.y(), rotateAxis.z())
                gl.glBegin(GL2.GL_POLYGON)
                gl.glVertex3d(-distance, distance, 0.0)
                gl.glVertex3d(distance, distance, 0.0)
                gl.glVertex3d(distance, -distance, 0.0)
                gl.glVertex3d(-distance, -distance, 0.0)
                gl.glEnd()
            }
            is Sphere -> {
                gl.glTranslated(surface.base().x(), surface.base().y(), surface.base().z())
                glut.glutSolidSphere(surface.radius(), 32, 32)
            }
        }
        gl.glPopMatrix()
    }

    init {
        // GLCanvas生成
        val caps = GLCapabilities(GLProfile.get(GLProfile.GL2))
        canvas = GLCanvas(caps).apply {
            preferredSize = Dimension(width, height)
            isFocusable = false
            addGLEventListener(this@StateView3D)
        }
    }
}