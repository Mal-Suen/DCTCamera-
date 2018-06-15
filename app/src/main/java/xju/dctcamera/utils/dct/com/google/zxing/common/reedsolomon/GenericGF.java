/*
 * Copyright 2007 ZXing authors Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package xju.dctcamera.utils.dct.com.google.zxing.common.reedsolomon;


/**
 * 谷歌提供的纠删码算法
 */


/**
 * <p>
 * This class contains utility methods for performing mathematical operations over the Galois Fields. Operations use a
 * given primitive polynomial in calculations.
 * </p>
 * <p>
 * Throughout this package, elements of the GF are represented as an <code>int</code> for convenience and speed (but at
 * the cost of memory).
 * </p>
 * 
 * @author Sean Owen
 * @author David Olivier
 */
public final class GenericGF {

    public static final GenericGF AZTEC_DATA_12 = new GenericGF(0x1069, 4096); // x^12 + x^6 + x^5 + x^3 + 1

    public static final GenericGF AZTEC_DATA_10 = new GenericGF(0x409, 1024); // x^10 + x^3 + 1

    public static final GenericGF AZTEC_DATA_6 = new GenericGF(0x43, 64); // x^6 + x + 1

    public static final GenericGF AZTEC_PARAM = new GenericGF(0x13, 16); // x^4 + x + 1

    public static final GenericGF QR_CODE_FIELD_256 = new GenericGF(0x011D, 256); // x^8 + x^4 + x^3 + x^2 + 1

    public static final GenericGF DATA_MATRIX_FIELD_256 = new GenericGF(0x012D, 256); // x^8 + x^5 + x^3 + x^2 + 1

    public static final GenericGF AZTEC_DATA_8 = DATA_MATRIX_FIELD_256;

    private static final int INITIALIZATION_THRESHOLD = 0;

    /**
     * Implements both addition and subtraction -- they are the same in GF(size).
     * 
     * @return sum/difference of a and b
     */
    static int addOrSubtract(final int a, final int b) {
        return a ^ b;
    }

    private int[] expTable;

    private int[] logTable;

    private GenericGFPoly zero;

    private GenericGFPoly one;

    private final int size;

    private final int primitive;

    private boolean initialized = false;

    /**
     * Create a representation of GF(size) using the given primitive polynomial.
     * 
     * @param primitive irreducible polynomial whose coefficients are represented by the bits of an int, where the
     *            least-significant bit represents the constant coefficient
     */
    public GenericGF(final int primitive, final int size) {
        this.primitive = primitive;
        this.size = size;

        if (size <= INITIALIZATION_THRESHOLD) {
            initialize();
        }
    }

    /**
     * @return the monomial representing coefficient * x^degree
     */
    GenericGFPoly buildMonomial(final int degree, final int coefficient) {
        checkInit();

        if (degree < 0)
            throw new IllegalArgumentException();
        if (coefficient == 0)
            return this.zero;
        final int[] coefficients = new int[degree + 1];
        coefficients[0] = coefficient;
        return new GenericGFPoly(this, coefficients);
    }

    private void checkInit() {
        if (!this.initialized) {
            initialize();
        }
    }

    /**
     * @return 2 to the power of a in GF(size)
     */
    int exp(final int a) {
        checkInit();

        return this.expTable[a];
    }

    GenericGFPoly getOne() {
        checkInit();

        return this.one;
    }

    public int getSize() {
        return this.size;
    }

    GenericGFPoly getZero() {
        checkInit();

        return this.zero;
    }

    private void initialize() {
        this.expTable = new int[this.size];
        this.logTable = new int[this.size];
        int x = 1;
        for (int i = 0; i < this.size; i++) {
            this.expTable[i] = x;
            x <<= 1; // x = x * 2; we're assuming the generator alpha is 2
            if (x >= this.size) {
                x ^= this.primitive;
                x &= this.size - 1;
            }
        }
        for (int i = 0; i < this.size - 1; i++) {
            this.logTable[this.expTable[i]] = i;
        }
        // logTable[0] == 0 but this should never be used
        this.zero = new GenericGFPoly(this, new int[] {
            0 });
        this.one = new GenericGFPoly(this, new int[] {
            1 });
        this.initialized = true;
    }

    /**
     * @return multiplicative inverse of a
     */
    int inverse(final int a) {
        checkInit();

        if (a == 0)
            throw new ArithmeticException();
        return this.expTable[this.size - this.logTable[a] - 1];
    }

    /**
     * @return base 2 log of a in GF(size)
     */
    int log(final int a) {
        checkInit();

        if (a == 0)
            throw new IllegalArgumentException();
        return this.logTable[a];
    }

    /**
     * @param a
     * @param b
     * @return product of a and b in GF(size)
     */
    int multiply(int a, final int b) {
        checkInit();

        if (a == 0 || b == 0)
            return 0;

        if (a < 0 || b < 0 || a >= this.size || b >= this.size) {
            a++;
        }

        final int logSum = this.logTable[a] + this.logTable[b];
        return this.expTable[logSum % this.size + logSum / this.size];
    }

}