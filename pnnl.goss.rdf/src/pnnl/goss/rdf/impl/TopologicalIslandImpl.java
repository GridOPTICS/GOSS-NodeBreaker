package pnnl.goss.rdf.impl;

import java.util.ArrayList;
import java.util.List;

import pnnl.goss.rdf.TopologicalBranch;
import pnnl.goss.rdf.TopologicalIsland;
import pnnl.goss.rdf.TopologicalNode;

public class TopologicalIslandImpl implements TopologicalIsland {

	String identifier;

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	List<TopologicalBranch> topologicalBranches = new ArrayList<TopologicalBranch>();

	protected void setTopologicalBranches(
			List<TopologicalBranch> topologicalBranch) {
		this.topologicalBranches = topologicalBranches;
	}

	List<TopologicalNode> topologicalNodes = new ArrayList<TopologicalNode>();

	protected void setTopologicalNodes(List<TopologicalNode> topologicalNodes) {
		this.topologicalNodes = topologicalNodes;
	}

	public void addTopologyNode(TopologicalNode node) {
		topologicalNodes.add(node);
	}

	public void addTopologicalBranch(TopologicalBranch branch) {
		topologicalBranches.add(branch);
	}

	@Override
	public List<TopologicalBranch> getTopologicalBranches() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<TopologicalNode> getTopologicalNodes() {
		// TODO Auto-generated method stub
		return null;
	}

}
