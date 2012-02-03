/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.quartz.tests;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PermStress {

  public void stress(int num) {
    List<Loader> loaders = new ArrayList<Loader>();
    String name = PermStress.class.getName();
    byte[] clazz = getBytes(name);

    for (int i = 0; i < num; i++) {
      try {
        Loader loader = new Loader();
        loaders.add(loader);
        loader.defineClass(name, clazz);
      } catch (OutOfMemoryError error) {
        loaders.clear();
        error.printStackTrace();
        break;
      }
    }
  }

  private byte[] getBytes(String name) {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      InputStream in = getClass().getClassLoader().getResourceAsStream(name.replace('.', '/').concat(".class"));
      int b;
      while ((b = in.read()) >= 0) {
        out.write(b);
      }

      return out.toByteArray();
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  private static class Loader extends ClassLoader {
    public Class<?> defineClass(String name, byte[] clazz) {
      return defineClass(name, clazz, 0, clazz.length);
    }
  }

}
