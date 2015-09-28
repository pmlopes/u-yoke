/**
 * Copyright 2011-2014 the original author or authors.
 */
package io.u.yoke.engine;

import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.template.ClasspathTemplateLoader;
import de.neuland.jade4j.template.JadeTemplate;
import io.u.yoke.Context;
import io.u.yoke.Engine;
import io.u.yoke.impl.AbstractContext;

import java.util.HashMap;
import java.util.Map;

public class Jade implements Engine {

  private static final String extension = ".jade";

  private final Map<String, JadeTemplate> cache = new HashMap<>();
  private final JadeConfiguration jade;

  @Override
  public String getExtension() {
    return extension;
  }

  public Jade() {
    jade = new JadeConfiguration();
    // we always use classpath
    jade.setTemplateLoader(new ClasspathTemplateLoader());
  }

  @Override
  public void render(final String filename, final Context ctx) {
    try {
      JadeTemplate template = cache.get(filename);

      if (template == null) {
        synchronized (this) {
          // normalize
          final String templateName;

          if (filename.endsWith(extension)) {
            templateName = filename.substring(0, filename.length() - extension.length());
          } else {
            templateName = filename;
          }

          // compile
          template = jade.getTemplate("views/" + templateName);
          cache.put(filename, template);
        }
      }

      final String render = jade.renderTemplate(template, ((AbstractContext) ctx).getLocals());

      if (ctx.get("Context-Type") == null) {
        ctx.getResponse().setType("text/html; charset=utf-8");
      }
      ctx.getResponse().end(render);

    } catch (Exception ex) {
      ex.printStackTrace();
      ctx.fail(ex);
    }
  }

  public final JadeConfiguration getJade() {
    return jade;
  }
}
