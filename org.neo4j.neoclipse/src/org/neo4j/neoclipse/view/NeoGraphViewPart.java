/*
 * NeoGraphViewPart.java
 */
package org.neo4j.neoclipse.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.mylyn.zest.core.viewers.GraphViewer;
import org.eclipse.mylyn.zest.core.viewers.IGraphContentProvider;
import org.eclipse.mylyn.zest.layouts.LayoutStyles;
import org.eclipse.mylyn.zest.layouts.algorithms.SpringLayoutAlgorithm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.IPropertySourceProvider;
import org.eclipse.ui.views.properties.PropertySheetPage;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.Transaction;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.neo.NeoServiceEvent;
import org.neo4j.neoclipse.neo.NeoServiceEventListener;
import org.neo4j.neoclipse.neo.NeoServiceManager;
import org.neo4j.neoclipse.neo.NeoServiceStatus;

/**
 * This class is a view that shows the contents of a Neo database as a graph of
 * connected objects.
 * 
 * @author Peter H&auml;nsgen
 */
public class NeoGraphViewPart extends ViewPart
{
    /**
     * The property sheet page.
     */
    protected PropertySheetPage propertySheetPage;

    /**
     * The graph.
     */
    protected GraphViewer viewer;
    
    /**
     * Creates the view.
     */
    public void createPartControl(Composite parent)
    {
        viewer = new GraphViewer(parent, SWT.NONE);
        viewer.setContentProvider(new NeoGraphContentProvider());
        viewer.setLabelProvider(new NeoGraphLabelProvider());
        viewer.addDoubleClickListener(new NeoGraphDoubleClickListener());
        viewer.setLayoutAlgorithm(new SpringLayoutAlgorithm(
                LayoutStyles.NO_LAYOUT_NODE_RESIZING));

        NeoServiceManager sm = Activator.getDefault().getNeoServiceManager(); 
        sm.addServiceEventListener(new NeoGraphServiceEventListener());

        getSite().setSelectionProvider(viewer);
        
        showReferenceNode();
    }
    
    /**
     * Returns the viewer that contains the graph.
     */
    protected Viewer getViewer()
    {
        return viewer;
    }

