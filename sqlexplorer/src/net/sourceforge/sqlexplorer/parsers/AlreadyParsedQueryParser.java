/**
 * 
 */
package net.sourceforge.sqlexplorer.parsers;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 * Implementation to re run an already parsed query
 * @author Heiko
 *
 */
public class AlreadyParsedQueryParser implements QueryParser {

	private Set<Query> queries;
	private ExecutionContext context;

	public AlreadyParsedQueryParser(Query pQuery, ExecutionContext pContext)
	{
		this.queries = Collections.singleton(pQuery);
		this.context = pContext;
	}
	
	/* (non-Javadoc)
	 * @see net.sourceforge.sqlexplorer.parsers.QueryParser#addLineNoOffset(int, int)
	 */
	public void addLineNoOffset(int originalLineNo, int numLines) {
		throw new RuntimeException("Not implemented");

	}

	/* (non-Javadoc)
	 * @see net.sourceforge.sqlexplorer.parsers.QueryParser#addParameter(net.sourceforge.sqlexplorer.parsers.NamedParameter)
	 */
	public void addParameter(NamedParameter parameter) {
		throw new RuntimeException("Not implemented");

	}

	/* (non-Javadoc)
	 * @see net.sourceforge.sqlexplorer.parsers.QueryParser#adjustLineNo(int)
	 */
	public int adjustLineNo(int lineNo) {
		return lineNo;
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.sqlexplorer.parsers.QueryParser#getContext()
	 */
	public ExecutionContext getContext() {
		return this.context;
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.sqlexplorer.parsers.QueryParser#numberOfQueries()
	 */
	public int numberOfQueries() {
		return this.queries.size();
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.sqlexplorer.parsers.QueryParser#parse()
	 */
	public void parse() throws ParserException {
		throw new RuntimeException("Not implemented");

	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<Query> iterator() {
		return this.queries.iterator();
	}

}
