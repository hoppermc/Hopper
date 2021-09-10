package dev.helight.hopper.utilities.kvec

import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.util.Vector

/**
 * Multiple Math utilities to compare and calculate using Vectors and raw values
 */
object KVMath {
    private const val CHUNK_BITS = 4
    private const val CHUNK_VALUES = 16
    const val DEGTORAD = 0.017453293f
    const val RADTODEG = 57.29577951f
    const val HALFROOTOFTWO = 0.707106781
    fun lengthSquared(vararg values: Double): Double {
        var rval = 0.0
        for (value in values) {
            rval += value * value
        }
        return rval
    }

    fun length(vararg values: Double): Double {
        return Math.sqrt(lengthSquared(*values))
    }

    fun distance(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        return length(x1 - x2, y1 - y2)
    }

    fun distanceSquared(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        return lengthSquared(x1 - x2, y1 - y2)
    }

    fun distance(x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double): Double {
        return length(x1 - x2, y1 - y2, z1 - z2)
    }

    fun distanceSquared(x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double): Double {
        return lengthSquared(x1 - x2, y1 - y2, z1 - z2)
    }

    /**
     * Gets a percentage and round it with a cusotm amound of decimals
     *
     * @param subtotal to get percentags for
     * @param total to use as 100% value
     * @param decimals to round with
     * @return Percentage for subtotal with custom decimals
     */
    fun getPercentage(subtotal: Int, total: Int, decimals: Int): Double {
        return round(getPercentage(subtotal, total), decimals)
    }

    /**
     * Gets a percentags of 2 values
     *
     * @param subtotal to get percentage for
     * @param total to sue as 100% value
     * @return percentage
     */
    fun getPercentage(subtotal: Int, total: Int): Double {
        return (subtotal.toFloat() / total.toFloat() * 100).toDouble()
    }

    /**
     * Gets the angle difference between two angles
     *
     * @param angle1
     * @param angle2
     * @return angle difference
     */
    fun getAngleDifference(angle1: Int, angle2: Int): Int {
        return Math.abs(wrapAngle(angle1 - angle2))
    }

    /**
     * Gets the angle difference between two angles
     *
     * @param angle1
     * @param angle2
     * @return angle difference
     */
    fun getAngleDifference(angle1: Float, angle2: Float): Float {
        return Math.abs(wrapAngle(angle1 - angle2))
    }

    /**
     * Gets the angle difference between two angles
     *
     * @param angle1
     * @param angle2
     * @return angle difference
     */
    fun getAngleDifference(angle1: Double, angle2: Double): Double {
        return Math.abs(wrapAngle(angle1 - angle2))
    }

    /**
     * Wraps the angle to be between -180 and 180 degrees
     *
     * @param angle to wrap
     * @return [-180 > angle >= 180]
     */
    fun wrapAngle(angle: Int): Int {
        var wrappedAngle = angle
        while (wrappedAngle <= -180) {
            wrappedAngle += 360
        }
        while (wrappedAngle > 180) {
            wrappedAngle -= 360
        }
        return wrappedAngle
    }

    /**
     * Wraps the angle to be between -180 and 180 degrees
     *
     * @param angle to wrap
     * @return [-180 > angle >= 180]
     */
    fun wrapAngle(angle: Float): Float {
        var wrappedAngle = angle
        while (wrappedAngle <= -180f) {
            wrappedAngle += 360f
        }
        while (wrappedAngle > 180f) {
            wrappedAngle -= 360f
        }
        return wrappedAngle
    }

    /**
     * Wraps the angle to be between -180 and 180 degrees
     *
     * @param angle to wrap
     * @return [-180 > angle >= 180]
     */
    fun wrapAngle(angle: Double): Double {
        var wrappedAngle = angle
        while (wrappedAngle <= -180.0) {
            wrappedAngle += 360.0
        }
        while (wrappedAngle > 180.0) {
            wrappedAngle -= 360.0
        }
        return wrappedAngle
    }

    /**
     * Normalizes a 2D-vector to be the length of another 2D-vector<br></br>
     * Calculates the normalization factor to multiply the input vector with, to
     * get the requested length
     *
     * @param x axis of the vector
     * @param z axis of the vector
     * @param reqx axis of the length vector
     * @param reqz axis of the length vector
     * @return the normalization factor
     */
    fun normalize(x: Double, z: Double, reqx: Double, reqz: Double): Double {
        return Math.sqrt(lengthSquared(reqx, reqz) / lengthSquared(x, z))
    }

