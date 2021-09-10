package dev.helight.hopper.utilities.kvec

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.Vector

class Matrix4x4 : Cloneable {
    var m00 = 0.0
    var m01 = 0.0
    var m02 = 0.0
    var m03 = 0.0
    var m10 = 0.0
    var m11 = 0.0
    var m12 = 0.0
    var m13 = 0.0
    var m20 = 0.0
    var m21 = 0.0
    var m22 = 0.0
    var m23 = 0.0
    var m30 = 0.0
    var m31 = 0.0
    var m32 = 0.0
    var m33 = 0.0

    /**
     * Constructs a new 4x4 matrix, initialized as an identity matrix:
     * <pre>
     * 1 0 0 0
     * 0 1 0 0
     * 0 0 1 0
     * 0 0 0 1
    </pre> *
     */
    constructor() {
        setIdentity()
    }

    /**
     * Constructs a new 4x4 matrix using the 16 values specified
     */
    constructor(
        m00: Double, m01: Double, m02: Double, m03: Double,
        m10: Double, m11: Double, m12: Double, m13: Double,
        m20: Double, m21: Double, m22: Double, m23: Double,
        m30: Double, m31: Double, m32: Double, m33: Double
    ) {
        this[m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32] = m33
    }

    /**
     * Constructs a new 4x4 matrix, copying the 16 values from another matrix
     *
     * @param matrix to set to
     */
    constructor(matrix: Matrix4x4) {
        this.set(matrix)
    }

    /**
     * Sets all 16 values of this 4x4 matrix
     */
    operator fun set(
        m00: Double, m01: Double, m02: Double, m03: Double,
        m10: Double, m11: Double, m12: Double, m13: Double,
        m20: Double, m21: Double, m22: Double, m23: Double,
        m30: Double, m31: Double, m32: Double, m33: Double
    ) {
        this.m00 = m00
        this.m01 = m01
        this.m02 = m02
        this.m03 = m03
        this.m10 = m10
        this.m11 = m11
        this.m12 = m12
        this.m13 = m13
        this.m20 = m20
        this.m21 = m21
        this.m22 = m22
        this.m23 = m23
        this.m30 = m30
        this.m31 = m31
        this.m32 = m32
        this.m33 = m33
    }

    /**
     * Sets this matrix to all values of another 4x4 matrix
     *
     * @param m matrix to set to
     */
    fun set(m: Matrix4x4) {
        this[m.m00, m.m01, m.m02, m.m03, m.m10, m.m11, m.m12, m.m13, m.m20, m.m21, m.m22, m.m23, m.m30, m.m31, m.m32] =
            m.m33
    }

    /**
     * Sets this matrix to the identity matrix:<br></br>
     * <pre>
     * 1 0 0 0
     * 0 1 0 0
     * 0 0 1 0
     * 0 0 0 1
    </pre> *
     */
    fun setIdentity() {
        this[1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0] = 1.0
    }

    /**
     * Sets this matrix to all the values in an array
     *
     * @param values to set to (length 16 or less)
     */
    fun set(values: FloatArray) {
        this[values[0].toDouble(), values[1].toDouble(), values[2].toDouble(), values[3].toDouble(), values[4].toDouble(), values[5].toDouble(), values[6].toDouble(), values[7].toDouble(), values[8].toDouble(), values[9].toDouble(), values[10].toDouble(), values[11].toDouble(), values[12].toDouble(), values[13].toDouble(), values[14].toDouble()] =
            values[15].toDouble()
    }

    /**
     * Sets this matrix to all the values in an array
     *
     * @param values array to read from (length 16 or more)
     */
    fun set(values: DoubleArray) {
        this[values[0], values[1], values[2], values[3], values[4], values[5], values[6], values[7], values[8], values[9], values[10], values[11], values[12], values[13], values[14]] =
            values[15]
    }

    /**
     * Gets all 16 values from this 4x4 matrix and writes them to an array
     *
     * @param values array to write to (length 16 or more)
     */
    fun toArray(values: FloatArray) {
        values[0] = m00.toFloat()
        values[1] = m01.toFloat()
        values[2] = m02.toFloat()
        values[3] = m03.toFloat()
        values[4] = m10.toFloat()
        values[5] = m11.toFloat()
        values[6] = m12.toFloat()
        values[7] = m13.toFloat()
        values[8] = m20.toFloat()
        values[9] = m21.toFloat()
        values[10] = m22.toFloat()
        values[11] = m23.toFloat()
        values[12] = m30.toFloat()
        values[13] = m31.toFloat()
        values[14] = m32.toFloat()
        values[15] = m33.toFloat()
    }

    /**
     * Gets all 16 values from this 4x4 matrix and writes them to an array
     *
     * @param values array to write to (length 16 or more)
     */
    fun toArray(values: DoubleArray) {
        values[0] = m00
        values[1] = m01
        values[2] = m02
        values[3] = m03
        values[4] = m10
        values[5] = m11
        values[6] = m12
        values[7] = m13
        values[8] = m20
        values[9] = m21
        values[10] = m22
        values[11] = m23
        values[12] = m30
        values[13] = m31
        values[14] = m32
        values[15] = m33
    }

