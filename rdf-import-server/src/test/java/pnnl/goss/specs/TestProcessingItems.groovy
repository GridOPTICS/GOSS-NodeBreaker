package pnnl.goss.specs

import pnnl.goss.rdf.EscaType
import pnnl.goss.rdf.impl.DefaultEscaType;
import pnnl.goss.rdf.impl.ProcessingItems
import spock.lang.Specification;

class TestProcessingItems  extends Specification{

	def "Adding item to processingitems"() {
		ProcessingItems processingItems = new ProcessingItems("getIdentifier");
		
		given: "No items have been added"
		def item = DefaultEscaType.construct(null, 'Terminal', 'm1')
		
		when: 'Adding item to processingItems'
		processingItems.addItemToProcess(item)
		
		then: 'item processed to be false'
		assert processingItems.wasProcessed(item) == false
		and: "all items haven't been processed"
		assert processingItems.areAllProcessed() == false
		
		when: 'Processing item'
		processingItems.processItem(item)
		
		then: 'Was processed should be true'
		assert processingItems.wasProcessed(item) == true 
		and: 'All elements should have been processed'
		assert processingItems.areAllProcessed()		
	}
	
	def "Adding multiple itesm to processing items"(){
		ProcessingItems processingItems = new ProcessingItems("getIdentifier");
		
		given: "Given items to be processed"
		def items = [DefaultEscaType.construct(null, 'Terminal', 'm1'),
			DefaultEscaType.construct(null, 'Terminal', 'm2'),
			DefaultEscaType.construct(null, 'Terminal', 'm3')] as List
		
		when: 'Adding three items'
		processingItems.addItemsToProcess(items)
		then: 'areallprocessed is false'
		assert processingItems.areAllProcessed() == false
		when: 'processing an item'
		processingItems.processItem(items.pop())
		then: 'still should have items to processs'
		assert processingItems.areAllProcessed() == false
		when: 'processing next two items'
		processingItems.processItem(items.pop())
		processingItems.processItem(items.pop())
		then: 'All items should be processed'
		processingItems.areAllProcessed() == true			
	}
	
	def "Testing pop of item"() {
		given: "A processing item with two items"
		ProcessingItems processingItems = new ProcessingItems("getIdentifier");
		def items = [DefaultEscaType.construct(null, 'Terminal', 'm1'),
			DefaultEscaType.construct(null, 'Terminal', 'm2')]
		processingItems.addItemsToProcess(items)
		
		when: "nextItem is called"
		def item = processingItems.nextItem()
		assert item != null
		and: "item is marked as processed"
		processingItems.processItem(item)
		then: 'A call to next gives a different item'
		def item2 = processingItems.nextItem() 
		assert item2 != item
		and: 'Are all processed is false'
		assert processingItems.areAllProcessed() == false
		when: "processing second item"
		processingItems.processItem(item2)
		then: 'All items are processed'
		assert processingItems.areAllProcessed() == true
		and: 'A call to next item returns null'
		assert processingItems.nextItem() == null
		
		assert processingItems.nextItem() == null
	}
	
	def "Test order of nextItem"() {
		given: "A processing item with two items"
		ProcessingItems processingItems = new ProcessingItems("getIdentifier");
		def items = [DefaultEscaType.construct(null, 'Terminal', 'm1'),
			DefaultEscaType.construct(null, 'Terminal', 'm2')]
		processingItems.addItemsToProcess(items)
		
		when: "calling next"
		def item = processingItems.nextItem()
		then: "the item0 element is returned"
		assert items[0] == item
		when: 'processed item0 and getting next'
		processingItems.processItem(item)
		item = processingItems.nextItem()
		then: 'The item1 element is returned'
		assert items[1] == item
		
	}
}
