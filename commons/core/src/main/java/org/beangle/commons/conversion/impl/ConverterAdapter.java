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
package org.beangle.commons.conversion.impl;

import org.beangle.commons.conversion.Converter;
import org.beangle.commons.lang.tuple.Pair;

/**
 * Adapte a Converter to GenericConverter
 * 
 * @author chaostone
 * @since 3.2.0
 */
public class ConverterAdapter implements GenericConverter {

  final Converter<Object, Object> converter;

  final Pair<Class<?>, Class<?>> typeinfo;

  @SuppressWarnings("unchecked")
  public ConverterAdapter(Converter<?, ?> converter, Pair<Class<?>, Class<?>> typeinfo) {
    super();
    this.converter = (Converter<Object, Object>) converter;
    this.typeinfo = typeinfo;
  }

  @Override
  public Object convert(Object input, Class<?> sourceType, Class<?> targetType) {
    return converter.apply(input);
  }

  @Override
  public Pair<Class<?>, Class<?>> getTypeinfo() {
    return typeinfo;
  }

}
