package pnnl.goss.rdf.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.rdf.EscaType;
import pnnl.goss.rdf.InvalidArgumentException;
import pnnl.goss.rdf.Network;
import pnnl.goss.rdf.Terminal;
import pnnl.goss.rdf.TopologicalBranch;
import pnnl.goss.rdf.TopologicalIsland;
import pnnl.goss.rdf.TopologicalNode;
import pnnl.goss.rdf.core.RdfBranch;
import pnnl.goss.rdf.core.RdfBranches;
import pnnl.goss.rdf.core.RdfBuses;
import pnnl.goss.rdf.core.RdfProperty;
import pnnl.goss.rdf.server.EscaVocab;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * A Network is a linking of nodes and edges of a powergrid.
 *
 * @author d3m614
 *
 */
public class NetworkImpl implements Network {

    private static Logger log = LoggerFactory.getLogger(NetworkImpl.class);
    /*
     * Full network of esca types.
     */
    private EscaTypes escaTypes;
    private List<EscaType> substations = new ArrayList<EscaType>();

    private LinkedHashMap<String, EscaType> connectivityNodes = new LinkedHashMap<>();

    private LinkedHashMap<String, TopologicalNodeImpl> topologicalNodes = new LinkedHashMap<>();
    private LinkedHashMap<String, TopologicalIsland> topologicalIslands = new LinkedHashMap<>();

    private LinkedHashMap<String, TopologicalBranch> topologicalBranches = new LinkedHashMap<>();
    private List<String> branchMridList = new ArrayList<>();

    private LinkedHashMap<String, Terminal> terminalLookup = new LinkedHashMap<>();

    public Collection<TopologicalIsland> getTopologicalIslands() {
        return Collections.unmodifiableCollection(topologicalIslands.values());
    }
    
    private void printSelf(Set<String> done, EscaType esca, int tabs){
    	log.debug(getTabs(tabs)+ esca.toString());
    	
    	for(EscaType child: esca.getRefersToMe()){
    		printSelf(done, child, tabs+1);
    	}
    	done.add(esca.getIdentifier());
    }
    
    private String getTabs(int tabs){
    	String data = "";
    	for(int x=0; x<tabs;x++){
    		data += "\t";
    	}
    	return data;
    }

    /**
     * Transforms an <code>EscaTypes</code> structure into a network of
     * TopologicalNodes, TopologicalIslands and TopologicalBranches.
     *
     * After the constructor is finished the TopologicalNodes,
     * TopologicalIslands and TopologicalBranches will be populated.
     *
     * @param escaTypes
     * @throws InvalidArgumentException
     */
    public NetworkImpl(EscaTypes escaTypes) throws InvalidArgumentException {
        log.debug("Creating nework with: " + escaTypes.keySet().size()
                + " elements.");
        log.debug("# subgeo: " + escaTypes.getByResourceType(EscaVocab.SUBGEOGRAPHICALREGION_OBJECT).size());
        
        EscaType substation = escaTypes.get("_8300407115104535728");
        
        Set<String> done = new HashSet<>();
        int tabs = 0;
        printSelf(done, substation, tabs);
    
        System.out.println("CONFORM LOADS");
        done = new HashSet<>();
        tabs = 0;
        for (EscaType et: escaTypes.getByResourceType(EscaVocab.SHUNTCOMPENSATOR_OBJECT)){
        	printSelf(done, et, tabs);
        }
        
        done = new HashSet<>();
        tabs = 0;
        for (EscaType et: escaTypes.getByResourceType(EscaVocab.SYNCHRONOUSMACHINE_OBJECT)){
        	printSelf(done, et, tabs);
        }
        
        log.debug("# Substations: " + escaTypes.getByResourceType(EscaVocab.SUBSTATION_OBJECT).size());
        done = new HashSet<>();
        tabs = 0;
        for (EscaType et: escaTypes.getByResourceType(EscaVocab.SUBSTATION_OBJECT)){
        	printSelf(done, et, tabs);
        }
        
        log.debug("# AREAS: "+escaTypes.getByResourceType(EscaVocab.HOSTCONTROLAREA_OBJECT));
        done = new HashSet<>();
        tabs = 0;
        for (EscaType et: escaTypes.getByResourceType(EscaVocab.HOSTCONTROLAREA_OBJECT)){
        	printSelf(done, et, tabs);
        }
        
//        
//        log.debug("# AcLineSegments: "
//                + escaTypes.getByResourceType(EscaVocab.ACLINESEGMENT_OBJECT).size());
//        log.debug("# TransformerWinding: "
//                + escaTypes
//                        .getByResourceType(EscaVocab.TRANSFORMERWINDING_OBJECT).size());
//        log.debug("# Substations: "
//                + escaTypes.getByResourceType(EscaVocab.SUBSTATION_OBJECT).size());
//        log.debug("# ConnectivityNodes: "
//                + escaTypes.getByResourceType(EscaVocab.CONNECTIVITYNODE_OBJECT).size());
//        log.debug("# Terminals: "
//                + escaTypes.getByResourceType(EscaVocab.TERMINAL_OBJECT).size());
//        log.debug("# Breakers: "
//                + escaTypes.getByResourceType(EscaVocab.BREAKER_OBJECT).size());

        this.escaTypes = escaTypes;


        // Preload connectivity nodes
        for(EscaType t: escaTypes.getByResourceType(EscaVocab.CONNECTIVITYNODE_OBJECT)){
            connectivityNodes.put(t.getMrid(), t);
        }

        // Build TopologicalNodes up
        this.buildTopology();
        //this.buildTopoIslandsNew();
        this.buildBranches();

//        for(TopologicalNode t: this.topologicalNodes.values()){
//            log.debug(t.getIdentifier());
//            for(Terminal tt: t.getTerminals()){
//                log.debug("\t"+ tt);
//            }
//            for(ConnectivityNode cn: ((TopologicalNodeImpl)t).getConnectivityNodes()){
//                log.debug("\t"+ cn);
//                for(EscaType t2: cn.getTerminals()){
//                    log.debug("\t\t"+ t2);
//                }
//            }
//        }
    }
    
