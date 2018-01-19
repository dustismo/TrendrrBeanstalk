# A Java beanstalk client

I wrote this client because we needed a pooled high performance client to interact with beanstalk.  
It uses socket channels instead of regular sockets for increased throughput (in our environment it is 10 to 20X faster then the regular socket implementation). 

There is a simple connection pool so client connections can be reused.

See **example/Example.java** for example usage.

**License is MIT**

== Please Note - This is not actively maintained.  If someone wants to maintain a fork I am happy to link to that.  


---

# How to install the requirements and build this package (Using Gradle)

## Download TrendrrBeanstalk
```
$ git clone https://github.com/dustismo/TrendrrBeanstalk.git TrendrrBeanstalk
```

Download the dependency **org.apache.commons.logging** from this [link](https://commons.apache.org/proper/commons-logging/download_logging.cgi)  (download **binary**).

Put the **jar** file of org.apache.commons.logging in yourTrendrrBeanstalk directory.

Create the glade file and add the dependency of the jar and apply Gradle's Java plugin as follows:

*build.gradle*
```
apply plugin: 'java'

dependencies {
    compile files('commons-logging-1.2.jar')
}
```

Assembling the jar

```
$ gradle jar
```

This command will create the directory build which contains the package TrendrrBeanstalk in build/libs/TrendrrBeanstalk.jar

Well, now we can compile and run the file example.java with these follow commands:

```
$ javac -cp commons-logging-1.2.jar:build/libs/TrendrrBeanstalk.jar:. example/Example.java
$ java -cp commons-logging-1.2.jar:build/libs/TrendrrBeanstalk.jar:example Example
```

Take care about the last path after : ("example"), is the directory where your file Example.class is, if you are already in the directory example/
you probably need to run:
```
$ java -cp ../commons-logging-1.2.jar:../build/libs/TrendrrBeanstalk.jar:. Example
```
