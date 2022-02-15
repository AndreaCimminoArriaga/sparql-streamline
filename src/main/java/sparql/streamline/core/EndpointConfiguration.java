package sparql.streamline.core;

import java.util.Objects;

public class EndpointConfiguration {


	private String sparqlQuery;

	private String sparqlUpdate;

	private String username;

	private String password;

	public EndpointConfiguration() {
		super();
	}

	public String getSparqlQuery() {
		return sparqlQuery;
	}


	public void setSparqlQuery(String sparqlQuery) {
		this.sparqlQuery = sparqlQuery;
	}


	public String getSparqlUpdate() {
		return sparqlUpdate;
	}


	public void setSparqlUpdate(String sparqlUpdate) {
		this.sparqlUpdate = sparqlUpdate;
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
		return Objects.hash(password, sparqlQuery, sparqlUpdate, username);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if ((obj == null) || (getClass() != obj.getClass()))
			return false;
		EndpointConfiguration other = (EndpointConfiguration) obj;
		return Objects.equals(password, other.password) && Objects.equals(sparqlQuery, other.sparqlQuery)
				&& Objects.equals(sparqlUpdate, other.sparqlUpdate) && Objects.equals(username, other.username);
	}




}
