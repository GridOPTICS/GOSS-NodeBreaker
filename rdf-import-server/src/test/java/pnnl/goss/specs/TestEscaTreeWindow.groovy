package pnnl.goss.specs

import pnnl.goss.rdf.EscaType
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
			assert escaTypes.getByResourceType(EscaVocab.CONNECTIVITYNODE_OBJECT).size() == 4
			
			for(int i=0; i<escaTypes.getByResourceType(EscaVocab.CONNECTIVITYNODE_OBJECT).size(); i++){
				String key = "Conn"+i
				EscaType esca = escaTypes.get(key)
				assert esca != null
				if (i > 2){
					assert esca.getRefersToMe().size() == 4
				}
				else{
					assert esca.getRefersToMe().size() == 2
				}
			}
			
			for(int i=0; i<escaTypes.getByResourceType(EscaVocab.TERMINAL_OBJECT).size(); i++){
				String key = "Term"+i
				EscaType esca = escaTypes.get(key)
				assert esca != null
				esca.getDirectLinks().size() == 1
			}
				
	}

}
