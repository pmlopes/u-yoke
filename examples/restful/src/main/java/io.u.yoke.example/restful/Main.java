package io.u.yoke.example.restful;

import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoDatabase;
import io.u.yoke.Yoke;
import io.u.yoke.example.restful.routes.Index;
import io.u.yoke.example.restful.routes.users.UserCreate;
import io.u.yoke.example.restful.routes.users.UserDelete;
import io.u.yoke.example.restful.routes.users.UserList;
import io.u.yoke.handler.ErrorHandler;
import io.u.yoke.handler.Logger;
import io.u.yoke.handler.Router;

import java.io.File;

public class Main {

  public static void main(String[] args) {

    final Yoke yoke = Yoke.getDefault();

    final MongoDatabase database = MongoClients.create(System.getProperty("mongo.host", "mongodb://localhost"))
        .getDatabase("hello_world");

    yoke.use(ctx -> {
      // make the db available on all routes
      ctx.putAt("db", database);
      ctx.next();
    });

    yoke.use("/download", ctx -> {
      ctx.response().sendFile("public/javascripts/global.js");
    });

    // setup the routes
    yoke.use(new Router()
        .get("/", new Index())
        .get("/users", new UserList())
        .post("/users", new UserCreate())
        .delete("/users/:id", new UserDelete()));

    yoke.setErrorHandler(new ErrorHandler(true));

    yoke.listen(8080);
  }
}
