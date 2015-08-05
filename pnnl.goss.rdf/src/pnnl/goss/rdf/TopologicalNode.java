package pnnl.goss.rdf;

import java.util.List;

public interface TopologicalNode {
	
	public static final String TOPO_NODE_NO_GENERATION = "TOPO_NODE_NO_GENERATION";
	public static final String TOPO_NODE_HAS_GENERATION = "TOPO_NODE_HAS_GENERATION";
	public static final String TOPO_NODE_IS_ISOLATED = "TOPO_NODE_IS_ISOLATED";
	public static final String TOPO_NODE_IS_SWING = "TOPO_NODE_IS_SWING";
	
	
	List<Terminal> getTerminals();
	
	String getIdentifier();
	
	String getSubstationName();
	
	/**
	 * Based upon context one of the four static strings should
	 * be returned:
	 *  
	 *  TOPO_NODE_NO_GENERATION,
	 *	TOPO_NODE_HAS_GENERATION,
	 *	TOPO_NODE_IS_SWING,
	 *	TOPO_NODE_IS_ISOLATED
	 *  
	 * These correspond to the state in psse IDE bus.
	 * 
	 * @return
	 */
	String getTopoNodeType();
	
	/**
	 * Measured in W
	 * @return
	 */
	double getNetPInjection();
	
	/**
	 * Measured in VAr
	 * @return
	 */
	double getNetQInjection();
	
	/**
	 * Measured in V
	 * @return
	 */
	double getVoltage();
	
	/**
	 * Measured in rad
	 * @return
	 */
	double getAngle();
	
	/**
	 * Zero sequence shunt (charging) susceptance, uniformly distributed, of the entire line section.
	 * Measured in S
	 * @return
	 */
	double getB0ch();
	
	/**
	 * Positive sequence shunt (charging) susceptance, uniformly distributed, of the entire line section.  This value represents the full charging over the full length of the line.
	 * Measured in S
 	 * @return
	 */
	double getBch();
	
	/**
	 * Zero sequence shunt (charging) conductance, uniformly distributed, of the entire line section.
	 * Measured in S
	 * @return
	 */
	double getG0ch();
	
	/**
	 * Measured in S
 	 * @return
	 */
	double getGch();
	
	/**
	 * Base voltage for the node (PowerSystemResource)
	 * @return
	 */
	double getNominalVoltage();

	/**
	 * Returns the container's mrid (identifier)
	 * @return
	 */
	String getSubstationMrid();
	
	
}
