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
	private Map recordToProcessed = [:] as LinkedHashMap
	
	/**
	 * A map from propertykey to index in the elements list. 
	 */
	private Map recordToIndex = [:] as LinkedHashMap
	
	/**
	 * An ordered list of elements.
	 */
	private List elements = []
	
	/**
	 * Keeps track of what function/property should be used to lookup the key on
	 * each of the objects.
	 */
	private String propertyKey;
	
	/**
	 * Construct a processing items object with the specified functionName to be used
	 * as the comparison for equality.
	 * 
	 * @param propertyKey
	 */
	public ProcessingItems(String propertyKey){
		this.propertyKey = propertyKey;
	}
	
	def areAllProcessed(){
		// if all are processed then all should not be false			
		return recordToProcessed.values().every({it == true})
	}

	def addItemsToProcess(Collection items){
		items.each {addItemToProcess(it)}
	}
	
	// Add an item to the processable elemnts.
	def addItemToProcess(def item){
		if (item == null){
			throw new Exception("Trying to add null item!");
		}
		
		def realKey = item."$propertyKey"()
		
		if (!recordToProcessed.containsKey(realKey)){
			recordToIndex[realKey] = elements.size()
			elements.add(item)
			recordToProcessed[realKey] = false
		}
	}
	
	def processItem(Object item) {
		def realKey = item."$propertyKey"()
		
		if (!recordToProcessed.containsKey(realKey)) throw new Exception("Invalid key in processItem")
				
		recordToProcessed[realKey] = true
		elements[recordToIndex[realKey]] = item
	}
	
	Boolean contains(Object item){
		def realKey = item."$propertyKey"()
		return recordToProcessed.containsKey(realKey)
	}
	
	Boolean wasProcessed(Object item) {
		def realKey = item."$propertyKey"()
		
		if (!recordToProcessed.containsKey(realKey)) return false
		
		return recordToProcessed[realKey] == true
	}
	
	def nextItem() {
		def item =  elements.find {
			def realkey = it."$propertyKey"()
			return recordToProcessed[realkey] == false
			}		
		return item	
	}
}