    @SuppressWarnings("unchecked")
	private void buildBranches() throws InvalidArgumentException {
    	RdfBranches branches = new RdfBranches();
    	
    	
    	for(TopologicalNodeImpl tn: topologicalNodes.values()){
    		for(Terminal t: tn.getTerminals()){
    			log.debug("tn has: " + t);
    			if (t.hasDirectLink(EscaVocab.ACLINESEGMENT_OBJECT)) {
    				Collection<EscaType> conLineSeg = t.getDirectLinkedResources(EscaVocab.ACLINESEGMENT_OBJECT);
    				
    				if (conLineSeg.size() > 1){
    					log.error("More than one ac connection?");
    				}
    				
    				EscaType ac = conLineSeg.iterator().next();
    				
    				Collection<EscaType> otherTerminals = ac.getRefersToMe(EscaVocab.TERMINAL_OBJECT, t);
    				
    				if(otherTerminals.size() > 1){
    					log.error("More than one???");
    				}
    				
    				EscaType os = otherTerminals.iterator().next();
    				
    				List<TopologicalNode> nodes = new ArrayList<>();
    				nodes.add(((Terminal)t).getTopologicalNode());
    				nodes.add(((Terminal)os).getTopologicalNode());
    				
    				RdfBranch b = new RdfBranch();
    				
    				RdfProperty prop = new RdfProperty();
    				
    				prop = new RdfProperty();
    				prop.setDataType(String.class.getName());
    				prop.setName("Identifier");
    				prop.setValue(ac.getIdentifier());
    				b.set("Identifier", prop);
    				
    				prop = new RdfProperty();
    				prop.setDataType(EscaType.class.getName());
    				prop.setName("PowerTransferEquipment");
    				prop.setValue(ac);
    				b.set("PowerTransferEquipment", prop);
    				
    				prop = new RdfProperty();
    				prop.setDataType(List.class.getName());
    				prop.setName("TopologicalNodes");
    				prop.setValue(nodes);    				    				
    				b.set("TopologicalNodes", prop);
    				branches.add(b);
    				
    				List<Terminal> terminals = new ArrayList<>();
    				terminals.add(t);
    				terminals.add((Terminal) os);

    				// Switch to terms now.
    				prop = new RdfProperty();
    				prop.setDataType(List.class.getName());
    				prop.setName("Terminals");
    				prop.setValue(terminals);
    				b.set("Terminals", prop);
    				
    				branches.add(b);
    				
    			}
    			else if (t.hasDirectLink(EscaVocab.TRANSFORMERWINDING_OBJECT)){
    				
    				Collection<EscaType> otherSideTrxm = findOtherSideTransformer(t);
    				
    				List<TopologicalNode> nodes = new ArrayList<>();
    				List<Terminal> terminals = new ArrayList<>();
    				terminals.add(t);
    				nodes.add(((Terminal)t).getTopologicalNode());
    				for(EscaType a: otherSideTrxm){
    					for(EscaType term: a.getRefersToMe(EscaVocab.TERMINAL_OBJECT)){
    						terminals.add((Terminal) term);
    						if (!nodes.contains(((Terminal)term).getTopologicalNode())){
    							nodes.add(((Terminal)term).getTopologicalNode());
    						}
    					}    					
    				}
    				
    				RdfBranch b = new RdfBranch();
    				
    				RdfProperty prop = new RdfProperty();
    				prop.setDataType(String.class.getName());
    				prop.setName("Identifier");
    				prop.setValue(t.getIdentifier());
    				b.set("Identifier", prop);
    				
    				prop = new RdfProperty();
    				prop.setDataType(EscaType.class.getName());
    				prop.setName("PowerTransferEquipment");
    				prop.setValue(t.getDirectLinkedResources(EscaVocab.TRANSFORMERWINDING_OBJECT).iterator().next());
    				b.set("PowerTransferEquipment", prop);
    				
    				
    				prop = new RdfProperty();
    				prop.setDataType(List.class.getName());
    				prop.setName("TopologicalNodes");
    				prop.setValue(nodes);
    				
    				b.set("TopologicalNodes", prop);
    				
    				// Switch to terms now.
    				prop = new RdfProperty();
    				prop.setDataType(List.class.getName());
    				prop.setName("Terminals");
    				prop.setValue(terminals);
    				b.set("Terminals", prop);
    				
    				branches.add(b);
    			}
    				
    		}
    		
    	}    	
    	    	
    	log.debug("RDF BRANCHES");
    	for(RdfBranch b: branches){
    		RdfProperty prop = b.get("TopologicalNodes");
    		TopologicalBranch br = new TopologicalBranchImpl(b);
    		for(TopologicalNode tn: br.getNodes()){
    			((TopologicalNodeImpl)tn).addBranch(br);
    		}
    		topologicalBranches.put((String)b.get("Identifier").getValue(), br);
    		
    	}
    	
    	log.debug("// Build branches complete!");
    }
    
