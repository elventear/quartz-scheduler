1. To compile:
  %> mvn install -DskipTests
  
Note:  the final Quartz jar is found under quartz/target 

2. To build Quartz distribution kit:

  %> cd distribution
  %> mvn package

3. To deploy Maven central repo (via Sonatype)

  %> mvn clean deploy -P sign-artifacts,deploy-sonatype -DskipTests
