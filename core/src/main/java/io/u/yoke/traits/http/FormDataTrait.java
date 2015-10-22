package io.u.yoke.traits.http;

import java.util.Collections;

public interface FormDataTrait {

  /**
   * Return getRequest getParam by name.
   * <p/>
   * A getParam can be a getPath getParam, getQuery getParam or form getParam.
   *
   * @param parameter the getParam we are looking for.
   * @return its value
   */
  default String getParam(String parameter) {
    return null;
  }

  default Iterable<String> getParamValues(String parameter) {
    return Collections.emptyList();
  }

  /**
   * Returns a list of all the parameters names in the getRequest.
   */
  default Iterable<String> getParams() {
    return Collections.emptyList();
  }
}
