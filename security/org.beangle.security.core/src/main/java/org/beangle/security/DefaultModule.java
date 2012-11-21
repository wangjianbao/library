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
package org.beangle.security;

import org.beangle.commons.context.inject.AbstractBindModule;
import org.beangle.security.auth.dao.DaoAuthenticationProvider;
import org.beangle.security.auth.encoding.DigestPasswordEncoder;
import org.beangle.security.auth.encoding.PlaintextPasswordEncoder;
import org.beangle.security.core.session.category.DbSessionController;
import org.beangle.security.core.session.impl.DbSessionRegistry;
import org.beangle.security.core.session.impl.MemSessionRegistry;

/**
 * @author chaostone
 * @version $Id: DefaultModule.java Jun 17, 2011 7:56:25 PM chaostone $
 */
public class DefaultModule extends AbstractBindModule {

  @Override
  protected void doBinding() {
    // session control bean
    bind(DbSessionRegistry.class, DigestPasswordEncoder.class).primary();
    bind(MemSessionRegistry.class, DbSessionController.class).shortName();

    bind(DaoAuthenticationProvider.class).shortName();
    bind(PlaintextPasswordEncoder.class);
  }
}
