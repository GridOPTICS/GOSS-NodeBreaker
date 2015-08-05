package pnnl.goss.rdf;

import java.util.Collection;
import java.util.List;

import pnnl.goss.rdf.core.RdfBranches;
import pnnl.goss.rdf.core.RdfBuses;
import pnnl.goss.rdf.impl.TopologicalNodeImpl;

public interface Network {
	
	Collection<TopologicalNode> getTopologicalNodes();
	
	Collection<TopologicalBranch> getTopologicalBranches();
	
	Collection<TopologicalIsland> getTopologicalIslands();
	
	RdfBuses getRdfBuses();
	RdfBranches getRdfBranches();
	
}
