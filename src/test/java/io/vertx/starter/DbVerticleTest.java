package io.vertx.starter;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.starter.db.MediaDbService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.vertx.starter.util.EBPaths.DB_MEDIA;

//fixme add actual result checks
@RunWith(VertxUnitRunner.class)
public class DbVerticleTest {

    private Vertx vertx;
    private MediaDbService mediaDbService;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();

        vertx.deployVerticle(new DbVerticle(), new DeploymentOptions().setConfig(new JsonObject().put("test", "what the fuck")),
                context.asyncAssertSuccess());
        mediaDbService = MediaDbService.createProxy(vertx, DB_MEDIA);
    }

    @After
    public void finish(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }


    @Test
    public void checkCanCreateNewEntity(TestContext testContext) throws InterruptedException {

        mediaDbService.save("someTitle", "somePath", testContext.asyncAssertSuccess());

    }

    @Test
    public void getMediaList(TestContext testContext) throws InterruptedException {
        mediaDbService.getMediaList(testContext.asyncAssertSuccess(System.out::println));
    }

    @Test
    public void canFindOne(TestContext testContext) {
        Async async = testContext.async();
        mediaDbService.findOne(0L, res -> {
            async.complete();
        });
    }
}