    private Collection<EscaType> findOtherSideTransformer(EscaType terminal){
    	
    	// We know we are connected to a transformer windding object.
    	Collection<EscaType> conTransWinding = terminal.getDirectLinkedResources(EscaVocab.TRANSFORMERWINDING_OBJECT);
		
		if (conTransWinding.size() > 1){
			log.error("More than one ac connection?");
		}
		
		EscaType tw = conTransWinding.iterator().next();
		
		// Up to powertransformer and then down to the other transformer.
		EscaType ptxm = tw.getLink(EscaVocab.TRANSFORMERWINDING_MEMBEROF_POWERTRANSFORMER); //.getDirectLinkedResources(EscaVocab.TRANSFORMERWINDING_MEMBEROF_POWERTRANSFORMER);
		
//		if (ptxms.size() > 1){
//			log.error("More than one powertransformer");
//		}
		
		//EscaType ptxm = ptxms.iterator().next();
		    				
		Collection<EscaType> otherTerminals = ptxm.getRefersToMe(EscaVocab.TRANSFORMERWINDING_OBJECT); //.getDirectLinkedResources(EscaVocab.TRANSFORMERWINDING_MEMBEROF_POWERTRANSFORMER,);
		
		List<EscaType> otherSideTerminals = new ArrayList<>();
		
		for (EscaType e: otherTerminals){
			if (!e.getIdentifier().equals(terminal.getIdentifier())){
				otherSideTerminals.add(e);
			}
		}
		
		if (otherSideTerminals.size() > 1){
			log.error("Lots of terminals refer to powertransformer.");
		}
		
		return otherSideTerminals;
		
		
	
    }

//    private void buildTopoIslands() throws InvalidArgumentException{
//        TopologicalIslandImpl island = new TopologicalIslandImpl();
//
//        for(EscaType ac :
//                this.escaTypes.getByResourceType(
//                        EscaVocab.ACLINESEGMENT_OBJECT)){
//            TopologicalBranchImpl branch = new TopologicalBranchImpl(ac, terminalLookup);
//            island.addTopologicalBranch(branch);
//
//        }
//
//        for(EscaType pwr :
//            this.escaTypes.getByResourceType(
//                    EscaVocab.POWERTRANSFORMER_OBJECT)){
//            TopologicalBranchImpl branch = new TopologicalBranchImpl(pwr, terminalLookup);
//            island.addTopologicalBranch(branch);
//
//        }
//
//    }

//    private void buildTopoIslandsNew(){
//        log.debug("Building Islands...");
//
//        for(EscaType ac :
//                this.escaTypes.getByResourceType(
//                        EscaVocab.ACLINESEGMENT_OBJECT)){
//            this.branchMridList.add(ac.getIdentifier());
//        }
//
//        for(EscaType xfmr:
//                this.escaTypes.getByResourceType(
//                        EscaVocab.POWERTRANSFORMER_OBJECT)){
//            this.branchMridList.add(xfmr.getIdentifier());
//        }
//
//        log.debug("Branch Listing:");
//        for (String l: branchMridList){
//            log.debug("\t"+l+": "+this.escaTypes.get(l));
//        }
//        log.debug("# Branches: "+ branchMridList.size());
//
//        Set<String> processedNodes = new HashSet<>();
//        Deque<String> unprocessedNodes = new LinkedList<>();
//        Set<String> allNodes = new HashSet<>();
//
//        for (TopologicalNode t: topologicalNodes.values()){
//            unprocessedNodes.add(t.getIdentifier());
//            allNodes.add(t.getIdentifier());
//        }
//
//        Set<Terminal> processedTerminals = new HashSet<>();
//
//        while(unprocessedNodes.size() > 0){
//            TopologicalIslandImpl island = new TopologicalIslandImpl();
//            String islandKey = "ISLAND: "+topologicalIslands.size()+1;
//            island.setIdentifier(islandKey);
//            topologicalIslands.put(islandKey, island);
//
//            TopologicalNodeImpl currentNode = topologicalNodes.get(
//                                                unprocessedNodes.removeFirst());
//
//            island.addTopologyNode(currentNode);
//
//            Deque<Terminal> unprocessedTerminals = new LinkedList<>(
//                                                    currentNode.getTerminals());
//            processedNodes.add(currentNode.getIdentifier());
//
//            while(unprocessedTerminals.size()> 0){
//                Terminal currentTerminal = unprocessedTerminals.removeFirst();
//                Terminal otherEnd = null;
//                boolean isBranch = false;
//
//                if (currentTerminal.hasDirectLink(EscaVocab.ACLINESEGMENT_OBJECT)){
//                    log.debug("yes it has an ac line segment!");
//                    isBranch = true;
//                    for(EscaType t: currentTerminal.getDirectLinkedResources(EscaVocab.ACLINESEGMENT_OBJECT)){
//                        if(!t.getIdentifier().equals(currentTerminal.getIdentifier())){
//                            otherEnd = terminalLookup.get(t.getIdentifier());
//                            break;
//                        }
//                    }
//
//                }
//                else if(currentTerminal.hasDirectLink(EscaVocab.TRANSFORMERWINDING_OBJECT)){
//                    log.debug("yes it has a transformer winding.");
//                    isBranch = true;
//                    // Grab the powertransformer that is related to this
//                    // sides transformer winding
//                    EscaType winding = currentTerminal.getLink(EscaVocab.TERMINAL_CONDUCTINGEQUIPMENT);
//                    EscaType ptxfm = winding.getLink(EscaVocab.TRANSFORMERWINDING_MEMBEROF_POWERTRANSFORMER);
//
//                    for(EscaType otherWinding: ptxfm.getRefersToMe(
//                            EscaVocab.TRANSFORMERWINDING_OBJECT, winding)){
//                        if (terminalLookup.get(otherWinding) != null){
//                            log.debug("Found other!");
//                        }
//
//                    }
//                    EscaType otherWinding = ptxfm.getRefersToMe(
//                            EscaVocab.TRANSFORMERWINDING_OBJECT, winding).iterator().next();
//
//                   EscaType otherEndEsca = otherWinding.getRefersToMe(
//                            EscaVocab.TERMINAL_OBJECT).iterator().next();
//                   otherEnd = terminalLookup.get(otherEndEsca.getIdentifier());
//                }
//
//                processedTerminals.add(currentTerminal);
//
//                if(isBranch){
//                    if (otherEnd != null){
//                        TopologicalNode otherNode = otherEnd.getTopologicalNode();
//                        String otherNodeId = otherNode.getIdentifier();
//
//                        if (!processedNodes.contains(otherNodeId)){
//                            island.addTopologyNode(otherEnd.getTopologicalNode());
//                            for (Terminal t: otherEnd.getTopologicalNode().getTerminals()){
//                                if (!processedTerminals.contains(t)){
//                                    unprocessedTerminals.add(t);
//                                }
//                            }
//                            unprocessedNodes.remove(otherNodeId);
//                            processedNodes.add(otherNodeId);
//                        }
//                    }
//                    else{
//                        log.debug("Other node was null from: " + currentTerminal);
//                    }
//                }
//            }
//        }
//
//        log.debug("# Topo Islands: "+topologicalIslands.size());
//    }

//    private void buildTopoIslandsOld() {
//
//        debugStep("Building Islands");
//        Map<String, Boolean> topologicalNodesProcessStatus = new LinkedHashMap<>();
//
//        for(String k: topologicalNodes.keySet()){
//            topologicalNodesProcessStatus.put(k, false);
//        }
//
//        Map<String, EscaType> terminals = new LinkedHashMap<>();
//        Map<String, Boolean> terminalProcessedStatus = new LinkedHashMap<>();
//
//
//        TopologicalNodeImpl processingNode = null;
//
//        for (String k: topologicalNodes.keySet()){
//            if(topologicalNodesProcessStatus.get(k) == false){
//                processingNode = topologicalNodes.get(k);
//                break;
//            }
//        }
//
//        List otherNodes = new ArrayList();
//        // bag of currently unprocessed terminals.
//
//        TopologicalIslandImpl island = new TopologicalIslandImpl();
//        island.setIdentifier("Island: "+topologicalIslands.size());
//        topologicalIslands.put(island.getIdentifier(), island);
//
//        while (processingNode != null) {
//
//            island.getTopologicalNodes().add(processingNode);
//            ((TopologicalNodeImpl) processingNode).setTopologicalIsland(island);
//
//            // Add the terminals to be processed.
//            for(Terminal t: processingNode.getTerminals()){
//                terminals.put(t.getMrid(), t);
//                terminalProcessedStatus.put(t.getMrid(), false);
//            }
//
//            // Mark the currently procesing node as being processed.
//            topologicalNodesProcessStatus.put(processingNode.getIdentifier(), true);
//
//            EscaType processingTerminal = null;
//
//            for(String k: terminals.keySet()){
//                if (terminalProcessedStatus.get(k)== false){
//                    processingTerminal = terminals.get(k);
//                    break;
//                }
//            }
//
//            while (processingTerminal != null) {
//                debugStep("\tProcessing Terminal: "
//                        + processingTerminal.toString());
//
//                List<EscaType> canidates = new ArrayList<>();
//
//                for (EscaType e : processingTerminal
//                        .getDirectLinkedResources(EscaVocab.TRANSFORMERWINDING_OBJECT)) {
//                    debugStep("\t\tAdding canidate " + e);
//                    canidates.add(e);
//                }
//
//                for (EscaType e : processingTerminal
//                        .getDirectLinkedResources(EscaVocab.ACLINESEGMENT_OBJECT)) {
//                    debugStep("\t\tAdding canidate " + e);
//                    canidates.add(e);
//                }
//
//                if (canidates.size() == 0) {
//                    debugStep("\tNo canidates found");
//                }
//                else{
//                    for (EscaType currentCanidate : canidates) {
//                        TopologicalBranchImpl branch = new TopologicalBranchImpl(null,terminalLookup);
//                        //branch.setTerminalFrom((Terminal) processingTerminal);
//                        branch.setPowerTransferEquipment(currentCanidate);
//                        branch.setIdentifier(currentCanidate.getLiteralValue(EscaVocab.IDENTIFIEDOBJECT_PATHNAME).getString());
//
//                        Collection<EscaType> possibleOtherTerminalList = currentCanidate
//                                .getRefersToMe(EscaVocab.TERMINAL_OBJECT);
//                        debugStep("\t\tCurrent canidate is: " + currentCanidate
//                                + " testing " + possibleOtherTerminalList.size()
//                                + " terminls for the other.");
//
//                        // If a transformer then we have to look up to the
//                        // powertransformer that ownes the winding.
//                        if (currentCanidate
//                                .isResourceType(EscaVocab.TRANSFORMERWINDING_OBJECT)) {
//                            // Grab the powertransformer that is related to this
//                            // sides transformer winding
//                            EscaType ptxfm = currentCanidate
//                                    .getLink(EscaVocab.TRANSFORMERWINDING_MEMBEROF_POWERTRANSFORMER);
//
//                            // Grab the winding on the other side of the
//                            // powertransformer.
//                            Collection<EscaType> otherWindings = ptxfm
//                                    .getRefersToMe(
//                                            EscaVocab.TRANSFORMERWINDING_OBJECT,
//                                            currentCanidate);
//
//                            // TODO Assumes only a single processing here so we may
//                            // need to make this generic in the future.
//                            EscaType otherWinding = otherWindings.iterator().next();
//                            Terminal otherTerm = (Terminal) otherWinding
//                                    .getRefersToMe(EscaVocab.TERMINAL_OBJECT)
//                                    .iterator().next();
////                            branch.setTerminalTo(otherTerm);
//
//                            TopologicalNodeImpl otherNode = (TopologicalNodeImpl) otherTerm
//                                    .getTopologicalNode();
//                            debugStep("\t\tOther Node is: " + otherNode.toString());
//
//                            if (!otherNodes.contains(otherNode) && !topologicalNodesProcessStatus.get(otherNode.getIdentifier())) {
//                                otherNodes.add(otherNode);
//                            }
//
//                            //if (!topologicalNodeItems.wasProcessed(otherNode)) {
//                            //	island.getTopologicalNodes().add(otherNode);
//                            //	topologicalNodeItems.processItem(otherNode);
//                            //	terminalItems.addItemsToProcess(otherNode
//                            //			.getTerminals());
//                            //}
//                        }
//                        // This is an ACLINESEGMENT_OBJECT
//                        else {
//
//                            Terminal otherTerminal = (Terminal) currentCanidate
//                                    .getRefersToMe(EscaVocab.TERMINAL_OBJECT,
//                                            processingTerminal).iterator().next();
//                            TopologicalNodeImpl otherNode = (TopologicalNodeImpl) otherTerminal
//                                    .getTopologicalNode();
////                            branch.setTerminalTo(otherTerminal);
//                            if (!otherNodes.contains(otherNode) && !topologicalNodesProcessStatus.get(otherNode.getIdentifier())) {
//                                otherNodes.add(otherNode);
//                            }
////							if (!topologicalNodeItems.wasProcessed(otherNode)) {
////								debugStep("\t\tAdding other node's terminals");
////								island.getTopologicalNodes().add(otherNode);
////								// topologicalNodeItems.addItemToProcess(otherNode);
////								topologicalNodeItems.processItem(otherNode);
////								for(Terminal t: otherNode.getTerminals()){
////									debugStep("\tTerminal test: " + t.toString());
////								}
////								terminalItems.addItemsToProcess(otherNode
////										.getTerminals());
////
////							}
//                        }
//
//                        if (!containsBranch(branch)){
//                            branchMridList.add(currentCanidate.getMrid());
//                            topologicalBranches.put(branch.getIdentifier(), branch);
//                            island.getTopologicalBranches().add(branch);
//                        }
////						if (!containsBranch(branch.getTerminalFrom(),
////								branch.getTerminalTo())) {
////							topologicalBranches.add(branch);
////							island.getTopologicalBranches().add(branch);
////						}
//                    }
//
//
//                    debugStep("\tDone processing Terminal: " + processingTerminal);
//                }
//
//                terminalProcessedStatus.put(processingTerminal.getMrid(), true);
//                processingTerminal = null;
//                for (String k: terminals.keySet()){
//                    if (terminalProcessedStatus.get(k) == false){
//                        processingTerminal = terminals.get(k);
//                        break;
//                    }
//                }
//            }
//
//            debugStep("Done processing topology node " + processingNode);
//
//            if (otherNodes.size()> 0){
//                processingNode = (TopologicalNodeImpl) otherNodes.get(0);
//                otherNodes.remove(processingNode);
//            }
//            else{
//                for(TopologicalNode n: island.getTopologicalNodes()){
//                    debugStep(n.getSubstationName()+ " "+((TopologicalNodeImpl)n).getBaseVoltage());
//                }
//
//                island = new TopologicalIslandImpl();
//                island.setIdentifier("Island: "+topologicalIslands.size());
//                topologicalIslands.put(island.getIdentifier(), island);
//                processingNode = null;
//                for(String k: topologicalNodes.keySet()){
//                    if (topologicalNodesProcessStatus.get(k)== false){
//                        processingNode = topologicalNodes.get(k);
//                        break;
//                    }
//                }
//            }
//
//            printIsland(island);
//
////			processingNode = (TopologicalNodeImpl) topologicalNodeItems
////					.nextItem();
//        }
//
//        for (TopologicalNode n : topologicalNodes.values()) {
//            TopologicalNodeImpl nImpl = (TopologicalNodeImpl) n;
//
//            if (nImpl.getSubstation() == null) {
//                String nodeString = "";
//                for (ConnectivityNode cn : nImpl.getConnectivityNodes()) {
//                    nodeString += "\n\t" + cn.toString();
//                }
//                log.debug(n.toString()
//                        + " Doesn't have a substation Connectivity Nodes are "
//                        + nodeString);
//            } else {
//                substations.add(((TopologicalNodeImpl) n).getSubstation());
//            }
//        }
//
//    }

