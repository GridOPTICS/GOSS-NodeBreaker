package pnnl.goss.rdf;

public interface NodeBreakerService {
	
	Network getNetwork(String networkKey);
	
	String processNetwork(String fileName);
	
}
