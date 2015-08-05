package pnnl.goss.rdf.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import pnnl.goss.rdf.EscaType;
import pnnl.goss.rdf.InvalidArgumentException;
import pnnl.goss.rdf.TopologicalBranch;
import pnnl.goss.rdf.TopologicalNode;
import pnnl.goss.rdf.server.EscaVocab;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;

public class MatchMrids {
	
	/**
	 * The list of mrids from the export_cim.xml file.
	 */
	private Set<String> originalMrids = new HashSet<>();
	
	/**
	 * The set of mrids that were found in top.
	 */
	private Set<String> mridsFromTop = new HashSet<>();
	
	private Map<String, List<Element>> busElements = new HashMap<>(); 
	
	
	public class Element{
		public String type;
		public String value;
		
		public Element(String type, String value){
			this.type=type;
			this.value=value;
		}
		
		public String toString(){
			return "\""+ this.type + "\"=\""+this.value+"\"";
		}
	}
	
	private Map<String, CSVRecord> createMap(int keyField, List<CSVRecord> records){
		Map<String, CSVRecord> map = new ConcurrentHashMap<>();
		
		for(CSVRecord r: records){
			map.put(r.get(keyField), r);
		}
		
		return map;
	}
	
	private List<CSVRecord> getRecords(String filename, int skipLines){
		List<CSVRecord> recs = null;
		try (FileInputStream  idmapfile = new FileInputStream(new File(filename)))
		{
			try(BufferedReader  rdr = new BufferedReader(new InputStreamReader(idmapfile))){
				for(int i=0; i<skipLines; i++){ 
					rdr.readLine();				
				}
				
				String message = org.apache.commons.io.IOUtils.toString(rdr);
				CSVParser parser = CSVParser.parse(message, CSVFormat.EXCEL);
				recs = parser.getRecords();
			}				
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return recs;
	}
	
	private String getBusString(int num){
		return String.format("bus-%d", num);
	}

	private void loadBusesFromCsv(){
		List<CSVRecord> idmaprecords = getRecords("C:/temp/cim_state_variable_test/Viper_ws_e-terrasource_netmom.netmom.idmap", 4);
		List<CSVRecord> auxrecords = getRecords("C:/temp/cim_state_variable_test/hdbexport/aux_mark.csv", 4);
		List<CSVRecord> capacitorrecords = getRecords("C:/temp/cim_state_variable_test/hdbexport/capacitor_mark.csv", 4);
		List<CSVRecord> kvrecords = getRecords("C:/temp/cim_state_variable_test/hdbexport/kv_mark.csv", 6);
		List<CSVRecord> noderecords = getRecords("C:/temp/cim_state_variable_test/hdbexport/node_mark.csv", 5);
		List<CSVRecord> stationrecords = getRecords("C:/temp/cim_state_variable_test/hdbexport/station_mark.csv", 3);
		
		for (CSVRecord rec: kvrecords){
			String bus = getBusString(Integer.parseInt(rec.get(3)));
			
			if (!busElements.containsKey(bus)){
				busElements.put(bus, new ArrayList<Element>());
			}
			
			busElements.get(bus).add(new Element("name", bus));
			String value = rec.get(3);
			System.out.println(value);
		}
		
		Map<String, CSVRecord> mridmap = createMap(3, idmaprecords);
		Map<String, CSVRecord> idmap = createMap(5, idmaprecords);
		Map<String, CSVRecord> auxmap = createMap(2, auxrecords);
		Map<String, CSVRecord> capacitormap = createMap(2, capacitorrecords);
		Map<String, CSVRecord> kvmap = createMap(2, kvrecords);
		Map<String, CSVRecord> nodemap = createMap(2, noderecords);
		Map<String, CSVRecord> stationmap = createMap(2, stationrecords);
		
		
		
		// Nodemap contains the number number and type of elements  (line/transformers) 
		// within the topological node. 
		Map<String, Set> nodeMap = new ConcurrentHashMap<String, Set>();
		boolean containsAll = true;
		for(Entry<String, CSVRecord> rec: mridmap.entrySet()){
			
			String lookupMrid = "_"+rec.getKey();
			//System.out.println("Looking up mrid: "+ lookupMrid);
			if (!originalMrids.contains(lookupMrid)){
				containsAll = false;
			}
			//EscaType term = escaTypes.get(lookupMrid);
			
			//System.out.println(term);
			
		}
		
		if (containsAll){
			System.out.println("All idmap mrids were found.");
		}
		else{
			System.out.println("Some idmap mrids were not found.");
		}
		
		
		
//		for(String k: idmap.keySet()){
//			String[] fields = k.split("\\.");
//			System.out.println("key: "+k);
//			String mapKey = fields[0]+fields[1]+fields[2];
//			String mapValue = fields[3]+"."+fields[4];
//			if (fields.length > 5){
//				mapValue += "."+fields[5];
//			}
//			
//			if (!nodeMap.containsKey(mapKey)){
//				nodeMap.put(mapKey, new HashSet<String>());
//			}
//			
//			nodeMap.get(mapKey).add(mapValue);
//		}
//		
//		for(CSVRecord rec: idmap.values()){		
//			EscaType term = escaTypes.get("_"+rec.get(3));
//			if (term != null){
//			//if (term.getDataType().equals(EscaVocab.TERMINAL_OBJECT.getLocalName())){
//				System.out.println(String.format("%s, %s", term.getMrid(), term.getDataType()));
//			}
//		}
//		
//		for(TopologicalNode node: network.getTopologicalNodes()){
//			System.out.println(node+" is connected to branches");
//			for(TopologicalBranch br: ((TopologicalNodeImpl)node).getBranches()){
//				System.out.println("\t"+br);
//			}
//		}
		
	}
	
	private void attemptToFindMrids(Set<Resource> subjects){
		
		for (Resource res: subjects){
			StmtIterator itr = res.listProperties();
			while(itr.hasNext()){
				Statement st = itr.next();
				
				System.out.println("Pred: " + st.getPredicate().getLocalName());
				
				if (st.getObject().isResource() && !st.getPredicate().getLocalName().equalsIgnoreCase("type")){
					System.out.println("Object is resource: "+ st.getObject().asResource().getLocalName());
				}
				
				
			}
		}
		
	}
	
	public void writeData(){
		for(Entry<String, List<Element>> element: busElements.entrySet()){
			System.out.println(element.getKey()+ " "+element.getValue());
		}
	}
	
	/**
	 * Loads data from export_cim and 
	 * @throws InvalidArgumentException
	 */
	public void loadData() throws InvalidArgumentException {

			// Load the exported cim file into memory and then loop over the subjects and 
			// add there mrids to the originalMrids variable.
			Model rdfModel = EscaVocab.readModel(new File("C:/temp/cim_state_variable_test/export_cim.xml"));
			
			StmtIterator stmtIter = rdfModel.listStatements();
			while (stmtIter.hasNext()) {
				Statement st = stmtIter.next();
				
//				System.out.println(String.format("Subject: %s, Predicate: %s, Object: %s",
//						st.getSubject().getLocalName(), st.getPredicate().getLocalName(), st.getObject().toString()));
//				System.out.println(st.getPredicate().getLocalName());
				if (st.getSubject().getLocalName().equalsIgnoreCase("Substation")){
					System.out.println("Substation: "+ st.getSubject().getLocalName());
					StmtIterator stmtItr2 = st.getSubject().listProperties();
					while (stmtItr2.hasNext()){
						Statement stmt2 = stmtItr2.next();
						System.out.println(stmt2.getPredicate().getLocalName());
					}
				}
				if (st.getPredicate().getLocalName().equalsIgnoreCase("IdentifiedObject.name")){
					System.out.println("Object is: "+ st.getObject().toString());
				}
				
				if (st.getPredicate().getLocalName().equalsIgnoreCase("IdentifiedObject.alias")){
					System.out.println("Object is: "+ st.getObject().toString());
				}
				if (st.getPredicate().getLocalName().equalsIgnoreCase("type") && 
						st.getSubject().getLocalName().equalsIgnoreCase("Substation")){
					System.out.println("Substation: "+ st.getSubject().getLocalName());
					StmtIterator stmtItr2 = st.getSubject().listProperties();
					while (stmtItr2.hasNext()){
						Statement stmt2 = stmtItr2.next();
						System.out.println(stmt2.getPredicate().getLocalName());
					}
				}
				originalMrids.add(st.getSubject().getLocalName());
				if (st.getPredicate().getLocalName().contains("pathName")){
					System.out.println("Path name: "+st.getPredicate().getLocalName() + " -> "+st.getObject().toString());
				}
			}
			
			// Contains an overal list of the string version of the mrids.
			Set<String> mridSet = new HashSet<>();
			
			// Contains an overall list of mrids that are found in the top file or after mapping from the different
			// csv files to get the mrid.  If the value is null, then there wasn't a need to lookup the rdf:id from
			// alternate means.
			Map<Resource, String> mridMap = new HashMap<>();
			
			// This contains the entries that need to be looked up using the csv files and the netmon.id map in order
			// to find the mird.
			Set<Resource> todoSubjects = new HashSet<>();
			
			Model aModel = ModelFactory.createDefaultModel();
			FileManager.get().readModel(aModel, 
					"C:/temp/cim_state_variable_test/mark_export_stnet_dts_20150518_170619_top.xml");
			
			StmtIterator stmtIter1 = aModel.listStatements();
			while (stmtIter1.hasNext()) {
				Statement st = stmtIter1.next();
				if (st.getSubject().getLocalName().startsWith("_")){
					mridMap.put(st.getSubject(), null);
					mridsFromTop.add(st.getSubject().getLocalName());
//					if (originalModelMrids.contains(st.getSubject().getLocalName())){
//						System.out.println("Found mrid: "+ st.getSubject().getLocalName());
//					}
				}
				else{
					todoSubjects.add(st.getSubject());
				}
			}
			
			
			loadBusesFromCsv();
			attemptToFindMrids(todoSubjects);
			
			
			
			if (originalMrids.containsAll(mridsFromTop)){
				System.out.println("All top mrids are present in original dataset.");
			}
			else{
				System.out.println("Some mrids from top were not present in original dataset.");
			}
			
			
		
			// StmtIterator stmtIter = rdfModel.listStatements(new
			// SimpleSelector(null, RDF.type, Esca60Vocab.SUBSTATION_OBJECT));

			while (stmtIter.hasNext()) {
				Statement stmt = stmtIter.nextStatement();
				Resource subject = stmt.getSubject();
				Resource newRes = ResourceFactory.createResource(subject.getLocalName());
				//Resource newRes = rdfModel.createResource(EscaVocab.URI_ROOT+subject.getLocalName());
				System.out.println(subject);
				if (rdfModel.contains(null, RDF.value, newRes)){ //.containsResource(newRes)){ //.contains(newRes, null, (RDFNode) null)){
					System.out.println("FinALLY!");
				}
				String fqn = EscaVocab.URI_ROOT+subject.getLocalName();
				System.out.println(fqn);
				Resource fullCimResource = rdfModel.getResource(fqn);
				
				StmtIterator stmtItr2 = fullCimResource.listProperties();
				while (stmtItr2.hasNext()) {
					Statement stmtfqn = stmtItr2.next();
					System.out.println("Found: "+stmtfqn.getSubject());
				}
				
			}
			
//			ResIterator resItr = aModel.listSubjects();
//			
//			while(resItr.hasNext()){
//				Resource res1 = resItr.next(); 
//								
//				System.out.println(res1.getId());
//			}
//			
//			System.out.println("bah");
			

//			rdfModel = EscaVocab.readModel(new File("C:/temp/cim_state_variable_test/mark_export_stnet_dts_20150518_170619_top.xml"));
//			System.out.println("Num Terminals: "+findRdfType(EscaVocab.TERMINAL_OBJECT).size());
//			for(Resource res: findRdfType(EscaVocab.TERMINAL_OBJECT)){
//				afterMrids.add(res.getLocalName());
//			}
//			
//			if(afterMrids.removeAll(beforeMrids)){
//				System.out.println("At least one element was removed YAY!");
//				System.out.println("There were "+afterMrids.size()+" not in big cim");
//				for(String r:afterMrids){
//					System.out.println(r);
//				}
//			}
//			
//			performMatchProceedure(afterMrids);
			
			
			//rdfModel = EscaVocab.readModel(file.getAbsoluteFile());
		
	}
	
	public static void main(String[] args) throws Exception {
		MatchMrids matcher = new MatchMrids();
		
		matcher.loadData();
		matcher.writeData();
	}
}
