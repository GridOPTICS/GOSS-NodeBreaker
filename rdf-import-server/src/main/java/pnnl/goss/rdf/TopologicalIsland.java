package pnnl.goss.rdf;

import java.util.Set;

public interface TopologicalIsland {

	Set<TopologicalBranch> getTopologicalBranches();
	
	Set<TopologicalNode> getTopologicalNodes();
	
}
