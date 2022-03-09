package sparql.streamline.core;

import java.io.ByteArrayOutputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.http.HttpClient;
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

import sparql.streamline.exception.SparqlConfigurationException;
import sparql.streamline.exception.SparqlQuerySyntaxException;
import sparql.streamline.exception.SparqlRemoteEndpointException;



public class SparqlEndpoint {
	
	private SparqlEndpointConfiguration configuration;
	private static final String CONFIGURATION_ERROR_MESSAGE = "Current SparqlEndpointConfiguration configuration is null, provide a valid one";
	public SparqlEndpoint() {
		super();
	}

	public SparqlEndpoint(SparqlEndpointConfiguration configuration) {
		super();
		this.configuration = configuration;
	}
	
	// 
	
	public SparqlEndpointConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(SparqlEndpointConfiguration configuration) {
		this.configuration = configuration;
	}
	
	// 
	
	public static ResultsFormat guess(String str) {
		return ResultsFormat.lookup(str);
	}

	// query methods

	public ByteArrayOutputStream query(String sparql, ResultsFormat format) throws SparqlQuerySyntaxException, SparqlRemoteEndpointException, SparqlConfigurationException {
		if(configuration==null)
			throw new SparqlConfigurationException(CONFIGURATION_ERROR_MESSAGE);
		String sparqlQuery = configuration.getEndpointQuery();
		String username = configuration.getUsername();
		String password = configuration.getPassword();
		return  query(sparql, format, sparqlQuery, username, password, null);
	}
	
	public ByteArrayOutputStream query(String sparql, ResultsFormat format, String namespace) throws SparqlQuerySyntaxException, SparqlRemoteEndpointException, SparqlConfigurationException {
		if(configuration==null)
			throw new SparqlConfigurationException(CONFIGURATION_ERROR_MESSAGE);
		
		String sparqlQuery = configuration.getEndpointQuery();
		String username = configuration.getUsername();
		String password = configuration.getPassword();
		return  query(sparql, format, sparqlQuery, username, password, namespace);
	}

	private ByteArrayOutputStream query(String sparql, ResultsFormat format, String endpoint, String username, String password, String namespace) throws SparqlQuerySyntaxException, SparqlRemoteEndpointException {

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			Query query = QueryFactory.create(sparql) ;
			QueryExecution qexec =  QueryExecutionHTTPBuilder.service(endpoint).query(query).build();
			if(username!= null && password!=null) {
				HttpClient client = authHttpClient(username,password);					
				qexec = QueryExecutionHTTPBuilder.service(endpoint).query(query).httpClient(client).build();
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

	protected RDFFormat toRDFFormat(ResultsFormat format) {
		if(ResultsFormat.FMT_RDF_JSONLD.equals(format)) return RDFFormat.JSONLD;
		else if(ResultsFormat.FMT_RDF_TURTLE.equals(format)) return RDFFormat.TURTLE;
		else if(ResultsFormat.FMT_RDF_NT.equals(format)) return RDFFormat.NTRIPLES;
		else if(ResultsFormat.FMT_RDF_NQ.equals(format)) return RDFFormat.NQ;
		else return RDFFormat.NT;
	}
	
	protected HttpClient authHttpClient(String username, String password) {		
		
        return HttpClient.newBuilder().authenticator(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password.toCharArray());
            }
        }).version(HttpClient.Version.HTTP_2).build();

    }

	public  void update(String sparql) throws SparqlRemoteEndpointException, SparqlQuerySyntaxException, SparqlConfigurationException {
		if(configuration==null)
			throw new SparqlConfigurationException(CONFIGURATION_ERROR_MESSAGE);
		
		String sparqlUpdate = configuration.getEndpointUpdate();
		String username = configuration.getUsername();
		String password = configuration.getPassword();

		update(sparql,  sparqlUpdate, username, password);
	}

	private  void update(String sparql, String endpoint, String username, String password) throws SparqlRemoteEndpointException, SparqlQuerySyntaxException  {
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
