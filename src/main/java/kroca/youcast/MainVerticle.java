package kroca.youcast;

import io.vertx.core.AbstractVerticle;
import kroca.youcast.api.HttpApiVerticle;
import kroca.youcast.db.DbVerticle;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start() {
        vertx.deployVerticle(new HttpApiVerticle());
        vertx.deployVerticle(new DbVerticle());
    }

}
