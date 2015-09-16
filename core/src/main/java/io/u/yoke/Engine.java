package io.u.yoke;

@FunctionalInterface
public interface Engine {

  default String getExtension() {
    return "html";
  }

  /**
   * The implementation of the render engine. The implementation should render the given file with the context in an
   * asynchronous way.
   *
   * @param filename String representing the file path to the template
   * @param context  Map with key values that might get substituted in the template
   */
  void render(final String filename, final Context context);
}
