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
package org.beangle.commons.entity.pojo;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.beangle.commons.entity.pojo.LongIdEntity;
import org.beangle.commons.entity.util.ValidEntityKeyPredicate;

@MappedSuperclass
public class LongIdObject implements LongIdEntity {
  private static final long serialVersionUID = -7530111699332363124L;

  /** 非业务主键 */
  @Id
  @GeneratedValue(generator = "table_sequence")
  protected Long id;

  public LongIdObject() {
    super();
  }

  public LongIdObject(Long id) {
    super();
    this.id = id;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getIdentifier() {
    return id;
  }

  public boolean isPersisted() {
    return ValidEntityKeyPredicate.INSTANCE.evaluate(id);
  }

  public boolean isTransient() {
    return !ValidEntityKeyPredicate.INSTANCE.evaluate(id);
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return new HashCodeBuilder(-64900959, -454788261).append(this.id).toHashCode();
  }

  /**
   * <p>
   * 比较id,如果任一方id是null,则不相等
   * </p>
   * 由于业务对象被CGlib或者javassist增强的原因，这里只提供一般的基于id的比较,不提供基于Class的比较。<br>
   * 如果在存在继承结构， 请重置equals方法。
   * 
   * @see java.lang.Object#equals(Object)
   */
  public boolean equals(final Object object) {
    if (this == object) return true;
    if (!(object instanceof LongIdObject)) { return false; }
    LongIdObject rhs = (LongIdObject) object;
    if (null == getId() || null == rhs.getId()) { return false; }
    return getId().equals(rhs.getId());
  }
}
