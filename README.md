# COF-assignment

This is an spring boot application that fetches all user transactions from an endpoint and displays average income and expenditure.  To build this application you will require java, maven.  Eclipse IDE is optional.  After successful build, you will notice an artifact in target folder with a name transaction-bot-1.0.jar.

Pre-requisites:
1.  You will need a Java Runtime Environment. Please refer to https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html for installation and setup of your Java environment.

2.  This application requires maven for building.  Please refer to maven installation instructions here:
http://maven.apache.org/install.html.  

How to build:

1.  After downloading the sourcecode, create a maven project in Eclipse IDE and import the downloaded code using pom.xml.  
2.  In the application.properties, as a good practice I have not included authToken, apiToken values.  Please include them.
    Please ask James or reachout to me regarding these token values.
3.  Right-click on pom.xml file and choose "Run As" and select "Maven install".  After,
    or alternatively use command line and type maven clean install.

After successful build, you will notice a target folder under application folder with transaction-bot-1.0.jar.  If you are receiving errors, please let me know.

How to run:
1.  For alltransactions use the following command:  java -jar transaction-bot-1.0.jar

2.  For feature#1:  alltransactions ignoring donut amounts, use: java -jar transaction-bot-1.0.jar --ignore-donuts
