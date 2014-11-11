package pnnl.goss.rdf.impl

import groovy.transform.EqualsAndHashCode;
import pnnl.goss.rdf.EscaType
import pnnl.goss.rdf.Terminal
import pnnl.goss.rdf.TopologicalIsland;
import pnnl.goss.rdf.TopologicalNode
import pnnl.goss.rdf.server.EscaVocab

import com.hp.hpl.jena.rdf.model.Resource

@EqualsAndHashCode
class TopologicalNodeImpl implements TopologicalNode{
	
	EscaType substation
	TopologicalIsland topologicalIsland
	
	String getSubstationName(){
		return this.substation?.getLiteralValue(EscaVocab.IDENTIFIEDOBJECT_PATHNAME)
	}
	
	String getSubstationMrid(){
		return this.substation?.getMrid() 
	}
	
	boolean hasBeenInitialized = false;
	protected void setHasBeenInitialized(v) {this.hasBeenInitialized = v}
	
	double baseVoltage;
	protected void setBaseVoltage(v){this.baseVoltage = v}
	
	EscaType baseVoltageEsca;
	protected void setBaseVoltageEsca(v){this.baseVoltageEsca = v}
	
	List<ConnectivityNode> connectivityNodes = []
	protected void setConnectivityNodes(List<ConnectivityNode> nodes) {this.connectivityNodes = nodes}
	
	List<EscaType> breakers = []
	protected void setBreakers(v){this.breakers = breakers}
	
	List<EscaType> generators = [];
	protected void setGenerators(List<EscaType> generators) {this.generators = generators}
		
	String identifier;
	Set<EscaType> loads = [];
	protected void setLoads(Set<EscaType> loads) {this.loads = loads}
	
	List<EscaType> shunts  = []
	protected void setShunts(List<EscaType> shunts) {this.shunts = shunts}
	
	List<Terminal> terminals = []
	protected void setTerminals(List<Terminal> terminals) {this.terminals = terminals}
	
	List<EscaType> transformers = []
	protected void setTransformers(List<EscaType> transformers) {this.transformers = transformers}
	
	EscaType voltageLevel;
	protected void setVoltageLevel(EscaType item) {this.voltageLevel = item}
	
	Boolean hasTerminal(String mrid){
		return terminals.find({it.getMrid() == mrid}) != null
	}

	def initialize(){

		if (hasBeenInitialized) return
		
		connectivityNodes.each { element ->
			element.setToplogicalNode(this)
			
			if (this.voltageLevel == null){
				this.voltageLevel = element.getDirectLinks()?.find({it.isResourceType(EscaVocab.VOLTAGELEVEL_OBJECT)})
			}
			
			element.getRefersToMe().each {
				if (it.isResourceType(EscaVocab.TERMINAL_OBJECT)){
					((TerminalImpl)it).setTopologicalNode(this)
					terminals.add(it)					
				}
			}
		}
		
		/**
		 * VoltageLevels only link to substation and base voltage objects.
		 */
		voltageLevel?.getDirectLinks()?.each {
			if (it.isResourceType(EscaVocab.SUBSTATION_OBJECT)){
				substation = it
			}
			else if(it.isResourceType(EscaVocab.BASEVOLTAGE_OBJECT)){
				baseVoltage = it.getLiteralValue(EscaVocab.BASEVOLTAGE_NOMINALVOLTAGE).getDouble()
				baseVoltageEsca = it
			}
		}
		
		if (substation == null){
			println "No substation presnt!"
		}
		//substation = voltageLevel.getLink(EscaVocab.VOLTAGELEVEL_MEMBEROF_SUBSTATION)
		
		def noPrint = [EscaVocab.CONNECTIVITYNODE_OBJECT, EscaVocab.TERMINAL_OBJECT, EscaVocab.BREAKER_OBJECT]
		voltageLevel?.getRefersToMe()?.each{
			if (it.isResourceType(EscaVocab.SHUNTCOMPENSATOR_OBJECT)){
				shunts.add(it)
			}
			else if (it.isResourceType(EscaVocab.TRANSFORMERWINDING_OBJECT)){
				transformers.add(it)
			}
			else if (it.isResourceType(EscaVocab.CONFORMLOAD_OBJECT)){
				loads.add(it)
			}
			else if (it.isResourceType(EscaVocab.SYNCHRONOUSMACHINE_OBJECT)){
				generators.add(it)
			}
			else if (it.isResourceType(EscaVocab.BREAKER_OBJECT)){
				breakers.add(it)
			}
			else if (it.isResourceType(EscaVocab.TERMINAL_OBJECT)){
				terminals.add(it)
			}
			else{
				def printIt = true
				noPrint.each{ other->
					if (it.isResourceType(other)){
						printIt = false
					}
				}
				
				//if (printIt) printf "Unadded: %s\n", it.toString()
			}
		}
		
		 
//		connectivityNodes.each {
//			it.getDirectLinks().each {
//				printf("\t%s\n", it)
//				if (it.isResourceType(EscaVocab.VOLTAGELEVEL_OBJECT)){
//					this.voltageLevel = it
//				}
//				else if(it.isResourceType(EscaVocab.SYNCHRONOUSMACHINE_OBJECT)){
//					this.generators.add(it)
//				}
//				else if(it.isResourceType(EscaVocab.BASEVOLTAGE_OBJECT)){
//					println "Setting "+it
//					this.baseVoltage = it.getLiteralValue(EscaVocab.BASEVOLTAGE_NOMINALVOLTAGE).getDouble()
//					this.baseVoltageEsca = it
//				}
//			}
////			it.getRefersToMe().each{
////				if(it.isResourceType(EscaVocab.BASEVOLTAGE_OBJECT)){
////					this.baseVoltage = it.getLiteralValue(EscaVocab.BASEVOLTAGE_NOMINALVOLTAGE).getDouble()
////					this.baseVoltageEsca = it
////				}
////			}
//		}	
		hasBeenInitialized = true
	}	
	
	@Override
	public String toString() {
		if (!hasBeenInitialized){
			initialize()
		}
		return this.identifier + " Substation: " +this.getSubstationName() + " <"+getSubstationMrid() + ">";
	}	
}
