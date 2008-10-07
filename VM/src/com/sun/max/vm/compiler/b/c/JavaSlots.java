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
/*VCSID=68ffbb20-2848-4148-ab2f-bad16e5fd47c*/
package com.sun.max.vm.compiler.b.c;

import com.sun.max.lang.*;
import com.sun.max.profile.*;
import com.sun.max.vm.compiler.cir.*;
import com.sun.max.vm.compiler.cir.variable.*;
import com.sun.max.vm.type.*;

/**
 * Aggregation of Java stack locations and a variable factory.
 *
 * @see JavaFrame
 * @see JavaStack
 *
 * @author Bernd Mathiske
 */
abstract class JavaSlots implements Cloneable {

    public abstract static class JavaStackSlot {
    }
    public static class FillerJavaStackSlot extends JavaStackSlot {
        private final Kind _kind;
        public FillerJavaStackSlot(Kind kind) {
            _kind = kind;
        }
    }
    public static class VariableJavaStackSlot extends JavaStackSlot {
        private final CirVariable _cirVariable;
        public VariableJavaStackSlot(CirVariable variable) {
            _cirVariable = variable;
        }
        public CirVariable cirVariable() {
            return _cirVariable;
        }
    }

    protected final SlotVariableFactory _variableFactory;
    protected JavaStackSlot[] _slots;

    protected JavaSlots(SlotVariableFactory variableFactory) {
        _variableFactory = variableFactory;
        _slots = new JavaStackSlot[_variableFactory.getMaxSlotCount()];
    }

    public JavaSlots copy() {
        Metrics.increment("JavaSlots.copy()");
        final JavaSlots result = Objects.clone(this);
        result._slots = Arrays.copy(_slots, new JavaStackSlot[_slots.length]);
        return result;
    }

    protected abstract int effectiveLength();

    public final CirValue[] makeDescriptor() {
        final CirValue[] descriptor = new CirValue[effectiveLength()];
        for (int i = 0; i < descriptor.length; i++) {
            if (_slots[i] == null || _slots[i] instanceof FillerJavaStackSlot) {
                descriptor[i] = CirValue.UNDEFINED;
            } else {
                assert _slots[i] instanceof VariableJavaStackSlot;
                descriptor[i] = ((VariableJavaStackSlot) (_slots[i])).cirVariable();
            }
        }
        return descriptor;
    }

}
