package pnnl.goss.rdf;

import pnnl.goss.rdf.server.EscaVocab;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

public class RdfModelFixtures {
	
	public static Model get10TerminalSystem(){
		Model model = ModelFactory.createDefaultModel();
		
		addTypeToModel(model, EscaVocab.TERMINAL_OBJECT, "Term", 10);
		addTypeToModel(model, EscaVocab.CONNECTIVITYNODE_OBJECT, "Conn", 6);
		
		return model;
	}
	
	private static void addTypeToModel(Model model, Resource resType, String prefix, int numItems){
		for(int i=0; i< numItems; i++){
			String id = EscaVocab.URI_ROOT+prefix + i;
			Resource res = model.createResource(id, resType);
			res.addProperty(EscaVocab.IDENTIFIEDOBJECT_NAME, model.createLiteral(prefix+i));				
		}
	}
}
