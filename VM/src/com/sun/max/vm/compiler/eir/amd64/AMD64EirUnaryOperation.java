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
/*VCSID=7ef1d6ba-db04-4c4c-82b7-d6cb694b2998*/
package com.sun.max.vm.compiler.eir.amd64;

import com.sun.max.collect.*;
import com.sun.max.vm.compiler.eir.*;

/**
 * @author Bernd Mathiske
 */
public abstract class AMD64EirUnaryOperation extends AMD64EirOperation {

    private final EirOperand _operand;

    protected AMD64EirUnaryOperation(EirBlock block, EirValue operand, EirOperand.Effect event, PoolSet<EirLocationCategory> locationCategories) {
        super(block);
        _operand = new EirOperand(this, event, locationCategories);
        _operand.setEirValue(operand);
    }

    public EirOperand operand() {
        return _operand;
    }

    public EirValue operandValue() {
        return _operand.eirValue();
    }

    public EirLocation operandLocation() {
        return _operand.location();
    }

    public AMD64EirRegister.General operandGeneralRegister() {
        return (AMD64EirRegister.General) operandLocation();
    }

    @Override
    public void visitOperands(EirOperand.Procedure visitor) {
        if (_operand != null) {
            visitor.run(_operand);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "  " + _operand;
    }
}