    fun getLookAtYaw(loc: Entity, lookat: Entity): Double {
        return getLookAtYaw(loc.getLocation(), lookat.getLocation())
    }

    fun getLookAtYaw(loc: Block, lookat: Block): Double {
        return getLookAtYaw(loc.getLocation(), lookat.getLocation())
    }

    fun getLookAtYaw(loc: Location, lookat: Location): Double {
        return getLookAtYaw(lookat.getX() - loc.getX(), lookat.getZ() - loc.getZ())
    }

    fun getLookAtYaw(motion: Vector): Double {
        return getLookAtYaw(motion.x, motion.z)
    }

    /**
     * Gets the horizontal look-at angle in degrees to look into the
     * 2D-direction specified
     *
     * @param dx axis of the direction
     * @param dz axis of the direction
     * @return the angle in degrees
     */
    fun getLookAtYaw(dx: Double, dz: Double): Double {
        return atan2(dz, dx) - 180f
    }

    /**
     * Gets the pitch angle in degrees to look into the direction specified
     *
     * @param dX axis of the direction
     * @param dY axis of the direction
     * @param dZ axis of the direction
     * @return look-at angle in degrees
     */
    fun getLookAtPitch(dX: Double, dY: Double, dZ: Double): Double {
        return getLookAtPitch(dY, length(dX, dZ))
    }

    /**
     * Gets the pitch angle in degrees to look into the direction specified
     *
     * @param dY axis of the direction
     * @param dXZ axis of the direction (length of x and z)
     * @return look-at angle in degrees
     */
    fun getLookAtPitch(dY: Double, dXZ: Double): Double {
        return -atan(dY / dXZ)
    }

    /**
     * Gets the inverse tangent of the value in degrees
     *
     * @param value
     * @return inverse tangent angle in degrees
     */
    fun atan(value: Double): Double {
        return Math.atan(value)
    }

    /**
     * Gets the inverse tangent angle in degrees of the rectangle vector
     *
     * @param y axis
     * @param x axis
     * @return inverse tangent 2 angle in degrees
     */
    fun atan2(y: Double, x: Double): Double {
        return Math.atan2(x,y)
    }

    /**
     * Gets the floor long value from a double value
     *
     * @param value to get the floor of
     * @return floor value
     */
    fun longFloor(value: Double): Long {
        val l = value.toLong()
        return if (value < l) l - 1L else l
    }

    /**
     * Gets the floor integer value from a double value
     *
     * @param value to get the floor of
     * @return floor value
     */
    fun floor(value: Double): Int {
        val i = value.toInt()
        return if (value < i.toDouble()) i - 1 else i
    }

    /**
     * Gets the floor integer value from a float value
     *
     * @param value to get the floor of
     * @return floor value
     */
    fun floor(value: Float): Int {
        val i = value.toInt()
        return if (value < i.toFloat()) i - 1 else i
    }

    /**
     * Gets the ceiling integer value from a double value
     *
     * @param value to get the ceiling of
     * @return ceiling value
     */
    fun ceil(value: Double): Int {
        val i = value.toInt()
        return if (value > i.toDouble()) i + 1 else i
    }

    /**
     * Gets the ceiling integer value from a float value
     *
     * @param value to get the ceiling of
     * @return ceiling value
     */
    fun ceil(value: Float): Int {
        val i = value.toInt()
        return if (value > i.toFloat()) i + 1 else i
    }

    /**
     * Moves a Location into the yaw and pitch of the Location in the offset
     * specified
     *
     * @param loc to move
     * @param offset vector
     * @return Translated Location
     */
    fun move(loc: Location, offset: Vector): Location {
        return move(loc, offset.x, offset.y, offset.z)
    }

    /**
     * Moves a Location into the yaw and pitch of the Location in the offset
     * specified
     *
     * @param loc to move
     * @param dx offset
     * @param dy offset
     * @param dz offset
     * @return Translated Location
     */
    fun move(loc: Location, dx: Double, dy: Double, dz: Double): Location {
        val off = rotate(loc.getYaw(), loc.getPitch(), dx, dy, dz)
        val x: Double = loc.getX() + off.x
        val y: Double = loc.getY() + off.y
        val z: Double = loc.getZ() + off.z
        return Location(loc.getWorld(), x, y, z, loc.getYaw(), loc.getPitch())
    }