    private void printIsland(TopologicalIsland island) {

        debugStep("############################################################################################");
        debugStep("# Topo nodes in island: "
                + island.getTopologicalNodes().size() + " branches: "
                + island.getTopologicalBranches().size());
        // for(TopologicalNode n: island.getTopologicalNodes()){
        // debugStep("Substation in island is: " + n.getSubstationName());
        // }

        for (TopologicalBranch br : island.getTopologicalBranches()) {
            debugStep("branch: " + br.toString());
        }
        // for(EscaType t:
        // escaTypes.getByResourceType(EscaVocab.ACLINESEGMENT_OBJECT)){
        // log.debug(t.toString());
        // for(EscaType t1: t.getRefersToMe(EscaVocab.TERMINAL_OBJECT)){
        // log.debug("\t"+t1.toString());
        // }
        // }
        //
        // for(EscaType t:
        // escaTypes.getByResourceType(EscaVocab.POWERTRANSFORMER_OBJECT)){
        // log.debug(t.toString());
        // for(EscaType t1:
        // t.getRefersToMe(EscaVocab.TRANSFORMERWINDING_OBJECT)){
        // log.debug("\t"+t1.toString());
        // }
        // }
    }

    /**
     * Notes to myself as building the topology. - ConnectivityNodes are
     * directly connected to a VoltageLevel and Substations and referred to by
     * Terminals. Terminals are the only thing that refers to a
     * ConnectivityNode.
     *
     * - If a ConnectivityNode is directly connected to a Substation then it is
     * not directly connected to a VoltageLevel.
     *
     * - If a ConnectivityNode is referred to be a Terminal then it will be
     * referred to ba at least two Terminals.
     *
     * - It seems that if a ConnectivityNode is directly connected to a
     * Substation then it will not be referred to by any Terminals.
     *
     * - It seems that if a ConnectivityNode is directly connected to a
     * VoltageLevel then it will be connected to at least two Terminals.
     *
     * @throws InvalidArgumentException
     */
    private void buildTopology() throws InvalidArgumentException {

        Resource terminalRes = EscaVocab.TERMINAL_OBJECT;
        Resource connectivityNodeRes = EscaVocab.CONNECTIVITYNODE_OBJECT;
        Resource breakerRes = EscaVocab.BREAKER_OBJECT;

        Property switchOpenProp = EscaVocab.SWITCH_NORMALOPEN;

        Map<String, Boolean> connectivityNodeProcessedStatus = new LinkedHashMap<>();

        Map<String, EscaType> terminalItems = new LinkedHashMap<>();
        Map<String, Boolean> terminalProcessedStatus = new LinkedHashMap<>();

        ConnectivityNode processingConnectivityNode  = null;
        
        // Initialize status of all connectivity nodes.
        for (String k: connectivityNodes.keySet()) {
            connectivityNodeProcessedStatus.put(k, false);
            // Start processing with the first node in the keyset.
            if (processingConnectivityNode == null){
            	processingConnectivityNode = (ConnectivityNode)connectivityNodes.get(k);
            }
        }

        log.debug("Building Topology");
        
        // Define a new node/bus
        TopologicalNodeImpl topologicalNode = new TopologicalNodeImpl();
        topologicalNode.setIdentifier("T" + topologicalNodes.size());
        topologicalNodes.put(topologicalNode.getIdentifier(), topologicalNode);
        debugStep("Creating new topology node "
                + topologicalNode.getIdentifier());
        
        // Add the connectivity node to the topological node.
        topologicalNode.getConnectivityNodes().add(processingConnectivityNode);


        while (processingConnectivityNode != null) {

            debugStep("\tProcessing ", processingConnectivityNode);

            // Add terminals connected to the connectivityNode to the list of
            // to be processed terminals.
            for(EscaType z: processingConnectivityNode.getTerminalsAsEscaType()){
                if (!terminalItems.containsKey(z.getMrid())){
                    terminalItems.put(z.getMrid(), z);
                    terminalProcessedStatus.put(z.getMrid(), false);
                    log.debug("\tTo be processed: "+z);
                }
            }

            // Mark current node as being processed.
            connectivityNodeProcessedStatus.put(processingConnectivityNode.getMrid(), true);

            Terminal processingTerminal = null;
            // Get a terminal to process
            for (String t: terminalItems.keySet()){
                if (terminalProcessedStatus.get(t) == false){
                    processingTerminal = (Terminal)terminalItems.get(t);
                    break;
                }
            }

            while (processingTerminal != null) {
                log.debug("\tProcessing " + processingTerminal.toString());

                // Equipment associated with the terminal.
                Collection<EscaType> equipment = ((TerminalImpl) processingTerminal).getEquipment();
                if (!topologicalNode.hasTerminal(processingTerminal.getMrid())){
                    topologicalNode.getTerminals().add(processingTerminal);
                    ((TerminalImpl)processingTerminal).setTopologicalNode(topologicalNode);
                }

                for (EscaType eq : equipment) {

                    if (eq.hasLiteralProperty(EscaVocab.SWITCH_NORMALOPEN)){
                        debugStep("\t\tFound Equipment: "+ eq);

                        // Switch closed then add the terminals on the other
                        // side.
                        if (!eq.getLiteralValue(switchOpenProp).getBoolean()) {
                            debugStep("\t\tSwitch was closed.");
                            for (EscaType termToAdd: eq.getRefersToMe(EscaVocab.TERMINAL_OBJECT, processingTerminal)){
                                if (!terminalItems.containsKey(termToAdd.getMrid())){
                                    debugStep("\t\tAdding terminal to be processed: "+ termToAdd.toString());
                                    terminalItems.put(termToAdd.getMrid(), termToAdd);
                                    terminalProcessedStatus.put(termToAdd.getMrid(), false);
//                                    if (!topologicalNode.hasTerminal(termToAdd.getMrid())){
//                                        topologicalNode.getTerminals().add((Terminal) termToAdd);
//                                        ((TerminalImpl)termToAdd).setTopologicalNode(topologicalNode);
//                                    }
                                }
                            }
                        }
                        else{
                            debugStep("\t\tSwitch open.");
                        }
                    } else if (eq.isResourceType(EscaVocab.BUSBARSECTION_OBJECT)){
                        // Add terminals that are connected to the bus bar section to
                        // be processed.
                        for(EscaType t: eq.getRefersToMe(EscaVocab.TERMINAL_OBJECT, processingTerminal)){
                            if (!terminalItems.containsKey(t.getMrid())){
                                terminalItems.put(t.getMrid(), t);
                                terminalProcessedStatus.put(t.getMrid(), false);
//                                if (!topologicalNode.hasTerminal(t.getMrid())){
//                                    topologicalNode.getTerminals().add((Terminal) t);
//                                    ((TerminalImpl)t).setTopologicalNode(topologicalNode);
//                                }
                                log.debug("BB adding: "+t);
                            }
                        }
                    }
                    else if (eq.isResourceType(connectivityNodeRes)) {

                        ConnectivityNode node = (ConnectivityNode) eq;
                        if (connectivityNodeProcessedStatus.get(node.getMrid())== false){ // && !nextNodes.contains(node)){
                            debugStep("\t\tFound connectivity node: "+ eq);
//							debugStep("\t\t\tAdding to nextNodes "+node);
//							nextNodes.add(node);

                            for (EscaType termToAdd: node.getTerminalsAsEscaType()){
                                if (!terminalItems.containsKey(termToAdd.getMrid())){
                                    terminalItems.put(termToAdd.getMrid(), termToAdd);
                                    terminalProcessedStatus.put(termToAdd.getMrid(), false);
                                }
                            }

                            topologicalNode.getConnectivityNodes().add(node);
                            // node has been consumed now that we have put all the terminals in the
                            // to be processlist.
                            connectivityNodeProcessedStatus.put(node.getMrid(), true);
                            debugStep("\t\tAdded to topological node" + node);
                        } else{
                            debugStep("\t\tAlready processed or added to nextNode already." + eq);
                        }

                    } else {
                        // debugStep("Other Equipment Found: ", eq);
                    }
                }

                log.debug("\tEnd Processing: "+ processingTerminal);
                // Mark terminal as being processed and get the next one to
                // process.
                terminalProcessedStatus.put(processingTerminal.getMrid(), true);

                processingTerminal = null;
                for (String t: terminalItems.keySet()){
                    if (terminalProcessedStatus.get(t) == false){
                        processingTerminal = (Terminal)terminalItems.get(t);
                        break;
                    }
                }
                if (processingTerminal != null){
                    terminalProcessedStatus.put(processingTerminal.getMrid(), true);
                }
            }

            // mark the current node as processed and get the next one to be
            // processed.
            connectivityNodeProcessedStatus.put(processingConnectivityNode.getMrid(), true);
            processingConnectivityNode = null;
            // Grab the next connectivity node that hasn't been processed.
            for (String t: connectivityNodes.keySet()){
                if (connectivityNodeProcessedStatus.get(t) == false){
                    processingConnectivityNode = (ConnectivityNode)connectivityNodes.get(t);
                    break;
                }
            }

            if (processingConnectivityNode != null){
                topologicalNode = new TopologicalNodeImpl();
                topologicalNode.setIdentifier("T" + topologicalNodes.size());
                topologicalNodes.put(topologicalNode.getIdentifier(), topologicalNode);
                debugStep("Creating new topology node "
                        + topologicalNode.getIdentifier());
                // Add the connectivity node to the topological node.
                topologicalNode.getConnectivityNodes().add(processingConnectivityNode);
            }
        }

        log.debug("# topo ndoes: "+ this.topologicalNodes.size());
        int i=0;
        int ci=0;
        for (EscaType t: terminalItems.values()){
        	Terminal te = (Terminal)t;
        	if (te.getTopologicalNode()== null){
        		i++;
        		log.error(i+" TN is null for "+te);
        	}
        	Collection<EscaType> equipment = ((TerminalImpl) te).getEquipment();
        	for (EscaType eq: equipment){
        		if (eq.isResourceType(EscaVocab.CONNECTIVITYNODE_OBJECT)){
        			ci++;
        			break;
        		}
        	}
        }
        
        log.debug(i + " # of terminals don't have topological nodes");
        log.debug(i + " terminals have connectivity node.");

        for(TopologicalNodeImpl t: topologicalNodes.values()){
            for(ConnectivityNode cn: t.getConnectivityNodes()){
                for(TerminalImpl term: cn.getTerminals()){
                    term.setTopologicalNode(t);

                }
            }

            for (Terminal term: t.getTerminals()){
                ((TerminalImpl)term).setTopologicalNode(t);
                terminalLookup.put(t.getIdentifier(), term);
            }
        }
    }

