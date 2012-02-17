1. To compile:
  %> mvn install -DskipTests
  
Note:  the final Quartz jar is found under quartz/target 

2. Compiling quartz-oracle and quartz-weblogic:

    If you don't have Oracle and/or Weblogic third party libraries jars required for compilation (these can't be found
    on public Maven repos due to licensing) you have 2 options:
    
  a. run Maven with -Dno-oracle-weblogic : this will exclude those 2 modules
  
  b. download and install needed artifacts
     
  For Oracle: ojdbc5.jar can be found at http://www.oracle.com/technetwork/database/enterprise-edition/jdbc-112010-090769.html
  For Weblogic: com.bea.core.datasource_1.6.0.0.jar can be found under "modules" of a Weblogic installation
  
  Once you have the jars, install them to Maven local repo:
  
  %> mvn install:install-file -DgroupId=com.oracle -DartifactId=ojdbc5 \
         -Dversion=11.2.0.1.0 -Dpackaging=jar -Dfile=ojdbc5.jar
         
  %> mvn install:install-file -DgroupId=com.bea.core -DartifactId=datasource \
         -Dversion=1.6.0 -Dpackaging=jar -Dfile=com.bea.core.datasource_1.6.0.0.jar
         
         
3. To build Quartz distribution kit:

  %> cd distribution
  %> mvn package
