/**
 * Licensed to Neo Technology under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Neo Technology licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.neo4j.neoclipse.preference;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.neo4j.neoclipse.graphdb.GraphDbServiceMode;

/**
 * The page for neo4j preferences.
 * 
 * @author Peter H&auml;nsgen
 * @author Anders Nawroth
 * @author Radhakrishna Kalyan
 */
public class PreferencePage extends AbstractPreferencePage
{
    private static final String NEO4J_CONNECTION_MODE = "Connection mode:";
    private static final String HELP_ON_START_LABEL = "Show help view on startup";
    private RadioGroupFieldEditor connectionMode;
    private IntegerFieldEditor maxNodesField;
    private IntegerFieldEditor maxTraversalDepthField;

    /**
     * Initializes the several input fields.
     */
    @Override
    protected void createFieldEditors()
    {
        // connection mode
        String[][] labels = {
                { "read/write embedded",
                    GraphDbServiceMode.READ_WRITE_EMBEDDED.name() },
                    { "read-only embedded",
                        GraphDbServiceMode.READ_ONLY_EMBEDDED.name() } };
        connectionMode = new RadioGroupFieldEditor(
                Preferences.CONNECTION_MODE, NEO4J_CONNECTION_MODE, 1, labels,
                getFieldEditorParent() );
        addField( connectionMode );


        // show help view on startup
        BooleanFieldEditor helpOnStart = new BooleanFieldEditor(
                Preferences.HELP_ON_START, HELP_ON_START_LABEL,
                getFieldEditorParent() );
        addField( helpOnStart );

        maxNodesField = new IntegerFieldEditor( Preferences.MAX_NODES,
                "Maximum number of nodes", getFieldEditorParent(), 4 );
        maxNodesField.setEmptyStringAllowed( false );
        addField( maxNodesField );

        maxTraversalDepthField = new IntegerFieldEditor( Preferences.MAX_TRAVERSAL_DEPTH, "Maximum Traversal Depth",
                getFieldEditorParent(), 4 );
        maxTraversalDepthField.setEmptyStringAllowed( false );
        addField( maxTraversalDepthField );
    }
}
