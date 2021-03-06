/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
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
package org.beangle.commons.text.i18n.impl;

import java.util.Locale;
import java.util.Map;

import org.beangle.commons.collection.CollectUtils;
import org.beangle.commons.text.i18n.TextCache;

public class FlatTextCache implements TextCache {

  private Map<Locale, Map<Class<?>, Map<String, String>>> cache = CollectUtils.newHashMap();

  public void put(Locale locale, Class<?> clazz, String key, String value) {
    Map<Class<?>, Map<String, String>> classCache = cache.get(locale);
    if (null == classCache) {
      classCache = CollectUtils.newConcurrentHashMap();
      cache.put(locale, classCache);
    }
    Map<String, String> bundle = classCache.get(clazz);
    if (null == bundle) {
      bundle = CollectUtils.newConcurrentHashMap();
      classCache.put(clazz, bundle);
    }
    bundle.put(key, value);
  }

  public String get(Locale locale, Class<?> clazz, String key) {
    Map<Class<?>, Map<String, String>> classCache = cache.get(locale);
    if (null == classCache) {
      classCache = CollectUtils.newConcurrentHashMap();
      cache.put(locale, classCache);
    }
    Map<String, String> bundle = classCache.get(clazz);
    return null == bundle ? null : bundle.get(key);
  }

}
