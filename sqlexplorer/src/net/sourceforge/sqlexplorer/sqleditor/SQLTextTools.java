package net.sourceforge.sqlexplorer.sqleditor;

/*
 * Copyright (C) 2002-2004 Andrea Mazzolini
 * andreamazzolini@users.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.sessiontree.model.utility.Dictionary;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * Tools required to configure a Java text viewer. The color manager and all
 * scanner exist only one time, i.e. the same instances are returned to all
 * clients. Thus, clients share those tools.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class SQLTextTools {

    private class PreferenceListener implements IPropertyChangeListener {
        public void propertyChange(PropertyChangeEvent event) {
            adaptToPreferenceChange(event);
        }
    };

    private SQLColorManager fColorManager;

    private SQLCodeScanner fCodeScanner;

    private SingleTokenSQLScanner fMultilineCommentScanner;

    private SingleTokenSQLScanner fSinglelineCommentScanner;

    private SingleTokenSQLScanner fStringScanner;

    private SQLPartitionScanner fPartitionScanner;

    /** The preference store */
    private IPreferenceStore fPreferenceStore;

    /** The preference change listener */
    private PreferenceListener fPreferenceListener = new PreferenceListener();

    private Dictionary dictionary;

    /**
     * Creates a new Java text tools collection.
     */
    public SQLTextTools(IPreferenceStore store, Dictionary dictionary) {
        // public SQLTextTools(IPreferenceStore store) {
        fPreferenceStore = store;
        this.dictionary = dictionary;
        fPreferenceStore.addPropertyChangeListener(fPreferenceListener);

        fColorManager = new SQLColorManager();
        fCodeScanner = new SQLCodeScanner(fColorManager, store, dictionary);
        fMultilineCommentScanner = new SingleTokenSQLScanner(fColorManager,
                store, IConstants.SQL_COLOR_MULTILINE_COMMENT);
        fSinglelineCommentScanner = new SingleTokenSQLScanner(fColorManager,
                store, IConstants.SQL_COLOR_SINGLE_LINE_COMMENT);
        fStringScanner = new SingleTokenSQLScanner(fColorManager, store,
                IConstants.SQL_COLOR_STRING);
        fPartitionScanner = new SQLPartitionScanner();

    }

    public void setNewDictionary(Dictionary newDictionary) {
        dictionary = newDictionary;
        fCodeScanner = new SQLCodeScanner(fColorManager, fPreferenceStore,
                newDictionary);
    }

    public Dictionary getDictionary() {
        return dictionary;
    }

    /**
     * Disposes all the individual tools of this tools collection.
     */
    public void dispose() {

        fCodeScanner = null;
        fMultilineCommentScanner = null;
        fSinglelineCommentScanner = null;
        fStringScanner = null;
        fPartitionScanner = null;

        if (fColorManager != null) {
            fColorManager.dispose();
            fColorManager = null;
        }

        if (fPreferenceStore != null) {
            fPreferenceStore.removePropertyChangeListener(fPreferenceListener);
            fPreferenceStore = null;
            fPreferenceListener = null;
        }
    }

    public RuleBasedScanner getCodeScanner() {
        return fCodeScanner;
    }

    public RuleBasedScanner getMultilineCommentScanner() {
        return fMultilineCommentScanner;
    }

    public RuleBasedScanner getSinglelineCommentScanner() {
        return fSinglelineCommentScanner;
    }

    public RuleBasedScanner getStringScanner() {
        return fStringScanner;
    }

    public IPartitionTokenScanner getPartitionScanner() {
        return fPartitionScanner;
    }

    public IDocumentPartitioner createDocumentPartitioner() {

        String[] types = new String[] { IDocument.DEFAULT_CONTENT_TYPE,
                IConstants.SQL_COLOR_MULTILINE_COMMENT,
                IConstants.SQL_COLOR_SINGLE_LINE_COMMENT,
                IConstants.SQL_COLOR_STRING };

        return new FastPartitioner(getPartitionScanner(), types);
    }

    /**
     * Determines whether the preference change encoded by the given event
     * changes the behavior of one its contained components.
     * 
     * @param event
     *            the event to be investigated
     * @return <code>true</code> if event causes a behavioral change
     */
    public boolean affectsBehavior(PropertyChangeEvent event) {
        return fCodeScanner.affectsBehavior(event)
                || fMultilineCommentScanner.affectsBehavior(event)
                || fSinglelineCommentScanner.affectsBehavior(event)
                || fStringScanner.affectsBehavior(event);
    }

    /**
     * Adapts the behavior of the contained components to the change encoded in
     * the given event.
     * 
     * @param event
     *            the event to whch to adapt
     */
    protected void adaptToPreferenceChange(PropertyChangeEvent event) {
        if (fCodeScanner.affectsBehavior(event))
            fCodeScanner.adaptToPreferenceChange(event);
        if (fMultilineCommentScanner.affectsBehavior(event))
            fMultilineCommentScanner.adaptToPreferenceChange(event);
        if (fSinglelineCommentScanner.affectsBehavior(event))
            fSinglelineCommentScanner.adaptToPreferenceChange(event);
        if (fStringScanner.affectsBehavior(event))
            fStringScanner.adaptToPreferenceChange(event);
    }

}