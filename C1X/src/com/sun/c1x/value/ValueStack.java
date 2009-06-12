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
package com.sun.c1x.value;

import com.sun.c1x.ir.*;
import com.sun.c1x.util.Util;
import com.sun.c1x.util.InstructionClosure;
import com.sun.c1x.ci.CiMethod;
import com.sun.c1x.Bailout;

import java.util.ArrayList;

/**
 * The <code>ValueStack</code> class encapsulates the state of local variables
 * and the stack at a particular point in the abstract interpretation.
 *
 * @author Ben L. Titzer
 */
public class ValueStack {

    private final Instruction[] _values; // manages both stack and locals
    private int _stackIndex;
    private final int _maxLocals;

    private final IRScope _scope;
    private ArrayList<Instruction> _locks;
    private boolean _lockStack;

    public ValueStack(IRScope irScope, int maxLocals, int maxStack) {
        this._scope = irScope;
        _values = new Instruction[maxLocals + maxStack];
        _maxLocals = maxLocals;
    }

    /**
     * Copies the contents of this value stack so that further updates to
     * either stack aren't reflected in the other.
     * @param withLocals indicates whether to copy the local state
     * @param withStack indicates whether to copy the stack state
     * @param withLocks indicates whether to copy the lock state
     * @return a new value stack with the specified components
     */
    public ValueStack copy(boolean withLocals, boolean withStack, boolean withLocks) {
        final ValueStack other = new ValueStack(_scope, localsSize(), maxStackSize());
        if (withLocals && withStack) {
            // fast path: use array copy
            System.arraycopy(_values, 0, other._values, 0, valuesSize());
            other._stackIndex = _stackIndex;
        } else {
            if (withLocals) {
                other.replaceLocals(this);
            }
            if (withStack) {
                other.replaceStack(this);
            }
        }
        if (withLocks) {
            other.replaceLocks(this);
        }
        return other;
    }

    public ValueStack copyLocks() {
        int size = scope().lockStackSize();
        if (stackSize() == 0) {
            size = 0;
        }
        ValueStack s = new ValueStack(scope(), localsSize(), maxStackSize());
        s._lockStack = true;
        s.replaceLocks(this);
        s.replaceLocals(this);
        s.replaceStack(this);
        s._stackIndex = size; // trim stack back to lockstack size
        return s;
    }

    public ValueStack copy() {
        return copy(true, true, true);
    }

    public boolean isSame(ValueStack other) {
        assert scope() == other.scope();
        assert localsSize() == other.localsSize();
        return isSameAcrossScopes(other);
    }

