package io.u.yoke.http.form;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public interface Files {

  Iterable<String> getFiles();

  File getFile(@NotNull String name);
}
