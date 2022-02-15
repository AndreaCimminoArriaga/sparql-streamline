package test.core;

import java.io.ByteArrayOutputStream;

import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sparql.resultset.ResultsFormat;
import org.junit.Test;

import sparql.functions.Leveshtein;
import sparql.streamline.core.Sparql;
import sparql.streamline.exception.SparqlQuerySyntaxException;
import sparql.streamline.exception.SparqlRemoteEndpointException;

public class ServiceTest {

	//PREFIX f: <java:sparql.functions.>
	private static final String ns = "https://andreacimminoarriaga.github.io/sparql-streamline/functions#levenshtein";
	@Test
	public void test01() throws SparqlQuerySyntaxException, SparqlRemoteEndpointException {
		FunctionRegistry.get().put(ns, Leveshtein.class) ;
		ByteArrayOutputStream results = Sparql.queryService("PREFIX fn: <https://andreacimminoarriaga.github.io/sparql-streamline/functions#> \n SELECT ?t ?v { SERVICE <https://dbpedia.org/sparql> {?s a ?t . } BIND( fn:levenshtein(?s, ?t) AS ?v ) }LIMIT 10", ResultsFormat.FMT_RS_JSON, null);
		System.out.println(results.toString());
	}
}
