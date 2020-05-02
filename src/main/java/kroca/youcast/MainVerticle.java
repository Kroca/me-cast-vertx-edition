package kroca.youcast;

import io.vertx.core.AbstractVerticle;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start() {
        vertx.deployVerticle(new HttpApiVerticle());
        vertx.deployVerticle(new DbVerticle());
    }

}
