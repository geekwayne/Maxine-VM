/*
 * Copyright (c) 2009 Sun Microsystems, Inc.  All rights reserved.
 *
 * Sun Microsystems, Inc. has intellectual property rights relating to technology embodied in the product
 * that is described in this document. In particular, and without limitation, these intellectual property
 * rights may include one or more of the U.S. patents listed at http://www.sun.com/patents and one or
 * more additional patents or pending patent applications in the U.S. and in other countries.
 *
 * U.S. Government Rights - Commercial software. Government users are subject to the Sun
 * Microsystems, Inc. standard license agreement and applicable provisions of the FAR and its
 * supplements.
 *
 * Use is subject to license terms. Sun, Sun Microsystems, the Sun logo, Java and Solaris are trademarks or
 * registered trademarks of Sun Microsystems, Inc. in the U.S. and other countries. All SPARC trademarks
 * are used under license and are trademarks or registered trademarks of SPARC International, Inc. in the
 * U.S. and other countries.
 *
 * UNIX is a registered trademark in the U.S. and other countries, exclusively licensed through X/Open
 * Company, Ltd.
 */
package test.optimize;

/*
 * Tests constant folding of integer operations.
 * @Harness: java
 * @Runs: 0=18; 1=18; 2=!java.lang.NullPointerException
 */
public class VN_Field01 {
    static final VN_Field01 _object = new VN_Field01();

    int _field = 9;

    public static int test(int arg) {
        if (arg == 0) {
            return test1();
        }
        if (arg == 1) {
            return test2();
        }
        if (arg == 2) {
            return test3();
        }
        return 0;
    }

    private static int test1() {
        VN_Field01 a = _object;
        return a._field + a._field;
    }

    private static int test2() {
        VN_Field01 a = _object;
        VN_Field01 b = _object;
        return a._field + b._field;
    }

    private static int test3() {
        VN_Field01 a = null;
        VN_Field01 b = null;
        return a._field + b._field;
    }

}