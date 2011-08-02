/*
 * Copyright (c) 2010, 2011, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.max.vm.heap.gcx;
import static com.sun.max.vm.heap.gcx.HeapRegionConstants.*;

/**
 * Generic iterator over a {@link HeapRegionList}.
 */
public abstract class HeapRegionListIterable {
    HeapRegionList regionList;
    int cursor = INVALID_REGION_ID;

    HeapRegionListIterable() {
    }

    /**
     * Specifies to the iterator what region list it should iterate over.
     * @param regionList
     */
    void initialize(HeapRegionList regionList) {
        this.regionList = regionList;
    }

    /**
     * Reset the iterator to the head of the region list.
     */
    void reset() {
        cursor = regionList.head();
    }

    /**
     * Indicate whether the end of the region list is reached.
     * @return true if there are more region to iterate over
     */
    public boolean hasNext() {
        // Note: the tail of a HeapRegionList should have the invalid region as next field.
        return cursor != INVALID_REGION_ID;
    }

}