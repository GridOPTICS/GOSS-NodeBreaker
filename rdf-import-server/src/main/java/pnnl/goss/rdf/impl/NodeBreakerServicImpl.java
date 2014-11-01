package pnnl.goss.rdf.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Provides;

import pnnl.goss.rdf.Network;
import pnnl.goss.rdf.NodeBreakerService;

@Provides
public class NodeBreakerServicImpl implements NodeBreakerService {
	
	Map<String, Network> processedNetworks = new HashMap<>();

	@Override
	public Network getNetwork(String networkKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String processNetwork(String fileName) {
		// TODO Auto-generated method stub
		return null;
	}

}
