#!/bin/sh
APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`

MAX_FD="maximum"

warn () {
    echo "$*"
}

die () {
    echo
    echo "$*"
    echo
    exit 1
}

if [ "$APP_HOME" ] ; then
    SAVED="$APP_HOME"
    APP_HOME=`dirname "$0"`
    if [ -d "$APP_HOME" ] ; then
        APP_HOME=`cd "$APP_HOME" >/dev/null && pwd`
    fi
    APP_HOME="$SAVED"
fi

if [ -z "$APP_HOME" ] ; then
    APP_HOME=`dirname "$0"`
fi

APP_HOME=`cd "$APP_HOME" >/dev/null && pwd`

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

JAVA_OPTS="$JAVA_OPTS"

GRADLE_OPTS="$GRADLE_OPTS"

for i in "$@" ; do
    case "$i" in
        -Dorg.gradle.daemon=*) ;;
        *) ;;
    esac
done

exec "$JAVACMD" $JAVA_OPTS \
    -classpath "$CLASSPATH" \
    org.gradle.wrapper.GradleWrapperMain \
    "$@"