    /**
     * General invert routine.  Inverts m1 and places the result in "this".
     * Note that this routine handles both the "this" version and the
     * non-"this" version.
     *
     * Also note that since this routine is slow anyway, we won't worry
     * about allocating a little bit of garbage.
     */
    fun invert(): Boolean {
        // Copy source matrix to t1tmp
        val mInput = DoubleArray(16)
        val row_perm = IntArray(4)
        this.toArray(mInput)
        if (!MatrixMath.luDecomposition(mInput, row_perm)) {
            // Matrix has no inverse
            return false
        }

        // Perform back substitution on the identity matrix
        val mOutput = DoubleArray(16)
        for (i in 0..15) mOutput[i] = 0.0
        mOutput[0] = 1.0
        mOutput[5] = 1.0
        mOutput[10] = 1.0
        mOutput[15] = 1.0
        MatrixMath.luBacksubstitution(mInput, row_perm, mOutput)
        this.set(mOutput)
        return true
    }

    /**
     * Multiplies this matrix with a rotation transformation defined in a Quaternion
     *
     * @param quat to rotate with
     */
    fun rotate(quat: Quaternion) {
        val x = quat.x
        val y = quat.y
        val z = quat.z
        val w = quat.w
        val q00 = 2.0 * (-y * y + -z * z)
        val q01 = 2.0 * (x * y + -z * w)
        val q02 = 2.0 * (x * z + y * w)
        val q10 = 2.0 * (x * y + z * w)
        val q11 = 2.0 * (-x * x + -z * z)
        val q12 = 2.0 * (y * z + -x * w)
        val q20 = 2.0 * (x * z + -y * w)
        val q21 = 2.0 * (y * z + x * w)
        val q22 = 2.0 * (-x * x + -y * y)
        val a00: Double = m00 * q00 + m01 * q10 + m02 * q20
        val a01: Double = m00 * q01 + m01 * q11 + m02 * q21
        val a02: Double = m00 * q02 + m01 * q12 + m02 * q22
        val a10: Double = m10 * q00 + m11 * q10 + m12 * q20
        val a11: Double = m10 * q01 + m11 * q11 + m12 * q21
        val a12: Double = m10 * q02 + m11 * q12 + m12 * q22
        val a20: Double = m20 * q00 + m21 * q10 + m22 * q20
        val a21: Double = m20 * q01 + m21 * q11 + m22 * q21
        val a22: Double = m20 * q02 + m21 * q12 + m22 * q22
        val a30: Double = m30 * q00 + m31 * q10 + m32 * q20
        val a31: Double = m30 * q01 + m31 * q11 + m32 * q21
        val a32: Double = m30 * q02 + m31 * q12 + m32 * q22
        m00 += a00
        m01 += a01
        m02 += a02
        m10 += a10
        m11 += a11
        m12 += a12
        m20 += a20
        m21 += a21
        m22 += a22
        m30 += a30
        m31 += a31
        m32 += a32
    }

    /**
     * Multiplies this matrix with a rotation transformation about the X-axis
     *
     * @param angle the angle to rotate about the X axis in degrees
     */
    fun rotateX(angle: Double) {
        if (angle != 0.0) {
            val angleRad = Math.toRadians(angle)
            rotateX_unsafe(Math.cos(angleRad), Math.sin(angleRad))
        }
    }

    /**
     * Multiplies this matrix with a rotation transformation about the X-axis.
     * Instead of a single angle, the y and z of the rotated vector can be specified.
     *
     * @param y
     * @param z
     */
    fun rotateX(y: Double, z: Double) {
        val f: Double = KVMath.getNormalizationFactor(y, z)
        rotateX_unsafe(y * f, z * f)
    }

    private fun rotateX_unsafe(cos: Double, sin: Double) {
        val m01: Double = this.m01 * cos + this.m02 * sin
        val m02: Double = this.m01 * -sin + this.m02 * cos
        val m11: Double = this.m11 * cos + this.m12 * sin
        val m12: Double = this.m11 * -sin + this.m12 * cos
        val m21: Double = this.m21 * cos + this.m22 * sin
        val m22: Double = this.m21 * -sin + this.m22 * cos
        val m31: Double = this.m31 * cos + this.m32 * sin
        val m32: Double = this.m31 * -sin + this.m32 * cos
        this.m01 = m01
        this.m02 = m02
        this.m11 = m11
        this.m12 = m12
        this.m21 = m21
        this.m22 = m22
        this.m31 = m31
        this.m32 = m32
    }

    /**
     * Multiplies this matrix with a rotation transformation about the Y-axis
     *
     * @param angle the angle to rotate about the Y axis in degrees
     */
    fun rotateY(angle: Double) {
        if (angle != 0.0) {
            val angleRad = Math.toRadians(angle)
            rotateY_unsafe(Math.cos(angleRad), Math.sin(angleRad))
        }
    }

