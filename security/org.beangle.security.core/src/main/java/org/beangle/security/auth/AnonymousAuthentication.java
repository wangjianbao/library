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
package org.beangle.security.auth;

import java.util.Collection;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.beangle.security.core.GrantedAuthority;

public class AnonymousAuthentication extends AbstractAuthentication {

  private static final long serialVersionUID = 3236987468644441586L;

  private Object principal;

  /**
   * Default anonymous instance
   */
  public static AnonymousAuthentication Instance = new AnonymousAuthentication("anonymous", null);

  public AnonymousAuthentication(Object principal, Collection<? extends GrantedAuthority> authorities) {
    super(authorities);
    this.principal = principal;
    setAuthenticated(true);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof AnonymousAuthentication) {
      AbstractAuthentication test = (AbstractAuthentication) obj;
      return new EqualsBuilder().append(getPrincipal(), test.getPrincipal()).isEquals();
    }
    return false;
  }

  public Object getCredentials() {
    return "";
  }

  public Object getPrincipal() {
    return principal;
  }

}
