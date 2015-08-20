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
import org.apache.commons.logging.impl.NoOpLog;

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
	
	private enum FileType{
		CapFile, KvFile, AuxFile, NodeFile,	NetmonFile,	StationFile
	}
	
	private enum CapFields{
		Type, CapFileIndx, CapName, P_Kv_Cp 
	}
	
	private enum StationFields{
		Type, StFileIndx, StName 
	}
	
	private enum NodeFields{
		Type, NodeFileIndx, NodeName, I_Bus_ND, P_Kv_ND 
	}
	
	private enum KvFields{
		Type, KvFileIndx, KvName, I_Bus_Kv, VL_Kv, P_ST_Kv 
	}
	
	private enum AuxFields{
		Type, AuxFileIndx, AuxName, P_KV_Aux
	}
	
	
	
	/**
	 * The list of mrids from the export_cim.xml file.
	 */
	private Set<String> originalMrids = new HashSet<>();
	
	/**
	 * The set of mrids that were found in top.
	 */
	private Set<String> mridsFromTop = new HashSet<>();
	
	private Map<String, List<Element>> busElements = new HashMap<>(); 
	
	private Map<FileType, List<CSVRecord>> csvDataMap;
	
	private Map<String, List<Map<Integer, Map<FileType, List<CSVRecord>>>>> stationContents;
	private Map<Integer, Map<FileType, List<CSVRecord>>> busContents;
	
	
	Tree csvFileRoot = new Tree();
	
	class TreeElement extends Tree {
		String type;
		CSVRecord record;
		
		public TreeElement(String type){
			this(type, null);
		}
		
		public TreeElement(String type, CSVRecord record){
			this.type = type;
			this.record = record;
		}
		
		public String getType(){
			return type;
		}
		
		public CSVRecord getRecord(){
			return record;
		}
		
		@Override
		public String toString() {
			if (record == null){
				return type;
			}
			
			StringBuilder builder = new StringBuilder();
			boolean first = true;
			for(String item: record){
				if (first) {
					first = false;
					builder.append(item);
				}
				else{
					builder.append(","+item);
				}
				
			}
			
			return type + " " + builder.toString();
		}
	}
	
	class Tree{
		private List<Tree> children = new ArrayList<>();
		private Tree parent;
		private TreeElement element;
		
		public void setTreeElement(TreeElement element){
			this.element = element;
		}
		
		public TreeElement getTreeElement(){
			return element;
		}
		
		public void addChild(Tree obj){
			if (!children.contains(obj)){
				System.out.println("Adding tree element: "+ obj + " to "+this);
				children.add(obj);
			}
		}
		
		public Tree getParent() {
			return parent;
		}
		
		public boolean isLeaf(){
			return (children.size() == 0);
		}
		
		public List<Tree> getLeaves(){
			List<Tree> leaves = new ArrayList<>();
			
			for(Tree ch: this.children){
				if (ch.isLeaf()){
					leaves.add(ch);
				} else{
					leaves.addAll(ch.getLeaves());
				}
			}
			
			return leaves;
		}
		
		public void setParent(Tree parent) {
			this.parent = parent;
			
			if (parent != null){
				this.parent.addChild(this);
			}
			
		}
		public List<Tree> getChildren() {
			return children;
		}
		
		public List<Tree> getChildren(String type) {
			List<Tree> ch = new ArrayList();
			for (Tree child: this.children){
				if(child.getTreeElement().getType().equals(type)){
					ch.add(child);
				}
			}
			return ch;
		}
		
		public List<String> getChildTypes(){
			List<String> types = new ArrayList();
			for (Tree child: this.children){
				if (!types.contains(child.getTreeElement().getType())){
					types.add(child.getTreeElement().getType());
				}
			}
			return types;
		}
		
		@Override
		public String toString() {
			if (element != null){
				return element.toString();
			}
			return super.toString();
		}
		
	}
	
	
	public TreeElement getCapFromLeaf(Tree leaf){
				
		while(!leaf.getTreeElement().getType().equals(FileType.CapFile.name())){
			leaf = leaf.getParent();
		}
		
		leaf = leaf.getParent();
		
		return leaf.getTreeElement();
	}
	
	public TreeElement getStationFromLeaf(Tree leaf){
		
		while(!leaf.getParent().getTreeElement().getType().equals("Stations")){
			leaf = leaf.getParent();
		}
		
		return leaf.getTreeElement();
	}
	
	public String trimQt(String data){
		return data.replace("'", "");
	}
	
	public String buildStationId(){
		// Station tree
		List<Tree> leaves = csvFileRoot.getLeaves();
		
		for(Tree tr: leaves){
			TreeElement st = getStationFromLeaf(tr);
			String stationName = st.getRecord().get(StationFields.StName.ordinal());
			String elementId = "ST."+trimQt(stationName);
			
			TreeElement te = tr.getTreeElement();
			if (te.getType().equals(FileType.NodeFile.name())){
				String nodeName = te.getRecord().get(NodeFields.NodeName.ordinal());
				elementId += ".ND."+trimQt(nodeName);
			} 
			else if(te.getType().equals(FileType.CapFile.name())){
				String capName = te.getRecord().get(CapFields.CapName.ordinal());
				elementId += ".CP."+trimQt(capName);
			}
									
			System.out.println(elementId);
		}
//		for(Tree child: csvFileRoot.getChildren(FileType.StationFile.name())){
//			for (Tree bus)
//			StringBuilder sb = new StringBuilder();
//			System.out.println("Building kv with "+child);
//			for (String ch: child.getChildTypes()){
//				System.out.println("\t"+ch);
//			}
//		}
		return "woot";
	}
		
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
	
	/**
	 * Add the field names to the properties list using rec as the datasource.
	 * @param properties
	 * @param rec
	 * @param field_names
	 */
	private void addProperties(List<Element> properties, CSVRecord rec, String[] field_names){
		for(int i=0; i< field_names.length; i++){
			if (field_names[i] == null || field_names[i].isEmpty()){
				continue;
			}
			
			properties.add(new Element(field_names[i], rec.get(i)));
		}
	}
	
	private List<CSVRecord> getRecords(FileType fileType){
		return csvDataMap.get(fileType);
	}
	
	private List<CSVRecord> getStationKvLevelIndexes(int stationIndx){
		// Look up station index from the kv file and return the records that match
		// the kv file.
		List<CSVRecord> kvLevels = new ArrayList<>();
		for(CSVRecord rec: getRecords(FileType.KvFile)){
			int pstationIdx = Integer.parseInt(rec.get(KvFields.P_ST_Kv.ordinal()));
			if (pstationIdx == stationIndx ){
				kvLevels.add(rec);
			}
		}
		return kvLevels;
	}
	
	private List<CSVRecord> getNodeFromKvLevelIndexes(int kvLevelndx){
		List<CSVRecord> nodes = new ArrayList<>();
		for(CSVRecord rec: getRecords(FileType.NodeFile)){
			int pkvIndx = Integer.parseInt(rec.get(NodeFields.P_Kv_ND.ordinal()));
			if (pkvIndx == kvLevelndx ){
				nodes.add(rec);
			}
		}
		return nodes;
	}
	
	private List<CSVRecord> getAuxFromKvLevelIndexes(int kvLevelndx){
		List<CSVRecord> recs = new ArrayList<>();
		for(CSVRecord rec: getRecords(FileType.AuxFile)){
			int pkvIndx = Integer.parseInt(rec.get(AuxFields.P_KV_Aux.ordinal()));
			if (pkvIndx == kvLevelndx ){
				recs.add(rec);
			}
		}
		return recs;
	}
	
	private List<CSVRecord> getCapFromKvLevelIndexes(int kvLevelndx){
		List<CSVRecord> recs = new ArrayList<>();
		for(CSVRecord rec: getRecords(FileType.CapFile)){
			int pkvIndx = Integer.parseInt(rec.get(CapFields.P_Kv_Cp.ordinal()));
			if (pkvIndx == kvLevelndx ){
				recs.add(rec);
			}
		}
		return recs;
	}
	
