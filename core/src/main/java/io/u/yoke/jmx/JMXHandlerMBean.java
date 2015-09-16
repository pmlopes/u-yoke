package io.u.yoke.jmx;

import io.u.yoke.Handler;

public interface JMXHandlerMBean<T> extends Handler<T> {

  boolean isEnabled();
  void setEnabled(boolean enabled);

  int getIndex();

  void moveUp();
  void moveDown();
}
