package com.lolskillz;

import ai.grakn.GraknSession;
import ai.grakn.GraknTx;
import ai.grakn.GraknTxType;
import ai.grakn.Keyspace;
import ai.grakn.concept.AttributeType;
import ai.grakn.graql.Match;
import ai.grakn.graql.admin.Answer;
import ai.grakn.remote.RemoteGrakn;
import ai.grakn.util.SimpleURI;

import java.util.List;

import static ai.grakn.graql.Graql.*;

public class Main {
    public static void main(String[] args) {
        final String GRAKN_URI = "localhost:48555";
        final String GRAKN_KEYSPACE = "grakn2";
        try (GraknSession session = RemoteGrakn.session(new SimpleURI(GRAKN_URI), Keyspace.of(GRAKN_KEYSPACE))) {
            System.out.println("defining the parent-child schema...");
            // define schema
            define(session);

            // define the parent, Johnny Sr.
            System.out.println("inserting parent...");
            String parent = "Johnny Sr.";
            insertName(session, parent);
            insertPerson(session, parent);


            // insert the child, Johnny Jr.
            System.out.println("inserting child...");
            String child = "Johnny Jr.";
            insertName(session, child);
            insertPerson(session, child);

            // create relationship
            System.out.println("creating parent-child relationship...");
            insertParentChildRelationship(session, parent, child);

            // print the created relationship
            System.out.println("print the created relationship, and perform aggregate count followed by compute count queries...");
            print(session, parent, child);
        }
    }

    private static void define(GraknSession session) {
        try (GraknTx tx = session.open(GraknTxType.WRITE)) {
            tx.graql().define(
                    label("name").sub("attribute").datatype(AttributeType.DataType.STRING),
                    label("parent").sub("role"),
                    label("child").sub("role"),
                    label("person").sub("entity").has("name").plays("parent").plays("child"),
                    label("parentchild").sub("relationship").relates("parent").relates("child")
                ).execute();
            tx.commit();
        }
    }

    private static void insertName(GraknSession session, String name) {
        // insert name with value 'name'
        try (GraknTx tx = session.open(GraknTxType.WRITE)) {
            tx.graql().insert(var().isa("name").val(name)).execute();
            tx.commit();
        }
    }

    private static void insertPerson(GraknSession session, String name) {
        try (GraknTx tx = session.open(GraknTxType.WRITE)) {
            tx.graql().insert(var().isa("person").has("name", name)).execute();
            tx.commit();
        }
    }

    private static void insertParentChildRelationship(GraknSession session, String parent, String child) {
        try (GraknTx tx = session.open(GraknTxType.WRITE)) {
            Match toBeLinked = tx.graql().match(
                    var("prnt").isa("person").has("name", parent),
                    var("chld").isa("person").has("name", child));
            toBeLinked.insert(var().isa("parentchild").rel("parent", "prnt").rel("child", "chld")).execute();
            tx.commit();
        }
    }

    private static void print(GraknSession session, String p1, String p2) {
        try (GraknTx tx = session.open(GraknTxType.WRITE)) {
            String prntId = p1;
            String chldId = p2;
            Match toBeLinked = tx.graql().match(
                    var("prnt").isa("person").has("name", prntId),
                    var("chld").isa("person").has("name", chldId),
                    var("prntchld").rel("parent", "prnt").rel("child", "chld")
            );
            List<Answer> execute = toBeLinked.get().execute();
            execute.forEach(e -> System.out.println(e.get("prnt").getId() + " name = " + prntId + " (prnt) --> (chld) " + e.get("chld").getId() + " " + chldId + " via relationship '" + e.get("prntchld")));
        }

        try (GraknTx tx = session.open(GraknTxType.WRITE)) {
            System.out.print("performing count using match - aggregate count...");
            long person = tx.graql().match(var("n").isa("person")).aggregate(count()).execute();
            long name = tx.graql().match(var("n").isa("name")).aggregate(count()).execute();
            System.out.println("person instance count = " + person + ", name instance count = " + name);
        }

        try (GraknTx tx = session.open(GraknTxType.WRITE)) {
            System.out.print("performing count using compute count...");
            long person = tx.graql().compute().count().in("person").execute();
            long name = tx.graql().compute().count().in("name").execute();
            System.out.println("person instance count = " + person + ", name instance count = " + name);
        }
    }
}
