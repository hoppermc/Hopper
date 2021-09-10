package dev.helight.hopper.utilities.kvec

import org.bukkit.util.Vector

/**
 * A quaternion for performing rotations in 3D space.
 * The quaternion is automatically normalized.
 */
class Quaternion : Cloneable {
    var x: Double
        private set
    var y: Double
        private set
    var z: Double
        private set
    var w: Double
        private set

    constructor() {
        x = 0.0
        y = 0.0
        z = 0.0
        w = 1.0
    }

    constructor(quat: Quaternion) {
        x = quat.x
        y = quat.y
        z = quat.z
        w = quat.w
    }

    constructor(x: Double, y: Double, z: Double, w: Double) {
        this.x = x
        this.y = y
        this.z = z
        this.w = w
        this.normalize()
    }

    /**
     * Sets this Quaternion to the identity quaternion (0,0,0,1)
     */
    fun setIdentity() {
        x = 0.0
        y = 0.0
        z = 0.0
        w = 1.0
    }

    /**
     * Sets this Quaternion to the values of another Quaternion
     *
     * @param q to set to
     */
    fun setTo(q: Quaternion) {
        x = q.x
        y = q.y
        z = q.z
        w = q.w
    }

    /**
     * Calculates the dot product of this Quaternion with another
     *
     * @param q other quaternion
     * @return dot product
     */
    fun dot(q: Quaternion): Double {
        return x * q.x + y * q.y + z * q.z + w * q.w
    }

    /**
     * Transforms a point, applying the rotation of this quaternion with 0,0,0 as origin.
     *
     * @param point to rotate using this quaternion
     */
    fun transformPoint(point: Vector) {
        val px: Double = point.getX()
        val py: Double = point.getY()
        val pz: Double = point.getZ()
        point.setX(px + 2.0 * (px * (-y * y - z * z) + py * (x * y - z * w) + pz * (x * z + y * w)))
        point.setY(py + 2.0 * (px * (x * y + z * w) + py * (-x * x - z * z) + pz * (y * z - x * w)))
        point.setZ(pz + 2.0 * (px * (x * z - y * w) + py * (y * z + x * w) + pz * (-x * x - y * y)))
    }

    /**
     * Retrieves the right vector, which is the result of transforming a (1,0,0) point
     * with this Quaternion.
     *
     * @return right vector
     */
    fun rightVector(): Vector {
        return Vector(1.0 + 2.0 * (-y * y - z * z), 2.0 * (x * y + z * w), 2.0 * (x * z - y * w))
    }

    /**
     * Retrieves the up vector, which is the result of transforming a (0,1,0) point
     * with this Quaternion.
     *
     * @return up vector
     */
    fun upVector(): Vector {
        return Vector(2.0 * (x * y - z * w), 1.0 + 2.0 * (-x * x - z * z), 2.0 * (y * z + x * w))
    }

    /**
     * Retrieves the forward vector, which is the result of transforming a (0,0,1) point
     * with this Quaternion.
     *
     * @return forward vector
     */
    fun forwardVector(): Vector {
        return Vector(2.0 * (x * z + y * w), 2.0 * (y * z - x * w), 1.0 + 2.0 * (-x * x - y * y))
    }

    /**
     * Divides this quaternion by another quaternion. This operation is equivalent to multiplying
     * with the quaternion after calling [.invert] on it.
     *
     * @param quat to divide with
     */
    fun divide(quat: Quaternion) {
        val x = w * -quat.x + x * quat.w + y * -quat.z - z * -quat.y
        val y = w * -quat.y + y * quat.w + z * -quat.x - this.x * -quat.z
        val z = w * -quat.z + z * quat.w + this.x * -quat.y - this.y * -quat.x
        val w = w * quat.w - this.x * -quat.x - this.y * -quat.y - this.z * -quat.z
        this.x = x
        this.y = y
        this.z = z
        this.w = w
        this.normalize()
    }

    /**
     * Multiplies this quaternion with another quaternion. The result is stored in this quaternion.
     *
     * @param quat to multiply with
     */
    fun multiply(quat: Quaternion) {
        val x = w * quat.x + x * quat.w + y * quat.z - z * quat.y
        val y = w * quat.y + y * quat.w + z * quat.x - this.x * quat.z
        val z = w * quat.z + z * quat.w + this.x * quat.y - this.y * quat.x
        val w = w * quat.w - this.x * quat.x - this.y * quat.y - this.z * quat.z
        this.x = x
        this.y = y
        this.z = z
        this.w = w
        this.normalize()
    }

