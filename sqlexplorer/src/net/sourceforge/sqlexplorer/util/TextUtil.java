package net.sourceforge.sqlexplorer.util;


/**
 * Text handling utility.
 * 
 * @author Davy Vanherbergen
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 */
public class TextUtil {

    public static final int DEFAULT_WRAPLENGTH = 150;
    
    private static final String NEWLINE_SEPARATOR = "\n";
    
    private static final String NEWLINE_EXPR = "\\n";
    
    private static final String RETURN_EXPR = "\\r";
    
    private static final String TAB_EXPR = "\\t";
    
    
    /**
     * Clear all linebreaks and carriage returns from input text.
     * @return cleaned string
     */
    public static String removeLineBreaks(String input) {
        if (input == null) {
            return null;
        }
        String tmp = input.replaceAll(NEWLINE_EXPR, " ");
        tmp = tmp.replaceAll(TAB_EXPR, " ");
        return tmp.replaceAll(RETURN_EXPR, "");
    }
    
    
    
    /**
     * Return the text reformatted to have a max charwidth of maxWidth.
     * @param maxWidth number of chars that the text can be wide.
     */
    public static String getWrappedText(String input) {
        return getWrappedText(input, DEFAULT_WRAPLENGTH);
    }
    
    /**
     * Return the text reformatted to have a max charwidth of maxWidth.
     * @param maxWidth number of chars that the text can be wide.
     */
    public static String getWrappedText(String input, int maxWidth) {
              
        if (input == null) {
            return "";
        }
        
        String[] text = input.split(NEWLINE_EXPR);
        String wrappedText = "";
        
        for (int i = 0; i < text.length; i++) {
            
            text[i] = text[i].replaceAll(RETURN_EXPR, "");
            
            if (text[i].length() == 0) {
                continue;
            }
            
            if (text[i].length() <= maxWidth) {
                wrappedText += text[i];
                
                if (i < text.length - 1) {
                    wrappedText += NEWLINE_SEPARATOR;
                }
            } else {                
                
                String tmp = text[i];
                
                while (tmp.length() > maxWidth) {
                    
                    for (int j = tmp.length() - 1; j >= 0; j--) {
                        
                        if (j < maxWidth) {
                            
                            char c = text[i].charAt(j); 
                            if (c == ',') {
                                wrappedText += tmp.substring(0, j + 1);
                                wrappedText += NEWLINE_SEPARATOR;
                                tmp = tmp.substring(j + 1);
                                break;
                            }
                            if (c == ' ') {
                                wrappedText += tmp.substring(0, j + 1);
                                wrappedText += NEWLINE_SEPARATOR;
                                tmp = tmp.substring(j + 1);
                                break;
                            }
                        }
                        
                        if (j == 0) {
                            wrappedText += tmp.substring(0, maxWidth + 1);
                            tmp = "";
                            break;
                        }
                    }
                    
                }
                
                wrappedText += tmp;
                wrappedText += NEWLINE_SEPARATOR;
            }            
            
        }        
        
        return wrappedText;
    }
    
    /**
     * Trims whitespace from a string and compresses all internal whitespace
     * down to a single space.
     * @param source the string to compress
     * @return the compressed string
     */
    public static String compressWhitespace(CharSequence source) {
    	return compressWhitespace(source, 0);
    }
    
    /**
     * Trims whitespace from a string and compresses all internal whitespace
     * down to a single space.  Keeps the length of the string to at most
     * maxLength, but if it truncates then it makes the last 3 characters
     * an elipsis
     * @param source the string to compress
     * @param maxLength maximum length of the result
     * @return the compressed string
     */
    public static String compressWhitespace(CharSequence source, int maxLength) {
		StringBuffer sb = new StringBuffer(source);
		
		// Trim leading whitespace
		while (sb.length() > 0 && Character.isWhitespace(sb.charAt(0)))
			sb.deleteCharAt(0);
		
		boolean lastWasWhite = false;
		for (int i = 0; i < sb.length(); i++) {
			if (Character.isWhitespace(sb.charAt(i))) {
				if (lastWasWhite) {
					// Delete continguous whitespace
					sb.deleteCharAt(i);
					i--;
				} else {
					lastWasWhite = true;
					
					// Force all whitespace to be a space - IE no funny characters for CR etc
					sb.setCharAt(i, ' ');
				}
			} else
				lastWasWhite = false;
		}
		
		// Optionally trim to size
		if (maxLength > 0 && sb.length() > maxLength) {
			if (maxLength > 3) {
				sb.delete(maxLength - 3, sb.length());
				sb.append("...");
			} else
				sb.delete(maxLength, sb.length());
		}
		
		return sb.toString().trim();
    }
    

    /**
     * Replace all occurrences of replaceFrom in inputString with replaceTo.
     * 
     * @param inputString string to update
     * @param replaceFrom occurrences to replace
     * @param replaceTo string that replaces occurrences
     * @return
     */
    public static String replaceChar(String inputString, char replaceFrom, String replaceTo) {

        if (inputString == null || inputString.length() == 0) {
            return inputString;
        }
        
        StringBuffer buffer = new StringBuffer();                
        char[] input = inputString.toCharArray();
            
        for (int i = 0; i < input.length; i++) {
            
            if (input[i] == replaceFrom) {
                buffer.append(replaceTo);
            } else {
                buffer.append(input[i]);
            }                
        }            

        return buffer.toString();
    }

    /**
     * Remove trailing spaces from input.
     * @param input Input string.
     * @return String without trailing spaces.
     */
    public static String rtrim(String input) {
    	if (input == null)
    		return null;
    	int i = 0;
    	for (i = input.length() - 1; i >= 0 && Character.isWhitespace(input.charAt(i)); i--)
    		;
    	return input.substring(0, i + 1);
    }

    /**
     * Escape input string suitable for HTML output.
     * @param input Input string.
     * @return String with most dangerous characters escaped.
     */
    public static String htmlEscape(String input) {
    	String ret = input.replaceAll("&", "&amp;");
    	ret = ret.replaceAll("<", "&lt;");
    	ret = ret.replaceAll(">", "&gt;");
    	return ret;
    }
    
    /**
     * quote the given String using double quotes, replace all double quotes
     * within the given String by doubleing them.
     * 
     * @param input
     * @return
     */
    public static String quote(String input)
    {
		return '"' +input.replaceAll("\"", "\"\"") + '"';
    }
}
