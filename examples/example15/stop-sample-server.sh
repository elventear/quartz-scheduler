#!/bin/sh
#
# All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
#


# Set the path to your Terracotta server home here
TC_HOME=../../..
if [ ! -f $TC_HOME/bin/stop-tc-server.sh ]; then
  echo "Please set TC_HOME properly."
  exit -1
fi

exec $TC_HOME/bin/stop-tc-server.sh&
