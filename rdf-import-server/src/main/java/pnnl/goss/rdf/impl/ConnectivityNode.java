package pnnl.goss.rdf.impl;

import java.io.InvalidObjectException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.rdf.EscaType;
import pnnl.goss.rdf.TopologicalNode;
import pnnl.goss.rdf.server.EscaVocab;

public class ConnectivityNode extends AbstractEscaType {

	private TopologicalNode topologicalNode;
	private Set<TerminalImpl> terminals;
	private static Logger log = LoggerFactory.getLogger(ConnectivityNode.class);
	private EscaType voltageLevel;
	private EscaType baseVoltage;
	private EscaType substation;
	private double baseVoltageDbl;
	
	public TopologicalNode getTopologicalNode(){
		return this.topologicalNode;
	}
	
	public void setToplogicalNode(TopologicalNode topologicalNode){
		this.topologicalNode = topologicalNode;
	}
	
	public EscaType getSubstationRes(){
		return substation;
	}
	
	public EscaType getBaseVoltageRes(){
		setupProperties();
		return voltageLevel;
	}
	public double getBaseVoltage(){
		setupProperties();
		return baseVoltageDbl;
	}
	
	public EscaType getVoltageLevelRes(){
		setupProperties();
		return voltageLevel;
	}
	
	private void setupProperties(){
		if(baseVoltage == null){
			for(EscaType t: getDirectLinks()){
				if (t.isResourceType(EscaVocab.VOLTAGELEVEL_OBJECT)){
					voltageLevel = t;
					baseVoltage = voltageLevel.getLink(EscaVocab.VOLTAGELEVEL_BASEVOLTAGE);
					baseVoltageDbl = baseVoltage.getLiteralValue(EscaVocab.BASEVOLTAGE_NOMINALVOLTAGE).getDouble();
					if (voltageLevel != null){
						substation = voltageLevel.getLink(EscaVocab.VOLTAGELEVEL_MEMBEROF_SUBSTATION);
					}
				}
			}
		}
	}
	
	/**
	 * Lazy load terminals that are connected to this node.
	 * 
	 * @return set of terminals connected to this node.
	 */
	public Set<TerminalImpl> getTerminals(){
		if(terminals == null){
			terminals = new HashSet<TerminalImpl>();
			for(EscaType t: this.getRefersToMe(EscaVocab.TERMINAL_OBJECT)) {
				TerminalImpl tt = (TerminalImpl)t;
				try {
					tt.setConnectivityNode(this);
				} catch (InvalidObjectException e) {
					log.error("Only one cn per terminal!", e);
					e.printStackTrace();
				}
				terminals.add(tt);
			}
		}
		
		return terminals;
	}
	
	@SuppressWarnings("unchecked")
	public Set<EscaType> getTerminalsAsEscaType(){
		return (Set) getTerminals(); // Collections.unmodifiableSet((Set<EscaType>) getTerminals());
	}
}
