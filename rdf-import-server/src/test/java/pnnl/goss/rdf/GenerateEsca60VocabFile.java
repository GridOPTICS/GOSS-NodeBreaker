/*
	Copyright (c) 2014, Battelle Memorial Institute
    All rights reserved.
    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:
    1. Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.
    2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
    ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
    WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
     
    DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
    ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
    (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
    LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
    ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
    The views and conclusions contained in the software and documentation are those
    of the authors and should not be interpreted as representing official policies,
    either expressed or implied, of the FreeBSD Project.
    This material was prepared as an account of work sponsored by an
    agency of the United States Government. Neither the United States
    Government nor the United States Department of Energy, nor Battelle,
    nor any of their employees, nor any jurisdiction or organization
    that has cooperated in the development of these materials, makes
    any warranty, express or implied, or assumes any legal liability
    or responsibility for the accuracy, completeness, or usefulness or
    any information, apparatus, product, software, or process disclosed,
    or represents that its use would not infringe privately owned rights.
    Reference herein to any specific commercial product, process, or
    service by trade name, trademark, manufacturer, or otherwise does
    not necessarily constitute or imply its endorsement, recommendation,
    or favoring by the United States Government or any agency thereof,
    or Battelle Memorial Institute. The views and opinions of authors
    expressed herein do not necessarily state or reflect those of the
    United States Government or any agency thereof.
    PACIFIC NORTHWEST NATIONAL LABORATORY
    operated by BATTELLE for the UNITED STATES DEPARTMENT OF ENERGY
    under Contract DE-AC05-76RL01830
*/
package pnnl.goss.rdf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;

public class GenerateEsca60VocabFile {

	private static final String ESCA_TEST = "esca60_cim.xml";
	private static final String START_PROPERTY = "\tpublic final static Property ";
	private static final String START_RESOURCE = "\tpublic final static Resource ";
	private static final String END_FINAL_NAME = ";\n";
		
	private class ResourceTree{
		private String namespace;
		private String objectType;
		private HashSet<String> properties;
		
		public ResourceTree(){
			properties = new HashSet<String>();
		}
		
		public ResourceTree(String namespace, String objectType){
			this();
			this.objectType = objectType;
			this.namespace = namespace;
		}
		
		public String getObjectType(){
			return objectType;
		}
		
		public void setObjectType(String objectType){
			this.objectType = objectType;
		}
		
		public String getNamespace(){
			return namespace;
		}
		
		public void setNamespace(String namespace){
			this.namespace = namespace;
		}
		
		public void addProperty(String property){
			this.properties.add(property);
		}
		
		public Collection<String> getProperties(){
			return properties;
		}
	}

	public static void main(String[] args) {
		GenerateEsca60VocabFile tests = new GenerateEsca60VocabFile();
		tests.run();
	}

	public void run() {

		URL url = Thread.currentThread().getContextClassLoader().getResource(ESCA_TEST);
		File file = new File(url.getPath());

		// creates a new, empty in-memory model
		Model m = ModelFactory.createDefaultModel();

		// load some data into the model
		System.out.println(file.getAbsolutePath());
		FileManager.get().readModel(m, file.getAbsolutePath());

		// generate some output
		//showStatements(m);
		showModelSize(m);
		HashSet<String> properties = buildPropertySet(m);
		HashSet<String> classes = buildClassSet(m);
		HashMap<String, ResourceTree> map = buildResourceTrees(m);
		HashMap<String, String> namespaceVarMap = buildNamespaceVarMap(map);
		writeVocab(properties, classes,  namespaceVarMap);
		
		//writeEsca60Vocab(map, namespaceVarMap);
		/*for(String k: map.keySet()){
			ResourceTree res = map.get(k);
			System.out.println("object: "+k);
			for(String p:res.getProperties()){
				System.out.println("property: "+p);
			}
		}*/
		//buildFinalStatics(m);
		// listCheeses( m );
	}
	
	protected HashMap<String, String> buildNamespaceVarMap(HashMap<String, ResourceTree> resTreeMap){
		HashMap<String, String> namespaceVarMap = new HashMap<String, String>();
		int uriNum = 0;
		String uriPrefix = "uri";
		for(ResourceTree t: resTreeMap.values()){
			if(!namespaceVarMap.containsKey(t.getNamespace())){
				namespaceVarMap.put(t.getNamespace(), uriPrefix + uriNum);
				uriNum++;
			}
		}
		return namespaceVarMap;		
	}
	
