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
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.sparql.resultset.ResultsFormat;

import sparql.streamline.exception.SparqlQuerySyntaxException;
import sparql.streamline.exception.SparqlRemoteEndpointException;

public class Sparql {
	
	public static ByteArrayOutputStream queryModel(String sparql, Model model, ResultsFormat format, String namespace) throws SparqlQuerySyntaxException, SparqlRemoteEndpointException {
		
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			Query query = QueryFactory.create(sparql) ;
			QueryExecution qexec =  QueryExecutionFactory.create(query, model);
			// init format if null
			if(format==null && (query.isSelectType() || query.isAskType()))
				format = ResultsFormat.FMT_RS_JSON;
			if(format==null && (query.isConstructType() || query.isDescribeType()))
				format = ResultsFormat.FMT_RDF_TURTLE;
			// proceed solving the query
			if(query.isSelectType()) {
				ResultSetFormatter.output(stream, qexec.execSelect(), format);
			}else if(query.isAskType()) {
				ResultSetFormatter.output(stream, qexec.execAsk(), ResultsFormat.convert(format));
	        }else if(query.isConstructType()) {
	        	RDFFormat formatOutput = toRDFFormat(format);
	        	RDFWriter.create().source(qexec.execConstruct()).format(formatOutput).base(namespace).output(stream);
	        }else if(query.isDescribeType()) {
	        	RDFFormat formatOutput = toRDFFormat(format);
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

	public static ByteArrayOutputStream queryService(String sparql, ResultsFormat format, String namespace) throws SparqlQuerySyntaxException, SparqlRemoteEndpointException {
		return queryModel(sparql,ModelFactory.createDefaultModel(), format, namespace);
	}
	
	protected static RDFFormat toRDFFormat(ResultsFormat format) {
		if(ResultsFormat.FMT_RDF_JSONLD.equals(format)) return RDFFormat.JSONLD;
		else if(ResultsFormat.FMT_RDF_TURTLE.equals(format)) return RDFFormat.TURTLE;
		else if(ResultsFormat.FMT_RDF_NT.equals(format)) return RDFFormat.NTRIPLES;
		else if(ResultsFormat.FMT_RDF_NQ.equals(format)) return RDFFormat.NQ;
		else return RDFFormat.NT;
	}

}
