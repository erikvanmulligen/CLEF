package nl.erasmusmc.bios.clef.icd10;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Evaluate {

    static Logger logger = LogManager.getLogger();
    static CloseableHttpClient client = HttpClients.createDefault();
    static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HHmmss");

    public static void main(String[] args) throws URISyntaxException {
	Options options = new Options()
	.addOption("solr", true, "SOLR repository")
	.addOption("core", true, "SOLR core")
	.addOption("file", true, "file to import")
	.addOption("match", true, "match type: NO_SUB or LONGEST_DOMINANT_RIGHT (default)")
	.addOption("type", true, "optional file type")
	.addOption("output", true, "optional output file");

	CommandLineParser parser = new DefaultParser();

	try {
	    // parse the command line arguments
	    CommandLine line = parser.parse(options, args, false);

	    if (line.getArgList().size() > 0) {
		throw new ParseException("unknown arguments");
	    }
	    String solr = line.hasOption("solr") ? line.getOptionValue("solr") : "http://localhost:8990/solr";
	    String core = line.hasOption("core") ? line.getOptionValue("core") : null;
	    String match = line.hasOption("match") ? line.getOptionValue("match") : "LONGEST_DOMINANT_RIGHT";
	    String file = line.hasOption("file") ? line.getOptionValue("file") : null;
	    String type = line.hasOption("type") ? line.getOptionValue("type") : "dictionary";
	    String output = line.hasOption("output") ? line.getOptionValue("output") : generateFileName(file,core,match);

	    if (solr != null && core != null && file != null && type != null && match != null && output != null) {
		process(solr, core, file, match, type, output);
	    } else {
		new HelpFormatter().printHelp(Evaluate.class.getCanonicalName(), options);
	    }
	} catch (ParseException e) {
	    System.err.println("Parsing failed.  Reason: " + e.getMessage());
	    logger.error("Parsing failed.  Reason: " + e.getMessage());
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    /*
     * this method generates a new output file name based on the various parameters
     */
    private static String generateFileName(String file, String core, String match){
	return FilenameUtils.removeExtension(file) + "_" + core + "_" + match + "_" + dateFormat.format(new Date()) + ".out";
    }

    private static void process(String server, String core, String filename, String match, String type, String output) throws IOException, URISyntaxException {
	switch (type){
	case "dictionary":
	    processDictionary(server, core, filename, match, type, output);
	    break;
	case "mantra_corpus":
	    processMantraCorpus(server, core, filename.split(";"), match, type, output);
	    break;
	}
    }

    private static void processDictionary(String server, String core, String filename, String match, String type, String output) throws IOException, URISyntaxException {

	HashMap<String, String> headers = new HashMap<String, String>();
	headers.put("Content-Type", "text/plain ; charset=utf-8");

	//Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"));

	FileOutputStream fos = new FileOutputStream(FilenameUtils.removeExtension(output)+".zip");
	ZipOutputStream zos = new ZipOutputStream(fos, Charset.forName("UTF-8"));
	ZipEntry ze = new ZipEntry(output);
	zos.putNextEntry(ze);

	try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF8"));) {
	    String line = null;
	    while ( ( line = br.readLine() ) != null) {
		String[] pieces = line.split(";");
		if (pieces.length == 3){
		    try {
			String request = "http://localhost:8990/solr/" + core + "/tag?fl=uuid,icd10,term&overlaps=" + match + "&tagsLimit=5000&wt=json";
			TagResponse response = parse(getStringContent(request, pieces[2] + " AND origin:ontology", headers));
			for ( TagItem item : response.getItems() ){
			    byte[] buffer = (line + ";" + item.getTerm() + ";" + item + "\n").getBytes(); 
			    zos.write(buffer, 0, buffer.length);
			}
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		}
	    }
	}
	zos.closeEntry();
	zos.close();
    }

    private static void processMantraCorpus(String server, String core, String[] filenames, String match, String type, String output) throws IOException, URISyntaxException {

	HashMap<String, String> headers = new HashMap<String, String>();
	headers.put("Content-Type", "text/plain ; charset=utf-8");
	HashMap<String,Set<String>> frequencies = new HashMap<String,Set<String>>();

	Map<String,String> abbreviations = new HashMap<String,String>();
	for (String f : filenames){
	    abbreviations.put(f, FilenameUtils.getBaseName(f).substring(0,1));
	}

	for (int i = 0 ; i < filenames.length ; i++){
	    System.out.println("processing file "+filenames[i] + " (" + abbreviations.get(filenames[i]) + ")");
	    try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filenames[i]), "UTF8"));) {
		String line = null;
		Integer linenr = 0;
		while ( ( line = br.readLine() ) != null) {
		    linenr++;
		    try {
			String request = "http://localhost:8990/solr/" + core + "/tag?fl=uuid,icd10,term&overlaps=" + match + "&tagsLimit=5000&wt=json";
			TagResponse response = parse(getStringContent(request, line, headers));
			for ( TagItem item : response.getItems() ){
			    String key = item.getTerm();
			    if(!frequencies.containsKey(key)){
				frequencies.put(key, new HashSet<String>());
			    }
			    Set<String> lineSet = frequencies.get(key);
			    lineSet.add(abbreviations.get(filenames[i])+linenr);
			    frequencies.put(key, lineSet);
			}
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		}
		br.close();
	    }
	}

	Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"));
	for (String key : frequencies.keySet()){
	    out.write(key + "\t" + frequencies.get(key).size() + "\t" + StringUtils.join(frequencies.get(key),";") + "\n");
	}
	out.close();
    }

    public static String getStringContent(String uri, String postData, HashMap<String, String> headers) throws Exception {
	HttpPost request = new HttpPost(uri);
	request.setEntity(new StringEntity(postData,"UTF-8"));
	for(Entry<String, String> s : headers.entrySet()){
	    request.setHeader(s.getKey(), s.getValue());
	}
	HttpResponse response = client.execute(request);

	InputStream ips  = response.getEntity().getContent();
	BufferedReader buf = new BufferedReader(new InputStreamReader(ips,"UTF-8"));
	if(response.getStatusLine().getStatusCode()!=HttpStatus.SC_OK){
	    throw new Exception(response.getStatusLine().getReasonPhrase());
	}
	StringBuilder sb = new StringBuilder();
	String s;
	while (true){
	    s = buf.readLine();
	    if(s==null || s.length()==0)
		break;
	    sb.append(s);
	}
	buf.close();
	ips.close();
	return sb.toString();

    }

    public static TagResponse parse(String jsonLine) {
	Map<String,List<Position>> positions = new HashMap<String,List<Position>>();
	TagResponse result = new TagResponse();
	JsonElement jelement = new JsonParser().parse(jsonLine);
	JsonObject  jobject = jelement.getAsJsonObject();
	//JsonObject  object = jobject.getAsJsonObject("responseHeader");
	JsonArray tags = jobject.getAsJsonArray("tags");
	for ( JsonElement tagElt : tags ){
	    JsonArray tag = tagElt.getAsJsonArray();
	    Integer aStartOffset = null, aEndOffset = null;
	    for ( int i = 0 ; i < tag.size() ; i += 2){
		switch (tag.get(i).getAsString()){
		case "startOffset":
		    aStartOffset = tag.get(i+1).getAsInt();
		    break;
		case "endOffset":
		    aEndOffset = tag.get(i+1).getAsInt();
		    break;
		case "ids":
		    for ( JsonElement id : tag.get(i+1).getAsJsonArray() ){
			String uuid = id.getAsString();
			if (!positions.containsKey(uuid)){
			    positions.put(uuid,new ArrayList<Position>());
			}
			positions.get(uuid).add(new Position(aStartOffset,aEndOffset));
		    }
		    break;
		}
	    }
	}

	JsonObject response = jobject.getAsJsonObject().getAsJsonObject("response");
	for ( JsonElement doc : response.getAsJsonArray("docs") ){
	    String uuid = doc.getAsJsonObject().get("uuid").getAsString();
	    for ( Position position : positions.get(uuid)){
		TagItem item = new TagItem();
		item.setUuid(uuid);
		item.setIcd10(doc.getAsJsonObject().get("icd10").getAsString());
		item.setTerm(doc.getAsJsonObject().get("term").getAsString());
		item.setStart(position.getStart());
		item.setEnd(position.getEnd());
		result.add(item);
	    }
	}
	return result;
    }

}