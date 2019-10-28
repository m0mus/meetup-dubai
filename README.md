# Dubai Meetup on Oct 30th, 2019

## Prerequisites

- Maven 3.5+
- Java 8+
- Docker
- Any kind of Java IDE

## Helidon MP

### Create project

1. Create a working folder (ex. `helidon-dubai`) and make it active.

2. Generate project using Maven archetype:

```bash
mvn archetype:generate -DinteractiveMode=false \
    -DarchetypeGroupId=io.helidon.archetypes \
    -DarchetypeArtifactId=helidon-quickstart-mp \
    -DarchetypeVersion=1.3.1 \
    -DgroupId=io.helidon.examples \
    -DartifactId=helidon-quickstart-mp \
    -Dpackage=io.helidon.examples.quickstart.mp
```

> Windows users must make it in one row by deleting backslashes and end of lines

3. Open project in your favorite IDE.

```bash
cd helidon-quickstart-mp
idea pom.xml
```

4. Build the project

```bash
mvn clean package
```

5. Run the project

```bash
java -jar target/helidon-quickstart-mp.jar
```

6. Test the project

In another terminal tab:

```bash
curl -X GET http://localhost:8080/greet
curl -X GET http://localhost:8080/greet/Dmitry
curl -X PUT -H "Content-Type: application/json" -d '{"greeting" : “Ahoj"}' http://localhost:8080/greet/greeting
curl -X GET http://localhost:8080/greet/Dmitry
```

7. Build a Docker image

```bash
docker build -t helidon-quickstart-mp .
```

8. Run project in Docker

```bash
docker run --rm -p 8080:8080 helidon-quickstart-mp:latest
```

### HealthCheck

#### HealthCheck endpoints

1. HealthCheck 1.0 endpoint

```bash
curl -X GET http://localhost:8080/health | jq .
```

2. HealthCheck 2.0 liveness and readiness checks

```bash
curl -X GET http://localhost:8080/health/live | jq .
curl -X GET http://localhost:8080/health/ready | jq .
```

#### Adding a custom readiness health check which is always 'DOWN'

1. Create a new java class:

```java
package io.helidon.examples.quickstart.mp;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import javax.enterprise.context.ApplicationScoped;

@Readiness
@ApplicationScoped
public class StateHealthCheck implements HealthCheck {

    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.named("state")
                .down()
                .build();
    }
}
```

2. Recompile and rerun the app

```bash
mvn clean package -DskipTests
java -jar target/helidon-quickstart-mp.jar
```

3. Test

```bash
curl -X GET http://localhost:8080/health/ready | jq .
```

#### Read state from a configuration property

1. Add `app.state` property to `META-INF\microprofile-config.properties` file.

```
app.state=Down
```

2. Modify `StateHealthCheck` to read state from configuration

```java
@Readiness
@ApplicationScoped
public class StateHealthCheck implements HealthCheck {

    @Inject
    @ConfigProperty(name = "app.state")
    private String state;

    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.named(“state")
                .state("up".equalsIgnoreCase(state))
                .build();
    }
}
```

3. Recompile and rerun the app

```bash
mvn clean package -DskipTests
java -jar target/helidon-quickstart-mp.jar
```

4. Test

```bash
curl -X GET http://localhost:8080/health/ready | jq .
```

5. Pass state using a system property.

```bash
java -Dapp.state=Up -jar target/helidon-quickstart-mp.jar
```

#### Make application get notified when configuration property is changed

1. Remove `app.state` property from `META-INF/microprofile-config.properties` file.

2. Create `mp.yaml` configuration file in `conf` directory of your working folder.

3. Update `mp.yaml`

```yaml
app:
  status: "down"
```

4. Crete custom configuration in `Main.java`

```java
static Server startServer() {
    Config config = Config.builder()
            .sources(
                    file("../conf/mp.yaml")
                        .pollingStrategy(PollingStrategies::watch)
                        .optional(),
                    classpath("META-INF/microprofile-config.properties"))
            .build();

    return Server.builder().config(config).build().start();
}
```

5. Update `StateHealthCheck` to support dynamic properties

```java
@Readiness
@ApplicationScoped
public class StateHealthCheck implements HealthCheck {

    @Inject
    @ConfigProperty(name = "app.state")
    private Supplier<String> state;

    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.named("state")
                .state("up".equalsIgnoreCase(state.get()))
                .build();
    }
}
```

6. Recompile and rerun the app

```bash
mvn clean package -DskipTests
java -jar target/helidon-quickstart-mp.jar
```

6. Test (must be down)

```bash
curl -X GET http://localhost:8080/health/ready | jq .
```

7. Without stopping the app update `mp.yaml` file. Change `app.state` property to `up`.

```yaml
app:
  status: "up"
```

8. Test again (must be UP)

```bash
curl -X GET http://localhost:8080/health/ready | jq .
```

> It may take some time to propagate the change. If status is not changed to UP from the first try, re-run the command.

### Metrics

#### Metrics endpoint

1. Metrics in Prometeus format

```bash
curl -X GET http://localhost:8080/metrics
```

