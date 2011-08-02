/*
 * Copyright (c) 2009, 2011, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.max.vm.actor.member;

import com.sun.max.unsafe.*;
import com.sun.max.vm.*;
import com.sun.max.vm.compiler.target.*;
import com.sun.max.vm.jni.*;

/**
 * A native function represents a {@linkplain #makeSymbol() symbol} associated with a {@linkplain #classMethodActor()
 * method} that can be {@linkplain #link() linked} at runtime to produce a native machine code pointer.
 */
public class NativeFunction {
    private final ClassMethodActor classMethodActor;
    private String symbol;

    private Address address = Address.zero();

    /**
     * The stub generated for calling this native function.
     */
    private TargetMethod stub;

    /**
     * The position of the native call in this native function's stub.
     */
    private int nativeCallPos;

    public Address address() {
        return address;
    }

    public NativeFunction(ClassMethodActor classMethodActor) {
        this.classMethodActor = classMethodActor;
    }

    public ClassMethodActor classMethodActor() {
        return classMethodActor;
    }

    /**
     * Gets the address of the call to this native function from within its native stub.
     */
    public Address nativeCallAddress() {
        return stub.codeStart().plus(nativeCallPos);
    }

    /**
     * Sets the address of the call to this native function from within its native stub.
     */
    public void setCallSite(TargetMethod stub, int pos) {
        assert this.stub == null : "cannot have more than one stub per native method";
        this.stub = stub;
        this.nativeCallPos = pos;
    }

    /**
     * Gets the native symbol derived from the method this native function implements.
     *
     * Only the first call to this method can cause allocation. Subsequent calls return the value cached by the
     * first call.
     */
    public String makeSymbol() {
        if (symbol == null) {
            final ClassMethodActor m = classMethodActor;
            symbol = m.isCFunction() ? m.name.toString() : Mangle.mangleMethod(m.holder().typeDescriptor, m.name.toString(), m.descriptor(), true);
        }
        return symbol;
    }

    /**
     * Gets the native function pointer for this native function, linking it first if necessary.
     *
     * There's no need to synchronize this method - it's fine for multiple threads to each link the native function in
     * the case where's there is a race as the result of resolution will always be the same value.
     *
     * ATTENTION: do not declare this synchronized - it is called in the primordial phase.
     *
     * @throws UnsatisfiedLinkError if the native function cannot be found
     */
    public Address link() throws UnsatisfiedLinkError {
        if (address.isZero()) {
            address = DynamicLinker.lookup(classMethodActor, makeSymbol()).asAddress();
            if (!MaxineVM.isPrimordialOrPristine()) {
                if (NativeInterfaces.verbose()) {
                    Log.println("[Dynamic-linking native method " + classMethodActor.holder().name + "." + classMethodActor.name + " = " + address.toHexString() + "]");
                }
            }
        }
        return address;
    }

    /**
     * Determines if the native function pointer has been linked.
     */
    public boolean isLinked() {
        return !address.isZero();
    }

    /**
     * Sets (or clears) the machine code address for this native function.
     */
    public void setAddress(Address address) {
        this.address = address;
        if (!MaxineVM.isPrimordialOrPristine()) {
            if (NativeInterfaces.verbose()) {
                Log.println("[" + (address.isZero() ? "Unregistering" : "Registering") + " JNI native method " + classMethodActor.holder().name + "." + classMethodActor.name + "]");
            }
        }
    }
}