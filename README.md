# Custom-ORM-Framework
A custom hibernate like ORM framework built in core Java using Reflections and Annotations.


Steps to get the project running

1. In order to connect to a database, you need to add the database driver jar (library) to your classpath (external libraries) using your IDE
2. Download the driver jar from here - https://repo.maven.apache.org/maven2/
3. If you are using IntelliJ IDE, then you can add the jar to your classpath by following the below step
4. Go to the File -> Project Structure -> Libraries -> Add new Jar -> (Add the downloaded JAR)


<img width="1025" alt="Screenshot 2021-12-31 at 12 24 04 PM" src="https://user-images.githubusercontent.com/22891754/147808438-6df34f48-bae6-4bc6-a7f3-7d5b82366365.png">


I have used MySQL as the underlying database, you can use any database of your choice, but make sure to add the driver jar in the classpath of the project 
and change the JDBC url in the connection.

For adding any new entity and playing with the basic CRUD operations.
1. When adding any new entity, make sure to give annotate one attribute with @Id annotation and rest other attributes as @Column annotation. 
2. @Id and @Column annotations can be given to a single attribute
3. Currently only few data types are supported in this ORM framework. Data types like Date, timestmap, blobs are not supported yet.
