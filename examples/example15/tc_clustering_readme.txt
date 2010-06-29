Example 15
==========

Overview:
=========

This example demonstrates how Quartz can be used in a Terracotta clustered
environment to support failover.  In a clustered environment, you 
are running multiple instances of Quartz using a shared scheduler.
This shared scheduler is persisted in the Terracotta server, so that both
instances can share the same scheduling data.

Note:  This example works best when you run the client and server on 
different computers. However, you can certainly run the server and 
the client on the same box!

Running the Example:
====================
Note: Windows users please use equivalent Batch scripts (.bat) 

1. Configure the instance1.properties file and the instance2.properties
file as necessary (see the "Configuration" section below for details).

2. Start Terracotta server with start-sample-server.sh

3. Modify instance1.sh and instance2.sh (if necessary)
to set your JAVA_HOME and TC_HOME.  Execute instance1.sh.  Once
the first instance is started, run instance2.sh (note: these may or may 
not be on the same box!)

Note:  If you have access to both Windows boxes and UNIX/Linux boxes, try
running the example on different platforms!

Note:  You may pass the "clearJobs" argument to your batch file or script
for instance 1 to clear all existing jobs in the cluster before executing.

Note:  The script or batch file for instance two automatically uses the 
parameter "dontScheduleJobs" this allows instance 1 to do the job scheduling
and that instance 2 will not reschedule the same jobs on start up.


Configuration:
==============

1.  You can decide to specify a log4j.properties file to control logging 
output (optional)

2.  If you start Terracott server on a different box, you may need to
configure both properties file so that Quartz knows how to connect to 
your server.

The following parameter may need to be changed:

org.quartz.jobStore.tcConfigUrl = localhost:9510

