package io.u.yoke.traits.http.session;

import io.u.yoke.Handler;
import io.u.yoke.http.Response;
import io.u.yoke.traits.StoreTrait;
import io.u.yoke.traits.http.ExchangeTrait;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

public interface SessionTrait extends ExchangeTrait, StoreTrait {

  SessionStore STORE = SessionStore.getDefault();

  default String getSessionId() {
    Map<String, ?> session = getAt("session");
    if (session != null) {
      return (String) session.get("id");
    }

    return null;
  }

  /**
   * Destroys a session from the request context and also from the storage engine.
   */
  default void destroySession() {
    Map<String, ?> session = getAt("session");
    if (session == null) {
      return;
    }

    String sessionId = (String) session.get("id");

    // remove from the context
    putAt("session", null);

    if (sessionId == null) {
      return;
    }

    STORE.destroy(sessionId, error -> {
      if (error != null) {
        // TODO: better handling of errors
        System.err.println(error);
      }
    });
  }

  /**
   * Loads a session given its session id and sets the "session" property in the request context.
   *
   * @param sessionId the id to load
   * @param handler   the success/complete handler
   */
  default void loadSession(@Nullable final String sessionId, final Handler<?> handler) {
    if (sessionId == null) {
      handler.handle(null);
      return;
    }

    STORE.get(sessionId, session -> {
      if (session != null) {
        putAt("session", new SessionMap<>(session));
      }

      final Response res = getResponse();

      res.headersHandler(event -> {
        int responseStatus = res.getStatus().getCode();
        // Only save on success and redirect status codes
        if (responseStatus >= 200 && responseStatus < 400) {
          SessionMap<String, ?> updatedSession = getAt("session");
          if (updatedSession != null && updatedSession.isModified()) {
            STORE.set(sessionId, updatedSession, error -> {
              if (error != null) {
                // TODO: better handling of errors
                System.err.println(error);
              }
            });
          }
        }
      });

      handler.handle(null);
    });
  }

  /** Create a new Session and store it with the underlying storage.
   * Internally create a entry in the request context under the name "session" and add a end handler to save that
   * object once the execution is terminated.
   *
   * @return session
   */
  default Map<String, ?> createSession() {
    return createSession(UUID.randomUUID().toString());
  }


  /** Create a new Session with custom Id and store it with the underlying storage.
   * Internally create a entry in the request context under the name "session" and add a end handler to save that
   * object once the execution is terminated. Custom session id could be used with external auth provider like mod-auth-mgr.
   *
   * @param sessionId custom session id
   * @return session
   */
  default Map<String, ?> createSession(@NotNull final String sessionId) {
    final SessionMap<String, Object> session = new SessionMap<>();
    final Response res = getResponse();

    session.put("id", sessionId);

    putAt("session", session);

    res.headersHandler(v -> {
      int responseStatus = res.getStatus().getCode();
      // Only save on success and redirect status codes
      if (responseStatus >= 200 && responseStatus < 400) {
        SessionMap<String, ?> updatedSession = getAt("session");
        if (updatedSession != null && session.isModified()) {
          STORE.set(sessionId, updatedSession, error -> {
            if (error != null) {
              // TODO: better handling of errors
              System.err.println(error);
            }
          });
        }
      }
    });

    return session;
  }
}
