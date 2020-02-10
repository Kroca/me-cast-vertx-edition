package io.vertx.starter.db;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;


@ProxyGen
@VertxGen
public interface MediaDbService {

    /**
     * Returns list of currently available media files
     */
    @Fluent
    MediaDbService getMediaList(Handler<AsyncResult<JsonArray>> resultHandler);

    @Fluent
    MediaDbService findOne(Long mediaId, Handler<AsyncResult<JsonObject>> resultHandler);

    @Fluent
    MediaDbService save(String title, String path, Handler<AsyncResult<Void>> resultHandler);

    @GenIgnore
    static MediaDbService create(JDBCClient jdbcClient, Handler<AsyncResult<MediaDbService>> resultHandler) {
        return new MediaDbServiceImpl(jdbcClient, resultHandler);
    }

    @GenIgnore
    static MediaDbService createProxy(Vertx vertx, String address) {
        return new MediaDbServiceVertxEBProxy(vertx, address);
    }
}
