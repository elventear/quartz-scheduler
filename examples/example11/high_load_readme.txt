Example 11
==========

Overview:
=========
This example demonstrates how Quartz can handle a large
number of jobs.   This example starts with 500 jobs.  However, 
this number can be changed by modifying the start scripts.  

Due to the size of the thread pool (this example uses as thread
count of 12), only 12 threads will run concurrently in the 
scheduler.   

You can change this parameter in the quartz.properties file.


Running the Example:
====================
1. Windows users - Modify the example11.bat file (if necessary) 
to set your JAVA_HOME.  Run example11.bat

2. UNIX/Linux users - Modify the example11.sh file (if necessary)
to set your JAVA_HOME.  Execute example11.sh


Configuration:
==============
1.  You can decide to specify a log4j.properties file to
control logging output (optional)

2.  This example uses the quartz.properties file to 
configure quartz.   Modify the "threadCount" property to 
control how many threads are in the Quartz thread pool 
(this limits the number of jobs that can execute at the 
same time)

3.  You can also modify the number of jobs that are run
in this example.  Simply edit the script and pass in a valid
number into the program.
