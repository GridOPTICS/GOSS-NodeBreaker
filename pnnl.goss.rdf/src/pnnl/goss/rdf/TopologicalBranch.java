package pnnl.goss.rdf;

public interface TopologicalBranch {

    String getName();
    Terminal getTerminalPrimary();
    Terminal getTerminalSecondary();
    Terminal getTerminalTertiary();

}
