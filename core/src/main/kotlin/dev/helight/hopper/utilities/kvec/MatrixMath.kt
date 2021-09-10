package dev.helight.hopper.utilities.kvec

object MatrixMath {
    /**
     * Given a 4x4 array "matrix0", this function replaces it with the
     * LU decomposition of a row-wise permutation of itself.  The input
     * parameters are "matrix0" and "dimen".  The array "matrix0" is also
     * an output parameter.  The vector "row_perm[4]" is an output
     * parameter that contains the row permutations resulting from partial
     * pivoting.  The output parameter "even_row_xchg" is 1 when the
     * number of row exchanges is even, or -1 otherwise.  Assumes data
     * type is always double.
     *
     * This function is similar to luDecomposition, except that it
     * is tuned specifically for 4x4 matrices.
     *
     * @return true if the matrix is nonsingular, or false otherwise.
     */
    //
    // Reference: Press, Flannery, Teukolsky, Vetterling,
    //        _Numerical_Recipes_in_C_, Cambridge University Press,
    //        1988, pp 40-45.
    //
    fun luDecomposition(
        matrix0: DoubleArray,
        row_perm: IntArray
    ): Boolean {
        val row_scale = DoubleArray(4)

        // Determine implicit scaling information by looping over rows
        run {
            var i: Int
            var j: Int
            var ptr: Int
            var rs: Int
            var big: Double
            var temp: Double
            ptr = 0
            rs = 0

            // For each row ...
            i = 4
            while (i-- != 0) {
                big = 0.0

                // For each column, find the largest element in the row
                j = 4
                while (j-- != 0) {
                    temp = matrix0[ptr++]
                    temp = Math.abs(temp)
                    if (temp > big) {
                        big = temp
                    }
                }

                // Is the matrix singular?
                if (big == 0.0) {
                    return false
                }
                row_scale[rs++] = 1.0 / big
            }
        }
        run {
            var j: Int
            val mtx: Int
            mtx = 0

            // For all columns, execute Crout's method
            j = 0
            while (j < 4) {
                var i: Int
                var imax: Int
                var k: Int
                var target: Int
                var p1: Int
                var p2: Int
                var sum: Double
                var big: Double
                var temp: Double

                // Determine elements of upper diagonal matrix U
                i = 0
                while (i < j) {
                    target = mtx + 4 * i + j
                    sum = matrix0[target]
                    k = i
                    p1 = mtx + 4 * i
                    p2 = mtx + j
                    while (k-- != 0) {
                        sum -= matrix0[p1] * matrix0[p2]
                        p1++
                        p2 += 4
                    }
                    matrix0[target] = sum
                    i++
                }

                // Search for largest pivot element and calculate
                // intermediate elements of lower diagonal matrix L.
                big = 0.0
                imax = -1
                i = j
                while (i < 4) {
                    target = mtx + 4 * i + j
                    sum = matrix0[target]
                    k = j
                    p1 = mtx + 4 * i
                    p2 = mtx + j
                    while (k-- != 0) {
                        sum -= matrix0[p1] * matrix0[p2]
                        p1++
                        p2 += 4
                    }
                    matrix0[target] = sum

                    // Is this the best pivot so far?
                    if (row_scale[i] * Math.abs(sum).also { temp = it } >= big) {
                        big = temp
                        imax = i
                    }
                    i++
                }
                if (imax < 0) {
                    return false
                }

                // Is a row exchange necessary?
                if (j != imax) {
                    // Yes: exchange rows
                    k = 4
                    p1 = mtx + 4 * imax
                    p2 = mtx + 4 * j
                    while (k-- != 0) {
                        temp = matrix0[p1]
                        matrix0[p1++] = matrix0[p2]
                        matrix0[p2++] = temp
                    }

                    // Record change in scale factor
                    row_scale[imax] = row_scale[j]
                }

                // Record row permutation
                row_perm[j] = imax

                // Is the matrix singular
                // Was:
                //   matrix0[(mtx + (4*j) + j)] == 0.0
                // Changed it because of float range issues
                val SINGULAR_EP = 0.00001
                val v = matrix0[mtx + 4 * j + j]
                if (v >= -SINGULAR_EP && v <= SINGULAR_EP) {
                    return false
                }

                // Divide elements of lower diagonal matrix L by pivot
                if (j != 4 - 1) {
                    temp = 1.0 / matrix0[mtx + 4 * j + j]
                    target = mtx + 4 * (j + 1) + j
                    i = 3 - j
                    while (i-- != 0) {
                        matrix0[target] *= temp
                        target += 4
                    }
                }
                j++
            }
        }
        return true
    }

    /**
     * Solves a set of linear equations.  The input parameters "matrix1",
     * and "row_perm" come from luDecompostionD4x4 and do not change
     * here.  The parameter "matrix2" is a set of column vectors assembled
     * into a 4x4 matrix of floating-point values.  The procedure takes each
     * column of "matrix2" in turn and treats it as the right-hand side of the
     * matrix equation Ax = LUx = b.  The solution vector replaces the
     * original column of the matrix.
     *
     * If "matrix2" is the identity matrix, the procedure replaces its contents
     * with the inverse of the matrix from which "matrix1" was originally
     * derived.
     */
    //
    // Reference: Press, Flannery, Teukolsky, Vetterling,
    //        _Numerical_Recipes_in_C_, Cambridge University Press,
    //        1988, pp 44-45.
    //
    fun luBacksubstitution(
        matrix1: DoubleArray,
        row_perm: IntArray,
        matrix2: DoubleArray
    ) {
        var i: Int
        var ii: Int
        var ip: Int
        var j: Int
        var k: Int
        val rp: Int
        var cv: Int
        var rv: Int

        //  rp = row_perm;
        rp = 0

        // For each column vector of matrix2 ...
        k = 0
        while (k < 4) {

            //      cv = &(matrix2[0][k]);
            cv = k
            ii = -1

            // Forward substitution
            i = 0
            while (i < 4) {
                var sum: Double
                ip = row_perm[rp + i]
                sum = matrix2[cv + 4 * ip]
                matrix2[cv + 4 * ip] = matrix2[cv + 4 * i]
                if (ii >= 0) {
                    //          rv = &(matrix1[i][0]);
                    rv = i * 4
                    j = ii
                    while (j <= i - 1) {
                        sum -= matrix1[rv + j] * matrix2[cv + 4 * j]
                        j++
                    }
                } else if (sum != 0.0) {
                    ii = i
                }
                matrix2[cv + 4 * i] = sum
                i++
            }

            // Backsubstitution
            //      rv = &(matrix1[3][0]);
            rv = 3 * 4
            matrix2[cv + 4 * 3] /= matrix1[rv + 3]
            rv -= 4
            matrix2[cv + 4 * 2] = (matrix2[cv + 4 * 2] -
                    matrix1[rv + 3] * matrix2[cv + 4 * 3]) / matrix1[rv + 2]
            rv -= 4
            matrix2[cv + 4 * 1] =
                (matrix2[cv + 4 * 1] - matrix1[rv + 2] * matrix2[cv + 4 * 2] - matrix1[rv + 3] * matrix2[cv + 4 * 3]) / matrix1[rv + 1]
            rv -= 4
            matrix2[cv + 4 * 0] =
                (matrix2[cv + 4 * 0] - matrix1[rv + 1] * matrix2[cv + 4 * 1] - matrix1[rv + 2] * matrix2[cv + 4 * 2] - matrix1[rv + 3] * matrix2[cv + 4 * 3]) / matrix1[rv + 0]
            k++
        }
    }
}