    private static void debugStep(String message) {
        log.debug(message);
        // System.out.println(message);
    }

    private static void debugStep(String message, List<EscaType> typeList) {
        log.debug(message);
        debugStep(typeList);
    }

    private static void debugStep(List<EscaType> typeList) {
        for (EscaType t : typeList) {
            log.debug(t.toString());
        }
    }

    private static void debugStep(String message, EscaType escaType) {
        if (escaType.getLiteralValue(EscaVocab.IDENTIFIEDOBJECT_PATHNAME) != null) {
            log.debug(message
                    + " "
                    + escaType.getDataType()
                    + " ("
                    + escaType.getMrid()
                    + ") ["
                    + escaType
                            .getLiteralValue(EscaVocab.IDENTIFIEDOBJECT_PATHNAME)
                    + "]");
            // System.out.println(message+" "+escaType.getDataType()+
            // " ("+escaType.getMrid()+
            // ") ["+escaType.getLiteralValue(Esca60Vocab.IDENTIFIEDOBJECT_PATHNAME)+
            // "]");
        } else {
            log.debug(message + " " + escaType.getDataType() + " ("
                    + escaType.getMrid() + ")");
            // System.out.println(message+" "+escaType.getDataType()+
            // " ("+escaType.getMrid()+ ")");
        }
    }

