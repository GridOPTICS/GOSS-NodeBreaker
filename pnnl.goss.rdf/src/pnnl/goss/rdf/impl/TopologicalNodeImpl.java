package pnnl.goss.rdf.impl;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;








import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.rdf.Equipment;
import pnnl.goss.rdf.EscaType;
import pnnl.goss.rdf.Terminal;
import pnnl.goss.rdf.TopologicalBranch;
import pnnl.goss.rdf.TopologicalIsland;
import pnnl.goss.rdf.TopologicalNode;
import pnnl.goss.rdf.server.EscaVocab;

public class TopologicalNodeImpl implements TopologicalNode {

	private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
	
	EscaType substation;
	TopologicalIsland topologicalIsland;
	
	final List<EscaType> transformers = new ArrayList<>();
	final List<TopologicalBranch> branches = new ArrayList<>();
	
	final Set<ConnectivityNode> connectivityNodes = new LinkedHashSet<>();
	final Set<Terminal> terminals = new LinkedHashSet<>();
	final Set<Equipment> equipment = new LinkedHashSet<>();
	
	public void addEquipment(Equipment equipment){
		this.equipment.add(equipment);
	}
	
	public void addConnectivityNode(ConnectivityNode cn){
		this.connectivityNodes.add(cn); 
	}
	
	public void addTerminal(Terminal terminal){
		this.terminals.add(terminal);
	}
	
		
	public Collection<Equipment> getEquipment(){
		return this.equipment;
	}
	
	public void printDebug(PrintStream out){
		out.println("Debug output for TN: "+this.identifier);
		out.println("Connectivity Nodes: ");
		for(ConnectivityNode cn: this.getConnectivityNodes()){
			out.println(cn);
		}
		out.println("Terminals: ");
		for(Terminal t: this.getTerminals()){
			out.println(t);
		}
		out.println("Generation Units: ");
		out.println("Transformers: ");
		
	}
	
	public TopologicalNodeImpl addBranch(TopologicalBranch branch){
		branches.add(branch);
		return this;
	}
	
	public List<TopologicalBranch> getBranches(){
		return branches;
	}

	public String getSubstationName() {
		initialize();
		if (this.substation == null){
			return "NO SUBSTATION FOR TN: "+identifier;
		}
		
		return this.substation.getLiteralValue(
				EscaVocab.IDENTIFIEDOBJECT_PATHNAME).toString();
	}

	public String getSubstationMrid() {
		if (this.substation == null){
			return "NO SUBSTATION FOR TN: "+identifier;
		}
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

	

//	protected void setConnectivityNodes(List<ConnectivityNode> nodes) {
//		this.connectivityNodes = nodes;
//	}
	
	public Collection<ConnectivityNode> getConnectivityNodes(){
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

		if (hasBeenInitialized){
			return;
		}

		for(ConnectivityNode element: connectivityNodes){
			 element.setToplogicalNode(this);	 
		
			 if (this.voltageLevel == null){
				 if (element.hasDirectLink(EscaVocab.VOLTAGELEVEL_OBJECT)){
					 this.voltageLevel = element.getDirectLinkedResources(EscaVocab.VOLTAGELEVEL_OBJECT).iterator().next();
					 
					 
				 }
			 }
			 
			 for(EscaType t: element.getRefersToMe()){
				 if (t.isResourceType(EscaVocab.TERMINAL_OBJECT)){
					 ((TerminalImpl)t).setTopologicalNode(this);
					 terminals.add((Terminal) t);
				 }
			 }
		 }
		 
		 if (voltageLevel == null){
			 //log.error("Voltage level not present for TN: " + this.identifier);
			 hasBeenInitialized = true;
			 return;
		 }
		 
		 /**
		 * VoltageLevels only link to substation and base voltage objects.
		 */
		 for(EscaType t: voltageLevel.getDirectLinks()){
			 if (t.isResourceType(EscaVocab.SUBSTATION_OBJECT)){
				 substation = t;
			 }
			 else if(t.isResourceType(EscaVocab.BASEVOLTAGE_OBJECT)){
				 baseVoltage = t.getLiteralValue(EscaVocab.BASEVOLTAGE_NOMINALVOLTAGE).getDouble();
				 baseVoltageEsca = t;
			 }
		 }
		
		 
		
		 if (substation == null){
			 log.error("No substation present for toponode: " + this.identifier);
		 }
		 
//		 def noPrint = [EscaVocab.CONNECTIVITYNODE_OBJECT,
//		 EscaVocab.TERMINAL_OBJECT, EscaVocab.BREAKER_OBJECT]
//		 voltageLevel?.getRefersToMe()?.each{
//		 if (it.isResourceType(EscaVocab.SHUNTCOMPENSATOR_OBJECT)){
//		 shunts.add(it)
//		 }
//		 else if (it.isResourceType(EscaVocab.TRANSFORMERWINDING_OBJECT)){
//		 transformers.add(it)
//		 }
//		 else if (it.isResourceType(EscaVocab.CONFORMLOAD_OBJECT)){
//		 loads.add(it)
//		 }
//		 else if (it.isResourceType(EscaVocab.SYNCHRONOUSMACHINE_OBJECT)){
//		 generators.add(it)
//		 }
//		 else if (it.isResourceType(EscaVocab.BREAKER_OBJECT)){
//		 breakers.add(it)
//		 }
//		 else if (it.isResourceType(EscaVocab.TERMINAL_OBJECT)){
//		 terminals.add(it)
//		 }
//		 else{
//		 def printIt = true
//		 noPrint.each{ other->
//		 if (it.isResourceType(other)){
//		 printIt = false
//		 }
//		 }
		 

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
		return new ArrayList<Terminal>(terminals);
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	public void setTopologicalIsland(TopologicalIslandImpl island) {
		// TODO Auto-generated method stub
		this.island= island;
	}

	@Override
	public String getTopoNodeType() {
		String nodeType = "";
		// TODO Determine if node is isolated
		// TODO Determine if node is swing
		if (generators.size() > 0){
			nodeType = TOPO_NODE_HAS_GENERATION;
		}
		else { // generators.size() == 0
			nodeType = TOPO_NODE_NO_GENERATION;
		}
		
		return nodeType;
		
	}

	@Override
	public double getNetPInjection() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getNetQInjection() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getVoltage() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getAngle() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getB0ch() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getBch() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getG0ch() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getGch() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getNominalVoltage() {
		// TODO Auto-generated method stub
		return 0;
	}
}
