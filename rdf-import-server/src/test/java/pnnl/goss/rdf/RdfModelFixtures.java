package pnnl.goss.rdf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import pnnl.goss.rdf.server.EscaVocab;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class RdfModelFixtures {
	
	public static Model get4SubModel(){
		Model model = ModelFactory.createDefaultModel();
		
		List<Resource> substations = addTypeToModel(model, EscaVocab.SUBSTATION_OBJECT, "SS",  4);
		List<Resource> terminals = addTypeToModel(model, EscaVocab.TERMINAL_OBJECT, "Term", 82);
		List<Resource> breakers = addTypeToModel(model, EscaVocab.BREAKER_OBJECT, "CB", 24);
		List<Resource> acLineSegment = addTypeToModel(model, EscaVocab.ACLINESEGMENT_OBJECT, "ACLine", 5);
		List<Resource> loads = addTypeToModel(model, EscaVocab.LOADBREAKSWITCH_OBJECT, "L", 2);
		List<Resource> generators = addTypeToModel(model, EscaVocab.SYNCHRONOUSMACHINE_OBJECT, "SM", 2);
		List<Resource> connectivityNodes = addTypeToModel(model, EscaVocab.CONNECTIVITYNODE_OBJECT, "Conn", 34);
		List<Resource> busBars = addTypeToModel(model,  EscaVocab.BUSBARSECTION_OBJECT, "BB", 8);
	
		Map<Integer, Integer> terminalToConnMap = createTermToConnectivityNodeMap();
			
		createLinkProperty(terminalToConnMap, EscaVocab.TERMINAL_CONNECTIVITYNODE, terminals, connectivityNodes);
		
		Map<Integer, Integer> terminalToBrekaerMap = createTermToBreakerMap();
		
		createLinkProperty(terminalToBrekaerMap, EscaVocab.TERMINAL_CONDUCTINGEQUIPMENT, terminals, breakers);
		
		for(Resource res: breakers){
			res.addLiteral(EscaVocab.SWITCH_NORMALOPEN, false);
		}
		
		terminals.get( 0).addProperty(EscaVocab.TERMINAL_CONDUCTINGEQUIPMENT, busBars.get(0));
		terminals.get(10).addProperty(EscaVocab.TERMINAL_CONDUCTINGEQUIPMENT, busBars.get(0));
		
		terminals.get(9).addProperty(EscaVocab.TERMINAL_CONDUCTINGEQUIPMENT, busBars.get(1));
		terminals.get(19).addProperty(EscaVocab.TERMINAL_CONDUCTINGEQUIPMENT, busBars.get(1));
		
		terminals.get(20).addProperty(EscaVocab.TERMINAL_CONDUCTINGEQUIPMENT, busBars.get(2));
		terminals.get(27).addProperty(EscaVocab.TERMINAL_CONDUCTINGEQUIPMENT, busBars.get(2));
		terminals.get(34).addProperty(EscaVocab.TERMINAL_CONDUCTINGEQUIPMENT, busBars.get(2));
		
		terminals.get(26).addProperty(EscaVocab.TERMINAL_CONDUCTINGEQUIPMENT, busBars.get(3));
		terminals.get(33).addProperty(EscaVocab.TERMINAL_CONDUCTINGEQUIPMENT, busBars.get(3));
		terminals.get(40).addProperty(EscaVocab.TERMINAL_CONDUCTINGEQUIPMENT, busBars.get(3));
		
		
		terminals.get(41).addProperty(EscaVocab.TERMINAL_CONDUCTINGEQUIPMENT, busBars.get(4));
		terminals.get(48).addProperty(EscaVocab.TERMINAL_CONDUCTINGEQUIPMENT, busBars.get(4));
		terminals.get(55).addProperty(EscaVocab.TERMINAL_CONDUCTINGEQUIPMENT, busBars.get(4));
		
		terminals.get(47).addProperty(EscaVocab.TERMINAL_CONDUCTINGEQUIPMENT, busBars.get(5));
		terminals.get(54).addProperty(EscaVocab.TERMINAL_CONDUCTINGEQUIPMENT, busBars.get(5));
		terminals.get(61).addProperty(EscaVocab.TERMINAL_CONDUCTINGEQUIPMENT, busBars.get(5));
		
		terminals.get(62).addProperty(EscaVocab.TERMINAL_CONDUCTINGEQUIPMENT, busBars.get(6));
		terminals.get(72).addProperty(EscaVocab.TERMINAL_CONDUCTINGEQUIPMENT, busBars.get(6));
		
		terminals.get(71).addProperty(EscaVocab.TERMINAL_CONDUCTINGEQUIPMENT, busBars.get(7));
		terminals.get(81).addProperty(EscaVocab.TERMINAL_CONDUCTINGEQUIPMENT, busBars.get(7));
		
		// Now the acline segments.
		terminals.get( 6).addProperty(EscaVocab.TERMINAL_CONDUCTINGEQUIPMENT, acLineSegment.get(0));
		terminals.get(51).addProperty(EscaVocab.TERMINAL_CONDUCTINGEQUIPMENT, acLineSegment.get(0));
		
		terminals.get(13).addProperty(EscaVocab.TERMINAL_CONDUCTINGEQUIPMENT, acLineSegment.get(1));
		terminals.get(22).addProperty(EscaVocab.TERMINAL_CONDUCTINGEQUIPMENT, acLineSegment.get(1));
		
		terminals.get(16).addProperty(EscaVocab.TERMINAL_CONDUCTINGEQUIPMENT, acLineSegment.get(2));
		terminals.get(64).addProperty(EscaVocab.TERMINAL_CONDUCTINGEQUIPMENT, acLineSegment.get(2));
		
		terminals.get(29).addProperty(EscaVocab.TERMINAL_CONDUCTINGEQUIPMENT, acLineSegment.get(3));
		terminals.get(72).addProperty(EscaVocab.TERMINAL_CONDUCTINGEQUIPMENT, acLineSegment.get(3));
		
		terminals.get(58).addProperty(EscaVocab.TERMINAL_CONDUCTINGEQUIPMENT, acLineSegment.get(4));
		terminals.get(66).addProperty(EscaVocab.TERMINAL_CONDUCTINGEQUIPMENT, acLineSegment.get(4));
		
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
			res.addProperty(EscaVocab.IDENTIFIEDOBJECT_PATHNAME, model.createLiteral("prefix"+i+" path"));
		}
		
		return resources;
	}
	
	private static Map<Integer, Integer> createTermToBreakerMap(){
		Map<Integer, Integer> map = (Map)Collections.synchronizedMap(new LinkedHashMap<>());
		
		map.put(1,  0);
		map.put(4, 1);
		map.put(7,  2);
		map.put(11, 3);
		map.put(14, 4);
		map.put(17, 5);
		map.put(21, 6);
		map.put(24,  7);
		map.put(28, 8);
		map.put(31, 9);
		map.put(35, 10);
		map.put(38, 11);
		map.put(42,  12);
		map.put(45, 13);
		map.put(49, 14);
		map.put(52, 15);
		map.put(56, 16);
		map.put(59, 17);
		map.put(63, 18);
		map.put(66, 19);
		map.put(69, 20);
		map.put(73, 21);
		map.put(76, 22);
		map.put(79, 23);
		
		Map<Integer, Integer> itemsToAdd = new LinkedHashMap<>();
		for(Integer k: map.keySet()){
			itemsToAdd.put(k+1, map.get(k));
		}
		
		map.putAll(itemsToAdd);

		return map;
	}
	
	/**
	 * Creates a maping from terminal to connectivity node.  For this model there
	 * are either 2 or 3 referencens depending upon whether connected to a circuit breaker
	 * or not.
	 * 
	 * @return A map from terminal index to connectivity node index.
	 */
	private static Map<Integer, Integer> createTermToConnectivityNodeMap(){
		Map<Integer, Integer> termToCnMap = new LinkedHashMap<>();

		// SS1 - Column 1
		termToCnMap.put(0, 0);
		termToCnMap.put(1, 0);
		
		termToCnMap.put(2, 1);
		termToCnMap.put(3, 1);  // Generator 1
		termToCnMap.put(4, 1);
		
		termToCnMap.put(5, 2);
		termToCnMap.put(6, 2);  // AC Line to CN 3 - 22
		termToCnMap.put(7, 2);
		
		termToCnMap.put(8, 3);
		termToCnMap.put(9, 3);

		// SS1 - Column 2		
		termToCnMap.put(10, 4);
		termToCnMap.put(11, 4);
		
		termToCnMap.put(12, 5);
		termToCnMap.put(13, 5);	// AC Line to CN 8 - 10
		termToCnMap.put(14, 5);
		
		termToCnMap.put(15, 6);
		termToCnMap.put(16, 6);	// AC Line to CN 7 - 28
		termToCnMap.put(17, 6);
		
		termToCnMap.put(18, 7);
		termToCnMap.put(19, 7);
		
		// SS2 - Column 1
		termToCnMap.put(20, 8);
		termToCnMap.put(21, 8);
		
		termToCnMap.put(22, 9);
		termToCnMap.put(23, 9);  // AC Line to CN 8 - 10
		termToCnMap.put(24, 9);
		
		termToCnMap.put(25, 10);
		termToCnMap.put(26, 10);
		
		// SS2 - Column 2
		termToCnMap.put(27, 11);		
		termToCnMap.put(28, 11);
		
		termToCnMap.put(29, 12);
		termToCnMap.put(30, 12);  // AC Line to CN 13 - 32
		termToCnMap.put(31, 12);
		
		termToCnMap.put(32, 13);
		termToCnMap.put(33, 13);
		
		// SS2 - Column 3
		termToCnMap.put(34, 14);		
		termToCnMap.put(35, 14);
		
		termToCnMap.put(36, 15);  
		termToCnMap.put(37, 15);  // Generator 2
		termToCnMap.put(38, 15);
		
		termToCnMap.put(39, 16);
		termToCnMap.put(40, 16);
		
		// SS3 - Column 1
		termToCnMap.put(41, 17);		
		termToCnMap.put(42, 17);
		
		termToCnMap.put(43, 18);  
		termToCnMap.put(44, 18);  // Load 1
		termToCnMap.put(45, 18);
		
		termToCnMap.put(46, 19);
		termToCnMap.put(47, 19);
		
		// SS3 - Column 2
		termToCnMap.put(48, 20);
		termToCnMap.put(49, 20);

		termToCnMap.put(50, 21);
		termToCnMap.put(51, 21);	// AC Line 3 - 22
		termToCnMap.put(52, 21);
		
		termToCnMap.put(53, 22);
		termToCnMap.put(54, 22);
		
		// SS3 - Column 3
		termToCnMap.put(55, 23);
		termToCnMap.put(56, 23); 
		
		termToCnMap.put(57, 24);		
		termToCnMap.put(58, 24);  // AC Line 25 - 29 
		termToCnMap.put(59, 24);
		
		termToCnMap.put(60, 25);
		termToCnMap.put(61, 25);
		
		// SS4 - Column 1
		termToCnMap.put(62, 26);
		termToCnMap.put(63, 26);
		
		termToCnMap.put(64, 27);
		termToCnMap.put(65, 27);  // AC Line 7 - 28
		termToCnMap.put(66, 27);
		
		termToCnMap.put(67, 28);		
		termToCnMap.put(68, 28);  // AC Line 25 - 29
		termToCnMap.put(69, 28);

		termToCnMap.put(70, 29);
		termToCnMap.put(71, 29);		
		
		// SS4 - Column 2
		termToCnMap.put(72, 30);	
		termToCnMap.put(73, 30);
		
		termToCnMap.put(74, 31);	
		termToCnMap.put(75, 31);  // AC Line 7 - 29
		termToCnMap.put(76, 31);
		
		termToCnMap.put(77, 32);		
		termToCnMap.put(78, 32);  // Load 2
		termToCnMap.put(79, 32);
		
		termToCnMap.put(80, 33);
		termToCnMap.put(81, 33);
		return termToCnMap;
	}
}
