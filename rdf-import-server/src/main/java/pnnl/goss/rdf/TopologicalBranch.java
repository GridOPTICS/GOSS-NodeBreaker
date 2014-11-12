package pnnl.goss.rdf;

public interface TopologicalBranch {
	
	String getName();
	Terminal getTerminalFrom();
	Terminal getTerminalTo();
	
}
