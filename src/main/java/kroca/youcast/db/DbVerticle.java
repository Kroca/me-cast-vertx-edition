package kroca.youcast.db;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.serviceproxy.ServiceBinder;
import kroca.youcast.util.EBPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbVerticle extends AbstractVerticle {
    private Logger logger = LoggerFactory.getLogger(DbVerticle.class);

    @Override
    public void start(Promise promise) {
        JDBCClient jdbcClient = JDBCClient.createShared(vertx, new JsonObject()
                .put("url", "jdbc:hsqldb:file:db/wiki")
                .put("driver_class", "org.hsqldb.jdbcDriver")
                .put("max_pool_size", 30));

        MediaDbService.create(jdbcClient, res -> {
            if (!res.succeeded()) {
                logger.error(res.cause().getMessage());
                promise.fail(res.cause());
            } else {
                ServiceBinder binder = new ServiceBinder(vertx);
                binder.setAddress(EBPaths.DB_MEDIA)
                        .register(MediaDbService.class, res.result());
                promise.complete();
            }
        });
    }
}
