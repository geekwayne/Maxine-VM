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
package com.sun.c1x.ri;

import com.sun.c1x.ci.CiKind;
import com.sun.c1x.ci.CiLocation;
import com.sun.c1x.ci.CiRegister;
import com.sun.c1x.ci.CiTargetMethod;

/**
 * The <code>RiRuntime</code> class provides the major interface between the compiler and the
 * runtime system, including access to constant pools, OSR frames, inlining requirements,
 * and runtime calls such as checkcast. C1X may insert calls to the
 * implementation of these methods into compiled code, typically as the slow path.
 *
 * @author Ben L. Titzer
 * @author Thomas Wuerthinger
 */
public interface RiRuntime {
    /**
     * Gets the constant pool for a method.
     * @param method the method
     * @return the constant pool for the method
     */
    RiConstantPool getConstantPool(RiMethod method);

    /**
     * Gets an {@link RiOsrFrame OSR frame} instance for the specified method
     * at the specified OSR bytecode index.
     * @param method the method
     * @param bci the bytecode index
     * @return an OSR frame that describes the layout of the frame
     */
    RiOsrFrame getOsrFrame(RiMethod method, int bci);

    /**
     * Checks whether the specified method is required to be inlined (for semantic reasons).
     * @param method the method being called
     * @return {@code true} if the method must be inlined; {@code false} to let the compiler
     * use its own heuristics
     */
    boolean mustInline(RiMethod method);

    /**
     * Checks whether the specified method must not be inlined (for semantic reasons).
     * @param method the method being called
     * @return {@code true} if the method must not be inlined; {@code false} to let the compiler
     * use its own heuristics
     */
    boolean mustNotInline(RiMethod method);

    /**
     * Checks whether the specified method cannot be compiled.
     * @param method the method being called
     * @return {@code true} if the method cannot be compiled
     */
    boolean mustNotCompile(RiMethod method);

    /**
     * Byte offset of the array length of an array object.
     * @return the byte offset of the array length
     */
    int arrayLengthOffsetInBytes();

    /**
     * The header size of an object in words.
     * @return the header size in words
     */
    int headerSize();

    /**
     * Byte offset of the field of an object that contains the pointer to the internal class representation of the type of the object.
     * @return the byte offset of the class field
     */
    int hubOffsetInBytes();

    /**
     * Byte offset of the field of the internal thread representation that contains the pointer to the thread exception object.
     * @return the byte offset of the exception object field
     */
    int threadExceptionOopOffset();

    /**
     * Byte offset of the field of the internal thread representation that contains the pointer to the Java thread object.
     * @return the byte offset of the thread object field
     */
    int threadObjOffset();

    /**
     * Byte offset of the field of the internal class representation that contains the pointer to the Java class object.
     * @return the byte offset of the class object field
     */
    int klassJavaMirrorOffsetInBytes();

    /**
     * Checks whether an explicit null check is needed with the given offset of accessing an object.
     * If this the offset is low, then an implicit null check will work.
     * @param offset the offset at which the object is accessed
     * @return true if an explicit null check is needed, false otherwise
     */
    boolean needsExplicitNullCheck(int offset);

    /**
     * Checks whether we are on a multiprocessor system.
     * @return true if we are on a multiprocessor system, false otherwise
     */
    boolean isMP();

    /**
     * Byte offset of the limit field of java.nio.Buffer.
     * @return the byte offset of the limit field
     */
    int javaNioBufferLimitOffset();

    /**
     * Checks whether jvmti can post exceptions.
     * @return true if jvmti can post exceptions, false otherwise.
     */
    boolean jvmtiCanPostExceptions();

    /**
     * Byte offset of the virtual method table of an internal class object.
     * @return the virtual method table offset in bytes
     */
    int vtableStartOffset();

    /**
     * Byte size of a single virtual method table entry of an internal class object.
     * @return the virtual method table entry
     */
    int vtableEntrySize();

    /**
     * Byte offset of the method field of a virtual method table entry.
     * @return the method offset in bytes
     */
    int vtableEntryMethodOffsetInBytes();

    /**
     * Resolves a given identifier to a type.
     * @param string the name of the type
     * @return the resolved type
     */
    RiType resolveType(String string);

    int firstArrayElementOffsetInBytes(CiKind type);

    int sunMiscAtomicLongCSImplValueOffset();

    int arrayHeaderSize(CiKind type);

    int interpreterFrameMonitorSize();

    int basicObjectLockSize();

    int basicObjectLockOffsetInBytes();

    int initStateOffsetInBytes();

    int instanceKlassFullyInitialized();

    int elementKlassOffsetInBytes();

    int methodDataNullSeenByteConstant();

    int secondarySuperCacheOffsetInBytes();

    int markOffsetInBytes();

    int threadTlabTopOffset();

    int threadTlabEndOffset();

    int threadTlabStartOffset();

    int superCheckOffsetOffsetInBytes();

    int secondarySupersOffsetInBytes();

    int threadTlabSizeOffset();

    int vtableLengthOffset();

    int itableMethodEntryMethodOffset();

    int itableOffsetEntrySize();

    int itableInterfaceOffsetInBytes();

    int itableOffsetOffsetInBytes();

    int biasedLockMaskInPlace();

    int biasedLockPattern();

    int maxArrayAllocationLength();

    int prototypeHeaderOffsetInBytes();

    int markOopDescPrototype();

    int klassPartOffsetInBytes();

    int getMinObjAlignmentInBytesMask();

    int instanceOopDescBaseOffsetInBytes();

    // TODO: why not pass the RiSignature instead?
    CiLocation[] javaCallingConvention(CiKind[] types, boolean outgoing);

    CiLocation[] runtimeCallingConvention(CiKind[] signature);

    CiRegister returnRegister(CiKind object);

    int overflowArgumentsSize(CiKind basicType);

    int getJITStackSlotSize();

    int sizeofBasicObjectLock();

    int codeOffset();

    String disassemble(byte[] copyOf);

    Object registerTargetMethod(CiTargetMethod targetMethod, String name);

    RiType primitiveArrayType(CiKind elemType);

    CiRegister threadRegister();

    public RiType getRiType(Class<?> javaClass);

    CiRegister getSafepointRegister();
}