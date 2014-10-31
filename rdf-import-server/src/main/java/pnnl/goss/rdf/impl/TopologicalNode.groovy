package pnnl.goss.rdf.impl

import com.hp.hpl.jena.rdf.model.Resource
import pnnl.goss.rdf.EscaType;
import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class TopologicalNode {
	
	EscaType substation;
	EscaType voltageLevel;
	
	String identifier;
	EscaTypeBag loads;
	EscaTypeBag shunts;
	EscaTypeBag generators;
	
	Resource voltageLevelRes;
	
	Set<ConnectivityNode> connectivityNodes = new HashSet<ConnectivityNode>();
	//ConnectivityNodes connectivityNodes = new ConnectivityNodes();
	//Terminals terminals = new Terminals();
	Set<Terminal> terminals = new HashSet<Terminal>();
	
	
}
