/*
 * NeoGraphViewPart.java
 */
package org.neo4j.neoclipse.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.IPropertySourceProvider;
import org.eclipse.ui.views.properties.PropertySheetPage;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.viewers.IGraphEntityRelationshipContentProvider;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.SpringLayoutAlgorithm;
import org.neo4j.api.core.Direction;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.Transaction;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.NeoIcons;
import org.neo4j.neoclipse.action.DecreaseTraversalDepthAction;
import org.neo4j.neoclipse.action.IncreaseTraversalDepthAction;
import org.neo4j.neoclipse.action.RefreshAction;
import org.neo4j.neoclipse.action.ShowGridLayoutAction;
import org.neo4j.neoclipse.action.ShowRadialLayoutAction;
import org.neo4j.neoclipse.action.ShowReferenceNodeAction;
import org.neo4j.neoclipse.action.ShowSpringLayoutAction;
import org.neo4j.neoclipse.action.ShowTreeLayoutAction;
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
     * The Eclipse view ID.
     */
    public static final String ID = "org.neo4j.neoclipse.view.NeoGraphViewPart";
    
    /**
     * The property sheet page.
     */
    protected PropertySheetPage propertySheetPage;

    /**
     * The graph.
     */
    protected GraphViewer viewer;
    
    /**
     * The decrease traversal depth action.
     */
    protected DecreaseTraversalDepthAction decAction;

    /**
     * The depth how deep we should traverse into the network.
     */
    private int traversalDepth = 1;
    
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

        makeContributions();
        
        NeoServiceManager sm = Activator.getDefault().getNeoServiceManager(); 
        sm.addServiceEventListener(new NeoGraphServiceEventListener());

        getSite().setSelectionProvider(viewer);
        
        showReferenceNode();
    }
    
    /**
     * Initializes menus, toolbars etc.
     */
    protected void makeContributions()
    {
        // initialize actions
        IToolBarManager tm = getViewSite().getActionBars().getToolBarManager();
        IMenuManager mm = getViewSite().getActionBars().getMenuManager();
        
        // standard actions
        {
            ShowReferenceNodeAction refNodeAction = new ShowReferenceNodeAction(this);
            refNodeAction.setText("Show Reference Node");
            refNodeAction.setToolTipText("Show Reference Node");
            refNodeAction.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(NeoIcons.HOME));
            
            tm.add(refNodeAction);
            
            RefreshAction refreshAction = new RefreshAction(this);
            refreshAction.setText("Refresh");
            refreshAction.setToolTipText("Refresh");
            refreshAction.setImageDescriptor(
                    Activator.getDefault().getImageRegistry().getDescriptor(NeoIcons.REFRESH));
            
            tm.add(refreshAction);
            tm.add(new Separator());
        }
        
        // recursion level actions
        {
            IncreaseTraversalDepthAction incAction = new IncreaseTraversalDepthAction(this);
            incAction.setText("Increase Traversal Depth");
            incAction.setToolTipText("Increase Traversal Depth");
            incAction.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(NeoIcons.PLUS_ENABLED));
            incAction.setDisabledImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(NeoIcons.PLUS_DISABLED));
            
            tm.add(incAction);
            
            decAction = new DecreaseTraversalDepthAction(this);
            decAction.setText("Decrease Traversal Depth");
            decAction.setToolTipText("Decrease Traversal Depth");
            decAction.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(NeoIcons.MINUS_ENABLED));
            decAction.setDisabledImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(NeoIcons.MINUS_DISABLED));
            
            tm.add(decAction);
            tm.add(new Separator());
        }
        
        // layout actions
        {
            String groupName = "layout";
            GroupMarker layoutGroup = new GroupMarker(groupName);
            tm.add(layoutGroup);
            mm.add(layoutGroup);
    
            // spring layout
            ShowSpringLayoutAction springLayoutAction = new ShowSpringLayoutAction(this);
            springLayoutAction.setText("Spring Layout");
            springLayoutAction.setToolTipText("Spring Layout");
            springLayoutAction.setImageDescriptor(NeoIcons.getDescriptor(NeoIcons.SPRING));
            springLayoutAction.setChecked(true);
    
            tm.appendToGroup(groupName, springLayoutAction);
            mm.appendToGroup(groupName, springLayoutAction);
    
            // tree layout
            ShowTreeLayoutAction treeLayoutAction = new ShowTreeLayoutAction(this);
            treeLayoutAction.setText("Tree Layout");
            treeLayoutAction.setToolTipText("Tree Layout");
            treeLayoutAction.setImageDescriptor(NeoIcons.getDescriptor(NeoIcons.TREE));
            treeLayoutAction.setChecked(false);
    
            tm.appendToGroup(groupName, treeLayoutAction);
            mm.appendToGroup(groupName, treeLayoutAction);
            
            // radial layout
            ShowRadialLayoutAction radialLayoutAction = new ShowRadialLayoutAction(this);
            radialLayoutAction.setText("Radial Layout");
            radialLayoutAction.setToolTipText("Radial Layout");
            radialLayoutAction.setImageDescriptor(NeoIcons.getDescriptor(NeoIcons.RADIAL));
            radialLayoutAction.setChecked(false);
    
            tm.appendToGroup(groupName, radialLayoutAction);
            mm.appendToGroup(groupName, radialLayoutAction);
            
            // grid layout
            ShowGridLayoutAction gridLayoutAction = new ShowGridLayoutAction(this);
            gridLayoutAction.setText("Grid Layout");
            gridLayoutAction.setToolTipText("Grid Layout");
            gridLayoutAction.setImageDescriptor(NeoIcons.getDescriptor(NeoIcons.GRID));
            gridLayoutAction.setChecked(false);
    
            tm.appendToGroup(groupName, gridLayoutAction);
            mm.appendToGroup(groupName, gridLayoutAction);
        }        
    }
    
    /**
     * Updates the content of the status bar.
     */
    protected void refreshStatusBar()
    {
        getViewSite().getActionBars().getStatusLineManager().setMessage(
                "Traversal Depth: " + String.valueOf(traversalDepth));
    }
    
    /**
     * Returns the viewer that contains the graph.
     */
    public GraphViewer getViewer()
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
        viewer.getControl().setFocus();
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
     * Focuses the view on the given node.
     */
    public void showNode(Node node)
    {
        NeoServiceManager sm = Activator.getDefault().getNeoServiceManager(); 
        NeoService ns = sm.getNeoService();
        if (ns != null)
        {
            Transaction txn = Transaction.begin();

            try
            {
                viewer.setInput(node);
            }
            finally
            {
                txn.finish();
            }
        }
    }

    /**
     * Returns the current traversal depth.
     */
    public int getTraversalDepth()
    {
        return traversalDepth;
    }

    /**
     * Increments the traversal depth.
     */
    public void incTraversalDepth()
    {
        NeoServiceManager sm = Activator.getDefault().getNeoServiceManager(); 
        NeoService ns = sm.getNeoService();
        if (ns != null)
        {
            Transaction txn = Transaction.begin();

            try
            {
                traversalDepth++;
                refreshStatusBar();
                
                viewer.refresh();
                viewer.applyLayout();
            }
            finally
            {
                txn.finish();
            }
        }
        
        if (traversalDepth > 0)
        {
            decAction.setEnabled(true);
        }
    }

    /**
     * Decrements the traversal depth.
     */
    public void decTraversalDepth()
    {
        if (traversalDepth > 0)
        {
            NeoServiceManager sm = Activator.getDefault().getNeoServiceManager(); 
            NeoService ns = sm.getNeoService();
            if (ns != null)
            {
                Transaction txn = Transaction.begin();

                try
                {
                    traversalDepth--;
                    refreshStatusBar();
                    
                    viewer.refresh();
                    viewer.applyLayout();
                }
                finally
                {
                    txn.finish();
                }
            }
            
            if (traversalDepth == 0)
            {
                decAction.setEnabled(false);
            }
        }
    }

    /**
     * Refreshes the view.
     */
    public void refresh()
    {
        NeoServiceManager sm = Activator.getDefault().getNeoServiceManager(); 
        NeoService ns = sm.getNeoService();
        if (ns != null)
        {
            Transaction txn = Transaction.begin();

            try
            {
                viewer.refresh();
                viewer.applyLayout();
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
                // when called during shutdown the content provider may already have been disposed
                if (getViewer().getContentProvider() != null)
                {
                    getViewer().setInput(null);
                }
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
        private static final String NODE_CATEGORY = "Node";
        private static final String PROPERTIES_CATEGORY = "Properties";
        
        private static final String NODE_ID = "Id";
        
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
                List<IPropertyDescriptor> descs = new ArrayList<IPropertyDescriptor>();

                // standard properties for nodes
                descs.add(new NeoPropertyDescriptor(NODE_ID, NODE_ID, NODE_CATEGORY));

                // custom properties for nodes
                Iterable<String> keys = node.getPropertyKeys();
                for (String key : keys)
                {
                    descs.add(new NeoPropertyDescriptor(key, key, PROPERTIES_CATEGORY));
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
                if (id == NODE_ID)
                {
                    return String.valueOf(node.getId());                    
                }
                else
                {
                    return node.getProperty((String) id);
                }
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
                if (id == NODE_ID)
                {
                    return true;                    
                }
                else
                {
                    return node.hasProperty((String) id);
                }
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
        private static final String RELATIONSHIP_CATEGORY   = "Relationship";
        private static final String PROPERTIES_CATEGORY     = "Properties";
        
        private static final String RELATIONSHIP_ID         = "Id";
        private static final String RELATIONSHIP_TYPE       = "Type";
        
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
                List<IPropertyDescriptor> descs = new ArrayList<IPropertyDescriptor>();

                // standard properties for relationships
                descs.add(new NeoPropertyDescriptor(RELATIONSHIP_ID, RELATIONSHIP_ID, RELATIONSHIP_CATEGORY));
                descs.add(new NeoPropertyDescriptor(RELATIONSHIP_TYPE, RELATIONSHIP_TYPE, RELATIONSHIP_CATEGORY));
                
                // custom properties for relationships
                Iterable<String> keys = rs.getPropertyKeys();
                for (String key : keys)
                {
                    descs.add(new NeoPropertyDescriptor(key, key, PROPERTIES_CATEGORY));
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
                if (id == RELATIONSHIP_ID)
                {
                    return String.valueOf(rs.getId());
                }
                else if (id == RELATIONSHIP_TYPE)
                {
                    return String.valueOf(rs.getType().name());
                }
                else
                {
                    return rs.getProperty((String) id);
                }
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
                if (id == RELATIONSHIP_ID)
                {
                    return true;
                }
                else if (id == RELATIONSHIP_TYPE)
                {
                    return true;
                }
                else
                {
                    return rs.hasProperty((String) id);
                }
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
         * The key for identifying the value of the property.
         */
        private Object key;
        
        /**
         * The name of the property.
         */
        private String name;
        
        /**
         * The category of the property.
         */
        private String category;

        /**
         * The constructor.
         */
        public NeoPropertyDescriptor(Object key, String name, String category)
        {
            this.key = key;
            this.name = name;
            this.category = category;
        }

        public CellEditor createPropertyEditor(Composite parent)
        {
            return null;
        }

        public String getCategory()
        {
            return category;
        }

        public String getDescription()
        {
            return "The property with the name '" + key + "'.";
        }

        public String getDisplayName()
        {
            return name;
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
    class NeoGraphContentProvider implements IGraphEntityRelationshipContentProvider
    {
        /**
         * Returns the relationships between the given nodes.
         */
        public Object[] getRelationships(Object source, Object dest)
        {
            Node start = (Node) source;
            Node end = (Node) dest;
            
            List<Relationship> rels = new ArrayList<Relationship>();
            
            Iterable<Relationship> rs = start.getRelationships(Direction.OUTGOING);
            for (Relationship r : rs)
            {
                if (r.getEndNode().getId() == end.getId())
                {
                    rels.add(r);
                }
            }            
            
            return rels.toArray();
        }

        /**
         * Returns all nodes the given node is connected with.
         */
        public Object[] getElements(Object inputElement)
        {
            Node node = (Node) inputElement;
            
            Map<Long, Node> nodes = new HashMap<Long, Node>();            
            getElements(node, nodes, traversalDepth);

            return nodes.values().toArray();
        }
        
        /**
         * Determines the connected nodes within the given traversal depth.
         */
        private void getElements(Node node, Map<Long, Node> nodes, int depth)
        {
            // add the start node too
            nodes.put(node.getId(), node);
            
            if (depth > 0)
            {
                Iterable<Relationship> rs = node.getRelationships(Direction.INCOMING);
                for (Relationship r : rs)
                {
                    Node start = r.getStartNode();
                    if (!nodes.containsKey(start.getId()))
                    {
                        nodes.put(start.getId(), start);
                
                        getElements(start, nodes, depth - 1);
                    }
                }

                rs = node.getRelationships(Direction.OUTGOING);
                for (Relationship r : rs)
                {
                    Node end = r.getEndNode();
                    if (!nodes.containsKey(end.getId()))
                    {
                        nodes.put(end.getId(), end);
                    
                        getElements(end, nodes, depth - 1);
                    }
                }
            }
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
        private Image nodeImage = NeoIcons.getImage(NeoIcons.NEO);

        /**
         * The icon for the root node.
         */
        private Image rootImage = NeoIcons.getImage(NeoIcons.NEO_ROOT);

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
