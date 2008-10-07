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
/*VCSID=df5cc02c-7c78-4115-9e4a-1ececcafd8cc*/
package com.sun.max.vm.code;

import com.sun.max.asm.*;
import com.sun.max.lang.*;

/**
 * Helper for modifying the target of a branch in target code.
 *
 * @author Laurent Daynes
 */
public class BranchTargetModifier extends InstructionModifier {
    final WordWidth _displacementWidth;

    public BranchTargetModifier(int position, int length, WordWidth displacementWidth) {
        super(position, length);
        _displacementWidth = displacementWidth;
    }

    public void fix(byte[] codeRegion, int offsetToCode, int dispToBranchTarget) throws AssemblyException {
        final AssemblyInstructionEditor editor =  createAssemblyInstructionEditor(codeRegion, offsetToCode + startPosition(), size());
        editor.fixBranchRelativeDisplacement(_displacementWidth, dispToBranchTarget);
    }
}
