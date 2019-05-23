package de.julielab.semedico.bioportal;

import static org.junit.Assert.*;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.junit.Test;

import de.julielab.bioportal.ontologies.data.OntologyClass;

public class TestBioPortalTermImporter {

	@Test
	public void testLoadOntologyClasses() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Class<BioPortalTermImporter> importerClass = BioPortalTermImporter.class;
		Method method = importerClass.getDeclaredMethod("loadOntologyClasses", File.class, OntologyMetaData.class);
		method.setAccessible(true);
	
		File downloadedOntologyNamesDir = new File("src/test/resources/bioportal");
		OntologyMetaData metaData = new OntologyMetaData("BCO", "BCO");
		@SuppressWarnings("unchecked")
		List<OntologyClass> ontologyClasses = (List<OntologyClass>) method.invoke(new BioPortalTermImporter(null), downloadedOntologyNamesDir, metaData);
		
		assertEquals(116, ontologyClasses.size());
	}

}
