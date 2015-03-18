package pnnl.goss.rdf;

import java.util.Collection;
import java.util.List;

import pnnl.goss.rdf.impl.TopologicalNodeImpl;

public interface Network {
	
	Collection<TopologicalNodeImpl> getTopologicalNodes();
	
	Collection<TopologicalBranch> getTopologicalBranches();
	
	Collection<TopologicalIsland> getTopologicalIslands();
}
