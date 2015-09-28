package io.u.yoke.starter;

import io.u.yoke.Yoke;

import javax.script.*;
import java.io.FileNotFoundException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.util.regex.Pattern;

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

    // create a script loader
    final JSRequire require = new JSRequire(JSMain.class.getClassLoader(), engine, isDebugging());

    Bindings bindings = new SimpleBindings();
    // emulate NodeJS require
    bindings.put("require", require);
    // set a global yoke object
    bindings.put("yoke", Yoke.getDefault());
    engine.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);

    // start the app by loading the main script
    require.require(mainScript);
  }

  private final static Pattern DEBUG = Pattern.compile("-Xdebug|jdwp");

  public static boolean isDebugging() {
    for (String arg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
      if (DEBUG.matcher(arg).find()) {
        return true;
      }
    }
    return false;
  }
}
