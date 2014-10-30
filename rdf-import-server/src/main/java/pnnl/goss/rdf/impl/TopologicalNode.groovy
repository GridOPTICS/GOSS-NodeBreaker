package pnnl.goss.rdf.impl

import com.hp.hpl.jena.rdf.model.Resource
import pnnl.goss.rdf.EscaType;

class TopologicalNode {
	
	EscaType substation;
	EscaType voltageLevel;
	
	String identifier;
	EscaTypeBag loads;
	EscaTypeBag shunts;
	EscaTypeBag generators;
	
	Resource voltageLevelRes;
	
	ConnectivityNodes connectivityNodes = new ConnectivityNodes();
	Terminals terminals = new Terminals();
}
