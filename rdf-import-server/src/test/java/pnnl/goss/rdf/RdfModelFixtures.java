package pnnl.goss.rdf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pnnl.goss.rdf.server.EscaVocab;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.sun.org.apache.xml.internal.security.keys.content.KeyValue;

public class RdfModelFixtures {
	
	public static Model get10TerminalSystem(){
		Model model = ModelFactory.createDefaultModel();
		
		List<Resource> terminals = addTypeToModel(model, EscaVocab.TERMINAL_OBJECT, "Term", 10);
		
		List<Resource> connectivityNodes = addTypeToModel(model, EscaVocab.CONNECTIVITYNODE_OBJECT, "Conn", 4);
	
		Map<Integer, Integer> terminalToConnMap = new HashMap<>();
		terminalToConnMap.put(0, 0);
		terminalToConnMap.put(1, 0);
		terminalToConnMap.put(2, 1);
		terminalToConnMap.put(3, 1);
		terminalToConnMap.put(4, 2);
		terminalToConnMap.put(5, 2);
		terminalToConnMap.put(6, 3);
		terminalToConnMap.put(7, 3);
		terminalToConnMap.put(8, 3);
		terminalToConnMap.put(9, 3);
		
		createLinkProperty(terminalToConnMap, EscaVocab.TERMINAL_CONNECTIVITYNODE, terminals, connectivityNodes);
		
		return model;
	}
	
	/**
	 * Create links on elements in elementsToAddProperty that point to elements in propertyLinks.  The property
	 * will be added according to the mapping passed where each key is an index in propertiesTo and the
	 * value is the index into propertyLinks.
	 * 
	 * @param mapping
	 * @param property
	 * @param elementsToAddProperty
	 * @param propertyLinks
	 */
	private static void createLinkProperty(Map<Integer, Integer> mapping, Property property, List<Resource> elementsToAddProperty, List<Resource> propertyLinks){
		
		for(Map.Entry<Integer, Integer> item: mapping.entrySet()){
			elementsToAddProperty.get(item.getKey()).addProperty(property, propertyLinks.get(item.getValue()));
		}
	}
	

	
	/**
	 * Create x number of resources and add them to the model.  Return a list of the added
	 * resources so that they can be used in other functions by index.
	 * 
	 * @param model
	 * @param resType
	 * @param prefix
	 * @param numItems
	 * @return
	 */
	private static List<Resource> addTypeToModel(Model model, Resource resType, String prefix, int numItems){
		List<Resource> resources = new ArrayList<>();
		
		for(int i=0; i< numItems; i++){
			String id = EscaVocab.URI_ROOT+prefix + i;
			Resource res = model.createResource(id, resType);
			resources.add(res);
			res.addProperty(EscaVocab.IDENTIFIEDOBJECT_NAME, model.createLiteral(prefix+i));				
		}
		
		return resources;
	}
}
