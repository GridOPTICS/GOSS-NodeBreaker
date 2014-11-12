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
			EscaTreeWindow window = new EscaTreeWindow(RdfModelFixtures.get4SubModel())
		then:
			assert window.getEscaTypeMap() != null
			EscaTypes escaTypes = window.getEscaTypeMap()
			
			assert escaTypes.getByResourceType(EscaVocab.ACLINESEGMENT_OBJECT).size() == 5
			assert escaTypes.getByResourceType(EscaVocab.SYNCHRONOUSMACHINE_OBJECT).size() == 2
			assert escaTypes.getByResourceType(EscaVocab.LOADBREAKSWITCH_OBJECT).size() == 2
			assert escaTypes.getByResourceType(EscaVocab.BREAKER_OBJECT).size() == 24
			assert escaTypes.getByResourceType(EscaVocab.TERMINAL_OBJECT).size() == 82
			assert escaTypes.getByResourceType(EscaVocab.CONNECTIVITYNODE_OBJECT).size() == 35
			
//			for(int i=0; i<escaTypes.getByResourceType(EscaVocab.CONNECTIVITYNODE_OBJECT).size(); i++){
//				String key = "Conn"+i
//				EscaType esca = escaTypes.get(key)
//				assert esca != null
//				if (i > 2){
//					assert esca.getRefersToMe().size() == 4
//				}
//				else{
//					assert esca.getRefersToMe().size() == 2
//				}
//			}
//			
//			for(int i=0; i<escaTypes.getByResourceType(EscaVocab.TERMINAL_OBJECT).size(); i++){
//				String key = "Term"+i
//				EscaType esca = escaTypes.get(key)
//				assert esca != null
//				esca.getDirectLinks().size() == 1
//			}
				
	}

}
