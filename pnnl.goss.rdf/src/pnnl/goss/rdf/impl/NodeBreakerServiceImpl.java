package pnnl.goss.rdf.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.rdf.EscaType;
import pnnl.goss.rdf.InvalidArgumentException;
import pnnl.goss.rdf.Network;
import pnnl.goss.rdf.NodeBreakerService;
import pnnl.goss.rdf.TopologicalBranch;
import pnnl.goss.rdf.TopologicalNode;
import pnnl.goss.rdf.server.EscaVocab;


public class NodeBreakerServiceImpl implements NodeBreakerService {
	private static Logger log = LoggerFactory.getLogger(NodeBreakerServiceImpl.class);

	Map<String, Network> processedNetworks = new HashMap<>();

	@Override
	public Network getNetwork(String networkKey) {
		return processedNetworks.get(networkKey);
	}
	private Map<String, CSVRecord> createMap(int keyField, List<CSVRecord> records){
		Map<String, CSVRecord> map = new ConcurrentHashMap<>();
		
		for(CSVRecord r: records){
			map.put(r.get(keyField), r);
		}
		
		return map;
	}
	
	private void performMatchProceedure(EscaTypes escaTypes,
			NetworkImpl network){
		List<CSVRecord> idmaprecords = getRecords("C:/temp/cim_state_variable_test/Viper_ws_e-terrasource_netmom.netmom.idmap", 4);
		List<CSVRecord> auxrecords = getRecords("C:/temp/cim_state_variable_test/hdbexport/aux_mark.csv", 4);
		List<CSVRecord> capacitorrecords = getRecords("C:/temp/cim_state_variable_test/hdbexport/capacitor_mark.csv", 4);
		List<CSVRecord> kvrecords = getRecords("C:/temp/cim_state_variable_test/hdbexport/kv_mark.csv", 6);
		List<CSVRecord> noderecords = getRecords("C:/temp/cim_state_variable_test/hdbexport/node_mark.csv", 5);
		List<CSVRecord> stationrecords = getRecords("C:/temp/cim_state_variable_test/hdbexport/station_mark.csv", 3);
		
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
		
		for(Entry<String, CSVRecord> rec: mridmap.entrySet()){
			
			String lookupMrid = "_"+rec.getKey();
			EscaType term = escaTypes.get(lookupMrid);
			
			System.out.println(term);
			
		}
		
		for(String k: idmap.keySet()){
			String[] fields = k.split("\\.");
			System.out.println("key: "+k);
			String mapKey = fields[0]+fields[1]+fields[2];
			String mapValue = fields[3]+"."+fields[4];
			if (fields.length > 5){
				mapValue += "."+fields[5];
			}
			
			if (!nodeMap.containsKey(mapKey)){
				nodeMap.put(mapKey, new HashSet<String>());
			}
			
			nodeMap.get(mapKey).add(mapValue);
		}
		
		for(CSVRecord rec: idmap.values()){		
			EscaType term = escaTypes.get("_"+rec.get(3));
			if (term != null){
			//if (term.getDataType().equals(EscaVocab.TERMINAL_OBJECT.getLocalName())){
				System.out.println(String.format("%s, %s", term.getMrid(), term.getDataType()));
			}
		}
		
		for(TopologicalNode node: network.getTopologicalNodes()){
			System.out.println(node+" is connected to branches");
			for(TopologicalBranch br: ((TopologicalNodeImpl)node).getBranches()){
				System.out.println("\t"+br);
			}
		}
		
	}
	
//	private String buildResource(Resource res, String mrid){
//		return res.getURI()+"/"+mrid;
//	}
	
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


	@Override
	public String processNetwork(String fileName) {
		EscaTreeWindow window;
		try {
			window = new EscaTreeWindow(fileName, true, "esca_tree.txt");
			// Load data from the rdf into memory.
			window.loadData();
			// Build an mrid->escatype mapping for referencing all of the subjects by mrid
			// in the system.
			window.loadTypeMap();

			EscaTypes escaTypes = window.getEscaTypeMap();

			for(EscaType a: escaTypes.getByResourceType(EscaVocab.CONNECTIVITYNODE_OBJECT)) {
				log.debug("For cn: "+ a.getMrid() + " # terminals: "+ a.getRefersToMe(EscaVocab.TERMINAL_OBJECT).size());
			}
			
			EscaType type = escaTypes.get("_2583601325004599187");
			for (String key: type.getLiterals().keySet()){
				log.debug("key: "+ key + " value: " + type.getLiteralValue(key));
			}
			
			for(EscaType t: type.getDirectLinks()){
				log.debug("Direct: "+ type);
				for (String key: t.getLiterals().keySet()){
					log.debug("key: "+ key + " value: " + t.getLiteralValue(key));
				}
			}
			
;			System.out.println("NUM TERMINALS: " + escaTypes.getByResourceType(EscaVocab.TERMINAL_OBJECT).size());
			
			Network network = new NetworkImpl(escaTypes);
			String key = UUID.randomUUID().toString();
			processedNetworks.put(key, network);
			//performMatchProceedure(escaTypes, (NetworkImpl) network);
			return key;
		} catch (MalformedURLException | InvalidArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

}
