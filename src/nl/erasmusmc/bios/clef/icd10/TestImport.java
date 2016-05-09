package nl.erasmusmc.bios.clef.icd10;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;

public class TestImport {
    
    static Logger logger = LogManager.getLogger();
    
    public static void main(String[] args) throws SolrServerException, IOException {
	
	SolrClient solr = new HttpSolrClient("http://localhost:8990/solr/spel/");
	solr.deleteByQuery("*:*");
	solr.commit();
	SolrInputDocument doc1 = new SolrInputDocument();
	doc1.addField("term", "apple");
	solr.add(doc1);
	SolrInputDocument doc2 = new SolrInputDocument();
	doc1.addField("term", "pear");
	solr.add(doc2);
	solr.commit();
	solr.close();
    }

}
