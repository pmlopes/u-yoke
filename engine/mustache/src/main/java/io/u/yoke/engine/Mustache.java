/**
 * Copyright 2011-2014 the original author or authors.
 */
package io.u.yoke.engine;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheFactory;
import com.github.mustachejava.resolver.ClasspathResolver;
import io.u.yoke.Context;
import io.u.yoke.Engine;
import io.u.yoke.impl.AbstractContext;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class Mustache implements Engine {

  private final Map<String, com.github.mustachejava.Mustache> cache = new HashMap<>();

  private final MustacheFactory mustache;
  private static final String extension = ".mustache";

  @Override
  public String getExtension() {
    return extension;
  }

  public Mustache() {
    mustache = new DefaultMustacheFactory(new ClasspathResolver("views"));
  }

  @Override
  public void render(final String filename, final Context ctx) {
    try {
      com.github.mustachejava.Mustache template = cache.get(filename);

      if (template == null) {
        synchronized (this) {
          // normalize
          final String templateName;

          if (filename.endsWith(extension)) {
            templateName = filename.substring(0, filename.length() - extension.length());
          } else {
//            templateName = filename;
            templateName = filename + extension;
          }

          // compile
          template = mustache.compile(templateName);
          cache.put(filename, template);
        }
      }

      final StringWriter render = new StringWriter();

      template.execute(render, ((AbstractContext) ctx).getLocals());

      if (ctx.get("Context-Type") == null) {
        ctx.getResponse().setType("text/html; charset=utf-8");
      }
      ctx.getResponse().end(render.toString());

    } catch (Exception ex) {
      ex.printStackTrace();
      ctx.fail(ex);
    }
  }

  public final MustacheFactory getMustache() {
    return mustache;
  }
}
