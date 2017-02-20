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
    java -cp jar/lwjgl.jar:jar/joml.jar:bin -Djava.library.path=native ewewukek/swar/Main
    ;;
build)
    # if [ ! -f jar/lwjgl.jar ]; then
        # echo "build: downloading lwjgl-$LWJGL_VERSION"
        # wget -q --show-progress -O $TMP/lwjgl.zip http://build.lwjgl.org/release/$LWJGL_VERSION/lwjgl-$LWJGL_VERSION.zip
        # echo "build: extracting lwjgl-$LWJGL_VERSION"
        # unzip -o $TMP/lwjgl.zip jar/* native/* -d . >/dev/null
    # fi
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
