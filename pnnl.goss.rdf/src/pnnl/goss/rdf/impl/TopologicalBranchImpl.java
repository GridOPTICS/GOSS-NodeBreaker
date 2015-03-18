package pnnl.goss.rdf.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import pnnl.goss.rdf.EscaType;
import pnnl.goss.rdf.InvalidArgumentException;
import pnnl.goss.rdf.Terminal;
import pnnl.goss.rdf.TopologicalBranch;
import pnnl.goss.rdf.TopologicalIsland;
import pnnl.goss.rdf.server.EscaVocab;

public class TopologicalBranchImpl implements TopologicalBranch {

    TopologicalIsland topologicalIsland;

    EscaType powerTransferEquipment;
    String identifier;

    Terminal terminalPrimary;
    Terminal terminalSecondary;
    Terminal terminalTertiary;

    private Collection<EscaType> foundTerminals;

    public EscaType getPowerTransferEquipment() {
		return powerTransferEquipment;
	}

	public void setPowerTransferEquipment(EscaType powerTransferEquipment) {
		this.powerTransferEquipment = powerTransferEquipment;
	}

	public Collection<EscaType> getFoundTerminals() {
		return foundTerminals;
	}

	public void setFoundTerminals(Collection<EscaType> foundTerminals) {
		this.foundTerminals = foundTerminals;
	}

	public void setTerminalPrimary(Terminal terminalPrimary) {
		this.terminalPrimary = terminalPrimary;
	}

	public void setTerminalSecondary(Terminal terminalSecondary) {
		this.terminalSecondary = terminalSecondary;
	}

	public void setTerminalTertiary(Terminal terminalTertiary) {
		this.terminalTertiary = terminalTertiary;
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

	private void populateCorrectTerminal(Terminal t) throws InvalidArgumentException{
        if (terminalPrimary == null){
            terminalPrimary = t;
        }
        else if(terminalSecondary == null){
            terminalSecondary = t;
        }
        else if(terminalTertiary == null){
            terminalTertiary = t;
        }
        else{
            throw new InvalidArgumentException("Invalid # of terminals detected!");
        }
    }

    TopologicalBranchImpl(EscaType branchType, Map<String, Terminal> topoTerminals) throws InvalidArgumentException{

        if (branchType.isResourceType(EscaVocab.ACLINESEGMENT_OBJECT)){
            foundTerminals = branchType.getRefersToMe(EscaVocab.TERMINAL_OBJECT);
        }
        else {
        	for(EscaType it: branchType.getRefersToMe(EscaVocab.TRANSFORMERWINDING_OBJECT)){
        		System.out.println(it);
        	}
//
//                //println it.getLink(EscaVocab.TRANSFORMERWINDING_WINDINGTYPE)
////                it.properties.each { p, p1 ->
////                    println "${p} -->> ${p1}"
////                }
//                //println it.getLink(EscaVocab.TRANSFORMERWINDING_WINDINGTYPE)
////                if (it.getLink(EscaVocab.TRANSFORMERWINDING_WINDINGTYPE))
////                foundTerminals += it.getRefersToMe(EscaVocab.TERMINAL_OBJECT)
//            }
        }
        
        for(String k: topoTerminals.keySet()){
        	System.out.println(k+" --> "+ topoTerminals.get(k));
        }

        for(EscaType t: foundTerminals){
        	Terminal t1 = (Terminal)t;
        	System.out.println(t);
        	System.out.println(t.getIdentifier());
        	System.out.println("Contains: "+topoTerminals.containsKey(t1.getIdentifier()));
        	if (topoTerminals.containsKey(t1.getIdentifier())){
        		populateCorrectTerminal(t1);
        	}
        	
        }
    }

    public String getName(){
        return powerTransferEquipment.getLink(EscaVocab.IDENTIFIEDOBJECT_NAME).toString();
    }
    
    @Override
    public String toString() {
    	return "TODO Topo Branch!";
    	// TODO Auto-generated method stub
    	//return terminalPri.topologicalNode.getSubstationName()+ " <" + terminalFrom.getMrid() + "> " + terminalFrom.getLiteralValue(EscaVocab.IDENTIFIEDOBJECT_PATHNAME);
    }

	@Override
	public Terminal getTerminalPrimary() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Terminal getTerminalSecondary() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Terminal getTerminalTertiary() {
		// TODO Auto-generated method stub
		return null;
	}

//    @Override
//    pubic String toString() {
//        return terminalFrom.topologicalNode.getSubstationName()+ " <" + terminalFrom.getMrid() + "> " + terminalFrom.getLiteralValue(EscaVocab.IDENTIFIEDOBJECT_PATHNAME) + " -> "+terminalTo.topologicalNode.getSubstationName()+ " <" + terminalTo.getMrid() + "> " + terminalTo.getLiteralValue(EscaVocab.IDENTIFIEDOBJECT_PATHNAME);
//    };

}
