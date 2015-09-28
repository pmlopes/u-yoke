package io.u.yoke.starter;

import jdk.nashorn.api.scripting.JSObject;
import org.jetbrains.annotations.NotNull;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Set;

public class JSRequire implements JSObject {

  private final boolean debug;
  private final ClassLoader cl;
  private final ScriptEngine engine;

  JSRequire(ClassLoader cl, ScriptEngine engine, boolean debug) {
    this.cl = cl;
    this.engine = engine;
    this.debug = debug;
  }


  Object require(@NotNull String path) throws ScriptException {
    if (!path.endsWith(".js")) {
      path += ".js";
    }

    if (debug) {
      // in this case we cannot load from the classpath since intelliJ will not understand that
      File file = new File(System.getProperty("user.dir"), path);
      if (file.exists()) {
        // found it
        return engine.eval("load(\"" + file.getAbsolutePath() + "\")");
      } else {
        throw new ScriptException("cannot find script '" + path + "' in '" + System.getProperty("user.dir") + "'");
      }
    } else {
      URL url = cl.getResource(path);
      if (url != null) {
        return engine.eval("load(\"" + url.toExternalForm() + "\")");
      } else {
        throw new ScriptException("cannot find script '" + path + "' in classpath");
      }
    }
  }

  @Override
  public Object call(Object o, Object... objects) {
    if (objects == null || objects.length != 1) {
      throw new UnsupportedOperationException("Invalid arguments to require(var:String)");
    }

    try {
      return require(objects[0].toString());
    } catch (ScriptException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Object newObject(Object... objects) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object eval(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object getMember(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object getSlot(int i) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasMember(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasSlot(int i) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeMember(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setMember(String s, Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setSlot(int i, Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<String> keySet() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Collection<Object> values() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isInstance(Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isInstanceOf(Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getClassName() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isFunction() {
    return true;
  }

  @Override
  public boolean isStrictFunction() {
    return false;
  }

  @Override
  public boolean isArray() {
    return false;
  }

  @Override
  public double toNumber() {
    throw new UnsupportedOperationException();
  }
}
