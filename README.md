# jTimetable

jTimetable wants to be a small lightweight Timetable Generator for small trainingcenters or inhouse Trainings. It will Track Classes, Rooms and Trainingstaff, all from within one JAR File.

# Development

We try our best to use standard Maven and Java 1.8. We found in our development, that we have to define a Proxy for Maven to load the required Pakets, the put the Maven Repository out of your Windows Profiles and to define a toolchain to our JDK. 

settings.xml in C:\Users\USERNAME\.m2 / ~/.m2
```
<settings>
<proxies>
      <proxy>
        <id>proxy http</id>
        <active>true</active>
        <protocol>http</protocol>
        <host>PROXYIP</host>
        <port>PROXYPORT</port>
      </proxy>
	  <proxy>
        <id>proxy https</id>
        <active>true</active>
        <protocol>https</protocol>
        <host>PROXYIP</host>
        <port>PROXYPORT</port>
      </proxy>
  </proxies>
  <localRepository>PATH/TO/WHERE/PAKETS/SHOULD/BE/SAVED</localRepository>
</settings>
```

toolchains.xml in C:\Users\USERNAME\.m2 / ~/.m2
```
<toolchains xmlns="http://maven.apache.org/TOOLCHAINS/1.1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 xsi:schemaLocation="http://maven.apache.org/TOOLCHAINS/1.1.0 http://maven.apache.org/xsd/toolchains-1.1.0.xsd">
 <!-- JDK toolchains -->
 <toolchain>
   <type>jdk</type>
   <provides>
     <version>8</version>
     <vendor>sun</vendor>
   </provides>
   <configuration>
     <jdkHome>/PATH/TO/JDK/bin</jdkHome>
   </configuration>
 </toolchain>
</toolchains>
```