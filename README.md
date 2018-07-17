# Running
```
$ mvn package
$ cd target
$ tar -xf grakn-java-client-example-1.0-SNAPSHOT.tar.gz
$ ./grakn-java-client-example

defining the parent-child schema...
inserting parent...
inserting child...
creating parent-child relationship...
print the created relationship, and perform aggregate count followed by compute count queries...
V12368 name = Johnny Sr. (prnt) --> (chld) V16592 Johnny Jr. via relationship 'RemoteRelationship{tx=ai.grakn.remote.RemoteGraknTx@1c7696c6, getId=V16536}
performing count using match - aggregate count...person instance count = 2, name instance count = 2
performing count using compute count...person instance count = 2, name instance count = 2
```