    /**
     * Multiplies this Quaternion with a rotation around an axis
     *
     * @param axis vector
     * @param angleDegrees to rotate in degrees
     */
    fun rotateAxis(axis: Vector, angleDegrees: Double) {
        rotateAxis(axis.getX(), axis.getY(), axis.getZ(), angleDegrees)
    }

    /**
     * Multiplies this Quaternion with a rotation around an axis
     *
     * @param axisX vector coordinate
     * @param axisY vector coordinate
     * @param axisZ vector coordinate
     * @param angleDegrees to rotate in degrees
     */
    fun rotateAxis(axisX: Double, axisY: Double, axisZ: Double, angleDegrees: Double) {
        this.multiply(fromAxisAngles(axisX, axisY, axisZ, angleDegrees))
    }

    /**
     * Multiplies this quaternion with a rotation transformation in yaw/pitch/roll, based on the Minecraft
     * coordinate system. This will differ slightly from the standard rotateX/Y/Z functions.
     *
     * @param rotation (x=pitch, y=yaw, z=roll)
     */
    fun rotateYawPitchRoll(rotation: Vector3) {
        rotateYawPitchRoll(rotation.x, rotation.y, rotation.z)
    }

    /**
     * Multiplies this quaternion with a rotation transformation in yaw/pitch/roll, based on the Minecraft
     * coordinate system. This will differ slightly from the standard rotateX/Y/Z functions.
     *
     * @param rotation (x=pitch, y=yaw, z=roll)
     */
    fun rotateYawPitchRoll(rotation: Vector) {
        rotateYawPitchRoll(rotation.getX(), rotation.getY(), rotation.getZ())
    }

    /**
     * Multiplies this quaternion with a rotation transformation in yaw/pitch/roll, based on the Minecraft
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
     * Deduces the yaw/pitch/roll values in degrees that this quaternion transforms objects with
     *
     * @return axis rotations: {x=pitch, y=yaw, z=roll}
     */
    val yawPitchRoll: Vector
        get() = getYawPitchRoll(x, y, z, w)

    /**
     * Deduces the pitch component (x) of [.getYawPitchRoll]
     *
     * @return pitch
     */
    val pitch: Double
        get() = getPitch(x, y, z, w)

    /**
     * Deduces the yaw component (y) of [.getYawPitchRoll]
     *
     * @return yaw
     */
    val yaw: Double
        get() = getYaw(x, y, z, w)

    /**
     * Deduces the roll component (z) of [.getYawPitchRoll]
     *
     * @return roll
     */
    val roll: Double
        get() = getRoll(x, y, z, w)

    /**
     * Rotates the Quaternion 180 degrees around the x-axis
     */
    fun rotateXFlip() {
        rotateX_unsafe(0.0, 1.0)
    }

    /**
     * Rotates the Quaternion an angle around the x-axis
     *
     * @param angleDegrees to rotate
     */
    fun rotateX(angleDegrees: Double) {
        if (angleDegrees != 0.0) {
            val r = 0.5 * Math.toRadians(angleDegrees)
            rotateX_unsafe(Math.cos(r), Math.sin(r))
        }
    }

    /**
     * Rotates the Quaternion an angle around the X-axis, the angle defined by the y/z vector.
     * This is equivalent to calling [.rotateX] using [Math.atan2].
     *
     * @param y
     * @param z
     */
    fun rotateX(y: Double, z: Double) {
        val r = halfcosatan2(z, y)
        rotateX_unsafe(Math.sqrt(0.5 + r), Math.sqrt(0.5 - r))
    }

    private fun rotateX_unsafe(fy: Double, fz: Double) {
        val x = x * fy + w * fz
        val y = y * fy + z * fz
        val z = z * fy - this.y * fz
        val w = w * fy - this.x * fz
        this.x = x
        this.y = y
        this.z = z
        this.w = w
        this.normalize()
    }

    /**
     * Rotates the Quaternion 180 degrees around the y-axis
     */
    fun rotateYFlip() {
        rotateY_unsafe(0.0, 1.0)
    }

