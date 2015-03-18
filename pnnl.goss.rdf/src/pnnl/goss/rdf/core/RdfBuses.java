package pnnl.goss.rdf.core;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A collection of rdf buses that were created from the import of a cim file.
 * 
 * @author Craig Allwardt
 *
 */
public class RdfBuses {
	
	private final Set<RdfBus> busSet = new LinkedHashSet<RdfBus>();
	
	public void add(RdfBus bus){
		busSet.add(bus);
	}
	
	public Iterator<RdfBus> iterator(){
		return busSet.iterator();
	}
}
