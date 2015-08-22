package pnnl.goss.rdf.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;

import pnnl.goss.rdf.InvalidArgumentException;
import pnnl.goss.rdf.server.EscaVocab;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
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
	
	// This prefix is used in the dts top export so we use it in the json model object as well.
	private static String PRE_BUS = "bs-";
	
	/*
	 * This object reprensents the model that is loaded from the csv inputs.
	 * The structure of this is as follows
	 * /
	 * /staions = [{station1 ..}
	 * /aux
	 * /model: {station1 /kv
	 * 					   /buses
	 *                   /
	 */
	private JsonObject modelRoot;
	
	
	
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
		return String.format(PRE_BUS+"%d", num);
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
	
	/**
	 * Removes "'" and spaces from the passed string.
	 * 
	 * @param str
	 * @return
	 */
	private String trimString(String str){
		String s = str.replace("'", "");
		s = s.trim();
		return s;
	}
	
	/**
	 * Creates a json object representation of the passed station record.
	 * 
	 * @param rec
	 * @return
	 */
	private JsonObject buildStation(CSVRecord rec){
		JsonObject obj = new JsonObject();
		
		obj.addProperty("type", rec.get(StationFields.Type.ordinal()));
		obj.addProperty("id", Integer.parseInt(rec.get(StationFields.StFileIndx.ordinal())));
		obj.addProperty("name", trimString(rec.get(StationFields.StName.ordinal())));
		System.out.println(obj.toString());
				
		return obj;
	}
	
	/**
	 * Creates a json object representation of the passed capacitor record.
	 * 
	 * @param rec
	 * @return
	 */
	private JsonObject buildCap(CSVRecord rec){
		JsonObject obj = new JsonObject();
		
		obj.addProperty("type", rec.get(CapFields.Type.ordinal()));
		obj.addProperty("id", Integer.parseInt(rec.get(CapFields.CapFileIndx.ordinal())));
		obj.addProperty("name", trimString(rec.get(CapFields.CapName.ordinal())));
		obj.addProperty("p__kv_id", Integer.parseInt(rec.get(CapFields.P_Kv_Cp.ordinal())));
				
		return obj;
	}
	
	/**
	 * Creates a json object representation of the passed kv record.
	 * 
	 * @param rec
	 * @return
	 */
	private JsonObject buildKv(CSVRecord rec){
		JsonObject obj = new JsonObject();
		
		obj.addProperty("type", rec.get(KvFields.Type.ordinal()));
		obj.addProperty("id", Integer.parseInt(rec.get(KvFields.KvFileIndx.ordinal())));
		obj.addProperty("name", trimString(rec.get(KvFields.KvName.ordinal())));
		obj.addProperty("i__bs_kv", Integer.parseInt(rec.get(KvFields.I_Bus_Kv.ordinal())));
		obj.addProperty("vl_kv", Float.parseFloat(trimString(rec.get(KvFields.VL_Kv.ordinal()))));
		obj.addProperty("p__st_id", Integer.parseInt(rec.get(KvFields.P_ST_Kv.ordinal())));
				
		return obj;
	}
	
	/**
	 * Creates a json object representation of the passed aux record.
	 * 
	 * @param rec
	 * @return
	 */
	private JsonObject buildAux(CSVRecord rec){
		JsonObject obj = new JsonObject();
		
		obj.addProperty("type", rec.get(AuxFields.Type.ordinal()));
		obj.addProperty("id", Integer.parseInt(rec.get(AuxFields.AuxFileIndx.ordinal())));
		obj.addProperty("name", trimString(rec.get(AuxFields.AuxName.ordinal())));
		obj.addProperty("p__kv_id", Integer.parseInt(rec.get(AuxFields.P_KV_Aux.ordinal())));
				
		return obj;
	}
	
	/**
	 * Creates a json object representation of the passed node record.
	 * 
	 * @param rec
	 * @return
	 */
	private JsonObject buildNode(CSVRecord rec){
		JsonObject obj = new JsonObject();
		
		obj.addProperty("type", rec.get(NodeFields.Type.ordinal()));
		obj.addProperty("id", Integer.parseInt(rec.get(NodeFields.NodeFileIndx.ordinal())));
		obj.addProperty("name", trimString(rec.get(NodeFields.NodeName.ordinal())));
		obj.addProperty("i__bs_nd", Integer.parseInt(trimString(rec.get(NodeFields.I_Bus_ND.ordinal()))));
		obj.addProperty("p__kv_id", Integer.parseInt(rec.get(NodeFields.P_Kv_ND.ordinal())));
				
		return obj;
	}
	
	private JsonObject buildIdMap(CSVRecord rec){
		JsonObject obj = new JsonObject();
		
		obj.addProperty("type", "IDMAP");
		obj.addProperty("id", rec.get(3).toString());
		obj.addProperty("name", rec.get(5).toString());
				
		return obj;
	}
	
	private String findMridFromEquipString(String equipString){
		for(JsonElement ele: modelRoot.get("idmap").getAsJsonArray()){
			JsonObject obj = ele.getAsJsonObject();
			if (obj.get("name").getAsString().startsWith(equipString)){
				return obj.get("id").getAsString();
			}
		}
		return null;
	}
	
	private List<String> getNetMonAuxStrings(JsonObject busObj){
		List<String> capStrings = new ArrayList<>();
		if (busObj.has("aux")){
			
			for (JsonElement auxEle: busObj.get("aux").getAsJsonArray()){
				JsonObject auxObj = auxEle.getAsJsonObject();
				String toPrint = "ST."+busObj.get("st_name").getAsString()+".AUX."+auxObj.get("name").getAsString();
				capStrings.add(toPrint);
			}
		}
		return capStrings;
	}
	
	private List<String> getNetMonCapStrings(JsonObject busObj){
		List<String> capStrings = new ArrayList<>();
		if (busObj.has("capacitors")){
			
			for (JsonElement capEle: busObj.get("capacitors").getAsJsonArray()){
				JsonObject capObj = capEle.getAsJsonObject();
				String toPrint = "ST."+busObj.get("st_name").getAsString()+".CP."+capObj.get("name").getAsString();
				capStrings.add(toPrint);
			}
		}
		return capStrings;
	}
	
	private void buildModel(){
		
		JsonObject model = modelRoot.get("model").getAsJsonObject();
		
		Set<Integer> uniqueIds = new LinkedHashSet<>(); // = new Mapped<>();
		// Loop over the nodes and create unique buses from the i__bs_nd property.
		for (JsonElement ele: modelRoot.get("node").getAsJsonArray()){
			JsonObject nd = ele.getAsJsonObject();
			uniqueIds.add(nd.get("i__bs_nd").getAsInt());
		}
		
		JsonArray buses = modelRoot.get("buses").getAsJsonArray();
				
		for (Integer it: uniqueIds){
			JsonObject obj = new JsonObject();
			obj.addProperty("type", "bus");
			obj.addProperty("id", it);
			JsonArray nodes = new JsonArray();
			obj.add("nodes", nodes);
			
			buses.add(obj);			
			model.add(PRE_BUS+it.toString(), obj);
			
			// Add nodes to the bus object.
			for (JsonElement ele: modelRoot.get("node").getAsJsonArray()){
				JsonObject nd = ele.getAsJsonObject();
				if (nd.get("i__bs_nd").getAsInt() == obj.get("id").getAsInt()){
					nodes.add(nd);
				}
			}
		}
		
		// First loop over stations
		for (JsonElement ele: modelRoot.get("stations").getAsJsonArray()){
			JsonObject stationObj = ele.getAsJsonObject();
			
			model.add("station"+stationObj.get("id"), stationObj);
			
			for (JsonElement ele2: modelRoot.get("buses").getAsJsonArray()){
				JsonObject busObj = ele2.getAsJsonObject();
			
				// Now lets loop over kv
				for (JsonElement ele3: modelRoot.get("kv").getAsJsonArray()){
					JsonObject kvObj = ele3.getAsJsonObject();

					// If kv and bus match.
					if (kvObj.get("i__bs_kv").getAsInt() == busObj.get("id").getAsInt()){
						
						// If the kv is part of this station.
						if (kvObj.get("p__st_id").getAsInt() == stationObj.get("id").getAsInt()){
							busObj.addProperty("vl", kvObj.get("vl_kv").getAsNumber());
							busObj.add("kv", kvObj);
							busObj.addProperty("st_id", stationObj.get("id").getAsInt());
							busObj.addProperty("st_name", stationObj.get("name").getAsString());
							stationObj.add(PRE_BUS+busObj.get("id").getAsInt(), busObj);
							
							JsonArray capacitors = new JsonArray();
							for (JsonElement ele4: modelRoot.get("cap").getAsJsonArray()){
								JsonObject capObj = ele4.getAsJsonObject();
								
								if (kvObj.get("id").getAsInt() == capObj.get("p__kv_id").getAsInt()){
									capacitors.add(capObj);
								}
							}
							if (capacitors.size() > 0){
								busObj.add("capacitors", capacitors);
							}
							
							JsonArray auxArray = new JsonArray();
							for (JsonElement ele4: modelRoot.get("aux").getAsJsonArray()){
								JsonObject auxObj = ele4.getAsJsonObject();
								
								if (kvObj.get("id").getAsInt() == auxObj.get("p__kv_id").getAsInt()){
									auxArray.add(auxObj);
								}
							}
							if (auxArray.size() > 0){
								busObj.add("aux", auxArray);
							}
						}
					}						
				}
			}
		}
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try {
			FileUtils.write(new File("model.json"), gson.toJson(modelRoot));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		System.out.println(gson.toJson(modelRoot));
		
	}

	/**
	 * Loads the csv files into the rootModel.
	 */
	private void loadStationsFromCsv(){
		List<CSVRecord> idmaprecords = getRecords("C:/temp/cim_state_variable_test/Viper_ws_e-terrasource_netmom.netmom.idmap", 4);
		List<CSVRecord> auxrecords = getRecords("C:/temp/cim_state_variable_test/hdbexport/aux_mark.csv", 4);
		List<CSVRecord> capacitorrecords = getRecords("C:/temp/cim_state_variable_test/hdbexport/capacitor_mark.csv", 4);
		List<CSVRecord> kvrecords = getRecords("C:/temp/cim_state_variable_test/hdbexport/kv_mark.csv", 6);
		List<CSVRecord> noderecords = getRecords("C:/temp/cim_state_variable_test/hdbexport/node_mark.csv", 5);
		List<CSVRecord> stationrecords = getRecords("C:/temp/cim_state_variable_test/hdbexport/station_mark.csv", 3);
		
		// Only used to create a pretty version of json.
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		// Create the root object that will be available in the class.
		modelRoot = new JsonObject();
		
		// This array is going to be created from the node array in the buildModel function
		JsonArray busArray = new JsonArray();
		modelRoot.add("buses", busArray);
		
		// All the different csv files will be loaded into the arrays below.
		JsonArray stationArray = new JsonArray();
		JsonArray kvArray = new JsonArray();
		JsonArray capArray = new JsonArray();
		JsonArray auxArray = new JsonArray();
		JsonArray nodeArray = new JsonArray();
		JsonArray idmapArray = new JsonArray();
		
		JsonObject model = new JsonObject();
		modelRoot.add("model", model);
		
				
		for (CSVRecord rec: stationrecords){			
			stationArray.add(buildStation(rec));
		}
		modelRoot.add("stations", stationArray);
		
		for (CSVRecord rec: auxrecords){			
			auxArray.add(buildAux(rec));
		}		
		modelRoot.add("aux", auxArray);
		
		for (CSVRecord rec: kvrecords){			
			kvArray.add(buildKv(rec));
		}		
		modelRoot.add("kv", kvArray);
				
		for (CSVRecord rec: capacitorrecords){			
			capArray.add(buildCap(rec));
		}
		modelRoot.add("cap", capArray);
		
		for (CSVRecord rec: noderecords){			
			nodeArray.add(buildNode(rec));
		}
		modelRoot.add("node", nodeArray);
		
		for (CSVRecord rec: idmaprecords){			
			idmapArray.add(buildIdMap(rec));
		}
		modelRoot.add("idmap", idmapArray);
		
		// Now that the data is loaded into the rootModel populate the model property.
		buildModel();
		
		for(JsonElement busEle: modelRoot.get("buses").getAsJsonArray()){
			JsonObject busObj = busEle.getAsJsonObject();
			
			if (busObj.has("capacitors")){
				for(String s: getNetMonCapStrings(busObj)){
					String mrid = findMridFromEquipString(s);
					System.out.println(s + " -> "+mrid);
				}
			}
			
			if (busObj.has("aux")){
				for(String s: getNetMonAuxStrings(busObj)){
					String mrid = findMridFromEquipString(s);
					System.out.println(s + " -> "+mrid);
				}
			}
		}
		
		System.out.println(modelRoot.toString());
		
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
			
			
			loadStationsFromCsv();
			if(true)return;
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