    /**
     * Rotates a 3D-vector using yaw and pitch
     *
     * @param yaw angle in degrees
     * @param pitch angle in degrees
     * @param vector to rotate
     * @return Vector rotated by the angle (new instance)
     */
    fun rotate(yaw: Float, pitch: Float, vector: Vector): Vector {
        return rotate(yaw, pitch, vector.x, vector.y, vector.z)
    }

    /**
     * Rotates a 3D-vector using yaw and pitch
     *
     * @param yaw angle in degrees
     * @param pitch angle in degrees
     * @param x axis of the vector
     * @param y axis of the vector
     * @param z axis of the vector
     * @return Vector rotated by the angle
     */
    fun rotate(yaw: Float, pitch: Float, x: Double, y: Double, z: Double): Vector {
        // Conversions found by (a lot of) testing
        var angle: Double
        angle = Math.toRadians(yaw.toDouble())
        val sinyaw = Math.sin(angle)
        val cosyaw = Math.cos(angle)
        angle = Math.toRadians(pitch.toDouble())
        val sinpitch = Math.sin(angle)
        val cospitch = Math.cos(angle)
        val vector = Vector()
        vector.x = x * sinyaw - y * cosyaw * sinpitch - z * cosyaw * cospitch
        vector.y = y * cospitch - z * sinpitch
        vector.z = -(x * cosyaw) - y * sinyaw * sinpitch - z * sinyaw * cospitch
        return vector
    }

    /**
     * Returns the floor modulus of the int arguments
     *
     * @param x the dividend
     * @param y the divisor
     * @returnthe floor modulus x
     */
    fun floorMod(x: Int, y: Int): Int {
        return Math.floorMod(x, y)
    }

    /**
     * Returns the floor modulus of the long arguments
     *
     * @param x the dividend
     * @param y the divisor
     * @returnthe floor modulus x
     */
    fun floorMod(x: Long, y: Long): Long {
        return Math.floorMod(x, y)
    }

    /**
     * Returns the floor division of the int arguments
     * @param x the dividend
     * @param y the divisor
     * @return floor division x
     */
    fun floorDiv(x: Int, y: Int): Int {
        return Math.floorDiv(x, y)
    }

    /**
     * Returns the floor division of the long arguments
     * @param x the dividend
     * @param y the divisor
     * @return floor division x
     */
    fun floorDiv(x: Long, y: Long): Long {
        return Math.floorDiv(x, y)
    }

    /**
     * Rounds the specified value to the amount of decimals specified
     *
     * @param value to round
     * @param decimals count
     * @return value round to the decimal count specified
     */
    fun round(value: Double, decimals: Int): Double {
        val p = Math.pow(10.0, decimals.toDouble())
        return Math.round(value * p) / p
    }
    /**
     * Returns the default if the value is not-a-number
     *
     * @param value to check
     * @param def value
     * @return The value, or the default if it is NaN
     */
    /**
     * Returns 0 if the value is not-a-number
     *
     * @param value to check
     * @return The value, or 0 if it is NaN
     */
    @JvmOverloads
    fun fixNaN(value: Double, def: Double = 0.0): Double {
        return if (java.lang.Double.isNaN(value)) def else value
    }
    /**
     * Returns the default if the value is not-a-number
     *
     * @param value to check
     * @param def value
     * @return The value, or the default if it is NaN
     */
    /**
     * Returns 0 if the value is not-a-number
     *
     * @param value to check
     * @return The value, or 0 if it is NaN
     */
    @JvmOverloads
    fun fixNaN(value: Float, def: Float = 0.0f): Float {
        return if (java.lang.Float.isNaN(value)) def else value
    }

    /**
     * Converts a location value into a chunk coordinate
     *
     * @param loc to convert
     * @return chunk coordinate
     */
    fun toChunk(loc: Double): Int {
        return floor(loc / CHUNK_VALUES.toDouble())
    }

    /**
     * Converts a location value into a chunk coordinate
     *
     * @param loc to convert
     * @return chunk coordinate
     */
    fun toChunk(loc: Int): Int {
        return loc shr CHUNK_BITS
    }

    fun useOld(oldvalue: Double, newvalue: Double, peruseold: Double): Double {
        return oldvalue + peruseold * (newvalue - oldvalue)
    }

    fun lerp(d1: Double, d2: Double, stage: Double): Double {
        return if (java.lang.Double.isNaN(stage) || stage > 1) {
            d2
        } else if (stage < 0) {
            d1
        } else {
            d1 * (1 - stage) + d2 * stage
        }
    }

