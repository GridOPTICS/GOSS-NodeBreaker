package pnnl.goss.rdf.impl;

import java.io.InvalidObjectException;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.rdf.EscaType;
import pnnl.goss.rdf.Terminal
import pnnl.goss.rdf.TopologicalNode
import pnnl.goss.rdf.server.EscaVocab;

public class TerminalImpl extends AbstractEscaType implements Terminal {
	
	private static Logger log = LoggerFactory.getLogger(TerminalImpl.class);
	
	ConnectivityNode connectivityNode;
	TopologicalNode topologicalNode;
	boolean processed;
	
	
	public void setConnectivityNode(ConnectivityNode node) throws InvalidObjectException{
		if(connectivityNode != null){
			throw new InvalidObjectException("ConnectivityNode has already been set!");
		}
		this.connectivityNode = node;
		
	}
	
	public Collection<EscaType> getEquipment(){
		return getDirectLinks();
	}
	
	public String toString(){
		return this.dataType + ": <"+ this.getMrid() + "> " + this.getLiteralValue(EscaVocab.IDENTIFIEDOBJECT_PATHNAME)
	}
}
