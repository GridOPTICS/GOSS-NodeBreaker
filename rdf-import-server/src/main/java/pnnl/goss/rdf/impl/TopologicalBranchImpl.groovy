package pnnl.goss.rdf.impl

import pnnl.goss.rdf.EscaType;
import pnnl.goss.rdf.Terminal
import pnnl.goss.rdf.TopologicalBranch;
import pnnl.goss.rdf.server.EscaVocab;

class TopologicalBranchImpl implements TopologicalBranch {
	
	EscaType powerTransferEquipment
	String identifier
	Terminal terminalFrom
	Terminal terminalTo
	String getName(){
		return powerTransferEquipment.getLink(EscaVocab.IDENTIFIEDOBJECT_NAME)
	}
	
	@Override
	String toString() {
		return terminalFrom.topologicalNode.getSubstationName()+ " <" + terminalFrom.getMrid() + "> " + terminalFrom.getLiteralValue(EscaVocab.IDENTIFIEDOBJECT_PATHNAME) + " -> "+terminalTo.topologicalNode.getSubstationName()+ " <" + terminalTo.getMrid() + "> " + terminalTo.getLiteralValue(EscaVocab.IDENTIFIEDOBJECT_PATHNAME); 
	};
	
}
