package pnnl.goss.rdf.server;

import static pnnl.goss.rdf.server.PsseDictionary.*;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.hp.hpl.jena.rdf.model.Property;

/**
 * A collection of one to one mapping from cim to psse and vice versa.
 * 
 * @author C. Allwardt
 *
 */
public class PsseCimTranslations {
	
	private final Map<ResourceProperty, String> cimToPsse = new ConcurrentHashMap<>();
	private final Map<String, ResourceProperty> psseToCim =  new ConcurrentHashMap<>();
	@SuppressWarnings("rawtypes")
	private final Map<String, Class> psseToDataType = new ConcurrentHashMap<>();
	private final Set<String> unMappedPsseProperties = new LinkedHashSet<>();
	private final Set<ResourceProperty> unMappedCimProperties = new LinkedHashSet<>();
	
	public PsseCimTranslations() {
		// Unmapped psse properties
		addUnMappedPsseProperty(CASE_ID);
		addUnMappedPsseProperty(CASE_SBASE);
		addUnMappedPsseProperty(BUS_NUMBER);  // Psse uses Bus_numaber cim uses unique identifiers.
		
		
		
		// 
		
		//addUnMappedCimProperty(cimProperty);
	}
	
	public Collection<String> getMappedPsse(){
		return Collections.unmodifiableCollection(psseToCim.keySet());
	}
	
	public Collection<ResourceProperty> getMappedCim(){
		return Collections.unmodifiableCollection(cimToPsse.keySet());
	}
	
	@SuppressWarnings("rawtypes")
	public void addMapping(ResourceProperty cimProp, String pssePropString, Class dataType){
		if (cimToPsse.containsKey(cimProp)){
			throw new IllegalArgumentException("Cim Key "+ 
					cimProp.getResource().getLocalName() + "."+ cimProp.getProperty().getLocalName() + " already exists.");
		}
		if (psseToCim.containsKey(pssePropString)) {
			throw new IllegalArgumentException("Psse Key "+ pssePropString + " already exists.");
		}
		
		cimToPsse.put(cimProp, pssePropString);
		psseToCim.put(pssePropString, cimProp);
		psseToDataType.put(pssePropString, dataType);
	}
	
	
	public ResourceProperty getCimProperty(String psseProperty){
		return psseToCim.get(psseProperty);
	}
	
	public String getPsseProperty(Property cimProperty){
		return cimToPsse.get(cimProperty);
	}
	
	@SuppressWarnings("rawtypes")
	public Class getDataType(String psseProperty) {
		return psseToDataType.get(psseProperty);		
	}
	
	@SuppressWarnings("rawtypes")
	public Class getDataType(Property cimProperty) {
		return getDataType(getPsseProperty(cimProperty));
	}
	
	public void addUnMappedCimProperty(ResourceProperty cimProperty){
		unMappedCimProperties.add(cimProperty);
	}
	
	public void addUnMappedPsseProperty(String psseProperty){
		unMappedPsseProperties.add(psseProperty);
	}

	public Collection<ResourceProperty> getUnMappedCimProperties(){
		return unMappedCimProperties;
	}
	
	public Collection<String> getUnMappedPsseProperties(){
		return unMappedPsseProperties;
	}
}
