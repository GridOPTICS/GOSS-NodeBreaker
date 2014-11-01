package pnnl.goss.rdf.impl

import java.util.HashMap;

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
	Map<Object, Boolean> record = new HashMap<Object, Boolean>();
	private void setRecord(Map record){this.record = record}
	private Map getRecord(){return record}
	
	def areAllProcessed(){
		// if all are processed then all should not be false			
		return record.values().every({it == true})
	}
	
	def addItemsToProcess(Collection items){
		items.each {addItemToProcess(it)}
	}
	
	// Add an item to the processable elemnts.
	def addItemToProcess(Object item){
		if (!record.containsKey(item)){
			record.put(item, false)
		}
	}
	
	def processItem(Object item) {
		record[item] = true
	}
	
	Boolean wasProcessed(Object item) {
		return record[item] == true
	}
	
	def nextItem() {
		return record.find { it.value == false }?.key				
	}
}
