package io.u.yoke.traits.http;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collections;

public interface FileUploadTrait {

  default Iterable<String> getFiles() {
    return Collections.emptyList();
  }

  default File getFile(@NotNull String name) {
    return null;
  }
}
