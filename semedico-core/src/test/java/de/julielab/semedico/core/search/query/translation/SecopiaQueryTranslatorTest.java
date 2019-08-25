package de.julielab.semedico.core.search.query.translation;

import de.julielab.elastic.query.components.data.query.BoolClause;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SecopiaQueryTranslatorTest {
    @Test
    public void testSimple() {
        Registry registry = RegistryBuilder.buildAndStartupRegistry(SemedicoCoreTestModule.class);

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
        walker.walk(translator, parse.getParseTree());

        return translator.getQueryTranslation();
    }

    @Test
    public void testPhrase() {
        Registry registry = RegistryBuilder.buildAndStartupRegistry(SemedicoCoreTestModule.class);
        final SearchServerQuery queryTranslation = parseString("\"male mice\"", registry);
        assertThat(queryTranslation).isInstanceOf(MultiMatchQuery.class);
        MultiMatchQuery mmq = (MultiMatchQuery) queryTranslation;
        assertThat(mmq.type).isEqualTo(MultiMatchQuery.Type.phrase);
        registry.shutdown();
    }

    @Test
    public void testPhraseMixedWithTokens() {
        Registry registry = RegistryBuilder.buildAndStartupRegistry(SemedicoCoreTestModule.class);
        final SearchServerQuery queryTranslation = parseString("several \"male mice\" and 'a goat' went down the street", registry);
        assertThat(queryTranslation).isInstanceOf(BoolQuery.class);
        BoolQuery bq = (BoolQuery) queryTranslation;
        final SearchServerQuery secondQueryPart = bq.clauses.get(0).queries.get(1);
        assertThat(secondQueryPart).isInstanceOf(MultiMatchQuery.class);
        MultiMatchQuery mmq = (MultiMatchQuery) secondQueryPart;
        assertThat(mmq.query).isEqualTo("went down the street");
        registry.shutdown();
    }

    @Test
    public void testPhraseMixedWithTokens2() {
        Registry registry = RegistryBuilder.buildAndStartupRegistry(SemedicoCoreTestModule.class);
        final SearchServerQuery queryTranslation = parseString("several nice \"male mice\" met today 'a goat' on the moon", registry);
        assertThat(queryTranslation).isInstanceOf(BoolQuery.class);
        BoolQuery bq = (BoolQuery) queryTranslation;
        System.out.println(bq);
        assertThat(bq.clauses.get(0).queries).hasSize(5);
        assertThat(bq.clauses.get(0).occur).isEqualTo(BoolClause.Occur.MUST);
        List<MultiMatchQuery> subqueries = new ArrayList<>();
        for (SearchServerQuery ssq : bq.clauses.get(0).queries) {
            assertThat(ssq).isInstanceOf(MultiMatchQuery.class);
            subqueries.add((MultiMatchQuery) ssq);
        }
        assertThat(subqueries.get(0).query).isEqualTo("several nice");
        assertThat(subqueries.get(1).query).isEqualTo("male mice");
        assertThat(subqueries.get(2).query).isEqualTo("met today");
        assertThat(subqueries.get(3).query).isEqualTo("a goat");
        assertThat(subqueries.get(4).query).isEqualTo("on the moon");

        registry.shutdown();
    }

    @Test
    public void testUri() {
        Registry registry = RegistryBuilder.buildAndStartupRegistry(SemedicoCoreTestModule.class);
        final SearchServerQuery queryTranslation = parseString("water http://www.someontology.org/path/to/something#ID10 cake", registry);
        System.out.println(queryTranslation);
        // TODO do concept recognition and test the outcome here
    }

    @Test
    public void testSimple2() {
        Registry registry = RegistryBuilder.buildAndStartupRegistry(SemedicoCoreTestModule.class);
        final SearchServerQuery queryTranslation = parseString("title of the first document", registry);
        System.out.println(queryTranslation);
    }
}