    fun lerp(vec1: Vector, vec2: Vector, stage: Double): Vector {
        val newvec = Vector()
        newvec.setX(lerp(vec1.x, vec2.x, stage))
        newvec.setY(lerp(vec1.y, vec2.y, stage))
        newvec.setZ(lerp(vec1.z, vec2.z, stage))
        return newvec
    }

    fun lerp(loc1: Location, loc2: Location, stage: Double): Location {
        val newloc = Location(loc1.getWorld(), 0.0, 0.0, 0.0)
        newloc.setX(lerp(loc1.getX(), loc2.getX(), stage))
        newloc.setY(lerp(loc1.getY(), loc2.getY(), stage))
        newloc.setZ(lerp(loc1.getZ(), loc2.getZ(), stage))
        newloc.setYaw(lerp(loc1.getYaw().toDouble(), loc2.getYaw().toDouble(), stage) as Float)
        newloc.setPitch(lerp(loc1.getPitch().toDouble(), loc2.getPitch().toDouble(), stage) as Float)
        return newloc
    }

    /**
     * Checks whether one value is negative and the other positive, or opposite
     *
     * @param value1 to check
     * @param value2 to check
     * @return True if value1 is inverted from value2
     */
    fun isInverted(value1: Double, value2: Double): Boolean {
        return value1 > 0 && value2 < 0 || value1 < 0 && value2 > 0
    }

    /**
     * Gets the direction of yaw and pitch angles
     *
     * @param yaw angle in degrees
     * @param pitch angle in degrees
     * @return Direction Vector
     */
    fun getDirection(yaw: Float, pitch: Float): Vector {
        val vector = Vector()
        val rotX = Math.toRadians(yaw.toDouble())
        val rotY = Math.toRadians(pitch.toDouble())
        vector.y = -Math.sin(rotY)
        val h = Math.cos(rotY)
        vector.x = -h * Math.sin(rotX)
        vector.z = h * Math.cos(rotX)
        return vector
    }

    /**
     * Clamps the value between -limit and limit
     *
     * @param value to clamp
     * @param limit
     * @return value, -limit or limit
     */
    fun clamp(value: Double, limit: Double): Double {
        return clamp(value, -limit, limit)
    }

    /**
     * Clamps the value between the min and max values
     *
     * @param value to clamp
     * @param min
     * @param max
     * @return value, min or max
     */
    fun clamp(value: Double, min: Double, max: Double): Double {
        return if (value < min) min else if (value > max) max else value
    }

    /**
     * Clamps the value between -limit and limit
     *
     * @param value to clamp
     * @param limit
     * @return value, -limit or limit
     */
    fun clamp(value: Float, limit: Float): Float {
        return clamp(value, -limit, limit)
    }

    /**
     * Clamps the value between the min and max values
     *
     * @param value to clamp
     * @param min
     * @param max
     * @return value, min or max
     */
    fun clamp(value: Float, min: Float, max: Float): Float {
        return if (value < min) min else if (value > max) max else value
    }

    /**
     * Clamps the value between -limit and limit
     *
     * @param value to clamp
     * @param limit
     * @return value, -limit or limit
     */
    fun clamp(value: Int, limit: Int): Int {
        return clamp(value, -limit, limit)
    }

    /**
     * Clamps the value between the min and max values
     *
     * @param value to clamp
     * @param min
     * @param max
     * @return value, min or max
     */
    fun clamp(value: Int, min: Int, max: Int): Int {
        return if (value < min) min else if (value > max) max else value
    }

    /**
     * Clamps the value between -limit and limit
     *
     * @param value to clamp
     * @param limit
     * @return value, -limit or limit
     */
    fun clamp(value: Long, limit: Long): Long {
        return clamp(value, -limit, limit)
    }

    /**
     * Clamps the value between the min and max values
     *
     * @param value to clamp
     * @param min
     * @param max
     * @return value, min or max
     */
    fun clamp(value: Long, min: Long, max: Long): Long {
        return if (value < min) min else if (value > max) max else value
    }

    /**
     * Turns a value negative or keeps it positive based on a boolean input
     *
     * @param value to work with
     * @param negative - True to invert, False to keep the old value
     * @return the value or inverted (-value)
     */
    fun invert(value: Int, negative: Boolean): Int {
        return if (negative) -value else value
    }

