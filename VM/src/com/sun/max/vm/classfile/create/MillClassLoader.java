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
package com.sun.max.vm.classfile.create;

import com.sun.max.lang.*;
import com.sun.max.vm.classfile.*;

/**
 * ClassLoader that makes a class from an array of bytes.
 *
 * @author Bernd Mathiske
 */
public class MillClassLoader extends ClassLoader {

    private final byte[] classfileBytes;

    MillClassLoader(byte[] classfileBytes) {
        this.classfileBytes = classfileBytes.clone();
    }

    @Override
    public Class<?> findClass(String name) {
        final Class result = defineClass(name, classfileBytes, 0, classfileBytes.length);
        ClassfileReader.defineClassActor(name, this, classfileBytes, null, null, false);
        return result;
    }

    public static Class makeClass(String name, byte[] classfileBytes) {
        final MillClassLoader classLoader = new MillClassLoader(classfileBytes);
        synchronized (classLoader) {
            final Class result = Classes.load(classLoader, name);
            classLoader.resolveClass(result);
            return result;
        }
    }

}
