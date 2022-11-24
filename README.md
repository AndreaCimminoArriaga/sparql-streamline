
# SPARQL streamline

A [jena-wrapper](https://jena.apache.org/) for using SPARQL out of the box in your code.

### Maven install
Import in your *pom.xml* the following dependency

````xml
<dependency>
    <groupId>io.github.andreacimminoarriaga</groupId>
    <artifactId>sparql-streamline</artifactId>
    <version>1.1.6</version>
</dependency>
````

### Quick start

For interacting with a SPARQL endpoint first it has to be configured. Next excerpt shows how to set up a *Graph DB* with a repo named `test` and with security enabled (username is `root`and password is `toor`):

```java
SparqlEndpointConfiguration configuration = new SparqlEndpointConfiguration();
configuration.setEndpointQuery("http://localhost:7200/repositories/test");
// Set update endpoint when you need to write data in the triple store
configuration.setEndpointUpdate("http://localhost:7200/repositories/test/statements");
// Set credentials only when the triple store has security enabled, leave as null oterwhise.
configuration.setUsername("root");
configuration.setPassword("toor"); 
```

Once the SPARQl endpoint is configured, it can be accessed for either query or write.

```java
SparqlEndpoint endpoint = **new** SparqlEndpoint(sec);

// Querying the endpoint
String query = "SELECT * { ?s a ?t }"; // SELECT, ASK, CONSTRUCT, or DESCRIBE queries
// The ResultsFormat class is an enum containing all the possible result serialisations
ByteArrayOutputStream result = endpoint.query(query, ResultsFormat.FMT_RS_CSV) ;
// method query() can also receive a base namespace as third argument

// Updating the endpoint
String update = "CLEAR ALL" // UPDATE queries
endpoint.update(update);
```