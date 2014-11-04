package pnnl.goss.rdf;

import java.util.Set;

public interface TopologicalNode {
	
	Set<Terminal> getTerminals();
	
	String getIdentifier();
	
	String getSubstationName();

}
