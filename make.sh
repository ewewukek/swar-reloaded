#!/bin/bash

if [ -z ${TMP+x} ]; then TMP=/tmp; fi

LWJGL_VERSION=2.9.3
JOML_VERSION=1.8.2

case $1 in
clean)
    rm -rf bin
    ;;
clean-all)
    rm -rf bin jar native
    ;;
run)
    java -cp jar/lwjgl.jar:jar/joml.jar:bin -Djava.library.path=native ewewukek/swar/Game
    ;;
build)
    if [ ! -f jar/lwjgl.jar ]; then
        echo "build: downloading lwjgl-$LWJGL_VERSION"
        wget -q --show-progress -O $TMP/lwjgl.zip https://downloads.sourceforge.net/project/java-game-lib/Official%20Releases/LWJGL%20$LWJGL_VERSION/lwjgl-$LWJGL_VERSION.zip
        echo "build: extracting lwjgl-$LWJGL_VERSION"
        unzip -j -o $TMP/lwjgl.zip lwjgl-$LWJGL_VERSION/jar/lwjgl.jar -d ./jar >/dev/null
        if [[ "$OSTYPE" == "linux-gnu" ]]; then
            OS_DIR=linux
        elif [[ "$OSTYPE" == "cygwin" || "$OSTYPE" == "msys" ]]; then
            OS_DIR=windows
        elif [[ "$OSTYPE" == "darwin" ]]; then
            OS_DIR=macosx
        fi
        unzip -j -o $TMP/lwjgl.zip lwjgl-$LWJGL_VERSION/native/$OS_DIR/* -d ./native >/dev/null
    fi
    if [ ! -f jar/joml.jar ]; then
        echo "build: downloading joml-$JOML_VERSION"
        wget -q --show-progress -O jar/joml.jar https://github.com/JOML-CI/JOML/releases/download/$JOML_VERSION/joml-$JOML_VERSION.jar
    fi
    mkdir -p bin
    echo > $TMP/srclist
    shopt -s globstar
    uptodate=true
    for srcfile in src/**/*.java
    do
        binfile="bin/${srcfile:4}"
        binfile="${binfile%.java}.class"
        if [ ! -f $binfile ]; then
            echo $srcfile >> $TMP/srclist
            uptodate=false
        else
            srcctime=$(stat -c %Y $srcfile)
            binctime=$(stat -c %Y $binfile)
            if (( srcctime > binctime )); then
                echo $srcfile >> $TMP/srclist
                uptodate=false
            fi
        fi
    done
    if $uptodate ; then
        echo "build: up-to-date"
        exit
    fi
    if [ -n "$JAVA7_HOME" ]; then bootclasspath="-bootclasspath $JAVA7_HOME/lib/rt.jar"; fi
    javac @javacargs $bootclasspath @$TMP/srclist
    ;;
*)
    echo "usage: ./make.sh [command]"
    echo "commands: build, run, clean, clean-all"
esac
