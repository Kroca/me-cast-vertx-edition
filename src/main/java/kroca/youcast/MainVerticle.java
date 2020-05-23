package kroca.youcast;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import kroca.youcast.api.HttpApiVerticle;
import kroca.youcast.db.DbVerticle;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start() {
        vertx.deployVerticle(new HttpApiVerticle(), new DeploymentOptions().setConfig(config()));
        vertx.deployVerticle(new DbVerticle(), new DeploymentOptions().setConfig(config()));
    }

}
