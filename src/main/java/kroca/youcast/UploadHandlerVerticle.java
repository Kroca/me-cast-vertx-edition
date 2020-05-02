package kroca.youcast;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import kroca.youcast.util.EBPaths;

import java.util.List;

public class UploadHandlerVerticle extends AbstractVerticle {

    private Logger logger = LoggerFactory.getLogger(UploadHandlerVerticle.class);
    private static final String CONTENT_TYPE_MP3 = "audio/mp3";

    @Override
    public void start(Promise promise) throws Exception {
        registerRouter();
    }

    void registerRouter() {
        Router router = Router.router(vertx);
        // Enable multipart form data parsing
        router.route().handler(BodyHandler.create().setUploadsDirectory("src/main/uploads"));

        List<String> files = vertx.fileSystem().readDirBlocking("src/main/uploads");
        //todo сделать нормально =)
        String names = files.stream()
                .map(val -> {
                    String[] vals = val.split("/");
                    return vals[vals.length - 1];
                })
                .map(val -> "<li><a href=\"/download/?name=" + val + "\">" + val + "</a></li>")
                .reduce((c, n) -> c + n)
                .orElse("");
        System.out.println(names);

        router.get("/*").handler(StaticHandler.create().setCachingEnabled(false));
        router.get("/").handler(context -> context.reroute("/index.html"));


        router.route("/download").handler(this::loadFile);
        // handle the form
        router.post("/form").handler(ctx -> {
            ctx.response().putHeader("Content-Type", "text/plain");

            ctx.response().setChunked(true);

            for (FileUpload f : ctx.fileUploads()) {
                ctx.response().write("Filename: " + f.fileName());
                ctx.response().write("\n");
                ctx.response().write("Size: " + f.size());
                JsonObject uploadedFile = new JsonObject();
                uploadedFile.put("title", f.fileName());
                uploadedFile.put("path", f.uploadedFileName());
                //todo add batch handling
                DeliveryOptions deliveryOptions = new DeliveryOptions().addHeader("method", "save");
                //todo add response handler
                vertx.eventBus().send(EBPaths.DB_MEDIA, uploadedFile, deliveryOptions);
            }
            ctx.response().end();
        });

        vertx.createHttpServer().requestHandler(router).listen(8080);
    }

    private void loadFile(RoutingContext request) {
        String name = request.request().getParam("name");
        download(name, request.request());
    }

    private void download(String fileName, HttpServerRequest request) {
        String path = "src/main/uploads/" + fileName;
        if (!vertx.fileSystem().existsBlocking(path)) {
            request.response().setStatusCode(400).end();
        }

        OpenOptions openOptions = new OpenOptions().setRead(true);
        vertx.fileSystem().open(path, openOptions, ar -> {
            if (ar.succeeded()) {
                HttpServerResponse response = request.response();
                response.setStatusCode(200).putHeader("Content-Type", "audio/mpeg").setChunked(true);
                AsyncFile file = ar.result();
                file.pipeTo(response);
            } else {
                logger.error(ar.cause());
                request.response().setStatusCode(500).end();
            }
        });
    }
}
