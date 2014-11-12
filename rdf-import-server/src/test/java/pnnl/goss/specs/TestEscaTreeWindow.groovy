package pnnl.goss.specs

import pnnl.goss.rdf.EscaType
import pnnl.goss.rdf.RdfModelFixtures
import pnnl.goss.rdf.impl.EscaTreeWindow
import pnnl.goss.rdf.impl.EscaTypes
import pnnl.goss.rdf.server.EscaVocab
import spock.lang.Specification

class TestEscaTreeWindow extends Specification {
	
	def "constructing 4 substation model from the 05730516.pdf paper"() {
		when: "Model is constructed"
			EscaTreeWindow window = new EscaTreeWindow(RdfModelFixtures.get4SubModel())
		then: "EscaType getByResourceType has the correct number of each object type."
			assert window.getEscaTypeMap() != null
			EscaTypes escaTypes = window.getEscaTypeMap()
			
			assert escaTypes.getByResourceType(EscaVocab.ACLINESEGMENT_OBJECT).size() == 5
			assert escaTypes.getByResourceType(EscaVocab.SYNCHRONOUSMACHINE_OBJECT).size() == 2
			assert escaTypes.getByResourceType(EscaVocab.LOADBREAKSWITCH_OBJECT).size() == 2
			assert escaTypes.getByResourceType(EscaVocab.BREAKER_OBJECT).size() == 24
			assert escaTypes.getByResourceType(EscaVocab.TERMINAL_OBJECT).size() == 82
			assert escaTypes.getByResourceType(EscaVocab.CONNECTIVITYNODE_OBJECT).size() == 35
			
		and: "Connectivity nodes are linked correctly."
		
			// Create a node to terminals that are referred to it.
			def cn = [ 0: [0, 1],
				1: [2, 3, 4],
				2: [5, 6, 7],
				3: [8, 9],
				4: [10, 11],
				5: [12, 13, 14],
				6: [15, 16, 17],
				7: [18, 19],
				8: [20, 21],
				9: [22, 23, 24],
				10: [25, 26],
				11: [27, 28],
				12: [29, 30, 31],
				13: [32, 33],
				14: [34, 35],
				15: [36, 37, 38],
				16: [39, 40],
				17: [41, 42], 
				18: [43, 44, 45],
				19: [46, 47],
				20: [48, 49],
				21: [50, 51, 52],
				22: [53, 54],
				23: [55, 56],
				24: [57, 58, 59],
				25: [60, 61],
				26: [62, 63],
				27: [64, 65, 66],
				28: [67, 68, 69],
				29: [70, 71],
				30: [72, 73],
				31: [74, 75, 76],
				32: [77, 78, 79],
				33: [80, 81]] as LinkedHashMap
			
			cn.each{ cnid, items ->
				String cnkey = "Conn$cnid"
				
				assert escaTypes != null
				assert escaTypes.getByResourceType(EscaVocab.CONNECTIVITYNODE_OBJECT).size() > 0
				def cnode = escaTypes.get(cnkey)
				assert cnode != null
				
				items.each{ tid ->
					String tkey = "Term$tid"
					EscaType escaterm = escaTypes.get(tkey)
					println "cnkey=>$cnkey tnkey=>$tkey"
					assert escaterm != null
					assert escaterm in cnode.getRefersToMe()
				}
//				def cnNodes = escaTypes.getByResourceType(EscaVocab.CONNECTIVITYNODE_OBJECT)
//				cnNodes.each{
//					println "NODES: $it"
//					def v = escaTypes.get(it.getIdentifier())
//					assert v != null
//					println v.getIdentifier()
//				}

//				
				
			}
		
			
			
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
