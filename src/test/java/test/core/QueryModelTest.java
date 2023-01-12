package test.core;

import java.io.ByteArrayInputStream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.resultset.ResultsFormat;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import sparql.streamline.core.Sparql;

public class QueryModelTest {

	private Model model;
	private String rdf = "@prefix : <http://example.org/resources/> ."
			+ "@prefix schema: <https://schema.org/> ."
			+ ":alice schema:name      \"Alice Cooper\" .\n"
			+ " \n"
			+ ":bob   schema:givenName \"Bob\", \"Robert\" ;\n"
			+ "       schema:lastName  \"Smith\" .\n"
			+ "\n"
			+ ":carol schema:name      \"Carol King\" ;\n"
			+ "       schema:givenName \"Carol\" ;\n"
			+ "       schema:lastName  \"King\" .\n"
			+ "";
	
	@Before
	public void setup() {
		model = ModelFactory.createDefaultModel();
		model.read(new ByteArrayInputStream(rdf.getBytes()), null, "turtle");
	}
	
	@Test
	public void queryASK() {
		try {
		String results = new String(Sparql.queryModel("ASK { ?s <https://schema.org/name> \"Alice Cooper\"}", model, null, null).toByteArray());
		Assert.assertTrue(results.contains("\"boolean\" : true"));
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	

	@Test
	public void queryASKFormatCSV() {
		try {
		String results = new String(Sparql.queryModel("ASK { ?s <https://schema.org/name> \"Alice Cooper\"}", model, ResultsFormat.FMT_RS_CSV, null).toByteArray());
		System.out.println(results);
		Assert.assertTrue(results.contains("\"boolean\" : true"));
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
