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
package com.sun.max.ins.object;

import javax.swing.*;
import javax.swing.event.*;

import com.sun.max.ins.*;
import com.sun.max.ins.object.StringPane.*;
import com.sun.max.tele.*;
import com.sun.max.tele.object.*;

/**
 * An object inspector specialized for displaying a Maxine low-level character array in the {@link TeleVM}.
 *
 * @author Michael Van De Vanter
 */
public final class CharacterArrayInspector extends ObjectInspector {

    private JTabbedPane _tabbedPane;
    private ObjectPane _elementsPane;
    private StringPane _stringPane;

    // Should the alternate visualization be displayed?
    // Follows user's tab selection, but should persist when view reconstructed.
    private boolean _alternateDisplay;

    CharacterArrayInspector(Inspection inspection, ObjectInspectorFactory factory, Residence residence, TeleObject teleObject) {
        super(inspection, factory, residence, teleObject);
        // This is the default for a newly created inspector.
        // TODO (mlvdv) make this a global view option?
        _alternateDisplay = true;
        createFrame(null);
    }

    @Override
    protected synchronized void createView(long epoch) {
        super.createView(epoch);

        final TeleArrayObject teleArrayObject = (TeleArrayObject) teleObject();
        final String componentTypeName = teleArrayObject.classActorForType().componentClassActor().javaSignature(false);

        _tabbedPane = new JTabbedPane();

        _elementsPane = ObjectPane.createArrayElementsPane(this, teleArrayObject);
        _tabbedPane.add(componentTypeName + "[" + teleArrayObject.getLength() + "]", _elementsPane);

        _stringPane = StringPane.createStringPane(this, new StringSource() {
            public String fetchString() {
                final char[] chars = (char[]) teleArrayObject.shallowCopy();
                final int length = Math.min(chars.length, style().maxStringFromCharArrayDisplayLength());
                return new String(chars, 0, length);
            }
        });
        _tabbedPane.add("string value", _stringPane);

        _tabbedPane.setSelectedComponent(_alternateDisplay ? _stringPane : _elementsPane);
        _tabbedPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent event) {
                final Prober prober = (Prober) _tabbedPane.getSelectedComponent();
                // Remember which display is now selected
                _alternateDisplay = prober == _stringPane;
                // Refresh the display that is now visible.
                prober.refresh(teleVM().epoch(), true);
            }
        });
        frame().getContentPane().add(_tabbedPane);
    }

    @Override
    public void refreshView(long epoch, boolean force) {
        // Only refresh the visible pane.
        final Prober prober = (Prober) _tabbedPane.getSelectedComponent();
        prober.refresh(epoch, force);
        super.refreshView(epoch, force);
    }

}