    /**
     * Rotates the Quaternion an angle around the y-axis
     *
     * @param angleDegrees to rotate
     */
    fun rotateY(angleDegrees: Double) {
        if (angleDegrees != 0.0) {
            val r = 0.5 * Math.toRadians(angleDegrees)
            rotateY_unsafe(Math.cos(r), Math.sin(r))
        }
    }

    /**
     * Rotates the Quaternion an angle around the y-axis, the angle defined by the x/z vector.
     * This is equivalent to calling [.rotateY] using [Math.atan2].
     *
     * @param x
     * @param z
     */
    fun rotateY(x: Double, z: Double) {
        val r = halfcosatan2(z, x)
        rotateY_unsafe(Math.sqrt(0.5 + r), Math.sqrt(0.5 - r))
    }

    private fun rotateY_unsafe(fx: Double, fz: Double) {
        val x = x * fx - z * fz
        val y = y * fx + w * fz
        val z = z * fx + this.x * fz
        val w = w * fx - this.y * fz
        this.x = x
        this.y = y
        this.z = z
        this.w = w
        this.normalize()
    }

    /**
     * Rotates the Quaternion 180 degrees around the z-axis
     */
    fun rotateZFlip() {
        rotateZ_unsafe(0.0, 1.0)
    }

    /**
     * Rotates the Quaternion an angle around the z-axis
     *
     * @param angleDegrees to rotate
     */
    fun rotateZ(angleDegrees: Double) {
        if (angleDegrees != 0.0) {
            val r = 0.5 * Math.toRadians(angleDegrees)
            rotateZ_unsafe(Math.cos(r), Math.sin(r))
        }
    }

    /**
     * Rotates the Quaternion an angle around the z-axis, the angle defined by the x/y vector.
     * This is equivalent to calling [.rotateZ] using [Math.atan2].
     *
     * @param x
     * @param y
     */
    fun rotateZ(x: Double, y: Double) {
        val r = halfcosatan2(y, x)
        rotateZ_unsafe(Math.sqrt(0.5 + r), Math.sqrt(0.5 - r))
    }

    private fun rotateZ_unsafe(fx: Double, fy: Double) {
        val x = x * fx + y * fy
        val y = y * fx - this.x * fy
        val z = z * fx + w * fy
        val w = w * fx - this.z * fy
        this.x = x
        this.y = y
        this.z = z
        this.w = w
        this.normalize()
    }

    /**
     * Converts the rotation transformations defined in this quaternion to a
     * 4x4 transformation matrix. This is as if the unit matrix was multiplied
     * with this quaternion.
     *
     * @return 4x4 transformation matrix.
     */
    fun toMatrix4x4(): Matrix4x4 {
        return Matrix4x4(
            1.0 - 2.0 * y * y - 2.0 * z * z, 2.0 * x * y - 2.0 * z * w, 2.0 * x * z + 2.0 * y * w, 0.0,
            2.0 * x * y + 2.0 * z * w, 1.0 - 2.0 * x * x - 2.0 * z * z, 2.0 * y * z - 2.0 * x * w, 0.0,
            2.0 * x * z - 2.0 * y * w, 2.0 * y * z + 2.0 * x * w, 1.0 - 2.0 * x * x - 2.0 * y * y, 0.0,
            0.0, 0.0, 0.0, 1.0
        )
    }

    /**
     * Inverts this Quaternion.
     */
    fun invert() {
        x = -x
        y = -y
        z = -z
    }

    private fun normalize() {
        val f: Double = KVMath.getNormalizationFactor(x, y, z, w)
        x *= f
        y *= f
        z *= f
        w *= f
    }

    public override fun clone(): Quaternion {
        return Quaternion(this)
    }

    override fun equals(o: Any?): Boolean {
        return if (o === this) {
            true
        } else if (o is Quaternion) {
            val q = o
            q.x == x && q.y == y && q.z == z && q.w == w
        } else {
            false
        }
    }

    override fun toString(): String {
        return "{" + x + ", " + y + ", " + z + ", " + w + "}"
    }

