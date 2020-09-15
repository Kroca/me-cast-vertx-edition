package kroca.youcast.db;


import io.reactivex.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.SingleHelper;
import io.vertx.reactivex.core.Future;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import io.vertx.reactivex.ext.sql.SQLClientHelper;
import io.vertx.reactivex.ext.sql.SQLConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MediaDbServiceImpl implements MediaDbService {
    private Logger logger = LoggerFactory.getLogger(MediaDbServiceImpl.class);
    private String createMediaTable = "CREATE TABLE IF NOT EXISTS MEDIA(ID integer identity primary key, title varchar(255), file_path varchar (512) unique );";
    private String getAllMedia = "SELECT * from MEDIA";
    private String createMedia = "insert into MEDIA(id,title,file_path) values (null,?,?)";
    private String findOne = "select * from MEDIA where id = ?";
    private JDBCClient jdbcClient;

    public MediaDbServiceImpl(JDBCClient jdbcClient, Handler<AsyncResult<MediaDbService>> resultHandler) {
        this.jdbcClient = jdbcClient;
        SQLClientHelper.usingConnectionSingle(jdbcClient, con ->
                con.rxExecute(createMediaTable)
                        .andThen(Single.just(this)))
                .subscribe(SingleHelper.toObserver(resultHandler));
    }

    @Override
    public MediaDbService getMediaList(Handler<AsyncResult<JsonArray>> resultHandler) {
        jdbcClient.rxQuery(getAllMedia)
                .map(resultSet -> new JsonArray(resultSet.getResults()))
                .subscribe(SingleHelper.toObserver(resultHandler));
        return this;
    }

    @Override
    public MediaDbService findOne(Long mediaId, Handler<AsyncResult<JsonObject>> resultHandler) {
        jdbcClient.rxQueryWithParams(findOne, new JsonArray().add(mediaId))
                .map(resultSet -> {
                    JsonObject jsonObject = new JsonObject();
                    JsonArray jsonArray = resultSet.getResults().get(0);
                    jsonObject.put("title", jsonArray.getString(1));
                    jsonObject.put("path", jsonArray.getString(2));
                    return jsonObject;
                })
                .subscribe(SingleHelper.toObserver(resultHandler));
        return this;
    }

    @Override
    public MediaDbService save(String title, String path, Handler<AsyncResult<Long>> resultHandler) {
        JsonArray params = new JsonArray().add(title).add(path);
        jdbcClient.rxUpdateWithParams(createMedia, params)
                .map(res -> res.getKeys().getLong(0))
                .subscribe(SingleHelper.toObserver(resultHandler));
        return this;
    }
}
