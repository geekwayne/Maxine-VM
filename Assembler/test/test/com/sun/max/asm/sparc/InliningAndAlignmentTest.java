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
/*VCSID=15ac55a1-f428-49f1-990d-5f7a84709c7a*/
package test.com.sun.max.asm.sparc;

import java.io.*;

import junit.framework.*;
import test.com.sun.max.asm.*;

import com.sun.max.asm.*;
import com.sun.max.asm.Assembler.*;
import com.sun.max.asm.dis.sparc.*;
import com.sun.max.asm.sparc.complete.*;
import com.sun.max.ide.*;

/**
 * @author David Liu
 */
public class InliningAndAlignmentTest extends MaxTestCase {

    public InliningAndAlignmentTest() {
        super();

    }

    public InliningAndAlignmentTest(String name) {
        super(name);
    }

    public static Test suite() {
        final TestSuite suite = new TestSuite(InliningAndAlignmentTest.class.getName());
        //$JUnit-BEGIN$
        suite.addTestSuite(InliningAndAlignmentTest.class);
        //$JUnit-END$
        return suite;
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(InliningAndAlignmentTest.class);
    }

    private void disassemble(SPARCDisassembler disassembler, byte[] bytes) throws IOException, AssemblyException {
        final BufferedInputStream stream = new BufferedInputStream(new ByteArrayInputStream(bytes));
        disassembler.scanAndPrint(stream, System.out);
    }

    private byte[] assembleInlineData(SPARCAssembler asm, long startAddress, int pointerSize) throws IOException, AssemblyException {
        final Directives dir = asm.directives();
        final Label label1 = new Label();

        asm.ba(label1);
        asm.nop();

        final byte byteValue = (byte) 0x77;
        final Label inlinedByte = new Label();
        asm.bindLabel(inlinedByte);
        dir.inlineByte(byteValue);

        final short shortValue = (short) 0xABCD;
        final Label inlinedShort = new Label();
        asm.bindLabel(inlinedShort);
        dir.inlineShort(shortValue);

        final int intValue = 0x12345678;
        final Label inlinedInt = new Label();
        asm.bindLabel(inlinedInt);
        dir.inlineInt(intValue);

        final long longValue = 0x12345678CAFEBABEL;
        final Label inlinedLong = new Label();
        asm.bindLabel(inlinedLong);
        dir.inlineLong(longValue);

        final byte[] byteArrayValue = {1, 2, 3, 4, 5};
        final Label inlinedByteArray = new Label();
        asm.bindLabel(inlinedByteArray);
        dir.inlineByteArray(byteArrayValue);

        final Label labelValue = label1;
        final Label inlinedLabel = new Label();
        asm.bindLabel(inlinedLabel);
        dir.inlineAddress(labelValue);

        final Label inlinedPaddingByte = new Label();
        asm.bindLabel(inlinedPaddingByte);
        dir.inlineByte((byte) 0);

        final Label unalignedLabel = new Label();
        asm.bindLabel(unalignedLabel);

        dir.align(4);
        asm.bindLabel(label1);
        asm.nop();

        // retrieve the byte stream output of the assembler and confirm that the inlined data is in the expected format, and are aligned correctly
        final byte[] asmBytes = asm.toByteArray();

        assertTrue(ByteUtils.checkBytes(ByteUtils.toByteArray(byteValue), asmBytes, inlinedByte.position()));
        assertEquals(1, inlinedShort.position() - inlinedByte.position());

        assertTrue(ByteUtils.checkBytes(ByteUtils.toBigEndByteArray(shortValue), asmBytes, inlinedShort.position()));
        assertEquals(2, inlinedInt.position() - inlinedShort.position());

        assertTrue(ByteUtils.checkBytes(ByteUtils.toBigEndByteArray(intValue), asmBytes, inlinedInt.position()));
        assertEquals(4, inlinedLong.position() - inlinedInt.position());

        assertTrue(ByteUtils.checkBytes(ByteUtils.toBigEndByteArray(0x12345678CAFEBABEL), asmBytes, inlinedLong.position()));
        assertEquals(8, inlinedByteArray.position() - inlinedLong.position());

        assertTrue(ByteUtils.checkBytes(byteArrayValue, asmBytes, inlinedByteArray.position()));
        assertEquals(5, inlinedLabel.position() - inlinedByteArray.position());

        if (pointerSize == 4) {
            assertTrue(ByteUtils.checkBytes(ByteUtils.toBigEndByteArray((int) (startAddress + labelValue.position())), asmBytes, inlinedLabel.position()));
        } else if (pointerSize == 8) {
            assertTrue(ByteUtils.checkBytes(ByteUtils.toBigEndByteArray(startAddress + labelValue.position()), asmBytes, inlinedLabel.position()));
        }
        assertEquals(pointerSize, inlinedPaddingByte.position() - inlinedLabel.position());

        return asmBytes;
    }

    public void testInlineData32() throws IOException, AssemblyException {
        System.out.println("--- testInlineData32: ---");
        final int startAddress = 0x12345678;
        final SPARC32Assembler assembler = new SPARC32Assembler(startAddress);
        final SPARC32Disassembler disassembler = new SPARC32Disassembler(startAddress);
        final byte[] bytes = assembleInlineData(assembler, startAddress, 4);
        disassemble(disassembler, bytes);
        System.out.println();
    }

