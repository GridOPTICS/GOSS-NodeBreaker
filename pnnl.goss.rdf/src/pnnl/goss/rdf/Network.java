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
	
	/**
	 * Allow all of the elements to be looked up on the network based
	 * upon the mrid.
	 * 
	 * @param mrid
	 * @return The esca type returned or null.
	 */
	EscaType getByMrid(String mrid);
	
}
