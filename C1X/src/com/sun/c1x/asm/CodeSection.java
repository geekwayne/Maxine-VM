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
package com.sun.c1x.asm;

import com.sun.c1x.asm.RelocInfo.*;


public class CodeSection {

    public int remaining() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void relocate(Pointer pc, Type polltype) {
        // TODO Auto-generated method stub

    }

    public void clearMark() {
        // TODO Auto-generated method stub

    }

    public Pointer start() {
        // TODO Auto-generated method stub
        return null;
    }

    public Pointer limit() {
        // TODO Auto-generated method stub
        return null;
    }

    public Pointer end() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean maybeExpandToEnsureRemaining(int requiredSpace) {
        // TODO Auto-generated method stub
        return false;
    }

    public void setEnd(Pointer codePos) {
        // TODO Auto-generated method stub

    }

    public CodeBuffer outer() {
        // TODO Auto-generated method stub
        return null;
    }

    public Pointer mark() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setMark() {
        // TODO Auto-generated method stub

    }

    public Pointer target(Label l, Pointer pointer) {
        // TODO Auto-generated method stub
        return null;
    }

    public int index() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void relocate(Pointer pointer, RelocationHolder rspec, int format) {
        // TODO Auto-generated method stub

    }

    public boolean isAllocated() {
        // TODO Auto-generated method stub
        return false;
    }

}