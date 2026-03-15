#!/bin/sh
DIRNAME=$(dirname "$0")
exec java $JAVA_OPTS -classpath "$DIRNAME/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
