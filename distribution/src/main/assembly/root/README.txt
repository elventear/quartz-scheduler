
==============================================================================
This file is intended to help you get started with the Quartz project.

For more information see http://www.quartz-scheduler.org
==============================================================================


What is Quartz?
==============================================================================

Quartz is an open source project aimed at creating a free-for-use Job 
Scheduler, with enterprise features.

Licensed under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License. You may obtain a copy
of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Additionally, a copy of the license and its accompanying notice file is
included with the distribution.


What is in this package?
==============================================================================

lib                         a directory which should contain all of the
                            third-party libraries that are needed in order
                            to use all of the features of Quartz.
                            
  + quartz-<ver>.jar        Quartz library.

samples                    a directory containing some code samples on the
                            usage of Quartz.  The first example you should
                            look at is 'example1.bat' or 'example1.sh' -
                            depending if you're a win-dos or unix person.
                            This example uses the code found in the
                            SchedTest.java class, which is also in the
                            examples directory.





Where should I start if I am new to Quartz?
==============================================================================

There is an FAQ, tutorial and configuration reference that can be found on the 
main Quartz website at http://quartz-scheduler.org/docs/index.html

Most of the Java source files are fairly well documented with JavaDOC -
consider this your "reference manual".  

Start by looking at org.quartz.Scheduler, org.quartz.Job,
org.quartz.JobDetail and org.quartz.Trigger.

Examine and run the examples found in the "examples" directory.

If you're interested in the "behind the scenes" (server-side) code,
you'll want to look at org.quartz.core.QuartzSchedulerThread, which
will make you interested in org.quartz.spi.JobStore.java,
org.quartz.spi.ThreadPool.java and org.quartz.core.JobRunShell.


What should I do if I encounter a problem?
==============================================================================

Help is available via the Quartz Users forum:

  http://forums.terracotta.org/forums/forums/show/17.page

Please report bugs / issues to JIRA at:

  https://jira.terracotta.org/jira/browse/QTZ


How can I get started with the Terracotta Job Store?
==============================================================================

The Terracotta Job Store provides an easy way to implement a highly 
available, highly scalable, and durable way to schedule jobs across 
multiple nodes. As with other Terracotta solutions, Quartz clustering 
can be achieved via Terracotta Job Store. Configure your app to use 
the Terracotta Job Store by setting the following in your quartz.properties file 
(or set these properties directly within the application)

    org.quartz.jobStore.class = org.terracotta.quartz.TerracottaJobStore
    org.quartz.jobStore.tcConfigUrl = localhost:9510

This assumes that you are running the Terracotta server on the localhost 
(which can be started using the bin/start-tc-server.[sh|bat] script). If 
not, replace localhost as appropriate. The Terracotta Job Store requires 
Terracotta 3.2.0 or greater.