    /**
     * Multiplies this matrix with a rotation transformation about the Y-axis.
     * Instead of a single angle, the x and z of the rotated vector can be specified.
     *
     * @param x
     * @param z
     */
    fun rotateY(x: Double, z: Double) {
        val f: Double = KVMath.getNormalizationFactor(x, z)
        rotateY_unsafe(x * f, z * f)
    }

    private fun rotateY_unsafe(cos: Double, sin: Double) {
        val m00: Double = this.m00 * cos + this.m02 * -sin
        val m02: Double = this.m00 * sin + this.m02 * cos
        val m10: Double = this.m10 * cos + this.m12 * -sin
        val m12: Double = this.m10 * sin + this.m12 * cos
        val m20: Double = this.m20 * cos + this.m22 * -sin
        val m22: Double = this.m20 * sin + this.m22 * cos
        val m30: Double = this.m30 * cos + this.m32 * -sin
        val m32: Double = this.m30 * sin + this.m32 * cos
        this.m00 = m00
        this.m02 = m02
        this.m10 = m10
        this.m12 = m12
        this.m20 = m20
        this.m22 = m22
        this.m30 = m30
        this.m32 = m32
    }

    /**
     * Multiplies this matrix with a rotation transformation about the Z-axis
     *
     * @param angle the angle to rotate about the Z axis in degrees
     */
    fun rotateZ(angle: Double) {
        if (angle != 0.0) {
            val angleRad = Math.toRadians(angle)
            rotateZ_unsafe(Math.cos(angleRad), Math.sin(angleRad))
        }
    }

    /**
     * Multiplies this matrix with a rotation transformation about the Z-axis.
     * Instead of a single angle, the x and y of the rotated vector can be specified.
     *
     * @param x
     * @param y
     */
    fun rotateZ(x: Double, y: Double) {
        val f: Double = KVMath.getNormalizationFactor(x, y)
        rotateZ_unsafe(x * f, y * f)
    }

    private fun rotateZ_unsafe(cos: Double, sin: Double) {
        val m00: Double = this.m00 * cos + this.m01 * sin
        val m01: Double = this.m00 * -sin + this.m01 * cos
        val m10: Double = this.m10 * cos + this.m11 * sin
        val m11: Double = this.m10 * -sin + this.m11 * cos
        val m20: Double = this.m20 * cos + this.m21 * sin
        val m21: Double = this.m20 * -sin + this.m21 * cos
        val m30: Double = this.m30 * cos + this.m31 * sin
        val m31: Double = this.m30 * -sin + this.m31 * cos
        this.m00 = m00
        this.m01 = m01
        this.m10 = m10
        this.m11 = m11
        this.m20 = m20
        this.m21 = m21
        this.m30 = m30
        this.m31 = m31
    }

    /**
     * Multiplies this matrix with a rotation transformation in yaw/pitch/roll, based on the Minecraft
     * coordinate system. This will differ slightly from the standard rotateX/Y/Z functions.
     *
     * @param rotation (x=pitch, y=yaw, z=roll)
     */
    fun rotateYawPitchRoll(rotation: Vector3) {
        rotateYawPitchRoll(rotation.x, rotation.y, rotation.z)
    }

    /**
     * Multiplies this matrix with a rotation transformation in yaw/pitch/roll, based on the Minecraft
     * coordinate system. This will differ slightly from the standard rotateX/Y/Z functions.
     *
     * @param rotation (x=pitch, y=yaw, z=roll)
     */
    fun rotateYawPitchRoll(rotation: Vector) {
        rotateYawPitchRoll(rotation.x, rotation.y, rotation.z)
    }

    /**
     * Multiplies this matrix with a rotation transformation in yaw/pitch/roll, based on the Minecraft
     * coordinate system. This will differ slightly from the standard rotateX/Y/Z functions.
     *
     * @param pitch rotation (X)
     * @param yaw rotation (Y)
     * @param roll rotation (Z)
     */
    fun rotateYawPitchRoll(pitch: Double, yaw: Double, roll: Double) {
        this.rotateY(-yaw)
        this.rotateX(pitch)
        this.rotateZ(roll)
    }

    /**
     * Multiplies this matrix with a rotation transformation in yaw/pitch/roll, based on the Minecraft
     * coordinate system. This will differ slightly from the standard rotateX/Y/Z functions.
     *
     * @param pitch rotation (X)
     * @param yaw rotation (Y)
     * @param roll rotation (Z)
     */
    fun rotateYawPitchRoll(pitch: Float, yaw: Float, roll: Float) {
        this.rotateY(-yaw.toDouble())
        this.rotateX(pitch.toDouble())
        this.rotateZ(roll.toDouble())
    }

