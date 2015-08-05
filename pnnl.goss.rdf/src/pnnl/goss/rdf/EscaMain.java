package pnnl.goss.rdf;


import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.LogManager;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.rdf.impl.ConnectivityNode;
import pnnl.goss.rdf.impl.EscaTreeWindow;
import pnnl.goss.rdf.impl.EscaTypes;
import pnnl.goss.rdf.impl.MatchMrids;
import pnnl.goss.rdf.impl.NetworkImpl;
import pnnl.goss.rdf.impl.NodeBreakerServiceImpl;
import pnnl.goss.rdf.impl.TopologicalNodeImpl;
import pnnl.goss.rdf.Network;
import pnnl.goss.rdf.impl.TerminalImpl;
import pnnl.goss.rdf.impl.TopologicalBranchImpl;
import pnnl.goss.rdf.TopologicalIsland;
import pnnl.goss.rdf.TopologicalNode;
import pnnl.goss.rdf.server.EscaVocab;
import pnnl.goss.rdf.server.Psse23Writer;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class EscaMain {

    private static final String PERSISTANCE_UNIT = "nodebreaker_cass_pu";

    private static final String ESCA_TEST = "./resources/esca60_cim.xml";
    private static boolean bufferedOut = false;
    private static BufferedOutputStream outStream = null;

    private static Logger log = LoggerFactory.getLogger(EscaMain.class);

    /**
     * A loaded mapping from mrid to escatype which is loaded from the cim model
     * file.
     */
    private EscaTypes escaTypes = null;

    private EscaMain(String inputFile, boolean isCim, String outputFile) throws Exception{

        EscaTreeWindow window = new EscaTreeWindow(ESCA_TEST, true, "esca_tree.txt");
        // Load data from the rdf into memory.
        window.loadData();
        // Build an mrid->escatype mapping for referencing all of the subjects by mrid
        // in the system.
        window.loadTypeMap();

        escaTypes = window.getEscaTypeMap();
    }

    

    private void printObjectType(Resource subject){
        for(EscaType t: escaTypes.values()){
            if (t.getDataType().equals(subject.getLocalName())){
                System.out.println(t);
            }
        }
    }

    private EscaType getTypeByMrid(String mrid){
        return escaTypes.get(mrid);
    }

    public EscaTypes getEscaTypes(){
        return escaTypes;
    }

    public static void debugCollection(Collection<EscaType> escaTypes){
        boolean first = true;
        for(EscaType t: escaTypes){
            if(first){
                log.debug("Printing collection of: "+ t.getDataType());
                first = false;
            }
            log.debug(t.toString());
        }

    }

    public static void logLinkAndReferring(EscaType type){
        log.debug("\t\tDirectly Linked "+type.toString());
        for(EscaType d: type.getDirectLinks()){
            log.debug("\t\t\t"+d.toString());
        }
        log.debug("\t\tReferred To: "+type.toString());
        for(EscaType d: type.getRefersToMe()){
            log.debug("\t\t\t"+d.toString());
        }
    }

    public static void main(String[] args) throws Exception {
    	
//    	MatchMrids mrids = new MatchMrids();
//    	
//    	mrids.loadData();
//    	if(true) return;
    	
    	//System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG");
    	
    	// Specify the log location because we aren't using a standard java
    	// resource path instead we are using our osgi paths.
    	File logPropertiesFile = new File("./resources/log4j.properties"); //resources/log4j.properties");
    	if (logPropertiesFile.exists()){
    		try{
    			LogManager manager = LogManager.getLogManager();
    			manager.readConfiguration(new FileInputStream(logPropertiesFile));
    		}
    		catch (IOException e){
    			System.err.println("Log properties not found!");
    		}
    	}

    	log.debug("Setup successful!");
        NodeBreakerService service = new NodeBreakerServiceImpl();

        String key = service.processNetwork(ESCA_TEST);

        Network network = service.getNetwork(key);
                
        int i=1;
        
            
        
        File f=new File("substation-bus-mapping.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(f));
        Map<String, List<TopologicalNode>> substationTopo = new LinkedHashMap<>();
        
        for(TopologicalNode b: network.getTopologicalNodes()){
        	if (b.getSubstationName() == null || b.getSubstationName().isEmpty()){
        		writer.write(b + " HAS NO SUBSTATIONS.\n");
        		continue;
        	}
        	if (!substationTopo.containsKey(b.getSubstationName())){
        		substationTopo.put(b.getSubstationName(), new ArrayList<>());
        	}
        	substationTopo.get(b.getSubstationName()).add(b);
        }
        
        
        for(Entry<String, List<TopologicalNode>> entry: substationTopo.entrySet()){
        	writer.write("Substation: "+ entry.getKey()+"\n");
        	for(TopologicalNode n:entry.getValue()){
        		writer.write("\t"+n+"\n");
        	}        	
        }
        
        f=new File("bus-branch-map.txt");
        writer = new BufferedWriter(new FileWriter(f));
        
        for(TopologicalBranch tb: network.getTopologicalBranches()){
        	writer.write("Branch: "+tb.getName()+ " type: "+ tb.getType() + "Connected to\n");
        	for (TopologicalNode tn: tb.getNodes()){
        		writer.write("\t"+ tn);
        	}
        }
        
        
        
//        writer.write("TOPONODES\n");
//        for(TopologicalNode br: network.getTopologicalNodes()){
//        	
//        	writer.write("Bus: " + br+"\n");
//        	
//        }
//        writer.write("End TOPONODES\n");
//        writer.write("ACLINES\n");
//        for(TopologicalBranch br: network.getTopologicalBranches()){
//        	if(br.getType().equals(EscaVocab.ACLINESEGMENT_OBJECT.getLocalName())){
//	        	writer.write("Branch: " + br.getName()+"\n");
//	        	writer.write("Buses\n");
//	        	log.debug("Branch: " + br.getName());
//	        	log.debug("Buses");
//	        	for(TopologicalNode tn: br.getNodes()){
//	        		writer.write("\t"+tn+"\n");
//	        		log.debug("\t"+tn);
//	        	}
//	        	log.debug("End Branch: " + br.getName());
//        	}
//        }
//        
//        writer.write("WINDINGS\n");
//        for(TopologicalBranch br: network.getTopologicalBranches()){
//        	if(br.getType().equals(EscaVocab.TRANSFORMERWINDING_OBJECT.getLocalName())){
//	        	writer.write("Branch: " + br.getName()+"\n");
//	        	writer.write("Buses\n");
//	        	log.debug("Branch: " + br.getName());
//	        	log.debug("Buses");
//	        	for(TopologicalNode tn: br.getNodes()){
//	        		writer.write("\t"+tn+"\n");
//	        		log.debug("\t"+tn);
//	        	}
//	        	log.debug("End Branch: " + br.getName());
//        	}
//        }
        
        writer.close();
        
        if(true) return;
        Psse23Writer w = new Psse23Writer();
        w.write(network);
        
        if(true) return;
        
        NetworkImpl netImpl = (NetworkImpl)network;
        log.debug("# Substations: "+netImpl.getSubstations().size());
        log.debug("SUBSTATIONS");
        for (EscaType s: netImpl.getSubstations()){
            if (s == null){
                log.debug("S is null!");
            }
            else{
                Literal path = s.getLiteralValue(EscaVocab.IDENTIFIEDOBJECT_PATHNAME);
                if (path != null){
                    log.debug(s.getDataType()+" <"+s.getMrid()+"> No path!");
                }
                else{
                    log.debug(s.getDataType()+" <"+s.getMrid()+"> " + path.getString());
                }
            }
        }

        log.debug("# TOPO NODES: "+ network.getTopologicalNodes().size());

        for (TopologicalNode n2: network.getTopologicalNodes()){
            TopologicalNodeImpl n = (TopologicalNodeImpl)n2;
            log.debug(n.toString());
            log.debug(n.getIdentifier()+ " voltage: "+n.getBaseVoltage());
            log.debug("\tTERMINALS");
            for (Terminal t: n.getTerminals()){
                logLinkAndReferring((EscaType)t);
            }

        }

        log.debug("Islands: " + network.getTopologicalIslands().size());
        log.debug("Topology Nodes: "+ network.getTopologicalNodes().size());
        log.debug("Topology Branches: "+ network.getTopologicalBranches().size());

        

        //setBufferedOut();setBufferedOut();
        if (bufferedOut){
            outStream.flush();
        }

        System.out.println("Import Complete!");

        

    }

    private static void setBufferedOut() throws FileNotFoundException{
        bufferedOut = true;
        File file = new File("stdout.txt");
        if (file.exists()){
            file.delete();
        }
        outStream = new BufferedOutputStream(new FileOutputStream(file));
        System.setOut(new PrintStream(outStream));
    }

//	private static void populateIdentityObjects(EscaType escaType, IdentifiedObject ident){
//		Resource resource = escaType.getResource();
//		ident.setMrid(escaType.getMrid());
//		//ident.setDataType(escaType.getDataType());
//
//		Statement stmt = resource.getProperty(Esca60Vocab.IDENTIFIEDOBJECT_ALIASNAME);
//		if (stmt == null){
//			//log.warn(Esca60Vocab.IDENTIFIEDOBJECT_ALIASNAME + " was null!");
//		}
//		else{
//			ident.setAlias(stmt.getString());
//		}
//		stmt = resource.getProperty(Esca60Vocab.IDENTIFIEDOBJECT_NAME);
//		if (stmt == null){
//			//log.warn(Esca60Vocab.IDENTIFIEDOBJECT_NAME + " was null!");
//		}
//		else{
//			ident.setName(stmt.getString());
//		}
//		stmt = resource.getProperty(Esca60Vocab.IDENTIFIEDOBJECT_PATHNAME);
//		if (stmt == null){
//			//log.warn(Esca60Vocab.IDENTIFIEDOBJECT_PATHNAME + " was null!");
//		}
//		else{
//			ident.setPath(stmt.getString());
//		}
//		stmt = resource.getProperty(Esca60Vocab.IDENTIFIEDOBJECT_DESCRIPTION);
//		if (stmt == null){
//			//log.warn(Esca60Vocab.IDENTIFIEDOBJECT_DESCRIPTION + " was null!");
//		}
//		else{
//			ident.setDescription(stmt.getString());
//		}
//	}

    private static String getPropertyString(Resource resource, Property property){
        if (resource.getProperty(property) != null){
            // Look up the connecting resources mrid.
            if (resource.getProperty(property).getResource() != null){
                return resource.getProperty(property).getResource().getLocalName();
            }

            // String literal
            return resource.getProperty(property).getString();
        }
        return null;
    }

    private static String getPropertyString(Resource resource, String property){

        StmtIterator stmts = resource.listProperties();

        while(stmts.hasNext()){
            Statement stmt = stmts.next();

            Resource pred = stmt.getPredicate();
            // If the resource matches then the caller is expecting the mrid of the
            if (pred.isResource()){
                if (pred.getLocalName().equals(property)){
                    RDFNode node = stmt.getObject();
                    return node.asResource().getLocalName();
                }
            }
            else if(stmt.getPredicate().isProperty()){

            }
            else if(stmt.getPredicate().isLiteral()){

            }
        }


        return null;
    }
//
//	private static void storeAnalog(NodeBreakerDao dao, EscaType escaType){
//		IdentifiedObject ident = new IdentifiedObject();
//
//		populateIdentityObjects(escaType, ident);
//
//		Analog entity = new Analog();
//
//		entity.setIdentifiedObject(ident);
//
//		Resource resource = escaType.getResource();
//
//		entity.setNormalValue(resource.getProperty(Esca60Vocab.ANALOG_NORMALVALUE).getDouble());
//		entity.setPositiveFlowIn(resource.getProperty(Esca60Vocab.ANALOG_POSITIVEFLOWIN).getBoolean());
//
//		dao.persist(entity);
//	}
//
//	private static void storeAnalogLimitSet(NodeBreakerDao dao, EscaType escaType){
//		IdentifiedObject ident = new IdentifiedObject();
//
//		populateIdentityObjects(escaType, ident);
//
//		AnalogLimitSet entity = new AnalogLimitSet();
//
//		entity.setIdentifiedObject(ident);
//
//		Resource resource = escaType.getResource();
//
//		entity.setLimitSetIsPercentageLimits(resource.getProperty(Esca60Vocab.LIMITSET_ISPERCENTAGELIMITS).getBoolean());
//
//		dao.persist(entity);
//	}
//
//	private static void storeAnalogLimit(NodeBreakerDao dao, EscaType escaType){
//		IdentifiedObject ident = new IdentifiedObject();
//
//		populateIdentityObjects(escaType, ident);
//
//		AnalogLimit entity = new AnalogLimit();
//
//		entity.setIdentifiedObject(ident);
//
//		Resource resource = escaType.getResource();
//
//		entity.setValue(resource.getProperty(Esca60Vocab.ANALOGLIMIT_VALUE).getDouble());
//
//		dao.persist(entity);
//	}
//
//	private static void storeConnectivityNode(NodeBreakerDao dao, EscaType escaType){
//		IdentifiedObject ident = new IdentifiedObject();
//
//		populateIdentityObjects(escaType, ident);
//
//		ConnectivityNode entity = new ConnectivityNode();
//
//		entity.setIdentifiedObject(ident);
//
//		dao.persist(entity);
//	}
//
//	private static void storePowerTransformer(NodeBreakerDao dao, EscaType escaType){
//		IdentifiedObject ident = new IdentifiedObject();
//
//		populateIdentityObjects(escaType, ident);
//
//		PowerTransformer entity = new PowerTransformer();
//
//		entity.setIdentifiedObject(ident);
//
//		dao.persist(entity);
//	}
//
//	public static void storeBusBarSection(NodeBreakerDao dao, EscaType escaType){
//
//		IdentifiedObject ident = new IdentifiedObject();
//
//		populateIdentityObjects(escaType, ident);
//
//		BusbarSection entity = new BusbarSection();
//
//		entity.setIdentifiedObject(ident);
//
//		dao.persist(entity);
//	}
//
//	private static void storeDisconnector(NodeBreakerDao dao, EscaType escaType){
//
//		IdentifiedObject ident = new IdentifiedObject();
//
//		populateIdentityObjects(escaType, ident);
//
//		Disconnector entity = new Disconnector();
//
//		entity.setIdentifiedObject(ident);
//
//		Resource resource = escaType.getResource();
//
//		entity.setSwitchNormalOpen(resource.getProperty(Esca60Vocab.SWITCH_NORMALOPEN).getBoolean());
//
//		dao.persist(entity);
//	}
//
//	private static void storeConformLoad(NodeBreakerDao dao, EscaType escaType){
//		IdentifiedObject ident = new IdentifiedObject();
//
//		populateIdentityObjects(escaType, ident);
//
//		ConformLoad entity = new ConformLoad();
//
//		entity.setIdentifiedObject(ident);
//
//		Resource resource = escaType.getResource();
//
//		entity.setEnergyConsumerpFexp(resource.getProperty(Esca60Vocab.ENERGYCONSUMER_PFEXP).getDouble());
//		entity.setEnergyConsumerpfixed(resource.getProperty(Esca60Vocab.ENERGYCONSUMER_PFIXED).getDouble());
//		entity.setEnergyConsumerpfixedPct(resource.getProperty(Esca60Vocab.ENERGYCONSUMER_PFIXEDPCT).getDouble());
//		entity.setEnergyConsumerpVexp(resource.getProperty(Esca60Vocab.ENERGYCONSUMER_PVEXP).getDouble());
//
//		entity.setEnergyConsumerqFexp(resource.getProperty(Esca60Vocab.ENERGYCONSUMER_QFEXP).getDouble());
//		entity.setEnergyConsumerqfixed(resource.getProperty(Esca60Vocab.ENERGYCONSUMER_QFIXED).getDouble());
//		entity.setEnergyConsumerqfixedPct(resource.getProperty(Esca60Vocab.ENERGYCONSUMER_QFIXEDPCT).getDouble());
//		entity.setEnergyConsumerqVexp(resource.getProperty(Esca60Vocab.ENERGYCONSUMER_QVEXP).getDouble());
//
//
//		dao.persist(entity);
//	}
//
//	private static void storeTerminal(NodeBreakerDao dao, EscaType escaType){
//		IdentifiedObject ident = new IdentifiedObject();
//
//		populateIdentityObjects(escaType, ident);
//
//		Terminal entity = new Terminal();
//
//		entity.setIdentifiedObject(ident);
//
//		dao.persist(entity);
//	}
//
//	private static void storeDiscrete(NodeBreakerDao dao, EscaType escaType){
//		IdentifiedObject ident = new IdentifiedObject();
//
//		populateIdentityObjects(escaType, ident);
//
//		Discrete entity = new Discrete();
//
//		entity.setIdentifiedObject(ident);
//
//		dao.persist(entity);
//	}
//
//	private static Substation storeSubstation(NodeBreakerDao dao, EscaType escaType){
//		Substation entity = new Substation();
//
//		populateIdentityObjects(escaType, entity);
//
//		dao.persist(entity);
//
//		return entity;
//	}
//
//	private static VoltageLevel storeVoltageLevel(NodeBreakerDao dao, EscaType escaType){
//		IdentifiedObject ident = new IdentifiedObject();
//
//		populateIdentityObjects(escaType, ident);
//
//		VoltageLevel entity = new VoltageLevel();
//
//		entity.setIdentifiedObject(ident);
//
//		dao.persist(entity);
//
//		return entity;
//	}
//
//	private static void storeLine(NodeBreakerDao dao, EscaType line){
//		IdentifiedObject ident = new IdentifiedObject();
//
//		populateIdentityObjects(line, ident);
//
//		Line lineObj = new Line();
//
//		lineObj.setIdentifiedObject(ident);
//
//		Resource resource = line.getResource();
//
//		lineObj.setLineRegion(resource.getProperty(Esca60Vocab.LINE_REGION).getResource().getLocalName());
//
//		dao.persist(lineObj);
//	}
//
//	private static void storeCurveData(NodeBreakerDao dao, EscaType escaType){
//		IdentifiedObject ident = new IdentifiedObject();
//
//		populateIdentityObjects(escaType, ident);
//
//		CurveData entity = new CurveData();
//
//		entity.setIdentifiedObject(ident);
//
//		Resource resource = escaType.getResource();
//
//		entity.setXvalue(resource.getProperty(Esca60Vocab.CURVEDATA_XVALUE).getDouble());
//		entity.setY1value(resource.getProperty(Esca60Vocab.CURVEDATA_Y1VALUE).getDouble());
//		entity.setY2value(resource.getProperty(Esca60Vocab.CURVEDATA_Y2VALUE).getDouble());
//
//		dao.persist(entity);
//	}
//
//	private static void storeTapChanger(NodeBreakerDao dao, EscaType breaker){
//
//		IdentifiedObject ident = new IdentifiedObject();
//
//		populateIdentityObjects(breaker, ident);
//
//		TapChanger entity = new TapChanger();
//
//		entity.setIdentifiedObject(ident);
//
//		Resource resource = breaker.getResource();
//
//		entity.setHighStep(resource.getProperty(Esca60Vocab.TAPCHANGER_HIGHSTEP).getInt());
//		entity.setNormalStep(resource.getProperty(Esca60Vocab.TAPCHANGER_NORMALSTEP).getInt());
//		entity.setLowStep(resource.getProperty(Esca60Vocab.TAPCHANGER_LOWSTEP).getInt());
//		entity.setNeutralStep(resource.getProperty(Esca60Vocab.TAPCHANGER_NEUTRALSTEP).getInt());
//		entity.setTculControlMode(resource.getProperty(Esca60Vocab.TAPCHANGER_TCULCONTROLMODE).getResource().getLocalName());
//		Statement stmt = resource.getProperty(Esca60Vocab.TAPCHANGER_STEPVOLTAGEINCREMENT);
//		if (stmt != null){
//			entity.setStepVoltageIncrement(stmt.getDouble());
//		}
//		entity.setTransformerWinding(resource.getProperty(Esca60Vocab.TAPCHANGER_TRANSFORMERWINDING).getResource().getLocalName());
//
//		dao.persist(entity);
//	}
//
//	private static void storeRegularTimePoint(NodeBreakerDao dao, EscaType breaker){
//
//		IdentifiedObject ident = new IdentifiedObject();
//
//		populateIdentityObjects(breaker, ident);
//
//		RegularTimePoint entity = new RegularTimePoint();
//
//		entity.setIdentifiedObject(ident);
//
//		Resource resource = breaker.getResource();
//
//		Statement stmt = resource.getProperty(Esca60Vocab.REGULARTIMEPOINT_INTERVALSCHEDULE);
//		if (stmt != null && stmt.getResource() != null){
//			entity.setIntervalSchedule(stmt.getResource().getLocalName());
//		}
//
//		entity.setValue1(resource.getProperty(Esca60Vocab.REGULARTIMEPOINT_VALUE1).getDouble());
//		entity.setValue2(resource.getProperty(Esca60Vocab.REGULARTIMEPOINT_VALUE2).getDouble());
//
//		dao.persist(entity);
//	}
//
//	private static void storeBreaker(NodeBreakerDao dao, EscaType breaker){
//
//		IdentifiedObject ident = new IdentifiedObject();
//
//		populateIdentityObjects(breaker, ident);
//
//		Breaker entity = new Breaker();
//
//		entity.setIdentifiedObject(ident);
//
//
//		Resource resource = breaker.getResource();
//
//		entity.setSwitchNormalOpen(resource.getProperty(Esca60Vocab.SWITCH_NORMALOPEN).getBoolean());
//		entity.setRatedCurrent(resource.getProperty(Esca60Vocab.BREAKER_RATEDCURRENT).getDouble());
//
//		entity.setMemberOfEquipmentContainer(
//				getPropertyString(resource, Esca60Vocab.EQUIPMENT_MEMBEROF_EQUIPMENTCONTAINER));
//		entity.setConductingEquipmentBaseVoltage(
//				getPropertyString(resource, Esca60Vocab.CONDUCTINGEQUIPMENT_BASEVOLTAGE));
//
//		dao.persist(entity);
//
//		System.out.println("\n");
//	}
//
//	private static void storeTransformerWinding(NodeBreakerDao dao, EscaType breaker){
//		IdentifiedObject ident = new IdentifiedObject();
//
//		populateIdentityObjects(breaker, ident);
//
//		TransformerWinding entity = new TransformerWinding();
//
//		entity.setIdentifiedObject(ident);
//
//		Resource resource = breaker.getResource();
//
//		entity.setB(resource.getProperty(Esca60Vocab.TRANSFORMERWINDING_B).getDouble());
//		entity.setG(resource.getProperty(Esca60Vocab.TRANSFORMERWINDING_G).getDouble());
//		entity.setR(resource.getProperty(Esca60Vocab.TRANSFORMERWINDING_R).getDouble());
//		entity.setX(resource.getProperty(Esca60Vocab.TRANSFORMERWINDING_X).getDouble());
//		entity.setRatedU(resource.getProperty(Esca60Vocab.TRANSFORMERWINDING_RATEDU).getDouble());
//		entity.setRatedS(resource.getProperty(Esca60Vocab.TRANSFORMERWINDING_RATEDS).getDouble());
//
//		dao.persist(entity);
//	}
//
//	private static Object buildIdentified(Class cls, EscaType escaType){
//
//		Identified identified = null;
//		try {
//			IdentifiedObject obj = new IdentifiedObject();
//			populateIdentityObjects(escaType, obj);
//			identified = (Identified) cls.newInstance();
//			identified.setIdentifiedObject(obj);
//		} catch (InstantiationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return identified;
//	}

//	public static SynchronousMachine createSynchronousMachine(EscaType escaType){
//		SynchronousMachine s = new SynchronousMachine();
//		populateIdentityObjects(escaType, s);
//		return s;
//	}
//
//	public static Substation createSubstation(EscaType escaType){
//		Substation s = new Substation();
//		populateIdentityObjects(escaType, s);
//		return s;
//	}
//
//	public static Terminal createTerminal(EscaType escaType){
//		Terminal t = new Terminal();
//		populateIdentityObjects(escaType, t);
//		return t;
//	}
//
//	public static VoltageLevel createVoltageLevel(EscaType escaType){
//		VoltageLevel e = new VoltageLevel();
//		populateIdentityObjects(escaType, e);
//		return e;
//	}
//
//	public static GeographicalRegion createGeographicRegion(EscaType escaType){
//		GeographicalRegion g = new GeographicalRegion();
//		populateIdentityObjects(escaType, g);
//		return g;
//	}
//
//	public static void populateGeographicRegions(){
//		for (String d : typeMap.keySet()){
//			EscaType escaType = typeMap.get(d);
//			String dataType = escaType.getDataType();
//			NodeBreakerDataType entity = null;
//
//			if ("GeographicalRegion".equals(dataType)){
//				entity = createGeographicRegion(escaType);
//			}
//
//			if(entity != null){
//				identifiedMap.put(((IdentifiedObject)entity).getMrid(), entity);
//				nodeBreakerDao.persist(entity);
//			}
//		}
//	}
//
//	public static String populateDataType(Class klass, String escaDataType){
//		int countAdded = 0;
//		for (String d : typeMap.keySet()){
//			EscaType escaType = typeMap.get(d);
//			String dataType = escaType.getDataType();
//			NodeBreakerDataType entity = null;
//
//			if (escaDataType.equals(dataType)){
//				try {
//					entity = (NodeBreakerDataType)klass.newInstance();
//					populateIdentityObjects(escaType, (IdentifiedObject)entity);
//				} catch (InstantiationException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (IllegalAccessException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}// createGeographicRegion(escaType);
//			}
//
//			if(entity != null){
//				identifiedMap.put(((IdentifiedObject)entity).getMrid(), entity);
//				countAdded+=1;
//			}
//		}
//
//		return "Added: "+countAdded+ " " + escaDataType;
//	}
//
//	public static void printIdentifiedMap(){
//		for (String d : identifiedMap.keySet()){
//			NodeBreakerDataType obj = identifiedMap.get(d);
//			System.out.println("Type: "+obj.getDataType()+"\n\t"+obj.toString());
//		}
//	}
//
//
//	public static void addToParent(String parentMrid, String methodName, Object objectToAdd){
//
//		Object parent = identifiedMap.get(parentMrid);
//		try {
//			Method method = parent.getClass().getMethod(methodName, objectToAdd.getClass());
//			method.invoke(method,  objectToAdd);
//		} catch (NoSuchMethodException | SecurityException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalArgumentException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InvocationTargetException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//	/**
//	 * Call a method on the passed mrid referenced objects.  The sinkMrid object will
//	 * be looked up from the  map.  The sink object will have its methodName called.
//	 * The methodName will be passed the sourceMrid's object.
//	 *
//	 * In addition if the source's main interface is not the interface that is
//	 * used as the parameter then the code will search the other interfaces before
//	 * raising an error.
//	 *
//	 * @param sinkMrid
//	 * @param sourceMrid
//	 * @param methodName
//	 * @throws InvalidArgumentException
//	 */
//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	public static void callMethod(String sinkMrid, String sourceMrid, String methodName) throws InvalidArgumentException{
//		Object sinkObj = identifiedMap.get(sinkMrid);
//		Object sourceObj = identifiedMap.get(sourceMrid);
//
//		if (sinkObj == null){
//			throw new InvalidArgumentException("sinkMrid not valid. ("+sinkMrid+")");
//		}
//
//		if (sourceObj == null){
//			throw new InvalidArgumentException("sourceMrid not valid. ("+sourceMrid+")");
//		}
//		Class sinkClass = sinkObj.getClass();
//		Class sourceClass = sourceObj.getClass();
//		Method method=null;
//		try {
//			method = sinkClass.getMethod(methodName, sourceClass);
//			method.invoke(sinkObj, sourceObj);
//		}
//		catch (NoSuchMethodException e){
//			boolean success = false;
//			for(Class c : sourceClass.getInterfaces()){
//				try{
//					sourceClass = c;
//					method = sinkClass.getMethod(methodName, sourceClass);
//					method.invoke(sinkObj, sourceObj);
//					success = true;
//				}
//				catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
//
//				}
//				if (success){
//					break;
//				}
//			}
//			if (!success){
//				System.out.println("No method takes any of the source interfaces as an argument.");
//				e.printStackTrace();
//			}
//		}
//		catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
//			e.printStackTrace();
//		}
//	}
//
//	/**
//	 * Call a on sink and pass it an instance of the sourceClass.
//	 *
//	 * @param sinkClass
//	 * @param sourceClass
//	 * @param escaType
//	 * @param escaPropertyName
//	 * @param methodName
//	 */
//	@SuppressWarnings("rawtypes")
//	public static void addSingularRelation(Class sinkClass, Class sourceClass,
//			String escaType, String escaPropertyName, String methodName){
//
//		for(EscaType t: typeMap.values()){
//			// escaType is the datatype associated with the child (nodebreaker model) class
//			// and the escaPropertyName should be a reference to the parent (nodebreaker model)
//			// object.
//			if (t.getDataType().equals(escaType)){
//
//				String sinkMrid = t.getMrid();
//				String sourceMrid = getPropertyString(t.getResource(), escaPropertyName);
//
//				try {
//					callMethod(sinkMrid, sourceMrid, methodName);
//				} catch (InvalidArgumentException e) {
//					System.err.println(e.getMessage());
//				}
//			}
//		}
//	}
//
//

//	/**
//	 * Add relations from and to parent and child using the passed child escaType and
//	 * escaPropertyName as the linking mechanism.
//	 *
//	 * The child class is expected to have a set<parenttype> method.
//	 * The parent class is expected to have an add<childtype> method.
//	 *
//	 * The child and parent class types are determined by class.getSimpleType().
//	 *
//	 * @param child
//	 * @param parent
//	 * @param escaType
//	 * @param escaPropertyName
//	 */
//	@SuppressWarnings("rawtypes")
//	public static void addRelation(Class child, Class parent, String escaType, String escaPropertyName){
//
//		for(EscaType t: typeMap.values()){
//			// escaType is the datatype associated with the child (nodebreaker model) class
//			// and the escaPropertyName should be a reference to the parent (nodebreaker model)
//			// object.
//			if (t.getDataType().equals(escaType)){
//
//				String mridChild = t.getMrid();
//				String mridParent = getPropertyString(t.getResource(), escaPropertyName);
//
//
//				// Assumption is that child will have a set<parentclassname> method and the
//				// child will have an add<parentclassname> method.
//				try {
//					callMethod(mridChild, mridParent, "set"+parent.getSimpleName());
//				} catch (InvalidArgumentException e) {
//					System.err.println(e.getMessage());
//				}
//				try {
//					callMethod(mridParent, mridChild, "add"+child.getSimpleName());
//				} catch (InvalidArgumentException e) {
//					System.err.println(e.getMessage());
//				}
//
//			}
//		}
//	}
//
//	public static void storeData(){
//		for(NodeBreakerDataType obj: identifiedMap.values()){
//			try{
//				nodeBreakerDao.persist(obj);
//			}
//			catch (Exception e){
//				System.err.println("couldn't save: "+obj.getClass().getSimpleName()+" "+obj.toString());
//			}
//		}
//	}



}
