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
/*VCSID=40a8212e-106f-4d33-8e83-da64cc134ea5*/
package com.sun.max.vm.run.jitTest;

import com.sun.max.vm.*;
import com.sun.max.vm.debug.*;
import com.sun.max.vm.prototype.*;
import com.sun.max.vm.run.java.*;

/**
 * Basic RunScheme for testing JIT output.
 * @author Laurent Daynes
 */
public class JitTestRunScheme extends JavaRunScheme {

    public JitTestRunScheme(VMConfiguration vmConfiguration) {
        super(vmConfiguration);
    }

    @Override
    public void run() {
        initializeBasicFeatures();
        Debug.println("*** begin of JIT tests");
        JitTest.test();
        Debug.println("*** end of JIT tests");
    }

    @Override
    public void initialize(MaxineVM.Phase phase) {
        if (MaxineVM.isPrototyping()) {
            CompiledPrototype.registerJitClass(JitTest.class);
        }
    }
}
