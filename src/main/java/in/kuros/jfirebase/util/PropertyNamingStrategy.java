//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package in.kuros.jfirebase.util;

import java.io.Serializable;

public abstract class PropertyNamingStrategy implements Serializable {
  private static final long serialVersionUID = 2L;

  protected PropertyNamingStrategy() {
  }

  public abstract String translate(String input);
}
