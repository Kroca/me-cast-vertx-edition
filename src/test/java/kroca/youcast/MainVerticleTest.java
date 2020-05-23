package kroca.youcast;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.multipart.MultipartForm;
import kroca.youcast.api.HttpApiVerticle;
import kroca.youcast.db.DbVerticle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static io.netty.handler.codec.http.HttpResponseStatus.*;

@RunWith(VertxUnitRunner.class)
public class MainVerticleTest {

    private Vertx vertx;
    private String testUploadsPath = "src/main/testUploads";

    @Before
    public void setUp(TestContext tc) {
        vertx = Vertx.vertx();
        JsonObject httpConfig = new JsonObject().put("fileUploadsDirectory", testUploadsPath);
        JsonObject dbConfig = new JsonObject()
                .put("url", "jdbc:hsqldb:mem:db/wiki")
                .put("driver_class", "org.hsqldb.jdbcDriver")
                .put("max_pool_size", 5);
        vertx.deployVerticle(new MainVerticle(), new DeploymentOptions().setConfig(httpConfig), tc.asyncAssertSuccess());
        vertx.deployVerticle(new DbVerticle(), new DeploymentOptions().setConfig(dbConfig), tc.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext tc) {
        vertx.close(tc.asyncAssertSuccess());
        if (vertx.fileSystem().existsBlocking(testUploadsPath)) {
            vertx.fileSystem().deleteRecursiveBlocking(testUploadsPath, true);
        }
    }

    @Test
    public void testThatTheServerIsStarted(TestContext tc) {
        Async async = tc.async();
        vertx.createHttpClient().getNow(8080, "localhost", "/", response -> {
            tc.assertEquals(response.statusCode(), 200);
            response.bodyHandler(body -> {
                tc.assertTrue(body.length() > 0);
                async.complete();
            });
        });
    }

    @Test
    public void testAudioUploadIsPossible(TestContext testContext) {
        Async async = testContext.async();
        WebClient webClient = WebClient.create(vertx);
        File musicFile = new File("src/test/java/audio/ex.mp3");
        testContext.assertTrue(musicFile.exists());
        MultipartForm multipartForm = MultipartForm.create()
                .binaryFileUpload("audioExample", musicFile.getName(), musicFile.getAbsolutePath(), "audio/mp3");
        webClient.post(8080, "localhost", "/upload")
                .followRedirects(false)
                .sendMultipartForm(multipartForm, ar -> {
                    HttpResponse response = ar.result();
                    testContext.assertEquals(SEE_OTHER.code(), response.statusCode());
                    async.complete();
                });
    }

    @Test
    public void testOnlyAcceptsAudio(TestContext testContext) {
        Async async = testContext.async();
        WebClient webClient = WebClient.create(vertx);
        File musicFile = new File("src/test/java/audio/ex.mp3");
        testContext.assertTrue(musicFile.exists());
        MultipartForm multipartForm = MultipartForm.create()
                .binaryFileUpload("audioExample", musicFile.getName(), musicFile.getAbsolutePath(), "image/png");
        webClient.post(8080, "localhost", "/upload")
                .followRedirects(false)
                .sendMultipartForm(multipartForm, ar -> {
                    HttpResponse response = ar.result();
                    testContext.assertEquals(BAD_REQUEST.code(), response.statusCode());
                    async.complete();
                });
    }

    @Test
    public void testCanLoadAudio(TestContext testContext) {
        Async async = testContext.async();
        WebClient webClient = WebClient.create(vertx);
        File musicFile = new File("src/test/java/audio/ex.mp3");
        long fileSize = musicFile.length();
        testContext.assertTrue(musicFile.exists());
        MultipartForm multipartForm = MultipartForm.create()
                .binaryFileUpload("audioExample", musicFile.getName(), musicFile.getAbsolutePath(), "audio/mp3");
        webClient.post(8080, "localhost", "/upload")
                .followRedirects(false)
                .sendMultipartForm(multipartForm, ar -> {
                    HttpResponse response = ar.result();
                    testContext.assertEquals(SEE_OTHER.code(), response.statusCode());

                    vertx.createHttpClient().getNow(8080, "localhost", "/download/0", res -> {
                        testContext.assertEquals(OK.code(), res.statusCode());
                        testContext.assertEquals(fileSize, Long.valueOf(res.getHeader("Content-Length")));
                        async.complete();
                    });

                });
    }
}
