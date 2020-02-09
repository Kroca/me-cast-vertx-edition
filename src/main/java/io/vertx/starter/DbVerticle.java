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
    private String createMediaTable = "CREATE TABLE IF NOT EXISTS MEDIA(ID integer identity primary key, title varchar(255), file_path varchar (512) unique );";
    private String getAllMedia = "SELECT * from MEDIA";
    private String createMedia = "insert into MEDIA(id,title,file_path) values (null,?,?)";
    private String findOne = "select * from MEDIA where id = ?";
    private JDBCClient jdbcClient;

    @Override
    public void start(Promise promise) {
        jdbcClient = JDBCClient.createShared(vertx, new JsonObject()
                .put("url", "jdbc:hsqldb:mem:db/wiki")
                .put("driver_class", "org.hsqldb.jdbcDriver")
                .put("max_pool_size", 30));
        System.out.println(config().getString("test", "ttt"));
        jdbcClient.getConnection(ar -> {
            if (ar.succeeded()) {
                SQLConnection connection = ar.result();
                connection.query(createMediaTable, creationAr -> {
                    connection.close();
                    if (creationAr.succeeded()) {
                        vertx.eventBus().localConsumer(DB_MEDIA, this::onMessage);
                        promise.complete();
                    } else {
                        logger.error(ar.cause());
                        promise.fail(ar.cause());
                    }
                });
            } else {
                logger.error(ar.cause());
                promise.fail(ar.cause());
            }
        });
    }

    private void onMessage(Message<JsonObject> message) {
        System.out.println("received request");
        String method = message.headers().get("method");
        switch (method) {
            case "save":
                saveMedia(message);
                break;
            case "list":
                getMediaList(message);
                break;
            case "findOne":
                findById(message);
                break;
            default:
                logger.error("method not supported");
                break;
        }
    }

    public void saveMedia(Message<JsonObject> message) {
        jdbcClient.getConnection(car -> {
            if (car.succeeded()) {
                SQLConnection connection = car.result();
                JsonArray params = new JsonArray();
                params
                        .add(message.body().getValue("title"))
                        .add(message.body().getValue("path"));
                connection.updateWithParams(createMedia, params, creationRes -> {
                    connection.close();
                    if (creationRes.succeeded()) {
                        System.out.println("Success :" + creationRes.result().toJson());
                        message.reply(creationRes.result().toJson().toString());
                    } else {
                        message.fail(500, creationRes.cause().getLocalizedMessage());
                        logger.error(creationRes.cause());
                    }
                });
            } else {
                message.fail(500, car.cause().getMessage());
                logger.error(car.cause());
            }
        });
    }

    private void getMediaList(Message<JsonObject> message) {
        jdbcClient.getConnection(car -> {
            if (car.succeeded()) {
                SQLConnection connection = car.result();
                connection.query(getAllMedia, res -> {
                    connection.close();
                    if (res.succeeded()) {
                        message.reply(res.result().toJson().toString());
                        System.out.println(res.result().toJson());
                    } else {
                        message.fail(500, res.cause().getMessage());
                        logger.error(res.cause());
                    }
                });
            } else {
                logger.error(car.cause());
            }
        });
    }

    private void findById(Message<JsonObject> message) {
        jdbcClient.getConnection(car -> {
            if (car.succeeded()) {
                SQLConnection connection = car.result();
                JsonArray params = new JsonArray().add(message.body().getValue("id"));
                connection.queryWithParams(findOne, params, res -> {
                    connection.close();
                    if (res.succeeded()) {
                        message.reply(res.result().toJson().toString());
                    } else {
                        message.fail(500, res.cause().getMessage());
                        logger.error(res.cause());
                    }
                });
            }
        });
    }
}
