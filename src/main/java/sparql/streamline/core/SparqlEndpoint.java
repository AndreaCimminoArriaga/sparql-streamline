package sparql.streamline.core;

import java.io.ByteArrayOutputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.http.HttpClient;

import org.apache.jena.atlas.web.HttpException;
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


/**
 * This class allows interacting with a remote endpoint with SPARQL 1.1 queries (SELECT/ASK/CONSTRUCT/DESCRIBE/UPDATE). 
 * @author Andrea Cimmino
 * @author Juan Cano Benito
 */
public class SparqlEndpoint {
	
	private SparqlEndpointConfiguration configuration;
	private static final String CONFIGURATION_ERROR_MESSAGE = "Current SparqlEndpointConfiguration configuration is null, provide a valid one";
	
	/**
	 * Empty constructor, provide a {@link SparqlEndpointConfiguration} using the setter.
	 */
	public SparqlEndpoint() {
		super();
	}

	/**
	 * Constructor
	 * @param configuration a valid {@link SparqlEndpointConfiguration}
	 */
	public SparqlEndpoint(SparqlEndpointConfiguration configuration) {
		super();
		this.configuration = configuration;
	}
	
	// 
	
	/**
	 * Gets the provided configuration
	 * @return a {@link SparqlEndpointConfiguration}
	 */
	public SparqlEndpointConfiguration getConfiguration() {
		return configuration;
	}

	/**
	 * Sets a new configuration
	 * @param configuration a {@link SparqlEndpointConfiguration}
	 */
	public void setConfiguration(SparqlEndpointConfiguration configuration) {
		this.configuration = configuration;
	}
	
	// 
	/**
	 * Tries to guess a {@link ResultsFormat} from a String
	 * @param str a string representation of a format, e.g., "CSV"
	 * @return a {@link ResultsFormat}
	 */
	public static ResultsFormat guess(String str) {
		return ResultsFormat.lookup(str);
	}

	// query methods
	/**
	 * Solves a provided query in the remote endpoint.
	 * @param sparql a SPARQL query SELECT, ASK, CONSTRUC, or DESCRIBE
	 * @param format the {@link ResultsFormat} for displaying the results. This argument must be compatible with the type of query provided.
	 * @return a {@link ByteArrayOutputStream} containing the query results
	 * @throws SparqlQuerySyntaxException is thrown when the query has syntax errors
	 * @throws SparqlRemoteEndpointException is thrown when an error occurs in the remote endpoint
	 * @throws SparqlConfigurationException is thrown when an error related to the configuration occurs
	 */
	public ByteArrayOutputStream query(String sparql, ResultsFormat format) throws SparqlQuerySyntaxException, SparqlRemoteEndpointException, SparqlConfigurationException {
		if(configuration==null)
			throw new SparqlConfigurationException(CONFIGURATION_ERROR_MESSAGE);
		String sparqlQuery = configuration.getEndpointQuery();
		String username = configuration.getUsername();
		String password = configuration.getPassword();
		return  query(sparql, format, sparqlQuery, username, password, null);
	}
	
	/**
	 * Solves a provided query in the remote endpoint.
	 * @param sparql a SPARQL query SELECT, ASK, CONSTRUC, or DESCRIBE
	 * @param format the {@link ResultsFormat} for displaying the results. This argument must be compatible with the type of query provided.
	 * @param namespace a base namespace to be used during the results formatting
	 * @return a {@link ByteArrayOutputStream} containing the query results
	 * @throws SparqlQuerySyntaxException is thrown when the query has syntax errors
	 * @throws SparqlRemoteEndpointException is thrown when an error occurs in the remote endpoint
	 * @throws SparqlConfigurationException is thrown when an error related to the configuration occurs
	 */
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
			String m = e.getMessage();
			if(m!=null && m.equals("Unauthorized"))
				throw new SparqlRemoteEndpointException("Unauthorized, provided credentials are wrong");
			throw new SparqlQuerySyntaxException(e.toString());
        }catch(HttpException e) {
        	throw new SparqlRemoteEndpointException(e.getResponse());
        }
		catch(Exception e) {
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


	/**
	 * Solves a provided query in the remote endpoint.
	 * @param sparql a SPARQL query UPDATE
	 * @throws SparqlQuerySyntaxException is thrown when the query has syntax errors
	 * @throws SparqlRemoteEndpointException is thrown when an error occurs in the remote endpoint
	 * @throws SparqlConfigurationException is thrown when an error related to the configuration occurs
	 */
	public  void update(String sparql) throws SparqlQuerySyntaxException, SparqlRemoteEndpointException, SparqlConfigurationException {
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
				qexec = UpdateExecutionHTTPBuilder.create().endpoint(endpoint).update(updateRequest).httpClient(client).build();
			}
			qexec.execute();
		 }catch(QueryException e){
	        throw new SparqlQuerySyntaxException(e.toString()); // syntax error
		}catch(org.apache.jena.atlas.web.HttpException e) {
			throw new SparqlRemoteEndpointException(e.getMessage());
		}
	}

	
	

}