    /**
     * Turns a value negative or keeps it positive based on a boolean input
     *
     * @param value to work with
     * @param negative - True to invert, False to keep the old value
     * @return the value or inverted (-value)
     */
    fun invert(value: Float, negative: Boolean): Float {
        return if (negative) -value else value
    }

    /**
     * Turns a value negative or keeps it positive based on a boolean input
     *
     * @param value to work with
     * @param negative - True to invert, False to keep the old value
     * @return the value or inverted (-value)
     */
    fun invert(value: Double, negative: Boolean): Double {
        return if (negative) -value else value
    }

    /**
     * Merges two ints into a long
     *
     * @param msw integer
     * @param lsw integer
     * @return merged long value
     */
    fun toLong(msw: Int, lsw: Int): Long {
        return (msw.toLong() shl 32) + lsw - Int.MIN_VALUE
    }

    fun longHashToLong(msw: Int, lsw: Int): Long {
        return (msw.toLong() shl 32) + lsw - Int.MIN_VALUE
    }

    fun longHashMsw(key: Long): Int {
        return (key shr 32).toInt()
    }

    fun longHashLsw(key: Long): Int {
        return (key and -0x1).toInt() + Int.MIN_VALUE
    }

    /**
     * Takes the most and least significant words of both keys, sums them together,
     * and produces a new key with the two words summed.
     *
     * @param keyA
     * @param keyB
     * @return words of keyA and keyB summed, and turned back into a long
     */
    fun longHashSumW(keyA: Long, keyB: Long): Long {
        val sum_msw = (keyA and -0x100000000L) + (keyB and -0x100000000L)
        val sum_lsw = (keyA and -0x1) + (keyB and -0x1)
        return sum_msw + sum_lsw.toInt() - Int.MIN_VALUE
    }

    /**
     * Shorthand equivalent of:<br></br>
     * longHashToLong(longHashMsw(a)+longHashMsw(b), longHashLsw(a)+longHashLsw(b))
     *
     * @param key_a
     * @param key_b
     * @return key_a + key_b
     */
    fun longHashAdd(key_a: Long, key_b: Long): Long {
        return key_a + key_b + Int.MIN_VALUE
    }

    fun setVectorLength(vector: Vector, length: Double) {
        setVectorLengthSquared(vector, Math.signum(length) * length * length)
    }

    fun setVectorLengthSquared(vector: Vector, lengthsquared: Double) {
        val vlength = vector.lengthSquared()
        if (Math.abs(vlength) > 0.0001) {
            if (lengthsquared < 0) {
                vector.multiply(-Math.sqrt(-lengthsquared / vlength))
            } else {
                vector.multiply(Math.sqrt(lengthsquared / vlength))
            }
        }
    }

    fun isHeadingTo(from: Location, to: Location, velocity: Vector): Boolean {
        return isHeadingTo(Vector(to.getX() - from.getX(), to.getY() - from.getY(), to.getZ() - from.getZ()), velocity)
    }

    fun isHeadingTo(offset: Vector, velocity: Vector): Boolean {
        val dbefore = offset.lengthSquared()
        if (dbefore < 0.0001) {
            return true
        }
        val clonedVelocity = velocity.clone()
        setVectorLengthSquared(clonedVelocity, dbefore)
        return dbefore > clonedVelocity.subtract(offset).lengthSquared()
    }

    /**
     * Calculates the normalization factor for a 3D vector.
     * Multiplying the input vector with this factor will turn it into a vector of unit length.
     * If the input vector is (0,0,0), Infinity is returned.
     *
     * @param v
     * @return normalization factor
     */
    fun getNormalizationFactor(v: Vector): Double {
        return getNormalizationFactorLS(v.lengthSquared())
    }

    /**
     * Calculates the normalization factor for a 4D vector.
     * Multiplying the input vector with this factor will turn it into a vector of unit length.
     * If the input vector is (0,0,0,0), Infinity is returned.
     *
     * @param x
     * @param y
     * @param z
     * @param w
     * @return normalization factor
     */
    fun getNormalizationFactor(x: Double, y: Double, z: Double, w: Double): Double {
        return getNormalizationFactorLS(x * x + y * y + z * z + w * w)
    }

    /**
     * Calculates the normalization factor for a 3D vector.
     * Multiplying the input vector with this factor will turn it into a vector of unit length.
     * If the input vector is (0,0,0), Infinity is returned.
     *
     * @param x
     * @param y
     * @param z
     * @return normalization factor
     */
    fun getNormalizationFactor(x: Double, y: Double, z: Double): Double {
        return getNormalizationFactorLS(x * x + y * y + z * z)
    }

