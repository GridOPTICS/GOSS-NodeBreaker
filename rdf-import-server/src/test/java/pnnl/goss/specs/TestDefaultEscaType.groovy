package pnnl.goss.specs

import static org.mockito.Mockito.*
import pnnl.goss.rdf.EscaType
import pnnl.goss.rdf.Terminal
import pnnl.goss.rdf.impl.AbstractEscaType
import pnnl.goss.rdf.impl.ConnectivityNode
import pnnl.goss.rdf.impl.DefaultEscaType
import pnnl.goss.rdf.server.EscaVocab
import spock.lang.Specification

import com.hp.hpl.jena.rdf.model.Resource
import com.hp.hpl.jena.rdf.model.Statement

class TestDefaultEscaType extends Specification {
	
	def "when adding refers to me"(){
		when: 
			AbstractEscaType test1 = DefaultEscaType.construct(mock(Resource.class), "test1", "mrid1")
			AbstractEscaType test2 = DefaultEscaType.construct(mock(Resource.class), "test2", "mrid2")
			
			test1.addRefersToMe(test2)			
		then:
			assert test1.refersToMe.size() == 1		
	}
	
	def "when adding direct link"(){
		when:
			AbstractEscaType test1 = DefaultEscaType.construct(mock(Resource.class), "test1", "mrid1")
			AbstractEscaType test2 = DefaultEscaType.construct(mock(Resource.class), "test2", "mrid2")
			
			test1.addDirectLink("testProp", test2)
		then:
			assert test1.getDirectLinks().size() == 1
			assert test1.getDirectLinks().iterator().next() == test2
			assert test1.getLinks().size() == 1
			
			assert test2.getRefersToMe().size() == 1
			assert test2.getRefersToMe().iterator().next() == test1
			
	}
	
	def "when constructing from a generic resource"(){		
		when:
			Resource resource = mock(Resource.class)
			def escaType = DefaultEscaType.construct(resource, "testing", "123mrid")
			
		then:
			assert escaType instanceof AbstractEscaType
			assert escaType.dataType == "testing"
			assert escaType.mrid == "123mrid"			
			assert escaType.getResource() == resource
	}
	
	def "when constructing connectivity node"() {
		when:
			Resource resource = mock(Resource.class)
			when(resource.getLocalName()).thenReturn(EscaVocab.CONNECTIVITYNODE_OBJECT.getLocalName())
			
			def escaType = DefaultEscaType.construct(resource, EscaVocab.CONNECTIVITYNODE_OBJECT.getLocalName(), "mrid1")
		
		then: "The type is an instance of connectivitynode"
			assert escaType instanceof ConnectivityNode
			assert escaType instanceof AbstractEscaType
			
	}
	
	def "when constructing terminal"() {
		when:
			Resource resource = mock(Resource.class)
			when(resource.getLocalName()).thenReturn(EscaVocab.TERMINAL_OBJECT.getLocalName())
			
			def escaType = DefaultEscaType.construct(resource, EscaVocab.TERMINAL_OBJECT.getLocalName(), "mrid1")
		
		then: "The type is an instance of connectivitynode"
			assert escaType instanceof Terminal
			assert escaType instanceof AbstractEscaType
	}	
}
