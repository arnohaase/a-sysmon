package com.ajjpj.asysmon.server.dummy;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

/**
 * @author arno
 */
public class ElasticSearchPlay {
    public static void main(String[] args) {


        final Node node = NodeBuilder.nodeBuilder()
//                .local(true)
                .client(false)
                //TODO configure location of node files in file system
                .node();
        final Client client = node.client();

        try {
            System.out.println("------------------------------------------- 1");
            final IndexResponse ir = client.prepareIndex("idx-1", "tpe-1")
                    .setSource(createPersonJson())
                    .execute()
                    .actionGet();

            System.out.println(ir.getId());
            System.out.println(ir.getIndex());
            System.out.println(ir.getType());

            System.out.println("------------------------------------------- 2");

            final GetResponse gr = client.prepareGet("idx-1", "tpe-1", ir.getId())
                    .execute()
                    .actionGet();

            System.out.println(gr.getSource());

            System.out.println("------------------------------------------- 3");

            Thread.sleep(1000);

            final SearchResponse sr = client.prepareSearch()
                    .execute()
                    .actionGet();

            System.out.println(sr);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            node.stop();
        }
    }

    private static String SL(String s) {
        return '"' + s + '"';
    }

    private static String createPersonJson() {
        return "{" +
                SL("firstname") + ": " + SL("Arno") + "," +
                SL("lastname") + ": " + SL("Haase") +
//                ", " +
//                SL("created") + ": " + System.currentTimeMillis() +
                "}";
    }
}
