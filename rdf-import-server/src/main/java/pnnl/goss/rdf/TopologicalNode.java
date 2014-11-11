package pnnl.goss.rdf;

import java.util.List;

public interface TopologicalNode {
	
	List<Terminal> getTerminals();
	
	String getIdentifier();
	
	String getSubstationName();

}
