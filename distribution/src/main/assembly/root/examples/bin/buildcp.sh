#!/bin/sh

# You May Need To Change this to your Quartz installation root
workdir=`dirname $0`
workdir=`cd ${workdir} && pwd`
QUARTZ=${workdir}/../..

QUARTZ_CP=""

for jarfile in $QUARTZ/examples/lib/*.jar; do
  QUARTZ_CP=$QUARTZ_CP:$jarfile
done


for jarfile in $QUARTZ/lib/*.jar; do
  QUARTZ_CP=$QUARTZ_CP:$jarfile
done

# Convert to Windows path if cygwin detected
# This allows users to use .sh scripts in cygwin
if [ `uname | grep CYGWIN` ]; then
  QUARTZ_CP=`cygpath -w -p $QUARTZ_CP`
fi
