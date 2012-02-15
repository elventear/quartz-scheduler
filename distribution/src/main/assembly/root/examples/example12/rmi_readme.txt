Example 12
==========

Overview:
=========

This example demonstrates how Quartz can be used in a client/server
environment to remotely scheudle jobs on a remote server using
RMI (Remote Method Invocation).  

This example will run a server that will execute the scheudle.  The
server itself will not schedule any jobs.   This example will also
execute a client that will connect to the server (via RMI) to 
schedule the job.  Once the job is remotely scheduled, the sceduler on
the server will run the job (at the correct time).

Note:  This example works best when you run the client and server on 
different computers.  However, you can certainly run the server and 
the client on the same box!

Running the Example:
====================

1. Configure the client.properties file and the server.properties
as necesarry (see the "Configuration" section below for details).

2. Windows users - Modify server.bat and client.bat (if necessary) 
to set your JAVA_HOME.  Run server.bat.  Once the server is started, run 
client.bat (note: these may or may not be on the same box!)
3. UNIX/Linux users - Modify server.sh and client.sh (if necessary)
to set your JAVA_HOME.  Execute server.sh.  Once the server is started, 
run client.sh (note: these may or may not be on the same box!)

Note:  If you have access to both Windows boxes and UNIX/Linux boxes, try
running the example on different platforms!

Configuration:
==============

1.  You can decide to specify a log4j.properties file to
control logging output (optional)
2.  There is a server.properties file that is used to
configure the server and the host and port that the server
is listening to for RMI request.  Typically, this host is
localhost and the port is 1099:
	
	org.quartz.scheduler.rmi.registryHost = localhost
	org.quartz.scheduler.rmi.registryPort = 1099

3.  There is a client.properties file that is used to configure
the client to tell it which server and port to connect to.  If the
client is running on the same box as the server, then localhost will
be fine.  If server is running on a different box than the client, then
you will want to specify the host or IP address for registryHost.  This
will tell the client which server/host to connect to for its remote
method invokations.