package pnnl.goss.rdf.impl;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.rdf.EscaType;
import pnnl.goss.rdf.Terminal;
import pnnl.goss.rdf.TopologicalBranch;
import pnnl.goss.rdf.TopologicalIsland;
import pnnl.goss.rdf.TopologicalNode;
import pnnl.goss.rdf.core.RdfBranch;
import pnnl.goss.rdf.server.EscaVocab;

public class TopologicalBranchImpl implements TopologicalBranch {
	
	final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    TopologicalIsland topologicalIsland;

    EscaType powerTransferEquipment;
    String identifier;

    private List<TopologicalNode> topologicalNodes;
    private List<Terminal> terminals;

    @Override
	public Collection<TopologicalNode> getNodes() {
		return topologicalNodes;
	}
    
    @Override
    public String getType(){
    	if (powerTransferEquipment.isResourceType(EscaVocab.ACLINESEGMENT_OBJECT)){
    		return EscaVocab.ACLINESEGMENT_OBJECT.getLocalName();
    	}
    	else{
    		return EscaVocab.TRANSFORMERWINDING_OBJECT.getLocalName();
    	}
    }
    
    public EscaType getPowerTransferEquipment() {
		return powerTransferEquipment;
	}

	public void setPowerTransferEquipment(EscaType powerTransferEquipment) {
		this.powerTransferEquipment = powerTransferEquipment;
	}

	public TopologicalIsland getTopologicalIsland() {
		return topologicalIsland;
	}

	public void setTopologicalIsland(TopologicalIsland topologicalIsland) {
		this.topologicalIsland = topologicalIsland;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	@SuppressWarnings("unchecked")
	public TopologicalBranchImpl(RdfBranch branchDef) {
		identifier = (String)  branchDef.get("Identifier").getValue();
		topologicalNodes = (List<TopologicalNode>) branchDef.get("TopologicalNodes").getValue();
		terminals = (List<Terminal>) branchDef.get("Terminals").getValue();
		powerTransferEquipment = (EscaType)branchDef.get("PowerTransferEquipment").getValue();
	}

//    TopologicalBranchImpl(EscaType branchType, Map<String, Terminal> topoTerminals) throws InvalidArgumentException{
//
//        if (branchType.isResourceType(EscaVocab.ACLINESEGMENT_OBJECT)){
//            foundTerminals = branchType.getRefersToMe(EscaVocab.TERMINAL_OBJECT);
//        }
//        else if (branchType.isResourceType(EscaVocab.POWERTRANSFORMER_OBJECT)){
//        	Collection<EscaType> transFormers = branchType.getRefersToMe(EscaVocab.TRANSFORMERWINDING_OBJECT);
//        	
//        	for(EscaType tx: transFormers){
//        		log.debug("Testing for transformer: " + tx.getIdentifier());
//        		
//	        	for(Terminal t: topoTerminals.values()){
//	        		if (t.hasDirectLink(EscaVocab.TERMINAL_CONDUCTINGEQUIPMENT)){
//	        			log.debug("Terminal: "+ t.getIdentifier()+ " conducting equipment is: "+ t.getLink(EscaVocab.TERMINAL_CONDUCTINGEQUIPMENT).getIdentifier());
//	        			if (t.getLink(EscaVocab.TERMINAL_CONDUCTINGEQUIPMENT).getIdentifier().equals(tx.getIdentifier())){
//	        				System.out.println("Woot!");
//	        			}
//	        		}
//	        	}
//        	}
//        	
//        	if (foundTerminals == null){
//        		foundTerminals = new ArrayList<>();
//        	}
//        	foundTerminals = branchType.getRefersToMe(EscaVocab.TRANSFORMERWINDING_OBJECT);
//        	
//        }
//        else {
//        	for(EscaType it: branchType.getRefersToMe(EscaVocab.TRANSFORMERWINDING_OBJECT)){
//        		System.out.println(it);
//        	}
////
////                //println it.getLink(EscaVocab.TRANSFORMERWINDING_WINDINGTYPE)
//////                it.properties.each { p, p1 ->
//////                    println "${p} -->> ${p1}"
//////                }
////                //println it.getLink(EscaVocab.TRANSFORMERWINDING_WINDINGTYPE)
//////                if (it.getLink(EscaVocab.TRANSFORMERWINDING_WINDINGTYPE))
//////                foundTerminals += it.getRefersToMe(EscaVocab.TERMINAL_OBJECT)
////            }
//        }
//        
//        for(String k: topoTerminals.keySet()){
//        	System.out.println(k+" --> "+ topoTerminals.get(k));
//        }
//
//        for(EscaType t: foundTerminals){
//        	Terminal t1 = (Terminal)t;
//        	System.out.println(t);
//        	System.out.println(t.getIdentifier());
//        	System.out.println("Contains: "+topoTerminals.containsKey(t1.getIdentifier()));
//        	if (topoTerminals.containsKey(t1.getIdentifier())){
//        		populateCorrectTerminal(t1);
//        	}
//        	
//        }
//    }

    public String getName(){
        return powerTransferEquipment.toString(); //.getLink(EscaVocab.IDENTIFIEDOBJECT_NAME).toString();
    }
    
    @Override
    public String toString() {    	
    	return powerTransferEquipment.toString();
    }

	

//    @Override
//    pubic String toString() {
//        return terminalFrom.topologicalNode.getSubstationName()+ " <" + terminalFrom.getMrid() + "> " + terminalFrom.getLiteralValue(EscaVocab.IDENTIFIEDOBJECT_PATHNAME) + " -> "+terminalTo.topologicalNode.getSubstationName()+ " <" + terminalTo.getMrid() + "> " + terminalTo.getLiteralValue(EscaVocab.IDENTIFIEDOBJECT_PATHNAME);
//    };

}
