package io.u.yoke;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.concurrent.FastThreadLocal;
import io.u.yoke.impl.AbstractYoke;

import java.net.InetSocketAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public final class NettyYoke extends AbstractYoke {

//  private static final FastThreadLocal<DateFormat> FORMAT = new FastThreadLocal<DateFormat>() {
//    @Override
//    protected DateFormat initialValue() {
//      return new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
//    }
//  };

  private final boolean epool;
  private final Class<? extends ServerChannel> serverChannelClass;

  private EventLoopGroup eventLoopGroup;

//  private volatile CharSequence date = HttpHeaders.newEntity(FORMAT.get().format(new Date()));

  public NettyYoke() {

    epool = Epoll.isAvailable();

    if (epool) {
      serverChannelClass = EpollServerSocketChannel.class;
    } else {
      serverChannelClass = NioServerSocketChannel.class;
    }
  }

  @Override
  public void listen(int port) {

    eventLoopGroup = epool ? new EpollEventLoopGroup() : new NioEventLoopGroup();

    try {
      final InetSocketAddress inet = new InetSocketAddress(port);
      final ServerBootstrap b = new ServerBootstrap();
      final EventLoop eventLoop = eventLoopGroup.next();

//      eventLoop.scheduleWithFixedDelay(new Runnable() {
//        private final DateFormat format = FORMAT.get();
//
//        @Override
//        public void run() {
//          date = HttpHeaders.newEntity(format.format(new Date()));
//        }
//      }, 1000, 1000, TimeUnit.MILLISECONDS);


      b.option(ChannelOption.SO_BACKLOG, 1024);
      b.option(ChannelOption.SO_REUSEADDR, true);
      b.group(eventLoopGroup).channel(serverChannelClass).childHandler(new ChannelInitializer<SocketChannel>() {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
          final ChannelPipeline p = ch.pipeline();

          p.addLast("encoder", new HttpResponseEncoder());
          p.addLast("decoder", new HttpRequestDecoder(4096, 8192, 8192, false));
          p.addLast("handler", new SimpleChannelInboundHandler<Object>() {

            @Override
            protected void channelRead0(ChannelHandlerContext _ctx, Object msg) throws Exception {
              if (msg instanceof HttpRequest) {
                final HttpRequest req = (HttpRequest) msg;

                if (req.getDecoderResult().isFailure()) {
                  // in this case we should end right away with 400
                  final FullHttpResponse res = new DefaultFullHttpResponse(
                      req.getProtocolVersion(),
                      HttpResponseStatus.BAD_REQUEST,
                      Unpooled.wrappedBuffer(HttpResponseStatus.BAD_REQUEST.reasonPhrase().getBytes()),
                      false);

                  _ctx.write(res).addListener(ChannelFutureListener.CLOSE);
                  return;
                }

                final NettyContext ctx = new NettyContext(_ctx, locals, (HttpRequest) msg);

                // add x-powered-by header is enabled
                Boolean poweredBy = ctx.getAt("x-powered-by");
                if (poweredBy != null && poweredBy) {
                  ctx.set("X-Powered-By", "yoke");
                }

                ctx.setIterator(handlers, getErrorHandler());
                // start the handling
                ctx.next();
              }
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
              ctx.close();
            }

            @Override
            public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
              ctx.flush();
            }
          });
        }
      });

      b.childOption(ChannelOption.ALLOCATOR, new PooledByteBufAllocator(true));
      b.childOption(ChannelOption.SO_REUSEADDR, true);
      b.bind(inet).sync().channel();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void close() {
    try {
      if (eventLoopGroup != null) {
        eventLoopGroup.shutdownGracefully().sync();
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}