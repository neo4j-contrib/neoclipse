/*
 * Licensed to "Neo Technology," Network Engine for Objects in Lund AB
 * (http://neotechnology.com) under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership. Neo Technology licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License
 * at (http://www.apache.org/licenses/LICENSE-2.0). Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.neo4j.neoclipse.action.reltype;

import org.eclipse.jface.dialogs.InputDialog;
import org.neo4j.neoclipse.action.AbstractBaseAction;
import org.neo4j.neoclipse.action.Actions;
import org.neo4j.neoclipse.reltype.RelationshipTypesProvider;

/**
 * Action to create a new RelationshipType. The new type will be persisted upon
 * use in a relationship.
 * @author Anders Nawroth
 */
public class NewRelationshipTypeAction extends AbstractBaseAction
{
    private static final String NEW_RELTYPE_DIALOG_TEXT = "Please enter the name of the new relationships type";
    private static final String NEW_RELTYPE_DIALOG_TITLE = "New relationship type entry";
    protected static final int OK = 0;
    private RelationshipTypesProvider provider;

    public NewRelationshipTypeAction( RelationshipTypesProvider provider )
    {
        super( Actions.NEW_RELATIONSHIP_TYPE );
        this.provider = provider;
    }

    @Override
    public void run()
    {
        InputDialog input = new InputDialog( null, NEW_RELTYPE_DIALOG_TITLE,
            NEW_RELTYPE_DIALOG_TEXT, null, null );
        if ( input.open() == OK && input.getReturnCode() == OK )
        {
            provider.addFakeType( input.getValue() );
        }
    }
}
