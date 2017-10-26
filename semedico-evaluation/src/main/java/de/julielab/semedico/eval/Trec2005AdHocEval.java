package de.julielab.semedico.eval;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.julielab.elastic.query.components.data.FieldTermItem;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.query.AggregationRequests;
import de.julielab.semedico.core.search.query.SentenceQuery;
import de.julielab.semedico.core.search.query.UserQuery;
import de.julielab.semedico.core.search.results.FieldTermsRetrievalResult;
import de.julielab.semedico.core.search.results.SingleSearchResult;
import de.julielab.semedico.core.search.services.IResultCollectorService;
import de.julielab.semedico.core.services.SearchService.SearchOption;
import de.julielab.semedico.core.services.interfaces.IQueryAnalysisService;
import de.julielab.semedico.core.services.interfaces.ISearchService;
import de.julielab.semedico.eval.services.SemedicoEvaluationModule;

public class Trec2005AdHocEval {

	private static final Logger log = LoggerFactory.getLogger(Trec2005AdHocEval.class);
	private IQueryAnalysisService queryAnalysisService;
	private ISearchService searchService;
	private Matcher topicMatcher = Pattern.compile("<([^>]+)>").matcher("");
	private IResultCollectorService resultCollectorService;

	public Trec2005AdHocEval(IQueryAnalysisService queryAnalysisService, ISearchService searchService,
			IResultCollectorService resultCollectorService) {
		this.queryAnalysisService = queryAnalysisService;
		this.searchService = searchService;
		this.resultCollectorService = resultCollectorService;
	}

	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("Usage: " + Trec2005AdHocEval.class.getSimpleName() + " <query file> <result file>");
			System.exit(1);
		}

		String queryFile;
		String resultFile;
		queryFile = args[0];
		resultFile = args[1];

		Registry registry = RegistryBuilder.buildAndStartupRegistry(SemedicoEvaluationModule.class);
		Trec2005AdHocEval eval = registry.autobuild(Trec2005AdHocEval.class);
		eval.evaluate(queryFile, resultFile);
		registry.shutdown();
	}

	/**
	 * From <url>http://skynet.ohsu.edu/trec-gen/2005protocol.html</url>: <br />
	 * <br />
	 * We collected other data about submitted runs besides the system output.
	 * One item was the run type, which fell into one of (at least) three
	 * categories:
	 * <ul>
	 * <li>Automatic - no manual intervention in building queries</li>
	 * <li>Manual - manual construction of queries but no further human
	 * interaction</li>
	 * <li>Interactive - completely interactive construction of queries and
	 * further interaction with system output</li>
	 * </ul>
	 * <p>
	 * Recall and precision for the ad hoc retrieval task wree calculated in the
	 * classic IR way, using the preferred TREC statistic of mean average
	 * precision (average precision at each point a relevant document is
	 * retrieved, also called MAP). This was done using the trec_eval program.
	 * The code for trec_eval is available at
	 * http://trec.nist.gov/trec_eval/trec_eval.7.3.tar.gz [note: This link is
	 * dead, we use a newer version instead, found at the TREC2005 evaluation
	 * directory in the NFS, at the time of writing:
	 * /data/data_corpora/genomics/trec2005].
	 * </p>
	 * <p>
	 * The trec_eval program requires two files for input. One file is the
	 * topic-document output, sorted by each topic and then subsorted by the
	 * order of the IR system output for a given topic. This format is required
	 * for official runs submitted to NIST to obtain official scoring.
	 * </p>
	 * <p>
	 * The topic-document ouptut should be formatted as follows:
	 * 
	 * <pre>
	 * 100 Q0 12474524 1 5567     tag1
	 * 100 Q0 12513833 2 5543     tag1
	 * 100 Q0 12517948 3 5000     tag1
	 * 101 Q0 12531694 4 2743     tag1
	 * 101 Q0 12545156 5 1456     tag1
	 * 102 Q0 12101238 1 3.0      tag1
	 * 102 Q0 12527917 2 2.7      tag1
	 * 103 Q0 11731410 1 .004     tag1
	 * 103 Q0 11861293 2 .0003    tag1
	 * 103 Q0 11861295 3 .0000001 tag1
	 * </pre>
	 * 
	 * </p>
	 * <p>
	 * where:
	 * <ul>
	 * <li>The first column is the topic number (100-149) for the 2005
	 * topics.</li>
	 * <li>The second column is the query number within that topic. This is
	 * currently unused and must always be Q0.</li>
	 * <li>The third column is the official PubMedID of the retrieved
	 * document.</li>
	 * <li>The fourth column is the rank the document is retrieved</li>
	 * <li>The fifth column shows the score (integer or floating point) that
	 * generated the ranking. This score MUST be in descending (non-increasing)
	 * order. The trec_eval program ranks documents based on the scores, not the
	 * ranks in column 4. If a submitter wants the exact ranking submitted to be
	 * evaluated, then the SCORES must reflect that ranking.</li>
	 * <li>The sixth column is called the "run tag" and must be a unique
	 * identifier across all runs submitted to TREC. Thus, each run tag should
	 * have a part that identifies the group and a part that distinguishes runs
	 * from that group. Tags are restricted to 12 or fewer letters and numbers,
	 * and *NO* punctuation, to facilitate labeling graphs and such with the
	 * tags.</li>
	 * </ul>
	 * </p>
	 * <p>
	 * The second file required for trec_eval is the relevance judgments, which
	 * are called "qrels" in TREC jargon. More information about qrels can be
	 * found at http://trec.nist.gov/data/qrels_eng/ . The qrels file is in the
	 * following format:
	 * 
	 * <pre>
	 * 100    0    12474524    1
	 * 101    0    12513833    1
	 * 101    0    12517948    1
	 * 101    0    12531694    1
	 * 101    0    12545156    1
	 * 102    0    12101238    1
	 * 102    0    12527917    1
	 * 103    0    11731410    1
	 * 103    0    11861293    1
	 * 103    0    11861295    1
	 * 103    0    12080468    1
	 * 103    0    12091359    1
	 * 103    0    12127395    1
	 * 103    0    12203785    1
	 * </pre>
	 * 
	 * </p>
	 * <p>
	 * where:
	 * <ul>
	 * <li>The first column is the topic number (100-149) for the 2005
	 * topics.</li>
	 * <li>The second column is always 0.</li>
	 * <li>The third column is the PubMedID of the document.</li>
	 * <li>The fourth column is always 1.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param queryFile
	 * @param resultFile
	 */
	private void evaluate(String queryFile, String resultFile) {
		log.info("Evaluation from query file {}, writing results to {}.", queryFile, resultFile);
		// 'raw' because those lines will still include markup indicating the
		// number of the topic, e.g. <101>
		List<String> queryLinesRaw = null;
		try {
			queryLinesRaw = IOUtils.readLines(new FileInputStream(queryFile));

			log.info("Querying Semedico for {} queries.", queryLinesRaw.size());
			long time = System.currentTimeMillis();
			// Build query objects
			List<TrecEvalQuery> evalQueries = queryLinesRaw.stream().filter(l -> !l.trim().isEmpty())
					.map(this::createTrecEvalQuery).collect(Collectors.toList());

			// Do the actual query
			evalQueries.forEach(q -> {
				log.debug("Dispatching query {}", q.query);
				q.resultFuture = searchService.search(
						new SentenceQuery(q.analyzedQuery,
								AggregationRequests.getFieldTermsByDocScoreRequest("pmids", "_uid", 1000)),
						EnumSet.of(SearchOption.NO_FIELDS, SearchOption.NO_HITS),
						resultCollectorService.getFieldTermsCollector("pmids", "pmids"));
				log.debug("Result future created");
			});

			List<TrecEvalResultRow> resultRows = evalQueries.stream().map(this::getResultRows)
					.flatMap(rows -> rows.stream()).collect(Collectors.toList());
			time = System.currentTimeMillis() - time;
			log.info("Document retrieval took {}ms ({}s).", time, time / 1000);

			File output = new File(resultFile);
			if (output.exists()) {
				log.info("Deleting existing result output file {}.", resultFile);
				output.delete();
			}
			log.info("Writing results to {}.", output.getAbsolutePath());
			Set<String> alreadySeenDocIds = new HashSet<>();
			for (TrecEvalResultRow resultRow : resultRows) {
				// Each document ID must only appear once in the result. Here,
				// we just take the highest scored appearance. It is not clear
				// at all that this is the best strategy: A document with
				// multiple high-ranked sentences might be more relevant than
				// another with a single, higher-ranked sentence.
				if (alreadySeenDocIds.add(resultRow.documentId))
					FileUtils.write(output, resultRow.toTsv() + "\n", "UTF-8", true);
			}
			log.info("Done.");
			
			String evalCommand = "data/eval_scripts/trec_eval.8.1/trec_eval -q -c -M1000 data/trec2005/genomics.qrels.txt output/adhoc2005.eval";
			log.info("Calling evaluation script with the following command line: {}", evalCommand);
			Process proc = Runtime.getRuntime().exec(evalCommand);
			try(InputStream is = proc.getInputStream()){
				String evaluation = IOUtils.toString(is);
				log.info(evaluation);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Given a query, adds the respective result rows to <tt>resultRows</tt>.
	 * 
	 * @param evalQuery
	 * @param resultRows
	 * @return
	 */
	public List<TrecEvalResultRow> getResultRows(TrecEvalQuery evalQuery) {
		List<TrecEvalResultRow> resultRows = new ArrayList<>();
		try {
			log.debug("Getting search result");
			SingleSearchResult<FieldTermsRetrievalResult> searchResult = evalQuery.resultFuture.get();
			log.debug("Search result retrieved");
			List<FieldTermItem> docIds = searchResult.getResult().getFieldTerms("pmids").collect(Collectors.toList());
			for (int i = 0; i < docIds.size(); ++i) {
				FieldTermItem docIdItem = docIds.get(i);
				TrecEvalResultRow resultRow = new TrecEvalResultRow();
				// the terms themselves look like this: items#9456348_Sentence0
				resultRow.documentId = ((String) docIdItem.term).split("[#_]")[1];
				// always the same, unused, see comment or TREC2005 protocol (or
				// don't, it's not important)
				resultRow.queryNumber = "Q0";
				// the ranks are 1-based, so add one to the 0-based iteration
				// index
				resultRow.rank = i + 1;
				resultRow.score = (double) docIdItem.values.get(FieldTermItem.ValueType.MAX_DOC_SCORE);
				resultRow.tag = "firstApproach";
				resultRow.topic = evalQuery.topic;
				resultRows.add(resultRow);
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return resultRows;
	}

	/**
	 * Takes a line that starts with the topic ID and then states the query. The
	 * topic ID is expected to be embraced by brackets. Example:
	 * 
	 * <pre>
	 * <100>Describe the procedure or methods for how to "open up" a cell through a process called "electroporation."
	 * </pre>
	 * 
	 * @param line
	 * @return
	 */
	public TrecEvalQuery createTrecEvalQuery(String line) {
		// first, retrieve the topic number
		String topic = null;
		topicMatcher.reset(line);
		if (topicMatcher.find())
			topic = topicMatcher.group(1);
		// remove topic number markup
		String query = line.replaceAll("<[^>]+>", "");

		ParseTree analyzedQuery = queryAnalysisService.analyseQueryString(new UserQuery(query), 0, true);

		TrecEvalQuery trecEvalQuery = new TrecEvalQuery();
		trecEvalQuery.topic = topic;
		trecEvalQuery.analyzedQuery = analyzedQuery;
		trecEvalQuery.query = query;
		return trecEvalQuery;
	}

	public static class TrecEvalQuery {
		public String topic;
		public String query;
		public ParseTree analyzedQuery;
		public Future<SingleSearchResult<FieldTermsRetrievalResult>> resultFuture;

		@Override
		public String toString() {
			return "TrecEvalQuery [topic=" + topic + ", query=" + query + ", analyzedQuery=" + analyzedQuery + "]";
		}

	}

	private class TrecEvalResultRow {
		public String topic;
		public String queryNumber;
		public String documentId;
		public int rank;
		public double score;
		public String tag;

		public String toTsv() {
			List<Object> lines = new ArrayList<>();
			lines.add(topic);
			lines.add(queryNumber);
			lines.add(documentId);
			lines.add(rank);
			lines.add(score);
			lines.add(tag);
			return StringUtils.join(lines, "\t");
		}

		@Override
		public String toString() {
			return "TrecEvalResultRow [topic=" + topic + ", documentId=" + documentId + ", queryNumber=" + queryNumber
					+ ", rank=" + rank + ", score=" + score + ", tag=" + tag + "]";
		}

	}

}
