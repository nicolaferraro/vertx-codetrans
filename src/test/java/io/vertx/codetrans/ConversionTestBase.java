package io.vertx.codetrans;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import jdk.nashorn.internal.runtime.ScriptObject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public abstract class ConversionTestBase {

  public static Lang[] langs() { return new Lang[] { new GroovyLang(), new JavaScriptLang() }; }

  public String runJavaScript(String path) {
    return run(new JavaScriptLang(), path);
  }

  public String runJavaScript(String path, String method) {
    return run(new JavaScriptLang(), path, method);
  }

  public String runGroovy(String path) {
    return run(new GroovyLang(), path);
  }

  public String runGroovy(String path, String method) {
    return run(new GroovyLang(), path, method);
  }

  public void runAll(String path, Runnable after) {
    for (Lang lang : langs()) {
      run(lang, path);
      after.run();
    }
  }

  public void runAll(String path, String method, Runnable after) {
    for (Lang lang : langs()) {
      run(lang, path, method);
      after.run();
    }
  }

  public String run(Lang lang, String path) {
    return run(lang, path, "start");
  }

  public String run(Lang lang, String path, String method) {
    Map<String, String> results;
    try {
      results = ConvertingProcessor.convert(ClassIdentifierExpressionTest.class.getClassLoader(), lang, path + ".java");
    } catch (Exception e) {
      throw new AssertionError(e);
    }
    Thread current = Thread.currentThread();
    ClassLoader prev = current.getContextClassLoader();
    LoadingClassLoader loader = new LoadingClassLoader(current.getContextClassLoader(), results);
    current.setContextClassLoader(loader);
    try {
      Callable<?> callable = lang.compile(loader, path + "_" + method);
      callable.call();
    } catch (Exception e) {
      throw new AssertionError(e);
    } finally {
      current.setContextClassLoader(prev);
    }
    return results.get(path + "_" + method + "." + lang.getExtension());
  }

  private Object unwrapJsonElement(ScriptObject obj) {
    if (obj.isArray()) {
      return unwrapJsonArray(obj);
    } else {
      return unwrapJsonObject(obj);
    }
  }

  public JsonObject unwrapJsonObject(ScriptObject obj) {
    JsonObject unwrapped = new JsonObject();
    for (String key : obj.getOwnKeys(true)) {
      Object value = obj.get(key);
      if (value instanceof ScriptObject) {
        value = unwrapJsonElement((ScriptObject) value);
      }
      unwrapped.put(key, value);
    }
    return unwrapped;
  }

  public JsonArray unwrapJsonArray(ScriptObject obj) {
    JsonArray unwrapped = new JsonArray();
    long len = (long) obj.getLength();
    for (int i = 0;i < len;i++) {
      Object value = obj.get(i);
      if (value instanceof ScriptObject) {
        value = unwrapJsonElement((ScriptObject) value);
      }
      unwrapped.add(value);
    }
    return unwrapped;
  }

  public JsonObject unwrapJsonObject(Map<String, ?> obj) {
    JsonObject ret = new JsonObject();
    obj.forEach((k,v) -> ret.put(k, unwrapJson(v)));
    return ret;
  }

  public JsonArray unwrapJsonArray(List<?> obj) {
    JsonArray ret = new JsonArray();
    obj.forEach(e -> ret.add(unwrapJson(e)));
    return ret;
  }

  private Object unwrapJson(Object o) {
    if (o instanceof Map) {
      return unwrapJsonObject((Map<String, ?>) o);
    } else if (o instanceof List) {
      return unwrapJsonArray((List<?>) o);
    } else {
      return o;
    }
  }
}