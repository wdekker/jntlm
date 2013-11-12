#!/bin/sh

APP_HOME=`dirname "$0"`
cd $APP_HOME

mvn compile
mvn dependency:copy-dependencies

TARGET="$APP_HOME/target"
LIBS="$TARGET/dependency"

CLASSPATH="$TARGET/classes"
for JAR in `ls $LIBS` ; do
	CLASSPATH=$CLASSPATH:"$LIBS/$JAR"
done

echo $CLASSPATH
exec java -cp $CLASSPATH nl.willem.http.jntlm.JNTLM $*

