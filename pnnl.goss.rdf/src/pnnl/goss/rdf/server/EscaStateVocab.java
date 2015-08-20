package pnnl.goss.rdf.server;

import java.io.File;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

public class EscaStateVocab {
	public static Model readModel(File modelData){
		FileManager.get().readModel(m, modelData.getAbsolutePath());
		return m;
	}

	private static Model m = ModelFactory.createDefaultModel();

    public static void main(String[] args) throws Exception {
    	
    }
}
