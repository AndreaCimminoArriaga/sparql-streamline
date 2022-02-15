package sparql.streamline.core;

import java.io.ByteArrayOutputStream;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTPBuilder;
import org.apache.jena.sparql.exec.http.UpdateExecutionHTTP;
import org.apache.jena.sparql.exec.http.UpdateExecutionHTTPBuilder;
import org.apache.jena.sparql.resultset.ResultsFormat;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

import sparql.streamline.exception.SparqlQuerySyntaxException;
import sparql.streamline.exception.SparqlRemoteEndpointException;



public class SparqlEndpoint {
	
	private static EndpointConfiguration configuration;

	private SparqlEndpoint() {
		super();
	}

	public static EndpointConfiguration getConfiguration() {
		return configuration;
	}

	public static void setConfiguration(EndpointConfiguration configuration) {
		SparqlEndpoint.configuration = configuration;
	}

	public static ResultsFormat guess(String str) {
		return ResultsFormat.lookup(str);
	}

	// query methods

	public static ByteArrayOutputStream query(String sparql, ResultsFormat format) throws SparqlQuerySyntaxException, SparqlRemoteEndpointException {
		String sparqlQuery = configuration.getSparqlQuery();
		String username = configuration.getUsername();
		String password = configuration.getPassword();
		return  query(sparql, format, sparqlQuery, username, password, null);
	}
	
	public static ByteArrayOutputStream query(String sparql, ResultsFormat format, String namespace) throws SparqlQuerySyntaxException, SparqlRemoteEndpointException {
		String sparqlQuery = configuration.getSparqlQuery();
		String username = configuration.getUsername();
		String password = configuration.getPassword();
		return  query(sparql, format, sparqlQuery, username, password, namespace);
	}

	private static ByteArrayOutputStream query(String sparql, ResultsFormat format, String endpoint, String username, String password, String namespace) throws SparqlQuerySyntaxException, SparqlRemoteEndpointException {

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			Query query = QueryFactory.create(sparql) ;
			QueryExecution qexec =  QueryExecutionHTTPBuilder.service(endpoint).query(query).build();
			if(username!= null && password!=null) {
				HttpClient client = authHttpClient(username,password);
				qexec = QueryExecutionHTTPBuilder.service(endpoint).query(query).httpClient((java.net.http.HttpClient) client).build();
			}

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
			throw new SparqlQuerySyntaxException(e.toString());
        }catch(Exception e) {
        	throw new SparqlRemoteEndpointException(e.toString());
        }
        return stream;
	}

	protected static RDFFormat toRDFFormat(ResultsFormat format) {
		if(ResultsFormat.FMT_RDF_JSONLD.equals(format)) return RDFFormat.JSONLD;
		else if(ResultsFormat.FMT_RDF_TURTLE.equals(format)) return RDFFormat.TURTLE;
		else if(ResultsFormat.FMT_RDF_NT.equals(format)) return RDFFormat.NTRIPLES;
		else if(ResultsFormat.FMT_RDF_NQ.equals(format)) return RDFFormat.NQ;
		else return RDFFormat.NT;
	}

	static HttpClient authHttpClient(String username, String password) {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        Credentials credentials = new UsernamePasswordCredentials(username, password);
        credsProvider.setCredentials(AuthScope.ANY, credentials);
        return HttpClients.custom()
            .setDefaultCredentialsProvider(credsProvider)
            .build();

    }

	public static void update(String sparql ) throws SparqlRemoteEndpointException, SparqlQuerySyntaxException {
		String sparqlUpdate = configuration.getSparqlUpdate();
		String username = configuration.getUsername();
		String password = configuration.getPassword();

		update(sparql,  sparqlUpdate, username, password);
	}

	private static void update(String sparql, String endpoint, String username, String password) throws SparqlRemoteEndpointException, SparqlQuerySyntaxException  {
		try {
			UpdateRequest updateRequest = UpdateFactory.create(sparql);
			UpdateExecutionHTTP qexec =  UpdateExecutionHTTPBuilder.create().endpoint(endpoint).update(updateRequest).build();

			if(username!=null && password!=null) {
				HttpClient client = authHttpClient(username, password);
				UpdateExecutionHTTPBuilder.create().endpoint(endpoint).update(updateRequest).httpClient((java.net.http.HttpClient) client).build();
			}
			qexec.execute();
		 }catch(QueryException e){
	        throw new SparqlQuerySyntaxException(e.toString()); // syntax error
		}catch(org.apache.jena.atlas.web.HttpException e) {
			throw new SparqlRemoteEndpointException(e.getMessage());
		}
	}

}
