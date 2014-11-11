package pnnl.goss.rdf;

import java.util.List;

public interface TopologicalIsland {

	List<TopologicalBranch> getTopologicalBranches();
	
	List<TopologicalNode> getTopologicalNodes();
	
}
