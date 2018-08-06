package com.lolski;

import ai.grakn.GraknTx;
import ai.grakn.GraknTxType;
import ai.grakn.Keyspace;
import ai.grakn.client.Grakn;
import ai.grakn.concept.AttributeType;
import ai.grakn.graql.Match;
import ai.grakn.graql.answer.ConceptMap;
import ai.grakn.util.GraqlSyntax;
import ai.grakn.util.SimpleURI;

import java.util.List;

import static ai.grakn.graql.Graql.*;

public class Main {
    public static void main(String[] args) {
        final String GRAKN_URI = "localhost:48555";
        final String GRAKN_KEYSPACE = "grakn14";
        try (Grakn.Session session = Grakn.session(new SimpleURI(GRAKN_URI), Keyspace.of(GRAKN_KEYSPACE))) {
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
        }
    }
}
