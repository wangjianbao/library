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
package org.beangle.commons.transfer.io;

/**
 * 数据读取类
 * 
 * @author chaostone
 * @version $Id: $
 */
public interface Reader {

  /**
   * 读取数据
   * 
   * @return a {@link java.lang.Object} object.
   */
  Object read();

  /**
   * 返回读取类型的格式
   * 
   * @return a {@link java.lang.String} object.
   */
  TransferFormat getFormat();

  /**
   * 关闭
   */
  void close();
}