//	public List<String> getDeviceLabels(){
//		
//	}

	private void loadBusesFromCsv(){
		List<CSVRecord> idmaprecords = getRecords("C:/temp/cim_state_variable_test/Viper_ws_e-terrasource_netmom.netmom.idmap", 4);
		List<CSVRecord> auxrecords = getRecords("C:/temp/cim_state_variable_test/hdbexport/aux_mark.csv", 4);
		List<CSVRecord> capacitorrecords = getRecords("C:/temp/cim_state_variable_test/hdbexport/capacitor_mark.csv", 4);
		List<CSVRecord> kvrecords = getRecords("C:/temp/cim_state_variable_test/hdbexport/kv_mark.csv", 6);
		List<CSVRecord> noderecords = getRecords("C:/temp/cim_state_variable_test/hdbexport/node_mark.csv", 5);
		List<CSVRecord> stationrecords = getRecords("C:/temp/cim_state_variable_test/hdbexport/station_mark.csv", 3);
		
		csvDataMap = new HashMap<>();
		busContents = new HashMap<>();
		stationContents = new HashMap<>();
		
		csvDataMap.put(FileType.AuxFile,  auxrecords);
		csvDataMap.put(FileType.CapFile, capacitorrecords);
		csvDataMap.put(FileType.KvFile, kvrecords);
		csvDataMap.put(FileType.NetmonFile,  idmaprecords);
		csvDataMap.put(FileType.NodeFile, noderecords);
		csvDataMap.put(FileType.StationFile, stationrecords);
		
		String[] kv_fields = {"", "indx", "id_kv", "i_bs_kv", "vl_kv", "p_st_kv"};
		
		
		csvFileRoot.setTreeElement(new TreeElement("Stations"));
		
		for (CSVRecord rec: stationrecords){
			String stationName = rec.get(StationFields.StName.ordinal());
			Tree stTree = new Tree();
			stTree.setTreeElement(new TreeElement(FileType.StationFile.name(), rec));
			stTree.setParent(csvFileRoot);
			
			
			System.out.println("Station: "+stationName);
			int stationIndx = Integer.parseInt(rec.get(StationFields.StFileIndx.ordinal()));
			List<Map<Integer, Map<FileType, List<CSVRecord>>>> stationBuses = new ArrayList<>();
			stationContents.put(stationName, stationBuses);
			for(CSVRecord kvRec: getStationKvLevelIndexes(stationIndx)){
				
				int busNum = Integer.parseInt(kvRec.get(KvFields.I_Bus_Kv.ordinal()));
				
				Tree busTree = new Tree();
				busTree.setTreeElement(new TreeElement("bus-"+busNum));
				busTree.setParent(stTree);
				
				Tree kvTree = new Tree();
				kvTree.setTreeElement(new TreeElement(FileType.KvFile.name(), kvRec));
				kvTree.setParent(busTree);
				
				int kvIndx = Integer.parseInt(kvRec.get(KvFields.KvFileIndx.ordinal()));
								
				for (CSVRecord capRec: getCapFromKvLevelIndexes(kvIndx)){
					Tree item = new Tree();
					item.setTreeElement(new TreeElement(FileType.CapFile.name(), capRec));
					item.setParent(kvTree);
				}
				
				for (CSVRecord ndRec: getNodeFromKvLevelIndexes(kvIndx)){
					Tree item = new Tree();
					item.setTreeElement(new TreeElement(FileType.NodeFile.name(), ndRec));
					item.setParent(kvTree);
				}
				
								
				for (CSVRecord auxRec: getAuxFromKvLevelIndexes(kvIndx)){
					Tree item = new Tree();
					item.setTreeElement(new TreeElement(FileType.AuxFile.name(), auxRec));
					item.setParent(kvTree);
				}				
			}			
		}
		
		
		
		
		
		for (CSVRecord rec: kvrecords){
			
			// Bus number associated with this record and how we get 
			// the properties associated with the bus.
			String bus = getBusString(Integer.parseInt(rec.get(3)));
			
			if (!busElements.containsKey(bus)){
				busElements.put(bus, new ArrayList<Element>());
			}
			
			List<Element> elements = busElements.get(bus);
			
			addProperties(elements, rec, kv_fields);
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
