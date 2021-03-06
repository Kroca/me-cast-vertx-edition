package kroca.youcast.api;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.FileProps;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import kroca.youcast.db.MediaDbService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static kroca.youcast.util.EBPaths.DB_MEDIA;

public class HttpApiVerticle extends AbstractVerticle {
    private Logger logger = LoggerFactory.getLogger(HttpApiVerticle.class);
    private MediaDbService mediaDbService;
    private RadioStreaming streaming;

    @Override
    public void start(Promise<Void> startPromise) {
        JsonObject config = buildConfig();
        Router router = Router.router(vertx);
        this.mediaDbService = MediaDbService.createProxy(vertx, DB_MEDIA);
        streaming = new RadioStreaming(vertx);
        router.route().handler(BodyHandler.create()
                .setUploadsDirectory(config.getString("fileUploadsDirectory"))
        );

        router.get("/*").handler(StaticHandler.create().setCachingEnabled(false));
        router.get("/").handler(context -> context.reroute("/index.html"));
        router.get("/list").handler(this::loadAllMedia);
        router.get("/radio").handler(streaming);
        router.post().handler(BodyHandler.create());
        router.post("/upload").handler(this::uploadMedia);
        router.get("/download/:id").handler(this::downloadMedia);

        vertx.createHttpServer().requestHandler(router).listen(8080);
    }

    private JsonObject buildConfig() {
        String defaultUploadsPath = "src/main/uploads";
        return new JsonObject()
                .put("fileUploadsDirectory", config().getString("fileUploadsDirectory", defaultUploadsPath));
    }

    private void downloadMedia(RoutingContext context) {
        Long fileId = Long.valueOf(context.request().getParam("id"));
        mediaDbService.findOne(fileId, res -> {
            if (res.succeeded()) {
                JsonObject result = res.result();
                String path = result.getString("path");
                if (!vertx.fileSystem().existsBlocking(path)) {
                    context.response().setStatusCode(400).end();
                    return;
                }
                FileSystem fileSystem = vertx.fileSystem();
                OpenOptions openOptions = new OpenOptions().setRead(true);
                fileSystem.props(path, props -> {
                    FileProps fileProps = props.result();
                    fileProps.size();
                    fileSystem.open(path, openOptions, ar -> {
                        if (ar.succeeded()) {
                            HttpServerResponse response = context.response();
                            response.setStatusCode(200)
                                    .putHeader("Content-Length", "" + fileProps.size())
                                    .putHeader("Content-Type", "audio/mpeg")
                                    .putHeader("Accept-Ranges", "bytes")
                                    .setChunked(true);
                            AsyncFile file = ar.result();
                            file.pipeTo(response);
                            file.endHandler(endRes -> response.end());
                        } else {
                            logger.error(ar.cause().getMessage());
                            context.response().setStatusCode(500).end();
                        }
                    });
                });
            } else {
                context.fail(res.cause());
            }
        });
    }

    private void uploadMedia(RoutingContext context) {
        List<Future> savedFiles = new ArrayList<>();
        for (FileUpload fileUpload : context.fileUploads()) {
            if (fileUpload.contentType().startsWith("audio/")) {
                savedFiles.add(Future.<Long>future(promise ->
                        mediaDbService.save(fileUpload.fileName(), fileUpload.uploadedFileName(), promise)));
            } else {
                context.fail(400);
                return;
            }
        }
        CompositeFuture.all(savedFiles).setHandler(ar -> {
            if (ar.succeeded()) {
                context.response().setStatusCode(303);
                context.response().putHeader("Location", "/index.html");
                context.response().end();
            } else {
                logger.error("Could't save any data");
                context.fail(500);
            }
        });

    }

    private void loadAllMedia(RoutingContext context) {
        mediaDbService.getMediaList(res -> {
            if (res.succeeded()) {
                context.response().end(res.result().encode());
            } else {
                logger.error(res.cause().getMessage());
                context.fail(res.cause());
            }
        });
    }
}
