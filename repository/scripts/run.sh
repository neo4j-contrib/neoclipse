#!/bin/sh
#
export JAVA_HOME=`/usr/libexec/java_home -v '1.7+'`
LAUNCHER_JAR=plugins/org.eclipse.equinox.launcher_1.2.0.v20110502.jar

java \
-showversion \
-XX:MaxPermSize=256m \
-Xms1024m \
-Xmx1024m \
-Xdock:icon=neoclipse.app/Contents/Resources/neo.icns \
-XstartOnFirstThread \
-Dorg.eclipse.swt.internal.carbon.smallFonts \
-Dosgi.requiredJavaVersion=1.5 \
-Dorg.xml.sax.driver=org.apache.xerces.parsers.SAXParser \
-jar $LAUNCHER_JAR