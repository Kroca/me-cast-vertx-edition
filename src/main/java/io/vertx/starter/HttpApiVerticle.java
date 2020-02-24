package io.vertx.starter;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.starter.db.MediaDbService;

import java.util.ArrayList;
import java.util.List;

import static io.vertx.starter.util.EBPaths.DB_MEDIA;

public class HttpApiVerticle extends AbstractVerticle {
    private Logger logger = LoggerFactory.getLogger(HttpApiVerticle.class);
    private MediaDbService mediaDbService;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        Router router = Router.router(vertx);
        this.mediaDbService = MediaDbService.createProxy(vertx, DB_MEDIA);
        router.route().handler(BodyHandler.create().setUploadsDirectory("src/main/uploads"));

        router.get("/*").handler(StaticHandler.create().setCachingEnabled(false));
        router.get("/").handler(context -> context.reroute("/index.html"));
        router.get("/list").handler(this::loadAllMedia);

        router.post().handler(BodyHandler.create());
        router.post("/upload").handler(this::uploadMedia);

        vertx.createHttpServer().requestHandler(router).listen(8080);
    }

    private void uploadMedia(RoutingContext context) {

        List<Future> savedFiles = new ArrayList<>();
        System.out.println(context.fileUploads());
        for (FileUpload fileUpload : context.fileUploads()) {
            savedFiles.add(Future.<Void>future(promise -> mediaDbService.save(fileUpload.fileName(), fileUpload.uploadedFileName(), promise)));
        }
        CompositeFuture.all(savedFiles).setHandler(ar -> {
            if (ar.succeeded()) {
                context.response().end("Files uploaded");
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
                logger.error(res.cause());
                context.fail(res.cause());
            }
        });
    }
}
