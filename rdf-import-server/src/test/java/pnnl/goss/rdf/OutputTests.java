package pnnl.goss.rdf;

import java.io.File;
import java.net.URL;

import pnnl.goss.rdf.server.Esca60Vocab;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;

public class OutputTests {
	private static final String ESCA_TEST = "esca60_cim.xml";
		
	public static void main(String[] args) {
		URL url = Thread.currentThread().getContextClassLoader().getResource(ESCA_TEST);
		File file = new File(url.getPath());

		// creates a new, empty in-memory model
		Model m = Esca60Vocab.readModel(file.getAbsoluteFile()); // ModelFactory.createDefaultModel();

		
/*		StmtIterator iter1 = m.listStatements();

		while (iter1.hasNext()) {
			Statement stmt = iter1.nextStatement();

			Resource subject = stmt.getSubject(); // get the subject
			System.out.println(subject.getURI());
		}*/
		
		StmtIterator stmtIter = m.listStatements(new SimpleSelector(null, RDF.type, Esca60Vocab.SUBSTATION_OBJECT));
		
		while(stmtIter.hasNext()){
			Statement stmt = stmtIter.nextStatement();
			
			
			System.out.println("    " + stmt.getPredicate()); //.asTriple().toString());
			
			StmtIterator propIter = stmt.getResource().listProperties();
			
			while(propIter.hasNext()){
				System.out.println(propIter.nextStatement().getObject().toString());
			}
		}
		
		if(true)return;
		
		ResIterator pathNameIter = m.listSubjectsWithProperty(Esca60Vocab.IDENTIFIEDOBJECT_PATHNAME);
		// Because subjects of statements are Resources, the method returned a ResIterator
		while (pathNameIter.hasNext()) {

		  // ResIterator has a typed nextResource() method
		  Resource res = pathNameIter.nextResource();

		  StmtIterator propIterator = res.listProperties();
		  
		  while(propIterator.hasNext()){
			  Statement propRes = propIterator.next();
			  
			  System.out.println(propRes.getPredicate());
		  }
		}
		
		
		

		
/*		StmtIterator iter = m.listStatements(null, RDF.type, Esca60Vocab.TerminalResource); // "http://iec.ch/TC57/2007/CIM-schema-cim12#Line"); // Esca60Vocab.LineResource); // null, null);

		while (iter.hasNext()) {
		    String entityID = iter.next().getSubject().getURI();
		    System.out.println(entityID);
		}*/
	}

}
