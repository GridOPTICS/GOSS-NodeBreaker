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
		
		when: 'Processing item'
		processingItems.processItem(item)
		
		then: 'Was processed should be true'
		assert processingItems.wasProcessed(item) == true 
		
		
		
		
	}
}