    /**
     * Gets the rotation transformation performed as a Quaternion
     *
     * @return rotation quaternion
     */
    val rotation: Quaternion
        get() {
            val tr = m00 + m11 + m22
            return if (tr > 0) {
                Quaternion(m21 - m12, m02 - m20, m10 - m01, 1.0 + tr)
            } else if ((m00 > m11) and (m00 > m22)) {
                Quaternion(1.0 + m00 - m11 - m22, m01 + m10, m02 + m20, m21 - m12)
            } else if (m11 > m22) {
                Quaternion(m01 + m10, 1.0 + m11 - m00 - m22, m12 + m21, m02 - m20)
            } else {
                Quaternion(m02 + m20, m12 + m21, 1.0 + m22 - m00 - m11, m10 - m01)
            }
        }/* == This portion is repeated and copied from getRotation() == */
    /* ============================================================ */
    /**
     * Deduces the pitch component (x) of [.getYawPitchRoll]
     *
     * @return pitch
     */
    val rotationPitch: Double
        get() {
            /* == This portion is repeated and copied from getRotation() == */
            var x: Double
            var y: Double
            var z: Double
            var w: Double
            val tr = m00 + m11 + m22
            if (tr > 0) {
                x = m21 - m12
                y = m02 - m20
                z = m10 - m01
                w = 1.0 + tr
            } else if (m00 > m11 && m00 > m22) {
                x = 1.0 + m00 - m11 - m22
                y = m01 + m10
                z = m02 + m20
                w = m21 - m12
            } else if (m11 > m22) {
                x = m01 + m10
                y = 1.0 + m11 - m00 - m22
                z = m12 + m21
                w = m02 - m20
            } else {
                x = m02 + m20
                y = m12 + m21
                z = 1.0 + m22 - m00 - m11
                w = m10 - m01
            }
            val f: Double = KVMath.getNormalizationFactor(x, y, z, w)
            x *= f
            y *= f
            z *= f
            w *= f
            /* ============================================================ */return getPitch(x, y, z, w)
        }/* == This portion is repeated and copied from getRotation() == */
    /* ============================================================ */
    /**
     * Deduces the yaw component (y) of [.getYawPitchRoll]
     *
     * @return yaw
     */
    val rotationYaw: Double
        get() {
            /* == This portion is repeated and copied from getRotation() == */
            var x: Double
            var y: Double
            var z: Double
            var w: Double
            val tr = m00 + m11 + m22
            if (tr > 0) {
                x = m21 - m12
                y = m02 - m20
                z = m10 - m01
                w = 1.0 + tr
            } else if (m00 > m11 && m00 > m22) {
                x = 1.0 + m00 - m11 - m22
                y = m01 + m10
                z = m02 + m20
                w = m21 - m12
            } else if (m11 > m22) {
                x = m01 + m10
                y = 1.0 + m11 - m00 - m22
                z = m12 + m21
                w = m02 - m20
            } else {
                x = m02 + m20
                y = m12 + m21
                z = 1.0 + m22 - m00 - m11
                w = m10 - m01
            }
            val f: Double = KVMath.getNormalizationFactor(x, y, z, w)
            x *= f
            y *= f
            z *= f
            w *= f
            /* ============================================================ */return getYaw(x, y, z, w)
        }/* == This portion is repeated and copied from getRotation() == */
    /* ============================================================ */
    /**
     * Deduces the roll component (z) of [.getYawPitchRoll]
     *
     * @return roll
     */
    val rotationRoll: Double
        get() {
            /* == This portion is repeated and copied from getRotation() == */
            var x: Double
            var y: Double
            var z: Double
            var w: Double
            val tr = m00 + m11 + m22
            if (tr > 0) {
                x = m21 - m12
                y = m02 - m20
                z = m10 - m01
                w = 1.0 + tr
            } else if (m00 > m11 && m00 > m22) {
                x = 1.0 + m00 - m11 - m22
                y = m01 + m10
                z = m02 + m20
                w = m21 - m12
            } else if (m11 > m22) {
                x = m01 + m10
                y = 1.0 + m11 - m00 - m22
                z = m12 + m21
                w = m02 - m20
            } else {
                x = m02 + m20
                y = m12 + m21
                z = 1.0 + m22 - m00 - m11
                w = m10 - m01
            }
            val f: Double = KVMath.getNormalizationFactor(x, y, z, w)
            x *= f
            y *= f
            z *= f
            w *= f
            /* ============================================================ */return getRoll(x, y, z, w)
        }/* == This portion is repeated and copied from getRotation() == */
    /* ============================================================ */
    /**
     * Deduces the yaw/pitch/roll values in degrees that this matrix transforms objects with
     *
     * @return axis rotations: {x=pitch, y=yaw, z=roll}
     */
    val yawPitchRoll: Vector
        get() {
            /* == This portion is repeated and copied from getRotation() == */
            var x: Double
            var y: Double
            var z: Double
            var w: Double
            val tr = m00 + m11 + m22
            if (tr > 0) {
                x = m21 - m12
                y = m02 - m20
                z = m10 - m01
                w = 1.0 + tr
            } else if (m00 > m11 && m00 > m22) {
                x = 1.0 + m00 - m11 - m22
                y = m01 + m10
                z = m02 + m20
                w = m21 - m12
            } else if (m11 > m22) {
                x = m01 + m10
                y = 1.0 + m11 - m00 - m22
                z = m12 + m21
                w = m02 - m20
            } else {
                x = m02 + m20
                y = m12 + m21
                z = 1.0 + m22 - m00 - m11
                w = m10 - m01
            }
            val f: Double = KVMath.getNormalizationFactor(x, y, z, w)
            x *= f
            y *= f
            z *= f
            w *= f
            /* ============================================================ */return getYawPitchRoll(x, y, z, w)!!
        }

