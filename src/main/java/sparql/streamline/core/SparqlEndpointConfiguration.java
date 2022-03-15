package sparql.streamline.core;

import java.util.Objects;

/**
 * This class contain the configuration for connecting to a remote SPARQL endpoint
 * @author Andrea Cimmino
 * @author Juan Cano
 *
 */
public class SparqlEndpointConfiguration {


	private String endpointQuery;

	private String endpointUpdate;

	private String username;

	private String password;

	/**
	 * Empty constructor, use setters for establishing the configuration
	 */
	public SparqlEndpointConfiguration() {
		super();
	}
	
	/**
	 * Configures a SPARQL endpoint only for reading data
	 * @param endpointQuery the address of the remote SPARQL endpoint that allows reading data
	 */
	public SparqlEndpointConfiguration(String endpointQuery) {
		super();
		this.endpointQuery = endpointQuery;
	}
	
	/**
	 * Configures a SPARQL endpoint for reading and writing data
	 * @param endpointQuery the address of the remote SPARQL endpoint that allows reading data
	 * @param endpointUpdate the address of the remote SPARQL endpoint that allows writing data
	 */
	public SparqlEndpointConfiguration(String endpointQuery, String endpointUpdate) {
		super();
		this.endpointQuery = endpointQuery;
		this.endpointUpdate = endpointUpdate;
	}
	
	/**
	 * Configures a SPARQL endpoint with authentication for reading and writing data
	 * @param endpointQuery the address of the remote SPARQL endpoint that allows reading data
	 * @param endpointUpdate the address of the remote SPARQL endpoint that allows writing data
	 * @param username a valid username
	 * @param password a valid password
	 */
	public SparqlEndpointConfiguration(String endpointQuery, String endpointUpdate, String username, String password) {
		super();
		this.endpointQuery = endpointQuery;
		this.endpointUpdate = endpointUpdate;
		this.username = username;
		this.password = password;
	}


	public String getEndpointQuery() {
		return endpointQuery;
	}

	public void setEndpointQuery(String endpointQuery) {
		this.endpointQuery = endpointQuery;
	}

	public String getEndpointUpdate() {
		return endpointUpdate;
	}

	public void setEndpointUpdate(String endpointUpdate) {
		this.endpointUpdate = endpointUpdate;
	}



	public String getUsername() {
		return username;
	}


	public void setUsername(String username) {
		this.username = username;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public int hashCode() {
		return Objects.hash(endpointQuery, endpointUpdate, username, password);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if ((obj == null) || (getClass() != obj.getClass()))
			return false;
		SparqlEndpointConfiguration other = (SparqlEndpointConfiguration) obj;
		return Objects.equals(password, other.password) && Objects.equals(endpointQuery, other.getEndpointQuery())
				&& Objects.equals(endpointUpdate, other.getEndpointUpdate()) && Objects.equals(username, other.username);
	}


}
