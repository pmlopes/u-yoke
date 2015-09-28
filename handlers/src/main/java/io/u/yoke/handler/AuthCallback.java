/**
 * Copyright 2011-2014 the original author or authors.
 */
package io.u.yoke.handler;

import io.u.yoke.Callback;

/**
 * # AuthCallback
 * <p/>
 * AuthCallback interface that needs to be implemented in order to validate usernames/passwords.
 */
@FunctionalInterface
public interface AuthCallback {
  /**
   * Handles a challenge authentication getRequest and asynchronously returns the user object on success, null for error.
   *
   * @param username  the security principal user name
   * @param password  the security principal password
   * @param cb        authentication callback
   */
  void handle(String username, String password, Callback<String> cb);
}
