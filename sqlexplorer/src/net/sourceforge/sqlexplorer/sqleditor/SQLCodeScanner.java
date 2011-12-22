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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.sessiontree.model.utility.Dictionary;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * A SQL code scanner.
 */
public class SQLCodeScanner extends AbstractSQLScanner {

	private static class VersionedWordRule extends WordRule {

		private final String fVersion;
		private final boolean fEnable;
		
		private String fCurrentVersion;

		public VersionedWordRule(IWordDetector detector, String version, boolean enable, String currentVersion) {
			super(detector);

			fVersion= version;
			fEnable= enable;
			fCurrentVersion= currentVersion;
		}
		
		/*
		 * @see IRule#evaluate
		 */
		public IToken evaluate(ICharacterScanner scanner) {
			IToken token= super.evaluate(scanner);

			if (fEnable) {
				if (fCurrentVersion.equals(fVersion))
					return token;
					
				return Token.UNDEFINED;

			} else {
				if (fCurrentVersion.equals(fVersion))
					return Token.UNDEFINED;
					
				return token;
			}
		}
	}
	public static String[]getFgKeywords(){
		return fgKeywords;
	}
	private static String[] fgKeywords= { 

    "alter", //$NON-NLS-1$
    "begin", //$NON-NLS-1$
    "call", //$NON-NLS-1$
    "close", //$NON-NLS-1$
    "comment", //$NON-NLS-1$
    "commit", //$NON-NLS-1$
    "connect", //$NON-NLS-1$
    "create", //$NON-NLS-1$
    "declare", //$NON-NLS-1$
    "delete", //$NON-NLS-1$
    "describe", //$NON-NLS-1$
    "disconnect", //$NON-NLS-1$
    "drop", //$NON-NLS-1$
    "end", //$NON-NLS-1$
    "execute", //$NON-NLS-1$
    "explain", //$NON-NLS-1$
    "fetch", //$NON-NLS-1$
    "flush", //$NON-NLS-1$
    "free", //$NON-NLS-1$
    "grant", //$NON-NLS-1$
    "include", //$NON-NLS-1$
    "insert", //$NON-NLS-1$
    "lock", //$NON-NLS-1$
    "open", //$NON-NLS-1$
    "prepare", //$NON-NLS-1$
    "refresh", //$NON-NLS-1$
    "release", //$NON-NLS-1$
    "rename", //$NON-NLS-1$
    "revoke", //$NON-NLS-1$
    "rollback", //$NON-NLS-1$
    "select", //$NON-NLS-1$
    "set", //$NON-NLS-1$
    "signal", //$NON-NLS-1$
    "update", //$NON-NLS-1$
    "values", //$NON-NLS-1$
    "whenever", //$NON-NLS-1$
    "alias", //$NON-NLS-1$
    "bufferpool", //$NON-NLS-1$
    "connection", //$NON-NLS-1$
    "cursor", //$NON-NLS-1$
    "distinct", //$NON-NLS-1$
    "event", //$NON-NLS-1$
    "function", //$NON-NLS-1$
    "immediate", //$NON-NLS-1$
    "index", //$NON-NLS-1$
    "integrity", //$NON-NLS-1$
    "into", //$NON-NLS-1$
    "locator", //$NON-NLS-1$
    "mapping", //$NON-NLS-1$
    "monitor", //$NON-NLS-1$
    "nickname", //$NON-NLS-1$
    "nodegroup", //$NON-NLS-1$
    "on", //$NON-NLS-1$
    "option", //$NON-NLS-1$
    "packageset", //$NON-NLS-1$
    "passthru", //$NON-NLS-1$
    "path", //$NON-NLS-1$
    "procedure", //$NON-NLS-1$
    "schema", //$NON-NLS-1$
    "section", //$NON-NLS-1$
    "server", //$NON-NLS-1$
    "sqlstate", //$NON-NLS-1$
    "state", //$NON-NLS-1$
    "table", //$NON-NLS-1$
    "tablespace", //$NON-NLS-1$
    "trigger", //$NON-NLS-1$
    "type", //$NON-NLS-1$
    "user", //$NON-NLS-1$
    "view", //$NON-NLS-1$
    "wrapper", //$NON-NLS-1$
    "action", //$NON-NLS-1$
    "activate", //$NON-NLS-1$
    "add", //$NON-NLS-1$
    "after", //$NON-NLS-1$
    "allow", //$NON-NLS-1$
    "alterin", //$NON-NLS-1$
    "append", //$NON-NLS-1$
    "appl_id", //$NON-NLS-1$
    "appl_name", //$NON-NLS-1$
    "as", //$NON-NLS-1$
    "asc", //$NON-NLS-1$
    "asutime", //$NON-NLS-1$
    "atomic", //$NON-NLS-1$
    "attribute", //$NON-NLS-1$
    "authorization", //$NON-NLS-1$
    "auth_id", //$NON-NLS-1$
    "autostart", //$NON-NLS-1$
    "before", //$NON-NLS-1$
    "bind", //$NON-NLS-1$
    "bindadd", //$NON-NLS-1$
    "bit", //$NON-NLS-1$
    "blocked", //$NON-NLS-1$
    "buffer", //$NON-NLS-1$
    "bufferpools", //$NON-NLS-1$
    "buffersize", //$NON-NLS-1$
    "by", //$NON-NLS-1$
    "c", //$NON-NLS-1$
    "called", //$NON-NLS-1$
    "capture", //$NON-NLS-1$
    "cardinality", //$NON-NLS-1$
    "cascade", //$NON-NLS-1$
    "cast", //$NON-NLS-1$
    "changes", //$NON-NLS-1$
    "check", //$NON-NLS-1$
    "checked", //$NON-NLS-1$
    "cluster", //$NON-NLS-1$
    "cobol", //$NON-NLS-1$
    "collid", //$NON-NLS-1$
    "column", //$NON-NLS-1$
    "columns", //$NON-NLS-1$
    "compact", //$NON-NLS-1$
    "comparisons", //$NON-NLS-1$
    "compound", //$NON-NLS-1$
    "confirm", //$NON-NLS-1$
    "connections", //$NON-NLS-1$
    "constraint", //$NON-NLS-1$
    "contains", //$NON-NLS-1$
    "continue", //$NON-NLS-1$
    "control", //$NON-NLS-1$
    "createin", //$NON-NLS-1$
    "createtab", //$NON-NLS-1$
    "create_not_fenced", //$NON-NLS-1$
    "data", //$NON-NLS-1$
    "database", //$NON-NLS-1$
    "db2", //$NON-NLS-1$
    "db2dari", //$NON-NLS-1$
    "db2general", //$NON-NLS-1$
    "db2options", //$NON-NLS-1$
    "db2sql", //$NON-NLS-1$
    "db", //$NON-NLS-1$
    "dbadm", //$NON-NLS-1$
    "dbinfo", //$NON-NLS-1$
    "deadlocks", //$NON-NLS-1$
    "deferred", //$NON-NLS-1$
    "definer", //$NON-NLS-1$
    "definition", //$NON-NLS-1$
    "desc", //$NON-NLS-1$
    "descriptor", //$NON-NLS-1$
    "deterministic", //$NON-NLS-1$
    "device", //$NON-NLS-1$
    "disallow", //$NON-NLS-1$
    "dropin", //$NON-NLS-1$
    "dropped", //$NON-NLS-1$
    "each", //$NON-NLS-1$
    "empty", //$NON-NLS-1$
    "environment", //$NON-NLS-1$
    "evaluate", //$NON-NLS-1$
    "except", //$NON-NLS-1$
    "extend", //$NON-NLS-1$
    "extended", //$NON-NLS-1$
    "extentsize", //$NON-NLS-1$
    "external", //$NON-NLS-1$
    "federated", //$NON-NLS-1$
    "fenced", //$NON-NLS-1$
    "file", //$NON-NLS-1$
    "final", //$NON-NLS-1$
    "first", //$NON-NLS-1$
    "for", //$NON-NLS-1$
    "foreign", //$NON-NLS-1$
    "from", //$NON-NLS-1$
    "fs", //$NON-NLS-1$
    "general", //$NON-NLS-1$
    "generated", //$NON-NLS-1$
    "global", //$NON-NLS-1$
    "go", //$NON-NLS-1$
    "goto", //$NON-NLS-1$
    "group", //$NON-NLS-1$
    "hashing", //$NON-NLS-1$
    "hierarchy", //$NON-NLS-1$
    "hold", //$NON-NLS-1$
    "implicit_schema", //$NON-NLS-1$
    "incremental", //$NON-NLS-1$
    "indexes", //$NON-NLS-1$
    "infix", //$NON-NLS-1$
    "inherit", //$NON-NLS-1$
    "initially", //$NON-NLS-1$
    "inout", //$NON-NLS-1$
    "input", //$NON-NLS-1$
    "intersect", //$NON-NLS-1$
    "iterate", //$NON-NLS-1$
    "java", //$NON-NLS-1$
    "key", //$NON-NLS-1$
    "language", //$NON-NLS-1$
    "leave", //$NON-NLS-1$
    "library", //$NON-NLS-1$
    "limit", //$NON-NLS-1$
    "link", //$NON-NLS-1$
    "linktype", //$NON-NLS-1$
    "local", //$NON-NLS-1$
    "locksize", //$NON-NLS-1$
    "logged", //$NON-NLS-1$
    "longvar", //$NON-NLS-1$
    "main", //$NON-NLS-1$
    "managed", //$NON-NLS-1$
    "manualstart", //$NON-NLS-1$
    "maxfiles", //$NON-NLS-1$
    "maxfilesize", //$NON-NLS-1$
    "minpctused", //$NON-NLS-1$
    "modifies", //$NON-NLS-1$
    "name", //$NON-NLS-1$
    "new", //$NON-NLS-1$
    "new_table", //$NON-NLS-1$
    "no", //$NON-NLS-1$
    "node", //$NON-NLS-1$
    "nodes", //$NON-NLS-1$
    "nonblocked", //$NON-NLS-1$
    "none", //$NON-NLS-1$
    "off", //$NON-NLS-1$
    "oid", //$NON-NLS-1$
    "old", //$NON-NLS-1$
    "old_table", //$NON-NLS-1$
    "ole", //$NON-NLS-1$
    "oledb", //$NON-NLS-1$
    "online", //$NON-NLS-1$
    "options", //$NON-NLS-1$
    "out", //$NON-NLS-1$
    "overhead", //$NON-NLS-1$
    "package", //$NON-NLS-1$
    "pagesize", //$NON-NLS-1$
    "parallel", //$NON-NLS-1$
    "parameter", //$NON-NLS-1$
    "partitioning", //$NON-NLS-1$
    "password", //$NON-NLS-1$
    "pctfree", //$NON-NLS-1$
    "pending", //$NON-NLS-1$
    "permission", //$NON-NLS-1$
    "pipe", //$NON-NLS-1$
    "plan", //$NON-NLS-1$
    "prefetchsize", //$NON-NLS-1$
    "primary", //$NON-NLS-1$
    "privileges", //$NON-NLS-1$
    "program", //$NON-NLS-1$
    "program_type_main", //$NON-NLS-1$
    "public", //$NON-NLS-1$
    "queryno", //$NON-NLS-1$
    "querytag", //$NON-NLS-1$
    "read", //$NON-NLS-1$
    "reads", //$NON-NLS-1$
    "recommend", //$NON-NLS-1$
    "reconcile", //$NON-NLS-1$
    "recovery", //$NON-NLS-1$
    "ref", //$NON-NLS-1$
    "references", //$NON-NLS-1$
    "referencing", //$NON-NLS-1$
    "regular", //$NON-NLS-1$
    "replace", //$NON-NLS-1$
    "replicated", //$NON-NLS-1$
    "reset", //$NON-NLS-1$
    "resident", //$NON-NLS-1$
    "resignal", //$NON-NLS-1$
    "restore", //$NON-NLS-1$
    "restrict", //$NON-NLS-1$
    "result", //$NON-NLS-1$
    "return", //$NON-NLS-1$
    "returns", //$NON-NLS-1$
    "reverse", //$NON-NLS-1$
    "row", //$NON-NLS-1$
    "scans", //$NON-NLS-1$
    "scope", //$NON-NLS-1$
    "scratchpad", //$NON-NLS-1$
    "security", //$NON-NLS-1$
    "selection", //$NON-NLS-1$
    "sets", //$NON-NLS-1$
    "single", //$NON-NLS-1$
    "size", //$NON-NLS-1$
    "source", //$NON-NLS-1$
    "specific", //$NON-NLS-1$
    "specification", //$NON-NLS-1$
    "sql", //$NON-NLS-1$
    "sqlca", //$NON-NLS-1$
    "sqlda", //$NON-NLS-1$
    "sqlerror", //$NON-NLS-1$
    "sqlwarning", //$NON-NLS-1$
    "statement", //$NON-NLS-1$
    "statements", //$NON-NLS-1$
    "static", //$NON-NLS-1$
    "stay", //$NON-NLS-1$
    "stop", //$NON-NLS-1$
    "storage", //$NON-NLS-1$
    "style", //$NON-NLS-1$
    "sub", //$NON-NLS-1$
    "summary", //$NON-NLS-1$
    "switch", //$NON-NLS-1$
    "synonym", //$NON-NLS-1$
    "system", //$NON-NLS-1$
    "tables", //$NON-NLS-1$
    "tablespaces", //$NON-NLS-1$
    "template", //$NON-NLS-1$
    "temporary", //$NON-NLS-1$
    "to", //$NON-NLS-1$
    "transactions", //$NON-NLS-1$
    "transferrate", //$NON-NLS-1$
    "unchecked", //$NON-NLS-1$
    "under", //$NON-NLS-1$
    "union", //$NON-NLS-1$
    "unique", //$NON-NLS-1$
    "unlink", //$NON-NLS-1$
    "url", //$NON-NLS-1$
    "use", //$NON-NLS-1$
    "using", //$NON-NLS-1$
    "variant", //$NON-NLS-1$
    "varying", //$NON-NLS-1$
    "version", //$NON-NLS-1$
    "volatile", //$NON-NLS-1$
    "when", //$NON-NLS-1$
    "where", //$NON-NLS-1$
    "with", //$NON-NLS-1$
    "without", //$NON-NLS-1$
    "wlm", //$NON-NLS-1$
    "work", //$NON-NLS-1$
    "write", //$NON-NLS-1$
    "yes", //$NON-NLS-1$
    "default", //$NON-NLS-1$
    "sqlcode", //$NON-NLS-1$
    "between", //$NON-NLS-1$
    "exists", //$NON-NLS-1$
    "in", //$NON-NLS-1$
    "like", //$NON-NLS-1$
    "and", //$NON-NLS-1$
    "not", //$NON-NLS-1$
    "escape", //$NON-NLS-1$
    "is", //$NON-NLS-1$
    "of", //$NON-NLS-1$
    "only", //$NON-NLS-1$
    "dynamic", //$NON-NLS-1$
    "all", //$NON-NLS-1$
    "any", //$NON-NLS-1$
    "some", //$NON-NLS-1$
    "bigint", //$NON-NLS-1$
    "blob", //$NON-NLS-1$
    "char", //$NON-NLS-1$
    "character", //$NON-NLS-1$
    "clob", //$NON-NLS-1$
    "datalink", //$NON-NLS-1$
    "date", //$NON-NLS-1$
    "dbclob", //$NON-NLS-1$
    "dec", //$NON-NLS-1$
    "decimal", //$NON-NLS-1$
    "double", //$NON-NLS-1$
    "float", //$NON-NLS-1$
    "graphic", //$NON-NLS-1$
    "int", //$NON-NLS-1$
    "integer", //$NON-NLS-1$
    "long", //$NON-NLS-1$
    "null", //$NON-NLS-1$
    "num", //$NON-NLS-1$
    "numeric", //$NON-NLS-1$
    "precision", //$NON-NLS-1$
    "real", //$NON-NLS-1$
    "smallint", //$NON-NLS-1$
    "time", //$NON-NLS-1$
    "timestamp", //$NON-NLS-1$
    "varchar", //$NON-NLS-1$
    "vargraphic", //$NON-NLS-1$
    "current", //$NON-NLS-1$
    "current_date", //$NON-NLS-1$
    "degree", //$NON-NLS-1$
    "mode", //$NON-NLS-1$
    "snapshot", //$NON-NLS-1$
    "query", //$NON-NLS-1$
    "optimization", //$NON-NLS-1$
    "age", //$NON-NLS-1$
    "sqlid", //$NON-NLS-1$
    "current_time", //$NON-NLS-1$
    "current_timestamp", //$NON-NLS-1$
    "timezone", //$NON-NLS-1$
    "syscat", //$NON-NLS-1$
    "sysfun", //$NON-NLS-1$
    "sysibm", //$NON-NLS-1$
    "sysstat", //$NON-NLS-1$
    "acquire", //$NON-NLS-1$
    "allocate", //$NON-NLS-1$
    "audit", //$NON-NLS-1$
    "case", //$NON-NLS-1$
    "ccsid", //$NON-NLS-1$
    "collection", //$NON-NLS-1$
    "cross", //$NON-NLS-1$
    "current_server", //$NON-NLS-1$
    "current_timezone", //$NON-NLS-1$
    "current_user", //$NON-NLS-1$
    "dba", //$NON-NLS-1$
    "dbspace", //$NON-NLS-1$
    "editproc", //$NON-NLS-1$
    "else", //$NON-NLS-1$
    "erase", //$NON-NLS-1$
    "exception", //$NON-NLS-1$
    "exclusive", //$NON-NLS-1$
    "fieldproc", //$NON-NLS-1$
    "full", //$NON-NLS-1$
    "having", //$NON-NLS-1$
    "hours", //$NON-NLS-1$
    "identified", //$NON-NLS-1$
    "indicator", //$NON-NLS-1$
    "inner", //$NON-NLS-1$
    "isolation", //$NON-NLS-1$
    "join", //$NON-NLS-1$
    "label", //$NON-NLS-1$
    "lockmax", //$NON-NLS-1$
    "microseconds", //$NON-NLS-1$
    "minutes", //$NON-NLS-1$
    "months", //$NON-NLS-1$
    "named", //$NON-NLS-1$
    "nheader", //$NON-NLS-1$
    "numparts", //$NON-NLS-1$
    "obid", //$NON-NLS-1$
    "optimize", //$NON-NLS-1$
    "or", //$NON-NLS-1$
    "order", //$NON-NLS-1$
    "outer", //$NON-NLS-1$
    "page", //$NON-NLS-1$
    "pages", //$NON-NLS-1$
    "part", //$NON-NLS-1$
    "pctindex", //$NON-NLS-1$
    "priqty", //$NON-NLS-1$
    "private", //$NON-NLS-1$
    "resource", //$NON-NLS-1$
    "rows", //$NON-NLS-1$
    "rrn", //$NON-NLS-1$
    "run", //$NON-NLS-1$
    "schedule", //$NON-NLS-1$
    "seconds", //$NON-NLS-1$
    "secqty", //$NON-NLS-1$
    "share", //$NON-NLS-1$
    "simple", //$NON-NLS-1$
    "statistics", //$NON-NLS-1$
    "stogroup", //$NON-NLS-1$
    "storpool", //$NON-NLS-1$
    "subpages", //$NON-NLS-1$
    "substring", //$NON-NLS-1$
    "transaction", //$NON-NLS-1$
    "trim", //$NON-NLS-1$
    "validproc", //$NON-NLS-1$
    "variable", //$NON-NLS-1$
    "vcat", //$NON-NLS-1$
    "volumes", //$NON-NLS-1$
    "years", //$NON-NLS-1$
    "absolute", //$NON-NLS-1$
    "are", //$NON-NLS-1$
    "assertion", //$NON-NLS-1$
    "at", //$NON-NLS-1$
    "bit_length", //$NON-NLS-1$
    "both", //$NON-NLS-1$
    "cascaded", //$NON-NLS-1$
    "catalog", //$NON-NLS-1$
    "char_length", //$NON-NLS-1$
    "character_length", //$NON-NLS-1$
    "collate", //$NON-NLS-1$
    "collation", //$NON-NLS-1$
    "constraints", //$NON-NLS-1$
    "convert", //$NON-NLS-1$
    "corresponding", //$NON-NLS-1$
    "deallocate", //$NON-NLS-1$
    "deferrable", //$NON-NLS-1$
    "diagnostics", //$NON-NLS-1$
    "domain", //$NON-NLS-1$
    "extract", //$NON-NLS-1$
    "false", //$NON-NLS-1$
    "found", //$NON-NLS-1$
    "get", //$NON-NLS-1$
    "identity", //$NON-NLS-1$
    "insensitive", //$NON-NLS-1$
    "interval", //$NON-NLS-1$
    "last", //$NON-NLS-1$
    "leading", //$NON-NLS-1$
    "level", //$NON-NLS-1$
    "match", //$NON-NLS-1$
    "module", //$NON-NLS-1$
    "names", //$NON-NLS-1$
    "national", //$NON-NLS-1$
    "natural", //$NON-NLS-1$
    "nchar", //$NON-NLS-1$
    "next", //$NON-NLS-1$
    "octet_length", //$NON-NLS-1$
    "output", //$NON-NLS-1$
    "overlaps", //$NON-NLS-1$
    "pad", //$NON-NLS-1$
    "partial", //$NON-NLS-1$
    "position", //$NON-NLS-1$
    "preserve", //$NON-NLS-1$
    "prior", //$NON-NLS-1$
    "relative", //$NON-NLS-1$
    "scroll", //$NON-NLS-1$
    "session", //$NON-NLS-1$
    "session_user", //$NON-NLS-1$
    "space", //$NON-NLS-1$
    "system_user", //$NON-NLS-1$
    "then", //$NON-NLS-1$
    "timezone_hour", //$NON-NLS-1$
    "timezone_minute", //$NON-NLS-1$
    "trailing", //$NON-NLS-1$
    "translation", //$NON-NLS-1$
    "true", //$NON-NLS-1$
    "unknown", //$NON-NLS-1$
    "usage", //$NON-NLS-1$
    "zone", //$NON-NLS-1$
    "boolean", //$NON-NLS-1$
    "constant", //$NON-NLS-1$
    "elsif", //$NON-NLS-1$
    "if", //$NON-NLS-1$
    "number", //$NON-NLS-1$
    "record", //$NON-NLS-1$
    "spaces", //$NON-NLS-1$
    "varchar2", //$NON-NLS-1$
    "zero", //$NON-NLS-1$
    "zeros", //$NON-NLS-1$
    "converttimestamptodate", //$NON-NLS-1$
    "exit", //$NON-NLS-1$
    "formatauditheader", //$NON-NLS-1$
    "formatattributesubstring", //$NON-NLS-1$
    "getcurrenttimestamp", //$NON-NLS-1$
    "index by binary_integer", //$NON-NLS-1$
    "is table of", //$NON-NLS-1$
    "loop", //$NON-NLS-1$
    "lpad", //$NON-NLS-1$
    "nodule", //$NON-NLS-1$
    "%notFound", //$NON-NLS-1$
    "others", //$NON-NLS-1$
    "originplus", //$NON-NLS-1$
    "reply", //$NON-NLS-1$
    "replyrepeatinggroup", //$NON-NLS-1$
    "request", //$NON-NLS-1$
    "row_not_found", //$NON-NLS-1$
    "sql_i_o_correct", //$NON-NLS-1$
    "to_char", //$NON-NLS-1$
    "to_number", //$NON-NLS-1$
    "while", //$NON-NLS-1$
    "binary_integer", //$NON-NLS-1$
    "do", //$NON-NLS-1$
    "exception_init", //$NON-NLS-1$
    "minus", //$NON-NLS-1$
    "nowait", //$NON-NLS-1$
    "positive", //$NON-NLS-1$
    "pragma", //$NON-NLS-1$
    "raise", //$NON-NLS-1$
    "savepoint", //$NON-NLS-1$
    "segment", //$NON-NLS-1$
    "start"};	 //$NON-NLS-1$
    