    private void debugSetOfLiterals(Resource resourceType) {
        Set<String> properties = new HashSet<>();
        Collection<EscaType> escaResources = escaTypes
                .getByResourceType(resourceType);
        for (EscaType t : escaResources) {
            for (String v : t.getLiterals().keySet()) {
                properties.add(v);
            }
        }
        log.debug("The set of literals for " + resourceType.getLocalName());
        for (String s : properties) {
            log.debug(s);
        }
    }

    private void debugSetOfReferralConnections(Resource resourceType) {
        Set<String> dataTypeSet = new HashSet<>();
        Collection<EscaType> escaResources = escaTypes
                .getByResourceType(resourceType);
        for (EscaType t : escaResources) {
            for (EscaType v : t.getRefersToMe()) {
                dataTypeSet.add(v.getDataType());
            }
        }
        log.debug("The following datatypes refer to "
                + resourceType.getLocalName());
        for (String s : dataTypeSet) {
            log.debug(s);
        }
    }

    private void debugSetOfDirectConnections(Resource resourceType) {
        Set<String> dataTypeSet = new HashSet<>();
        Collection<EscaType> escaResources = escaTypes
                .getByResourceType(resourceType);
        for (EscaType t : escaResources) {
            for (EscaType v : t.getLinks().values()) {
                dataTypeSet.add(v.getDataType());
            }
        }
        log.debug(resourceType.getLocalName()
                + " is directly connected to the following types: ");
        for (String s : dataTypeSet) {
            log.debug(s);
        }
    }

