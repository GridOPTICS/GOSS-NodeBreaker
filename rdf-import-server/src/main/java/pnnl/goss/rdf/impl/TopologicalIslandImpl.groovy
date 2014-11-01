package pnnl.goss.rdf.impl

import pnnl.goss.rdf.EscaType
import pnnl.goss.rdf.TopologicalBranch
import pnnl.goss.rdf.TopologicalIsland
import pnnl.goss.rdf.TopologicalNode

class TopologicalIslandImpl implements TopologicalIsland {
	
	Set<TopologicalBranch> topologicalBranches = new HashSet<TopologicalBranch>()
	protected setTopologicalBranches(Set<TopologicalBranch> topologicalBranch) {this.topologicalBranches = topologicalBranches}

	Set<TopologicalNode> topologicalNodes = new HashSet<TopologicalNode>()
	protected setTopologicalNodes(Set<TopologicalNode> topologicalNodes) {this.topologicalNodes = topologicalNodes}
	
}
