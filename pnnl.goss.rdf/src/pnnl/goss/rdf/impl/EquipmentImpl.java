package pnnl.goss.rdf.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.rdf.Equipment;
import pnnl.goss.rdf.EscaType;

public class EquipmentImpl extends AbstractEscaType implements Equipment {
	
	private static Logger log = LoggerFactory.getLogger(EquipmentImpl.class);

	public EquipmentImpl(EscaType esca){
		this.resource = esca.getResource();
    	this.dataType = esca.getDataType();
    	this.mrid = esca.getMrid();
		this.initialize();
	}
}