    private static String sqlFunctions[] = {
      "abs", //$NON-NLS-1$
      "absval", //$NON-NLS-1$
      "acos", //$NON-NLS-1$
      "ascii", //$NON-NLS-1$
      "asin", //$NON-NLS-1$
      "atan", //$NON-NLS-1$
      "atan2", //$NON-NLS-1$
      "avg", //$NON-NLS-1$
      "bigint",              // also built-in data type //$NON-NLS-1$
      "blob",                // also built-in data type //$NON-NLS-1$
      "ceil", //$NON-NLS-1$
      "ceiling", //$NON-NLS-1$
      "char",                // also built-in data type //$NON-NLS-1$
      "chr", //$NON-NLS-1$
      "clob",                // also built-in data type //$NON-NLS-1$
      "coalesce", //$NON-NLS-1$
      "concat", //$NON-NLS-1$
      "corr", //$NON-NLS-1$
      "correlation", //$NON-NLS-1$
      "cos", //$NON-NLS-1$
      "cot", //$NON-NLS-1$
      "count", //$NON-NLS-1$
      "count_big", //$NON-NLS-1$
      "covar", //$NON-NLS-1$
      "covariance", //$NON-NLS-1$
      "date",                // also built-in data type //$NON-NLS-1$
      "day", //$NON-NLS-1$
      "dayname", //$NON-NLS-1$
      "dayofweek", //$NON-NLS-1$
      "dayofyear", //$NON-NLS-1$
      "days", //$NON-NLS-1$
      "dbclob",              // also built-in data type //$NON-NLS-1$
      "dec", //$NON-NLS-1$
      "decimal",             // also built-in data type //$NON-NLS-1$
      "degrees", //$NON-NLS-1$
      "deref", //$NON-NLS-1$
      "difference", //$NON-NLS-1$
      "digits", //$NON-NLS-1$
      "dlcomment", //$NON-NLS-1$
      "dllinktype", //$NON-NLS-1$
      "dlurlcomplete", //$NON-NLS-1$
      "dlurlpath", //$NON-NLS-1$
      "dlurlpathonly", //$NON-NLS-1$
      "dlurlscheme", //$NON-NLS-1$
      "dlurlserver", //$NON-NLS-1$
      "dlvalue", //$NON-NLS-1$
      "double",              // also built-in data type //$NON-NLS-1$
      "double_precision", //$NON-NLS-1$
      "event_mon_state", //$NON-NLS-1$
      "exp", //$NON-NLS-1$
      "float",               // also built-in data type //$NON-NLS-1$
      "floor", //$NON-NLS-1$
      "generate_unique", //$NON-NLS-1$
      "graphic",             // also built-in data type //$NON-NLS-1$
      "grouping", //$NON-NLS-1$
      "hex", //$NON-NLS-1$
      "hour", //$NON-NLS-1$
      "insert",              // also SQL statement //$NON-NLS-1$
      "int", //$NON-NLS-1$
      "integer",             // also built-in data type //$NON-NLS-1$
      "julian_day", //$NON-NLS-1$
      "lcase", //$NON-NLS-1$
      "left", //$NON-NLS-1$
      "length", //$NON-NLS-1$
      "ln", //$NON-NLS-1$
      "locate", //$NON-NLS-1$
      "log", //$NON-NLS-1$
      "log10", //$NON-NLS-1$
      "long_varchar", //$NON-NLS-1$
      "long_vargraphic", //$NON-NLS-1$
      "lower", //$NON-NLS-1$
      "ltrim", //$NON-NLS-1$
      "max", //$NON-NLS-1$
      "microsecond", //$NON-NLS-1$
      "midnight_seconds", //$NON-NLS-1$
      "min", //$NON-NLS-1$
      "minute", //$NON-NLS-1$
      "mod", //$NON-NLS-1$
      "month", //$NON-NLS-1$
      "monthname", //$NON-NLS-1$
      "nodenumber", //$NON-NLS-1$
      "nullif", //$NON-NLS-1$
      "partition", //$NON-NLS-1$
      "posstr", //$NON-NLS-1$
      "power", //$NON-NLS-1$
      "quarter", //$NON-NLS-1$
      "radians", //$NON-NLS-1$
      "raise_error", //$NON-NLS-1$
      "rand", //$NON-NLS-1$
      "real",                // also built-in data type //$NON-NLS-1$
      "regr_avgx", //$NON-NLS-1$
      "regr_avgy", //$NON-NLS-1$
      "regr_count", //$NON-NLS-1$
      "regr_intercept", //$NON-NLS-1$
      "regr_icpt", //$NON-NLS-1$
      "regr_r2", //$NON-NLS-1$
      "regr_slope", //$NON-NLS-1$
      "regr_sxx", //$NON-NLS-1$
      "regr_sxy", //$NON-NLS-1$
      "regr_syy", //$NON-NLS-1$
      "repeat", //$NON-NLS-1$
      "replace", //$NON-NLS-1$
      "right", //$NON-NLS-1$
      "round", //$NON-NLS-1$
      "rtrim", //$NON-NLS-1$
      "second", //$NON-NLS-1$
      "sign", //$NON-NLS-1$
      "sin", //$NON-NLS-1$
      "smallint",            // also built-in data type //$NON-NLS-1$
      "soundex", //$NON-NLS-1$
      "space", //$NON-NLS-1$
      "sqlcache_snapshot", //$NON-NLS-1$
      "sqrt", //$NON-NLS-1$
      "stddev", //$NON-NLS-1$
      "substr", //$NON-NLS-1$
      "sum", //$NON-NLS-1$
      "table_name", //$NON-NLS-1$
      "table_schema", //$NON-NLS-1$
      "tan", //$NON-NLS-1$
      "time",                // also built-in data type //$NON-NLS-1$
      "timestamp",           // also built-in data type //$NON-NLS-1$
      "timestamp_iso", //$NON-NLS-1$
      "timestampdiff", //$NON-NLS-1$
      "translate", //$NON-NLS-1$
      "trunc", //$NON-NLS-1$
      "truncate", //$NON-NLS-1$
      "type_id", //$NON-NLS-1$
      "type_name", //$NON-NLS-1$
      "type_schema", //$NON-NLS-1$
      "ucase", //$NON-NLS-1$
      "upper", //$NON-NLS-1$
      "value", //$NON-NLS-1$
      "var", //$NON-NLS-1$
      "varchar",             // also built-in data type //$NON-NLS-1$
      "vargraphic",          // also built-in data type //$NON-NLS-1$
      "variance", //$NON-NLS-1$
      "week", //$NON-NLS-1$
      "year" //$NON-NLS-1$
      };

    
   	static String[] fgTokenProperties= {
   		IConstants.SQL_COLOR_TABLE,
		IConstants.SQL_COLOR_COLUMS,
		IConstants.SQL_COLOR_KEYWORD,
		IConstants.SQL_COLOR_STRING,
		IConstants.SQL_COLOR_DEFAULT
	};

