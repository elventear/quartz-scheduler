#!/bin/sh

QUARTZ_CP=""
for jarfile in $QUARTZ/*.jar; do
  QUARTZ_CP=$QUARTZ_CP:$jarfile
done

QUARTZ_CP=""
for jarfile in $QUARTZ/examples/*.jar; do
  QUARTZ_CP=$QUARTZ_CP:$jarfile
done

QUARTZ_CP=""
for jarfile in $QUARTZ/lib/core/*.jar; do
  QUARTZ_CP=$QUARTZ_CP:$jarfile
done

QUARTZ_CP=""
for jarfile in $QUARTZ/lib/build/*.jar; do
  QUARTZ_CP=$QUARTZ_CP:$jarfile
done

QUARTZ_CP=""
for jarfile in $QUARTZ/lib/optional/*.jar; do
  QUARTZ_CP=$QUARTZ_CP:$jarfile
done

echo "Classpath: " $QUARTZ_CP

