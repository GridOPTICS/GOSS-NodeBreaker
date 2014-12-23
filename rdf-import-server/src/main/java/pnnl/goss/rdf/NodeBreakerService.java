package pnnl.goss.rdf;

public interface NodeBreakerService {

    /**
     * Retrieve a cim model Network from the datastore
     *
     * @param networkKey
     * @return
     */
    Network getNetwork(String networkKey);

    /**
     * Create a cim model Network and store it in a datastore
     * from a filename.
     *
     * @param fileName
     * @return
     */
    String processNetwork(String fileName);

}
