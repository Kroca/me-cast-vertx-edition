package kroca.youcast.db;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;

public class MediaDbServiceImpl implements MediaDbService {
    private Logger logger = LoggerFactory.getLogger(MediaDbServiceImpl.class);
    private String createMediaTable = "CREATE TABLE IF NOT EXISTS MEDIA(ID integer identity primary key, title varchar(255), file_path varchar (512) unique );";
    private String getAllMedia = "SELECT * from MEDIA";
    private String createMedia = "insert into MEDIA(id,title,file_path) values (null,?,?)";
    private String findOne = "select * from MEDIA where id = ?";
    private JDBCClient jdbcClient;

    public MediaDbServiceImpl(JDBCClient jdbcClient, Handler<AsyncResult<MediaDbService>> resultHandler) {
        this.jdbcClient = jdbcClient;
        jdbcClient.getConnection(ar -> {
            if (ar.succeeded()) {
                SQLConnection connection = ar.result();
                connection.query(createMediaTable, creationAr -> {
                    connection.close();
                    if (creationAr.succeeded()) {
                        resultHandler.handle(Future.succeededFuture(this));
                    } else {
                        logger.error(ar.cause());
                        resultHandler.handle(Future.failedFuture(ar.cause()));
                    }
                });
            } else {
                logger.error(ar.cause());
                resultHandler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    @Override
    public MediaDbService getMediaList(Handler<AsyncResult<JsonArray>> resultHandler) {
        jdbcClient.query(getAllMedia, res -> {
            if (!res.succeeded()) {
                logger.error(res.cause());
                resultHandler.handle(Future.failedFuture(res.cause()));
            } else {
                JsonArray mediaFiles = new JsonArray(res.result().getResults());
                resultHandler.handle(Future.succeededFuture(mediaFiles));
            }
        });
        return this;
    }

    @Override
    public MediaDbService findOne(Long mediaId, Handler<AsyncResult<JsonObject>> resultHandler) {
        jdbcClient.queryWithParams(findOne, new JsonArray().add(mediaId), res -> {
            if (!res.succeeded()) {
                logger.error(res.cause());
                resultHandler.handle(Future.failedFuture(res.cause()));
            } else {
                JsonObject jsonObject = new JsonObject();
                JsonArray jsonArray = res.result().getResults().get(0);
                jsonObject.put("title", jsonArray.getString(1));
                jsonObject.put("path", jsonArray.getString(2));
                resultHandler.handle(Future.succeededFuture(jsonObject));
            }
        });
        return this;
    }

    @Override
    public MediaDbService save(String title, String path, Handler<AsyncResult<Long>> resultHandler) {
        JsonArray params = new JsonArray().add(title).add(path);
        jdbcClient.updateWithParams(createMedia, params, res -> {
            if (!res.succeeded()) {
                logger.error(res.cause());
                resultHandler.handle(Future.failedFuture(res.cause()));
            } else {
                long entityId = res.result().getKeys().getLong(0);
                resultHandler.handle(Future.succeededFuture(entityId));
            }
        });
        return this;
    }
}