    public boolean isSameAcrossScopes(ValueStack other) {
        assert stackSize() == other.stackSize();
        assert localsSize() == other.localsSize();
        assert locksSize() == other.locksSize();
        for (int i = 0; i < _stackIndex; i++) {
            Instruction x = stackAt(i);
            Instruction y = other.stackAt(i);
            if (x != y && typeMismatch(x, y)) {
                return false;
            }
        }
        if (_locks != null) {
            for (int i = 0; i < _locks.size(); i++) {
                if (lockAt(i) != other.lockAt(i)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns the inlining context associated with this value stack.
     * @return the inlining context
     */
    public final IRScope scope() {
        return _scope;
    }

    /**
     * Returns whether this value stack is locked (i.e. is for an exception site).
     * @return <code>true</code> if this stack is locked
     */
    public final boolean isLockStack() {
        return _lockStack;
    }

    /**
     * Returns the size of the local variables.
     * @return the size of the local variables
     */
    public final int localsSize() {
        return _maxLocals;
    }

    /**
     * Returns the size of the locks.
     * @return the size of the locks
     */
    public final int locksSize() {
        return _locks == null ? 0 : _locks.size();
    }

    /**
     * Returns the current size (height) of the stack.
     * @return the size of the stack
     */
    public final int stackSize() {
        return _stackIndex;
    }

    /**
     * Returns the maximum size of the stack.
     * @return the maximum size of the stack
     */
    public final int maxStackSize() {
        return _values.length - _maxLocals;
    }

    /**
     * Checks whether the stack is empty.
     * @return <code>true</code> the stack is currently empty
     */
    public final boolean stackEmpty() {
        return _stackIndex == 0;
    }

    /**
     * Checks whether there are any active locks.
     * @return <code>true</code> if there are <i>no</i> active locks
     */
    public final boolean noActiveLocks() {
        return locksSize() == 0;
    }

    /**
     * Invalidate the local variable at the specified index. If the specified index refers
     * to a doubleword local, then invalid the high word as well.
     * @param i the index of the local to invalidate
     */
    public void invalidateLocal(int i) {
        Instruction x = _values[i];
        if (isDoubleWord(x)) {
            assertHigh(_values[i + 1]);
            _values[i + 1] = null;
        }
        _values[i] = null;
    }

    /**
     * Load the local variable at the specified index.
     * @param i the index of the local variable to load
     * @return the instruction that produced the specified local
     */
    public Instruction loadLocal(int i) {
        Instruction x = _values[i];
        if (x != null) {
            if (x.type().isIllegal()) {
                return null;
            }
            assert x.type().isSingleWord() || _values[i + 1] == null;
        }
        return x;
    }

    /**
     * Store the local variable at the specified index. If the value is a doubleword,
     * then also overwrite the next local variable position.
     * @param i the index at which to store
     * @param x the instruction which produces the value for the local
     */
    public void storeLocal(int i, Instruction x) {
        invalidateLocal(i);
        _values[i] = x;
        if (isDoubleWord(x)) {
            // if this was a double word and i + 1 was a double word, then kill i + 2
            Instruction h = _values[i + 1];
            if (isDoubleWord(h)) {
                _values[i + 2] = null;
            }
        }
        if (i > 0) {
            // if there was a double word at i - 1, then kill it
            Instruction p = _values[i - 1];
            if (isDoubleWord(p)) {
                _values[i - 1] = null;
            }
        }
    }

    /**
     * Replace the local variables in this value stack with the local variables from the
     * specified value stack. This is used in inlining.
     * @param with the value stack containing the new local variables
     */
    public void replaceLocals(ValueStack with) {
        assert with._maxLocals == _maxLocals;
        System.arraycopy(with._values, 0, _values, 0, _maxLocals);
    }

    /**
     * Replace the stack in this value stack with the stack from the
     * specified value stack. This is used in inlining.
     * @param with the value stack containing the new local variables
     */
    public void replaceStack(ValueStack with) {
        System.arraycopy(with._values, with._maxLocals, _values, _maxLocals, with._stackIndex);
        _stackIndex = with._stackIndex;
    }

    /**
     * Replace the locks in this value stack with the locks from the
     * specified value stack. This is used in inlining.
     * @param with the value stack containing the new local variables
     */
    public void replaceLocks(ValueStack with) {
        if (with._locks == null) {
            _locks = null;
        } else {
            _locks = Util.uncheckedCast(with._locks.clone());
        }
    }

    /**
     * Get the value on the stack at the specified stack index.
     * @param i the index into the stack, with <code>0</code> being the bottom of the stack
     * @return the instruction at the specified position in the stack
     */
    public Instruction stackAt(int i) {
        final Instruction x = _values[i + _maxLocals];
        assert i < _stackIndex;
        return x;
    }

    /**
     * Get the value in the local variable at the specified offset.
     * @param i the index into the locals
     * @return the instruction that produced the value for the specified local
     */
    public Instruction localAt(int i) {
        return _values[i];
    }

    /**
     * Clears all values on this stack.
     */
    public void clearStack() {
        _stackIndex = 0;
    }

    public void clearLocals() {
        for (int i = 0; i < _values.length; i++) {
            _values[i] = null;
        }
    }

    /**
     * Truncates this stack to the specified size.
     * @param size the size to truncate to
     */
    public void truncateStack(int size) {
        _stackIndex = size;
    }

    /**
     * Pushes an instruction onto the stack with the expected type.
     * @param type the type expected for this instruction
     * @param x the instruction to push onto the stack
     */
    public void push(BasicType type, Instruction x) {
        xpush(assertType(type, x));
        if (type.sizeInSlots() == 2) {
            xpush(null);
        }
    }

    /**
     * Pushes a value onto the stack without checking the type.
     * @param x the instruction to push onto the stack
     */
    public void xpush(Instruction x) {
        assert _stackIndex >= 0;
        _values[_maxLocals + _stackIndex++] = x;
    }

    /**
     * Pushes a value onto the stack and checks that it is an int.
     * @param x the instruction to push onto the stack
     */
    public void ipush(Instruction x) {
        xpush(assertInt(x));
    }

    /**
     * Pushes a value onto the stack and checks that it is a float.
     * @param x the instruction to push onto the stack
     */
    public void fpush(Instruction x) {
        xpush(assertFloat(x));
    }

    /**
     * Pushes a value onto the stack and checks that it is an object.
     * @param x the instruction to push onto the stack
     */
    public void apush(Instruction x) {
        xpush(assertObject(x));
    }

    /**
     * Pushes a value onto the stack and checks that it is a JSR return address.
     * @param x the instruction to push onto the stack
     */
    public void jpush(Instruction x) {
        xpush(assertJsr(x));
    }

    /**
     * Pushes a value onto the stack and checks that it is a long.
     * @param x the instruction to push onto the stack
     */
    public void lpush(Instruction x) {
        xpush(assertLong(x));
        xpush(null);
    }

    /**
     * Pushes a value onto the stack and checks that it is a double.
     * @param x the instruction to push onto the stack
     */
    public void dpush(Instruction x) {
        xpush(assertDouble(x));
        xpush(null);
    }

    /**
     * Pops an instruction off the stack with the expected type.
     * @param basicType the tag of the expected type
     * @return the instruction on the top of the stack
     */
    public Instruction pop(BasicType basicType) {
        if (basicType.sizeInSlots() == 2) {
            xpop();
        }
        return assertType(basicType, xpop());
    }

    /**
     * Pops a value off of the stack without checking the type.
     * @return x the instruction popped off the stack
     */
    public Instruction xpop() {
        assert _stackIndex >= 1;
        return _values[_maxLocals + --_stackIndex];
    }

    /**
     * Pops a value off of the stack and checks that it is an int.
     * @return x the instruction popped off the stack
     */
    public Instruction ipop() {
        return assertInt(xpop());
    }

    /**
     * Pops a value off of the stack and checks that it is a float.
     * @return x the instruction popped off the stack
     */
    public Instruction fpop() {
        return assertFloat(xpop());
    }

    /**
     * Pops a value off of the stack and checks that it is an object.
     * @return x the instruction popped off the stack
     */
    public Instruction apop() {
        return assertObject(xpop());
    }

    /**
     * Pops a value off of the stack and checks that it is a JSR return address.
     * @return x the instruction popped off the stack
     */
    public Instruction jpop() {
        return assertJsr(xpop());
    }

    /**
     * Pops a value off of the stack and checks that it is a long.
     * @return x the instruction popped off the stack
     */
    public Instruction lpop() {
        assertHigh(xpop());
        return assertLong(xpop());
    }

    /**
     * Pops a value off of the stack and checks that it is a double.
     * @return x the instruction popped off the stack
     */
    public Instruction dpop() {
        assertHigh(xpop());
        return assertDouble(xpop());
    }

    /**
     * Pop the specified number of slots off of this stack and return
     * them as an array of instructions.
     * @param size the number of arguments off of the stack
     * @return an array containing the arguments off of the stack
     */
    public Instruction[] popArguments(int size) {
        int base = _stackIndex - size;
        Instruction[] r = new Instruction[size];
        System.arraycopy(_values, _maxLocals + base, r, 0, size);
        _stackIndex = base;
        return r;
    }

    /**
     * Locks a new object within the specified IRScope.
     * @param scope the IRScope in which this locking operation occurs
     * @param obj the object being locked
     * @return the index of the lock within the lock stack
     */
    public int lock(IRScope scope, Instruction obj) {
        if (_locks == null) {
            _locks = new ArrayList<Instruction>();
        }
        _locks.add(obj);
        int size = _locks.size();
        scope.setMinimumNumberOfLocks(size);
        return size - 1;
    }

    /**
     * Unlock the lock on the top of the stack.
     * @return the index of the lock just unlocked.
     */
    public int unlock() {
        _locks.remove(_locks.size() - 1);
        return _locks.size();
    }

    /**
     * Retrieves the lock at the specified index in the lock stack.
     * @param i the index into the lock stack
     * @return the instruction which produced the object at the specified location in the lock stack
     */
    public Instruction lockAt(int i) {
        return _locks.get(i);
    }

    /**
     * Creates a new ValueStack corresponding to inlining the specified method into this point in
     * this value stack.
     * @param scope the IRScope representing the inlined method
     * @return a new value stack representing the state at the beginning of inlining the specified
     * method into this one
     */
    public ValueStack pushScope(IRScope scope) {
        assert scope.caller() == _scope;
        CiMethod method = scope.method();
        ValueStack res = new ValueStack(scope, method.maxLocals(), maxStackSize() + method.maxStackSize());
        res.replaceStack(this);
        res.replaceLocks(this);
        return res;
    }

    /**
     * Creates a new ValueStack corresponding to the state upon returning from this inlined method
     * into the outer IRScope.
     * @return a new value stack representing the state at exit from this value stack
     */
    public ValueStack popScope() {
        IRScope callingScope = _scope.caller();
        int maxStack = maxStackSize() - _scope.method().maxStackSize();
        assert callingScope != null;
        assert maxStack >= 0;
        ValueStack res = new ValueStack(callingScope, callingScope.method().maxLocals(), maxStack);
        res.replaceStack(this);
        res.replaceLocks(this);
        res.replaceLocals(callingScope.callerState());
        return res;
    }

    /**
     * Inserts a phi statement into the stack at the specified stack index.
     * @param block the block begin for which we are creating the phi
     * @param i the index into the stack for which to create a phi
     */
    public void setupPhiForStack(BlockBegin block, int i) {
        Instruction p = stackAt(i);
        assert !(p instanceof Phi) || ((Phi) p).block() != block : "phi already created for this block";
        Instruction phi = new Phi(p.type(), block, -i - 1);
        _values[_maxLocals + i] = phi;
    }

    /**
     * Inserts a phi statement for the local at the specified index.
     * @param block the block begin for which we are creating the phi
     * @param i the index of the local variable for which to create the phi
     */
    public void setupPhiForLocal(BlockBegin block, int i) {
        Instruction p = _values[i];
        assert !(p instanceof Phi) || ((Phi) p).block() != block : "phi already created for this block";
        Instruction phi = new Phi(p.type(), block, i);
        storeLocal(i, phi);
    }

    /**
     * Iterates over all the values in this value stack, including the stack, locals, and locks.
     * @param closure the closure to apply to each value
     */
    public void valuesDo(InstructionClosure closure) {
        final int max = valuesSize();
        for (int i = 0; i < max; i++) {
            _values[i] = closure.apply(_values[i]);
        }
        if (_locks != null) {
            for (int i = 0; i < _locks.size(); i++) {
                _locks.set(i, closure.apply(_locks.get(i)));
            }
        }
        ValueStack state = this.scope().callerState();
        if (state != null) {
            state.valuesDo(closure);
        }
    }

    public void checkLocalAndStackTags(ValueStack other) {
        checkSize(other);
        final int max = valuesSize();
        for (int i = 0; i < max; i++) {
            Instruction x = _values[i];
            if (x != null) {
                Instruction y = other._values[i];
                if (y != null) {
                    if (typeMismatch(x, y)) {
                        throw new Bailout("tag mismatch at " + i);
                    }
                } else {
                    throw new Bailout("instruction missing at " + i);
                }
            }
        }
    }

    private int valuesSize() {
        return _maxLocals + _stackIndex;
    }

    public void checkPhis(BlockBegin block, ValueStack other) {
        checkSize(other);
        final int max = valuesSize();
        for (int i = 0; i < max; i++) {
            Instruction x = _values[i];
            Instruction y = other._values[i];
            if (x != null && x != y) {
                if (!(x instanceof Phi) || ((Phi) x).block() != block) {
                    // x is not a phi, or is not a phi for this block
                    throw new Bailout("instruction is not a phi or null at " + i);
                }
            }
        }
    }

    private void checkSize(ValueStack other) {
        if (other._stackIndex != _stackIndex) {
            throw new Bailout("stack sizes do not match");
        } else if (other._values.length != _values.length) {
            throw new Bailout("value sizes do not match");
        }
    }

    public void merge(BlockBegin block, ValueStack other) {
        checkSize(other);
        for (int i = 0; i < valuesSize(); i++) {
            Instruction x = _values[i];
            Instruction y = other._values[i];
            if (x != null && x != y) {
                // XXX: merging of constants would be possible here?
                if (x instanceof Phi && ((Phi) x).block() == block) {
                    continue; // phi already exists, continue
                }
                if (i < _maxLocals) {
                    // this a local
                    if (y == null || typeMismatch(x, y)) {
                        invalidateLocal(i); // it has become invalid
                    } else {
                        setupPhiForLocal(block, i); // it needs a phi
                    }
                } else {
                    // this is a stack slot
                    setupPhiForStack(block, i - _maxLocals);
                }
            }
        }
    }

    private static boolean typeMismatch(Instruction x, Instruction y) {
        return y.type().basicType() != x.type().basicType();
    }

    private static Instruction assertType(BasicType basicType, Instruction x) {
        assert x.type().basicType() == basicType;
        return x;
    }

    private static Instruction assertLong(Instruction x) {
        assert x.type().basicType() == BasicType.Long;
        return x;
    }

    private static Instruction assertJsr(Instruction x) {
        assert x.type().basicType() == BasicType.Jsr;
        return x;
    }

    private static Instruction assertInt(Instruction x) {
        assert x.type().basicType() == BasicType.Int;
        return x;
    }

    private static Instruction assertFloat(Instruction x) {
        assert x.type().basicType() == BasicType.Float;
        return x;
    }

    private static Instruction assertObject(Instruction x) {
        assert x.type().basicType() == BasicType.Object;
        return x;
    }

    private static Instruction assertDouble(Instruction x) {
        assert x.type().basicType() == BasicType.Double;
        return x;
    }

    private static void assertHigh(Instruction x) {
        assert x == null;
    }

    private static boolean isDoubleWord(Instruction x) {
        return x != null && x.type().isDoubleWord();
    }

}