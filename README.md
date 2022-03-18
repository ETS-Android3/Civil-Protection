<!-- Header -->
<div id="top"/>

<h1 align="center"> Civil Protection Platform based on IoT applications </h1>

<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li><a href="#about-the-project">About The Project</a></li>
    <li><a href="#built-with">Built With</a></li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#how-to-run">How to run</a></li>
      </ul>
    </li>
    <li>
      <a href="#project-demonstration">Project Demonstration</a>
      <ul>
        <li><a href="#android-client-application">Android client application</a></li>
        <li><a href="#iot-sensor-application">IoT sensor application</a></li>
        <li><a href="#server-application">Server Application</a></li>
        <li><a href="#database-log">Database Log</a></li>
      </ul>
    </li>
    <li><a href="#contribution">Contribution</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
  </ol>
</details>

<!-- Body -->

## About the project:
  This project consists of 3 applications that cooperate with each other in order to detect danger events and warn the civilians about natural disasters and more. The backbone of this platform is the [Edge Server](https://github.com/john-fotis/Civil-Protection/tree/master/EdgeServer). The other two applications are the [Client Application](https://github.com/john-fotis/Civil-Protection/tree/master/PhoneApp) and the [Sensors Application](https://github.com/john-fotis/Civil-Protection/tree/master/SensorApp). The server is responsible for gathering all the environment information from active sensors and process it rapidly in order to detect possible dangers. All known danger events must logged to a database and be reported to the clients in time.
  
## Built with:
  
  * Edge Server
    * [Java Oracle SE 8](https://www.oracle.com/java/technologies/javase/javase8u211-later-archive-downloads.html)
    * [Gradle 7.0](https://docs.gradle.org/current/userguide/userguide.html)
    * [Spring Boot](https://spring.io/projects/spring-boot)
    * [IntelliJ IDEA](https://www.jetbrains.com/idea/)

  * Android Applications:
    * [Android API level 29](https://developer.android.com/about/versions/10/highlights)
    * [Android Studio](https://developer.android.com/studio)

  * Database:
    * [MySQL Server](https://dev.mysql.com/downloads/installer/)
    * [MySQL Workbench](https://dev.mysql.com/downloads/installer/)
    * [MySQL ConnectorJ](https://dev.mysql.com/downloads/installer/)

  * Other technologies:
    * [MQTT Mosquitto Broker](https://mosquitto.org/download/)
    * [Eclipse Paho Java Client](https://www.eclipse.org/paho/index.php?page=clients/java/index.php)

## Getting Started:
  There are 2 ways you can try the android applications out. You can either download our [APK files](https://github.com/john-fotis/Civil-Protection/tree/master/APKs) or download the source code and proceed with a clean installation.

  ## Prerequisites:

  For the server you must create an SQL connection to the server with username set as ```root``` and password set as ```password```. Then you need to create a database scheme with the name ```cp_registry``` in this connection instance. __These credentials are purely for demonstration purposes and can be changed through [application.properties](https://github.com/john-fotis/Civil-Protection/blob/master/EdgeServer/src/main/resources/application.properties) file. It is not recommended you store you Database credentials in public and non encrypted files.__ You also need to have an instance of Mosquitto Broker or any other MQTT broker running on you machine on port ```1883```. Finally Gradle is necessary to build the project.

  ## How to run:
  * Server: In the project root folder open a cmd and type type ``gradle build``. Then to start the server do one of the following:
    * Type ```java -jar ./build/libs/server-1.0.0.jar```
    * Type ```gradle bootRun```
  Alternatively you can build and run the entire project with IntelliJ. After the server has started, the frontend API will be available at <http://localhost:8080/map>
  * Android applications: Do one of the following:
    * Install these [APKs](https://github.com/john-fotis/Civil-Protection/tree/master/APKs) in your device or emulator (Need permission for installation from 3rd party applications)
    * Build and run the applications in Android Studio

  ## Project Demonstration:

  Here you can see the main features of the different applications.

  ## Android client application:

  * Main windows

  ![AndroidApp](https://github.com/john-fotis/Civil-Protection/blob/master/Assets/AndroidApp.png)
  <br/>

  * Danger alert scenarios

  ![AndroidAlerts](https://github.com/john-fotis/Civil-Protection/blob/master/Assets/AndroidClientAlertTypes.png)
  <br/>

  ## IoT sensor application:

  * Main windows

  ![IoTApp](https://github.com/john-fotis/Civil-Protection/blob/master/Assets/IoTApp.png)

  ## Server Application:

  * Server frontend, normal operation
  <img src="https://github.com/john-fotis/Civil-Protection/blob/master/Assets/MapNoEvents.png" width="100%" height="auto"/>
  <br/><br/>

  * Server frontend, danger event detection
  <img src="https://github.com/john-fotis/Civil-Protection/blob/master/Assets/MapEvent.png" width="100%" height="auto"/>
  <br/><br/>

  * Server frontend, double danger event detection
  <img src="https://github.com/john-fotis/Civil-Protection/blob/master/Assets/MapDoubleEvent.png" width="100%" height="auto"/>

  ## Database Log:

  * Sample of logged events

  ![DatabaseLog](https://github.com/john-fotis/Civil-Protection/blob/master/Assets/DatabaseLog.png)

<!-- Footer -->
## Contribution:
[katerinagiann](https://github.com/katerinagiann)

## License:
This project is licensed under the [MIT License](https://github.com/john-fotis/Civil-Protection/blob/master/LICENSE.md)

## Contact:
:email: <giannisfotis@gmail.com>