    /**
     * Calculates the normalization factor for a 2D vector.
     * Multiplying the input vector with this factor will turn it into a vector of unit length.
     * If the input vector is (0,0), Infinity is returned.
     *
     * @param x
     * @param y
     * @return normalization factor
     */
    fun getNormalizationFactor(x: Double, y: Double): Double {
        return getNormalizationFactorLS(x * x + y * y)
    }

    /**
     * Calculates the normalization factor for a squared length.
     * Multiplying the input values with this factor will turn it into a vector of unit length.
     * If the squared length is 0, Infinity is returned.
     *
     * @param lengthSquared
     * @return normalization factor
     */
    fun getNormalizationFactorLS(lengthSquared: Double): Double {
        // https://stackoverflow.com/a/12934750
        return if (Math.abs(1.0 - lengthSquared) < 2.107342e-08) {
            2.0 / (1.0 + lengthSquared)
        } else {
            1.0 / Math.sqrt(lengthSquared)
        }
    }

    /**
     * Calculates the angle difference between two vectors in degrees
     *
     * @param v0 first vector
     * @param v1 second vector
     * @return absolute angle difference in degrees
     */
    fun getAngleDifference(v0: Vector, v1: Vector): Double {
        var dot = v0.dot(v1)
        dot *= getNormalizationFactor(v0)
        dot *= getNormalizationFactor(v1)
        return Math.toDegrees(Math.acos(dot))
    }

    /**
     * Sets the x, y and z coordinates of a Bukkit Vector
     *
     * @param vector The vector to update
     * @param value The value to set vector to
     * @return input vector
     */
    fun setVector(vector: Vector, value: Vector?): Vector {
        return vector.copy(value!!)
    }

    /**
     * Sets the x, y and z coordinates of a Bukkit Vector
     *
     * @param vector The vector to update
     * @param x The new x-coordinate to set in vector
     * @param y The new y-coordinate to set in vector
     * @param z The new z-coordinate to set in vector
     * @return input vector
     */
    fun setVector(vector: Vector, x: Double, y: Double, z: Double): Vector {
        vector.x = x
        vector.y = y
        vector.z = z
        return vector
    }

    /**
     * Adds the x, y and z coordinate values to the original coordinates of a vector.
     * The input vector is updated.
     *
     * @param vector The vector to update
     * @param ax The value to add to the x-coordinate
     * @param ay The value to add to the y-coordinate
     * @param az The value to add to the z-coordinate
     * @return input vector
     */
    fun addToVector(vector: Vector, ax: Double, ay: Double, az: Double): Vector {
        vector.x = vector.x + ax
        vector.y = vector.y + ay
        vector.z = vector.z + az
        return vector
    }

    /**
     * Subtracts the x, y and z coordinate values from the original coordinates of a vector.
     * The input vector is updated.
     *
     * @param vector The vector to update
     * @param sx The value to subtract from the x-coordinate
     * @param sy The value to subtract from the y-coordinate
     * @param sz The value to subtract from the z-coordinate
     * @return input vector
     */
    fun subtractFromVector(vector: Vector, sx: Double, sy: Double, sz: Double): Vector {
        vector.x = vector.x - sx
        vector.y = vector.y - sy
        vector.z = vector.z - sz
        return vector
    }

    /**
     * Multiplies the x, y and z coordinate values of the original coordinates of a vector.
     * The input vector is updated.
     *
     * @param vector The vector to update
     * @param mx The value to multiply the x-coordinate with
     * @param my The value to multiply the y-coordinate with
     * @param mz The value to multiply the z-coordinate with
     * @return input vector
     */
    fun multiplyVector(vector: Vector, mx: Double, my: Double, mz: Double): Vector {
        vector.x = vector.x * mx
        vector.y = vector.y * my
        vector.z = vector.z * mz
        return vector
    }

    /**
     * Divides the x, y and z coordinate values of the original coordinates of a vector.
     * The input vector is updated.
     *
     * @param vector The vector to update
     * @param mx The value to divide the x-coordinate with
     * @param my The value to divide the y-coordinate with
     * @param mz The value to divide the z-coordinate with
     * @return input vector
     */
    fun divideVector(vector: Vector, mx: Double, my: Double, mz: Double): Vector {
        vector.x = vector.x / mx
        vector.y = vector.y / my
        vector.z = vector.z / mz
        return vector
    }
}