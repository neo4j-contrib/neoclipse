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
package org.neo4j.neoclipse.action.context;

import org.eclipse.jface.dialogs.MessageDialog;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.PropertyContainer;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.Transaction;
import org.neo4j.neoclipse.NeoIcons;
import org.neo4j.neoclipse.view.NeoGraphViewPart;

/**
 * Action to delete a node or relationship.
 * @author Anders Nawroth
 */
public class DeleteAction extends ContextAction
{
    public DeleteAction( NeoGraphViewPart neoGraphViewPart )
    {
        super( "Delete", NeoIcons.getDescriptor( NeoIcons.DELETE ),
            neoGraphViewPart );
        setEnabled( false );
    }

    @Override
    protected void performOperation( PropertyContainer container )
    {
        String question = "Are you sure you want to remove the selected ";
        if ( container instanceof Node )
        {
            question += "node?";
        }
        else if ( container instanceof Relationship )
        {
            question += "relationship?";
        }
        boolean confirmation = MessageDialog.openQuestion( null,
            "Confirm delete", question );
        if ( !confirmation )
        {
            return;
        }
        NeoService ns = getNeoService();
        if ( ns == null )
        {
            return;
        }
        Transaction tx = ns.beginTx();
        try
        {
            if ( container instanceof Node )
            {
                Node node = (Node) container;
                if ( ns.getReferenceNode().equals( node ) )
                {
                    question = "Are you really sure you want to delete the REFERENCE NODE?";
                    confirmation = MessageDialog.openQuestion( null,
                        "Confirm delete", question );
                    if ( !confirmation )
                    {
                        return;
                    }
                }
                for ( Relationship rel : node.getRelationships() )
                {
                    rel.delete();
                }
                node.delete();
            }
            else if ( container instanceof Relationship )
            {
                ((Relationship) container).delete();
            }
            tx.success();
        }
        catch ( Exception e )
        {
            MessageDialog.openError( null, "Error", "Error when deleting: "
                + e.getMessage() );
        }
        finally
        {
            tx.finish();
        }
        graphView.refreshPreserveLayout();
    }
}