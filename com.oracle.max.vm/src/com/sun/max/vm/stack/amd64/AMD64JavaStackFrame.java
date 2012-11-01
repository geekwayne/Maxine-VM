/*
 * Copyright (c) 2007, 2011, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.max.vm.stack.amd64;

import com.sun.max.annotate.*;
import com.sun.max.unsafe.*;
import com.sun.max.vm.compiler.target.*;
import com.sun.max.vm.stack.*;

@HOSTED_ONLY
public class AMD64JavaStackFrame extends VMStackFrame {

    public AMD64JavaStackFrame(StackFrame callee, TargetMethod targetMethod, Pointer ip, Pointer fp, Pointer sp) {
        super(callee, targetMethod, ip, fp, sp);
    }

    @Override
    public boolean isSameFrame(StackFrame stackFrame) {
        if (stackFrame instanceof AMD64JavaStackFrame) {
            return targetMethod().equals(stackFrame.targetMethod()) && sp.equals(stackFrame.sp);
        }
        return false;
    }
}