    /**
     * Multiplies this matrix with a translation transformation
     *
     * @param translation
     */
    fun translate(translation: Vector3) {
        this.translate(translation.x, translation.y, translation.z)
    }

    /**
     * Multiplies this matrix with a translation transformation
     *
     * @param dx translation
     * @param dy translation
     * @param dz translation
     */
    fun translate(dx: Double, dy: Double, dz: Double) {
        m03 += m00 * dx + m01 * dy + m02 * dz
        m13 += m10 * dx + m11 * dy + m12 * dz
        m23 += m20 * dx + m21 * dy + m22 * dz
        m33 += m30 * dx + m31 * dy + m32 * dz
    }

    /**
     * Multiplies this matrix with a translation transformation.
     *
     * @param translation
     */
    fun translate(translation: Vector) {
        this.translate(translation.x, translation.y, translation.z)
    }

    /**
     * Multiplies this matrix with a scale transformation
     *
     * @param scale
     */
    fun scale(scale: Vector3) {
        this.scale(scale.x, scale.y, scale.z)
    }

    /**
     * Multiplies this matrix with a scale transformation
     *
     * @param sx scale
     * @param sy scale
     * @param sz scale
     */
    fun scale(sx: Double, sy: Double, sz: Double) {
        m00 *= sx
        m10 *= sx
        m20 *= sx
        m30 *= sx
        m01 *= sy
        m11 *= sy
        m21 *= sy
        m31 *= sy
        m02 *= sz
        m12 *= sz
        m22 *= sz
        m32 *= sz
    }

    /**
     * Multiplies this matrix with a scale transformation
     *
     * @param scale
     */
    fun scale(scale: Double) {
        this.scale(scale, scale, scale)
    }

    /**
     * Translates and rotates this Matrix with the position information of a Bukkit Location
     *
     * @param location to translate and rotate by
     */
    fun translateRotate(location: Location) {
        this.translateRotate(location.x, location.y, location.z, location.pitch, location.yaw)
    }

    /**
     * Translates and rotates this Matrix with position and rotation information
     *
     * @param x position
     * @param y position
     * @param z position
     * @param pitch rotation (X)
     * @param yaw rotation (Y)
     */
    fun translateRotate(x: Double, y: Double, z: Double, pitch: Float, yaw: Float) {
        this.translate(x, y, z)
        this.rotateYawPitchRoll(pitch, yaw, 0.0f)
    }

    /**
     * Translates and rotates this Matrix with position and rotation information
     *
     * @param x position
     * @param y position
     * @param z position
     * @param pitch rotation (X)
     * @param yaw rotation (Y)
     * @param roll rotation (Z)
     */
    fun translateRotate(x: Double, y: Double, z: Double, pitch: Float, yaw: Float, roll: Float) {
        this.translate(x, y, z)
        this.rotateYawPitchRoll(pitch, yaw, roll)
    }

    /**
     * Multiplies this matrix with another, storing the result in this matrix
     *
     * @param mRight the right-hand side matrix to multiply with
     */
    fun multiply(mRight: Matrix4x4) {
        multiply(this, mRight, this)
    }

