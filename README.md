# myeshop — Desktop Client

![License](https://img.shields.io/badge/License-MIT-green)
![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3-C71A36?logo=apachemaven&logoColor=white)

A console application that exercises the [myeshop-backend](https://github.com/SilviaBalmaseda/ecommerceSpringBootBackend) library. It demonstrates how a desktop client consumes the backend JAR as a plain Maven dependency — without any direct knowledge of the underlying entities, repositories, or database dialect.

## How it works

The frontend project declares the backend as a Maven dependency:

```xml
<dependency>
    <groupId>myeshop</groupId>
    <artifactId>backend</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

At runtime, `App.java` boots the backend Spring context, then runs a sequence of operations end-to-end: creates a customer with billing info, adds a product to the catalog, places an order with a line item, and reads back the result from the database.

## Prerequisites

- Java 21
- Maven 3
- `myeshop-backend` installed to your local Maven repository — follow the [backend setup guide](https://github.com/SilviaBalmaseda/ecommerceSpringBootBackend#getting-started) first

## Running

```bash
mvn compile exec:java -Dexec.mainClass="myeshop.front.App"
```

## Architecture note

`App.java` currently bootstraps the Spring context and injects the repositories directly, bypassing the Controller/DTO facade the backend exposes. A correctly layered client would depend only on `Controller` and `DTO` types and never import from `myeshop.backend.model` or `myeshop.backend.repository`. This is a known limitation, as this is just a test runner.

## Related projects

| Project | Description |
|---|---|
| [ecommerceSpringBootBackend](https://github.com/SilviaBalmaseda/ecommerceSpringBootBackend) | The backend library this client depends on |
| [ecommerceProject](https://github.com/kunohami/ecommerceProject) | First iteration — raw JPA & Hibernate without Spring |

## Authors

Silvia Balmaseda Hernández · Rafael Robles García  
DAM — Desarrollo de Aplicaciones Multiplataforma, 2026
