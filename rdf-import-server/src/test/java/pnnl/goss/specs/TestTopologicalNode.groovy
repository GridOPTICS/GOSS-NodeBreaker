package pnnl.goss.specs
//import pnnl.goss.rdf.TopologicalNode
import pnnl.goss.rdf.impl.ConnectivityNode
import pnnl.goss.rdf.impl.TopologicalNodeImpl
import spock.lang.Specification;

class TestTopologicalNodeImpl extends Specification {
		
	def "When two instances of topological nodes are created, they are equivelent"() {
		
		given: "The creation of two topological nodes with no attributes defined."
		
		TopologicalNodeImpl t1 = new TopologicalNodeImpl()
		TopologicalNodeImpl t2 = new TopologicalNodeImpl()
		
		expect: "The two should equal each other"
		assert t1 == t2
	} 
	
	def "When two instances are changed by identifier"() {
		given: "Two instances with different identifiers"
		
		TopologicalNodeImpl t1 = new TopologicalNodeImpl("identifier": "F1")
		TopologicalNodeImpl t2 = new TopologicalNodeImpl("identifier": "F2")
		
		expect: "The two aren't equal"
		assert t1 != t2
	}
	
	def "When two instances that have the same identifier and the same collections" (){
		given: "Two topoloogy nodes with the same connectivity nodes are equivelent."
		
		ConnectivityNode node = new ConnectivityNode('mrid': 'dadfadfe');
		def nodes = [new ConnectivityNode('mrid': 'dadfadfe'), new ConnectivityNode('mrid': 'dadfa'), new ConnectivityNode('mrid': 'fadfe')]
		
		TopologicalNodeImpl t1 = new TopologicalNodeImpl()
		TopologicalNodeImpl t2 = new TopologicalNodeImpl()
		
		assert t1.connectivityNodes.size() == 0
		assert node != null
		
		t1.connectivityNodes += nodes
		t2.connectivityNodes += nodes
		
		expect: "The two topological nodes are equal"
		assert t1.connectivityNodes == t2.connectivityNodes
		assert t1 == t2 		
	}
	
	def "When two instances that have the same identifier and a different collections" (){
		given: "Two topoloogy nodes with differing connectivity nodes are not equivelant."
		
		ConnectivityNode node = new ConnectivityNode('mrid': 'dadfadfe');
		def nodes = [new ConnectivityNode('mrid': 'dadfadfe'), new ConnectivityNode('mrid': 'dadfa'), new ConnectivityNode('mrid': 'fadfe')]
		def othernodes = [new ConnectivityNode('mrid': 'dad'), new ConnectivityNode('mrid': 'dadfa'), new ConnectivityNode('mrid': 'fadfe')]
		
		TopologicalNodeImpl t1 = new TopologicalNodeImpl()
		TopologicalNodeImpl t2 = new TopologicalNodeImpl()
		
		assert t1.connectivityNodes.size() == 0
		assert node != null
		
		t1.connectivityNodes += nodes
		t2.connectivityNodes += othernodes
		
		expect: "The two topological nodes are equal"
		assert t1.connectivityNodes != t2.connectivityNodes
		assert t1 != t2
	}

}