    /**
     * Stores the result of a matrix multiplication in this matrix.
     * One of the input matrices is allowed to be the same instance as this matrix.
     *
     * @param mLeft Left side of the matrix multiplication
     * @param mRight Right side of the matrix multiplication
     */
    fun storeMultiply(mLeft: Matrix4x4, mRight: Matrix4x4) {
        val m00: Double = mLeft.m00 * mRight.m00 + mLeft.m01 * mRight.m10 + mLeft.m02 * mRight.m20 + mLeft.m03 * mRight.m30
        val m01: Double = mLeft.m00 * mRight.m01 + mLeft.m01 * mRight.m11 + mLeft.m02 * mRight.m21 + mLeft.m03 * mRight.m31
        val m02: Double = mLeft.m00 * mRight.m02 + mLeft.m01 * mRight.m12 + mLeft.m02 * mRight.m22 + mLeft.m03 * mRight.m32
        val m03: Double = mLeft.m00 * mRight.m03 + mLeft.m01 * mRight.m13 + mLeft.m02 * mRight.m23 + mLeft.m03 * mRight.m33
        val m10: Double = mLeft.m10 * mRight.m00 + mLeft.m11 * mRight.m10 + mLeft.m12 * mRight.m20 + mLeft.m13 * mRight.m30
        val m11: Double = mLeft.m10 * mRight.m01 + mLeft.m11 * mRight.m11 + mLeft.m12 * mRight.m21 + mLeft.m13 * mRight.m31
        val m12: Double = mLeft.m10 * mRight.m02 + mLeft.m11 * mRight.m12 + mLeft.m12 * mRight.m22 + mLeft.m13 * mRight.m32
        val m13: Double = mLeft.m10 * mRight.m03 + mLeft.m11 * mRight.m13 + mLeft.m12 * mRight.m23 + mLeft.m13 * mRight.m33
        val m20: Double = mLeft.m20 * mRight.m00 + mLeft.m21 * mRight.m10 + mLeft.m22 * mRight.m20 + mLeft.m23 * mRight.m30
        val m21: Double = mLeft.m20 * mRight.m01 + mLeft.m21 * mRight.m11 + mLeft.m22 * mRight.m21 + mLeft.m23 * mRight.m31
        val m22: Double = mLeft.m20 * mRight.m02 + mLeft.m21 * mRight.m12 + mLeft.m22 * mRight.m22 + mLeft.m23 * mRight.m32
        val m23: Double = mLeft.m20 * mRight.m03 + mLeft.m21 * mRight.m13 + mLeft.m22 * mRight.m23 + mLeft.m23 * mRight.m33
        val m30: Double = mLeft.m30 * mRight.m00 + mLeft.m31 * mRight.m10 + mLeft.m32 * mRight.m20 + mLeft.m33 * mRight.m30
        val m31: Double = mLeft.m30 * mRight.m01 + mLeft.m31 * mRight.m11 + mLeft.m32 * mRight.m21 + mLeft.m33 * mRight.m31
        val m32: Double = mLeft.m30 * mRight.m02 + mLeft.m31 * mRight.m12 + mLeft.m32 * mRight.m22 + mLeft.m33 * mRight.m32
        val m33: Double = mLeft.m30 * mRight.m03 + mLeft.m31 * mRight.m13 + mLeft.m32 * mRight.m23 + mLeft.m33 * mRight.m33
        this.m00 = m00
        this.m01 = m01
        this.m02 = m02
        this.m03 = m03
        this.m10 = m10
        this.m11 = m11
        this.m12 = m12
        this.m13 = m13
        this.m20 = m20
        this.m21 = m21
        this.m22 = m22
        this.m23 = m23
        this.m30 = m30
        this.m31 = m31
        this.m32 = m32
        this.m33 = m33
    }

    /**
     * Transforms a 3D point vector using this transformation matrix.
     * The result is written to the input point.
     *
     * @param point to transform
     */
    fun transformPoint(point: Vector) {
        val x = m00 * point.x + m01 * point.y + m02 * point.z + m03
        val y = m10 * point.x + m11 * point.y + m12 * point.z + m13
        val z = m20 * point.x + m21 * point.y + m22 * point.z + m23
        point.x = x
        point.y = y
        point.z = z
    }

    /**
     * Transforms a 3D point vector using this transformation matrix.
     * The result is written to the input point.
     *
     * @param point to transform
     */
    fun transformPoint(point: Vector3) {
        val x: Double = m00 * point.x + m01 * point.y + m02 * point.z + m03
        val y: Double = m10 * point.x + m11 * point.y + m12 * point.z + m13
        val z: Double = m20 * point.x + m21 * point.y + m22 * point.z + m23
        point.x = x
        point.y = y
        point.z = z
    }

    /**
     * Transforms a 4D point vector using this transformation matrix.
     * The result is written to the input point.
     *
     * @param point to transform
     */
    fun transformPoint(point: Vector4) {
        val x: Double = m00 * point.x + m01 * point.y + m02 * point.z + m03 * point.w
        val y: Double = m10 * point.x + m11 * point.y + m12 * point.z + m13 * point.w
        val z: Double = m20 * point.x + m21 * point.y + m22 * point.z + m23 * point.w
        val w: Double = m30 * point.x + m31 * point.y + m32 * point.z + m33 * point.w
        point.x = x
        point.y = y
        point.z = z
        point.w = w
    }

    fun inverseTransformPoint(point: Vector3) {
        val invertedOrigin = clone()
        println(invertedOrigin.toString())
        println("Could invert: " + invertedOrigin.invert())
        println(invertedOrigin.toString())
        invertedOrigin.transformPoint(point)
    }

    /*
    /**
     * Transforms all four points of a quad using this transformation matrix.
     *
     * @param quad to transform
     */
    fun transformQuad(quad: Quad) {
        transformPoint(quad.p0)
        transformPoint(quad.p1)
        transformPoint(quad.p2)
        transformPoint(quad.p3)
    }
    */

