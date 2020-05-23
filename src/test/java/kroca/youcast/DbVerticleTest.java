package kroca.youcast;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import kroca.youcast.db.DbVerticle;
import kroca.youcast.db.MediaDbService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static kroca.youcast.util.EBPaths.DB_MEDIA;

@RunWith(VertxUnitRunner.class)
public class DbVerticleTest {

    private Vertx vertx;
    private MediaDbService mediaDbService;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();
        JsonObject dbConfig = new JsonObject()
                .put("url", "jdbc:hsqldb:mem:db/wiki")
                .put("driver_class", "org.hsqldb.jdbcDriver")
                .put("max_pool_size", 5);

        vertx.deployVerticle(new DbVerticle(), new DeploymentOptions().setConfig(dbConfig), context.asyncAssertSuccess());
        mediaDbService = MediaDbService.createProxy(vertx, DB_MEDIA);
    }

    @After
    public void finish(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }


    @Test
    public void checkCanCreateNewEntity(TestContext testContext) {
        Async async = testContext.async();
        mediaDbService.save("someTitle", "somePath", res -> {
            testContext.assertNotNull(res.result());
            async.complete();
        });
    }

    @Test
    public void getMediaList(TestContext testContext) {
        Async async = testContext.async();
        mediaDbService.save("someOtherTitle", "someOtherPath", testContext.asyncAssertSuccess());
        mediaDbService.getMediaList(res -> {
            testContext.assertTrue(res.result().size() > 0);
            async.complete();
        });
    }

    @Test
    public void canFindOne(TestContext testContext) {
        Async async = testContext.async();
        String title = "title";
        String path = "path";
        mediaDbService.save(title, path, res -> {
            long newElementId = res.result();
            mediaDbService.findOne(newElementId, findResult -> {
                JsonObject object = findResult.result();
                testContext.assertEquals(title, object.getString(title));
                testContext.assertEquals(path, object.getString(path));
                async.complete();
            });
        });

    }
}