	protected String getHeader(){
		String header = "package pnnl.goss.rdf;\n\n"
				+ "import java.io.File;\n\n"
				+ "import com.hp.hpl.jena.rdf.model.Model;\n"
				+ "import com.hp.hpl.jena.rdf.model.ModelFactory;\n"
				+ "import com.hp.hpl.jena.rdf.model.Property;\n"
				+ "import com.hp.hpl.jena.rdf.model.Resource;\n"
				+ "import com.hp.hpl.jena.util.FileManager;\n\n"
				+ "public class Esca60Vocab {\n\n" 
				+ "\tprotected static final String uri =\"http://fpgi.pnnl.gov/esca60\";\n\n"
				+ "\t/** returns the URI for this schema\n"
				+ "\t * @return the URI for this schema\n"
				+ "\t */\n\n"
				+ "\tpublic static String getURI() {\n"
		        + "\t\treturn uri;\n"
				+ "\t}\n\n"
		        + "\tpublic static Model readModel(File modelData){\n"
				+ "\t\tFileManager.get().readModel(m, modelData.getAbsolutePath());\n"
		        + "\t\treturn m;\n"
				+ "\t}\n\n"
		        + "\tprivate static Model m = ModelFactory.createDefaultModel();\n\n";

		return header;
	}
	
	private String fixPropertyVariableName(String property){
		return property.replace(".", "_").toUpperCase();
	}
	
