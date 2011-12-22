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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.TableFolderNode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.TableNode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.sessiontree.model.utility.Dictionary;
import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

class ExtendedCompletionProposal implements ICompletionProposal {

    CompletionProposal compProposal;

    TableNode tn;


   public ExtendedCompletionProposal(String proposalsString, int replacementOffset, int replacementLength, int cursorPosition, Image image, String displayString, TableNode tb) {
        compProposal = new CompletionProposal(proposalsString, replacementOffset, replacementLength, cursorPosition, image, displayString, null, null);
        this.tn = tb;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.text.contentassist.ICompletionProposal#apply(org.eclipse.jface.text.IDocument)
     */
    public void apply(IDocument document) {
        compProposal.apply(document);

    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getAdditionalProposalInfo()
     */
    public String getAdditionalProposalInfo() {
        return tn.getTableDesc();

    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getContextInformation()
     */
    public IContextInformation getContextInformation() {
        return compProposal.getContextInformation();
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getDisplayString()
     */
    public String getDisplayString() {
        return compProposal.getDisplayString();
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getImage()
     */
    public Image getImage() {
        return compProposal.getImage();
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getSelection(org.eclipse.jface.text.IDocument)
     */
    public Point getSelection(IDocument document) {
        return compProposal.getSelection(document);
    }
}

class ICompletionProposalComparator implements Comparator<ICompletionProposal> {

    public int compare(ICompletionProposal i1, ICompletionProposal i2) {
        String s1 = i1.getDisplayString();
        String s2 = i2.getDisplayString();
        return Collator.getInstance().compare(s1, s2);

    }
}

public class SQLCompletionProcessor implements IContentAssistProcessor {

    static String sep = System.getProperty("line.separator");

    private Image catalogImage;

    private Image colImage;

    private Dictionary dictionary;

    private char[] fProposalAutoActivationSet;

    private Image keywordImage;

    private Image tableImage;;

    private Image viewImage;


    public SQLCompletionProcessor(Dictionary dictionary) {
        this.dictionary = dictionary;

        try {
            colImage = ImageUtil.getImage("Images.ColumnIcon");
            tableImage = ImageUtil.getImage("Images.TableIcon");
            viewImage = ImageUtil.getImage("Images.TableIcon");
            keywordImage = ImageUtil.getImage("Images.TableIcon");
            catalogImage = ImageUtil.getImage("Images.DatabaseNodeIcon");
        } catch (Throwable e) {
            SQLExplorerPlugin.error("Error creating images", e); //$NON-NLS-1$
        }
    }

    private String getLastNamePart(String pName)
    {
        int dotPos = pName.lastIndexOf("."); //$NON-NLS-1$
        if (dotPos < 0)
        {
        	return pName;
        }
        return pName.substring(dotPos + 1);
    	
    }
    
    private String getObjectNameWithoutSchema(String objectName, String schemaName)
    {
    	
    	if (objectName.toLowerCase().startsWith(schemaName.toLowerCase() + '.')) //$NON-NLS-1$
    		return objectName.substring(schemaName.length() + 1);
    	else
    		return objectName;    	
    }

    /**
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeCompletionProposals(org.eclipse.jface.text.ITextViewer,
     *      int)
     */
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
        if (dictionary == null)
            return null;
        String text = viewer.getDocument().get();
        String string = text.substring(0, documentOffset);

        if (string.equals("")) //$NON-NLS-1$
            return null;
        int position = string.length() - 1;
        char character;

        while (position > 0) {
            character = string.charAt(position);
            if (!Character.isJavaIdentifierPart(character) && (character != '.'))
                break;
            --position;
        }
        if (position == 0)
            position = -1;
        string = string.substring(position + 1);
        // JFaceDbcPlugin.error("String: "+string,new Exception());
        if (string == null || string.equals(""))
            return null;

        string = string.toLowerCase();

        int length = string.length();
        if (length < 1)
            return null;
        int dotIndex = string.lastIndexOf("."); //$NON-NLS-1$
        if (string.charAt(length - 1) == ' ') {
            return null;
        } else if (string.charAt(length - 1) == '.') {// Last typed character
                                                        // is '.'
            String name = string.substring(0, length - 1);
            if (name == null)
                return null;
            name = getLastNamePart(name);
            if (name.equals(""))
                return null;
            Set<String> st = dictionary.getColumnListByTableName(name);
            if(st == null) {
            	// No table was found, perhaps this is an alias?
            	String words[] = text.split("\\s");
            	// start at word 1 since there shouldn't be any instances of this otherwise.
            	for(int i = 1; i < words.length; i++) {
            		if(words[i].equals(name)) {
            			// name has been found as a word, look up previous word
            			String potentialName = words[i-1].toLowerCase();
            			if (potentialName.equals("as") && i > 1) {
            				potentialName = words[i-2].toLowerCase();
            			}
            			potentialName = getLastNamePart(potentialName);
            			// if it is not a keyword then check against columnlist by table name
            			st = dictionary.getColumnListByTableName(potentialName);
            			if(st != null) {
            				name = potentialName;
            				break;
            			}
            		}
            	}
            }
            if (st != null) {
                List<INode> list = dictionary.getByTableName(name);
                if (list == null)
                    return null;
                TableNode nd = null;
                if (list.size() == 1)
                    nd = (TableNode) list.get(0);
                else
                    return null;
                Object[] obj = st.toArray();
                String[] arr = new String[obj.length];
                System.arraycopy(obj, 0, arr, 0, obj.length);

                ICompletionProposal[] result = new ICompletionProposal[arr.length];
                String tableDesc = null;
                if (nd != null)
                    tableDesc = nd.getTableDesc();
                for (int i = 0; i < arr.length; i++) {
                    result[i] = new CompletionProposal(arr[i], documentOffset, 0, arr[i].length(), colImage, arr[i], null, tableDesc);
                }
                return result;
            }
            INode node = (INode) dictionary.getByCatalogSchemaName(name);
            if (node != null) {
                Object children[] = (Object[]) node.getChildNodes();
                List<ICompletionProposal> propList = new ArrayList<ICompletionProposal>();
                if (children != null) {
                    for (int i = 0; i < children.length; i++) {
                        String childName = children[i].toString().toLowerCase();
                        if (childName.startsWith("table") || childName.startsWith("view") || 
                        	childName.equals("system table") || childName.equals("system view")) {
                            Object[] tables = (Object[]) ((INode) children[i]).getChildNodes();
                            if (tables != null) {
                                for (int j = 0; j < tables.length; j++) {
                                    Image tmpImage = null;
                                    String tableName = tables[j].toString();
                                    if (tables[j] instanceof TableNode) {
                                        if (((TableNode) tables[j]).isTable())
                                            tmpImage = tableImage;
                                        else if (((TableNode) tables[j]).isView())
                                            tmpImage = viewImage;

                                        // ashinger 
                                        // Insert Table/Viewname without schema, if the schema and a dot are the content before the caret
                                        // and the schema is part of the tablename
                                        String shortName = getObjectNameWithoutSchema(tableName, name);
                                        
                                        propList.add(new ExtendedCompletionProposal(shortName, documentOffset, 0, shortName.length(), tmpImage,
                                        		tableName, (TableNode) tables[j]));
                                    }
        
                                }
                            }
                        }
                    }
                }
                ICompletionProposal[] res = new ICompletionProposal[propList.size()];
                System.arraycopy(propList.toArray(), 0, res, 0, propList.size());
                Arrays.sort(res, new ICompletionProposalComparator());
                return res;
            }
        } else if (dotIndex == -1)// The string does not contain "."
        {
            String[] keywordProposal = Dictionary.matchKeywordsPrefix(string);
            ICompletionProposal[] resKey = new ICompletionProposal[keywordProposal.length];
            for (int i = 0; i < keywordProposal.length; i++) {
                resKey[i] = new CompletionProposal(keywordProposal[i], documentOffset - length, length, keywordProposal[i].length(),
                        keywordImage, keywordProposal[i], null, null);
            }

            String[] proposalsString = dictionary.matchTablePrefix(string.toLowerCase());

            List<ICompletionProposal> propList = new ArrayList<ICompletionProposal>();
            for (int i = 0; i < proposalsString.length; i++) {
                List<INode> ls = dictionary.getTableObjectList(proposalsString[i]);
                for (int j = 0; j < ls.size(); j++) {

                    TableNode tbNode = (TableNode) ls.get(j);
                    Image tmpImage = null;
                    if (tbNode.isView())
                        tmpImage = viewImage;
                    else if (tbNode.isTable())
                        tmpImage = tableImage;

                    ICompletionProposal cmp = new ExtendedCompletionProposal(proposalsString[i], documentOffset - length, length,
                            proposalsString[i].length(), tmpImage, proposalsString[i], tbNode);
                    propList.add(cmp);
					// ashinger to prevent multiple entries
                    break;
                }
            }
            String[] proposalsString2 = dictionary.matchCatalogSchemaPrefix(string.toLowerCase());
            ICompletionProposal[] resKey2 = new ICompletionProposal[proposalsString2.length];
            for (int i = 0; i < proposalsString2.length; i++) {
                resKey2[i] = new CompletionProposal(proposalsString2[i], documentOffset - length, length, proposalsString2[i].length(),
                        catalogImage, proposalsString2[i], null, null);
            }

            ICompletionProposal[] res = new ICompletionProposal[propList.size() + keywordProposal.length + resKey2.length];
            System.arraycopy(resKey, 0, res, 0, resKey.length);
            System.arraycopy(propList.toArray(), 0, res, resKey.length, propList.size());
            System.arraycopy(resKey2, 0, res, resKey.length + propList.size(), resKey2.length);
            Arrays.sort(res, new ICompletionProposalComparator());
            return res;
        } else if (dotIndex != -1) {
            String firstPart = string.substring(0, dotIndex);
            int otherDot = firstPart.indexOf(".");
            if (otherDot != -1)
                firstPart = firstPart.substring(otherDot + 1);
            String lastPart = string.substring(dotIndex + 1);
            if (lastPart == null || firstPart == null || lastPart.equals("") || firstPart.equals(""))
                return null;
            Set<String> st = dictionary.getColumnListByTableName(firstPart);
            if (st != null) {
                Iterator<String> iter = st.iterator();
                List<ICompletionProposal> propList = new ArrayList<ICompletionProposal>();
                while (iter.hasNext()) {
                    String colName = (String) iter.next();
                    int length2 = lastPart.length();
                    if (colName.length() >= length2) {
                        if ((colName.substring(0, lastPart.length())).equalsIgnoreCase(lastPart)) {
                            CompletionProposal cmp = new CompletionProposal(colName, documentOffset - length2, length2, colName.length(),
                                    colImage, colName, null, null);
                            propList.add(cmp);
                        }
                    }
                }
                ICompletionProposal[] res = new ICompletionProposal[propList.size()];
                System.arraycopy(propList.toArray(), 0, res, 0, propList.size());
                return res;
            }
            INode node = (INode) dictionary.getByCatalogSchemaName(firstPart);
            if (node != null) {
                String[] proposalsString = dictionary.matchTablePrefix(lastPart.toLowerCase());
                List<ICompletionProposal> propList = new ArrayList<ICompletionProposal>();
                for (int i = 0; i < proposalsString.length; i++) {
                    List<INode> ls = dictionary.getTableObjectList(proposalsString[i]);
                    for (int j = 0; j < ls.size(); j++) {
                        TableNode tbNode = (TableNode) ls.get(j);
                        Image tmpImage = null;
                        TableFolderNode totn = (TableFolderNode) tbNode.getParent();

                        INode catSchema = (INode) totn.getParent();
                        if (catSchema == node) {
                            if (tbNode.isView())
                                tmpImage = viewImage;
                            else if (tbNode.isTable())
                                tmpImage = tableImage;
                            ICompletionProposal cmp = new ExtendedCompletionProposal(proposalsString[i],
                                    documentOffset - lastPart.length(), lastPart.length(), proposalsString[i].length(), tmpImage,
                                    proposalsString[i], tbNode);
                            propList.add(cmp);
                        }
                    }
                }
                ICompletionProposal[] res = new ICompletionProposal[propList.size()];
                System.arraycopy(propList.toArray(), 0, res, 0, propList.size());
                return res;
            }
        }
        return null;

    }


    /**
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeContextInformation(org.eclipse.jface.text.ITextViewer,
     *      int)
     */
    public IContextInformation[] computeContextInformation(ITextViewer arg0, int arg1) {
        return null;
    }


    public void dispose() {
        
        ImageUtil.disposeImage("Images.ColumnIcon");
        ImageUtil.disposeImage("Images.TableIcon");
        ImageUtil.disposeImage("Images.TableIcon");
        ImageUtil.disposeImage("Images.TableIcon");
        ImageUtil.disposeImage("Images.DatabaseNodeIcon");
        
    }


    /**
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
     */
    public char[] getCompletionProposalAutoActivationCharacters() {
        return fProposalAutoActivationSet;
    }


    /**
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationAutoActivationCharacters()
     */
    public char[] getContextInformationAutoActivationCharacters() {
        return null;
    }


    /**
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationValidator()
     */
    public IContextInformationValidator getContextInformationValidator() {
        return null;
    }


    /**
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getErrorMessage()
     */
    public String getErrorMessage() {
        return null;
    }


    public void setCompletionProposalAutoActivationCharacters(char[] activationSet) {
        fProposalAutoActivationSet = activationSet;
    }

}
