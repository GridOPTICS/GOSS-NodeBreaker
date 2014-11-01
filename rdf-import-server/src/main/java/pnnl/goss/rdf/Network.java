package pnnl.goss.rdf;

import java.util.Set;

public interface Network {
	
	Set<TopologicalNode> getTopologicalNodes();
	
	Set<TopologicalBranch> getTopologicalBranches();
	
	Set<TopologicalIsland> getTopologicalIslands();
}
