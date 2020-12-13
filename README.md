# Java DocGen

The goal of Java DocGen is to automatically document the inner works for your Java Microservices by using static code analyzes.
In order to do so, it combines information extracted for the Java source code files with information extracted from the compiled `.class` files.

In its initial version Java DocGen makes a number of assumptions:
 
 - The service is assumed to use gRPC as transport layer together with the [protoc-jar-maven-plugin](https://github.com/os72/protoc-jar-maven-plugin) code generator.
 - The service is further assumed to use [jOOQ](https://www.jooq.org/) to access the database.
 - Finally, the service is expected to be packaged as a Spring Boot fat JAR.
 
## Features

Java DocGen will create a report either in HTML or Markdown format.

For each service method, it will report

 - the required permission to access it (`@PreAuthorize` annotation)
 - any further permission check within the code body
 - other gRPC services called (fan out)
 - database tables accessed
 - a comment describing the service method

## Example Output

TODO

## Usage
Java DocGen expects a single argument pointing to a JSON config file.

TODO

## TODOs

 - Add support for "default" methods in interfaces
 - Add links to source code
 - List method callers (if less than N)
