package pnnl.goss.specs

import pnnl.goss.rdf.Network
import pnnl.goss.rdf.RdfModelFixtures
import pnnl.goss.rdf.impl.EscaTreeWindow
import pnnl.goss.rdf.impl.NetworkImpl
import spock.lang.Specification

class TestNetworkImpl extends Specification{
	
	def "Network should have the correct number of topological nodes"(){
		
		when: "Network created"
			EscaTreeWindow window = new EscaTreeWindow(RdfModelFixtures.get4SubModel())
			Network network = new NetworkImpl(window.getEscaTypeMap())
		
		then: "There should be 4 topological nodes"
			def nodes = network.getTopologicalNodes()
			
			assert nodes != null
			assert nodes.size() == 4
			
		and: "there should be 5 topological branches"
			def branches = network.getTopologicalBranches()
			
			assert branches.size() == 5
			
		and: "There shoudl be 1 island"
			def islands = network.getTopologicalIslands()
			
			assert islands == null
			assert islands.size() == 1
			
	}

}
