package io.vertx.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;

import static io.vertx.starter.util.EBPaths.DB_MEDIA;

public class DbVerticle extends AbstractVerticle {

    private Logger logger = LoggerFactory.getLogger(DbVerticle.class);
    private String createMediaTable = "CREATE TABLE IF NOT EXISTS MEDIA(ID integer identity primary key, title varchar(255), uuid varchar (255) unique );";
    private String getAllMedia = "SELECT * from MEDIA";
    private String createMedia = "insert into MEDIA(id,title,uuid) values (null,?,?)";
    JDBCClient jdbcClient;

    @Override
    public void start(Promise promise) throws Exception {
        jdbcClient = JDBCClient.createShared(vertx, new JsonObject()
                .put("url", "jdbc:hsqldb:file:db/wiki")
                .put("driver_class", "org.hsqldb.jdbcDriver")
                .put("max_pool_size", 30));
        System.out.println(config().getString("test", "ttt"));
        jdbcClient.getConnection(ar -> {
            if (ar.succeeded()) {
                SQLConnection connection = ar.result();
                connection.query(createMediaTable, creationAr -> {
                    connection.close();
                    if (creationAr.succeeded()) {
                        System.out.println("Success");
                        vertx.eventBus().localConsumer(DB_MEDIA, this::onMessage);
                        promise.complete();
                    } else {
                        logger.error(ar.cause());
                    }
                });
            } else {
                logger.error(ar.cause());
                promise.fail("Hui");
            }
        });

    }

    private void onMessage(Message<String> message) {
        System.out.println("received request");
        String action = message.body();
        switch (action) {
            case "create":
                saveMedia();
                break;
            case "getList":
                getMediaList();
                break;
            default:
                System.out.println("idi naher");
                break;
        }
    }

    public void saveMedia() {
        jdbcClient.getConnection(car -> {
            if (car.succeeded()) {
                SQLConnection connection = car.result();
                JsonArray params = new JsonArray();
                params.add("title").add("uuid");
                connection.updateWithParams(createMedia, params, creationRes -> {
                    connection.close();
                    if (creationRes.succeeded()) {
                        System.out.println("Success :" + creationRes.result().toJson());
                    } else {
                        logger.error(creationRes.cause());
                    }
                });
            } else {
                logger.error(car.cause());
            }
        });
    }

    private void getMediaList() {
        jdbcClient.getConnection(car -> {
            if (car.succeeded()) {
                SQLConnection connection = car.result();
                connection.query(getAllMedia, res -> {
                    connection.close();
                    if (res.succeeded()) {
                        System.out.println(res.result().toJson());
                    } else {
                        logger.error(res.cause());
                    }
                });
            } else {
                logger.error(car.cause());
            }
        });
    }

    private void getById() {

    }
}
