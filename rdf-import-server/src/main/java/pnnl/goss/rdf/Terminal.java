package pnnl.goss.rdf;

public interface Terminal extends EscaType {
	
	TopologicalNode getTopologicalNode();
	boolean getProcessed();
	void setProcessed(boolean bl);

}