    companion object {
        // Helper function reused by Matrix4x4, portion cut out from getYawPitchRoll()
        protected fun getYaw(x: Double, y: Double, z: Double, w: Double): Double {
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
        protected fun getPitch(x: Double, y: Double, z: Double, w: Double): Double {
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
        protected fun getRoll(x: Double, y: Double, z: Double, w: Double): Double {
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
        protected fun getYawPitchRoll(x: Double, y: Double, z: Double, w: Double): Vector {
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

        /**
         * Creates a new identity quaternion
         *
         * @return identity quaternion (x=0, y=0, z=0, w=1)
         */
        fun identity(): Quaternion {
            return Quaternion()
        }

        /**
         * Performs a multiplication between two quaternions.
         * A new quaternion instance is returned.
         *
         * @param q1
         * @param q2
         * @return q1 x q2
         */
        fun multiply(q1: Quaternion, q2: Quaternion): Quaternion {
            val result = q1.clone()
            result.multiply(q2)
            return result
        }

        /**
         * Performs a division between two quaternions.
         * A new quaternion instance is returned.
         *
         * @param q1
         * @param q2
         * @return q1 / q2
         */
        fun divide(q1: Quaternion, q2: Quaternion): Quaternion {
            val result = q1.clone()
            result.divide(q2)
            return result
        }

        /**
         * Computes the difference transformation between two quaternions
         *
         * @param q1 Old rotation transformation quaternion
         * @param q2 New rotation transformation quaternion
         * @return Quaternion that rotates the old quaternion into the new quaternion
         */
        fun diff(q1: Quaternion, q2: Quaternion): Quaternion {
            val diff = q1.clone()
            diff.invert()
            diff.multiply(q2)
            return diff
        }

        /**
         * Creates a quaternion for a rotation around an axis
         *
         * @param axis
         * @param angleDegrees
         * @return quaternion for the rotation around the axis
         */
        fun fromAxisAngles(axis: Vector3, angleDegrees: Double): Quaternion {
            return fromAxisAngles(axis.x, axis.y, axis.z, angleDegrees)
        }

        /**
         * Creates a quaternion for a rotation around an axis
         *
         * @param axis
         * @param angleDegrees
         * @return quaternion for the rotation around the axis
         */
        fun fromAxisAngles(axis: Vector, angleDegrees: Double): Quaternion {
            return fromAxisAngles(axis.getX(), axis.getY(), axis.getZ(), angleDegrees)
        }

        /**
         * Creates a quaternion for a rotation around an axis
         *
         * @param axisX
         * @param axisY
         * @param axisZ
         * @param angleDegrees
         * @return quaternion for the rotation around the axis
         */
        fun fromAxisAngles(axisX: Double, axisY: Double, axisZ: Double, angleDegrees: Double): Quaternion {
            val r = 0.5 * Math.toRadians(angleDegrees)
            val f = Math.sin(r)
            return Quaternion(f * axisX, f * axisY, f * axisZ, Math.cos(r))
        }

        /**
         * Creates a quaternion from yaw/pitch/roll rotations as performed by Minecraft
         *
         * @param rotation (x=pitch, y=yaw, z=roll)
         * @return quaternion for the yaw/pitch/roll rotation
         */
        fun fromYawPitchRoll(rotation: Vector): Quaternion {
            return fromYawPitchRoll(rotation.getX(), rotation.getY(), rotation.getZ())
        }

        /**
         * Creates a quaternion from yaw/pitch/roll rotations as performed by Minecraft
         *
         * @param pitch rotation (X)
         * @param yaw rotation (Y)
         * @param roll rotation (Z)
         * @return quaternion for the yaw/pitch/roll rotation
         */
        fun fromYawPitchRoll(pitch: Double, yaw: Double, roll: Double): Quaternion {
            //TODO: Can be optimized to reduce the number of multiplications
            val quat = Quaternion()
            quat.rotateYawPitchRoll(pitch, yaw, roll)
            return quat
        }

        /**
         * Creates a quaternion that transforms the input vector (u) into the output vector (v).
         * The vectors do not have to be unit vectors for this function to work.
         * The d vector specifies an axis to rotate around when a 180-degree rotation is encountered.
         *
         * @param u input vector (from)
         * @param v expected output vector (to)
         * @param d direction axis around which to rotate for 180-degree angles
         * @return quaternion that rotates u to become v
         */
        fun fromToRotation(u: Vector, v: Vector, d: Vector): Quaternion {
            // xyz = cross(u, v), w = dot(u, v)
            // add magnitude of quaternion to w, then normalize it
            val dot: Double = u.dot(v)
            val q = Quaternion()
            q.x = u.getY() * v.getZ() - v.getY() * u.getZ()
            q.y = u.getZ() * v.getX() - v.getZ() * u.getX()
            q.z = u.getX() * v.getY() - v.getX() * u.getY()
            q.w = dot
            q.w += Math.sqrt(q.x * q.x + q.y * q.y + q.z * q.z + q.w * q.w)
            q.normalize()

            // there is a special case for opposite vectors
            // here the quaternion ends up being 0,0,0,0
            // after normalization the terms are NaN as a result (0xinf=NaN)
            if (java.lang.Double.isNaN(q.w)) {
                q.x = d.getX()
                q.y = d.getY()
                q.z = d.getZ()
                q.w = 0.0
                q.normalize()
            }
            return q
        }

        /**
         * Creates a quaternion that transforms the input vector (u) into the output vector (v).
         * The vectors do not have to be unit vectors for this function to work.
         *
         * @param u input vector (from)
         * @param v expected output vector (to)
         * @return quaternion that rotates u to become v
         */
        fun fromToRotation(u: Vector, v: Vector): Quaternion {
            // xyz = cross(u, v), w = dot(u, v)
            // add magnitude of quaternion to w, then normalize it
            val dot: Double = u.dot(v)
            val q = Quaternion()
            q.x = u.getY() * v.getZ() - v.getY() * u.getZ()
            q.y = u.getZ() * v.getX() - v.getZ() * u.getX()
            q.z = u.getX() * v.getY() - v.getX() * u.getY()
            q.w = dot
            q.w += Math.sqrt(q.x * q.x + q.y * q.y + q.z * q.z + q.w * q.w)
            q.normalize()

            // there is a special case for opposite vectors
            // here the quaternion ends up being 0,0,0,0
            // after normalization the terms are NaN as a result (0xinf=NaN)
            if (java.lang.Double.isNaN(q.w)) {
                if (dot > 0.0) {
                    // Identity Quaternion
                    q.setIdentity()
                } else {
                    // Rotation of 180 degrees around a certain axis
                    // First try axis X, then try axis Y
                    // The cross product with either vector is used for the axis
                    var norm: Double = KVMath.getNormalizationFactor(u.getZ(), u.getY())
                    if (java.lang.Double.isInfinite(norm)) {
                        norm = KVMath.getNormalizationFactor(u.getZ(), u.getX())
                        q.x = norm * u.getZ()
                        q.y = 0.0
                        q.z = norm * -u.getX()
                        q.w = 0.0
                    } else {
                        q.x = 0.0
                        q.y = norm * -u.getZ()
                        q.z = norm * u.getY()
                        q.w = 0.0
                    }
                }
            }
            return q
        }

        /**
         * Creates a quaternion that transforms a forward vector (0, 0, 1) into the output vector (v).
         * The vector does not have to be a unit vector for this function to work.
         * If the 'up' axis is important, use [.fromLookDirection] instead.
         *
         * @param v expected output vector (to)
         * @return quaternion that rotates (0,0,1) to become v
         */
        fun fromLookDirection(dir: Vector): Quaternion {
            val q = Quaternion(-dir.getY(), dir.getX(), 0.0, dir.getZ() + dir.length())

            // there is a special case when dir is (0, 0, -1)
            if (java.lang.Double.isNaN(q.w)) {
                q.x = 0.0
                q.y = 1.0
                q.z = 0.0
                q.w = 0.0
            }
            return q
        }

        /**
         * Creates a quaternion that 'looks' into a given direction, with a known 'up' vector
         * to dictate roll around that direction axis.
         *
         * @param dir to look into
         * @param up direction
         * @return Quaternion with the look-direction transformation
         */
        fun fromLookDirection(dir: Vector, up: Vector): Quaternion {
            // Use the 3x3 rotation matrix solution found on SO, combined with a getRotation()
            // https://stackoverflow.com/a/18574797
            val D: Vector = dir.clone().normalize()
            val S: Vector = up.clone().crossProduct(dir).normalize()
            val U: Vector = D.clone().crossProduct(S)
            val result: Quaternion = Matrix4x4.fromColumns3x3(S, U, D).rotation

            // Fix NaN as a result of dir == up
            return if (java.lang.Double.isNaN(result.x)) {
                fromLookDirection(dir)
            } else {
                result
            }
        }

        /**
         * Performs a linear interpolation between two quaternions.
         * Separate theta values can be specified to set how much of each quaternion to keep
         * For smoother interpolation, [.slerp] can be used instead.
         *
         * @param q0 quaternion at theta=0
         * @param q1 quaternion at theta=1
         * @param t0 theta value for q0 amount (range 0 to 1)
         * @param t1 theta value for q1 amount (range 0 to 1)
         * @return lerp result
         */
        fun lerp(q0: Quaternion, q1: Quaternion, t0: Double, t1: Double): Quaternion {
            return Quaternion(
                t0 * q0.x + t1 * q1.x,
                t0 * q0.y + t1 * q1.y,
                t0 * q0.z + t1 * q1.z,
                t0 * q0.w + t1 * q1.w
            )
        }

        /**
         * Performs a linear interpolation between two quaternions.
         * For smoother interpolation, [.slerp] can be used instead.
         *
         * @param q0 quaternion at theta=0
         * @param q1 quaternion at theta=1
         * @param theta value (range 0 to 1)
         * @return lerp result
         */
        fun lerp(q0: Quaternion, q1: Quaternion, theta: Double): Quaternion {
            return lerp(q0, q1, 1.0 - theta, theta)
        }

        /**
         * Performs a spherical interpolation between two quaternions.
         *
         * @param q0 quaternion at theta=0
         * @param q1 quaternion at theta=0
         * @param theta value (range 0 to 1)
         * @return slerp result
         */
        fun slerp(q0: Quaternion, q1: Quaternion, theta: Double): Quaternion {
            val qs = q1.clone()
            var dot = q0.dot(q1)

            // Invert quaternion when dot < 0 to simplify maths
            if (dot < 0.0) {
                dot = -dot
                qs.x = -qs.x
                qs.y = -qs.y
                qs.z = -qs.z
                qs.w = -qs.w
            }

            // Above this a lerp is adequate
            if (dot >= 0.95) {
                return lerp(q0, qs, theta)
            }

            // Linear interpolation using sines
            val angle = Math.acos(dot)
            val qd = 1.0 / Math.sin(angle)
            val q0f = qd * Math.sin(angle * (1.0 - theta))
            val qsf = qd * Math.sin(angle * theta)
            return lerp(q0, qs, q0f, qsf)
        }

        /**
         * Produces an average rotation from several different rotation values.
         * If only one rotation value is specified, then that one value is returned.
         * If no rotation values are specified, identity is returned.
         * The returned Quaternion is always a copy.
         *
         * @param values Iterable of Quaternion rotation values
         * @return average rotation Quaternion
         */
        fun average(values: Iterable<Quaternion>): Quaternion {
            val iter = values.iterator()

            // No values, return identity
            if (!iter.hasNext()) {
                return identity()
            }

            // Only one value, return the one value (make sure to clone!)
            val first = iter.next()
            if (!iter.hasNext()) {
                return first.clone()
            }

            // Build up an average
            var num_values = 1
            val result = first.clone()
            do {
                val next = iter.next()
                if (first.dot(next) >= 0.0) {
                    result.x += next.x
                    result.y += next.y
                    result.z += next.z
                    result.w += next.w
                } else {
                    result.x -= next.x
                    result.y -= next.y
                    result.z -= next.z
                    result.w -= next.w
                }
                num_values++
            } while (iter.hasNext())

            // Divide by the number of values, then normalize the result
            val fact = 1.0 / num_values.toDouble()
            result.x *= fact
            result.y *= fact
            result.z *= fact
            result.w *= fact
            result.normalize()
            return result
        }

        // This method is used often for the two-arg rotateX/Y/Z functions
        // Optimized equivalent of 0.5 * Math.cos(Math.atan2(y, x))
        private fun halfcosatan2(y: Double, x: Double): Double {
            var tmp = y / x
            tmp *= tmp
            tmp += 1.0
            return (if (x < 0.0) -0.5 else 0.5) / Math.sqrt(tmp)
        }
    }
}