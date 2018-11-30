package io.vertx.book.http;

import
    io.vertx.core.json.JsonObject
    ;
import
    io.vertx.rxjava.core.AbstractVerticle
    ;
import
    io.vertx.rxjava.ext.web.*
    ;
import
    io.vertx.rxjava.ext.web.client.*
    ;
import
    io.vertx.rxjava.ext.web.codec.BodyCodec
    ;
import
    rx.Single
    ;

public class HelloConsumerMicroservice extends AbstractVerticle {

  private WebClient webClient;

  @Override
  public void start() {
    webClient = WebClient.create(vertx);
    Router router = Router.router(vertx);
    router.get("/").handler(this::invokeMicroservice);
    vertx.createHttpServer().requestHandler(router::accept).listen(8081);
  }

  private void invokeMicroservice(RoutingContext routingContext) {
    HttpRequest<JsonObject> request1 = webClient.get(8080, "localhost", "/luke")
        .as(BodyCodec.jsonObject());
    HttpRequest<JsonObject> request2 = webClient.get(8080, "localhost", "/leia")
        .as(BodyCodec.jsonObject());
    Single<JsonObject> s1 = request1.rxSend().map(HttpResponse::body);
    Single<JsonObject> s2 = request2.rxSend().map(HttpResponse::body);
    Single.zip(s1, s2, (luke, leia) -> {
      //We have the results of both requests in Luke and Leia
      return new JsonObject()
          .put("Luke", luke.getString("message"))
          .put("Leia", leia.getString("message"));
    }).subscribe(result -> routingContext.response().end(result.encodePrettily()),
        error -> {
          error.printStackTrace();
          routingContext.response().setStatusCode(500).end(error.getMessage());
        });
  }

}
