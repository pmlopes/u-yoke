/**
 * Copyright 2011-2014 the original author or authors.
 */
package io.u.yoke.engine;

import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.github.jknack.handlebars.io.TemplateSource;
import io.u.yoke.Context;
import io.u.yoke.Engine;
import io.u.yoke.impl.AbstractContext;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Handlebars implements Engine {

  private final Map<String, Template> cache = new HashMap<>();

  private final com.github.jknack.handlebars.Handlebars handlebars;
  private static final String extension = ".hbs";

  @Override
  public String getExtension() {
    return extension;
  }

  public Handlebars() {
    handlebars = new com.github.jknack.handlebars.Handlebars(new ClassPathTemplateLoader("/views", extension));
  }

  @Override
  public void render(final String filename, final Context ctx) {
    try {
      Template template = cache.get(filename);

      if (template == null) {
        // normalize
        final String templateName = filename.substring(0, filename.length() - extension.length());
        // compile
        template = handlebars.compile(templateName);
        cache.put(filename, template);
      }

      final String render = template.apply(((AbstractContext) ctx).getLocals());
      if (ctx.get("Context-Type") == null) {
        ctx.response().setType("text/html; charset=utf-8");
      }
      ctx.response().end(render);

    } catch (Exception ex) {
      ex.printStackTrace();
      ctx.fail(ex);
    }
  }

  public final com.github.jknack.handlebars.Handlebars getHandlebars() {
    return handlebars;
  }
}
