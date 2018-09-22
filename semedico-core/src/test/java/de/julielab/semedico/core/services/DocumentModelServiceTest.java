package de.julielab.semedico.core.services;


import de.julielab.semedico.core.entities.documents.DocumentDisplayContext;
import de.julielab.semedico.core.entities.documents.DocumentElement;
import de.julielab.semedico.core.entities.documents.DocumentModel;
import de.julielab.semedico.core.util.DocumentModelAccessException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class DocumentModelServiceTest {

    private static DocumentModelService documentModelService;

    @BeforeClass
    public static void setup() {
        documentModelService = new DocumentModelService();
    }

    @Test
    public void testLoadModel() throws Exception {
        DocumentModel model = documentModelService.getModel("testsource", DocumentDisplayContext.HITLIST);
        assertThat(model).isNotNull();
        assertThat(model.getElements()).hasSize(2).extracting(DocumentElement::getFieldName).
                contains("title", "abstracttext");
    }

    @Test
    public void testModelFileNotFound() {
        assertThatExceptionOfType(DocumentModelAccessException.class).isThrownBy(() -> documentModelService.getModel("nonexistentsource", DocumentDisplayContext.READER));


    }
}
