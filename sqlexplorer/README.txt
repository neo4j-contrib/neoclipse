

Eclipse SQL Explorer v3.5.0
===========================

Upgrading from Previous Versions (Plugin and RCP)
=================================================
When you start Eclipse for the first time after installing a new version, you MUST use the 
-clean command line argument to eclipse; this resets cached information in Eclipse that can
cause problems.


Upgrading from Previous Releases (Plugin Only)
==============================================
Before upgrading (including from beta versions) you must remove previous installations
of SQLExplorer; to do this, locate your Eclipse directory and delete:

	Folders to delete
	-----------------
	features/net.sourceforge.sqlexplorer_3.5.0.*
	plugins/net.sourceforge.sqlexplorer.help_3.5.0.*
	plugins/net.sourceforge.sqlexplorer_3.5.0.*
	
	Files to delete
	---------------
	plugins/net.sourceforge.sqlexplorer.db2_3.5.0.*.jar
	plugins/net.sourceforge.sqlexplorer.mysql_3.5.0.*.jar
	plugins/net.sourceforge.sqlexplorer.oracle_3.5.0.*.jar
	plugins/net.sourceforge.sqlexplorer.postgresql_3.5.0.*.jar


JRE Version (RCP Users)
=======================
SQLExplorer 3.5.0 requires that the JRE is at least v5.0 (aka JRE 1.5); if you are still using
JRE 1.4 this is not necessarily - simply download the "SQLExplorer RCP (inc JRE)" version.


JRE Version (Plugin Users)
==========================
SQLExplorer 3.5.0 requires that the JRE is at least v5.0 (aka JRE 1.5); if you are still using
JRE 1.4 this is not necessarily a problem, even if the software you develop using Eclipse has
to target 1.4 because Eclipse can run using a /different/ JRE to the one used to compile, run,
and debug your application(s).

There are two ways to do this, which one you choose depends on whether you want to use your
new JRE5.0 for more than just running Eclipse.

Option A - JRE5.0 is *only* used to run Eclipse
-----------------------------------------------
Install the JRE into a subdirectory of your Eclipse installation directory called "jre"; note 
that the default installation path includes version numbers etc - the folder must be called 
simply "jre".  To be clear, you should have a directory layout like this:

	c:\eclipse\
		eclipse.exe
		jre\
			bin\
				javaw.exe

Every time Eclipse starts it looks for a "jre" folder and will use that by default.  You do
NOT need to have the JRE on the system PATH or be referenced from anywhere so you can be
reasonably assured that the JRE you develop against (eg v1.4) has not been replaced.


Option B - JRE5.0 is used for Eclipse and other programs, just not always
-------------------------------------------------------------------------
Install the JRE in the default place and use the -vm parameter when starting Eclipse; eg,
under Windows, modify your shortcut like so:

	c:\eclipse\eclipse.exe -vm "c:\Program Files\Java\jre_1.5.0_13\bin\javaw.exe"
	
NOTE: This is actually how you are recommended to start Eclipse because if your OS path
is changed you might get unexpected results if you find that your JRE does too.



