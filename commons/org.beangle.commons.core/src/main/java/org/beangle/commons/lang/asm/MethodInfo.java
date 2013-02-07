/*
 * Beangle, Agile Java/Scala Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2012, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.lang.asm;

import static java.lang.Character.isUpperCase;
import static org.beangle.commons.lang.Strings.substringAfter;
import static org.beangle.commons.lang.Strings.uncapitalize;

import java.lang.reflect.Method;

import org.beangle.commons.lang.tuple.Pair;

/**
 * Method name and return type and parameters type
 * 
 * @author chaostone
 * @since 3.2.0
 */
final class MethodInfo implements Comparable<MethodInfo> {

  final int index;
  final Method method;

  public MethodInfo(int index, Method method) {
    super();
    this.index = index;
    this.method = method;
  }

  /**
   * Return thid method is property read method (0) or write method(1) or none(-1).
   */
  public Pair<Boolean, String> property() {
    String name = method.getName();
    Class<?>[] paramTypes = method.getParameterTypes();
    if (name.length() > 3 && name.startsWith("get") && isUpperCase(name.charAt(3)) && paramTypes.length == 0) {
      return Pair.of(Boolean.TRUE, uncapitalize(substringAfter(name, "get")));
    } else if (name.length() > 2 && name.startsWith("is") && isUpperCase(name.charAt(2))
        && paramTypes.length == 0) {
      return Pair.of(Boolean.TRUE, uncapitalize(substringAfter(name, "is")));
    } else if (name.length() > 3 && name.startsWith("set") && isUpperCase(name.charAt(3))
        && paramTypes.length == 1) { return Pair.of(Boolean.FALSE, uncapitalize(substringAfter(name, "set"))); }
    return null;
  }

  @Override
  public int compareTo(MethodInfo o) {
    return this.index - o.index;
  }

  public boolean matches(Object[] args) {
    final Class<?>[] paramTypes = method.getParameterTypes();
    if (paramTypes.length != args.length) return false;
    for (int i = 0; i < args.length; i++) {
      if (null != args[i] && !paramTypes[i].isInstance(args[i])) return false;
    }
    return true;
  }

  @Override
  public String toString() {
    final Class<?>[] paramTypes = method.getParameterTypes();
    final Class<?> returnType = method.getReturnType();
    StringBuilder sb = new StringBuilder();
    sb.append((null == returnType) ? "void" : returnType.getSimpleName());
    sb.append(' ').append(method.getName());
    if (paramTypes.length == 0) {
      sb.append("()");
    } else {
      sb.append('(');
      for (Class<?> type : paramTypes) {
        sb.append(type.getSimpleName()).append(",");
      }
      sb.deleteCharAt(sb.length() - 1);
      sb.append(')');
    }
    return sb.toString();
  }

}
