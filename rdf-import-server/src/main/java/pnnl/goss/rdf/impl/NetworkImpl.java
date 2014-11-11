package pnnl.goss.rdf.impl;

import java.util.ArrayList;
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
import pnnl.goss.rdf.Terminal;
import pnnl.goss.rdf.TopologicalBranch;
import pnnl.goss.rdf.TopologicalIsland;
import pnnl.goss.rdf.TopologicalNode;
import pnnl.goss.rdf.impl.TopologicalNodeImpl;
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

	private static Logger log = LoggerFactory.getLogger(NetworkImpl.class);
	/*
	 * Full network of esca types.
	 */
	private EscaTypes escaTypes;
	private List<EscaType> substations = new ArrayList<EscaType>();
	private List<TopologicalNodeImpl> topologicalNodes = new ArrayList<TopologicalNodeImpl>();
	private List<TopologicalIsland> topologicalIslands = new ArrayList<TopologicalIsland>();

	private List<TopologicalBranch> topologicalBranches = new ArrayList<TopologicalBranch>();

	private ProcessingItems connectivityItems = new ProcessingItems(
			"getIdentifier");
	private ProcessingItems topologicalNodeItems = new ProcessingItems(
			"getIdentifier");

	public List<TopologicalIsland> getTopologicalIslands() {
		return topologicalIslands;
	}

	public NetworkImpl(EscaTypes escaTypes) {
		log.debug("Creating nework with: " + escaTypes.keySet().size()
				+ " elements.");
		log.debug("# AcLineSegments: "
				+ escaTypes.getByResourceType(EscaVocab.ACLINESEGMENT_OBJECT));
		log.debug("# TransformerWinding: "
				+ escaTypes
						.getByResourceType(EscaVocab.TRANSFORMERWINDING_OBJECT));
		log.debug("# Substations: "
				+ escaTypes.getByResourceType(EscaVocab.SUBSTATION_OBJECT));
		this.escaTypes = escaTypes;
		try {

			// Preload connectivity nodes
			connectivityItems.addItemsToProcess(escaTypes
					.getByResourceType(EscaVocab.CONNECTIVITYNODE_OBJECT));

			// Build TopologicalNodes up
			this.buildTopology();

			// Initialize all of the topo nodes so that the properties are
			// properly populated.
			for (TopologicalNodeImpl n : topologicalNodes)
				((TopologicalNodeImpl) n).initialize();

			// Build TopologicalIslands, TopologicalBranches up.
			topologicalNodeItems.addItemsToProcess(topologicalNodes);
			this.buildTopoIslands();

		} catch (InvalidArgumentException e) {
			log.error("Error building topology", e);
		}
	}

	private void buildTopoIslands() {

		debugStep("Building Islands");
		TopologicalNodeImpl processingNode = (TopologicalNodeImpl) topologicalNodeItems
				.nextItem();
		
		for(Terminal t: processingNode.getTerminals()){
			debugStep("Terminal test: " + t.toString());
		}

		// bag of currently unprocessed terminals.
		ProcessingItems terminalItems = new ProcessingItems("getIdentifier");

		while (processingNode != null) {
						
			debugStep("Processing Topology Node: " + processingNode.toString());
			TopologicalIsland island = new TopologicalIslandImpl();
			topologicalIslands.add(island);
			island.getTopologicalNodes().add(processingNode);
			((TopologicalNodeImpl) processingNode).setTopologicalIsland(island);

			terminalItems.addItemsToProcess(processingNode.getTerminals());
			topologicalNodeItems.processItem(processingNode);

			// grab a terminal
			EscaType processingTerminal = (EscaType) terminalItems.nextItem();

			while (processingTerminal != null) {
				debugStep("\tProcessing Terminal: "
						+ processingTerminal.toString());

				List<EscaType> canidates = new ArrayList<>();

				for (EscaType e : processingTerminal
						.getDirectLinkedResources(EscaVocab.TRANSFORMERWINDING_OBJECT)) {
					debugStep("\t\tAdding canidate " + e);
					canidates.add(e);
				}

				for (EscaType e : processingTerminal
						.getDirectLinkedResources(EscaVocab.ACLINESEGMENT_OBJECT)) {
					debugStep("\t\tAdding canidate " + e);
					canidates.add(e);
				}

				if (canidates.size() == 0) {
					debugStep("\tNo canidates found");
				}
				else{
					for (EscaType currentCanidate : canidates) {
						TopologicalBranchImpl branch = new TopologicalBranchImpl();
						branch.setTerminalFrom((Terminal) processingTerminal);
						branch.setPowerTransferEquipment(currentCanidate);
	
						Collection<EscaType> possibleOtherTerminalList = currentCanidate
								.getRefersToMe(EscaVocab.TERMINAL_OBJECT);
						debugStep("\t\tCurrent canidate is: " + currentCanidate
								+ " testing " + possibleOtherTerminalList.size()
								+ " terminls for the other.");
	
						// If a transformer then we have to look up to the
						// powertransformer that ownes the winding.
						if (currentCanidate
								.isResourceType(EscaVocab.TRANSFORMERWINDING_OBJECT)) {
							// Grab the powertransformer that is related to this
							// sides transformer winding
							EscaType ptxfm = currentCanidate
									.getLink(EscaVocab.TRANSFORMERWINDING_MEMBEROF_POWERTRANSFORMER);
	
							// Grab the winding on the other side of the
							// powertransformer.
							Collection<EscaType> otherWindings = ptxfm
									.getRefersToMe(
											EscaVocab.TRANSFORMERWINDING_OBJECT,
											currentCanidate);
	
							// TODO Assumes only a single processing here so we may
							// need to make this generic in the future.
							EscaType otherWinding = otherWindings.iterator().next();
							Terminal otherTerm = (Terminal) otherWinding
									.getRefersToMe(EscaVocab.TERMINAL_OBJECT)
									.iterator().next();
							branch.setTerminalTo(otherTerm);
	
							TopologicalNodeImpl otherNode = (TopologicalNodeImpl) otherTerm
									.getTopologicalNode();
							debugStep("\t\tOther Node is: " + otherNode.toString());
	
							if (!topologicalNodeItems.wasProcessed(otherNode)) {
								island.getTopologicalNodes().add(otherNode);
								topologicalNodeItems.processItem(otherNode);
								terminalItems.addItemsToProcess(otherNode
										.getTerminals());
							}
						}
						// This is an ACLINESEGMENT_OBJECT
						else {
	
							Terminal otherTerminal = (Terminal) currentCanidate
									.getRefersToMe(EscaVocab.TERMINAL_OBJECT,
											processingTerminal).iterator().next();
							TopologicalNodeImpl otherNode = (TopologicalNodeImpl) otherTerminal
									.getTopologicalNode();
							branch.setTerminalTo(otherTerminal);
							if (!topologicalNodeItems.wasProcessed(otherNode)) {
								debugStep("\t\tAdding other node's terminals");
								island.getTopologicalNodes().add(otherNode);
								// topologicalNodeItems.addItemToProcess(otherNode);
								topologicalNodeItems.processItem(otherNode);
								for(Terminal t: otherNode.getTerminals()){
									debugStep("\tTerminal test: " + t.toString());
								}
								terminalItems.addItemsToProcess(otherNode
										.getTerminals());
								
							}
						}
						
						if (!containsBranch(branch.getTerminalFrom(),
								branch.getTerminalTo())) {
							topologicalBranches.add(branch);
							island.getTopologicalBranches().add(branch);
						}
					}
					
	
					debugStep("\tDone processing Terminal: " + processingTerminal);
				}
				terminalItems.processItem(processingTerminal);
				processingTerminal = (EscaType) terminalItems.nextItem();
			}

			printIsland(island);

			debugStep("Done processing topology node " + processingNode);
			processingNode = (TopologicalNodeImpl) topologicalNodeItems
					.nextItem();
		}

		for (TopologicalNode n : topologicalNodes) {
			TopologicalNodeImpl nImpl = (TopologicalNodeImpl) n;

			if (nImpl.getSubstation() == null) {
				String nodeString = "";
				for (ConnectivityNode cn : nImpl.getConnectivityNodes()) {
					nodeString += "\n\t" + cn.toString();
				}
				log.debug(n.toString()
						+ " Doesn't have a substation Connectivity Nodes are "
						+ nodeString);
			} else {
				substations.add(((TopologicalNodeImpl) n).getSubstation());
			}
		}

	}

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

		log.debug("Building Topology");
		// Grab the next connectivity node that hasn't been processed.
		ConnectivityNode processingNode = (ConnectivityNode) connectivityItems
				.nextItem();
		while (processingNode != null) {

			debugStep("Processing ", processingNode);

			// Define a new node/bus
			TopologicalNodeImpl topologicalNode = new TopologicalNodeImpl();
			topologicalNodes.add(topologicalNode);
			topologicalNode.setIdentifier("T" + topologicalNodes.size());
			debugStep("\tCreating new topology node "
					+ topologicalNode.getIdentifier());

			// Add the connectivity node to the topological node.
			topologicalNode.getConnectivityNodes().add(processingNode);

			// Add all of the terminals connected to the currently processing
			// node.
			ProcessingItems terminalItems = new ProcessingItems("getIdentifier");
			terminalItems.addItemsToProcess(processingNode
					.getTerminalsAsEscaType());

			// Get a terminal to process
			Terminal processingTerminal = (Terminal) terminalItems.nextItem();
			while (processingTerminal != null) {
				log.debug("\tProcessing " + processingTerminal.toString());

				// Equipment associated with the terminal.
				Collection<EscaType> equipment = ((TerminalImpl) processingTerminal)
						.getEquipment();

				for (EscaType eq : equipment) {
					if (eq.isResourceType(breakerRes)) {
						debugStep("\t\tFound Breaker: <"
								+ eq.getMrid()
								+ "> "
								+ eq.getLiteralValue(EscaVocab.IDENTIFIEDOBJECT_PATHNAME));

						// Switch closed then add the terminals on the other
						// side.
						if (!eq.getLiteralValue(switchOpenProp).getBoolean()) {
							for (EscaType t : eq.getRefersToMe(terminalRes)) {
								if (t != eq) {
									terminalItems.addItemToProcess(t);
								}
							}
						}
					} else if (eq.isResourceType(connectivityNodeRes)) {
						ConnectivityNode node = (ConnectivityNode) eq;
						connectivityItems.processItem(node);
						terminalItems.addItemsToProcess(node
								.getTerminalsAsEscaType());
						topologicalNode.getConnectivityNodes().add(node);
						debugStep("\t\tAdding: " + node.dataType + " <"
								+ node.mrid + "> " + " to "
								+ topologicalNode.getIdentifier()); // +topologicalNode.getIdentifier());
					} else {
						// debugStep("Other Equipment Found: ", eq);
					}
				}

				// Mark terminal as being processed and get the next one to
				// process.
				terminalItems.processItem(processingTerminal);
				processingTerminal = (Terminal) terminalItems.nextItem();
			}

			// mark the current node as processed and get the next one to be
			// processed.
			connectivityItems.processItem(processingNode);
			processingNode = (ConnectivityNode) connectivityItems.nextItem();
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

	private boolean containsBranch(EscaType item1, EscaType item2) {
		for (TopologicalBranch br : topologicalBranches) {
			if (br.getTerminalFrom().getMrid().equals(item1.getMrid())) {
				if (br.getTerminalTo().getMrid().equals(item2.getMrid())) {
					return true;
				}
			} else if (br.getTerminalFrom().getMrid().equals(item2.getMrid())) {
				if (br.getTerminalTo().getMrid().equals(item1.getMrid())) {
					return true;
				}
			}
		}
		return false;
	}

	public List<EscaType> getSubstations() {
		return substations;
	}

	public List<TopologicalNodeImpl> getTopologicalNodes() {
		return topologicalNodes;
	}

	@Override
	public List<TopologicalBranch> getTopologicalBranches() {
		return topologicalBranches;
	}

}
