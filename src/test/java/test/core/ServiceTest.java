package test.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sparql.resultset.ResultsFormat;
import org.junit.Test;

import sparql.functions.Leveshtein;
import sparql.streamline.core.Sparql;
import sparql.streamline.core.SparqlEndpoint;
import sparql.streamline.core.SparqlEndpointConfiguration;
import sparql.streamline.exception.SparqlConfigurationException;
import sparql.streamline.exception.SparqlQuerySyntaxException;
import sparql.streamline.exception.SparqlRemoteEndpointException;

public class ServiceTest {

	//PREFIX f: <java:sparql.functions.>
	private static final String ns = "https://andreacimminoarriaga.github.io/sparql-streamline/functions#levenshtein";
	@Test
	public void test01() throws SparqlQuerySyntaxException, SparqlRemoteEndpointException {
		String query1 = "\n"
				+ "BASE <http://localhost:4567/>\n"
				+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
				+ "SELECT ?x ?name ?url\n"
				+ "WHERE {\n";
			
		String query2 = "}";
		
		FunctionRegistry.get().put(ns, Leveshtein.class) ;
		ByteArrayOutputStream results = Sparql.queryService("PREFIX fn: <https://andreacimminoarriaga.github.io/sparql-streamline/functions#> \n SELECT ?t ?v { SERVICE <https://dbpedia.org/sparql> {?s a ?t . } BIND( fn:levenshtein(?s, ?t) AS ?v ) }LIMIT 10", ResultsFormat.FMT_RS_JSON, null);
		System.out.println(results.toString());
		
		String tripletas = "";
		String ns = "";
		String lang = "TURTLE";
		Model model = ModelFactory.createDefaultModel();
		model.read(new ByteArrayInputStream(tripletas.getBytes()), ns, lang);
	
		StringBuilder br = new StringBuilder();
		
		br.append(query1);
		Writer writer = new StringWriter();
		model.write(writer, "NT");
		br.append(writer.toString());
		br.append(query2);
		
		
	}
	
	@Test
	public void test02() throws SparqlQuerySyntaxException, SparqlRemoteEndpointException, SparqlConfigurationException {
		System.out.println("Test2");
		SparqlEndpointConfiguration sec = new SparqlEndpointConfiguration();
		sec.setEndpointQuery("http://localhost:7200/repositories/test2");
		sec.setEndpointUpdate("http://localhost:7200/repositories/test2/statements");
		sec.setUsername("admin");
		sec.setPassword("root");
		SparqlEndpoint se = new SparqlEndpoint(sec);
		System.out.println(se.query("SELECT * WHERE{?s ?p ?o}", ResultsFormat.FMT_RS_JSON).toString());
	}
	
	@Test
	public void test03() throws SparqlQuerySyntaxException, SparqlRemoteEndpointException, SparqlConfigurationException {
		System.out.println("Test2");
		SparqlEndpointConfiguration sec = new SparqlEndpointConfiguration();
		sec.setEndpointQuery("http://localhost:7200/repositories/t est2");
		sec.setEndpointUpdate("http://localhost:7200/repositories/test2/statements");
		sec.setUsername("admin");
		sec.setPassword("root");
		SparqlEndpoint se = new SparqlEndpoint(sec);
		//System.out.println(se.update("SELECT * WHERE{?s ?p ?o}", ResultsFormat.FMT_RS_JSON).toString());
	}
}
