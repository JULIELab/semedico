package de.julielab.semedico.core.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import de.julielab.semedico.core.util.DocumentModelAccessException;
import org.apache.tapestry5.ioc.internal.util.ClasspathResource;
import org.yaml.snakeyaml.Yaml;

import de.julielab.semedico.core.entities.documents.DocumentDisplayContext;
import de.julielab.semedico.core.entities.documents.DocumentModel;
import de.julielab.semedico.core.services.interfaces.IDocumentModelService;

public class DocumentModelService implements IDocumentModelService {

    private Yaml yaml;

    private Map<String, DocumentModel> cache;


    public DocumentModelService() {
        yaml = new Yaml();
        cache = new HashMap<>();
    }

    @Override
    public DocumentModel getModel(String source, DocumentDisplayContext context) throws DocumentModelAccessException {
        String sourceFile = String.format("/META-INF/documentmodels/%s_%s.yml", source.toLowerCase(), context.name().toLowerCase());
        DocumentModel model = cache.get(sourceFile);
        if (model == null) {
            ClasspathResource resource = new ClasspathResource(sourceFile);
            final InputStream is;
            try {
                is = resource.openStream();
            } catch (IOException e) {
                throw new DocumentModelAccessException(e);
            }
            if (is == null)
                throw new DocumentModelAccessException("The document model with facetSource " + source + " and display context " + context.name() + " should be located at " + sourceFile + ". However, no such file could be found.");

            model = yaml.loadAs(is, DocumentModel.class);
            cache.put(sourceFile, model);
        }

        return model;

    }

}
