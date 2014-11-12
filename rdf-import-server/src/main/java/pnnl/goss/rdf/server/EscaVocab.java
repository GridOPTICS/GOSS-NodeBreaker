package pnnl.goss.rdf.server;

import java.io.File;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;

public class EscaVocab {

	protected static final String uri ="http://fpgi.pnnl.gov/esca60";

	/** returns the URI for this schema
	 * @return the URI for this schema
	 */

	public static String getURI() {
		return uri;
	}

	public static Model readModel(File modelData){
		FileManager.get().readModel(m, modelData.getAbsolutePath());
		return m;
	}

	private static Model m = ModelFactory.createDefaultModel();

	public final static String URI_ROOT = "http://iec.ch/TC57/2007/CIM-schema-cim12#";

	public final static Resource SUBSTATION_OBJECT = m.createProperty(URI_ROOT + "Substation");
	public final static Resource SWITCH_OBJECT = m.createProperty(URI_ROOT + "Switch");
	public final static Resource LOADBREAKSWITCH_OBJECT = m.createProperty(URI_ROOT + "LoadBreakSwitch");
	public final static Resource MEASUREMENT_OBJECT = m.createProperty(URI_ROOT + "Measurement");
	public final static Resource BASEVOLTAGE_OBJECT = m.createProperty(URI_ROOT + "BaseVoltage");
	public final static Resource CURVEDATA_OBJECT = m.createProperty(URI_ROOT + "CurveData");
	public final static Resource TYPE_OBJECT = m.createProperty(URI_ROOT + "type");
	public final static Resource TAPCHANGER_OBJECT = m.createProperty(URI_ROOT + "TapChanger");
	public final static Resource IDENTIFIEDOBJECT_OBJECT = m.createProperty(URI_ROOT + "IdentifiedObject");
	public final static Resource LOADGROUP_OBJECT = m.createProperty(URI_ROOT + "LoadGroup");
	public final static Resource REGULATINGCONDEQ_OBJECT = m.createProperty(URI_ROOT + "RegulatingCondEq");
	public final static Resource REGULARTIMEPOINT_OBJECT = m.createProperty(URI_ROOT + "RegularTimePoint");
	public final static Resource ANALOGLIMITSET_OBJECT = m.createProperty(URI_ROOT + "AnalogLimitSet");
	public final static Resource TRANSFORMERWINDING_OBJECT = m.createProperty(URI_ROOT + "TransformerWinding");
	public final static Resource SERIESCOMPENSATOR_OBJECT = m.createProperty(URI_ROOT + "SeriesCompensator");
	public final static Resource STATICVARCOMPENSATOR_OBJECT = m.createProperty(URI_ROOT + "StaticVarCompensator");
	public final static Resource SHUNTCOMPENSATOR_OBJECT = m.createProperty(URI_ROOT + "ShuntCompensator");
	public final static Resource BREAKER_OBJECT = m.createProperty(URI_ROOT + "Breaker");
	public final static Resource CONDUCTINGEQUIPMENT_OBJECT = m.createProperty(URI_ROOT + "ConductingEquipment");
	public final static Resource BASICINTERVALSCHEDULE_OBJECT = m.createProperty(URI_ROOT + "BasicIntervalSchedule");
	public final static Resource EQUIPMENT_OBJECT = m.createProperty(URI_ROOT + "Equipment");
	public final static Resource IEC61970CIMVERSION_OBJECT = m.createProperty(URI_ROOT + "IEC61970CIMVersion");
	public final static Resource SYNCHRONOUSMACHINE_OBJECT = m.createProperty(URI_ROOT + "SynchronousMachine");
	public final static Resource TERMINAL_OBJECT = m.createProperty(URI_ROOT + "Terminal");
	public final static Resource CONNECTIVITYNODE_OBJECT = m.createProperty(URI_ROOT + "ConnectivityNode");
	public final static Resource CONFORMLOAD_OBJECT = m.createProperty(URI_ROOT + "ConformLoad");
	public final static Resource POWERTRANSFORMER_OBJECT = m.createProperty(URI_ROOT + "PowerTransformer");
	public final static Resource ANALOG_OBJECT = m.createProperty(URI_ROOT + "Analog");
	public final static Resource GROSSTONETACTIVEPOWERCURVE_OBJECT = m.createProperty(URI_ROOT + "GrossToNetActivePowerCurve");
	public final static Resource GENERATINGUNIT_OBJECT = m.createProperty(URI_ROOT + "GeneratingUnit");
	public final static Resource ENERGYCONSUMER_OBJECT = m.createProperty(URI_ROOT + "EnergyConsumer");
	public final static Resource VOLTAGELEVEL_OBJECT = m.createProperty(URI_ROOT + "VoltageLevel");
	public final static Resource CURVE_OBJECT = m.createProperty(URI_ROOT + "Curve");
	public final static Resource CONDUCTOR_OBJECT = m.createProperty(URI_ROOT + "Conductor");
	public final static Resource LIMITSET_OBJECT = m.createProperty(URI_ROOT + "LimitSet");
	public final static Resource SUBGEOGRAPHICALREGION_OBJECT = m.createProperty(URI_ROOT + "SubGeographicalRegion");
	public final static Resource ANALOGLIMIT_OBJECT = m.createProperty(URI_ROOT + "AnalogLimit");
	public final static Resource LINE_OBJECT = m.createProperty(URI_ROOT + "Line");
	public final static Resource ACLINESEGMENT_OBJECT = m.createProperty(URI_ROOT + "ACLineSegment");
	public final static Property MEASUREMENT_MEMBEROF_PSR = m.createProperty(URI_ROOT + "Measurement.MemberOf_PSR");
	public final static Property IEC61970CIMVERSION_DATE = m.createProperty(URI_ROOT + "IEC61970CIMVersion.date");
	public final static Property STATICVARCOMPENSATOR_SVCCONTROLMODE = m.createProperty(URI_ROOT + "StaticVarCompensator.sVCControlMode");
	public final static Property LINE_REGION = m.createProperty(URI_ROOT + "Line.Region");
	public final static Property IDENTIFIEDOBJECT_DESCRIPTION = m.createProperty(URI_ROOT + "IdentifiedObject.description");
	public final static Property ENERGYCONSUMER_QFIXEDPCT = m.createProperty(URI_ROOT + "EnergyConsumer.qfixedPct");
	public final static Property ENERGYCONSUMER_QFIXED = m.createProperty(URI_ROOT + "EnergyConsumer.qfixed");
	public final static Property CONDUCTOR_B0CH = m.createProperty(URI_ROOT + "Conductor.b0ch");
	public final static Property VOLTAGELEVEL_MEMBEROF_SUBSTATION = m.createProperty(URI_ROOT + "VoltageLevel.MemberOf_Substation");
	public final static Property CURVEDATA_CURVESCHEDULE = m.createProperty(URI_ROOT + "CurveData.CurveSchedule");
	public final static Property IDENTIFIEDOBJECT_ALIASNAME = m.createProperty(URI_ROOT + "IdentifiedObject.aliasName");
	public final static Property SWITCH_NORMALOPEN = m.createProperty(URI_ROOT + "Switch.normalOpen");
	public final static Property GENERATINGUNIT_MAXOPERATINGP = m.createProperty(URI_ROOT + "GeneratingUnit.maxOperatingP");
	public final static Property TAPCHANGER_STEPVOLTAGEINCREMENT = m.createProperty(URI_ROOT + "TapChanger.stepVoltageIncrement");
	public final static Property TAPCHANGER_NORMALSTEP = m.createProperty(URI_ROOT + "TapChanger.normalStep");
	public final static Property SYNCHRONOUSMACHINE_MEMBEROF_GENERATINGUNIT = m.createProperty(URI_ROOT + "SynchronousMachine.MemberOf_GeneratingUnit");
	public final static Property ENERGYCONSUMER_QFEXP = m.createProperty(URI_ROOT + "EnergyConsumer.qFexp");
	public final static Property SYNCHRONOUSMACHINE_MAXQ = m.createProperty(URI_ROOT + "SynchronousMachine.maxQ");
	public final static Property ENERGYCONSUMER_QVEXP = m.createProperty(URI_ROOT + "EnergyConsumer.qVexp");
	public final static Property TRANSFORMERWINDING_MEMBEROF_POWERTRANSFORMER = m.createProperty(URI_ROOT + "TransformerWinding.MemberOf_PowerTransformer");
	public final static Property GENERATINGUNIT_MINECONOMICP = m.createProperty(URI_ROOT + "GeneratingUnit.minEconomicP");
	public final static Property SHUNTCOMPENSATOR_MAXIMUMSECTIONS = m.createProperty(URI_ROOT + "ShuntCompensator.maximumSections");
	public final static Property REGULARTIMEPOINT_INTERVALSCHEDULE = m.createProperty(URI_ROOT + "RegularTimePoint.IntervalSchedule");
	public final static Property TRANSFORMERWINDING_WINDINGTYPE = m.createProperty(URI_ROOT + "TransformerWinding.windingType");
	public final static Property GENERATINGUNIT_RATEDGROSSMAXP = m.createProperty(URI_ROOT + "GeneratingUnit.ratedGrossMaxP");
	public final static Property ANALOG_NORMALVALUE = m.createProperty(URI_ROOT + "Analog.normalValue");
	public final static Property GENERATINGUNIT_SHORTPF = m.createProperty(URI_ROOT + "GeneratingUnit.shortPF");
	public final static Property CONDUCTOR_R0 = m.createProperty(URI_ROOT + "Conductor.r0");
	public final static Property SUBSTATION_REGION = m.createProperty(URI_ROOT + "Substation.Region");
	public final static Property STATICVARCOMPENSATOR_CAPACITIVERATING = m.createProperty(URI_ROOT + "StaticVarCompensator.capacitiveRating");
	public final static Property CONDUCTOR_BCH = m.createProperty(URI_ROOT + "Conductor.bch");
	public final static Property GENERATINGUNIT_MAXECONOMICP = m.createProperty(URI_ROOT + "GeneratingUnit.maxEconomicP");
	public final static Property ENERGYCONSUMER_PFIXED = m.createProperty(URI_ROOT + "EnergyConsumer.pfixed");
	public final static Property TRANSFORMERWINDING_B = m.createProperty(URI_ROOT + "TransformerWinding.b");
	public final static Property IEC61970CIMVERSION_VERSION = m.createProperty(URI_ROOT + "IEC61970CIMVersion.version");
	public final static Property ANALOGLIMIT_LIMITSET = m.createProperty(URI_ROOT + "AnalogLimit.LimitSet");
	public final static Property TRANSFORMERWINDING_G = m.createProperty(URI_ROOT + "TransformerWinding.g");
	public final static Property GENERATINGUNIT_MINOPERATINGP = m.createProperty(URI_ROOT + "GeneratingUnit.minOperatingP");
	public final static Property TRANSFORMERWINDING_RATEDU = m.createProperty(URI_ROOT + "TransformerWinding.ratedU");
	public final static Property TRANSFORMERWINDING_RATEDS = m.createProperty(URI_ROOT + "TransformerWinding.ratedS");
	public final static Property SHUNTCOMPENSATOR_REACTIVEPERSECTION = m.createProperty(URI_ROOT + "ShuntCompensator.reactivePerSection");
	public final static Property STATICVARCOMPENSATOR_INDUCTIVERATING = m.createProperty(URI_ROOT + "StaticVarCompensator.inductiveRating");
	public final static Property CONNECTIVITYNODE_MEMBEROF_EQUIPMENTCONTAINER = m.createProperty(URI_ROOT + "ConnectivityNode.MemberOf_EquipmentContainer");
	public final static Property LOADGROUP_SUBLOADAREA = m.createProperty(URI_ROOT + "LoadGroup.SubLoadArea");
	public final static Property ENERGYCONSUMER_PFIXEDPCT = m.createProperty(URI_ROOT + "EnergyConsumer.pfixedPct");
	public final static Property STATICVARCOMPENSATOR_VOLTAGESETPOINT = m.createProperty(URI_ROOT + "StaticVarCompensator.voltageSetPoint");
	public final static Property SHUNTCOMPENSATOR_NOMU = m.createProperty(URI_ROOT + "ShuntCompensator.nomU");
	public final static Property CURVEDATA_Y1VALUE = m.createProperty(URI_ROOT + "CurveData.y1value");
	public final static Property TRANSFORMERWINDING_R = m.createProperty(URI_ROOT + "TransformerWinding.r");
	public final static Property CURVEDATA_XVALUE = m.createProperty(URI_ROOT + "CurveData.xvalue");
	public final static Property SYNCHRONOUSMACHINE_INITIALREACTIVECAPABILITYCURVE = m.createProperty(URI_ROOT + "SynchronousMachine.InitialReactiveCapabilityCurve");
	public final static Property MEASUREMENT_TERMINAL = m.createProperty(URI_ROOT + "Measurement.Terminal");
	public final static Property TAPCHANGER_STEPPHASESHIFTINCREMENT = m.createProperty(URI_ROOT + "TapChanger.stepPhaseShiftIncrement");
	public final static Property EQUIPMENT_MEMBEROF_EQUIPMENTCONTAINER = m.createProperty(URI_ROOT + "Equipment.MemberOf_EquipmentContainer");
	public final static Property TRANSFORMERWINDING_X = m.createProperty(URI_ROOT + "TransformerWinding.x");
	public final static Property TERMINAL_CONDUCTINGEQUIPMENT = m.createProperty(URI_ROOT + "Terminal.ConductingEquipment");
	public final static Property GENERATINGUNIT_RATEDGROSSMINP = m.createProperty(URI_ROOT + "GeneratingUnit.ratedGrossMinP");
	public final static Property CONDUCTOR_X0 = m.createProperty(URI_ROOT + "Conductor.x0");
	public final static Property ENERGYCONSUMER_PFEXP = m.createProperty(URI_ROOT + "EnergyConsumer.pFexp");
	public final static Property TAPCHANGER_NEUTRALSTEP = m.createProperty(URI_ROOT + "TapChanger.neutralStep");
	public final static Property TAPCHANGER_TCULCONTROLMODE = m.createProperty(URI_ROOT + "TapChanger.tculControlMode");
	public final static Property SYNCHRONOUSMACHINE_REFERENCEPRIORITY = m.createProperty(URI_ROOT + "SynchronousMachine.referencePriority");
	public final static Property TYPE = m.createProperty(URI_ROOT + "type");
	public final static Property TAPCHANGER_HIGHSTEP = m.createProperty(URI_ROOT + "TapChanger.highStep");
	public final static Property CURVEDATA_Y2VALUE = m.createProperty(URI_ROOT + "CurveData.y2value");
	public final static Property SERIESCOMPENSATOR_R = m.createProperty(URI_ROOT + "SeriesCompensator.r");
	public final static Property REGULARTIMEPOINT_VALUE2 = m.createProperty(URI_ROOT + "RegularTimePoint.value2");
	public final static Property GENERATINGUNIT_RATEDNETMAXP = m.createProperty(URI_ROOT + "GeneratingUnit.ratedNetMaxP");
	public final static Property REGULARTIMEPOINT_VALUE1 = m.createProperty(URI_ROOT + "RegularTimePoint.value1");
	public final static Property LIMITSET_ISPERCENTAGELIMITS = m.createProperty(URI_ROOT + "LimitSet.isPercentageLimits");
	public final static Property MEASUREMENT_MEASUREMENTTYPE = m.createProperty(URI_ROOT + "Measurement.MeasurementType");
	public final static Property BASEVOLTAGE_NOMINALVOLTAGE = m.createProperty(URI_ROOT + "BaseVoltage.nominalVoltage");
	public final static Property SYNCHRONOUSMACHINE_RATEDS = m.createProperty(URI_ROOT + "SynchronousMachine.ratedS");
	public final static Property TAPCHANGER_REGULATIONSCHEDULE = m.createProperty(URI_ROOT + "TapChanger.RegulationSchedule");
	public final static Property TAPCHANGER_LOWSTEP = m.createProperty(URI_ROOT + "TapChanger.lowStep");
	public final static Property SHUNTCOMPENSATOR_NORMALSECTIONS = m.createProperty(URI_ROOT + "ShuntCompensator.normalSections");
	public final static Property CONDUCTINGEQUIPMENT_BASEVOLTAGE = m.createProperty(URI_ROOT + "ConductingEquipment.BaseVoltage");
	public final static Property TAPCHANGER_TRANSFORMERWINDING = m.createProperty(URI_ROOT + "TapChanger.TransformerWinding");
	public final static Property GENERATINGUNIT_NORMALPF = m.createProperty(URI_ROOT + "GeneratingUnit.normalPF");
	public final static Property SYNCHRONOUSMACHINE_TYPE = m.createProperty(URI_ROOT + "SynchronousMachine.type");
	public final static Property IDENTIFIEDOBJECT_PATHNAME = m.createProperty(URI_ROOT + "IdentifiedObject.pathName");
	public final static Property SYNCHRONOUSMACHINE_MINQ = m.createProperty(URI_ROOT + "SynchronousMachine.minQ");
	public final static Property CONDUCTOR_X = m.createProperty(URI_ROOT + "Conductor.x");
	public final static Property SUBGEOGRAPHICALREGION_REGION = m.createProperty(URI_ROOT + "SubGeographicalRegion.Region");
	public final static Property GROSSTONETACTIVEPOWERCURVE_GENERATINGUNIT = m.createProperty(URI_ROOT + "GrossToNetActivePowerCurve.GeneratingUnit");
	public final static Property ANALOG_POSITIVEFLOWIN = m.createProperty(URI_ROOT + "Analog.positiveFlowIn");
	public final static Property STATICVARCOMPENSATOR_SLOPE = m.createProperty(URI_ROOT + "StaticVarCompensator.slope");
	public final static Property CONFORMLOAD_LOADGROUP = m.createProperty(URI_ROOT + "ConformLoad.LoadGroup");
	public final static Property CONDUCTOR_R = m.createProperty(URI_ROOT + "Conductor.r");
	public final static Property CURVE_CURVESTYLE = m.createProperty(URI_ROOT + "Curve.curveStyle");
	public final static Property VOLTAGELEVEL_BASEVOLTAGE = m.createProperty(URI_ROOT + "VoltageLevel.BaseVoltage");
	public final static Property POWERTRANSFORMER_TRANSFORMERTYPE = m.createProperty(URI_ROOT + "PowerTransformer.transformerType");
	public final static Property IDENTIFIEDOBJECT_NAME = m.createProperty(URI_ROOT + "IdentifiedObject.name");
	public final static Property GENERATINGUNIT_LONGPF = m.createProperty(URI_ROOT + "GeneratingUnit.longPF");
	public final static Property CONDUCTOR_LENGTH = m.createProperty(URI_ROOT + "Conductor.length");
	public final static Property SERIESCOMPENSATOR_X = m.createProperty(URI_ROOT + "SeriesCompensator.x");
	public final static Property CONDUCTOR_GCH = m.createProperty(URI_ROOT + "Conductor.gch");
	public final static Property REGULATINGCONDEQ_REGULATIONSCHEDULE = m.createProperty(URI_ROOT + "RegulatingCondEq.RegulationSchedule");
	public final static Property TERMINAL_CONNECTIVITYNODE = m.createProperty(URI_ROOT + "Terminal.ConnectivityNode");
	public final static Property ANALOGLIMITSET_MEASUREMENTS = m.createProperty(URI_ROOT + "AnalogLimitSet.Measurements");
	public final static Property ENERGYCONSUMER_PVEXP = m.createProperty(URI_ROOT + "EnergyConsumer.pVexp");
	public final static Property ANALOGLIMIT_VALUE = m.createProperty(URI_ROOT + "AnalogLimit.value");
	public final static Property BASICINTERVALSCHEDULE_STARTTIME = m.createProperty(URI_ROOT + "BasicIntervalSchedule.startTime");
	public final static Property BREAKER_RATEDCURRENT = m.createProperty(URI_ROOT + "Breaker.ratedCurrent");
}