package io.u.yoke.starter;

import io.u.yoke.Yoke;

import javax.script.*;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;

public class JSMain {
  public static void main(String[] args) throws ScriptException, FileNotFoundException, MalformedURLException {

    String mainScript;

    switch (args.length) {
      case 0:
        mainScript = "index.js";
        break;
      case 1:
        mainScript = args[0];
        break;
      default:
        throw new ScriptException("Too many startup scripts!");
    }

    ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

    Bindings bindings = new SimpleBindings();
    bindings.put("yoke", Yoke.getDefault());
    engine.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);

    engine.eval("load('" + mainScript + "')");
  }
}
