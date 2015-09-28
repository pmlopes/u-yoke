# u-Yoke

This is a web micro-framework experiment. The idea is that one can have a simple API and run on top of any HTTP server
implementation, as of now one can use:

* vertx
* undertow
* netty
* jetty

The interesting experiment here is that the API can be used in a blocking server (Jetty) or AIO servers (vertx, netty,
undertow).

