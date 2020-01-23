package io.vertx.starter;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.sql.SQLConnection;

public class GetConnectionHandler implements Handler<AsyncResult<SQLConnection>> {
    @Override
    public void handle(AsyncResult<SQLConnection> sqlConnectionAsyncResult) {
        if (sqlConnectionAsyncResult.succeeded()) {
            System.out.println("Succefully acquired connection");
        } else {
            System.out.println(sqlConnectionAsyncResult.cause());
        }
    }
}
