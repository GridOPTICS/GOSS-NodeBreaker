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
	
	private final Map<Property, String> cimToPsse = new ConcurrentHashMap<>();
	private final Map<String, Property> psseToCim =  new ConcurrentHashMap<>();
	@SuppressWarnings("rawtypes")
	private final Map<String, Class> psseToDataType = new ConcurrentHashMap<>();
	private final Set<String> unMappedPsseProperties = new LinkedHashSet<>();
	private final Set<Property> unMappedCimProperties = new LinkedHashSet<>();
	
	public PsseCimTranslations() {
		addUnMappedPsseProperty(CASE_ID);
		
		//addUnMappedCimProperty(cimProperty);
	}
	
	public Collection<String> getMappedPsse(){
		return Collections.unmodifiableCollection(psseToCim.keySet());
	}
	
	public Collection<Property> getMappedCim(){
		return Collections.unmodifiableCollection(cimToPsse.keySet());
	}
	
	@SuppressWarnings("rawtypes")
	public void addMapping(Property cimProp, String pssePropString, Class dataType){
		if (cimToPsse.containsKey(cimProp)){
			throw new IllegalArgumentException("Cim Key "+ cimProp.getLocalName() + " already exists.");
		}
		if (psseToCim.containsKey(pssePropString)) {
			throw new IllegalArgumentException("Psse Key "+ pssePropString + " already exists.");
		}
		
		cimToPsse.put(cimProp, pssePropString);
		psseToCim.put(pssePropString, cimProp);
		psseToDataType.put(pssePropString, dataType);
	}
	
	
	public Property getCimProperty(String psseProperty){
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
	
	public void addUnMappedCimProperty(Property cimProperty){
		unMappedCimProperties.add(cimProperty);
	}
	
	public void addUnMappedPsseProperty(String psseProperty){
		unMappedPsseProperties.add(psseProperty);
	}

	public Collection<Property> getUnMappedCimProperties(){
		return unMappedCimProperties;
	}
	
	public Collection<String> getUnMappedPsseProperties(){
		return unMappedPsseProperties;
	}
}
