Installation:
1. Install Eclipse 3.4M6

2. deploy your local Eclispe installation to a local maven repo -http://maven.apache.org/plugins/maven-eclipse-plugin/to-maven-mojo.html
C:\Program Files\eclipse3.4M6\>mvn eclipse:to-maven -DstripQualifier=true -DeclipseDir=. -DdeployTo="local::default::file:///$PATH_TO_MAVEN_REPO