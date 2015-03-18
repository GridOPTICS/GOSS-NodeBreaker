package pnnl.goss.rdf.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RdfBus {

private final Map<String, RdfProperty> properties = new HashMap<String, RdfProperty>();
	
	public RdfBus set(String name, RdfProperty property){
		properties.put(name, property);
		return this;
	}
	
	public RdfProperty get(String name) {
		return properties.get(name);
	}

	
	public Set<String> propertyNames(){
		return properties.keySet();
	}

}
