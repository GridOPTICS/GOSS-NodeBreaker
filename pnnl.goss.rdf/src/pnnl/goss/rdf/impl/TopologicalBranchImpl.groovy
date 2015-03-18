package pnnl.goss.rdf.impl

import pnnl.goss.rdf.EscaType
import pnnl.goss.rdf.InvalidArgumentException;
import pnnl.goss.rdf.Terminal
import pnnl.goss.rdf.TopologicalBranch
import pnnl.goss.rdf.TopologicalIsland
import pnnl.goss.rdf.TopologicalNode
import pnnl.goss.rdf.server.EscaVocab

class TopologicalBranchImpl implements TopologicalBranch {

    TopologicalIsland topologicalIsland

    EscaType powerTransferEquipment
    String identifier

    Terminal terminalPrimary
    Terminal terminalSecondary
    Terminal terminalTertiary

    private foundTerminals

    private populateCorrectTerminal(Terminal t){
        if (terminalPrimary == null){
            terminalPrimary = t
        }
        else if(terminalSecondary == null){
            terminalSecondary = t
        }
        else if(terminalTertiary == null){
            terminalTertiary = t
        }
        else{
            throw new InvalidArgumentException("Invalid # of terminals detected!")
        }
    }

    TopologicalBranchImpl(EscaType branchType, Map<String, Terminal> topoTerminals){

        if (branchType.isResourceType(EscaVocab.ACLINESEGMENT_OBJECT)){
            foundTerminals = branchType.getRefersToMe(EscaVocab.TERMINAL_OBJECT)
        }
        else {

            branchType.getRefersToMe(EscaVocab.TRANSFORMERWINDING_OBJECT).each {
                println it
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

        topoTerminals.each {k, v ->
            println "${k} --> ${v}"
        }

        foundTerminals.each{ t->
            println t
            println t.identifier
            println "contains? "+topoTerminals.containsKey(t.identifier)
            if (topoTerminals.containsKey(t.identifier)){
                populateCorrectTerminal(t)
            }
        }


    }

    String getName(){
        return powerTransferEquipment.getLink(EscaVocab.IDENTIFIEDOBJECT_NAME)
    }

    @Override
    String toString() {
        return terminalFrom.topologicalNode.getSubstationName()+ " <" + terminalFrom.getMrid() + "> " + terminalFrom.getLiteralValue(EscaVocab.IDENTIFIEDOBJECT_PATHNAME) + " -> "+terminalTo.topologicalNode.getSubstationName()+ " <" + terminalTo.getMrid() + "> " + terminalTo.getLiteralValue(EscaVocab.IDENTIFIEDOBJECT_PATHNAME);
    };

}