	private void writeVocab(HashSet<String> propertySet, HashSet<String> classes, HashMap<String, String> namespaceVarMap){
		OutputStreamWriter os = null;
		try {
			File file = new File("c:/scratch/esca.java");
			os = new OutputStreamWriter(new FileOutputStream(file));
			os.write(getHeader());
			
			for(String k: namespaceVarMap.keySet()){
				os.write("\tprivate final static String "+namespaceVarMap.get(k)+ " = \"" + k + "\";\n");
			}
			os.write("\n");
			
			for(String p: classes){
				String line = START_RESOURCE + fixPropertyVariableName(p+"_OBJECT") + " = m.createProperty(uri0 + \"" + p + "\");\n";
				os.write(line);
				System.out.println("class: "+p);
			}
		
			for(String p: propertySet){
				String line = START_PROPERTY + fixPropertyVariableName(p) + " = m.createProperty(uri0 + \"" + p + "\");\n";
				os.write(line);
				System.out.println("property: "+p);
			}
			
			
			os.write("}");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if (os != null){
				try {
					os.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	@Deprecated
	protected void writeEsca60Vocab(HashMap<String, ResourceTree> resTreeMap, HashMap<String, String> namespaceVarMap){
		OutputStreamWriter os = null;
		HashSet<String> addedProperties = new HashSet<String>();
		try {
			File file = new File("c:/scratch/esca.java");
			os = new OutputStreamWriter(new FileOutputStream(file));
			os.write(getHeader());
			
			for(String k: namespaceVarMap.keySet()){
				os.write("\tprivate final static String "+namespaceVarMap.get(k)+ " = \"" + k + "\";\n");
			}
		
			for(String k: resTreeMap.keySet()){
				ResourceTree res = resTreeMap.get(k);
				String line = START_RESOURCE + k + "Resource = m.createResource("+ namespaceVarMap.get(res.getNamespace()) + " + \"" + res.getObjectType() + "\");\n";
				os.write(line);
				System.out.println("object: "+k);
				for(String p:res.getProperties()){
					if (!addedProperties.contains(p)){
						line = START_PROPERTY + p + " = m.createProperty("+ namespaceVarMap.get(res.getNamespace()) + " + \"" + p + "\");\n";
						os.write(line);
						System.out.println("property: "+p);
						addedProperties.add(p);
					}
				}
			}
			
			os.write("}");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if (os != null){
				try {
					os.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	protected HashSet<String> buildClassSet(Model m){
		StmtIterator iter = m.listStatements();
		HashSet<String> classNames = new HashSet<String>();
		
		while (iter.hasNext()) {
			Statement stmt = iter.nextStatement();

			Resource subject = stmt.getSubject(); // get the subject
			Property predicate = stmt.getPredicate(); // get the predicate	
						
			String property_name = predicate.getLocalName();
			
			String fields[] = property_name.split("\\.");
			
			classNames.add(fields[0]);
		}
		
		return classNames;		
		
	}
	
	protected HashSet<String> buildPropertySet(Model m){
		StmtIterator iter = m.listStatements();
		HashSet<String> propertyNames = new HashSet<String>();
		
		while (iter.hasNext()) {
			Statement stmt = iter.nextStatement();

			Resource subject = stmt.getSubject(); // get the subject
			Property predicate = stmt.getPredicate(); // get the predicate	
						
			String property_name = predicate.getLocalName();
			
			propertyNames.add(property_name);
		}
		
		return propertyNames;		
		
	}
	
	@Deprecated
	protected HashMap<String, ResourceTree> buildResourceTrees(Model m){
		HashMap<String, ResourceTree> map = new HashMap<String, ResourceTree>();
		
		StmtIterator iter = m.listStatements();
		StringBuilder sb = new StringBuilder();
			

		while (iter.hasNext()) {
			Statement stmt = iter.nextStatement();

			Resource subject = stmt.getSubject(); // get the subject
			Property predicate = stmt.getPredicate(); // get the predicate	
			String[] fields = predicate.getLocalName().split("\\."); //.split("\."); 
			
			
			//System.out.println(predicate.getNameSpace());
						
			if (fields.length > 1){
				String objectType = fields[0];
				String property = fields[1];
				String namespace = predicate.getNameSpace();
				ResourceTree resTree;
				
				if (!map.containsKey(objectType)){
					resTree = new ResourceTree(namespace, objectType);
					map.put(objectType, resTree);
				}
				else{
					resTree = map.get(objectType);
				}
				
				resTree.addProperty(property);
			}
			else if (fields.length == 1){
				/*System.out.println("Adding resource??? "+fields[0]);
				nsMap.put(fields[0], predicate.getNameSpace());
				resourceSet.add(fields[0]);
				nameSpaces.add(predicate.getNameSpace());*/
			}
		}
		return map;		
	}
	
	
	protected void buildFinalStatics(Model m){
		HashSet<String> resourceSet = new HashSet<String>();
		HashSet<String> propertySet = new HashSet<String>();
		HashSet<String> nameSpaces = new HashSet<String>();
		HashMap<String, String> nsMap = new HashMap<String, String>();
		HashMap<String, HashSet<String>> resourcePropertyMap = new HashMap<String, HashSet<String>>();
		StmtIterator iter = m.listStatements();
		StringBuilder sb = new StringBuilder();
		
		

		while (iter.hasNext()) {
			Statement stmt = iter.nextStatement();

			Resource subject = stmt.getSubject(); // get the subject
			Property predicate = stmt.getPredicate(); // get the predicate	
			String[] fields = predicate.getLocalName().split("\\."); //.split("\."); 
			
			//System.out.println(predicate.getNameSpace());
						
			if (fields.length > 1){
				resourceSet.add(fields[0]);
				propertySet.add(fields[1]);
				nsMap.put(fields[0], predicate.getNameSpace());
				nsMap.put(fields[1], predicate.getNameSpace());
				nameSpaces.add(predicate.getNameSpace());
				
				if(!resourcePropertyMap.containsKey(fields[0])){
					resourcePropertyMap.put(fields[0], new HashSet<String>());
				}
				
				resourcePropertyMap.get(fields[0]).add(fields[1]);
			}
			else if (fields.length == 1){
				nsMap.put(fields[0], predicate.getNameSpace());
				resourceSet.add(fields[0]);
				nameSpaces.add(predicate.getNameSpace());
			}
		}
		
		
		
		// Sort by Alpha
		ArrayList<String> sortedResource = new ArrayList<String>(new TreeSet<String>(resourceSet));
		ArrayList<String> sortedProperties = new ArrayList<String>(new TreeSet<String>(propertySet));
		ArrayList<String> sortedNs = new ArrayList<String>(new TreeSet<String>(nameSpaces));
		
		/*ArrayList<String> sortedResourceMap = new ArrayList<String>(new TreeSet<String>(resourcePropertyMap.keySet()));
		
		for(String s: sortedResourceMap){
			for(String y: resourcePropertyMap.get(s)){
				System.out.println(s+"->"+y);
			}
			
		}*/
		
		HashMap<String, String> nsLookup = new HashMap<String, String>();
		int nsNum = 0;
		for(String ns: sortedNs){
			sb.append("protected static final String uri" + nsNum + " \"" + ns + "\";\n");
			nsLookup.put(ns, "uri" + nsNum);
			nsNum += 1;
		}
		
		for(String s: sortedResource){
			String resource = s.replace(".", "_").toUpperCase() + "PROPERTIES"; 
			sb.append(START_RESOURCE);
			sb.append(resource);
			sb.append(" = ");
			sb.append("m.createResource(" + nsLookup.get(nsMap.get(s)));
			sb.append("\"");
			sb.append(resource);			
			sb.append("\"");
			sb.append(")");
			sb.append(END_FINAL_NAME);
		}
		
		for(String s: sortedProperties){
			sb.append(START_PROPERTY);
			sb.append(s.replace(".", "_").toUpperCase());
			sb.append(" = ");
			sb.append("m.createProperty(uri + ");
			sb.append("\"");
			sb.append(s);
			sb.append("\"");
			sb.append(")");
			sb.append(END_FINAL_NAME);
		}
		
		System.out.println(sb);
	}

	protected void showStatements(Model m) {
		StmtIterator iter = m.listStatements();

		while (iter.hasNext()) {
			Statement stmt = iter.nextStatement();

			Resource subject = stmt.getSubject(); // get the subject
			Property predicate = stmt.getPredicate(); // get the predicate
			RDFNode object = stmt.getObject(); // get the object

			System.out.println(subject.getLocalName()); //.toString());
			System.out.println("\t"+predicate.getLocalName());
			System.out.print("\n\t" + predicate.toString() + " ");
			if (object instanceof Resource) {
				System.out.print("\n\t\t" + object.toString());
			} else {
				// object is a literal
				System.out.print("Literal \"" + object.toString() + "\"");
			}

			System.out.println(" .");

		}
	}

	/**
	 * Show the size of the model on stdout
	 */
	protected void showModelSize(Model m) {
		System.out.println(String.format("The model contains %d triples", m.size()));
	}
}
