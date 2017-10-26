package de.julielab.semedico.eval;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.junit.Test;

import com.google.common.collect.Sets;

import de.julielab.semedico.eval.Trec2005AdHocEval.TrecEvalQuery;
import de.julielab.semedico.eval.services.SemedicoEvaluationModule;

public class Trec2005AdHocEvalTest {
	
	@Test
	public void testCreateTrecEvalQuery() throws Exception {
		String topic = "<100>Describe the procedure or methods for how to \"open up\" a cell through a process called \"electroporation.\"";
		Registry registry = RegistryBuilder.buildAndStartupRegistry(SemedicoEvaluationModule.class);
		Trec2005AdHocEval eval = registry.autobuild(Trec2005AdHocEval.class);
		Method m = eval.getClass().getDeclaredMethod("createTrecEvalQuery", String.class);
		m.setAccessible(true);
		TrecEvalQuery query = (TrecEvalQuery) m.invoke(eval, topic);
		assertEquals("100", query.topic);
		assertTrue(topic.contains(query.query));
		Set<String> expectedWords = new HashSet<>(Arrays.asList("process", "called", "open up", "methods", "procedure", "electroporation", "cell"));
		Set<String> actualWords = query.analyzedQuery.getTextNodes().stream().map(n -> n.getText()).collect(Collectors.toSet());
		assertEquals(expectedWords.size(), Sets.intersection(expectedWords, actualWords).size());
	}
	
//	@Test
//	public void testEvaluate() {
//		Registry registry = RegistryBuilder.buildAndStartupRegistry(SemedicoEvaluationModule.class);
//		Trec2005AdHocEval eval = registry.autobuild(Trec2005AdHocEval.class);
//		eval.evaluate(queryFile, resultFile);
//	}
}
