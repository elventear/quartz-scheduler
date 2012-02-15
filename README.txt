1. To compile:
  %> mvn install -DskipTests
  
Note:  
  a. the final Quartz jar is found under quartz/target 
  
  b. if you don't have Oracle and/or Weblogic third party libraries jars required for compilation (these can't be found
     on public Maven repos due to licensing) you can comment out those modules under the main pom.xml or download and install
     the needed artifacts.
     
  For Oracle: ojdbc5.jar can be found at http://www.oracle.com/technetwork/database/enterprise-edition/jdbc-112010-090769.html
  For Weblogic: com.bea.core.datasource_1.6.0.0.jar can be found under "modules" of a Weblogic installation
  
  Once you have the jars, install them to Maven local repo:
  
  %> mvn install:install-file -DgroupId=com.oracle -DartifactId=ojdbc5 \
         -Dversion=11.2.0.1.0 -Dpackaging=jar -Dfile=ojdbc5.jar
         
  %> mvn install:install-file -DgroupId=com.bea.core -DartifactId=datasource \
         -Dversion=1.6.0 -Dpackaging=jar -Dfile=com.bea.core.datasource_1.6.0.0.jar
         
         
2. To build Quartz distribution kit:

  %> cd distribution
  %> mvn package