2. Metrics in JSON

```bash
curl -H 'Accept: application/json' -X GET http://localhost:8080/metrics | jq .
```

3. Base metrics only

```bash
curl -H 'Accept: application/json' -X GET http://localhost:8080/metrics/base | jq .
```

4. Metrics metadata

```bash
curl -H 'Accept: application/json' -X OPTIONS http://localhost:8080/metrics | jq .
```


#### Adding custom Metrics

1. Add `@Counted` metric to `getDefaultMessage` method

```java
@GET
@Counted(name = "defaultCount",
    absolute = true,
    displayName = "Default Message Counter",
    description = "Number of times default message called")
@Produces(MediaType.APPLICATION_JSON)
public JsonObject getDefaultMessage() {
    return createResponse("World");
}
```

2. Add `@Timed` metric to `getMessage` method

```java
@Path("/{name}")
@GET
@Timed
@Produces(MediaType.APPLICATION_JSON)
public JsonObject getMessage(@PathParam("name") String name) {
    return createResponse(name);
}
```

3. Recompile and rerun the app

``` bash
mvn clean package -DskipTests
java -jar target/helidon-quickstart-mp.jar
```

4. Run commands below multiple times to fill metrics data

```bash
curl -X GET http://localhost:8080/greet
curl -X GET http://localhost:8080/greet/Dmitry
```

5. Test it

```bash
curl -H 'Accept: application/json' -X GET http://localhost:8080/metrics/application | jq .
curl -H 'Accept: application/json' -X OPTIONS http://localhost:8080/metrics/application | jq .
```

### JPA

1. Run H2 database in docker

```bash
docker run -d -p 9092:9082 -p 8082:8082 --name=h2 nemerosa/h2
```

2. Add dependencies

```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>jakarta.persistence</groupId>
    <artifactId>jakarta.persistence-api</artifactId>
    <version>2.2.2</version>
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>javax.transaction</groupId>
    <artifactId>javax.transaction-api</artifactId>
    <version>1.2</version>
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>io.helidon.integrations.cdi</groupId>
    <artifactId>helidon-integrations-cdi-hibernate</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.helidon.integrations.cdi</groupId>
    <artifactId>helidon-integrations-cdi-jpa</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.helidon.integrations.cdi</groupId>
    <artifactId>helidon-integrations-cdi-jta-weld</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.helidon.integrations.cdi</groupId>
    <artifactId>helidon-integrations-cdi-datasource-hikaricp</artifactId>
    <scope>runtime</scope>
</dependency>
```

3. Add `META-INF/persistence.xml`

```xml
<persistence version="2.2"
             xmlns="http://xmlns.jcp.org/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/persistence
                                 http://xmlns.jcp.org/xml/ns/persistence/persistence_2_2.xsd">
  <persistence-unit name="test" transaction-type="JTA">
    <jta-data-source>test</jta-data-source>
    <class>io.helidon.examples.quickstart.mp.Greeting</class>
    <properties>
      <property name="hibernate.dialect" value="org.hibernate.dialect.H2Dialect"/>
      <property name="hibernate.hbm2ddl.auto" value="create-drop" />
      <property name="show_sql" value="true"/>
      <property name="hibernate.temp.use_jdbc_metadata_defaults" value="false"/>
    </properties>
  </persistence-unit>
</persistence>
```

4. Configure data source in the application configuration. Add the following to `META-INF/microprofiloe-config.xml`

```
javax.sql.DataSource.test.dataSourceClassName=org.h2.jdbcx.JdbcDataSource
javax.sql.DataSource.test.dataSource.url=jdbc:h2:mem:test;INIT=CREATE TABLE IF NOT EXISTS GREETING (FIRSTPART VARCHAR NOT NULL, SECONDPART VARCHAR NOT NULL, PRIMARY KEY (FIRSTPART))\\;MERGE INTO GREETING (FIRSTPART, SECONDPART) VALUES ('hello', 'world')
javax.sql.DataSource.test.dataSource.user=sa
javax.sql.DataSource.test.dataSource.password=
```

5. ​​Add an entity `Greeting.java`

```java
package io.helidon.examples.quickstart.mp;

import javax.persistence.*;
import java.util.Objects;

@Access(AccessType.FIELD)
@Entity(name = "Greeting")
@Table(name = "GREETING")
public class Greeting {

    @Id
    @Column(name = "FIRSTPART", insertable = true, nullable = false, updatable = false)
    private String firstPart;

    @Basic(optional = false)
    @Column(name = "SECONDPART", insertable = true, nullable = false, updatable = true)
    private String secondPart;

    @Deprecated
    protected Greeting() {
        super();
    }

    public Greeting(final String firstPart, final String secondPart) {
        super();
        this.firstPart = Objects.requireNonNull(firstPart);
        this.secondPart = Objects.requireNonNull(secondPart);
    }

    public void setSecondPart(final String secondPart) {
        this.secondPart = Objects.requireNonNull(secondPart);
    }

    @Override
    public String toString() {
        return this.secondPart;
    }


    public String secondPart() {
        return secondPart;
    }
}
```

