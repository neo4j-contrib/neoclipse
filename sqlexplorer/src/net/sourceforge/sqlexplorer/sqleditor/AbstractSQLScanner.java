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


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;

public abstract class AbstractSQLScanner extends BufferedRuleBasedScanner {
	
	private IColorManager fColorManager;
	private IPreferenceStore fPreferenceStore;
	
	private Map<String,IToken> fTokenMap= new HashMap<String, IToken>();
	private String[] fPropertyNamesColor;
	private String[] fPropertyNamesStyle;
	
	
	/** 
	 * Returns the list of preference keys which define the tokens
	 * used in the rules of this scanner.
	 */
	abstract protected String[] getTokenProperties();
		
	/**
	 * Creates the list of rules controlling this scanner.
	 */
	abstract protected List<IRule> createRules();
		
	
	/**
	 * Creates an abstract Java scanner.
	 */
	public AbstractSQLScanner(IColorManager manager, IPreferenceStore store) {
		super();
		fColorManager= manager;
		fPreferenceStore= store;
	}
	
	/**
	 * Must be called after the constructor has been called.
	 */
	public final void initialize() {
		
		fPropertyNamesColor= getTokenProperties();
		int length= fPropertyNamesColor.length;
		fPropertyNamesStyle= new String[length];
		for (int i= 0; i < length; i++) {
			fPropertyNamesStyle[i]= fPropertyNamesColor[i] + "_bold"; //$NON-NLS-1$
			addToken(fPropertyNamesColor[i], fPropertyNamesStyle[i]);
		}
		
		initializeRules();
	}
		
	private void addToken(String colorKey, String styleKey) {
		//System.out.println("AddToken "+colorKey+" "+styleKey);
		RGB rgb= PreferenceConverter.getColor(fPreferenceStore, colorKey);
		if (fColorManager instanceof IColorManagerExtension) {
			IColorManagerExtension ext= (IColorManagerExtension) fColorManager;
			ext.unbindColor(colorKey);
			ext.bindColor(colorKey, rgb);
		}
		
		boolean bold= fPreferenceStore.getBoolean(styleKey);
		fTokenMap.put(colorKey, new Token(new TextAttribute(fColorManager.getColor(colorKey), null, bold ? SWT.BOLD : SWT.NORMAL)));
	}
	
	protected Token getToken(String key) {
		return (Token) fTokenMap.get(key);
	}
		
	private void initializeRules() {
		List<IRule> rules= createRules();
		if (rules != null) {
			IRule[] result= new IRule[rules.size()];
			rules.toArray(result);
			setRules(result);
		}
	}
	
	private int indexOf(String property) {
		if (property != null) {
			int length= fPropertyNamesColor.length;
			for (int i= 0; i < length; i++) {
				if (property.equals(fPropertyNamesColor[i]) || property.equals(fPropertyNamesStyle[i]))
					return i;
			}
		}
		return -1;
	}
	
	public boolean affectsBehavior(PropertyChangeEvent event) {
		return indexOf(event.getProperty()) >= 0;
	}
	
	public void adaptToPreferenceChange(PropertyChangeEvent event) {
		String p= event.getProperty();
		int index= indexOf(p);
		Token token= getToken(fPropertyNamesColor[index]);
		if (fPropertyNamesColor[index].equals(p))
			adaptToColorChange(token, event);
		else
			adaptToStyleChange(token, event);
	}
	
	private void adaptToColorChange(Token token, PropertyChangeEvent event) {
		RGB rgb= null;
		
		Object value= event.getNewValue();
		if (value instanceof RGB)
			rgb= (RGB) value;
		else if (value instanceof String)
			rgb= StringConverter.asRGB((String) value);
			
		if (rgb != null) {
			
			String property= event.getProperty();
			
			if (fColorManager instanceof IColorManagerExtension) {
				IColorManagerExtension ext= (IColorManagerExtension) fColorManager;
				ext.unbindColor(property);
				ext.bindColor(property, rgb);
			}
			
			Object data= token.getData();
			if (data instanceof TextAttribute) {
				TextAttribute oldAttr= (TextAttribute) data;
				token.setData(new TextAttribute(fColorManager.getColor(property), oldAttr.getBackground(), oldAttr.getStyle()));
			}
		}
	}
	
	private void adaptToStyleChange(Token token, PropertyChangeEvent event) {
		boolean bold= false;
		Object value= event.getNewValue();
		if (value instanceof Boolean)
			bold= ((Boolean) value).booleanValue();
		else if (value instanceof String) {
			String s= (String) value;
			if (IPreferenceStore.TRUE.equals(s))
				bold= true;
			else if (IPreferenceStore.FALSE.equals(s))
				bold= false;
		}
		
		Object data= token.getData();
		if (data instanceof TextAttribute) {
			TextAttribute oldAttr= (TextAttribute) data;
			boolean isBold= (oldAttr.getStyle() == SWT.BOLD);
			if (isBold != bold) 
				token.setData(new TextAttribute(oldAttr.getForeground(), oldAttr.getBackground(), bold ? SWT.BOLD : SWT.NORMAL));
		}
	}
}