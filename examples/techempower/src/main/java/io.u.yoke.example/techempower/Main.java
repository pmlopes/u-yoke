package io.u.yoke.example.techempower;

import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import io.u.yoke.Handler;
import io.u.yoke.Yoke;
import io.u.yoke.example.techempower.model.Fortune;
import io.u.yoke.example.techempower.model.Message;
import io.u.yoke.example.techempower.model.World;
import org.bson.Document;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mongodb.client.model.Filters.*;

public class Main {

  public static void main(String[] args) {

    final Yoke yoke = Yoke.getDefault();

    final MongoDatabase database = MongoClients.create(System.getProperty("mongo.host", "mongodb://localhost"))
        .getDatabase("hello_world");

    yoke.use(ctx -> {
      ctx.set("Server", "Yoke/3.0");
      ctx.next();
    });

    /**
     * This test exercises the framework fundamentals including keep-alive support, request routing, request header
     * parsing, object instantiation, JSON serialization, response header generation, and request count throughput.
     */
    yoke.use("/json", ctx -> {
      ctx.json(new Message("Hello, World!"));
    });

    /**
     * This test exercises the framework's object-relational mapper (ORM), random number generator, database driver,
     * and database connection pool.
     */
    yoke.use("/db", ctx -> {
      database.getCollection("world").find(eq("_id", Helper.randomWorld())).first((document, throwable) -> {
        if (throwable != null) {
          ctx.fail(throwable);
          return;
        }

        ctx.json(new World(
            // Need to cast because mongo returns numbers as doubles
            ((Number) document.get("_id")).intValue(),
            ((Number) document.get("randomNumber")).intValue()
        ));
      });
    });

    /**
     * This test is a variation of Test #2 and also uses the World table. Multiple rows are fetched to more dramatically
     * punish the database driver and connection pool. At the highest queries-per-request tested (20), this test
     * demonstrates all frameworks' convergence toward zero requests-per-second as database activity increases.
     */
    yoke.use("/queries", ctx -> {
      final int queries = Helper.getQueries(ctx);

      final MongoCollection<Document> coll = database.getCollection("world");

      final World[] worlds = new World[queries];

      new Handler<Integer>() {
        @Override
        public void handle(Integer index) {
          final Handler<Integer> self = this;
          coll.find(eq("_id", Helper.randomWorld())).first((document, throwable) -> {
            if (throwable != null) {
              ctx.fail(throwable);
              return;
            }

            worlds[index] = new World(
                // Need to cast because mongo returns numbers as doubles
                ((Number) document.get("_id")).intValue(),
                ((Number) document.get("randomNumber")).intValue()
            );

            int next = index + 1;

            if (next == queries) {
              ctx.json(worlds);
            } else {
              self.handle(next);
            }
          });
        }
      }.handle(0);
    });

    /**
     * This test exercises the ORM, database connectivity, dynamic-size collections, sorting, server-side templates,
     * XSS countermeasures, and character encoding.
     */
    yoke.use("/fortunes", ctx -> {
      final List<Fortune> fortunes = new LinkedList<>();

      database.getCollection("fortune").find().forEach(document -> {
        fortunes.add(new Fortune(
            // Need to cast because mongo returns numbers as doubles
            ((Number) document.get("_id")).intValue(),
            document.getString("message")
        ));
      }, (aVoid, throwable) -> {
        if (throwable != null) {
          ctx.fail(throwable);
          return;
        }

        fortunes.add(new Fortune(0, "Additional fortune added at request time."));
        Collections.sort(fortunes);

        ctx.putAt("fortunes", fortunes);
        ctx.getResponse().render("fortunes");
      });
    });

    /**
     * This test is a variation of Test #3 that exercises the ORM's persistence of objects and the database driver's
     * performance at running UPDATE statements or similar. The spirit of this test is to exercise a variable number of
     * read-then-write style database operations.
     */
    yoke.use("/update", ctx -> {
      final int queries = Helper.getQueries(ctx);

      final MongoCollection<Document> coll = database.getCollection("world");

      final World[] worlds = new World[queries];
      final AtomicInteger cnt = new AtomicInteger(0);

      for (int i = 0; i < queries; i++) {
        final int id = Helper.randomWorld();
        final int idx = i;

        coll.find(eq("_id", id)).first((document, throwable) -> {
          if (throwable != null) {
            ctx.fail(throwable);
            return;
          }

          // Per test requirement the old value must be read
          final Number oldRandomNumber = (Number) document.get("randomNumber");
          final int newRandomNumber = Helper.randomWorld();

          coll.updateOne(eq("_id", id),
              new Document("$putAt", new Document("randomNumber", newRandomNumber)), (result, throwable2) -> {
                if (throwable2 != null) {
                  ctx.fail(throwable2);
                  return;
                }

                worlds[idx] = new World(
                    // Need to cast because mongo returns numbers as doubles
                    id,
                    newRandomNumber
                );

                if (cnt.incrementAndGet() == queries) {
                  ctx.json(worlds);
                }
              });
        });
      }
    });

    /**
     * This test is an exercise of the request-routing fundamentals only, designed to demonstrate the capacity of
     * high-performance platforms in particular. Requests will be sent using HTTP pipelining. The response payload is
     * still small, meaning good performance is still necessary in order to saturate the gigabit Ethernet of the test
     * environment.
     */
    yoke.use("/plaintext", ctx -> {
      ctx.setType("text/plain");
      ctx.end("Hello, World!");
    });

    yoke.listen(8080);
  }
}
