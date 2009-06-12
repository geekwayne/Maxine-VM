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
package com.sun.c1x.ci;

import com.sun.c1x.util.BitMap;

import java.util.List;

/**
 * The <code>CiMethod</code> class definition.
 *
 * @author Ben L. Titzer
 */
public interface CiMethod {

    String name();
    CiType holder();
    CiSignature signatureType();
    byte[] code();
    int maxLocals();
    int maxStackSize();

    boolean hasBalancedMonitors();
    boolean hasExceptionHandlers();
    boolean isLoaded();
    boolean isAbstract();
    boolean isNative();
    boolean isFinalMethod();
    boolean isSynchronized();
    boolean isStrictFP();
    boolean isStatic();
    boolean isOverridden();
    boolean willLink(CiType where, int opcode);
    int vtableIndex();

    CiMethodData methodData();
    BitMap liveness(int bci);

    boolean canBeStaticallyBound();
    int codeSize();

    List<CiExceptionHandler> exceptionHandlers();
}