    public void testInlineData64() throws IOException, AssemblyException {
        System.out.println("--- testInlineData64: ---");
        final long startAddress = 0x1234567812340000L;
        final SPARC64Assembler assembler = new SPARC64Assembler(startAddress);
        final SPARC64Disassembler disassembler = new SPARC64Disassembler(startAddress);
        final byte[] bytes = assembleInlineData(assembler, startAddress, 8);
        disassemble(disassembler, bytes);
        System.out.println();
    }

    private byte[] assembleAlignmentPadding(SPARCAssembler asm, long startAddress) throws IOException, AssemblyException {
        // test memory alignment directives from 1 byte to 16 bytes
        final Directives dir = asm.directives();

        final Label unalignedLabel1 = new Label();
        final Label alignedLabel1 = new Label();

        final Label unalignedLabel2 = new Label();
        final Label alignedLabel2 = new Label();

        final Label unalignedLabel4By1 = new Label();
        final Label alignedLabel4By1 = new Label();

        final Label unalignedLabel4By2 = new Label();
        final Label alignedLabel4By2 = new Label();

        final Label unalignedLabel4By3 = new Label();
        final Label alignedLabel4By3 = new Label();

        final Label unalignedLabel8 = new Label();
        final Label alignedLabel8 = new Label();

        final Label unalignedLabel16 = new Label();
        final Label alignedLabel16 = new Label();

        final Label done = new Label();

        asm.ba(done);
        dir.inlineByteArray(new byte[]{1}); // padding to make the following unaligned by 1 byte
        asm.bindLabel(unalignedLabel1);
        dir.align(1);
        asm.bindLabel(alignedLabel1);
        asm.nop();

        dir.align(4);
        asm.ba(done);
        dir.inlineByteArray(new byte[]{1}); // padding to make the following unaligned by 1 byte
        asm.bindLabel(unalignedLabel2);
        dir.align(2);
        asm.bindLabel(alignedLabel2);
        asm.nop();

        dir.align(4);
        asm.ba(done);
        dir.inlineByteArray(new byte[]{1}); // padding to make the following unaligned by 1 byte
        asm.bindLabel(unalignedLabel4By1);
        dir.align(4);
        asm.bindLabel(alignedLabel4By1);
        asm.nop();

        dir.align(4);
        asm.ba(done);
        dir.inlineByteArray(new byte[]{1, 2}); // padding to make the following unaligned by 2 bytes
        asm.bindLabel(unalignedLabel4By2);
        dir.align(4);
        asm.bindLabel(alignedLabel4By2);
        asm.nop();

        dir.align(4);
        asm.ba(done);
        dir.inlineByteArray(new byte[]{1, 2, 3}); // padding to make the following unaligned by 3 bytes
        asm.bindLabel(unalignedLabel4By3);
        dir.align(4);
        asm.bindLabel(alignedLabel4By3);
        asm.nop();

        dir.align(4);
        asm.ba(done);
        dir.inlineByteArray(new byte[]{1}); // padding to make the following unaligned by 1 byte
        asm.bindLabel(unalignedLabel8);
        dir.align(8);
        asm.bindLabel(alignedLabel8);
        asm.nop();

        dir.align(4);
        asm.ba(done);
        dir.inlineByteArray(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9});  // padding to make the following unaligned by 1 byte
        asm.bindLabel(unalignedLabel16);
        dir.align(16);
        asm.bindLabel(alignedLabel16);
        asm.nop();

        dir.align(4);
        asm.bindLabel(done);
        asm.nop();

        // check the memory alignment (and that the memory locations were unaligned before the alignment directives)
        final byte[] asmCode = asm.toByteArray();

        assertEquals(1, (startAddress + unalignedLabel2.position()) % 2);
        assertEquals(0, (startAddress + alignedLabel2.position()) % 2);

        assertEquals(1, (startAddress + unalignedLabel4By1.position()) % 4);
        assertEquals(0, (startAddress + alignedLabel4By1.position()) % 4);

        assertEquals(2, (startAddress + unalignedLabel4By2.position()) % 4);
        assertEquals(0, (startAddress + alignedLabel4By2.position()) % 4);

        assertEquals(3, (startAddress + unalignedLabel4By3.position()) % 4);
        assertEquals(0, (startAddress + alignedLabel4By3.position()) % 4);

        assertEquals(1, (startAddress + unalignedLabel8.position()) % 8);
        assertEquals(0, (startAddress + alignedLabel8.position()) % 8);

        assertEquals(1, (startAddress + unalignedLabel16.position()) % 16);
        assertEquals(0, (startAddress + alignedLabel16.position()) % 16);

        assertEquals(0, (startAddress + done.position()) % 4);

        return asmCode;
    }

    public void testAlignmentPadding32() throws IOException, AssemblyException {
        System.out.println("--- testAlignmentPadding32: ---");
        final int startAddress = 0x12345678;
        final SPARC32Assembler assembler = new SPARC32Assembler(startAddress);
        final SPARC32Disassembler disassembler = new SPARC32Disassembler(startAddress);
        final byte[] bytes = assembleAlignmentPadding(assembler, startAddress);
        disassemble(disassembler, bytes);
        System.out.println();
    }

    public void testAlignmentPadding64() throws IOException, AssemblyException {
        System.out.println("--- testAlignmentPadding64: ---");
        final long startAddress = 0x1234567812345678L;
        final SPARC64Assembler assembler = new SPARC64Assembler(startAddress);
        final SPARC64Disassembler disassembler = new SPARC64Disassembler(startAddress);
        final byte[] bytes = assembleAlignmentPadding(assembler, startAddress);
        disassemble(disassembler, bytes);
        System.out.println();
    }
}
