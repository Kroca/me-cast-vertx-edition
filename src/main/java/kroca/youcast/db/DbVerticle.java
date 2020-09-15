package kroca.youcast.db;


import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.jdbc.JDBCClient;

import io.vertx.serviceproxy.ServiceBinder;
import kroca.youcast.util.EBPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbVerticle extends AbstractVerticle {
    private Logger logger = LoggerFactory.getLogger(DbVerticle.class);

    @Override
    public void start(Promise promise) {
        JsonObject dbConfig = new JsonObject()
                .put("url", config().getString("url", "jdbc:hsqldb:file:db/wiki"))
                .put("driver_class", config().getString("driver_class", "org.hsqldb.jdbcDriver"))
                .put("max_pool_size", config().getInteger("max_pool_size", 30));

        JDBCClient jdbcClient = JDBCClient.createShared(vertx, dbConfig);
        MediaDbService.create(jdbcClient, res -> {
            if (!res.succeeded()) {
                logger.error(res.cause().getMessage());
                promise.fail(res.cause());
            } else {
                ServiceBinder binder = new ServiceBinder(vertx.getDelegate());
                binder.setAddress(EBPaths.DB_MEDIA)
                        .register(MediaDbService.class, res.result());
                promise.complete();
            }
        });
    }
}