    /**
     * This is how the framework determines which interfaces we implement.
     */
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class key)
    {
        if (key.equals(IPropertySheetPage.class))
        {
            return getPropertySheetPage();
        }
        else
        {
            return super.getAdapter(key);
        }
    }

    /**
     * This accesses a cached version of the property sheet.
     */
    public IPropertySheetPage getPropertySheetPage()
    {
        if (propertySheetPage == null)
        {
            propertySheetPage = new PropertySheetPage();
            propertySheetPage
                    .setPropertySourceProvider(new NeoGraphPropertySourceProvider());
        }

        return propertySheetPage;
    }

    /**
     * Cleans up.
     */
    public void dispose()
    {
        if (propertySheetPage != null)
        {
            propertySheetPage.dispose();
        }

        super.dispose();
    }

    /**
     * Sets the focus.
     */
    public void setFocus()
    {
    }
    
    /**
     * Focuses the view on the reference node.
     */
    public void showReferenceNode()
    {
        NeoServiceManager sm = Activator.getDefault().getNeoServiceManager(); 
        NeoService ns = sm.getNeoService();
        if (ns != null)
        {
            Transaction txn = Transaction.begin();

            try
            {
                Node node = ns.getReferenceNode();
                viewer.setInput(node);
            }
            finally
            {
                txn.finish();
            }
        }
    }

    /**
     * Focuses the view on the node with the given id.
     */
    public void showNode(long nodeId)
    {
        NeoServiceManager sm = Activator.getDefault().getNeoServiceManager(); 
        NeoService ns = sm.getNeoService();
        if (ns != null)
        {
            Transaction txn = Transaction.begin();

            try
            {
                Node node = ns.getNodeById(nodeId);
                viewer.setInput(node);
            }
            finally
            {
                txn.finish();
            }
        }
    }

    /**
     * Updates the view according to service changes.
     */
    class NeoGraphServiceEventListener implements NeoServiceEventListener
    {
        /**
         * Refreshes the input source of the view.
         */
        public void serviceChanged(NeoServiceEvent event)
        {
            if (event.getStatus() == NeoServiceStatus.STOPPED)
            {
                getViewer().setInput(null);                
            }
            else if (event.getStatus() == NeoServiceStatus.STARTED)
            {
                showReferenceNode();                
            }
        }        
    }
    
    /**
     * Resolves the properties for Neo nodes and relationships.
     */
    static class NeoGraphPropertySourceProvider implements
            IPropertySourceProvider
    {
        public IPropertySource getPropertySource(Object source)
        {
            if (source instanceof Node)
            {
                return new NeoNodePropertySource((Node) source);
            }
            else if (source instanceof Relationship)
            {
                return new NeoRelationshipPropertySource((Relationship) source);
            }
            else
            {
                return null;
            }
        }
    }

    /**
     * Resolves the properties for a Neo node.
     */
    static class NeoNodePropertySource implements IPropertySource
    {
        /**
         * The node.
         */
        private Node node;

        /**
         * The constructor.
         */
        public NeoNodePropertySource(Node node)
        {
            this.node = node;
        }

        public Object getEditableValue()
        {
            return null;
        }

        /**
         * Returns the descriptors for the properties of the node.
         */
        public IPropertyDescriptor[] getPropertyDescriptors()
        {
            Transaction txn = Transaction.begin();

            try
            {
                Iterable<String> keys = node.getPropertyKeys();

                List<IPropertyDescriptor> descs = new ArrayList<IPropertyDescriptor>();
                for (String key : keys)
                {
                    descs.add(new NeoPropertyDescriptor(key));
                }

                return descs.toArray(new IPropertyDescriptor[descs.size()]);
            }
            finally
            {
                txn.finish();
            }
        }

        /**
         * Returns the value of the given property from the node.
         */
        public Object getPropertyValue(Object id)
        {
            Transaction txn = Transaction.begin();

            try
            {
                return node.getProperty((String) id);
            }
            finally
            {
                txn.finish();
            }
        }

        /**
         * Checks if the node has a given property.
         */
        public boolean isPropertySet(Object id)
        {
            Transaction txn = Transaction.begin();

            try
            {
                return node.hasProperty((String) id);
            }
            finally
            {
                txn.finish();
            }
        }

        /**
         * Does nothing.
         */
        public void resetPropertyValue(Object id)
        {
        }

        /**
         * Does nothing.
         */
        public void setPropertyValue(Object id, Object value)
        {
        }
    }

    /**
     * Resolves the properties for a Neo relationship.
     */
    static class NeoRelationshipPropertySource implements IPropertySource
    {
        /**
         * The relationship.
         */
        private Relationship rs;

        /**
         * The constructor.
         */
        public NeoRelationshipPropertySource(Relationship rs)
        {
            this.rs = rs;
        }

        public Object getEditableValue()
        {
            return null;
        }

        /**
         * Returns the descriptors for the properties of the relationship.
         */
        public IPropertyDescriptor[] getPropertyDescriptors()
        {
            Transaction txn = Transaction.begin();

            try
            {
                Iterable<String> keys = rs.getPropertyKeys();

                List<IPropertyDescriptor> descs = new ArrayList<IPropertyDescriptor>();
                for (String key : keys)
                {
                    descs.add(new NeoPropertyDescriptor(key));
                }

                return descs.toArray(new IPropertyDescriptor[descs.size()]);
            }
            finally
            {
                txn.finish();
            }
        }

        /**
         * Returns the value of the given property.
         */
        public Object getPropertyValue(Object id)
        {
            Transaction txn = Transaction.begin();

            try
            {
                return rs.getProperty((String) id);
            }
            finally
            {
                txn.finish();
            }
        }

        /**
         * Checks if the property is set.
         */
        public boolean isPropertySet(Object id)
        {
            Transaction txn = Transaction.begin();

            try
            {
                return rs.hasProperty((String) id);
            }
            finally
            {
                txn.finish();
            }
        }

        /**
         * Does nothing.
         */
        public void resetPropertyValue(Object id)
        {
        }

        /**
         * Does nothing.
         */
        public void setPropertyValue(Object id, Object value)
        {
        }
    }

    /**
     * Describes a single property of a Neo node or relationship.
     */
    static class NeoPropertyDescriptor implements IPropertyDescriptor
    {
        /**
         * The property name.
         */
        private String key;

        /**
         * The constructor.
         */
        public NeoPropertyDescriptor(String key)
        {
            this.key = key;
        }

        public CellEditor createPropertyEditor(Composite parent)
        {
            return null;
        }

        public String getCategory()
        {
            return null;
        }

        public String getDescription()
        {
            return "The property with the name '" + key + "'.";
        }

        public String getDisplayName()
        {
            return key;
        }

        public String[] getFilterFlags()
        {
            return null;
        }

        public Object getHelpContextIds()
        {
            return null;
        }

        public Object getId()
        {
            return key;
        }

        public ILabelProvider getLabelProvider()
        {
            return null;
        }

        public boolean isCompatibleWith(IPropertyDescriptor anotherProperty)
        {
            return false;
        }
    }

    /**
     * Handles double clicks on graph figures.
     */
    static class NeoGraphDoubleClickListener implements IDoubleClickListener
    {
        /**
         * Sets the selected node as input for the viewer.
         */
        public void doubleClick(DoubleClickEvent event)
        {
            StructuredSelection sel = (StructuredSelection) event
                    .getSelection();
            Object s = sel.getFirstElement();
            if ((s != null) && (s instanceof Node))
            {
                Transaction txn = Transaction.begin();

                try
                {
                    Viewer viewer = event.getViewer();
                    viewer.setInput(s);
                }
                finally
                {
                    txn.finish();
                }
            }
        }
    }

    /**
     * Provides the elements that must be displayed in the graph.
     */
    static class NeoGraphContentProvider implements IGraphContentProvider
    {
        /**
         * Returns the end node for a relationship.
         */
        public Object getDestination(Object rel)
        {
            Relationship rs = (Relationship) rel;
            return rs.getEndNode();
        }

        /**
         * Returns the relationships of the input node.
         */
        public Object[] getElements(Object input)
        {
            Node node = (Node) input;

            // collect all relationships to be displayed
            // TODO check for some maximum count in order to limit number of
            // nodes
            // TODO retrieve with configurable nesting level
            List<Relationship> rels = new ArrayList<Relationship>();

            Iterable<Relationship> rs = node.getRelationships();
            for (Relationship r : rs)
            {
                rels.add(r);
            }

            return rels.toArray();
        }

        /**
         * Returns the start node of a relationship.
         */
        public Object getSource(Object rel)
        {
            Relationship rs = (Relationship) rel;
            return rs.getStartNode();
        }

        public double getWeight(Object connection)
        {
            return 0;
        }

        public void dispose()
        {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
        {
        }
    }

    /**
     * Provides the labels for graph elements.
     */
    static class NeoGraphLabelProvider extends LabelProvider
    {
        /**
         * The icon for nodes.
         */
        // TODO use neo icons
        // private Image nodeImage = Activator.getDefault().getImageRegistry().get(NeoIcons.SMALL);

        private Image nodeImage = Display.getDefault().getSystemImage(
                SWT.ICON_INFORMATION);

        /**
         * The icon for the root node.
         */
        // TODO use neo icons
        private Image rootImage = Display.getDefault().getSystemImage(
                SWT.ICON_WARNING);

        /**
         * Returns the icon for an element.
         */
        public Image getImage(Object element)
        {
            if (element instanceof Node)
            {
                Long id = ((Node) element).getId();
                if (id.longValue() == 0L)
                {
                    return rootImage;
                }
                else
                {
                    return nodeImage;
                }
            }

            return null;
        }

        /**
         * Returns the text for an element.
         */
        public String getText(Object element)
        {
            if (element instanceof Node)
            {
                Node node = (Node) element;
                if (node.getId() == 0)
                {
                    return "Reference Node";
                }
                else
                {
                    return "Node " + String.valueOf(((Node) element).getId());
                }
            }
            else if (element instanceof Relationship)
            {
                return String.valueOf(((Relationship) element).getId());
            }

            return element.toString();
        }
    }
}
