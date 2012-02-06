1. To compile:
  %> mvn install -DskipTests
  
Note:  
  a. the final Quartz jar is found under packaging/target  
  b. if you don't have Oracle and/or Weblogic third party libraries jar required for compilation (these can't be found
     on public Maven repos due to licensing) you can comment out those modules under the main pom.xml  

2. To build Quartz distribution kit:

  %> cd packaging
  %> mvn package -P distribution

  
