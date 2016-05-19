package nl.erasmusmc.bios.clef.icd10;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

import nl.erasmusmc.bios.clef.utils.Permutations;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;

public class Import {

    static Logger logger = LogManager.getLogger();

    public static void main(String[] args) {

	Options options = new Options().addOption("solr", true, "SOLR repository").addOption("file", true, "file to import").addOption("core", true, "core").addOption("permutations", false, "permutations");
	CommandLineParser parser = new DefaultParser();

	try {
	    // parse the command line arguments
	    CommandLine line = parser.parse(options, args, false);

	    if (line.getArgList().size() > 0) {
		throw new ParseException("unknown arguments");
	    }
	    String solr = line.hasOption("solr") ? line.getOptionValue("solr") : "http://localhost:8990/solr";
	    String file = line.hasOption("file") ? line.getOptionValue("file") : null;
	    String core = line.hasOption("core") ? line.getOptionValue("core") : "filteredDictCorpus";
	    Boolean permutations = line.hasOption("permutations") ? Boolean.parseBoolean(line.getOptionValue("permutations")) : false;

	    if (solr != null && file != null && core != null) {
		processDictionary(solr, core, file, permutations);
	    } else {
		new HelpFormatter().printHelp(Process.class.getCanonicalName(), options);
	    }
	} catch (ParseException e) {
	    System.err.println("Parsing failed.  Reason: " + e.getMessage());
	    logger.error("Parsing failed.  Reason: " + e.getMessage());
	} catch (SolrServerException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private static void processDictionary(String server, String core, String filename, Boolean permutations) throws SolrServerException, IOException {
	SolrClient solr = new HttpSolrClient(server + "/" + core);
	solr.deleteByQuery("*:*");
	solr.commit();
	try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF8"));) {
	    String line = null;
	    int cnt = 0;
	    while ( ( line = br.readLine() ) != null) {
		String[] pieces = line.split("\\|");
		if (pieces.length == 3){
		    String[] terms = pieces[1].split(";");
		    for (String original_term : terms){
			cnt++;
			if ( cnt % 100 == 0 ){
			    System.out.println(cnt);
			    System.out.flush();
			}
			if (permutations){
        			for (String term : Permutations.get(original_term)){
        			    SolrInputDocument doc = new SolrInputDocument();
        			    doc.addField("uuid", UUID.randomUUID().toString());
        			    doc.addField("icd10", pieces[0]);
        			    doc.addField("term", term);
        			    doc.addField("origin", "dictionary");
        			    doc.addField("prefterm", terms[0]);
        			    solr.add(doc);
        			}
			}
			else{
			    SolrInputDocument doc = new SolrInputDocument();
			    doc.addField("uuid", UUID.randomUUID().toString());
			    doc.addField("icd10", pieces[0]);
			    doc.addField("term", original_term);
			    doc.addField("origin", "dictionary");
			    doc.addField("prefterm", terms[0]);
			    solr.add(doc);
			}
			
		    }
		}
	    }
	    br.close();
	}
	solr.commit();
	solr.close();
    }
}
