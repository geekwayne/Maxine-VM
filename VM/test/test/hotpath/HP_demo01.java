/*
 * Copyright (c) 2007 Sun Microsystems, Inc.  All rights reserved.
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
/*VCSID=374a9fbf-6ee6-4fe1-9d4d-d87a50c5d692*/
// Checkstyle: stop
package test.hotpath;
/*
 * @Harness: java
 * @Runs: 80 = 3160;
 */
public class HP_demo01 {

    public static int test(int count) {
        int sum = 0;

        for (int i = 0; i < count; i++) {
            int[] ia = new int[count];
            long[] la = new long[count];
            float[] fa = new float[count];
            double[] da = new double[count];
            sum += ia[i] = (int) (la[i] = (long) (fa[i] = (float) (da[i] = i)));
        }

        return sum;
    }
}
