package io.vertx.book.http;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;

public class HelloConsumerMicroservice extends AbstractVerticle {

  private WebClient webClient;

  @Override
  public void start() {
    webClient = WebClient.create(vertx);
    Router router = Router.router(vertx);
    router.get("/").handler(this::invokeMicroservice);
    vertx.createHttpServer().requestHandler(router::accept).listen(8080);
  }

  private void invokeMicroservice(RoutingContext routingContext) {
    HttpRequest<JsonObject> request = webClient
        .get(8080, "localhost", "/test")
        .as(BodyCodec.jsonObject());
    request.send(ar -> {
      if (ar.failed()) {
        routingContext.fail(ar.cause());
      } else {
        routingContext.response().end(ar.result().body().encode());
      }
    });
  }

}
