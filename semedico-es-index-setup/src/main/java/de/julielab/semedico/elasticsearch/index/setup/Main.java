package de.julielab.semedico.elasticsearch.index.setup;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;

import de.julielab.java.utilities.FileUtilities;
import de.julielab.semedico.elasticsearch.index.setup.indexes.AbstractSections;
import de.julielab.semedico.elasticsearch.index.setup.indexes.Chunks;
import de.julielab.semedico.elasticsearch.index.setup.indexes.Documents;
import de.julielab.semedico.elasticsearch.index.setup.indexes.Relations;
import de.julielab.semedico.elasticsearch.index.setup.indexes.Sentences;

public class Main {

	public static void main(String[] args) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		AbstractSections abstractSections = new AbstractSections();
		Chunks chunks = new Chunks();
		Documents documents = new Documents();
		Relations relations = new Relations();
		Sentences sentences = new Sentences();

		File file = new File("mappings");
		if (!file.exists())
			file.mkdirs();

		Stream.of(abstractSections, chunks, documents, relations, sentences).forEach(index -> {
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
