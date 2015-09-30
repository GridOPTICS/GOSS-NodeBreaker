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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;

import pnnl.goss.rdf.Equipment;
import pnnl.goss.rdf.EscaType;
import pnnl.goss.rdf.InvalidArgumentException;
import pnnl.goss.rdf.Network;
import pnnl.goss.rdf.NodeBreakerService;
import pnnl.goss.rdf.Terminal;
import pnnl.goss.rdf.TopologicalNode;
import pnnl.goss.rdf.server.EscaVocab;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;

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
	private JsonObject jsonCsvModelRoot;
	
	private JsonObject topModelRoot;
	
	private JsonObject svModelRoot;
	
	/**
	 * A processed model file network that was produced from a cim model
	 * file.
	 */
	private Network cimNetworkBusBranch;
	
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
	
	/**
	 * Set the network to compare to the model csv and idmap files from
	 * the alsom simulation.
	 * 
	 * @param network
	 */
	public void setCimNetwork(Network network){
		cimNetworkBusBranch = network;
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
	
	
	private List<CSVRecord> getRecords(FileType fileType){
		return csvDataMap.get(fileType);
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
		for(JsonElement ele: jsonCsvModelRoot.get("idmap").getAsJsonArray()){
			JsonObject obj = ele.getAsJsonObject();
			if (obj.get("name").getAsString().startsWith(equipString)){
				return obj.get("id").getAsString();
			}
		}
		return null;
	}
	
	private String findFullEquipmentFromEquipString(String equipString){
		for(JsonElement ele: jsonCsvModelRoot.get("idmap").getAsJsonArray()){
			JsonObject obj = ele.getAsJsonObject();
			if (obj.get("name").getAsString().startsWith(equipString)){
				return obj.get("name").getAsString();
			}
		}
		return null;
	}
	
	private List<String> getNetMonNodeStrings(JsonObject busObj){
		List<String> nodeStrings = new ArrayList<>();
		// 
		if (busObj.has("nodes") && busObj.has("st_name")){
			
			for (JsonElement nodeEle: busObj.get("nodes").getAsJsonArray()){
				JsonObject nodeObj = nodeEle.getAsJsonObject();
				String toPrint = "ST."+busObj.get("st_name").getAsString()+".ND."+nodeObj.get("name").getAsString();
				nodeStrings.add(toPrint);
			}
		}
		return nodeStrings;
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
		
		JsonObject model = jsonCsvModelRoot.get("model").getAsJsonObject();
		
		Set<Integer> uniqueIds = new LinkedHashSet<>(); // = new Mapped<>();
		// Loop over the nodes and create unique buses from the i__bs_nd property.
		for (JsonElement ele: jsonCsvModelRoot.get("node").getAsJsonArray()){
			JsonObject nd = ele.getAsJsonObject();
			uniqueIds.add(nd.get("i__bs_nd").getAsInt());
		}
		
		JsonArray buses = jsonCsvModelRoot.get("buses").getAsJsonArray();
				
		for (Integer it: uniqueIds){
			JsonObject obj = new JsonObject();
			obj.addProperty("type", "bus");
			obj.addProperty("id", it);
			JsonArray nodes = new JsonArray();
			obj.add("nodes", nodes);
			
			buses.add(obj);			
			model.add(PRE_BUS+it.toString(), obj);
			
			// Add nodes to the bus object.
			for (JsonElement ele: jsonCsvModelRoot.get("node").getAsJsonArray()){
				JsonObject nd = ele.getAsJsonObject();
				if (nd.get("i__bs_nd").getAsInt() == obj.get("id").getAsInt()){
					nodes.add(nd);
				}
			}
		}
		
		// First loop over stations
		for (JsonElement ele: jsonCsvModelRoot.get("stations").getAsJsonArray()){
			JsonObject stationObj = ele.getAsJsonObject();
			
			model.add("station"+stationObj.get("id"), stationObj);
			
			for (JsonElement ele2: jsonCsvModelRoot.get("buses").getAsJsonArray()){
				JsonObject busObj = ele2.getAsJsonObject();
			
				// Now lets loop over kv
				for (JsonElement ele3: jsonCsvModelRoot.get("kv").getAsJsonArray()){
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
							for (JsonElement ele4: jsonCsvModelRoot.get("cap").getAsJsonArray()){
								JsonObject capObj = ele4.getAsJsonObject();
								
								if (kvObj.get("id").getAsInt() == capObj.get("p__kv_id").getAsInt()){
									capacitors.add(capObj);
								}
							}
							if (capacitors.size() > 0){
								busObj.add("capacitors", capacitors);
							}
							
							JsonArray auxArray = new JsonArray();
							for (JsonElement ele4: jsonCsvModelRoot.get("aux").getAsJsonArray()){
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
			FileUtils.write(new File("model.json"), gson.toJson(jsonCsvModelRoot));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		System.out.println(gson.toJson(jsonCsvModelRoot));
		
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
		jsonCsvModelRoot = new JsonObject();
		
		// This array is going to be created from the node array in the buildModel function
		JsonArray busArray = new JsonArray();
		jsonCsvModelRoot.add("buses", busArray);
		
		// All the different csv files will be loaded into the arrays below.
		JsonArray stationArray = new JsonArray();
		JsonArray kvArray = new JsonArray();
		JsonArray capArray = new JsonArray();
		JsonArray auxArray = new JsonArray();
		JsonArray nodeArray = new JsonArray();
		JsonArray idmapArray = new JsonArray();
		
		JsonObject model = new JsonObject();
		jsonCsvModelRoot.add("model", model);
		
				
		for (CSVRecord rec: stationrecords){			
			stationArray.add(buildStation(rec));
		}
		jsonCsvModelRoot.add("stations", stationArray);
		
		for (CSVRecord rec: auxrecords){			
			auxArray.add(buildAux(rec));
		}		
		jsonCsvModelRoot.add("aux", auxArray);
		
		for (CSVRecord rec: kvrecords){			
			kvArray.add(buildKv(rec));
		}		
		jsonCsvModelRoot.add("kv", kvArray);
				
		for (CSVRecord rec: capacitorrecords){			
			capArray.add(buildCap(rec));
		}
		jsonCsvModelRoot.add("cap", capArray);
		
		for (CSVRecord rec: noderecords){			
			nodeArray.add(buildNode(rec));
		}
		jsonCsvModelRoot.add("node", nodeArray);
		
		for (CSVRecord rec: idmaprecords){			
			idmapArray.add(buildIdMap(rec));
		}
		jsonCsvModelRoot.add("idmap", idmapArray);
		
		// Now that the data is loaded into the rootModel populate the model property.
		buildModel();
		
		System.out.println("Populate csv models with mrid and equipment for capacitors, aux and nodes.");
		System.out.println("  null -> null implies that there was no entry for the item in the netmon idmap.");
		System.out.println("  EX: ST.DOUGLAS.ND.11 -> null -> null then ST.DOUGLAS.ND.11 isn't in netmon idmap");
		for(JsonElement busEle: jsonCsvModelRoot.get("buses").getAsJsonArray()){
			JsonObject busObj = busEle.getAsJsonObject();
			
			if (busObj.has("capacitors")){
				for(String s: getNetMonCapStrings(busObj)){
					String mrid = findMridFromEquipString(s);
					String equip = findFullEquipmentFromEquipString(s);
					System.out.println("Cap " + s + " -> "+mrid+ " -> "+equip);
				}
			}
			
			if (busObj.has("aux")){
				for(String s: getNetMonAuxStrings(busObj)){
					String mrid = findMridFromEquipString(s);
					String equip = findFullEquipmentFromEquipString(s);
					System.out.println("Aux " + s + " -> "+mrid+ " -> "+equip);
				}
			}
			
			if (busObj.has("nodes")){
				for(String s: getNetMonNodeStrings(busObj)){
					String mrid = findMridFromEquipString(s);
					String equip = findFullEquipmentFromEquipString(s);
					System.out.println("Node " + s + " -> "+mrid+ " -> "+equip);
				} 
			}
		}		
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
	public void loadCimRdfData() throws InvalidArgumentException {

		// Load the exported cim file into memory and then loop over the
		// subjects and
		// add there mrids to the originalMrids variable.
		Model rdfModel = EscaVocab.readModel(new File(
				"C:/temp/cim_state_variable_test/export_cim.xml"));

		StmtIterator stmtIter = rdfModel.listStatements();
		while (stmtIter.hasNext()) {
			Statement st = stmtIter.next();

			// System.out.println(String.format("Subject: %s, Predicate: %s, Object: %s",
			// st.getSubject().getLocalName(), st.getPredicate().getLocalName(),
			// st.getObject().toString()));
			// System.out.println(st.getPredicate().getLocalName());
			if (st.getSubject().getLocalName().equalsIgnoreCase("Substation")) {
				System.out.println("Substation: "
						+ st.getSubject().getLocalName());
				StmtIterator stmtItr2 = st.getSubject().listProperties();
				while (stmtItr2.hasNext()) {
					Statement stmt2 = stmtItr2.next();
					System.out.println(stmt2.getPredicate().getLocalName());
				}
			}
			if (st.getPredicate().getLocalName()
					.equalsIgnoreCase("IdentifiedObject.name")) {
				System.out.println("Object is: " + st.getObject().toString());
			}

			if (st.getPredicate().getLocalName()
					.equalsIgnoreCase("IdentifiedObject.alias")) {
				System.out.println("Object is: " + st.getObject().toString());
			}
			if (st.getPredicate().getLocalName().equalsIgnoreCase("type")
					&& st.getSubject().getLocalName()
							.equalsIgnoreCase("Substation")) {
				System.out.println("Substation: "
						+ st.getSubject().getLocalName());
				StmtIterator stmtItr2 = st.getSubject().listProperties();
				while (stmtItr2.hasNext()) {
					Statement stmt2 = stmtItr2.next();
					System.out.println(stmt2.getPredicate().getLocalName());
				}
			}
			originalMrids.add(st.getSubject().getLocalName());
			if (st.getPredicate().getLocalName().contains("pathName")) {
				System.out.println("Path name: "
						+ st.getPredicate().getLocalName() + " -> "
						+ st.getObject().toString());
			}
		}

		// Contains an overal list of the string version of the mrids.
		Set<String> mridSet = new HashSet<>();

		// Contains an overall list of mrids that are found in the top file or
		// after mapping from the different
		// csv files to get the mrid. If the value is null, then there wasn't a
		// need to lookup the rdf:id from
		// alternate means.
		Map<Resource, String> mridMap = new HashMap<>();

		// This contains the entries that need to be looked up using the csv
		// files and the netmon.id map in order
		// to find the mird.
		Set<Resource> todoSubjects = new HashSet<>();

		Model aModel = ModelFactory.createDefaultModel();
		FileManager
				.get()
				.readModel(aModel,
						"C:/temp/cim_state_variable_test/mark_export_stnet_dts_20150518_170619_top.xml");

		StmtIterator stmtIter1 = aModel.listStatements();
		while (stmtIter1.hasNext()) {
			Statement st = stmtIter1.next();
			if (st.getSubject().getLocalName().startsWith("_")) {
				mridMap.put(st.getSubject(), null);
				mridsFromTop.add(st.getSubject().getLocalName());
				// if
				// (originalModelMrids.contains(st.getSubject().getLocalName())){
				// System.out.println("Found mrid: "+
				// st.getSubject().getLocalName());
				// }
			} else {
				todoSubjects.add(st.getSubject());
			}
		}

	}
	
	private String getTypeOfSubject(Resource subject) {
		StmtIterator stmtItr = subject.listProperties();
		while (stmtItr.hasNext()) {
			Statement stmt = stmtItr.nextStatement();
			Property pred = stmt.getPredicate();

			if (pred.getLocalName().equals("type")) {

				// System.out.println(stmt.getObject().toString());
				// System.out.println(stmt.getObject().asResource().getLocalName());
				return stmt.getObject().asResource().getLocalName();

			}

		}

		return null;
	}
	
	private void addLiterals(Resource res, JsonObject jsonObj){
		StmtIterator itr = res.listProperties();
		
		while (itr.hasNext()){
			Statement stmt = itr.nextStatement();
			RDFNode node = stmt.getObject();
			Property prop = stmt.getPredicate();
			
			if (node.isLiteral()){
				jsonObj.addProperty(prop.getLocalName(), stmt.getLiteral().getString());
			}
		}
	}
	
	private void addLinks(Resource resource, JsonObject jsonObj){
		StmtIterator itr = resource.listProperties();

		while (itr.hasNext()) {
			Statement stmt = itr.nextStatement();
			RDFNode node = stmt.getObject();
			Property prop = stmt.getPredicate();
			if (node.isResource()) {
				Resource otherResource = node.asResource();
				String dataType = getTypeOfSubject(otherResource);
				JsonObject otherObj = new JsonObject();
				
				otherObj.addProperty("type", dataType);
				otherObj.addProperty("id", otherResource.getLocalName());
				addLiterals(otherResource, otherObj);
				
				jsonObj.add(otherResource.getLocalName(), otherObj);
			}
		}
		
	}

	private void findTopInCim(){
		for(JsonElement node : topModelRoot.get("terminals").getAsJsonArray()){
			JsonObject obj = node.getAsJsonObject();
			
			String id = obj.get("id").getAsString();
			if (id.startsWith("_")){
				System.out.println(this.cimNetworkBusBranch.getByMrid(id));
			}
			//System.out.println(obj.get("id").getAsString()); 
			
		}
	}
	
	private void loadTopRdfData(){
		Model m = ModelFactory.createDefaultModel();
		FileManager.get().readModel(m, "C:/temp/cim_state_variable_test/mark_export_stnet_dts_20150518_170619_top.xml");
		
		JsonArray islands = new JsonArray();
		JsonArray terminals = new JsonArray();
		JsonArray topicalNodes = new JsonArray();
		topModelRoot = new JsonObject();
		
		// Load all of the subjects into json based upon type
		ResIterator resItr = m.listSubjects();
		
		Set<String> types = new LinkedHashSet<>();
		while(resItr.hasNext()){
			
			Resource res = resItr.next();
			String dataType = getTypeOfSubject(res);
			JsonObject obj = new JsonObject();
			obj.addProperty("type", dataType);
			obj.addProperty("id", res.getLocalName());
			addLiterals(res, obj);
			addLinks(res, obj);
			/*
			 * Terminal
TopologicalIsland
TopologicalNode
			 */
			if (dataType.equals("ToplogicalNode")){
				topicalNodes.add(obj);
			}
			else if(dataType.equals("Terminal")){
				terminals.add(obj);
			}
			else { // Topoisland
				islands.add(obj);
			}
			//System.out.println(getTypeOfSubject(res));
			//System.out.println(res.getLocalName());
			
		}
		
		if (islands.size() > 0){
			topModelRoot.add("islands", islands);
		}
		
		if (topicalNodes.size() > 0){
			topModelRoot.add("topicalNodes", topicalNodes);
		}
		
		if (terminals.size() > 0){
			topModelRoot.add("terminals", terminals);
		}
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try {
			FileUtils.write(new File("top.json"), gson.toJson(topModelRoot));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

//		for(String s: types){
//			System.out.println(s);
//		}
		
//		StmtIterator stmtIter1 = m.listStatements();
//		while (stmtIter1.hasNext()) {
//			Statement st = stmtIter1.next();
//			
//			if (st.getObject().isResource()){
//				Resource rs = st.getObject().asResource();//.getResource();
//				if (rs.getLocalName().equals("ToplogicalNode")){
//					System.out.println("WOOT");
//				}
//				System.out.println(rs);
//				StmtIterator propIterator = rs.listProperties();
//				while(propIterator.hasNext()){
//					Statement propStmt = propIterator.next();
//					
//					System.out.println("PROP: " + propStmt);
//				}
//				
//			}
//			
//			
////			if (st.)
////			Resource rs = st.getObject().asResource();
////			st.getProperty(arg0)
////			System.out.println(st);
////			System.out.println(st.getObject());
////			System.out.println(st.getPredicate());
////			System.out.println(st.getSubject());
//			
//		}
		
	}
	
	private int getNumNodesInCsvSubstation(String substation){
		int count = 0;
		for(JsonElement el: jsonCsvModelRoot.get("buses").getAsJsonArray()){
			JsonObject obj = el.getAsJsonObject();
			if (obj.has("st_name")){
				if (obj.get("st_name").getAsString().toUpperCase().equals(substation.toUpperCase())){
					count += 1;
				}	
			} 
//			else{
//				System.out.println("No st_name param in: "+obj.toString());
//			}
			
		}
			
		return count;
	}
	
	private int getNumNodesInCimSubstation(String substation){
		int count = 0;
		
		
//		for(TopologicalNode tn: cimNetworkBusBranch.getTopologicalNodes()){
//			
//			if (tn.getSubstationName().contains("DOUGLAS")){
//				System.out.println(tn);
//				for(Terminal tm : tn.getTerminals()){
//					System.out.println("\tTerminal: "+tm);
//				}
//				for(Equipment eq: tn.getEquipment()){
//					System.out.println("Equipment: " + eq);
//				}
//			}
//		}
		
		for(TopologicalNode tn: cimNetworkBusBranch.getTopologicalNodes()){
			
			if (tn.getSubstationName().toUpperCase().contains(substation.toUpperCase())){
				count += 1;
			}
		}
			
		return count;
	}
	
	
	
	public void matchTopologicalNodes(){
		
		//System.out.println("Substation\t# Cim\t # Csv");
		String fmt = "%20s%10s%10s";
		Set<String> notFound = new HashSet<>();
		System.out.println("Attempting to match nodes in substations between cim and csv");
		System.out.println(String.format(fmt, "Substation", "#Cim", "#Csv"));
		
		for(JsonElement ele: jsonCsvModelRoot.get("stations").getAsJsonArray()){
			JsonObject obj = ele.getAsJsonObject();
			
			if(obj.has("name")){
				String substation = obj.get("name").getAsString();
//		
				int cimValue = getNumNodesInCimSubstation(substation);
				int csvValue = getNumNodesInCsvSubstation(substation);
				
				System.out.println(String.format(fmt, substation, cimValue, csvValue));
			}
			else{
				notFound.add(obj.toString());
				//System.out.println("Substation not found for: "+obj.toString());
			}			
		}
		if (true) return;
		System.out.println("The following topo nodes were not found.");
		for(String s: notFound){
			System.out.println(s);
		}
	}
	
	private boolean matchTopologicalNode(TopologicalNode node){
		// Returns true if the station is the same.
		
		return true;
	}
	
	/**
	 * This function attempts to match nodes from the csv file and the
	 * Network model that is set on the object.  It will populate an internal
	 * state for the objects that it matches.
	 */
	public void matchResourceIdentifiers(){
		
		JsonArray idMap = jsonCsvModelRoot.get("idmap").getAsJsonArray();
		
		Map<String, JsonObject> foundObject = new LinkedHashMap<>();
		Map<String, EscaType> foundNetworkObject = new LinkedHashMap<>();
		
		List<String> notFoundFromCsv = new ArrayList<>();
		
		for(JsonElement ele: idMap){
			
			boolean found = false;
			JsonObject idObj = ele.getAsJsonObject();
			// Retrieve the mrid from the idmap which does not have an underscore in front
			// of it like it does in the cim file.
			String mrid = idObj.get("id").getAsString();
			
			EscaType esca = cimNetworkBusBranch.getByMrid(("_"+mrid));
			
			if (esca != null){
				foundObject.put(esca.getMrid(), idObj);
				foundNetworkObject.put(esca.getMrid(), esca);
			}
			else{
				notFoundFromCsv.add(idObj.get("id").getAsString());
			}
			
		}
		System.out.println("-----------------------------------------------------------------------");
		System.out.println("Found");
		for (String s: foundObject.keySet()){
			JsonObject obj = foundObject.get(s);
			System.out.println(obj.toString());
		}
		System.out.println("***********************************************************************");
		System.out.println("Not Found");
		for (String s: notFoundFromCsv){
			JsonObject obj = null;
			for(JsonElement ele: idMap){
				JsonObject o = ele.getAsJsonObject();
				if (o.get("id").getAsString().equals(s)){
					obj = o;
					break;
				}
			}
			System.out.println(s+ " "+ obj.toString());
		}
		
		if (notFoundFromCsv.size() == 0){
			System.out.println("None");
		}
		System.out.println("***********************************************************************");
	}
	
	
	private void loadSvRdfData(){
		Model m = ModelFactory.createDefaultModel();
		FileManager.get().readModel(m, "C:/temp/cim_state_variable_test/mark_export_stnet_dts_20150518_170619_sv.xml");
		
		JsonArray islands = new JsonArray();
		JsonArray terminals = new JsonArray();
		JsonArray topicalNodes = new JsonArray();
		svModelRoot = new JsonObject();
		
		// Load all of the subjects into json based upon type
		ResIterator resItr = m.listSubjects();
		
		Set<String> types = new LinkedHashSet<>();
		while(resItr.hasNext()){
			
			Resource res = resItr.next();
			String dataType = getTypeOfSubject(res);
			JsonObject obj = new JsonObject();
			obj.addProperty("type", dataType);
			obj.addProperty("id", res.getLocalName());
			addLiterals(res, obj);
			addLinks(res, obj);
			svModelRoot.add(res.getLocalName(), obj);
		}
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try {
			FileUtils.write(new File("sv.json"), gson.toJson(svModelRoot));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) throws Exception {
		MatchMrids matcher = new MatchMrids();
		
		String testFile = "C:/temp/cim_state_variable_test/export_cim.xml";
		NodeBreakerService svc = new NodeBreakerServiceImpl();
		String networkKey = svc.processNetwork(testFile);
		Network network = svc.getNetwork(networkKey);
		matcher.setCimNetwork(network);
		matcher.loadStationsFromCsv();
		
		matcher.loadTopRdfData();
		
		matcher.findTopInCim();
		
		for(TopologicalNode tn: network.getTopologicalNodes()){
			tn.printDebug(System.out);
		}
		//matcher.matchResourceIdentifiers();
		//matcher.matchTopologicalNodes();
		
		
		//matcher.loadCimRdfData();
		//matcher.loadTopRdfData();
		//matcher.loadSvRdfData();
		
		
		
		//matcher.writeData();
	}
}
