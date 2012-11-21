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
package org.beangle.security.blueprint.event;

import org.beangle.security.Securities;
import org.beangle.security.blueprint.Role;

/**
 * @author chaostone
 * @version $Id: RoleCreationEvent.java Jul 27, 2011 10:30:24 AM chaostone $
 */
public class RoleCreationEvent extends RoleEvent {
  private static final long serialVersionUID = -60909204679372326L;

  public RoleCreationEvent(Role role) {
    super(role);
    setSubject(Securities.getUsername() + " 创建了 " + getRole().getName());
  }
}
