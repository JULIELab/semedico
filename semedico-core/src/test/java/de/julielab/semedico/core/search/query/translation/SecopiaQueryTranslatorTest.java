package de.julielab.semedico.core.search.query.translation;

import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.semedico.core.entities.documents.SemedicoIndexField;
import de.julielab.semedico.core.parsing.SecopiaParse;
import de.julielab.semedico.core.services.SemedicoCoreTestModule;
import de.julielab.semedico.core.services.query.ISecopiaQueryAnalysisService;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.testng.annotations.Test;

public class SecopiaQueryTranslatorTest {
    private Registry registry;
    @Test
    public void testSimple() {
        registry = RegistryBuilder.buildAndStartupRegistry(SemedicoCoreTestModule.class);
        final ISecopiaQueryAnalysisService qas = registry.getService(ISecopiaQueryAnalysisService.class);
        final SecopiaParse parse = qas.analyseQueryString("MTOR and MOUSE and CAT and FISH");
        final SecopiaQueryTranslator translator = new SecopiaQueryTranslator(parse.getQueryTokens(), new SemedicoIndexField("testfield"), ConceptTranslation.ID);
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk( translator, parse.getParseTree());

        final SearchServerQuery queryTranslation = translator.getQueryTranslation();

        System.out.println(queryTranslation);
    }
}
