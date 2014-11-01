package pnnl.goss.rdf.impl;

import java.io.InvalidObjectException;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.rdf.EscaType;
import pnnl.goss.rdf.server.EscaVocab;

public class Terminal extends AbstractEscaType {
	
	ConnectivityNode connectivityNode;
	private static Logger log = LoggerFactory.getLogger(Terminal.class);
	
	public void setConnectivityNode(ConnectivityNode node) throws InvalidObjectException{
		if(connectivityNode != null){
			throw new InvalidObjectException("ConnectivityNode has already been set!");
		}
		this.connectivityNode = node;
	}
	
	public Collection<EscaType> getEquipment(){
		return getDirectLinks();
	}
	
}
