package pnnl.goss.specs

import pnnl.goss.rdf.RdfModelFixtures
import pnnl.goss.rdf.impl.EscaTreeWindow
import pnnl.goss.rdf.impl.EscaTypes
import pnnl.goss.rdf.server.EscaVocab
import spock.lang.Specification

class TestEscaTreeWindow extends Specification {
	
	def "constructing object with 10 Terminal system"() {
		when:
			EscaTreeWindow window = new EscaTreeWindow(RdfModelFixtures.get10TerminalSystem())
		then:
			assert window.getEscaTypeMap() != null
			EscaTypes escaTypes = window.getEscaTypeMap()
			assert escaTypes.getByResourceType(EscaVocab.TERMINAL_OBJECT).size() == 10
		
	}

}
