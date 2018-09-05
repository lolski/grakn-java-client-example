package com.lolski;

import ai.grakn.GraknTx;
import ai.grakn.GraknTxType;
import ai.grakn.Keyspace;
import ai.grakn.client.Grakn;
import ai.grakn.concept.AttributeType;
import ai.grakn.graql.Match;
import ai.grakn.graql.answer.ConceptMap;
import ai.grakn.graql.answer.Value;
import ai.grakn.util.GraqlSyntax;
import ai.grakn.util.SimpleURI;

import java.util.List;

import static ai.grakn.graql.Graql.*;

public class Main {
    public static void main(String[] args) {
        final String GRAKN_URI = "localhost:48555";
        final String GRAKN_KEYSPACE = "grakn";
        try (Grakn.Session session = new Grakn(new SimpleURI(GRAKN_URI)).session(Keyspace.of(GRAKN_KEYSPACE))) {
            System.out.println("defining the parent-child schema...");
            // define schema
            try (GraknTx tx = session.transaction(GraknTxType.WRITE)) {
                tx.graql().define(
                        label("name").sub("attribute").datatype(AttributeType.DataType.STRING),
                        label("person").sub("entity").has("name").plays("parent").plays("child"),
                        label("parentchild").sub("relationship").relates("parent").relates("child")
                ).execute();
                tx.commit();
            }

            // define the parent, Johnny Sr.
            System.out.println("inserting parent & child...");
            try (GraknTx tx = session.transaction(GraknTxType.WRITE)) {
                tx.graql().insert(var().isa("person").has("name").val("Johnny Jr.")).execute();
                tx.graql().insert(var().isa("person").has("name").val("Johnny Sr.")).execute();
                tx.graql().match(
                        var("prnt").isa("person").has("name", "Johnny Sr."),
                        var("chld").isa("person").has("name", "Johnny Jr."))
                    .insert(var().isa("parentchild").rel("parent", "prnt").rel("child", "chld"))
                    .execute();
                tx.commit();
            }

            // print the created relationship
            System.out.println("print the created relationship, and perform aggregate count followed by compute count queries...");
            try (GraknTx tx = session.transaction(GraknTxType.READ)) {
                List<ConceptMap> results = tx.graql().match(
                        var("prnt").isa("person").has("name", "Johnny Sr."),
                        var("chld").isa("person").has("name", "Johnny Jr."),
                        var("prntchld").rel("parent", "prnt").rel("child", "chld"))
                    .get().execute();
                for (ConceptMap result: results) {
                    System.out.println(result.get("prnt").id() + " name = \"Johnny Sr.\" (prnt) --> (chld) " +
                            result.get("chld").id() + " name = \"Johnny Jr.\" via relationship " + result.get("prntchld").id());
                }

            }

            // perform counting using the aggregate query
            try (GraknTx tx = session.transaction(GraknTxType.WRITE)) {
                System.out.print("performing count using match - aggregate count...");
                final List<Value> countPersonResult = tx.graql().match(var("n").isa("person")).aggregate(count()).execute();
                long person = countPersonResult.get(0).number().longValue();
                final List<Value> countNameResult = tx.graql().match(var("n").isa("name")).aggregate(count()).execute();
                long name = countNameResult.get(0).number().longValue();
                System.out.println("person instance count = " + person + ", name instance count = " + name);
            }

            // perform counting using the compute query (see the doc on analytics for when to use compute count vs aggregate count)
            try (GraknTx tx = session.transaction(GraknTxType.WRITE)) {
                System.out.print("performing count using compute count...");
                final List<Value> countPersonResult = tx.graql().compute(GraqlSyntax.Compute.Method.COUNT).in("person").execute();
                long person = countPersonResult.get(0).number().longValue();
                final List<Value> countNameResult = tx.graql().compute(GraqlSyntax.Compute.Method.COUNT).in("name").execute();
                long name = countNameResult.get(0).number().longValue();
                System.out.println("person instance count = " + person + ", name instance count = " + name);
            }
        }
    }
}