	private VersionedWordRule fVersionedWordRule;
	//private static String[] fgTypes= { "void", "boolean", "char", "byte", "short", "int", "long", "float", "double" }; 

	//private static String[] fgConstants= { "false", "null", "true" }; 

	private Dictionary dictionary;
	/**
	 * Creates a Java code scanner
	 */
	public SQLCodeScanner(IColorManager manager, IPreferenceStore store, Dictionary dictionary) {
		super(manager, store);
		
		this.dictionary=dictionary;
		initialize();
	}
	

	/*
	 * @see AbstractJavaScanner#createRules()
	 */
	@Override
	protected synchronized List<IRule> createRules() {
				
		List<IRule> rules= new ArrayList<IRule>();		
		
		// Add rule for character constants.
		Token token= getToken(IConstants.SQL_COLOR_STRING);
		rules.add(new SingleLineRule("'", "'", token, '\\')); //$NON-NLS-2$ //$NON-NLS-1$
				
		
		// Add generic whitespace rule.
		rules.add(new WhitespaceRule(new net.sourceforge.sqlexplorer.sqleditor.SQLWhitespaceDetector()));
		
		
		// Add word rule for keywords, types, and constants.
		token= getToken(IConstants.SQL_COLOR_DEFAULT);
		//WordRule wordRule= new WordRule(new org.gnu.amaz.SQLEditor.util.SQLWordDetector(), token);
		//UnsignedWordRule wordRule= new UnsignedWordRule(new net.sourceforge.jfacedbc.sqleditor.SQLWordDetector(), token);
		UnsignedWordRule wordRule= new UnsignedWordRule(new SQLWordDetector(), token,getToken(IConstants.SQL_COLOR_TABLE),getToken(IConstants.SQL_COLOR_COLUMS),dictionary);

		token= getToken(IConstants.SQL_COLOR_KEYWORD);
		for (int i=0; i<fgKeywords.length; i++)
			wordRule.addWord(fgKeywords[i], token);
		for (int i=0; i<sqlFunctions.length; i++)
			wordRule.addWord(sqlFunctions[i], token);
		
		token= getToken(IConstants.SQL_COLOR_TABLE);
		
		if(dictionary!=null){
			Iterator<String> it=dictionary.getTableNames();
			while(it.hasNext()){
				wordRule.addWord(it.next().toString(),token);
			}
			it=dictionary.getCatalogSchemaNames();
			while(it.hasNext()){
				wordRule.addWord(it.next().toString(),token);
			}
		}
		

		rules.add(wordRule);
		
		
		setDefaultReturnToken(getToken(IConstants.SQL_COLOR_DEFAULT));
		return rules;
	}
	
	@Override
	protected String[] getTokenProperties() {
		return fgTokenProperties;
	}

	/*
	 * @see RuleBasedScanner#setRules(IRule[])
	 */
	@Override
	public void setRules(IRule[] rules) {
		int i;
		for (i= 0; i < rules.length; i++)
			if (rules[i].equals(fVersionedWordRule))
				break;

		// not found - invalidate fVersionedWordRule
		if (i == rules.length)
			fVersionedWordRule= null;
		
		super.setRules(rules);	
	}
	
	@Override
	public boolean affectsBehavior(PropertyChangeEvent event) {
		return super.affectsBehavior(event);
	}
	
	@Override
	public void adaptToPreferenceChange(PropertyChangeEvent event) {
		super.adaptToPreferenceChange(event);
	}



}
