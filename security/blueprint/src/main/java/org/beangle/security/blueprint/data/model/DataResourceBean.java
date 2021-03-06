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
package org.beangle.security.blueprint.data.model;

import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.beangle.commons.collection.CollectUtils;
import org.beangle.commons.entity.pojo.IntegerIdObject;
import org.beangle.commons.lang.Objects;
import org.beangle.security.blueprint.data.DataField;
import org.beangle.security.blueprint.data.DataResource;

/**
 * 系统数据资源
 * 
 * @author chaostone
 * @since 2012-07-24
 */
@Entity(name = "org.beangle.security.blueprint.data.DataResource")
@Cacheable
public class DataResourceBean extends IntegerIdObject implements DataResource {
  private static final long serialVersionUID = -8285208615351119572L;

  /** 名称(类型) */
  @Size(max = 100)
  @NotNull
  @Column(unique = true)
  private String name;

  /** 标题 */
  @Size(max = 100)
  @NotNull
  private String title;

  /** 简单描述 */
  @Size(max = 100)
  private String remark;

  /** 允许的操作 */
  @Size(max = 100)
  private String actions;

  /** 能够访问哪些属性 */
  @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL, orphanRemoval = true)
  protected Set<DataField> fields = CollectUtils.newHashSet();

  /** 是否可用 */
  @NotNull
  private boolean enabled = true;

  public DataResourceBean() {
    super();
  }

  public DataResourceBean(Integer id) {
    super(id);
  }

  public DataResourceBean(String name, String title) {
    this.name = name;
    this.title = title;
  }

  public String getRemark() {
    return remark;
  }

  public void setRemark(String remark) {
    this.remark = remark;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getActions() {
    return actions;
  }

  public void setActions(String actions) {
    this.actions = actions;
  }

  public Set<DataField> getFields() {
    return fields;
  }

  public void setFields(Set<DataField> fields) {
    this.fields = fields;
  }

  public String toString() {
    return Objects.toStringBuilder(this).add("name", this.name).add("id", this.id).add("remark", this.remark)
        .toString();
  }
}
