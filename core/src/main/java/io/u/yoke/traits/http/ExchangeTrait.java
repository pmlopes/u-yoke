package io.u.yoke.traits.http;

import io.u.yoke.http.Request;
import io.u.yoke.http.Response;

public interface ExchangeTrait {

  Request getRequest();

  Response getResponse();
}
