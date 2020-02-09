package io.vertx.starter;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.vertx.starter.util.EBPaths.DB_MEDIA;

@RunWith(VertxUnitRunner.class)
public class DbVerticleTest {

    private Vertx vertx;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();
        Async async = context.async();
        vertx.deployVerticle(new DbVerticle(), new DeploymentOptions().setConfig(new JsonObject().put("test", "what the fuck")),
                res -> {
                    System.out.println("Deployed " + res.result());
                    async.complete();
                });
    }

    @After
    public void finish(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }


    @Test
    public void checkCanCreateNewEntity(TestContext testContext) throws InterruptedException {
        Async async = testContext.async();
        vertx.eventBus().request(DB_MEDIA, "create", res -> {
            System.out.println(res.result());
            async.complete();
        });
    }

    @Test
    public void getMediaList(TestContext testContext) throws InterruptedException {
        Async async = testContext.async();
        vertx.eventBus().request(DB_MEDIA, "getList", res -> {
            System.out.println(res.result());
            async.complete();
        });
    }

}
