package kroca.youcast;


import io.vertx.core.DeploymentOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import kroca.youcast.api.HttpApiVerticle;
import kroca.youcast.db.DbVerticle;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start() {
        vertx.rxDeployVerticle(new DbVerticle(), new DeploymentOptions().setConfig(config()))
                .flatMap(id -> vertx.rxDeployVerticle(new HttpApiVerticle(), new DeploymentOptions().setConfig(config())))
                .subscribe();
    }

}
