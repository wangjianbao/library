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
package org.beangle.security.blueprint;

import java.security.Principal;

import org.beangle.commons.entity.Entity;
import org.beangle.commons.entity.TemporalEntity;

/**
 * 权限
 * 
 * @author chaostone 2005-9-26
 */
public interface Permission extends Entity<Integer>, Cloneable, TemporalEntity {
  /**
   * 系统资源
   */
  Resource getResource();

  /**
   * 获得授权对象
   */
  Principal getPrincipal();

  /**
   * 授权的操作
   */
  String getActions();

  /**
   * 访问资源时执行的检查条件
   */
  String getRestrictions();

  /**
   * 说明
   */
  String getRemark();
}
