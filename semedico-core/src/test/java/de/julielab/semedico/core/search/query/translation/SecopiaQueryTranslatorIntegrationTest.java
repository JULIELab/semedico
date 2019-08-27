package de.julielab.semedico.core.search.query.translation;

import de.julielab.elastic.query.components.data.query.MultiMatchQuery;
import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.neo4j.plugins.datarepresentation.constants.NodeIDPrefixConstants;
import de.julielab.semedico.core.entities.documents.SemedicoIndexField;
import de.julielab.semedico.core.parsing.SecopiaParse;
import de.julielab.semedico.core.services.DictionaryEntry;
import de.julielab.semedico.core.services.SemedicoCoreTestModule;
import de.julielab.semedico.core.services.query.ISecopiaQueryAnalysisService;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.ioc.annotations.ImportModule;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
@Test(groups = {"integration", "neo4j"})
public class SecopiaQueryTranslatorIntegrationTest {

    private SearchServerQuery parseString(String queryString, Registry registry) {
        final ISecopiaQueryAnalysisService qas = registry.getService(ISecopiaQueryAnalysisService.class);
        final SecopiaParse parse = qas.analyseQueryString(queryString);
        final SecopiaQueryTranslator translator = new SecopiaQueryTranslator(parse.getQueryTokens(), Collections.singleton(new SemedicoIndexField("testfield")), ConceptTranslation.ID);
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(translator, parse.getParseTree());

        return translator.getQueryTranslation();
    }



    @Test
    public void testConceptPhrase() {
        Registry registry = RegistryBuilder.buildAndStartupRegistry(ConceptPhraseTestModule.class);
        final SearchServerQuery queryTranslation = parseString("\"male mice\"", registry);
        assertThat(queryTranslation).isInstanceOf(MultiMatchQuery.class);
        MultiMatchQuery mmq = (MultiMatchQuery) queryTranslation;
        assertThat(mmq.query).isEqualTo("male " + NodeIDPrefixConstants.TERM + 15);
        assertThat(mmq.type).isEqualTo(MultiMatchQuery.Type.phrase);

        registry.shutdown();
    }

    @ImportModule(SemedicoCoreTestModule.class)
    public static class ConceptPhraseTestModule {
        public void contributeTermDictionaryChunker(Configuration<DictionaryEntry> configuration) {
            configuration.add(new DictionaryEntry("mice", NodeIDPrefixConstants.TERM + 15));
        }
    }
}
