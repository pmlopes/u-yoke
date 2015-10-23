package io.u.yoke.http.form;

public interface Form {

  String getParam(String parameter);

  Iterable<String> getParamValues(String parameter);

  Iterable<String> getParams();

  void remove(String parameter);
}
