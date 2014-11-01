package pnnl.goss.rdf.impl

import java.beans.PropertyChangeListener
import java.util.HashMap;

import pnnl.goss.rdf.EscaType;

/**
 * ProcessingItems includes a container of items that are to be processed.  It
 * manages the processing state of the individual items.
 * 
 * @author Craig Allwardt
 *
 */
class ProcessingItems {
	
	/**
	 * Keep a record of what has/hasn't been processed.
	 */
	Map<EscaType, Boolean> record = new HashMap<EscaType, Boolean>();
	
	def areAllProcessed(){
		// if all are processed then all should not be false			
		return record.values().find({it == false}) == null
	}
	
	def addItemsToProcess(List<EscaType> items){
		items.each {addItemToProcess(it)}
	}
	
	// Add an item to the processable elemnts.
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