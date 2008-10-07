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
/*VCSID=fc07097b-8fb9-4074-beef-fa3c0388d945*/
package com.sun.max.vm.compiler.dir;

import java.util.*;

import com.sun.max.vm.compiler.dir.transform.*;

/**
 * Unconditional DIR control flow.
 *
 * @author Bernd Mathiske
 */
public class DirGoto extends DirInstruction {

    private DirBlock _targetBlock;

    public DirGoto(DirBlock targetBlock) {
        super();
        _targetBlock = targetBlock;
    }

    public DirBlock targetBlock() {
        return _targetBlock;
    }

    public void setTargetBlock(DirBlock block) {
        _targetBlock = block;
    }

    @Override
    public void substituteBlocks(Map<DirBlock, DirBlock> blockMap) {
        final DirBlock block = blockMap.get(_targetBlock);
        if (block != null && block != _targetBlock) {
            _targetBlock = block;
        }
    }

    @Override
    public boolean isEquivalentTo(DirInstruction other, DirBlockEquivalence dirBlockEquivalence) {
        if (other instanceof DirGoto) {
            final DirGoto dirGoto = (DirGoto) other;
            return _targetBlock.isEquivalentTo(dirGoto._targetBlock, dirBlockEquivalence);
        }
        return false;
    }

    @Override
    public String toString() {
        return "goto " + _targetBlock.serial();
    }

    @Override
    public void acceptVisitor(DirVisitor visitor) {
        visitor.visitGoto(this);
    }
}
