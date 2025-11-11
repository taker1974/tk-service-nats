# tk-service-nats

## About the module

NATS service for Spring Boot applications. Uses Spring Boot autoconfiguration.

## Using the module

Set up and start your NATS server instance, of course.  

Add the following configuration to your application.yml:  

```yml
nats:
  enabled: ${NATS_ENABLED:true}
  servers: nats://${NATS_HOST:localhost}:${NATS_PORT:4222}
  connection:
    timeout: ${NATS_CONNECTION_TIMEOUT:5000}
    reconnect: ${NATS_CONNECTION_RECONNECT:true}
    max-reconnects: ${NATS_CONNECTION_MAX_RECONNECTS:-1}
```

Set your preferred values according to your needs.  

NOTE: If you have multiple servers, separate the server addresses with commas:  

```yml
nats:
  servers: nats://${NATS_HOST:localhost}:${NATS_PORT:4222},nats://${NATS_HOST:other-host}:${NATS_PORT:6222}
```

Add dependencies to your _pom.xml_:

```xml
<properties>
    <tk-service-nats.version>2.0.2</tk-service-nats.version>
</properties>

<!-- Add module itself -->
<dependency>
    <groupId>ru.spb.tksoft</groupId>
    <artifactId>tk-service-nats</artifactId>
    <version>${tk-service-nats.version}</version>
</dependency>
```

Add imports to your code:

```Java
import ru.spb.tksoft.service.nats.service.NatsService;
```

Inject the dependency and use it:

```Java
@Service
@RequiredArgsConstructor
public class MyService {
    
    private final NatsService natsService;

    public void onMyEvent() {
        
        natsService.publish(getMySubject(), getMyMessage());
    }

    // You can also subscribe to a subject:
    // just call natsService.subscribe(String subject, MessageHandler handler) - see documentation for details.
}

```

## Prerequisites

Java >= 17.

**Java**:

- install JDK or JRE version 17 or higher (development is done using Java 21; there are no obvious restrictions on the use of other versions of Java);
- make sure the installation is correct and that java, javac, and maven (mvn) are available;

## Build the module

1. Install Java 21 + Maven.
2. Build the module:

```bash
cd tk-service-nats && mvn clean package
```

Use `mvn clean install` if you need to install it locally.

If you want documentation:  

```bash
mvn compile javadoc:javadoc
```

## Licensing

This module is licensed under Apache 2.0. See LICENSE for details.

## Author

Konstantin Terskikh  
Email: <kostus.online.1974@yandex.ru>, <kostus.online@gmail.com>  
Saint-Petersburg 2025
