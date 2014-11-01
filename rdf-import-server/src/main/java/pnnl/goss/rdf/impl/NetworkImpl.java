package pnnl.goss.rdf.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.felix.ipojo.annotations.Provides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.rdf.EscaType;
import pnnl.goss.rdf.InvalidArgumentException;
import pnnl.goss.rdf.Network;
import pnnl.goss.rdf.server.EscaVocab;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * A Network is a linking of nodes and edges of a powergrid.
 * 
 * @author d3m614
 *
 */
@Provides
public class NetworkImpl implements Network {
	
	private static Logger log = LoggerFactory.getLogger(Network.class);
	/*
	 * Full network of esca types.
	 */
	private EscaTypes escaTypes;
	private Set<TopologicalNode> topologicalNodes = new HashSet<TopologicalNode>();
	private Set<TopologicalIsland> topologicalIslands = new HashSet<TopologicalIsland>();
	
	private ProcessingItems connectivityItems = new ProcessingItems();
	private ProcessingItems topologicalNodeItems = new ProcessingItems();
	
	public Set<TopologicalIsland> getTopologicalIslands(){
		return topologicalIslands;
	}

	public Network(EscaTypes escaTypes){
		log.debug("Creating nework with: " + escaTypes.keySet().size() + " elements.");
		this.escaTypes = escaTypes;
		try {
			
			// Preload connectivity nodes
			connectivityItems.addItemsToProcess(escaTypes.getByResourceType(EscaVocab.CONNECTIVITYNODE_OBJECT));
			
			// Build TopologicalNodes up
			this.buildTopology();
			
			// Initialize all of the topo nodes so that the properties are properly populated.
			for(TopologicalNode n: topologicalNodes) n.initialize();
			
			// Build TopologicalIslands, TopologicalBranches up.
			topologicalNodeItems.addItemsToProcess(topologicalNodes);
			this.buildTopoIslands();
			
		} catch (InvalidArgumentException e) {
			log.error("Error building topology", e);
		}
	}
	
	private void buildTopoIslands(){
		
		debugStep("Building Islands");
		
		TopologicalNode processingNode = (TopologicalNode) topologicalNodeItems.nextItem();
		while(processingNode != null){
			debugStep("Processing: "+processingNode.toString());
			TopologicalIsland island = new TopologicalIsland();
			topologicalIslands.add(island);
			topologicalNodeItems.processItem(processingNode);
			
			ProcessingItems terminalItems = new ProcessingItems();
			terminalItems.addItemsToProcess(processingNode.getTerminals());
			Terminal processingTerminal = (Terminal) terminalItems.nextItem();
			while(processingTerminal != null){
				debugStep("Processing: ", processingTerminal);
				for (EscaType ty: processingTerminal.getDirectLinks()){
					
					if (ty.isResourceType(EscaVocab.ACLINESEGMENT_OBJECT)){
						TopologicalBranch branch = new TopologicalBranch();
						branch.setTerminalFrom(processingTerminal);
						branch.setPowerTransferEquipment(ty);
//						for (EscaType tz: ty.getDirectLinks()) {
//							log.debug("Echo: "+tz.toString());
//						}
						for (EscaType tz: ty.getRefersToMe()) {
							if (tz.isResourceType(EscaVocab.TERMINAL_OBJECT) && !processingTerminal.getMrid().equals(tz.getMrid())){
								Terminal otherTerminal = (Terminal)tz;
								TopologicalNode otherNode = otherTerminal.getTopologicalNode();
								
								branch.setTerminalTo((Terminal)tz);		
								if (!topologicalNodeItems.wasProcessed(otherTerminal.getTopologicalNode())){
									island.getTopologicalNodes().add(otherNode);
									topologicalNodeItems.processItem(otherNode);
									terminalItems.addItemsToProcess(otherNode.getTerminals());									
								}
							}
						}
						
						island.getTopologicalBranchs().add(branch);
						//debugStep("Log it: ", ty);
					}
					else{
						log.debug(ty.toString());
					}

					//debugStep(ty.toString());
				}
				
				terminalItems.processItem(processingTerminal);
				processingTerminal = (Terminal) terminalItems.nextItem();
			}
			
			
			
			
			processingNode = (TopologicalNode) topologicalNodeItems.nextItem();
		}
		
		
	}
	
