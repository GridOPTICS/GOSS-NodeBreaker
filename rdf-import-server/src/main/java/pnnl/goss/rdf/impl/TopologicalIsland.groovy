package pnnl.goss.rdf.impl

import pnnl.goss.rdf.EscaType
import pnnl.goss.rdf.impl.TopologicalNode

class TopologicalIsland {
	
	Set<TopologicalBranch> topologicalBranchs = new HashSet<TopologicalBranch>()
	protected setTopologicalBranchs(Set<TopologicalBranch> topologicalBranch) {this.topologicalBranch = topologicalBranch}

	Set<TopologicalNode> topologicalNodes = new HashSet<TopologicalNode>()
	protected setTopologicalNodes(Set<TopologicalNode> topologicalNodes) {this.topologicalNodes = topologicalNodes}
	
}
