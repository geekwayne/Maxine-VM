/*
 * Copyright (c) 2007, 2009, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.sun.max.vm.cps.cir.variable;

import com.sun.max.vm.cps.cir.*;
import com.sun.max.vm.cps.cir.transform.*;
import com.sun.max.vm.type.*;

/**
 * A local variable in the sense of the JVM spec that is not a method parameter.
 *
 * @author Bernd Mathiske
 */
public class CirLocalVariable extends CirSlotVariable {

    public CirLocalVariable(int serial, Kind kind, int localIndex) {
        super(serial, kind, localIndex);
    }

    @Override
    public String toString() {
        String s = "l" + kind().character + slotIndex() + "-" + serial();
        if (CirNode.printingIds()) {
            s += "_" + id();
        }
        return s;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof CirLocalVariable)) {
            return false;
        }
        final CirLocalVariable otherLocalVariable = (CirLocalVariable) other;
        return kind().character == otherLocalVariable.kind().character
            && slotIndex() == otherLocalVariable.slotIndex()
            && serial() == otherLocalVariable.serial();
    }

    @Override
    public final void acceptVisitor(CirVisitor visitor) {
        visitor.visitLocalVariable(this);
    }

    @Override
    public void acceptBlockScopedVisitor(CirBlockScopedVisitor visitor, CirBlock scope) {
        visitor.visitLocalVariable(this, scope);
    }

}
