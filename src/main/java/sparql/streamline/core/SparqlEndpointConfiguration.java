package sparql.streamline.core;

import java.util.Objects;


public class SparqlEndpointConfiguration {


	private String endpointQuery;

	private String endpointUpdate;

	private String username;

	private String password;

	public SparqlEndpointConfiguration() {
		super();
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
