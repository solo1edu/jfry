# JFry [![Build Status](https://travis-ci.org/ggalmazor/jfry.svg?branch=master)](https://travis-ci.org/ggalmazor/jfry)
Java Fry is a web library inspired by Sinatra

## Getting started

This will improve with time. You just create a new instance with a server adapter and a port number. 

Then you can use JFry's DSL to register new Routes with their Handlers:
 
```java
JFry.of(new JettyAdapter(), 8080)
  .options("/foo", req -> req.buildResponse().ok("bar"))
  .get("/foo", req -> req.buildResponse().ok("bar"))
  .head("/foo", req -> req.buildResponse().ok("bar"))
  .post("/foo", req -> req.buildResponse().ok("bar"))
  .put("/foo", req -> req.buildResponse().ok("bar"))
  .delete("/foo", req -> req.buildResponse().ok("bar"))
  .trace("/foo", req -> req.buildResponse().ok("bar"))
  .connect("/foo", req -> req.buildResponse().ok("bar"))
  .start();
```

Now you can browse to [http://localhost:8080/foo](http://localhost:8080/foo)

## Why is JFry different?

JFry is strongly based on concepts related to Functional Programming: immutable objects, first order functions, etc.

Probably the main difference between JFry and almost any other Java web library is that Route Handlers are defined as ```Request -> Response``` functions. This is a key design decision in order to benefit from functions' composition and decoration capabilities.
 
In JFry there is no need for Route filters. Thanks to the functional nature of Handlers, any task like authentication, JSON serialization & deserialization, templating, etc. can be defined as a function decorator that can be easily applied and combined to your Handlers.

You can check JacksonDecoratorTest.java for an example of this idea.

JFry doesn't make any assumption on the kind of use that you're going to give it. Instead, it gives you an easy way to implement your own decorators to handle template rendering, input/output deserialization/serialization, authentication, session handling, etc.

JFry now ships with a JSON serialization/deserialization decorator implementation based on Jackson, but the list will grow soon with at least these decorators that we're currently using on our projects:

 - Java Web Token based authentication and stateless session handling
 - Role based ACLs
 - Database transactions handling with Jooq

## Use the tests

You can learn a lot about JFry browsing through its tests