package pnnl.goss.rdf;

import java.util.List;

import pnnl.goss.rdf.impl.TopologicalNodeImpl;

public interface Network {
	
	List<TopologicalNodeImpl> getTopologicalNodes();
	
	List<TopologicalBranch> getTopologicalBranches();
	
	List<TopologicalIsland> getTopologicalIslands();
}
