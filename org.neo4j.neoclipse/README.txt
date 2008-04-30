Installation:

As-is:
1. Install Eclipse 3.4M6
1.a Download Mylyn from http://www.eclipse.org/mylyn/downloads/ and point your Eclipse update manager to the unzipped archive as a local update site. Install it.
1.b Restart Eclipse and import this project into the workspace. There is a launch configuration included that should bring you up to test.
1.c Run the testcase creating local nodespace and point the Neoclipse at that in your test launch.

with Maven:
2. deploy your local Eclispe installation to a local maven repo -http://maven.apache.org/plugins/maven-eclipse-plugin/to-maven-mojo.html
C:\Program Files\eclipse3.4M6\>mvn eclipse:to-maven -DstripQualifier=true -DeclipseDir=. -DdeployTo="local::default::file:///$PATH_TO_MAVEN_REPO
