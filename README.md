# Grakn Java Client Example

This application shows how to work with Grakn using the [Java Client](http://dev.grakn.ai/docs/get-started/setup-guide). It will demonstrate the following:
 1. how to [define a schema](http://dev.grakn.ai/docs/building-schema/defining-schema).
 2. how to [insert](http://dev.grakn.ai/docs/querying-data/insert-queries) entities, attributes, and relationships.
 3. performing the [get query](http://dev.grakn.ai/docs/querying-data/get-queries)
 4. perform [aggregate](http://dev.grakn.ai/docs/querying-data/aggregate-queries) count query on the data.
 5. perform [compute](http://dev.grakn.ai/docs/distributed-analytics/overview) count query on the data.

## Running
NOTE: make sure you have the Grakn database up and running. If not, refer to the [Setup Guide](http://dev.grakn.ai/docs/get-started/setup-guide).
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



