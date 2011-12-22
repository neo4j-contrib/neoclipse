/*
 * Copyright (C) 2006 Davy Vanherbergen
 * dvanherbergen@users.sourceforge.net
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
package net.sourceforge.sqlexplorer.dbdetail.tab;

import java.sql.DatabaseMetaData;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dataset.DataSet;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;
import net.sourceforge.squirrel_sql.fw.sql.SQLDatabaseMetaData;

/**
 * @author Davy Vanherbergen
 * 
 */
public class ConnectionInfoTab extends AbstractDataSetTab {

    public String getLabelText() {
        return Messages.getString("DatabaseDetailView.Tab.ConnectionInfo");
    }

    public String getStatusMessage() {
        return Messages.getString("DatabaseDetailView.Tab.ConnectionInfo.status") + " " + getNode().getSession().getUser().getDescription();
    }
    
    public DataSet getDataSet() throws Exception {

        INode node = getNode();

        if (node == null) {
            return null;
        }

        SQLDatabaseMetaData sqlMetaData = node.getSession().getMetaData();
        DatabaseMetaData jdbcMetaData = sqlMetaData.getJDBCMetaData();

        String[] header = new String[2];
        header[0] = Messages.getString("DatabaseDetailView.Tab.ConnectionInfo.Property");
        header[1] = Messages.getString("DatabaseDetailView.Tab.ConnectionInfo.Value");

        String[][] data = new String[124][2];

    	SQLConnection connection = node.getSession().grabConnection();
    	boolean commitOnClose = false;
        try {
        	commitOnClose = connection.getCommitOnClose();
        } finally{
        	node.getSession().releaseConnection(connection);
        }
        
            data[0][0] = Messages.getString("DatabaseDetailView.Tab.ConnectionInfo.DatabaseProductName");
            try {data[0][1] = sqlMetaData.getDatabaseProductName();} catch (Throwable e) {}                
            data[1][0] = Messages.getString("DatabaseDetailView.Tab.ConnectionInfo.DriverMajor");
            try {data[1][1] = "" + jdbcMetaData.getDriverMajorVersion();} catch (Throwable e) {}        
            data[2][0] = Messages.getString("DatabaseDetailView.Tab.ConnectionInfo.DriverMinor");
            try {data[2][1] = "" + jdbcMetaData.getDriverMinorVersion();} catch (Throwable e) {}        
            data[3][0] = Messages.getString("DatabaseDetailView.Tab.ConnectionInfo.DriverName");
            try {data[3][1] = "" + sqlMetaData.getDriverName();} catch (Throwable e) {}
            data[4][0] = Messages.getString("DatabaseDetailView.Tab.ConnectionInfo.DriverVersion");
            try {data[4][1] = "" + jdbcMetaData.getDriverVersion();} catch (Throwable e) {}
            data[5][0] = Messages.getString("DatabaseDetailView.Tab.ConnectionInfo.UserName");
            try {data[5][1] = "" + sqlMetaData.getUserName();} catch (Throwable e) {}
            data[6][0] = Messages.getString("DatabaseDetailView.Tab.ConnectionInfo.URL");
            try {data[6][1] = "" + jdbcMetaData.getURL();} catch (Throwable e) {}
            data[7][0] = Messages.getString("DatabaseDetailView.Tab.ConnectionInfo.AutocommitMode");
            try {data[7][1] = "" + jdbcMetaData.getConnection().getAutoCommit();} catch (Throwable e) {}
            data[8][0] = Messages.getString("DatabaseDetailView.Tab.ConnectionInfo.CommitOnClose");            
            data[8][1] = "" + commitOnClose;            
            data[9][0] = Messages.getString("DatabaseDetailView.Tab.ConnectionInfo.ProceduresCallable");
            try {data[9][1] = "" + jdbcMetaData.allProceduresAreCallable();} catch (Throwable e) {}
            data[10][0] = Messages.getString("DatabaseDetailView.Tab.ConnectionInfo.TablesSelectable");
            try {data[10][1] = "" + jdbcMetaData.allTablesAreSelectable();} catch (Throwable e) {}
            data[11][0] = Messages.getString("DatabaseDetailView.Tab.ConnectionInfo.NullsSortedHigh");
            try {data[11][1] = "" + jdbcMetaData.nullsAreSortedHigh();} catch (Throwable e) {}
            data[12][0] = Messages.getString("DatabaseDetailView.Tab.ConnectionInfo.NullsSortedLow");
            try {data[12][1] = "" + jdbcMetaData.nullsAreSortedLow();} catch (Throwable e) {}
            data[13][0] = Messages.getString("DatabaseDetailView.Tab.ConnectionInfo.NullsSortedStart");
            try {data[13][1] = "" + jdbcMetaData.nullsAreSortedAtStart();} catch (Throwable e) {}
            data[14][0] = Messages.getString("DatabaseDetailView.Tab.ConnectionInfo.NullsSortedEnd");
            try {data[14][1] = "" + jdbcMetaData.nullsAreSortedAtEnd();} catch (Throwable e) {}
            data[15][0] = "Result Set Holdability";
            try {data[15][1] = "" + jdbcMetaData.getResultSetHoldability();} catch (Throwable e) {}
            data[16][0] = "Uses Local Files";
            try {data[16][1] = "" + jdbcMetaData.usesLocalFiles();} catch (Throwable e) {}
            data[17][0] = "Uses Local File per Table";
            try { data[17][1] = "" + jdbcMetaData.usesLocalFilePerTable();} catch (Throwable e) {}
            data[18][0] = "Supports Mixed Case Identifiers";
            try {data[18][1] = "" + jdbcMetaData.supportsMixedCaseIdentifiers();} catch (Throwable e) {}
            data[19][0] = "Stores Upper Case Identifiers";
            try {data[19][1] = "" + jdbcMetaData.storesUpperCaseIdentifiers();} catch (Throwable e) {}
            data[20][0] = "Stores Lower Case Identifiers";
            try {data[20][1] = "" + jdbcMetaData.storesLowerCaseIdentifiers();} catch (Throwable e) {}
            data[21][0] = "Stores Mixed Case Identifiers";
            try {data[21][1] = "" + jdbcMetaData.storesMixedCaseIdentifiers();} catch (Throwable e) {}
            data[22][0] = "Supports Mixed Case Quoted Identifiers";
            try {data[22][1] = "" + jdbcMetaData.supportsMixedCaseQuotedIdentifiers();} catch (Throwable e) {}
            data[23][0] = "Stores Upper Case Quoted Identifiers";
            try {data[23][1] = "" + jdbcMetaData.storesUpperCaseQuotedIdentifiers();} catch (Throwable e) {}
            data[24][0] = "Stores Lower Case Quoted Identifiers";
            try {data[24][1] = "" + jdbcMetaData.storesLowerCaseQuotedIdentifiers();} catch (Throwable e) {}
            data[25][0] = "Stores Mixed Case Quoted Identifiers";
            try {data[25][1] = "" + jdbcMetaData.storesMixedCaseQuotedIdentifiers();} catch (Throwable e) {}
            data[26][0] = "Identifier Quote";
            try { data[26][1] = "" + jdbcMetaData.getIdentifierQuoteString();} catch (Throwable e) {}
            data[27][0] = "Search String Escape";
            try {data[27][1] = "" + jdbcMetaData.getSearchStringEscape();} catch (Throwable e) {}
            data[28][0] = "Extra Name Characters";
            try {data[28][1] = "" + jdbcMetaData.getExtraNameCharacters();} catch (Throwable e) {}
            data[29][0] = "Supports Alter Table With Add Column";
            try { data[29][1] = "" + jdbcMetaData.supportsAlterTableWithAddColumn();} catch (Throwable e) {}
            data[30][0] = "Supports Alter Table With Drop Column";
            try {data[30][1] = "" + jdbcMetaData.supportsAlterTableWithDropColumn();} catch (Throwable e) {}
            data[31][0] = "Supports Column Aliasing";
            try {data[31][1] = "" + jdbcMetaData.supportsColumnAliasing();} catch (Throwable e) {}
            data[32][0] = "Null Plus Non Null Is Null";
            try {data[32][1] = "" + jdbcMetaData.nullPlusNonNullIsNull();} catch (Throwable e) {}
            data[33][0] = "Supports Convert";
            try {data[33][1] = "" + jdbcMetaData.supportsConvert();} catch (Throwable e) {}
            data[34][0] = "Supports Table Correlation Names";
            try {data[34][1] = "" + jdbcMetaData.supportsTableCorrelationNames();} catch (Throwable e) {}
            data[35][0] = "Supports Different Table Correlation Names";
            try {data[35][1] = "" + jdbcMetaData.supportsDifferentTableCorrelationNames();} catch (Throwable e) {}
            data[36][0] = "Supports Expressions in Order By";
            try {data[36][1] = "" + jdbcMetaData.supportsExpressionsInOrderBy();} catch (Throwable e) {}
            data[37][0] = "Supports Order By Unrelated";
            try {data[37][1] = "" + jdbcMetaData.supportsOrderByUnrelated();} catch (Throwable e) {}
            data[38][0] = "Supports Group By";
            try { data[38][1] = "" + jdbcMetaData.supportsGroupBy();} catch (Throwable e) {}
            data[39][0] = "Supports Group By Unrelated";
            try {data[39][1] = "" + jdbcMetaData.supportsGroupByUnrelated();} catch (Throwable e) {}
            data[40][0] = "Supports Group By Beyond Select";
            try {data[40][1] = "" + jdbcMetaData.supportsGroupByBeyondSelect();} catch (Throwable e) {}
            data[41][0] = "Supports Like Escape Clause";
            try {data[41][1] = "" + jdbcMetaData.supportsLikeEscapeClause();} catch (Throwable e) {}
            data[42][0] = "Supports Multiple Result Sets";
            try {data[42][1] = "" + jdbcMetaData.supportsMultipleResultSets();} catch (Throwable e) {}
            data[43][0] = "Supports Multiple Open Results ";
            try {data[43][1] = "" + jdbcMetaData.supportsMultipleOpenResults();} catch (Throwable e) {}
            data[44][0] = "Supports Multiple Transactions";
            try {data[44][1] = "" + jdbcMetaData.supportsMultipleTransactions();} catch (Throwable e) {}
            data[45][0] = "Supports Non Nullable Columns";
            try {data[45][1] = "" + jdbcMetaData.supportsNonNullableColumns();} catch (Throwable e) {}
            data[46][0] = "Supports Minimum SQL Grammar";
            try {data[46][1] = "" + jdbcMetaData.supportsMinimumSQLGrammar();} catch (Throwable e) {}
            data[47][0] = "Supports Core SQL Grammar";
            try {data[47][1] = "" + jdbcMetaData.supportsCoreSQLGrammar();} catch (Throwable e) {}
            data[48][0] = "Supports Extended SQL Grammar";
            try {data[48][1] = "" + jdbcMetaData.supportsExtendedSQLGrammar();} catch (Throwable e) {}
            data[49][0] = "Supports ANSI92 Entry Level SQL";
            try {data[49][1] = "" + jdbcMetaData.supportsANSI92EntryLevelSQL();} catch (Throwable e) {}
            data[50][0] = "Supports ANSI92 Intermediate SQL";
            try {data[50][1] = "" + jdbcMetaData.supportsANSI92IntermediateSQL();} catch (Throwable e) {}
            data[51][0] = "Supports ANSI92 Full SQL";
            try {data[51][1] = "" + jdbcMetaData.supportsANSI92FullSQL();} catch (Throwable e) {}
            data[52][0] = "Supports Integrity Enhancement Facility";
            try {data[52][1] = "" + jdbcMetaData.supportsIntegrityEnhancementFacility();} catch (Throwable e) {}
            data[53][0] = "Supports Outer Joins";
            try {data[53][1] = "" + jdbcMetaData.supportsOuterJoins();} catch (Throwable e) {}
            data[54][0] = "Supports Full Outer Joins";
            try {data[54][1] = "" + jdbcMetaData.supportsFullOuterJoins();} catch (Throwable e) {}
            data[55][0] = "Supports Limited Outer Joins";
            try {data[55][1] = "" + jdbcMetaData.supportsLimitedOuterJoins();} catch (Throwable e) {}
            data[56][0] = "Schema Term";
            try {data[56][1] = "" + jdbcMetaData.getSchemaTerm();} catch (Throwable e) {}
            data[57][0] = "Procedure Term";
            try {data[57][1] = "" + jdbcMetaData.getProcedureTerm();} catch (Throwable e) {}
            data[58][0] = "Catalog Term";
            try {data[58][1] = "" + jdbcMetaData.getCatalogTerm();} catch (Throwable e) {}
            data[59][0] = "Is Catalog at Start";
            try {data[59][1] = "" + jdbcMetaData.isCatalogAtStart();} catch (Throwable e) {}
            data[60][0] = "Catalog Separator";
            try {data[60][1] = "" + jdbcMetaData.getCatalogSeparator();} catch (Throwable e) {}
            data[61][0] = "Supports Schemas In Data Manipulation";
            try {data[61][1] = "" + jdbcMetaData.supportsSchemasInDataManipulation();} catch (Throwable e) {}
            data[62][0] = "Supports Schemas In Procedure Calls";
            try {data[62][1] = "" + jdbcMetaData.supportsSchemasInProcedureCalls();} catch (Throwable e) {}
            data[63][0] = "Supports Schemas In Table Definitions";
            try {data[63][1] = "" + jdbcMetaData.supportsSchemasInTableDefinitions();} catch (Throwable e) {}
            data[64][0] = "Supports Schemas In Index Definitions";
            try {data[64][1] = "" + jdbcMetaData.supportsSchemasInIndexDefinitions();} catch (Throwable e) {}
            data[65][0] = "Supports Schemas In Privilege Definitions";
            try {data[65][1] = "" + jdbcMetaData.supportsSchemasInPrivilegeDefinitions();} catch (Throwable e) {}
            data[66][0] = "Supports Catalogs In Data Manipulation";
            try {data[66][1] = "" + jdbcMetaData.supportsCatalogsInDataManipulation();} catch (Throwable e) {}
            data[67][0] = "Supports Catalogs In Procedure Calls";
            try {data[67][1] = "" + jdbcMetaData.supportsCatalogsInProcedureCalls();} catch (Throwable e) {}
            data[68][0] = "Supports Catalogs In Table Definitions";
            try {data[68][1] = "" + jdbcMetaData.supportsCatalogsInTableDefinitions();} catch (Throwable e) {}
            data[69][0] = "Supports Catalogs In Index Definitions";
            try {data[69][1] = "" + jdbcMetaData.supportsCatalogsInIndexDefinitions();} catch (Throwable e) {}
            data[70][0] = "Supports Catalogs In Privilege Definitions";
            try {data[70][1] = "" + jdbcMetaData.supportsCatalogsInPrivilegeDefinitions();} catch (Throwable e) {}
            data[71][0] = "Supports Positioned Delete";
            try {data[71][1] = "" + jdbcMetaData.supportsPositionedDelete();} catch (Throwable e) {}
            data[72][0] = "Supports Positioned Update";
            try {data[72][1] = "" + jdbcMetaData.supportsPositionedUpdate();} catch (Throwable e) {}
            data[73][0] = "Supports Stored Procedures";
            try {data[73][1] = "" + jdbcMetaData.supportsStoredProcedures();} catch (Throwable e) {}
            data[74][0] = "Supports Subqueries In Comparisons";
            try {data[74][1] = "" + jdbcMetaData.supportsSubqueriesInComparisons();} catch (Throwable e) {}
            data[75][0] = "Supports Subqueries In Exists";
            try {data[75][1] = "" + jdbcMetaData.supportsSubqueriesInExists();} catch (Throwable e) {}
            data[76][0] = "Supports Subqueries in IN Statements";
            try {data[76][1] = "" + jdbcMetaData.supportsSubqueriesInIns();} catch (Throwable e) {}
            data[77][0] = "Supports Subqueries in Quantified Expressions";
            try {data[77][1] = "" + jdbcMetaData.supportsSubqueriesInQuantifieds();} catch (Throwable e) {}
            data[78][0] = "Supports Correlated Subqueries";
            try {data[78][1] = "" + jdbcMetaData.supportsCorrelatedSubqueries();} catch (Throwable e) {}
            data[79][0] = "Supports Union";
            try {data[79][1] = "" + jdbcMetaData.supportsUnion();} catch (Throwable e) {}
            data[80][0] = "Supports Union All";
            try {data[80][1] = "" + jdbcMetaData.supportsUnionAll();} catch (Throwable e) {}
            data[81][0] = "Supports Open Cursors Across Commit";
            try {data[81][1] = "" + jdbcMetaData.supportsOpenCursorsAcrossCommit();} catch (Throwable e) {}
            data[82][0] = "Supports Open Cursors Across Rollback";
            try {data[82][1] = "" + jdbcMetaData.supportsOpenCursorsAcrossRollback();} catch (Throwable e) {}
            data[83][0] = "Supports Open Statements Across Commit";
            try {data[83][1] = "" + jdbcMetaData.supportsOpenStatementsAcrossCommit();} catch (Throwable e) {}
            data[84][0] = "Supports Open Statements Across Rollback";
            try {data[84][1] = "" + jdbcMetaData.supportsOpenStatementsAcrossRollback();} catch (Throwable e) {}
            data[85][0] = "Max Binary Literal Length";
            try {data[85][1] = "" + jdbcMetaData.getMaxBinaryLiteralLength();} catch (Throwable e) {}
            data[86][0] = "Max Char Literal Length";
            try {data[86][1] = "" + jdbcMetaData.getMaxCharLiteralLength();} catch (Throwable e) {}
            data[87][0] = "Max Column Name Length";
            try {data[87][1] = "" + jdbcMetaData.getMaxColumnNameLength();} catch (Throwable e) {}
            data[88][0] = "Max Columns In Group By";
            try {data[88][1] = "" + jdbcMetaData.getMaxColumnsInGroupBy();} catch (Throwable e) {}
            data[89][0] = "Max Columns In Index";
            try {data[89][1] = "" + jdbcMetaData.getMaxColumnsInIndex();} catch (Throwable e) {}
            data[90][0] = "Max Columns In Order By";
            try {data[90][1] = "" + jdbcMetaData.getMaxColumnsInOrderBy();} catch (Throwable e) {}
            data[91][0] = "Max Columns In Select";
            try {data[91][1] = "" + jdbcMetaData.getMaxColumnsInSelect();} catch (Throwable e) {}
            data[92][0] = "Max Columns In Table";
            try {data[92][1] = "" + jdbcMetaData.getMaxColumnsInTable();} catch (Throwable e) {}
            data[93][0] = "Max Connections";
            try {data[93][1] = "" + jdbcMetaData.getMaxConnections();} catch (Throwable e) {}
            data[94][0] = "Max Cursor Name Length";
            try {data[94][1] = "" + jdbcMetaData.getMaxCursorNameLength();} catch (Throwable e) {}
            data[95][0] = "Max Index Length";
            try {data[95][1] = "" + jdbcMetaData.getMaxIndexLength();} catch (Throwable e) {}
            data[96][0] = "Max Schema Name Length";
            try {data[96][1] = "" + jdbcMetaData.getMaxSchemaNameLength();} catch (Throwable e) {}
            data[97][0] = "Max Procedure Name Length";
            try {data[97][1] = "" + jdbcMetaData.getMaxProcedureNameLength();} catch (Throwable e) {}
            data[98][0] = "Max Catalog Name Length";
            try {data[98][1] = "" + jdbcMetaData.getMaxCatalogNameLength();} catch (Throwable e) {}
            data[99][0] = "Max Row Size";
            try {data[99][1] = "" + jdbcMetaData.getMaxRowSize();} catch (Throwable e) {}
            data[100][0] = "Max Row Size Include Blobs";
            try {data[100][1] = "" + jdbcMetaData.doesMaxRowSizeIncludeBlobs();} catch (Throwable e) {}
            data[101][0] = "Max Statement Length";
            try {data[101][1] = "" + jdbcMetaData.getMaxStatementLength();} catch (Throwable e) {}
            data[102][0] = "Max Statements";
            try {data[102][1] = "" + jdbcMetaData.getMaxStatements();} catch (Throwable e) {}
            data[103][0] = "Max Table Name Length";
            try {data[103][1] = "" + jdbcMetaData.getMaxTableNameLength();} catch (Throwable e) {}
            data[104][0] = "Max Tables In Select";
            try {data[104][1] = "" + jdbcMetaData.getMaxTablesInSelect();} catch (Throwable e) {}
            data[105][0] = "Max User Name Length";
            try {data[105][1] = "" + jdbcMetaData.getMaxUserNameLength();} catch (Throwable e) {}

            data[106][0] = "Default Transaction Isolation";
            try {
	            int isol = jdbcMetaData.getDefaultTransactionIsolation();
	            String is = null;
	            switch (isol) {
	                case java.sql.Connection.TRANSACTION_NONE:
	                    is = "TRANSACTION_NONE";
	                    break;
	
	                case java.sql.Connection.TRANSACTION_READ_COMMITTED:
	                    is = "TRANSACTION_READ_COMMITTED";
	                    break;
	
	                case java.sql.Connection.TRANSACTION_READ_UNCOMMITTED:
	                    is = "TRANSACTION_READ_UNCOMMITTED";
	                    break;
	
	                case java.sql.Connection.TRANSACTION_REPEATABLE_READ:
	                    is = "TRANSACTION_REPEATABLE_READ";
	                    break;
	
	                case java.sql.Connection.TRANSACTION_SERIALIZABLE:
	                    is = "TRANSACTION_SERIALIZABLE";
	                    break;
	
	                default:
	                    is = "";
	                    break;
	            }
	
	            data[106][1] = is;
            } catch (Throwable e) {}
           
            data[107][0] = "Supports Transactions";
            try {data[107][1] = "" + jdbcMetaData.supportsTransactions();} catch (Throwable e) {}
            data[108][0] = "Supports Data Definition and Data Manipulation Transactions";
            try {data[108][1] = "" + jdbcMetaData.supportsDataDefinitionAndDataManipulationTransactions();} catch (Throwable e) {}
            data[109][0] = "Supports Data Manipulation Transactions Only";
            try {data[109][1] = "" + jdbcMetaData.supportsDataManipulationTransactionsOnly();} catch (Throwable e) {}
            data[110][0] = "Data Definition Causes Transaction Commit";
            try {data[110][1] = "" + jdbcMetaData.dataDefinitionCausesTransactionCommit();} catch (Throwable e) {}
            data[111][0] = "Data Definition Ignored in Transactions";
            try {data[111][1] = "" + jdbcMetaData.dataDefinitionIgnoredInTransactions();} catch (Throwable e) {}
            data[112][0] = "Supports Batch Updates";
            try {data[112][1] = "" + jdbcMetaData.supportsBatchUpdates();} catch (Throwable e) {}
            data[113][0] = "Supports Savepoints";
            try {data[113][1] = "" + jdbcMetaData.supportsSavepoints();} catch (Throwable e) {}
            data[114][0] = "Supports Named Parameters";
            try {data[114][1] = "" + jdbcMetaData.supportsNamedParameters();} catch (Throwable e) {}
            data[115][0] = "Supports Get Generated Keys";
            try {data[115][1] = "" + jdbcMetaData.supportsGetGeneratedKeys();} catch (Throwable e) {}
            data[116][0] = "Database Major Version";
            try {data[116][1] = "" + jdbcMetaData.getDatabaseMajorVersion();} catch (Throwable e) {}
            data[117][0] = "Database Minor Version";
            try {data[117][1] = "" + jdbcMetaData.getDatabaseMinorVersion();} catch (Throwable e) {}
            data[118][0] = "JDBC Minor Version";
            try {data[118][1] = "" + jdbcMetaData.getJDBCMinorVersion();} catch (Throwable e) {}
            data[119][0] = "JDBC Major Version";
            try {data[119][1] = "" + jdbcMetaData.getJDBCMajorVersion();} catch (Throwable e) {}
            data[120][0] = "SQL State Type";
            try {data[120][1] = "" + jdbcMetaData.getSQLStateType();} catch (Throwable e) {}
            data[121][0] = "Locators Update Copy";
            try {data[121][1] = "" + jdbcMetaData.locatorsUpdateCopy();} catch (Throwable e) {}
            data[122][0] = "Supports Statement Pooling";
            try {data[122][1] = "" + jdbcMetaData.supportsStatementPooling();} catch (Throwable e) {}
            
            data[123][0] = Messages.getString("DatabaseDetailView.Tab.ConnectionInfo.ReadOnly");
            try {data[123][1] = "" + jdbcMetaData.isReadOnly();} catch (Throwable e) {}
        
        DataSet dataSet = new DataSet(header, data);

        return dataSet;

    }
}
