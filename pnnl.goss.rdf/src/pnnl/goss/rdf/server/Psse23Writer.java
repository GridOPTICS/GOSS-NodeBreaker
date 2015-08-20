package pnnl.goss.rdf.server;

import static pnnl.goss.rdf.TopologicalNode.*;

import java.util.Collection;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.rdf.Network;
import pnnl.goss.rdf.TopologicalNode;

public class Psse23Writer {
	
	private static Logger log = LoggerFactory.getLogger(Psse23Writer.class);
	private StringBuilder sb = new StringBuilder();
	
	private void writeHeader(int IC, double SBASE, String line1, String line2){
		if (line1 == null) line1="";
		if (line2 == null) line2="";
		
		sb.append(IC+","+SBASE+"\n");
		sb.append(line1+"\n");
		sb.append(line2+"\n");
	}
	
	private void writeShunts(){
		// The nominal voltage at which the nominal reactive power was measured. This should normally be within 10% of the voltage at which the capacitor is connected to the network.
		// ShuntCompensator.NomU
		// Zero sequence shunt (charging) susceptance per section
		// b0PerSection
		
		// ShuntCompensator.reactivePerSection
		// 		mVARInjection
		//		The injection of reactive power of the filter bank in the NA solution or VCS reactive power production
		
		// ShuntCompensator.normalSections
		// ShuntCompensator.maxSections
		// ShuntCompensator.reactivePerSection
	}
	
	
	private void writeBuses(Collection<TopologicalNode> nodes){
		int i=1;
		for(TopologicalNode tn : nodes) {
			addField(i);
									
			switch(tn.getTopoNodeType()){
			case TOPO_NODE_NO_GENERATION:
				addField(1);
				break;
			case TOPO_NODE_HAS_GENERATION:
				addField(2);
				break;
			case TOPO_NODE_IS_SWING:
				addField(3);
				break;
			case TOPO_NODE_IS_ISOLATED:
				addField(4);
				break;
			}
		
			addField(tn.getNetPInjection());  //TODO: needs to convert from W to MW
			addField(tn.getNetQInjection());  //TODO: needs to convert from W to MW
			addField("Shunt Conductance");
			addField("Shunt Susceptance");
			addField("Area Number (1-100)");
			addField(tn.getVoltage());
			addField(tn.getAngle());  // TODO: Convert from rad to degrees;
			addField(tn.getSubstationName());
			addField(tn.getNominalVoltage()); // Base voltage.
			addFieldLast("Zone Number");	
		}		
	}
	/*
A list of measurementTypes that were present in the esca60 cim model.

  <cim:MeasurementType rdf:ID="MeasurementType_12">
    <cim:IdentifiedObject.name>TransducerReading</cim:IdentifiedObject.name>
  </cim:MeasurementType>
  <cim:MeasurementType rdf:ID="MeasurementType_11">
    <cim:IdentifiedObject.name>PSS</cim:IdentifiedObject.name>
  </cim:MeasurementType>
  <cim:MeasurementType rdf:ID="MeasurementType_4">
    <cim:IdentifiedObject.name>LineCurrent</cim:IdentifiedObject.name>
  </cim:MeasurementType>
  <cim:MeasurementType rdf:ID="MeasurementType_9">
    <cim:IdentifiedObject.name>SwitchPosition</cim:IdentifiedObject.name>
  </cim:MeasurementType>
  <cim:MeasurementType rdf:ID="MeasurementType_7">
    <cim:IdentifiedObject.name>Angle</cim:IdentifiedObject.name>
  </cim:MeasurementType>
  <cim:MeasurementType rdf:ID="MeasurementType_2">
    <cim:IdentifiedObject.name>ThreePhaseActivePower</cim:IdentifiedObject.name>
  </cim:MeasurementType>
  <cim:MeasurementType rdf:ID="MeasurementType_6">
    <cim:IdentifiedObject.name>LineToLineVoltage</cim:IdentifiedObject.name>
  </cim:MeasurementType>
  <cim:MeasurementType rdf:ID="MeasurementType_10">
    <cim:IdentifiedObject.name>AVR</cim:IdentifiedObject.name>
  </cim:MeasurementType>
  <cim:MeasurementType rdf:ID="MeasurementType_8">
    <cim:IdentifiedObject.name>TapPosition</cim:IdentifiedObject.name>
  </cim:MeasurementType>
  <cim:MeasurementType rdf:ID="MeasurementType_5">
    <cim:IdentifiedObject.name>PhaseVoltage</cim:IdentifiedObject.name>
  </cim:MeasurementType>
  <cim:MeasurementType rdf:ID="MeasurementType_1">
    <cim:IdentifiedObject.name>ThreePhaseApparentPower</cim:IdentifiedObject.name>
  </cim:MeasurementType>
  <cim:MeasurementType rdf:ID="MeasurementType_3">
    <cim:IdentifiedObject.name>ThreePhaseReactivePower</cim:IdentifiedObject.name>
  </cim:MeasurementType>
	 */
	
	private void writeGeneraters(Collection<TopologicalNode> nodes){
		
//		I - Bus number
//		ID - Machine identifier (0-9, A-Z)
//		PG - MW output
//		QG - MVAR output
//		QT - Max MVAR
//		QB - Min MVAR
//		VS - Voltage setpoint
//		IREG - Remote controlled bus index (must be type 1), zero to control own
//		 voltage, and must be zero for gen at swing bus
//		MBASE - Total MVA base of this machine (or machines), defaults to system
//		 MVA base.
//		ZR,ZX - Machine impedance, pu on MBASE
//		RT,XT - Step up transformer impedance, p.u. on MBASE
//		GTAP - Step up transformer off nominal turns ratio
//		STAT - Machine status, 1 in service, 0 out of service
//		RMPCT - Percent of total VARS required to hold voltage at bus IREG
//		 to come from bus I - for remote buses controlled by several generators
//		PT - Max MW
//		PB - Min MW
		
		
		
		
		// getRatedS
		// getBaseVoltage;
		// getMachineType
		// getreferencePriority
		// getMinQ
		// getMaxQ
		// getIdentifiedAliasName
		// getIdentifiedPathName
		
		
	}
	
	public void write(Network network){
		
		writeHeader(0, 100.0, "Cim 2 Psse Test", new Date().toString());
		
		
//		writeBuses(network.getTopologicalNodes());
//		writeGeneraters(network.getTopologicalNodes());
//		writeBranch(network.getTopologicalBranches());
//		writeTransformerAdjustment(network.getTopologicalBranches());
//		writeAreaInterchange();
//		writeDcLines();
//		writeSwitchedShunts(network.getTopologicalNodes());
		
		
		log.debug("BEGIN PTI OUTPUT\n"+sb.toString()+"END PTI OUTPUT\n");
	}

	private void addField(Double value){
		addField(value.toString());
	}
	
	private void addFieldLast(Double value){
		addFieldLast(value.toString());
	}
	
	private void addField(Integer value){
		addField(value.toString());
	}
	
	private void addFieldLast(Integer value){
		addFieldLast(value.toString());
	}
	
	private void addField(String value){
		sb.append(value+",");
	}
	
	private void addFieldLast(String value){
		sb.append(value+"\n");
	}
}
