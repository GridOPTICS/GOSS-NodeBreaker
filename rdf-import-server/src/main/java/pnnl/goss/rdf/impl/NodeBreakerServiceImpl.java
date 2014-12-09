package pnnl.goss.rdf.impl;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.felix.ipojo.annotations.Provides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.rdf.EscaType;
import pnnl.goss.rdf.InvalidArgumentException;
import pnnl.goss.rdf.Network;
import pnnl.goss.rdf.NodeBreakerService;
import pnnl.goss.rdf.server.EscaVocab;
import sun.util.logging.resources.logging;

@Provides
public class NodeBreakerServiceImpl implements NodeBreakerService {
	private static Logger log = LoggerFactory.getLogger(NodeBreakerServiceImpl.class);
			
	Map<String, Network> processedNetworks = new HashMap<>();

	@Override
	public Network getNetwork(String networkKey) {
		return processedNetworks.get(networkKey);
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
			
			Network network = new NetworkImpl(escaTypes);
			String key = UUID.randomUUID().toString();
			processedNetworks.put(key, network);
			return key;
		} catch (MalformedURLException | InvalidArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

}
