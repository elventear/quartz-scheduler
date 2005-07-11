
==============================================================================
This file is meant to help you get started in poking around within the
Quartz project (http://www.opensymphony.com/quartz).
==============================================================================


What is Quartz?
==============================================================================
Quartz is an opensource project aimed at creating a
free-for-commercial use Job Scheduler, with 'enterprise' features.

Licensed under the Apache License, Version 2.0 (the "License"); you may not 
use this file except in compliance with the License. You may obtain a copy 
of the License at 
 
    http://www.apache.org/licenses/LICENSE-2.0 

Also, to keep the legal people happy:

    This product includes software developed by the
    Apache Software Foundation (http://www.apache.org/)


Competing Products?
==============================================================================
There are no known competing open source projects (there are a few
schedulers, but they are basically Cron replacements written in Java)

Commercially, take a look at Flux, which has many excellent features: 

    http://www.fluxcorp.com/



What is in this package?
==============================================================================

build.xml             		an "ANT" build file, for building Quartz.

readme.txt            		this file (duh!).

license.txt           		a document declaring the license under which
                      		Quartz can be used and distributed.

src/java/org/quartz   		the main package of the Quartz project,
                      		containing the 'public' (client-side) API for
                      		the scheduler

src/java/org/quartz/core   	a package containing the 'private' (server-side)
                      		components of Quartz.

src/java/org/quartz/simpl	a package contain simple implementations of
                      		Quartz support modules (JobStores, ThreadPools,
                      		Loggers, etc.) that have no dependencies on
                      		external (third-party) products.

src/java/org/quartz/impl 	a package containing implementations of Quartz
                      		support modules (JobStores, ThreadPools,
                      		Loggers, etc.) that may have dependencies on
                      		external (third-party) products - but may be
                      		more robust.

src/java/org/quartz/utils	a package containing some utility/helper
                      		components used through-out the main Quartz
                      		components.


src/examples/org/quartz		a directory containing some code samples on the
examples               		usage of Quartz.  The first example you should
                      		look at is 'example1.bat' or 'example1.sh' -
                      		depending if you're a win-dos or unix person.
                      		This example uses the code found in the
                      		SchedTest.java class, which is also in the
                      		examples directory.

webapp						a directory containing a simple web-app for managing
							Quartz schedulers.

lib                   		a directory containing a build of Quartz
                      		(quartz.jar) and which should contain all of the
                      		third-party libraries that are needed in order
                      		to use all of the features of Quartz. (Some are
                      		not automatically there, but you need to get them
                      		and put them there if you use the features they
                      		depend on -- see below)




Where should I start looking in order to figure this thing out?
==============================================================================

There is a tutorial distributed with Quartz that can be found in the "docs" 
directory.  You should also read the FAQ in the docs/wikidocs/ directory.

Most of the Java source files are fairly well documented with JavaDOC -
consider this your "manual".  

Start by looking at org.quartz.Scheduler.java, org.quartz.Job.java,
org.quartz.JobDetail.java and org.quartz.Trigger.java

Examine and run the examples found in the "examples" directory.

If you're interested in the "behind the scenes" (server-side) code,
you'll want to look at org.quartz.core.QuartzSchedulerThread, which
will make you interested in org.quartz.core.JobStore.java,
org.quartz.core.ThreadPool.java and org.quartz.core.JobRunShell.

Have fun.



What do I do when I find something stupid?
==============================================================================

Help is available via the Quarts-Users forum:

  http://forums.opensymphony.com/forum.jspa?forumID=6

Please report bugs / issues to JIRA at:

  http://jira.opensymphony.com/browse/QUARTZ  


HOW TO BUILD / RUN QUARTZ
==============================================================================
The current build process assumes you already have Ant installed.  If
you don't, downlaod from the Apache website and follow installation
instructions.


To build:

1) There should be a build.xml file located in the project root
directory.

2) You should be able to type: ant if you have it setup right. It will
search for build.xml in the current directory.

3) Available targets can be seen by typing: ant usage.

4) To use the default build target, just type "ant" on the command line while
sitting in the main 'quartz' directory.

