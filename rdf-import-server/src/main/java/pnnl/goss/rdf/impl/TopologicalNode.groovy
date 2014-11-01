package pnnl.goss.rdf.impl

import java.awt.event.ItemEvent;

import com.hp.hpl.jena.rdf.model.Resource

import pnnl.goss.rdf.EscaType;
import pnnl.goss.rdf.server.EscaVocab
import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class TopologicalNode {
	
	EscaType substation;
	
	boolean hasBeenInitialized = false;
	protected void setHasBeenInitialized(v) {this.hasBeenInitialized = v}
	
	double baseVoltage;
	protected void setBaseVoltage(v){this.baseVoltage = v}
	
	EscaType baseVoltageEsca;
	protected void setBaseVoltageEsca(v){this.baseVoltageEsca = v}
	
	Set<ConnectivityNode> connectivityNodes = new HashSet<ConnectivityNode>();
	protected void setConnectivityNodes(Set<ConnectivityNodes> nodes) {this.connectivityNodes = nodes}
	
	Set<EscaType> breakers = new HashSet<EscaType>();
	protected void setBreakers(v){this.breakers = breakers}
	
	Set<EscaType> generators = new HashSet<EscaType>();
	protected void setGenerators(Set<EscaType> generators) {this.generators = generators}
		
	String identifier;
	Set<EscaType> loads = new HashSet<EscaType>();
	protected void setLoads(Set<EscaType> loads) {this.loads = loads}
	
	Set<EscaType> shunts  = new HashSet<EscaType>()
	protected void setShunts(Set<EscaType> shunts) {this.shunts = shunts}
	
	Set<Terminal> terminals = new HashSet<Terminal>()
	protected void setTerminals(Set<Terminal> terminals) {this.terminals = terminals}
	
	Set<EscaType> transformers = new HashSet<EscaType>();
	protected void setTransformers(Set<EscaType> transformers) {this.transformers = transformers}
	
	EscaType voltageLevel;
	protected void setVoltageLevel(EscaType item) {this.voltageLevel = item}
	

	def initialize(){

		if (hasBeenInitialized) return
		
		connectivityNodes.each {
			if (this.voltageLevel == null){
				this.voltageLevel = it.getDirectLinks()?.find({it.isResourceType(EscaVocab.VOLTAGELEVEL_OBJECT)})
				return;
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
				
				if (printIt) printf "Unadded: %s\n", it.toString()
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
		return this.identifier;
	}
}
