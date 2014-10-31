package pnnl.goss.rdf.impl

import java.beans.PropertyChangeListener
import java.util.HashMap;

import pnnl.goss.rdf.EscaType;

class ProcessingItems {
	
	Map<EscaType, Boolean> record = new HashMap<EscaType, Boolean>();
	
	def addItemToProcess(EscaType item){
		if (!record.containsKey(item)){
			record.put(item, false)
		}
	}
	
	def processItem(EscaType item) {
		record[item] = true
	}
	
	def wasProcessed(EscaType item) {
		return record[item] == true
	}
	

}
