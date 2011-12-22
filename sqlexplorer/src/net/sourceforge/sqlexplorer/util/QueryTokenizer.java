package net.sourceforge.sqlexplorer.util;

import java.util.StringTokenizer;

/**
 * QueryTokenizer based on original SquirreL SQL tokenizer, but adds the
 * possibility to use multiple characters as the sql delimeter.
 * 
 * This is used for splitting the input text in the editor into
 * multiple executable sql statements.
 * 
 * @author Davy Vanherbergen
 */
public class QueryTokenizer {

    private static String _alternateQuerySeparator;

    private static String _querySeparator;

    private String _sQuerys;

    private String _sNextQuery;

    /**
     * These characters at the beginning of an SQL statement indicate that it is
     * a comment.
     */
    private String _solComment;

    /**
     * QueryTokenizer constructor comment.
     */
    public QueryTokenizer(String sql, String querySeparator, String alternateSeparator, String solComment) {

        if (querySeparator != null && querySeparator.trim().length() > 0) {
            _querySeparator = querySeparator.substring(0, 1);
        } else {
            // failsave..
            _querySeparator = ";";
        }
        
        if (alternateSeparator != null && alternateSeparator.trim().length() > 0) {
            _alternateQuerySeparator = alternateSeparator;    
        } else {
            _alternateQuerySeparator = null;
        }        

        if (solComment != null && solComment.trim().length() > 0) {
            _solComment = solComment;
        } else {
            _solComment = null;
        }

        if (sql != null) {
            _sQuerys = prepareSQL(sql);
            _sNextQuery = parse();
        } else {
            _sQuerys = "";
        }
    }

    public boolean hasQuery() {
        return _sNextQuery != null;
    }

    public String nextQuery() {
        String sReturnQuery = _sNextQuery;
        _sNextQuery = parse();
        return sReturnQuery;
    }

    private int findFirstSeparator() {

        String separator = _querySeparator;
        int separatorLength = _querySeparator.length();
        int iQuoteCount = 1;
        int iIndex1 = 0 - separatorLength;
        
        while (iQuoteCount % 2 != 0) {
            
            iQuoteCount = 0;
            iIndex1 = _sQuerys.indexOf(separator, iIndex1 + separatorLength);

            if (iIndex1 > -1) {
                int iIndex2 = _sQuerys.lastIndexOf('\'', iIndex1 + separatorLength - 1);
                while (iIndex2 != -1) {
                    if (_sQuerys.charAt(iIndex2 - 1) != '\\') {
                        iQuoteCount++;
                    }
                    iIndex2 = _sQuerys.lastIndexOf('\'', iIndex2 - 1);
                }
            } else {
                return -1;
            }
        }
        
        return iIndex1;
    }

    private int findFirstAlternateSeparator() {

        if (_alternateQuerySeparator == null) {
            return -1;
        }

        String separator = _alternateQuerySeparator;
        int separatorLength = _alternateQuerySeparator.length();
        int iQuoteCount = 1;
        int iIndex1 = 0 - separatorLength;
        
        while (iQuoteCount % 2 != 0) {
            
            iQuoteCount = 0;
            iIndex1 = _sQuerys.indexOf(separator, iIndex1 + separatorLength);

            if (iIndex1 > -1) {
                int iIndex2 = _sQuerys.lastIndexOf('\'', iIndex1 + separatorLength - 1);
                while (iIndex2 != -1) {
                    if (_sQuerys.charAt(iIndex2 - 1) != '\\') {
                        iQuoteCount++;
                    }
                    iIndex2 = _sQuerys.lastIndexOf('\'', iIndex2 - 1);
                }
            } else {
                return -1;
            }
        }
        
        return iIndex1;
    }

    public String parse() {
        
        if (_sQuerys.length() == 0) {
            return null;
        }
        
        String separator = _querySeparator;
                
        int indexSep = findFirstSeparator();
        int indexAltSep = findFirstAlternateSeparator();
        
        if (indexAltSep > -1) {
            if (indexSep < 0 || indexAltSep < indexSep) {
                // use alternate separator
                separator = _alternateQuerySeparator;
            }
        }
        
        int separatorLength = separator.length();
        int iQuoteCount = 1;
        int iIndex1 = 0 - separatorLength;
        
        while (iQuoteCount % 2 != 0) {
            
            iQuoteCount = 0;
            iIndex1 = _sQuerys.indexOf(separator, iIndex1 + separatorLength);

            if (iIndex1 > -1) {
                int iIndex2 = _sQuerys.lastIndexOf('\'', iIndex1 + separatorLength - 1);
                while (iIndex2 != -1) {
                    if (_sQuerys.charAt(iIndex2 - 1) != '\\') {
                        iQuoteCount++;
                    }
                    iIndex2 = _sQuerys.lastIndexOf('\'', iIndex2 - 1);
                }
            } else {
                String sNextQuery = _sQuerys;
                _sQuerys = "";
                if (_solComment != null && sNextQuery.startsWith(_solComment)) {
                    return parse();
                }
                return replaceLineFeeds(sNextQuery);
            }
        }
        String sNextQuery = _sQuerys.substring(0, iIndex1);
        _sQuerys = _sQuerys.substring(iIndex1 + separatorLength).trim();
        if (_solComment != null && sNextQuery.startsWith(_solComment)) {
            return parse();
        }
        return replaceLineFeeds(sNextQuery);
    }

    private String prepareSQL(String sql) {
        StringBuffer results = new StringBuffer(1024);

        for (StringTokenizer tok = new StringTokenizer(sql.trim(), "\n", false); tok.hasMoreTokens();) {
            String line = tok.nextToken();
            if (!line.startsWith(_solComment)) {
                results.append(line).append('\n');
            }
        }

        return results.toString();
    }

    private String replaceLineFeeds(String sql) {
        StringBuffer sbReturn = new StringBuffer();
        int iPrev = 0;
        int linefeed = sql.indexOf('\n');
        int iQuote = -1;
        while (linefeed != -1) {
            iQuote = sql.indexOf('\'', iQuote + 1);
            if (iQuote != -1 && iQuote < linefeed) {
                int iNextQute = sql.indexOf('\'', iQuote + 1);
                if (iNextQute > linefeed) {
                    sbReturn.append(sql.substring(iPrev, linefeed));
                    sbReturn.append('\n');
                    iPrev = linefeed + 1;
                    linefeed = sql.indexOf('\n', iPrev);
                }
            } else {
                linefeed = sql.indexOf('\n', linefeed + 1);
            }
        }
        sbReturn.append(sql.substring(iPrev));
        return sbReturn.toString();
    }

}
