package pnnl.goss.rdf;

import java.util.Collection;

public interface TopologicalBranch {

    String getName();
    Collection<TopologicalNode> getNodes();
    String getType();

}
