package pnnl.goss.rdf.impl;

import java.io.InvalidObjectException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.rdf.EscaType;
import pnnl.goss.rdf.server.EscaVocab;

public class ConnectivityNode extends AbstractEscaType {

	private Terminals terminals;
	private static Logger log = LoggerFactory.getLogger(ConnectivityNode.class);
	private EscaType voltageLevel;
	private EscaType baseVoltage;
	private EscaType substation;
	private double baseVoltageDbl;
	
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
	public Terminals getTerminals(){
		if(terminals == null){
			terminals = new Terminals();
			for(EscaType t: this.getRefersToMe(EscaVocab.TERMINAL_OBJECT)) {
				Terminal tt = (Terminal)t;
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
}
