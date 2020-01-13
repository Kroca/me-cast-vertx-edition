package io.vertx.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.starter.util.Runner;

public class MainVerticle extends AbstractVerticle {

    public static void main(String[] args) {
        Runner.runExample(UploadHandlerVerticle.class);
    }

    @Override
    public void start() {
        vertx.deployVerticle(new UploadHandlerVerticle());
    }

}
