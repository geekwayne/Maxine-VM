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
package com.sun.max.vm.compiler.ir.interpreter;

import com.sun.max.collect.*;
import com.sun.max.program.option.*;
import com.sun.max.vm.actor.member.*;
import com.sun.max.vm.bytecode.*;
import com.sun.max.vm.compiler.ir.interpreter.BirInterpreter.*;
import com.sun.max.vm.hotpath.compiler.*;
import com.sun.max.vm.hotpath.compiler.Console.*;
import com.sun.max.vm.hotpath.state.*;
import com.sun.max.vm.value.*;
import com.sun.max.vm.profile.TreeAnchor;

public class HotpathProfiler implements Profiler {

    public static OptionSet optionSet = new OptionSet();
    public static Option<Boolean> printState = optionSet.newBooleanOption("PP", false, "(P)rints the Profiler's Execution (S)tate.");

    private GrowableMapping<BytecodeLocation, TreeAnchor> anchors = new OpenAddressingHashMapping<BytecodeLocation, TreeAnchor>();

    private boolean isTracing = false;

    public void jump(BytecodeLocation fromlocation, BytecodeLocation toLocation, BirState state) {
        assert fromlocation.classMethodActor == toLocation.classMethodActor;
        if (toLocation.bytecodePosition < fromlocation.bytecodePosition) {
            backwardJump(fromlocation, toLocation, state);
        }
    }

    public void trace(BytecodeLocation location, BirState state) {
        if (isTracing) {
            isTracing = BirTracer.current().visitBytecode(location, state);
            if (printState.getValue()) {
                Console.println(Color.LIGHTRED, "tracing loc: " + location.toString());
            }
        }
    }

    public void invoke(ClassMethodActor target, BirState state) {
        if (isTracing) {
            isTracing = BirTracer.current().visitInvoke(target, state);
        }
    }

    private void backwardJump(BytecodeLocation fromlocation, BytecodeLocation toLocation, State<Value> state) {
        if (state.last().isEmpty() == false) {
            // Only record traces at anchors that have empty stacks. This should almost always be the case for java,
            // however due to bytecode rewriting for exception handlers this is not the case in Maxine.
            return;
        }

        final TreeAnchor hotpathAnchor = lookupAnchor(toLocation);
        final boolean isTracing = BirTracer.current().visitAnchor(hotpathAnchor, state);
        if (isTracing) {
            this.isTracing = true;
            if (printState.getValue()) {
                Console.println(Color.RED, "tracing starting at: " + hotpathAnchor);
            }
        } else {
            if (this.isTracing == true) {
                this.isTracing = false;
                if (printState.getValue()) {
                    Console.println(Color.RED, "tracing stopping at: " + hotpathAnchor);
                }
            }
        }
    }

    private TreeAnchor lookupAnchor(BytecodeLocation location) {
        TreeAnchor hotpathAnchor = anchors.get(location);
        if (hotpathAnchor == null) {
            hotpathAnchor = new TreeAnchor(location, 5);
            anchors.put(location, hotpathAnchor);
        }
        return hotpathAnchor;
    }
}