6. Modify `GreetResource.createResponse` method

```java
private JsonObject createResponse(String who) {
    Greeting greeting = this.entityManager.find(Greeting.class, who);
    String message;
    if (null == greeting) {
        // not in database
        message = greetingProvider.getMessage();
    } else {
        message = greeting.secondPart();
    }
    String msg = String.format("%s %s!", message, who);


    return JSON.createObjectBuilder()
            .add("message", msg)
            .build();
}
```

7. Add GreetResource.dbCreateMapping method

```java
@Path("/db/{firstPart}")
@POST
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.TEXT_PLAIN)
@Transactional(Transactional.TxType.REQUIRED)
public Response dbCreateMapping(@PathParam("firstPart") String firstPart, String secondPart) {
    Greeting greeting = new Greeting(firstPart, secondPart);
    this.entityManager.persist(greeting);

    return Response.created(URI.create("/greet/" + firstPart)).build();
}
```

8. Test it

```bash
curl -i -X POST -H 'Content-Type:text/plain' -d 'Use' http://localhost:8080/greet/db/helidon

curl -i http://localhost:8080/greet/helidon
```

9. Add GreetResource.dbUpdateMapping method

```java
@Path("/db/{firstPart}")
@PUT
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.TEXT_PLAIN)
@Transactional(Transactional.TxType.REQUIRED)
public Response dbUpdateMapping(@PathParam("firstPart") String firstPart, String secondPart) {
    try {
        Greeting greeting = this.entityManager.getReference(Greeting.class, firstPart);
        greeting.setSecondPart(secondPart);
    } catch (EntityNotFoundException e) {
        return Response.status(404).entity("Mapping for " + firstPart + " not found").build();
    }

    return Response.ok(firstPart).build();
}
```

10. Test it

```bash
curl -i -X PUT -H 'Content-Type:text/plain' -d 'I am using' http://localhost:8080/greet/db/helidon

curl -i http://localhost:8080/greet/helidon
```

## Helidon SE

### Create project

1. Create Helidon MP Quickstart project:

```bash
mvn archetype:generate -DinteractiveMode=false \
    -DarchetypeGroupId=io.helidon.archetypes \
    -DarchetypeArtifactId=helidon-quickstart-se \
    -DarchetypeVersion=1.3.1 \
    -DgroupId=io.helidon.examples \
    -DartifactId=helidon-quickstart-se \
    -Dpackage=io.helidon.examples.quickstart.se
```

2. Open it in IDE of your choice

```bash
idea pom.xml
```

3. Build the project

```bash
mvn package
```

4. Run the app

```bash
java -jar target/helidon-quickstart-se.jar
```

5. In a new terminal window demonstrate that all endpoints work

```bash
curl -X GET http://localhost:8080/greet
curl -X GET http://localhost:8080/greet/Dmitry
curl -X PUT -H "Content-Type: application/json" -d '{"greeting" : “Ahoj"}' http://localhost:8080/greet/greeting
curl -X GET http://localhost:8080/greet/Dmitry
```

6. Health check works too

```bash
curl -X GET http://localhost:8080/health
```

7. and metrics

```bash
curl -H 'Accept: application/json' -X GET http://localhost:8080/metrics | json_pp
```

### Make GraalVM native image

1. Download [GraalVM 19.2.1](https://github.com/oracle/graal/releases/tag/vm-19.2.1) and extract it to `~/graalvm` directory.

2. Install `native-image` feature

```bash
sudo yum install gcc, zlib-devel
~/graalvm/graalvm-ce-19.2.1/bin/gu install native-image
```

3. Create `GRAALVM_HOME` environment variable

```bash
export GRAALVM_HOME=~/graalvm/graalvm-ce-19.2.1
```

4. Build a native image

```bash
mvn package -DskipTests -Pnative-image
```

5. Run it

```bash
./target/helidon-quickstart-se
```

6. Test endpoints

```bash
curl -X GET http://localhost:8080/greet
curl -X GET http://localhost:8080/greet/Dmitry
curl -X PUT -H "Content-Type: application/json" -d '{"greeting" : “Ahoj"}' http://localhost:8080/greet/greeting
curl -X GET http://localhost:8080/greet/Dmitry
```

### Compare startup times on JVM and GraalVM

1. Modify source code the way that app stops after web server initialization

```java
server.start()
    .thenAccept(ws -> {
        System.exit(0);
        System.out.println(
                "WEB server is up! http://localhost:" + ws.port() + "/greet");
        ws.whenShutdown().thenRun(()
            -> System.out.println("WEB server is DOWN. Good bye!"));
        })
    .exceptionally(t -> {
        System.err.println("Startup failed: " + t.getMessage());
        t.printStackTrace(System.err);
        return null;
    });
```    

2. Compile and run on JVM

```bash
mvn package -DskipTests
time java -jar target/helidon-quickstart-se.jar
```

3. Compile and run on GraalVM

```bash
mvn package -DskipTests -Pnative-image
time ./target/helidon-quickstart-se
```
