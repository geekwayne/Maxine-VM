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
package com.sun.c1x.ir;

import com.sun.c1x.util.InstructionVisitor;
import com.sun.c1x.value.ValueType;
import com.sun.c1x.value.ValueStack;

/**
 * The <code>Phi</code> instruction represents the merging of dataflow
 * in the instruction graph. It refers to a join block and a variable.
 *
 * @author Ben L. Titzer
 */
public class Phi extends Instruction {

    public enum PhiFlag {
        CannotSimplify,
        Visited;

        public final int mask() {
            return 1 << ordinal();
        }
    }

    private final BlockBegin _block;
    private int _phiFlags;
    private final int _index;

    /**
     * Create a new Phi for the specified join block and local variable (or operand stack) slot.
     * @param type the type of the variable
     * @param block the join point
     * @param index the index into the stack (if < 0) or local variables
     */
    public Phi(ValueType type, BlockBegin block, int index) {
        super(type);
        _block = block;
        _index = index;
    }

    /**
     * Get the join block for this phi.
     * @return the join block of this phi
     */
    public BlockBegin block() {
        return _block;
    }

    /**
     * Check whether this phi corresponds to a local variable.
     * @return <code>true</code> if this phi refers to a local variable
     */
    public final boolean isLocal() {
        return _index >= 0;
    }

    /**
     * Check whether this phi corresponds to a stack location.
     * @return <code>true</code> if this phi refers to a stack location
     */
    public final boolean isOnStack() {
        return _index < 0;
    }

    /**
     * Get the local index of this phi.
     * @return the local index
     */
    public final int localIndex() {
        assert isLocal();
        return _index;
    }

    /**
     * Get the stack index of this phi.
     * @return the stack index of this phi
     */
    public final int stackIndex() {
        assert isOnStack();
        return -(_index + 1);
    }

    /**
     * Get the instruction that produces the value associated with the i'th predecessor
     * of the join block.
     * @param i the index of the predecessor
     * @return the instruction that produced the value in the i'th predecessor
     */
    public Instruction operandAt(int i) {
        ValueStack state;
        if (_block.isExceptionEntry()) {
            state = _block.exceptionHandlerStates().get(i);
        } else {
            state = _block.predecessors().get(i).end().state();
        }
        if (isLocal()) {
            return state.localAt(localIndex());
        } else {
            return state.stackAt(stackIndex());
        }
    }

    /**
     * Get the number of operands to this phi (i.e. the number of predecessors to the
     * join block).
     * @return the number of operands in this phi
     */
    public int operandCount() {
        if (_block.isExceptionEntry()) {
            return _block.exceptionHandlerStates().size();
        } else {
            return _block.predecessors().size();
        }
    }

    /**
     * Implements this instruction's half of the visitor pattern.
     * @param v the visitor to dispatch to
     */
    @Override
    public void accept(InstructionVisitor v) {
        v.visitPhi(this);
    }

    /**
     * Make this phi illegal if types were not merged correctly.
     */
    public void makeIllegal() {
        setPhiFlag(PhiFlag.CannotSimplify);
        setType(ValueType.ILLEGAL_TYPE);
    }

    /**
     * Check whether this instruction has the specified phi flag set.
     * @param flag the flag to test
     * @return <code>true</code> if this instruction has the flag
     */
    public boolean checkPhiFlag(PhiFlag flag) {
        return (_phiFlags & flag.mask()) != 0;
    }

    /**
     * Set a phi flag on this instruction.
     * @param flag the flag to set
     */
    public void setPhiFlag(PhiFlag flag) {
        _phiFlags |= flag.mask();
    }

    /**
     * Clear a phi flag on this instruction.
     * @param flag the flag to set
     */
    public void clearPhiFlag(PhiFlag flag) {
        _phiFlags &= ~flag.mask();
    }

    // XXX: why are there no input values to do with inputValuesDo?
}