    private void debugReferralTree(Resource resourceType) {
        Collection<EscaType> escaResources = escaTypes
                .getByResourceType(resourceType);
        for (EscaType t : escaResources) {
            log.debug(t.getDataType() + " " + t.getName()
                    + " is connected directly to: ");
            for (EscaType c : t.getLinks().values()) {
                log.debug(c.getDataType() + " " + c.getName());
            }
            log.debug("IS REFERED TO BY: " + t.getRefersToMe().size()
                    + " FROM: ");
            for (EscaType c : t.getRefersToMe()) {
                log.debug(c.getDataType() + " " + c.getName());
            }
        }
    }

    private boolean containsBranch(TopologicalBranch branch) {
        return branchMridList.contains(((TopologicalBranchImpl)branch).getPowerTransferEquipment().getMrid());
    }

    private boolean containsBranch(EscaType item1, EscaType item2) {
//        for (TopologicalBranch br : topologicalBranches.values()) {
//            if (br.getTerminalFrom().getMrid().equals(item1.getMrid())) {
//                if (br.getTerminalTo().getMrid().equals(item2.getMrid())) {
//                    return true;
//                }
//            } else if (br.getTerminalFrom().getMrid().equals(item2.getMrid())) {
//                if (br.getTerminalTo().getMrid().equals(item1.getMrid())) {
//                    return true;
//                }
//            }
//        }
        return false;
    }

    public Collection<EscaType> getSubstations() {
        return Collections.unmodifiableCollection(substations);
    }

    public Collection<TopologicalNode> getTopologicalNodes() {
        return Collections.unmodifiableCollection(topologicalNodes.values());
    }

    
    public Collection<TopologicalBranch> getTopologicalBranches() {
        return Collections.unmodifiableCollection(topologicalBranches.values());
    }

	@Override
	public RdfBuses getRdfBuses() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RdfBranches getRdfBranches() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EscaType getByMrid(String mrid) {
		return escaTypes.get(mrid);
	}

}
