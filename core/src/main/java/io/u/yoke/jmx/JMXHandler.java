package io.u.yoke.jmx;

import io.u.yoke.Context;
import io.u.yoke.Handler;
import org.jetbrains.annotations.NotNull;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.List;

public class JMXHandler<C extends Context> implements JMXHandlerMBean<C> {

  private final String prefix;
  private final List<JMXHandlerMBean<C>> handlers;
  private final Handler<C> delegate;

  private boolean enabled = true;


  public JMXHandler(@NotNull List<JMXHandlerMBean<C>> handlers, @NotNull String route, String prefix, @NotNull String method, @NotNull Handler<C> delegate) {
    this.prefix = prefix;
    this.handlers = handlers;
    this.delegate = delegate;

    // register on JMX
    try {
      ManagementFactory.getPlatformMBeanServer().registerMBean(this, new ObjectName("io.u.yoke:type=JMXHandler,route=" + ObjectName.quote(route) + ",verb=" + ObjectName.quote(method) + ",name=" + delegate.getClass().getSimpleName() + "@" + hashCode()));
    } catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public int getIndex() {
    return handlers.indexOf(this);
  }

  @Override
  public void moveUp() {
    int idx = handlers.indexOf(this);
    if (idx != -1) {
      final JMXHandlerMBean<C> self = handlers.get(idx);
      if (idx == 0) {
        // swap with last
        final int last = handlers.size() - 1;
        handlers.set(idx, handlers.get(last));
        handlers.set(last, self);
      } else {
        // swap with previous
        final int previous = idx - 1;
        handlers.set(idx, handlers.get(previous));
        handlers.set(previous, self);
      }
    }
  }

  @Override
  public void moveDown() {
    int idx = handlers.indexOf(this);
    if (idx != -1) {
      final JMXHandlerMBean<C> self = handlers.get(idx);
      if (idx == handlers.size() - 1) {
        // swap with first
        handlers.set(idx, handlers.get(0));
        handlers.set(0, self);
      } else {
        // swap with next
        final int next = idx + 1;
        handlers.set(idx, handlers.get(next));
        handlers.set(next, self);
      }
    }
  }

  @Override
  public void handle(C ctx) {
    if (isEnabled()) {
      if (prefix == null || ctx.getRequest().getPath().startsWith(prefix)) {
        delegate.handle(ctx);
      } else {
        ctx.next();
      }
    } else {
      ctx.next();
    }
  }
}
