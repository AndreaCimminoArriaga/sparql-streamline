package sparql.streamline.core;

import java.io.ByteArrayOutputStream;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.sparql.resultset.ResultsFormat;

import sparql.streamline.exception.SparqlQuerySyntaxException;
import sparql.streamline.exception.SparqlRemoteEndpointException;

public class Sparql {
	
	
	public static ByteArrayOutputStream queryService(String sparql, ResultsFormat format, String namespace) throws SparqlQuerySyntaxException, SparqlRemoteEndpointException {

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			Model model = ModelFactory.createDefaultModel();
			Query query = QueryFactory.create(sparql) ;
			QueryExecution qexec =  QueryExecutionFactory.create(query, model);
			
			if(query.isSelectType()) {
				ResultSetFormatter.output(stream, qexec.execSelect(), format);
			}else if(query.isAskType()) {
				ResultSetFormatter.output(stream, qexec.execAsk(), ResultsFormat.convert(format));
	        }else if(query.isConstructType()) {
	        	RDFFormat formatOutput = SparqlEndpoint.toRDFFormat(format);
	        	RDFWriter.create().source(qexec.execConstruct()).format(formatOutput).base(namespace).output(stream);
	        }else if(query.isDescribeType()) {
	        	RDFFormat formatOutput = SparqlEndpoint.toRDFFormat(format);
	        	RDFWriter.create().source(qexec.execDescribe()).format(formatOutput).base(namespace).output(stream);
	        }else {
	        	throw new SparqlRemoteEndpointException("Query not supported, provided one query SELECT, ASK, DESCRIBE or CONSTRUCT");
	        }
		}catch(QueryException e) {
			e.printStackTrace();
			throw new SparqlQuerySyntaxException(e.toString());
        }catch(Exception e) {
        	throw new SparqlRemoteEndpointException(e.toString());
        }
        return stream;
	}

}
