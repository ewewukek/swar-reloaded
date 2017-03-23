#!/bin/bash

if [ -z ${TMP+x} ]; then TMP=/tmp; fi

LWJGL_VERSION=2.9.3
JOML_VERSION=1.8.2

path_separator=':'
if [[ "$OSTYPE" == "cygwin" || "$OSTYPE" == "msys" ]]; then
    path_separator=';'
fi

case $1 in
clean)
    rm -rf bin build jar native
    ;;
run)
    java -cp "jar/lwjgl.jar${path_separator}jar/joml.jar${path_separator}bin" -Djava.library.path=native ewewukek/swar/Main
    ;;
build)
    if [ ! -f jar/lwjgl.jar ]; then
        echo "build: downloading lwjgl-$LWJGL_VERSION"
        curl -L -o $TMP/lwjgl.zip https://downloads.sourceforge.net/project/java-game-lib/Official%20Releases/LWJGL%20$LWJGL_VERSION/lwjgl-$LWJGL_VERSION.zip
        echo "build: extracting lwjgl-$LWJGL_VERSION"
        unzip -j -o $TMP/lwjgl.zip lwjgl-$LWJGL_VERSION/jar/lwjgl.jar -d jar >/dev/null
        unzip -j -o $TMP/lwjgl.zip lwjgl-$LWJGL_VERSION/native/linux/* -d native >/dev/null
        unzip -j -o $TMP/lwjgl.zip lwjgl-$LWJGL_VERSION/native/windows/* -d native >/dev/null
        unzip -j -o $TMP/lwjgl.zip lwjgl-$LWJGL_VERSION/native/macosx/* -d native >/dev/null
    fi
    if [ ! -f jar/joml.jar ]; then
        echo "build: downloading joml-$JOML_VERSION"
        curl -L -o jar/joml.jar https://github.com/JOML-CI/JOML/releases/download/$JOML_VERSION/joml-$JOML_VERSION.jar
    fi
    mkdir -p bin
    rm -rf bin/*
    if [ -n "$JAVA7_HOME" ]; then bootclasspath="-bootclasspath $JAVA7_HOME/jre/lib/rt.jar"; fi
    if [ -n "$JDK7_HOME" ]; then bootclasspath="-bootclasspath $JDK7_HOME/jre/lib/rt.jar"; fi
    find src -type f -name '*.java' | \
        xargs javac -source 1.7 -target 1.7 $bootclasspath -cp "jar/lwjgl.jar${path_separator}jar/joml.jar" -sourcepath src -d bin
    ;;
jar)
    ./make.sh build
    if [ $? != 0 ]; then exit; fi
    unzip -o jar/lwjgl.jar org/lwjgl/* -d bin >/dev/null
    unzip -o jar/joml.jar org/joml/* -d bin >/dev/null
    mkdir -p build
    jar cmf manifest build/swar.jar -C bin . native
    ;;
*)
    echo "usage: ./make.sh [command]"
    echo "commands: build, run, jar, clean"
esac
