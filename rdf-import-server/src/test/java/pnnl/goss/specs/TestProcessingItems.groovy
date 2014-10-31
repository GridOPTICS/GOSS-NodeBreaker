package pnnl.goss.specs

import pnnl.goss.rdf.impl.DefaultEscaType;
import pnnl.goss.rdf.impl.ProcessingItems
import spock.lang.Specification;

class TestProcessingItems  extends Specification{

	def "Adding item to processingitems"() {
		ProcessingItems processingItems = new ProcessingItems();
		
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
		ProcessingItems processingItems = new ProcessingItems();
		
		given: "Given items to be processed"
		def items = [DefaultEscaType.construct(null, 'Terminal', 'm1'),
			DefaultEscaType.construct(null, 'Terminal', 'm2'),
			DefaultEscaType.construct(null, 'Terminal', 'm3')]
		
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
}
