Example 13
==========

Overview:
=========

This example demonstrates how Quartz can be used in a clustered
environment to support failover.  In a clustered environment, you 
are running multiple instances of Quartz using a shared scheduler.
This shared scheduler is persisted in a database, so that both 
instances can share the same scheduling data.

Note:  This example works best when you run the client and server on 
different computers.  However, you can certainly run the server and 
the client on the same box!

Running the Example:
====================

1. Configure the instance1.properties file and the instance2.properties
file as necesary (see the "Configuration" section below for details).

2. Windows users - Modify instance1.bat and instance2.bat (if necessary) 
to set your JAVA_HOME and your JDBC_CP.  Run instance1.bat.  Once the 
first instance is started, run instance2.bat (note: these may or may not be 
on the same box!)
3. UNIX/Linux users - Modify instance1.sh and instance2.sh (if necessary)
to set your JAVA_HOME and your JDBC_CP.  Execute instance1.sh.  Once 
the first instance is started, run instance2.sh (note: these may or may 
not be on the same box!)

Note:  If you have access to both Windows boxes and UNIX/Linux boxes, try
running the example on different platforms!

Note:  You may pass the "clearJobs" argument to your batch file or script
for instance 1 to clear all existing jobs in the database before executing.

Note:  The script or batch file for instance two automatically uses the 
parameter "dontScheduleJobs" this allows instance 1 to do the job scheduling
and that instance 2 will not reschedule the same jobs on start up.


Configuration:
==============

1.  You can decide to specify a log4j.properties file to control logging 
output (optional)

2.  This example uses a database to maintain scheduling information in a 
clustered environment.   You will need to first install the Quartz 
database tables.  SQL table creation scripts are included with the Quartz 
distribution for many popular database platforms.

3. You will need a JDBC Driver for your database. The example uses Postgres to demonstrate
You can download Postgres JDBC driver here http://jdbc.postgresql.org 
Just put the jar under "lib" folder of the Quartz distribution 

4.  After you have installed the database scripts, you will need to 
configure both properties file so that Quartz knows how to connect to 
your database.    

The following parameters need to be set: (this shows a PostgreSQL example)

org.quartz.jobStore.class=org.quartz.impl.jdbcjobstore.JobStoreTX
org.quartz.jobStore.driverDelegateClass=org.quartz.impl.jdbcjobstore.PostgreSQLDelegate
org.quartz.jobStore.useProperties=false
org.quartz.jobStore.dataSource=myDS
org.quartz.jobStore.tablePrefix=QRTZ_
org.quartz.jobStore.isClustered=true

org.quartz.dataSource.myDS.driver = org.postgresql.Driver
org.quartz.dataSource.myDS.URL = jdbc:postgresql://localhost:5432/quartz
org.quartz.dataSource.myDS.user = quartz
org.quartz.dataSource.myDS.password = quartz
org.quartz.dataSource.myDS.maxConnections = 5
org.quartz.dataSource.myDS.validationQuery=


