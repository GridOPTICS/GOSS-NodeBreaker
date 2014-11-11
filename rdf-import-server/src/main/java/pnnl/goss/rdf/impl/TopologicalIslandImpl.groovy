package pnnl.goss.rdf.impl

import pnnl.goss.rdf.TopologicalBranch
import pnnl.goss.rdf.TopologicalIsland
import pnnl.goss.rdf.TopologicalNode

class TopologicalIslandImpl implements TopologicalIsland {
	
	List<TopologicalBranch> topologicalBranches = new ArrayList<TopologicalBranch>()
	protected setTopologicalBranches(List<TopologicalBranch> topologicalBranch) {this.topologicalBranches = topologicalBranches}

	List<TopologicalNode> topologicalNodes = new ArrayList<TopologicalNode>()
	protected setTopologicalNodes(List<TopologicalNode> topologicalNodes) {this.topologicalNodes = topologicalNodes}
	
}