    /**
     * Obtains the absolute position vector of this matrix, equivalent to performing
     * transformPoint with a zero vector.
     *
     * @return position vector
     */
    fun toVector3(): Vector3 {
        return Vector3(m03, m13, m23)
    }

    /**
     * Obtains the absolute position vector of this matrix, equivalent to performing
     * transformPoint with a zero vector.
     *
     * @return position vector
     */
    fun toVector(): Vector {
        return Vector(m03, m13, m23)
    }

    /**
     * Obtains the absolute position vector and rotation yaw/pitch information of this matrix
     *
     * @param world The world to use for the Location
     * @return location
     */
    fun toLocation(world: World? = null): Location {
        val ypr = yawPitchRoll
        return Location(world, m03, m13, m23, ypr.y.toFloat(), ypr.x.toFloat())
    }

    public override fun clone(): Matrix4x4 {
        return Matrix4x4(this)
    }

    override fun toString(): String {
        return """{$m00, $m01, $m02, $m03
 $m10, $m11, $m12, $m13
 $m20, $m21, $m22, $m23
 $m30, $m31, $m32, $m33}"""
    }

    companion object {
        /**
         * Creates a 4x4 matrix from 3 columns of a 3x3 matrix
         *
         * @param v0 column 0
         * @param v1 column 1
         * @param v2 column 2
         * @return 4x4 matrix
         */
        fun fromColumns3x3(v0: Vector, v1: Vector, v2: Vector): Matrix4x4 {
            return Matrix4x4(
                v0.x, v1.x, v2.x, 0.0,
                v0.y, v1.y, v2.y, 0.0,
                v0.z, v1.z, v2.z, 0.0,
                0.0, 0.0, 0.0, 1.0
            )
        }

        /**
         * Creates a 4x4 matrix by using the Location information of an Entity.
         * This is equivalent to calling [.translateRotate] on an
         * identity matrix.
         *
         * @param location
         * @return transformation matrix for location
         */
        fun fromLocation(location: Location): Matrix4x4 {
            val result = Matrix4x4()
            result.translateRotate(location)
            return result
        }

        /**
         * Creates a new 4x4 identity matrix. This has the initial values:
         * <pre>
         * 1 0 0 0
         * 0 1 0 0
         * 0 0 1 0
         * 0 0 0 1
        </pre> *
         *
         * @return identity matrix
         */
        fun identity(): Matrix4x4 {
            return Matrix4x4()
        }

        /**
         * Computes the difference transformation between two matrices
         *
         * @param m1 Old transformation matrix
         * @param m2 New transformation matrix
         * @return Matrix that transforms the old matrix into the new matrix
         */
        fun diff(m1: Matrix4x4, m2: Matrix4x4): Matrix4x4 {
            val diff = m1.clone()
            diff.invert()
            diff.multiply(m2)
            return diff
        }

        /**
         * Computes the difference rotation transformation between two matrices
         *
         * @param m1 Old transformation matrix
         * @param m2 New transformation matrix
         * @return Quaternion that rotates [.getRotation] of the old matrix into the new matrix
         */
        fun diffRotation(m1: Matrix4x4, m2: Matrix4x4): Quaternion {
            return diff(m1, m2).rotation
        }

        /**
         * Multiplies two matrices together, returning a new matrix with the result.
         *
         * @param mLeft Left matrix of the matrix multiplication
         * @param mRight Right matrix of the matrix multiplication
         * @return Result of the multiplication
         */
        fun multiply(mLeft: Matrix4x4, mRight: Matrix4x4): Matrix4x4 {
            val result = Matrix4x4()
            result.storeMultiply(mLeft, mRight)
            return result
        }

        /**
         * Multiplies two matrices together, storing the result in another matrix.
         * The result matrix is allowed to be the same instance as one of the input matrices.
         *
         * @param mLeft Left matrix of the matrix multiplication
         * @param mRight Right matrix of the matrix multiplication
         * @param mResult Result of the multiplication is written to this matrix
         */
        fun multiply(mLeft: Matrix4x4, mRight: Matrix4x4, mResult: Matrix4x4) {
            mResult.storeMultiply(mLeft, mRight)
        }

        // From https://math.stackexchange.com/questions/296794
        fun computeProjectionMatrix(p: Array<Vector3>): Matrix4x4? {
            val m = Matrix4x4(
                p[0].x, p[1].x, p[2].x, 0.0,
                p[0].y, p[1].y, p[2].y, 1.0,
                p[0].z, p[1].z, p[2].z, 0.0,
                1.0, 1.0, 1.0, 0.0
            )

            //TODO: For some reason we need to add a very small value to p[3].y to avoid glitching out
            // Any reason why? Should this value be calculated from somewhere? View clipping plane?
            // Or maybe the matrix inversion simply cannot handle an y value without it.
            val p3 = Vector4(p[3].x, p[3].y + 0.001f, p[3].z, 1.0)
            val mInv = Matrix4x4(m)
            if (!mInv.invert()) {
                return null
            }
            mInv.transformPoint(p3)
            m.m00 *= p3.x
            m.m01 *= p3.y
            m.m02 *= p3.z
            m.m03 *= p3.w
            m.m10 *= p3.x
            m.m11 *= p3.y
            m.m12 *= p3.z
            m.m13 *= p3.w
            m.m20 *= p3.x
            m.m21 *= p3.y
            m.m22 *= p3.z
            m.m23 *= p3.w
            m.m30 *= p3.x
            m.m31 *= p3.y
            m.m32 *= p3.z
            m.m33 *= p3.w
            return m
        }

        // Helper function reused by Matrix4x4, portion cut out from getYawPitchRoll()
        fun getYaw(x: Double, y: Double, z: Double, w: Double): Double {
            val test = 2.0 * (w * x - y * z)
            return if (Math.abs(test) < 1.0 - 1E-15) {
                var yaw: Double = KVMath.atan2(-2.0 * (w * y + z * x), 1.0 - 2.0 * (x * x + y * y)).toDouble()
                val roll_x = 0.5 - (x * x + z * z)
                if (roll_x <= 0.0 && Math.abs(w * z + x * y) > roll_x) {
                    yaw += if (yaw < 0.0) Math.PI else -Math.PI
                }
                Math.toDegrees(yaw)
            } else if (test < 0.0) {
                Math.toDegrees(-2.0 * KVMath.atan2(z, w))
            } else {
                Math.toDegrees(2.0 * KVMath.atan2(z, w))
            }
        }

        // Helper function reused by Matrix4x4, portion cut out from getYawPitchRoll()
        fun getPitch(x: Double, y: Double, z: Double, w: Double): Double {
            val test = 2.0 * (w * x - y * z)
            return if (Math.abs(test) < 1.0 - 1E-15) {
                var pitch = Math.asin(test)
                val roll_x = 0.5 - (x * x + z * z)
                if (roll_x <= 0.0 && Math.abs(w * z + x * y) > roll_x) {
                    pitch = -pitch
                    pitch += if (pitch < 0.0) Math.PI else -Math.PI
                }
                Math.toDegrees(pitch)
            } else if (test < 0.0) {
                -90.0
            } else {
                90.0
            }
        }

        // Helper function reused by Matrix4x4, portion cut out from getYawPitchRoll()
        fun getRoll(x: Double, y: Double, z: Double, w: Double): Double {
            val test = 2.0 * (w * x - y * z)
            return if (Math.abs(test) < 1.0 - 1E-15) {
                var roll: Double = KVMath.atan2(2.0 * (w * z + x * y), 1.0 - 2.0 * (x * x + z * z)).toDouble()
                if (Math.abs(roll) > 0.5 * Math.PI) {
                    roll += if (roll < 0.0) Math.PI else -Math.PI
                }
                Math.toDegrees(roll)
            } else {
                0.0
            }
        }

        // Helper function reused by Matrix4x4
        fun getYawPitchRoll(x: Double, y: Double, z: Double, w: Double): Vector? {
            val test = 2.0 * (w * x - y * z)
            return if (Math.abs(test) < 1.0 - 1E-15) {
                // Standard angle
                var roll: Double = KVMath.atan2(2.0 * (w * z + x * y), 1.0 - 2.0 * (x * x + z * z)).toDouble()
                var pitch = Math.asin(test)
                var yaw: Double = KVMath.atan2(-2.0 * (w * y + z * x), 1.0 - 2.0 * (x * x + y * y)).toDouble()

                // This means the following:
                // roll = Math.atan2(rightVector.getY(), upVector.getY());
                // pitch = Math.asin(-forwardVector.getY());
                // yaw = Math.atan2(forwardVector.getX(), forwardVector.getZ());

                // Reduce roll if it is > 90.0 degrees
                // This can be done thanks to the otherwise annoying 'gymbal lock' effect
                // We can rotate yaw and roll with 180 degrees, and invert pitch to adjust
                // This results in the equivalent rotation
                if (Math.abs(roll) > 0.5 * Math.PI) {
                    roll += if (roll < 0.0) Math.PI else -Math.PI
                    yaw += if (yaw < 0.0) Math.PI else -Math.PI
                    pitch = -pitch
                    pitch += if (pitch < 0.0) Math.PI else -Math.PI
                }
                Vector(Math.toDegrees(pitch), Math.toDegrees(yaw), Math.toDegrees(roll))
            } else if (test < 0.0) {
                // This is at the pitch=-90.0 singularity
                // All we can do is yaw (or roll) around the vertical axis
                Vector(-90.0, Math.toDegrees(-2.0 * KVMath.atan2(z, w)), 0.0)
            } else {
                // This is at the pitch=90.0 singularity
                // All we can do is yaw (or roll) around the vertical axis
                Vector(90.0, Math.toDegrees(2.0 * KVMath.atan2(z, w)), 0.0)
            }
        }

    }
}