package pnnl.goss.rdf.server;

import java.util.LinkedHashSet;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class ResourceProperty {
	
	private Resource resource;
	private Property property;
	
	private static Set<ResourceProperty> propertySet = new LinkedHashSet<>();
		
	private ResourceProperty(Resource resource, Property property){
		this.resource = resource;
		this.property = property;
	}
	
	public Resource getResource(){
		return resource;
	}
	
	public Property getProperty(){
		return property;
	}
	
	@Override
	public boolean equals(Object obj) {
		ResourceProperty other = (ResourceProperty)obj;
		
		return (other.getResource().equals(resource) && 
				other.getProperty().equals(property));
	}

	public static ResourceProperty create(Resource resource, Property property){
		ResourceProperty prop = new ResourceProperty(resource, property);
		propertySet.add(prop);
		return prop;
	}
}
