package pnnl.goss.rdf.impl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import pnnl.goss.rdf.EscaType;
import pnnl.goss.rdf.Terminal;
import pnnl.goss.rdf.TopologicalIsland;
import pnnl.goss.rdf.TopologicalNode;
import pnnl.goss.rdf.server.EscaVocab;

public class TopologicalNodeImpl implements TopologicalNode {

	EscaType substation;
	TopologicalIsland topologicalIsland;

	public String getSubstationName() {
		return this.substation.getLiteralValue(
				EscaVocab.IDENTIFIEDOBJECT_PATHNAME).toString();
	}

	public String getSubstationMrid() {
		return this.substation.getMrid();
	}

	boolean hasBeenInitialized = false;

	protected void setHasBeenInitialized(boolean v) {
		this.hasBeenInitialized = v;
	}

	double baseVoltage;

	protected void setBaseVoltage(double v) {
		this.baseVoltage = v;
	}
	
	public double getBaseVoltage(){
		return this.baseVoltage;
	}

	EscaType baseVoltageEsca;

	protected void setBaseVoltageEsca(EscaType v) {
		this.baseVoltageEsca = v;
	}

	List<ConnectivityNode> connectivityNodes = new ArrayList<>();

	protected void setConnectivityNodes(List<ConnectivityNode> nodes) {
		this.connectivityNodes = nodes;
	}
	
	public List<ConnectivityNode> getConnectivityNodes(){
		return this.connectivityNodes;
	}

	List<EscaType> breakers = new ArrayList<>();

	protected void setBreakers(List<EscaType> v) {
		this.breakers = breakers;
	}

	List<EscaType> generators = new ArrayList<>();

	protected void setGenerators(List<EscaType> generators) {
		this.generators = generators;
	}

	String identifier;
	
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	Set<EscaType> loads = new LinkedHashSet<>();

	protected void setLoads(Set<EscaType> loads) {
		this.loads = loads;
	}

	List<EscaType> shunts = new ArrayList<>();

	protected void setShunts(List<EscaType> shunts) {
		this.shunts = shunts;
	}

	List<Terminal> terminals = new ArrayList<>();

	protected void setTerminals(List<Terminal> terminals) {
		this.terminals = terminals;
	}

	List<EscaType> transformers = new ArrayList<>();

	protected void setTransformers(List<EscaType> transformers) {
		this.transformers = transformers;
	}

	EscaType voltageLevel;
	private TopologicalIslandImpl island;

	protected void setVoltageLevel(EscaType item) {
		this.voltageLevel = item;
	}

	Boolean hasTerminal(String mrid) {
		for (Terminal t : terminals) {
			if (t.getMrid().equals(mrid)) {
				return true;
			}
		}
		return false;
	}

	private void initialize() {

		if (hasBeenInitialized)
			return;

		// for(ConnectivityNode element: connectivityNodes){
		// element.setToplogicalNode(this);
		//
		// if (this.voltageLevel == null){
		// this.voltageLevel =
		// element.getDirectLinks()?.find({it.isResourceType(EscaVocab.VOLTAGELEVEL_OBJECT)})
		// }
		//
		// element.getRefersToMe().each {
		// if (it.isResourceType(EscaVocab.TERMINAL_OBJECT)){
		// ((TerminalImpl)it).setTopologicalNode(this)
		// terminals.add(it)
		// }
		// }
		//
		// }
		//
		// connectivityNodes.each { element ->
		// element.setToplogicalNode(this)
		//
		// if (this.voltageLevel == null){
		// this.voltageLevel =
		// element.getDirectLinks()?.find({it.isResourceType(EscaVocab.VOLTAGELEVEL_OBJECT)})
		// }
		//
		// element.getRefersToMe().each {
		// if (it.isResourceType(EscaVocab.TERMINAL_OBJECT)){
		// ((TerminalImpl)it).setTopologicalNode(this)
		// terminals.add(it)
		// }
		// }
		// }
		//
		// /**
		// * VoltageLevels only link to substation and base voltage objects.
		// */
		// voltageLevel?.getDirectLinks()?.each {
		// if (it.isResourceType(EscaVocab.SUBSTATION_OBJECT)){
		// substation = it
		// }
		// else if(it.isResourceType(EscaVocab.BASEVOLTAGE_OBJECT)){
		// baseVoltage =
		// it.getLiteralValue(EscaVocab.BASEVOLTAGE_NOMINALVOLTAGE).getDouble()
		// baseVoltageEsca = it
		// }
		// }
		//
		// if (substation == null){
		// println "No substation presnt!"
		// }
		// //substation =
		// voltageLevel.getLink(EscaVocab.VOLTAGELEVEL_MEMBEROF_SUBSTATION)
		//
		// def noPrint = [EscaVocab.CONNECTIVITYNODE_OBJECT,
		// EscaVocab.TERMINAL_OBJECT, EscaVocab.BREAKER_OBJECT]
		// voltageLevel?.getRefersToMe()?.each{
		// if (it.isResourceType(EscaVocab.SHUNTCOMPENSATOR_OBJECT)){
		// shunts.add(it)
		// }
		// else if (it.isResourceType(EscaVocab.TRANSFORMERWINDING_OBJECT)){
		// transformers.add(it)
		// }
		// else if (it.isResourceType(EscaVocab.CONFORMLOAD_OBJECT)){
		// loads.add(it)
		// }
		// else if (it.isResourceType(EscaVocab.SYNCHRONOUSMACHINE_OBJECT)){
		// generators.add(it)
		// }
		// else if (it.isResourceType(EscaVocab.BREAKER_OBJECT)){
		// breakers.add(it)
		// }
		// else if (it.isResourceType(EscaVocab.TERMINAL_OBJECT)){
		// terminals.add(it)
		// }
		// else{
		// def printIt = true
		// noPrint.each{ other->
		// if (it.isResourceType(other)){
		// printIt = false
		// }
		// }
		//
		// //if (printIt) printf "Unadded: %s\n", it.toString()
		// }
		// }

		hasBeenInitialized = true;
	}

	@Override
	public String toString() {
		if (!hasBeenInitialized) {
			initialize();
		}
		return this.identifier + " Substation: " + this.getSubstationName()
				+ " <" + getSubstationMrid() + ">";
	}

	@Override
	public List<Terminal> getTerminals() {
		return terminals;
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	public void setTopologicalIsland(TopologicalIslandImpl island) {
		// TODO Auto-generated method stub
		this.island= island;
	}
}