	/**
	 * Notes to myself as building the topology.  
	 * - ConnectivityNodes are directly connected to a VoltageLevel and Substations and 
	 *   referred to by Terminals.  Terminals are the only thing that refers to a 
	 *   ConnectivityNode.
	 *   
	 * - If a ConnectivityNode is directly connected to a Substation then it is not
	 *   directly connected to a VoltageLevel.
	 *   
	 * - If a ConnectivityNode is referred to be a Terminal then it will
	 *   be referred to ba at least two Terminals.
	 *     
	 * - It seems that if a ConnectivityNode is directly connected to a Substation then
	 *   it will not be referred to by any Terminals.
	 *   
	 * - It seems that if a ConnectivityNode is directly connected to a VoltageLevel then
	 *   it will be connected to at least two Terminals.
	 * @throws InvalidArgumentException 
	 */
	private void buildTopology() throws InvalidArgumentException{
		
		Resource terminalRes = EscaVocab.TERMINAL_OBJECT;
		Resource connectivityNodeRes = EscaVocab.CONNECTIVITYNODE_OBJECT;
		Resource breakerRes = EscaVocab.BREAKER_OBJECT;
		
		Property switchOpenProp = EscaVocab.SWITCH_NORMALOPEN;
		
		// Grab the next connectivity node that hasn't been processed.
		ConnectivityNode processingNode = (ConnectivityNode) connectivityItems.nextItem();
		while (processingNode != null){
			
//			debugStep("Processing ",  processingNode);
			
			// Define a new node/bus
			TopologicalNode topologicalNode = new TopologicalNode();
			topologicalNodes.add(topologicalNode);
			topologicalNode.setIdentifier("T"+topologicalNodes.size());
//			debugStep("Creating new topology node " + topologicalNode.getIdentifier());

			// Add the connectivity node to the topological node.
			topologicalNode.getConnectivityNodes().add(processingNode);
						
			// Add all of the terminals connected to the currently processing node.
			ProcessingItems terminalItems = new ProcessingItems();
			terminalItems.addItemsToProcess(processingNode.getTerminalsAsEscaType());
			
			// Get a terminal to process
			Terminal processingTerminal = (Terminal) terminalItems.nextItem();
			while(processingTerminal != null) {
//				debugStep("Processing ",  processingTerminal);
				
				// Equipment associated with the terminal.
				Collection<EscaType> equipment = processingTerminal.getEquipment();
				
				for(EscaType eq: equipment){
					if (eq.isResourceType(breakerRes)){
//						debugStep("Found Breaker: <"+eq.getMrid()+">");
						
						// Switch closed then add the terminals on the other side.
						if (!eq.getLiteralValue(switchOpenProp).getBoolean()){
							for (EscaType t: eq.getRefersToMe(terminalRes)){
								if (t != eq){
									terminalItems.addItemToProcess(t);
								}
							}
						}
					}
					else if(eq.isResourceType(connectivityNodeRes)){
						ConnectivityNode node = (ConnectivityNode)eq;
						connectivityItems.processItem(node);
						terminalItems.addItemsToProcess(node.getTerminalsAsEscaType());
						topologicalNode.getConnectivityNodes().add(node);
//						debugStep("Adding: "+node.dataType + " <"+node.mrid+"> " + " to " + topologicalNode.getIdentifier()); //+topologicalNode.getIdentifier());
					}
					else{
//						debugStep("Other Equipment Found: ", eq);
					}
				}
				
				// Mark terminal as being processed and get the next one to process.
				terminalItems.processItem(processingTerminal);
				processingTerminal = (Terminal) terminalItems.nextItem();				
			}
			
			// mark the current node as processed and get the next one to be processed.
			connectivityItems.processItem(processingNode);
			processingNode = (ConnectivityNode) connectivityItems.nextItem();					
		}
	}
	
	private static void debugStep(String message){
		log.debug(message);
		//System.out.println(message);
	}
	
	private static void debugStep(String message, List<EscaType> typeList){
		log.debug(message);
		debugStep(typeList);
	}
	private static void debugStep(List<EscaType> typeList){
		for(EscaType t:typeList){
			log.debug(t.toString());
		}
	}
	
	private static void debugStep(String message, EscaType escaType){
		if (escaType.getLiteralValue(EscaVocab.IDENTIFIEDOBJECT_PATHNAME) != null){
			log.debug(message+" "+escaType.getDataType()+ " ("+escaType.getMrid()+ ") ["+escaType.getLiteralValue(EscaVocab.IDENTIFIEDOBJECT_PATHNAME)+ "]");
			//System.out.println(message+" "+escaType.getDataType()+ " ("+escaType.getMrid()+ ") ["+escaType.getLiteralValue(Esca60Vocab.IDENTIFIEDOBJECT_PATHNAME)+ "]");
		}
		else{
			log.debug(message+" "+escaType.getDataType()+ " ("+escaType.getMrid()+ ")");
			//System.out.println(message+" "+escaType.getDataType()+ " ("+escaType.getMrid()+ ")");
		}
	}
	
	private void debugSetOfLiterals(Resource resourceType){
		Set<String> properties = new HashSet<>();
		Collection<EscaType> escaResources = escaTypes.getByResourceType(resourceType);
		for(EscaType t: escaResources){
			for(String v: t.getLiterals().keySet()){
				properties.add(v);
			}
		}
		log.debug("The set of literals for "+resourceType.getLocalName());
		for(String s: properties){
			log.debug(s);
		}
	}
	
	private void debugSetOfReferralConnections(Resource resourceType){
		Set<String> dataTypeSet = new HashSet<>();
		Collection<EscaType> escaResources = escaTypes.getByResourceType(resourceType);
		for(EscaType t: escaResources){
			for(EscaType v: t.getRefersToMe()){
				dataTypeSet.add(v.getDataType());
			}
		}
		log.debug("The following datatypes refer to "+resourceType.getLocalName());
		for(String s: dataTypeSet){
			log.debug(s);
		}
	}
	
	private void debugSetOfDirectConnections(Resource resourceType){
		Set<String> dataTypeSet = new HashSet<>();
		Collection<EscaType> escaResources = escaTypes.getByResourceType(resourceType);
		for(EscaType t: escaResources){
			for(EscaType v: t.getLinks().values()){
				dataTypeSet.add(v.getDataType());
			}
		}
		log.debug(resourceType.getLocalName() + " is directly connected to the following types: ");
		for(String s: dataTypeSet){
			log.debug(s);
		}
	}
	
	private void debugReferralTree(Resource resourceType){
		Collection<EscaType> escaResources = escaTypes.getByResourceType(resourceType);
		for(EscaType t: escaResources){
			log.debug(t.getDataType()+" "+t.getName() + " is connected directly to: ");
			for(EscaType c: t.getLinks().values()){
				log.debug(c.getDataType()+ " " + c.getName());
			}
			log.debug("IS REFERED TO BY: " + t.getRefersToMe().size()+ " FROM: ");
			for(EscaType c: t.getRefersToMe()){
				log.debug(c.getDataType()+ " " + c.getName());
			}
		}
	}

	public Set<TopologicalNode> getTopologicalNodes() {
		return topologicalNodes;
	}
}
