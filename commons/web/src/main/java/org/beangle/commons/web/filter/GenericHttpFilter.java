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
package org.beangle.commons.web.filter;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.beangle.commons.bean.Disposable;
import org.beangle.commons.bean.Initializing;
import org.beangle.commons.bean.PropertyUtils;
import org.beangle.commons.lang.Assert;
import org.beangle.commons.lang.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author chaostone
 * @version $Id: GenericHttpFilter.java Nov 20, 2010 7:12:16 PM chaostone $
 */
public abstract class GenericHttpFilter implements Filter, Initializing, Disposable {

  /** Logger available to subclasses */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /**
   * Set of required properties (Strings) that must be supplied as config
   * parameters to this filter.
   */
  private final Set<String> requiredProperties = new HashSet<String>();

  private FilterConfig filterConfig;

  private ServletContext servletContext;

  /**
   * Calls the <code>initFilterBean()</code> method that might contain custom
   * initialization of a subclass.
   * <p>
   * Only relevant in case of initialization as bean, where the standard
   * <code>init(FilterConfig)</code> method won't be called.
   * 
   * @see #initFilterBean()
   * @see #init(javax.servlet.FilterConfig)
   */
  public void init() throws Exception {
    initFilterBean();
  }

  /**
   * Subclasses can invoke this method to specify that this property (which
   * must match a JavaBean property they expose) is mandatory, and must be
   * supplied as a config parameter. This should be called from the
   * constructor of a subclass.
   * <p>
   * This method is only relevant in case of traditional initialization driven by a FilterConfig
   * instance.
   * 
   * @param property
   *          name of the required property
   */
  protected final void addRequiredProperty(String property) {
    this.requiredProperties.add(property);
  }

  /**
   * Standard way of initializing this filter. Map config parameters onto bean
   * properties of this filter, and invoke subclass initialization.
   * 
   * @param filterConfig
   *          the configuration for this filter
   * @throws ServletException
   *           if bean properties are invalid (or required properties are
   *           missing), or if subclass initialization fails.
   * @see #initFilterBean
   */
  public final void init(FilterConfig filterConfig) throws ServletException {
    Assert.notNull(filterConfig, "FilterConfig must not be null");
    logger.debug("Initializing filter '{}'", filterConfig.getFilterName());

    this.filterConfig = filterConfig;
    initParams(filterConfig);
    // Let subclasses do whatever initialization they like.
    initFilterBean();
    logger.debug("Filter '{}' configured successfully", filterConfig.getFilterName());
  }

  protected void initParams(FilterConfig config) throws ServletException {
    Set<String> missingProps = (requiredProperties != null && !requiredProperties.isEmpty()) ? new HashSet<String>(
        requiredProperties) : null;

    Enumeration<String> en = config.getInitParameterNames();
    while (en.hasMoreElements()) {
      String property = en.nextElement();
      Object value = config.getInitParameter(property);
      try {
        PropertyUtils.setProperty(this, property, value);
      } catch (Exception e) {
        throw new ServletException("Set filter property'" + property + "' failed; the value is: " + value);
      }
      if (missingProps != null) {
        missingProps.remove(property);
      }
    }

    // Fail if we are still missing properties.
    if (missingProps != null && missingProps.size() > 0) { throw new ServletException(
        "Initialization from FilterConfig for filter '" + config.getFilterName()
            + "' failed; the following required properties were missing: " + Strings.join(missingProps, ", ")); }

  }

  /**
   * Make the FilterConfig of this filter available, if any. Analogous to
   * GenericServlet's <code>getServletConfig()</code>.
   * <p>
   * Public to resemble the <code>getFilterConfig()</code> method of the Servlet Filter version that
   * shipped with WebLogic 6.1.
   * 
   * @return the FilterConfig instance, or <code>null</code> if none available
   * @see javax.servlet.GenericServlet#getServletConfig()
   */
  public final FilterConfig getFilterConfig() {
    return this.filterConfig;
  }

  /**
   * Make the name of this filter available to subclasses. Analogous to
   * GenericServlet's <code>getServletName()</code>.
   * <p>
   * Takes the FilterConfig's filter name by default. If initialized as bean in application context,
   * it falls back to the bean name as defined in the bean factory.
   * 
   * @return the filter name, or <code>null</code> if none available
   * @see javax.servlet.GenericServlet#getServletName()
   * @see javax.servlet.FilterConfig#getFilterName()
   */
  protected final String getFilterName() {
    return (this.filterConfig != null ? this.filterConfig.getFilterName() : "None");
  }

  /**
   * Make the ServletContext of this filter available to subclasses. Analogous
   * to GenericServlet's <code>getServletContext()</code>.
   * <p>
   * Takes the FilterConfig's ServletContext by default. If initialized as bean in application
   * context, it falls back to the ServletContext that the bean factory runs in.
   * 
   * @return the ServletContext instance, or <code>null</code> if none
   *         available
   * @see javax.servlet.GenericServlet#getServletContext()
   * @see javax.servlet.FilterConfig#getServletContext()
   */
  protected final ServletContext getServletContext() {
    return (this.filterConfig != null ? this.filterConfig.getServletContext() : this.servletContext);
  }

  /**
   * Subclasses may override this to perform custom initialization. All bean
   * properties of this filter will have been set before this method is
   * invoked.
   * <p>
   * Note: This method will be called from standard filter initialization as well as filter bean
   * initialization in a application context. Filter name and ServletContext will be available in
   * both cases.
   * <p>
   * This default implementation is empty.
   * 
   * @throws ServletException
   *           if subclass initialization fails
   * @see #getFilterName()
   * @see #getServletContext()
   */
  protected void initFilterBean() throws ServletException {
  }

  /**
   * Subclasses may override this to perform custom filter shutdown.
   * <p>
   * Note: This method will be called from standard filter destruction as well as filter bean
   * destruction in a application context.
   * <p>
   * This default implementation is empty.
   */
  public void destroy() {
  }

}
