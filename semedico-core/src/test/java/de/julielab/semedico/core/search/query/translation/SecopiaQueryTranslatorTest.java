package de.julielab.semedico.core.search.query.translation;

import de.julielab.elastic.query.components.data.query.BoolQuery;
import de.julielab.elastic.query.components.data.query.MultiMatchQuery;
import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.semedico.core.entities.documents.SemedicoIndexField;
import de.julielab.semedico.core.parsing.SecopiaParse;
import de.julielab.semedico.core.services.SemedicoCoreTestModule;
import de.julielab.semedico.core.services.query.ISecopiaQueryAnalysisService;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
public class SecopiaQueryTranslatorTest {
    @Test
    public void testSimple() {
        Registry   registry = RegistryBuilder.buildAndStartupRegistry(SemedicoCoreTestModule.class);

        final String queryString = "MTOR and MOUSE and CAT and FISH";
        final SearchServerQuery queryTranslation = parseString(queryString, registry);

        assertThat(queryTranslation).isInstanceOf(MultiMatchQuery.class);
        MultiMatchQuery mmq = (MultiMatchQuery) queryTranslation;
        assertThat(mmq.query.split("\\s+")).hasSize(4).containsExactly("MTOR", "MOUSE", "CAT", "FISH");
        assertThat(mmq.operator).isEqualTo("and");

        registry.shutdown();
    }

    private SearchServerQuery parseString(String queryString, Registry registry) {
        final ISecopiaQueryAnalysisService qas = registry.getService(ISecopiaQueryAnalysisService.class);
        final SecopiaParse parse = qas.analyseQueryString(queryString);
        final SecopiaQueryTranslator translator = new SecopiaQueryTranslator(parse.getQueryTokens(), Collections.singleton(new SemedicoIndexField("testfield")), ConceptTranslation.ID);
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk( translator, parse.getParseTree());

        return translator.getQueryTranslation();
    }

    @Test
    public void testPhrase() {
        Registry   registry = RegistryBuilder.buildAndStartupRegistry(SemedicoCoreTestModule.class);
        final SearchServerQuery queryTranslation = parseString("\"male mice\"", registry);
        assertThat(queryTranslation).isInstanceOf(MultiMatchQuery.class);
        MultiMatchQuery mmq = (MultiMatchQuery) queryTranslation;
        assertThat(mmq.type).isEqualTo(MultiMatchQuery.Type.phrase);
        registry.shutdown();
    }

    @Test
    public void testPhraseMixedWithTokens() {
        Registry   registry = RegistryBuilder.buildAndStartupRegistry(SemedicoCoreTestModule.class);
        final SearchServerQuery queryTranslation = parseString("several \"male mice\" and 'a goat' went down the street", registry);
        assertThat(queryTranslation).isInstanceOf(BoolQuery.class);
        BoolQuery bq = (BoolQuery) queryTranslation;
        final SearchServerQuery secondQueryPart = bq.clauses.get(0).queries.get(1);
        assertThat(secondQueryPart).isInstanceOf(MultiMatchQuery.class);
        MultiMatchQuery mmq = (MultiMatchQuery) secondQueryPart;
        assertThat(mmq.query).isEqualTo("went down the street");
        registry.shutdown();
    }
}
