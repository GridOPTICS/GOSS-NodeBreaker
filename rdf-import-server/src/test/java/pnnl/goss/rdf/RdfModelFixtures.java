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
	
	public static Model get4SubModel(){
		Model model = ModelFactory.createDefaultModel();
		
		List<Resource> substations = addTypeToModel(model, EscaVocab.SUBSTATION_OBJECT, "SS",  4);
		List<Resource> terminals = addTypeToModel(model, EscaVocab.TERMINAL_OBJECT, "Term", 82);
		List<Resource> breakers = addTypeToModel(model, EscaVocab.BREAKER_OBJECT, "CB", 24);
		List<Resource> acLineSegment = addTypeToModel(model, EscaVocab.ACLINESEGMENT_OBJECT, "ACLine", 5);
		List<Resource> loads = addTypeToModel(model, EscaVocab.LOADBREAKSWITCH_OBJECT, "L", 2);
		List<Resource> generators = addTypeToModel(model, EscaVocab.SYNCHRONOUSMACHINE_OBJECT, "SM", 2);
						
		
		List<Resource> connectivityNodes = addTypeToModel(model, EscaVocab.CONNECTIVITYNODE_OBJECT, "Conn", 35);
	
		Map<Integer, Integer> terminalToConnMap = new HashMap<>();

		// SS1 - Column 1
		terminalToConnMap.put(0, 0);
		terminalToConnMap.put(1, 0);
		
		terminalToConnMap.put(2, 1);
		terminalToConnMap.put(3, 1);  // Generator 1
		terminalToConnMap.put(4, 1);
		
		terminalToConnMap.put(5, 2);
		terminalToConnMap.put(6, 2);  // AC Line to CN 3 - 22
		terminalToConnMap.put(7, 2);
		
		terminalToConnMap.put(8, 3);
		terminalToConnMap.put(9, 3);

		// SS1 - Column 2		
		terminalToConnMap.put(10, 4);
		terminalToConnMap.put(11, 4);
		
		terminalToConnMap.put(12, 5);
		terminalToConnMap.put(13, 5);	// AC Line to CN 8 - 10
		terminalToConnMap.put(14, 5);
		
		terminalToConnMap.put(15, 6);
		terminalToConnMap.put(16, 6);	// AC Line to CN 7 - 28
		terminalToConnMap.put(17, 6);
		
		terminalToConnMap.put(18, 7);
		terminalToConnMap.put(19, 7);
		
		// SS2 - Column 1
		terminalToConnMap.put(20, 8);
		terminalToConnMap.put(21, 8);
		
		terminalToConnMap.put(22, 9);
		terminalToConnMap.put(23, 9);  // AC Line to CN 8 - 10
		terminalToConnMap.put(24, 9);
		
		terminalToConnMap.put(25, 10);
		terminalToConnMap.put(26, 10);
		
		// SS2 - Column 2
		terminalToConnMap.put(27, 11);		
		terminalToConnMap.put(28, 11);
		
		terminalToConnMap.put(29, 12);
		terminalToConnMap.put(30, 12);  // AC Line to CN 13 - 32
		terminalToConnMap.put(31, 12);
		
		terminalToConnMap.put(32, 13);
		terminalToConnMap.put(33, 13);
		
		// SS2 - Column 3
		terminalToConnMap.put(34, 14);		
		terminalToConnMap.put(35, 14);
		
		terminalToConnMap.put(36, 15);  
		terminalToConnMap.put(37, 15);  // Generator 2
		terminalToConnMap.put(38, 15);
		
		terminalToConnMap.put(39, 16);
		terminalToConnMap.put(40, 16);
		
		// SS3 - Column 1
		terminalToConnMap.put(41, 17);		
		terminalToConnMap.put(42, 17);
		
		terminalToConnMap.put(43, 18);  
		terminalToConnMap.put(44, 18);  // Load 1
		terminalToConnMap.put(45, 18);
		
		terminalToConnMap.put(46, 19);
		terminalToConnMap.put(47, 19);
		
		// SS3 - Column 2
		terminalToConnMap.put(48, 20);
		terminalToConnMap.put(49, 20);

		terminalToConnMap.put(50, 21);
		terminalToConnMap.put(51, 21);	// AC Line 3 - 22
		terminalToConnMap.put(52, 21);
		
		terminalToConnMap.put(53, 22);
		terminalToConnMap.put(54, 22);
		
		// SS3 - Column 3
		terminalToConnMap.put(55, 23);
		terminalToConnMap.put(56, 23); 
		
		terminalToConnMap.put(57, 24);		
		terminalToConnMap.put(58, 24);  // AC Line 25 - 29 
		terminalToConnMap.put(59, 24);
		
		terminalToConnMap.put(60, 25);
		terminalToConnMap.put(61, 25);
		
		// SS4 - Column 1
		terminalToConnMap.put(62, 26);
		terminalToConnMap.put(63, 26);
		
		terminalToConnMap.put(64, 27);
		terminalToConnMap.put(65, 27);  // AC Line 7 - 28
		terminalToConnMap.put(66, 27);
		
		terminalToConnMap.put(67, 28);		
		terminalToConnMap.put(68, 28);  // AC Line 25 - 29
		terminalToConnMap.put(69, 28);

		terminalToConnMap.put(70, 29);
		terminalToConnMap.put(71, 29);		
		
		// SS4 - Column 2
		terminalToConnMap.put(72, 30);	
		terminalToConnMap.put(73, 30);
		
		terminalToConnMap.put(74, 31);	
		terminalToConnMap.put(75, 31);  // AC Line 7 - 29
		terminalToConnMap.put(76, 31);
		
		terminalToConnMap.put(77, 32);		
		terminalToConnMap.put(78, 32);  // Load 2
		terminalToConnMap.put(79, 32);
		
		terminalToConnMap.put(80, 33);
		terminalToConnMap.put(81, 33);
		
		
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
