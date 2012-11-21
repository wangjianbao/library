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
package org.beangle.commons.orm.hibernate;

import java.sql.Connection;

import javax.sql.DataSource;

import org.beangle.commons.orm.hibernate.internal.SessionHolder;
import org.beangle.commons.orm.hibernate.internal.SessionUtils;
import org.hibernate.*;
import org.hibernate.exception.GenericJDBCException;
import org.hibernate.impl.SessionImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.JdbcTransactionObjectSupport;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.InvalidIsolationLevelException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.ResourceTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * @author chaostone
 * @version $Id: HibernateTransactionManager.java Feb 28, 2012 10:32:50 PM chaostone $
 */
public class HibernateTransactionManager extends AbstractPlatformTransactionManager implements
    ResourceTransactionManager, BeanFactoryAware, InitializingBean {

  private static final long serialVersionUID = 1L;

  private SessionFactory sessionFactory;

  private DataSource dataSource;

  private boolean autodetectDataSource = true;

  private boolean prepareConnection = true;

  private boolean hibernateManagedSession = false;

  private boolean earlyFlushBeforeCommit = false;

  private Object entityInterceptor;

  private SQLExceptionTranslator jdbcExceptionTranslator;

  private SQLExceptionTranslator defaultJdbcExceptionTranslator;

  /**
   * Just needed for entityInterceptorBeanName.
   * 
   * @see #setEntityInterceptorBeanName
   */
  private BeanFactory beanFactory;

  /**
   * Create a new HibernateTransactionManager instance.
   * A SessionFactory has to be set to be able to use it.
   * 
   * @see #setSessionFactory
   */
  public HibernateTransactionManager() {
  }

  /**
   * Create a new HibernateTransactionManager instance.
   * 
   * @param sessionFactory SessionFactory to manage transactions for
   */
  public HibernateTransactionManager(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
    afterPropertiesSet();
  }

  /**
   * Set the SessionFactory that this instance should manage transactions for.
   */
  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  /**
   * Return the SessionFactory that this instance should manage transactions for.
   */
  public SessionFactory getSessionFactory() {
    return this.sessionFactory;
  }

  /**
   * Set the JDBC DataSource that this instance should manage transactions for.
   * The DataSource should match the one used by the Hibernate SessionFactory:
   * for example, you could specify the same JNDI DataSource for both.
   * <p>
   * If the SessionFactory was configured with DataSourceConnectionProvider, i.e. by Spring's
   * SessionFactoryBean with a specified "dataSource", the DataSource will be auto-detected: You can
   * still explictly specify the DataSource, but you don't need to in this case.
   * <p>
   * A transactional JDBC Connection for this DataSource will be provided to application code
   * accessing this DataSource directly via DataSourceUtils or JdbcTemplate. The Connection will be
   * taken from the Hibernate Session.
   * <p>
   * The DataSource specified here should be the target DataSource to manage transactions for, not a
   * TransactionAwareDataSourceProxy. Only data access code may work with
   * TransactionAwareDataSourceProxy, while the transaction manager needs to work on the underlying
   * target DataSource. If there's nevertheless a TransactionAwareDataSourceProxy passed in, it will
   * be unwrapped to extract its target DataSource.
   * 
   * @see #setAutodetectDataSource
   * @see DataSourceConnectionProvider
   * @see SessionFactoryBean#setDataSource
   * @see org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy
   * @see org.springframework.jdbc.datasource.DataSourceUtils
   */
  public void setDataSource(DataSource dataSource) {
    if (dataSource instanceof TransactionAwareDataSourceProxy) {
      // If we got a TransactionAwareDataSourceProxy, we need to perform transactions
      // for its underlying target DataSource, else data access code won't see
      // properly exposed transactions (i.e. transactions for the target DataSource).
      this.dataSource = ((TransactionAwareDataSourceProxy) dataSource).getTargetDataSource();
    } else {
      this.dataSource = dataSource;
    }
  }

  /**
   * Return the JDBC DataSource that this instance manages transactions for.
   */
  public DataSource getDataSource() {
    return this.dataSource;
  }

  /**
   * Set whether to autodetect a JDBC DataSource used by the Hibernate SessionFactory,
   * if set via SessionFactoryBean's <code>setDataSource</code>. Default is "true".
   * <p>
   * Can be turned off to deliberately ignore an available DataSource, in order to not expose
   * Hibernate transactions as JDBC transactions for that DataSource.
   * 
   * @see #setDataSource
   * @see SessionFactoryBean#setDataSource
   */
  public void setAutodetectDataSource(boolean autodetectDataSource) {
    this.autodetectDataSource = autodetectDataSource;
  }

  /**
   * Set whether to prepare the underlying JDBC Connection of a transactional
   * Hibernate Session, that is, whether to apply a transaction-specific
   * isolation level and/or the transaction's read-only flag to the underlying
   * JDBC Connection.
   * <p>
   * Default is "true". If you turn this flag off, the transaction manager will not support
   * per-transaction isolation levels anymore. It will not call
   * <code>Connection.setReadOnly(true)</code> for read-only transactions anymore either. If this
   * flag is turned off, no cleanup of a JDBC Connection is required after a transaction, since no
   * Connection settings will get modified.
   * 
   * @see java.sql.Connection#setTransactionIsolation
   * @see java.sql.Connection#setReadOnly
   */
  public void setPrepareConnection(boolean prepareConnection) {
    this.prepareConnection = prepareConnection;
  }

  /**
   * Set whether to operate on a Hibernate-managed Session instead of a
   * Spring-managed Session, that is, whether to obtain the Session through
   * Hibernate's {@link org.hibernate.SessionFactory#getCurrentSession()} instead of
   * {@link org.hibernate.SessionFactory#openSession()} (with a Spring
   * {@link org.springframework.transaction.support.TransactionSynchronizationManager} check
   * preceding it).
   * <p>
   * Default is "false", i.e. using a Spring-managed Session: taking the current thread-bound
   * Session if available (e.g. in an Open-Session-in-View scenario), creating a new Session for the
   * current transaction otherwise.
   * <p>
   * Switch this flag to "true" in order to enforce use of a Hibernate-managed Session. Note that
   * this requires {@link org.hibernate.SessionFactory#getCurrentSession()} to always return a
   * proper Session when called for a Spring-managed transaction; transaction begin will fail if the
   * <code>getCurrentSession()</code> call fails.
   * <p>
   * This mode will typically be used in combination with a custom Hibernate
   * {@link org.hibernate.context.CurrentSessionContext} implementation that stores Sessions in a
   * place other than Spring's TransactionSynchronizationManager. It may also be used in combination
   * with Spring's Open-Session-in-View support (using Spring's default
   * {@link BeangleSessionContext}), in which case it subtly differs from the Spring-managed Session
   * mode: The pre-bound Session will <i>not</i> receive a <code>clear()</code> call (on rollback)
   * or a <code>disconnect()</code> call (on transaction completion) in such a scenario; this is
   * rather left up to a custom CurrentSessionContext implementation (if desired).
   */
  public void setHibernateManagedSession(boolean hibernateManagedSession) {
    this.hibernateManagedSession = hibernateManagedSession;
  }

  /**
   * Set whether to perform an early flush before proceeding with a commit.
   * <p>
   * Default is "false", performing an implicit flush as part of the actual commit step. Switch this
   * to "true" in order to enforce an explicit early flush right <i>before</i> the actual commit
   * step.
   * <p>
   * An early flush happens before the before-commit synchronization phase, making flushed state
   * visible to <code>beforeCommit</code> callbacks of registered
   * {@link org.springframework.transaction.support.TransactionSynchronization} objects. Such
   * explicit flush behavior is consistent with Spring-driven flushing in a JTA transaction
   * environment, so may also get enforced for consistency with JTA transaction behavior.
   * 
   * @see #prepareForCommit
   */
  public void setEarlyFlushBeforeCommit(boolean earlyFlushBeforeCommit) {
    this.earlyFlushBeforeCommit = earlyFlushBeforeCommit;
  }

  /**
   * Set the bean name of a Hibernate entity interceptor that allows to inspect
   * and change property values before writing to and reading from the database.
   * Will get applied to any new Session created by this transaction manager.
   * <p>
   * Requires the bean factory to be known, to be able to resolve the bean name to an interceptor
   * instance on session creation. Typically used for prototype interceptors, i.e. a new interceptor
   * instance per session.
   * <p>
   * Can also be used for shared interceptor instances, but it is recommended to set the interceptor
   * reference directly in such a scenario.
   * 
   * @param entityInterceptorBeanName the name of the entity interceptor in
   *          the bean factory
   * @see #setBeanFactory
   * @see #setEntityInterceptor
   */
  public void setEntityInterceptorBeanName(String entityInterceptorBeanName) {
    this.entityInterceptor = entityInterceptorBeanName;
  }

  /**
   * Set a Hibernate entity interceptor that allows to inspect and change
   * property values before writing to and reading from the database.
   * Will get applied to any new Session created by this transaction manager.
   * <p>
   * Such an interceptor can either be set at the SessionFactory level, i.e. on SessionFactoryBean,
   * or at the Session level, and HibernateTransactionManager. It's preferable to set it on
   * SessionFactoryBean or HibernateTransactionManager to avoid repeated configuration and guarantee
   * consistent behavior in transactions.
   */
  public void setEntityInterceptor(Interceptor entityInterceptor) {
    this.entityInterceptor = entityInterceptor;
  }

  /**
   * Return the current Hibernate entity interceptor, or <code>null</code> if none.
   * Resolves an entity interceptor bean name via the bean factory,
   * if necessary.
   * 
   * @throws IllegalStateException if bean name specified but no bean factory set
   * @throws BeansException if bean name resolution via the bean factory failed
   * @see #setEntityInterceptor
   * @see #setEntityInterceptorBeanName
   * @see #setBeanFactory
   */
  public Interceptor getEntityInterceptor() throws IllegalStateException, BeansException {
    if (this.entityInterceptor instanceof Interceptor) {
      return (Interceptor) entityInterceptor;
    } else if (this.entityInterceptor instanceof String) {
      if (this.beanFactory == null) { throw new IllegalStateException(
          "Cannot get entity interceptor via bean name if no bean factory set"); }
      String beanName = (String) this.entityInterceptor;
      return this.beanFactory.getBean(beanName, Interceptor.class);
    } else {
      return null;
    }
  }

  /**
   * Set the JDBC exception translator for this transaction manager.
   * <p>
   * Applied to any SQLException root cause of a Hibernate JDBCException that is thrown on flush,
   * overriding Hibernate's default SQLException translation (which is based on Hibernate's Dialect
   * for a specific target database).
   * 
   * @param jdbcExceptionTranslator the exception translator
   * @see java.sql.SQLException
   * @see org.hibernate.JDBCException
   * @see org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator
   * @see org.springframework.jdbc.support.SQLStateSQLExceptionTranslator
   */
  public void setJdbcExceptionTranslator(SQLExceptionTranslator jdbcExceptionTranslator) {
    this.jdbcExceptionTranslator = jdbcExceptionTranslator;
  }

  /**
   * Return the JDBC exception translator for this transaction manager, if any.
   */
  public SQLExceptionTranslator getJdbcExceptionTranslator() {
    return this.jdbcExceptionTranslator;
  }

  /**
   * The bean factory just needs to be known for resolving entity interceptor
   * bean names. It does not need to be set for any other mode of operation.
   * 
   * @see #setEntityInterceptorBeanName
   */
  public void setBeanFactory(BeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

  public void afterPropertiesSet() {
    if (getSessionFactory() == null) { throw new IllegalArgumentException(
        "Property 'sessionFactory' is required"); }
    if (this.entityInterceptor instanceof String && this.beanFactory == null) { throw new IllegalArgumentException(
        "Property 'beanFactory' is required for 'entityInterceptorBeanName'"); }

    // Check for SessionFactory's DataSource.
    if (this.autodetectDataSource && getDataSource() == null) {
      DataSource sfds = SessionUtils.getDataSource(getSessionFactory());
      if (sfds != null) {
        // Use the SessionFactory's DataSource for exposing transactions to JDBC code.
        if (logger.isInfoEnabled()) {
          logger.info("Using DataSource [" + sfds
              + "] of Hibernate SessionFactory for HibernateTransactionManager");
        }
        setDataSource(sfds);
      }
    }
  }

  public Object getResourceFactory() {
    return getSessionFactory();
  }

  @Override
  protected Object doGetTransaction() {
    HibernateTransactionObject txObject = new HibernateTransactionObject();
    txObject.setSavepointAllowed(isNestedTransactionAllowed());

    SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager
        .getResource(getSessionFactory());
    if (sessionHolder != null) {
      if (logger.isDebugEnabled()) {
        logger.debug("Found thread-bound Session [" + SessionUtils.toString(sessionHolder.getSession())
            + "] for Hibernate transaction");
      }
      txObject.setSessionHolder(sessionHolder);
    } else if (this.hibernateManagedSession) {
      try {
        Session session = getSessionFactory().getCurrentSession();
        if (logger.isDebugEnabled()) {
          logger.debug("Found Hibernate-managed Session [" + SessionUtils.toString(session)
              + "] for Spring-managed transaction");
        }
        txObject.setExistingSession(session);
      } catch (HibernateException ex) {
        throw new DataAccessResourceFailureException(
            "Could not obtain Hibernate-managed Session for Spring-managed transaction", ex);
      }
    }
    //add by duantihua since 3.0.1
    else {
      if (SessionUtils.isEnableThreadBinding()) txObject.setSessionHolder(SessionUtils
          .openSession(getSessionFactory()));
    }

    if (getDataSource() != null) {
      ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager
          .getResource(getDataSource());
      txObject.setConnectionHolder(conHolder);
    }

    return txObject;
  }

  @Override
  protected boolean isExistingTransaction(Object transaction) {
    HibernateTransactionObject txObject = (HibernateTransactionObject) transaction;
    return (txObject.hasSpringManagedTransaction() || (this.hibernateManagedSession && txObject
        .hasHibernateManagedTransaction()));
  }

  @Override
  protected void doBegin(Object transaction, TransactionDefinition definition) {
    HibernateTransactionObject txObject = (HibernateTransactionObject) transaction;

    if (txObject.hasConnectionHolder() && !txObject.getConnectionHolder().isSynchronizedWithTransaction()) { throw new IllegalTransactionStateException(
        "Pre-bound JDBC Connection found! HibernateTransactionManager does not support "
            + "running within DataSourceTransactionManager if told to manage the DataSource itself. "
            + "It is recommended to use a single HibernateTransactionManager for all transactions "
            + "on a single DataSource, no matter whether Hibernate or JDBC access."); }

    Session session = null;

    try {
      if (txObject.getSessionHolder() == null || txObject.getSessionHolder().isSynchronizedWithTransaction()) {
        Interceptor entityInterceptor = getEntityInterceptor();
        Session newSession = (entityInterceptor != null ? getSessionFactory().openSession(entityInterceptor)
            : getSessionFactory().openSession());
        if (logger.isDebugEnabled()) {
          logger.debug("Opened new Session [" + SessionUtils.toString(newSession)
              + "] for Hibernate transaction");
        }
        txObject.setSession(newSession);
      }

      session = txObject.getSessionHolder().getSession();

      if (this.prepareConnection && isSameConnectionForEntireSession(session)) {
        // We're allowed to change the transaction settings of the JDBC Connection.
        if (logger.isDebugEnabled()) {
          logger.debug("Preparing JDBC Connection of Hibernate Session [" + SessionUtils.toString(session)
              + "]");
        }
        @SuppressWarnings("deprecation")
        Connection con = session.connection();
        Integer previousIsolationLevel = DataSourceUtils.prepareConnectionForTransaction(con, definition);
        txObject.setPreviousIsolationLevel(previousIsolationLevel);
      } else {
        // Not allowed to change the transaction settings of the JDBC Connection.
        if (definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT) {
          // We should set a specific isolation level but are not allowed to...
          throw new InvalidIsolationLevelException(
              "HibernateTransactionManager is not allowed to support custom isolation levels: "
                  + "make sure that its 'prepareConnection' flag is on (the default) and that the "
                  + "Hibernate connection release mode is set to 'on_close' (BeangleTransactionFactory's default). "
                  + "Make sure that your SessionFactoryBean actually uses BeangleTransactionFactory: Your "
                  + "Hibernate properties should *not* include a 'hibernate.transaction.factory_class' property!");
        }
        if (logger.isDebugEnabled()) {
          logger.debug("Not preparing JDBC Connection of Hibernate Session ["
              + SessionUtils.toString(session) + "]");
        }
      }

      if (definition.isReadOnly() && txObject.isNewSession()) {
        // Just set to NEVER in case of a new Session for this transaction.
        session.setFlushMode(FlushMode.MANUAL);
      }

      if (!definition.isReadOnly() && !txObject.isNewSession()) {
        // We need AUTO or COMMIT for a non-read-only transaction.
        FlushMode flushMode = session.getFlushMode();
        if (flushMode.lessThan(FlushMode.COMMIT)) {
          session.setFlushMode(FlushMode.AUTO);
          txObject.getSessionHolder().setPreviousFlushMode(flushMode);
        }
      }

      Transaction hibTx;

      // Register transaction timeout.
      int timeout = determineTimeout(definition);
      if (timeout != TransactionDefinition.TIMEOUT_DEFAULT) {
        // Use Hibernate's own transaction timeout mechanism on Hibernate 3.1+
        // Applies to all statements, also to inserts, updates and deletes!
        hibTx = session.getTransaction();
        hibTx.setTimeout(timeout);
        hibTx.begin();
      } else {
        // Open a plain Hibernate transaction without specified timeout.
        hibTx = session.beginTransaction();
      }

      // Add the Hibernate transaction to the session holder.
      txObject.getSessionHolder().setTransaction(hibTx);

      // Register the Hibernate Session's JDBC Connection for the DataSource, if set.
      if (getDataSource() != null) {
        @SuppressWarnings("deprecation")
        Connection con = session.connection();
        ConnectionHolder conHolder = new ConnectionHolder(con);
        if (timeout != TransactionDefinition.TIMEOUT_DEFAULT) {
          conHolder.setTimeoutInSeconds(timeout);
        }
        if (logger.isDebugEnabled()) {
          logger.debug("Exposing Hibernate transaction as JDBC transaction [" + con + "]");
        }
        TransactionSynchronizationManager.bindResource(getDataSource(), conHolder);
        txObject.setConnectionHolder(conHolder);
      }

      // Bind the session holder to the thread.
      if (txObject.isNewSessionHolder()) {
        TransactionSynchronizationManager.bindResource(getSessionFactory(), txObject.getSessionHolder());
      }
      txObject.getSessionHolder().setSynchronizedWithTransaction(true);
    }

    catch (Exception ex) {
      if (txObject.isNewSession()) {
        try {
          if (session.getTransaction().isActive()) {
            session.getTransaction().rollback();
          }
        } catch (Throwable ex2) {
          logger.debug("Could not rollback Session after failed transaction begin", ex);
        } finally {
          SessionUtils.closeSession(session);
        }
      }
      throw new CannotCreateTransactionException("Could not open Hibernate Session for transaction", ex);
    }
  }

  @Override
  protected Object doSuspend(Object transaction) {
    HibernateTransactionObject txObject = (HibernateTransactionObject) transaction;
    txObject.setSessionHolder(null);
    SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager
        .unbindResource(getSessionFactory());
    txObject.setConnectionHolder(null);
    ConnectionHolder connectionHolder = null;
    if (getDataSource() != null) {
      connectionHolder = (ConnectionHolder) TransactionSynchronizationManager.unbindResource(getDataSource());
    }
    return new SuspendedResourcesHolder(sessionHolder, connectionHolder);
  }

  @Override
  protected void doResume(Object transaction, Object suspendedResources) {
    SuspendedResourcesHolder resourcesHolder = (SuspendedResourcesHolder) suspendedResources;
    if (TransactionSynchronizationManager.hasResource(getSessionFactory())) {
      // From non-transactional code running in active transaction synchronization
      // -> can be safely removed, will be closed on transaction completion.
      TransactionSynchronizationManager.unbindResource(getSessionFactory());
    }
    TransactionSynchronizationManager.bindResource(getSessionFactory(), resourcesHolder.getSessionHolder());
    if (getDataSource() != null) {
      TransactionSynchronizationManager.bindResource(getDataSource(), resourcesHolder.getConnectionHolder());
    }
  }

  @Override
  protected void prepareForCommit(DefaultTransactionStatus status) {
    if (this.earlyFlushBeforeCommit && status.isNewTransaction()) {
      HibernateTransactionObject txObject = (HibernateTransactionObject) status.getTransaction();
      Session session = txObject.getSessionHolder().getSession();
      if (!session.getFlushMode().lessThan(FlushMode.COMMIT)) {
        logger.debug("Performing an early flush for Hibernate transaction");
        try {
          session.flush();
        } catch (HibernateException ex) {
          throw convertHibernateAccessException(ex);
        } finally {
          session.setFlushMode(FlushMode.MANUAL);
        }
      }
    }
  }

  @Override
  protected void doCommit(DefaultTransactionStatus status) {
    HibernateTransactionObject txObject = (HibernateTransactionObject) status.getTransaction();
    if (status.isDebug()) {
      logger.debug("Committing Hibernate transaction on Session ["
          + SessionUtils.toString(txObject.getSessionHolder().getSession()) + "]");
    }
    try {
      txObject.getSessionHolder().getTransaction().commit();
    } catch (org.hibernate.TransactionException ex) {
      // assumably from commit call to the underlying JDBC connection
      throw new TransactionSystemException("Could not commit Hibernate transaction", ex);
    } catch (HibernateException ex) {
      // assumably failed to flush changes to database
      throw convertHibernateAccessException(ex);
    }
  }

  @Override
  protected void doRollback(DefaultTransactionStatus status) {
    HibernateTransactionObject txObject = (HibernateTransactionObject) status.getTransaction();
    if (status.isDebug()) {
      logger.debug("Rolling back Hibernate transaction on Session ["
          + SessionUtils.toString(txObject.getSessionHolder().getSession()) + "]");
    }
    try {
      txObject.getSessionHolder().getTransaction().rollback();
    } catch (org.hibernate.TransactionException ex) {
      throw new TransactionSystemException("Could not roll back Hibernate transaction", ex);
    } catch (HibernateException ex) {
      // Shouldn't really happen, as a rollback doesn't cause a flush.
      throw convertHibernateAccessException(ex);
    } finally {
      if (!txObject.isNewSession() && !this.hibernateManagedSession) {
        // Clear all pending inserts/updates/deletes in the Session.
        // Necessary for pre-bound Sessions, to avoid inconsistent state.
        txObject.getSessionHolder().getSession().clear();
      }
    }
  }

  @Override
  protected void doSetRollbackOnly(DefaultTransactionStatus status) {
    HibernateTransactionObject txObject = (HibernateTransactionObject) status.getTransaction();
    if (status.isDebug()) {
      logger.debug("Setting Hibernate transaction on Session ["
          + SessionUtils.toString(txObject.getSessionHolder().getSession()) + "] rollback-only");
    }
    txObject.setRollbackOnly();
  }

  @Override
  protected void doCleanupAfterCompletion(Object transaction) {
    HibernateTransactionObject txObject = (HibernateTransactionObject) transaction;

    // Remove the session holder from the thread.
    if (txObject.isNewSessionHolder()) {
      TransactionSynchronizationManager.unbindResource(getSessionFactory());
    }

    // Remove the JDBC connection holder from the thread, if exposed.
    if (getDataSource() != null) {
      TransactionSynchronizationManager.unbindResource(getDataSource());
    }

    Session session = txObject.getSessionHolder().getSession();
    if (this.prepareConnection && session.isConnected() && isSameConnectionForEntireSession(session)) {
      // We're running with connection release mode "on_close": We're able to reset
      // the isolation level and/or read-only flag of the JDBC Connection here.
      // Else, we need to rely on the connection pool to perform proper cleanup.
      try {
        @SuppressWarnings("deprecation")
        Connection con = session.connection();
        DataSourceUtils.resetConnectionAfterTransaction(con, txObject.getPreviousIsolationLevel());
      } catch (HibernateException ex) {
        logger.debug("Could not access JDBC Connection of Hibernate Session", ex);
      }
    }

    if (txObject.isNewSession()) {
      if (logger.isDebugEnabled()) {
        logger.debug("Closing Hibernate Session [" + SessionUtils.toString(session) + "] after transaction");
      }
      SessionUtils.closeSession(session);
    } else {
      if (logger.isDebugEnabled()) {
        logger.debug("Not closing pre-bound Hibernate Session [" + SessionUtils.toString(session)
            + "] after transaction");
      }
      if (txObject.getSessionHolder().getPreviousFlushMode() != null) {
        session.setFlushMode(txObject.getSessionHolder().getPreviousFlushMode());
      }
      if (!this.hibernateManagedSession) {
        session.disconnect();
      }
    }
    txObject.getSessionHolder().clear();
  }

  /**
   * Return whether the given Hibernate Session will always hold the same
   * JDBC Connection. This is used to check whether the transaction manager
   * can safely prepare and clean up the JDBC Connection used for a transaction.
   * <p>
   * Default implementation checks the Session's connection release mode to be "on_close".
   * Unfortunately, this requires casting to SessionImpl, as of Hibernate 3.1. If that cast doesn't
   * work, we'll simply assume we're safe and return <code>true</code>.
   * 
   * @param session the Hibernate Session to check
   * @see org.hibernate.impl.SessionImpl#getConnectionReleaseMode()
   * @see org.hibernate.ConnectionReleaseMode#ON_CLOSE
   */
  protected boolean isSameConnectionForEntireSession(Session session) {
    if (!(session instanceof SessionImpl)) {
      // The best we can do is to assume we're safe.
      return true;
    }
    ConnectionReleaseMode releaseMode = ((SessionImpl) session).getConnectionReleaseMode();
    return ConnectionReleaseMode.ON_CLOSE.equals(releaseMode);
  }

  /**
   * Convert the given HibernateException to an appropriate exception
   * from the <code>org.springframework.dao</code> hierarchy.
   * <p>
   * Will automatically apply a specified SQLExceptionTranslator to a Hibernate JDBCException, else
   * rely on Hibernate's default translation.
   * 
   * @param ex HibernateException that occured
   * @return a corresponding DataAccessException
   * @see #setJdbcExceptionTranslator
   */
  protected DataAccessException convertHibernateAccessException(HibernateException ex) {
    if (getJdbcExceptionTranslator() != null && ex instanceof JDBCException) {
      return convertJdbcAccessException((JDBCException) ex, getJdbcExceptionTranslator());
    } else if (GenericJDBCException.class.equals(ex.getClass())) { return convertJdbcAccessException(
        (GenericJDBCException) ex, getDefaultJdbcExceptionTranslator()); }
    return SessionUtils.convertHibernateAccessException(ex);
  }

  /**
   * Convert the given Hibernate JDBCException to an appropriate exception
   * from the <code>org.springframework.dao</code> hierarchy, using the
   * given SQLExceptionTranslator.
   * 
   * @param ex Hibernate JDBCException that occured
   * @param translator the SQLExceptionTranslator to use
   * @return a corresponding DataAccessException
   */
  protected DataAccessException convertJdbcAccessException(JDBCException ex, SQLExceptionTranslator translator) {
    return translator.translate("Hibernate flushing: " + ex.getMessage(), ex.getSQL(), ex.getSQLException());
  }

  /**
   * Obtain a default SQLExceptionTranslator, lazily creating it if necessary.
   * <p>
   * Creates a default {@link org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator}
   * for the SessionFactory's underlying DataSource.
   */
  protected synchronized SQLExceptionTranslator getDefaultJdbcExceptionTranslator() {
    if (this.defaultJdbcExceptionTranslator == null) {
      this.defaultJdbcExceptionTranslator = new SQLErrorCodeSQLExceptionTranslator(getDataSource());
    }
    return this.defaultJdbcExceptionTranslator;
  }

  /**
   * Hibernate transaction object, representing a SessionHolder.
   * Used as transaction object by HibernateTransactionManager.
   */
  private class HibernateTransactionObject extends JdbcTransactionObjectSupport {

    private SessionHolder sessionHolder;

    private boolean newSessionHolder;

    private boolean newSession;

    public void setSession(Session session) {
      this.sessionHolder = new SessionHolder(session);
      this.newSessionHolder = true;
      this.newSession = true;
    }

    public void setExistingSession(Session session) {
      this.sessionHolder = new SessionHolder(session);
      this.newSessionHolder = true;
      this.newSession = false;
    }

    public void setSessionHolder(SessionHolder sessionHolder) {
      this.sessionHolder = sessionHolder;
      this.newSessionHolder = false;
      this.newSession = false;
    }

    public SessionHolder getSessionHolder() {
      return this.sessionHolder;
    }

    public boolean isNewSessionHolder() {
      return this.newSessionHolder;
    }

    public boolean isNewSession() {
      return this.newSession;
    }

    public boolean hasSpringManagedTransaction() {
      return (this.sessionHolder != null && this.sessionHolder.getTransaction() != null);
    }

    public boolean hasHibernateManagedTransaction() {
      return (this.sessionHolder != null && this.sessionHolder.getSession().getTransaction().isActive());
    }

    public void setRollbackOnly() {
      this.sessionHolder.setRollbackOnly();
      if (hasConnectionHolder()) {
        getConnectionHolder().setRollbackOnly();
      }
    }

    public boolean isRollbackOnly() {
      return this.sessionHolder.isRollbackOnly()
          || (hasConnectionHolder() && getConnectionHolder().isRollbackOnly());
    }

    public void flush() {
      try {
        this.sessionHolder.getSession().flush();
      } catch (HibernateException ex) {
        throw convertHibernateAccessException(ex);
      }
    }
  }

  /**
   * Holder for suspended resources.
   * Used internally by <code>doSuspend</code> and <code>doResume</code>.
   */
  private static class SuspendedResourcesHolder {

    private final SessionHolder sessionHolder;

    private final ConnectionHolder connectionHolder;

    private SuspendedResourcesHolder(SessionHolder sessionHolder, ConnectionHolder conHolder) {
      this.sessionHolder = sessionHolder;
      this.connectionHolder = conHolder;
    }

    private SessionHolder getSessionHolder() {
      return this.sessionHolder;
    }

    private ConnectionHolder getConnectionHolder() {
      return this.connectionHolder;
    }
  }

}
