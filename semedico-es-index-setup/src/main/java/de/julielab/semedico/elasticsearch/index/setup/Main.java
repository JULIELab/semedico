package de.julielab.semedico.elasticsearch.index.setup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import de.julielab.java.utilities.FileUtilities;
import de.julielab.semedico.elasticsearch.index.setup.indexes.AllTypes;
import de.julielab.semedico.elasticsearch.index.setup.indexes.PubMedAbstracts;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.stream.Stream;

public class Main {

	public static void main(String[] args) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

//		AbstractSections abstractSections = new AbstractSections();
//		Chunks chunks = new Chunks();
//		Documents documents = new Documents();
//		Relations relations = new Relations();
//		Sentences sentences = new Sentences();
//		AllTypes allTypes = new AllTypes();
		PubMedAbstracts pmAbstracts = new PubMedAbstracts();

		File file = new File("mappings");
		if (!file.exists())
			file.mkdirs();

		Stream.of(pmAbstracts).forEach(index -> {
			String indexName = index.getClass().getSimpleName().toLowerCase();
			File indexMappingFile = new File(file.getAbsolutePath() + File.separator + indexName +".json");
			try (Writer w = FileUtilities.getWriterToFile(indexMappingFile)) {
				gson.toJson(index, w);
			} catch (JsonIOException | IOException e) {
				e.printStackTrace();
			}
		});
		
		System.out.println("Wrote mappings to " + file.getAbsolutePath());
	}

}
