package pnnl.goss.rdf.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RdfBranches implements Iterable<RdfBranch> {
	
	private final List<RdfBranch> branches = new ArrayList<>();
	
	public void add(RdfBranch branch){
		branches.add(branch);
	}

	@Override
	public Iterator<RdfBranch> iterator() {
		return branches.iterator();
	}

}
