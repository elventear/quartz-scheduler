==============================================================================
This file is meant to help you get started in poking around within the
Quartz project (http://www.quartz-scheduler.org).
==============================================================================


What is Quartz?
==============================================================================
Quartz is an opensource project aimed at creating a
free-for-commercial use Job Scheduler, with 'enterprise' features.

Licensed under the Apache License, Version 2.0 (the "License"); you may not 
use this file except in compliance with the License. You may obtain a copy 
of the License at 
 
    http://www.apache.org/licenses/LICENSE-2.0 

Additionally, a copy of the license and its accompanying notice file is
included with the distribution. 


Competing Products?
==============================================================================
There are no known competing open source projects (there are a few
schedulers, but they are basically Cron replacements written in Java)

Commercially, take a look at Flux, which has many excellent features: 

    http://www.fluxcorp.com/



What is in this package?
==============================================================================

quartz-all-<ver>.jar		all in one Quartz library.  Includes the core 
							Quartz components plus all optional packages.  If 
							you use this library, no other quartz-*.jars are 
							necessary.

quartz-<ver>.jar			core Quartz library.

quartz-jboss-<ver>.jar		optional JBoss specific Quartz extensions such as
							the Quartz startup MBean, QuartzService.

quartz-oracle-<ver>.jar		optional Oracle specific Quartz extensions such as
							the OracleDelegate.

quartz-weblogic-<ver>.jar	optional WebLogic specific Quartz extensions such
							as the WebLogicDelegate.

build.xml             		an "ANT" build file, for building Quartz.

readme.txt            		this file (duh!).

license.txt           		a document declaring the license under which
                      		Quartz can be used and distributed.

src-header.txt				piece of text to put at the top of each new file

docs					    the root directory of all documentation.

docs/wikidocs               the main documentation for Quartz.  Start with
                            the "index.html"
                            
docs/dbTables				sql scripts for creating Quartz database tables in
							a variety of different databases.                            
                            
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

lib                   		a directory which should contain all of the
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
consider this your "reference manual".  There is a tutorial and configuration
reference that can be found from docs/wikidocs/index.html .

Start by looking at org.quartz.Scheduler, org.quartz.Job,
org.quartz.JobDetail and org.quartz.Trigger.

Examine and run the examples found in the "examples" directory.

If you're interested in the "behind the scenes" (server-side) code,
you'll want to look at org.quartz.core.QuartzSchedulerThread, which
will make you interested in org.quartz.spi.JobStore.java,
org.quartz.spi.ThreadPool.java and org.quartz.core.JobRunShell.

Have fun.



What do I do when I find something stupid?
==============================================================================

Help is available via the Quarts-Users forum:

  http://forums.opensymphony.com/forum.jspa?forumID=6

Please report bugs / issues to JIRA at:

  http://jira.opensymphony.com/browse/QUARTZ  


HOW TO BUILD QUARTZ
==============================================================================
Quartz is built using the Maven project management tool.  If you don't already
have Maven installed, download from the Apache website (http://maven.apache.org)
and follow installation instructions.  You can confirm the version of Maven you 
have installed by typing: mvn --version

To build simply execute "mvn install" from the top level Quartz project directory.
This command will build the Quartz JAR files and install them into your local
repository.  Along the way it will also execute all of the Quartz unit and
integration tests.  You can disable tests by passing -Dmaven.test.skip=true
on the mvn command-line.

By default, only the main "quartz" module and the "quartz-jboss" module JARs are
built.  The quartz-oracle and quartz-weblogic depend on properietary Oracle and
WebLogic JARs that are not available in any public Maven repositories, and
therefore these modules have been excluded from the default build.  To build
these modules you will need to obtain the following JAR files and install them
into your local Maven repository:

    com.oracle:ojdbc5:jar:11.2.0
    com.bea.core:datasource:jar:1.6.0

Once you have these artifacts installed into your local repository you can
enable the quartz-oracle and quartz-weblogic modules by activating the Maven
profiles "oracle" and "weblogic" with the -P mvn command-line option. For example:

    mvn -Pweblogic install

Note that the quartz-weblogic module depends on the quartz-oracle module, and
therefore the weblogic Maven profile implies the oracle profile.

To create the downloadable distribution, invoke the package phase and the
assembly:assembly plugin goal with prepare-distribution profile enabled, i.e.:

    mvn -Pprepare-distribution package assembly:assembly

To include the quartz-oracle and quartz-weblogic artifacts in the distribution,
make sure to enable the respective profiles when assembling the package, i.e.:

    mvn -Pprepare-distribution -Poracle,weblogic package assembly:assembly

