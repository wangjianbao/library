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
package org.beangle.security.blueprint.nav.model;

import java.util.List;
import java.util.Set;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.beangle.commons.collection.CollectUtils;
import org.beangle.commons.entity.pojo.HierarchyLongIdObject;
import org.beangle.commons.lang.Strings;
import org.beangle.security.blueprint.function.FuncResource;
import org.beangle.security.blueprint.nav.Menu;
import org.beangle.security.blueprint.nav.MenuProfile;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * 系统菜单
 * 
 * @author chaostone
 */
@Entity(name = "org.beangle.security.blueprint.nav.Menu")
@Cacheable
@Cache(region = "beangle", usage = CacheConcurrencyStrategy.READ_WRITE)
public class MenuBean extends HierarchyLongIdObject<Menu> implements Menu{

  private static final long serialVersionUID = 3864556621041443066L;

  /** 菜单名称 */
  @NotNull
  @Size(max = 100)
  private String name;

  /** 菜单标题 */
  @NotNull
  @Size(max = 100)
  private String title;

  /** 菜单入口 */
  private String entry;

  /** 菜单备注 */
  private String remark;

  /** 引用资源集合 */
  @ManyToMany
  @Cache(region = "beangle", usage = CacheConcurrencyStrategy.READ_WRITE)
  private Set<FuncResource> resources = CollectUtils.newHashSet();

  /** 是否启用 */
  @NotNull
  private boolean enabled = true;

  /** 菜单配置 */
  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  private MenuProfile profile;

  /** 父级菜单 */
  @ManyToOne(fetch = FetchType.LAZY)
  private Menu parent;

  /** 直接下级菜单 */
  @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
  @Cache(region = "beangle", usage = CacheConcurrencyStrategy.READ_WRITE)
  @OrderBy("code")
  private List<Menu> children;

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEntry() {
    return entry;
  }

  public void setEntry(String entry) {
    this.entry = entry;
  }

  public String getRemark() {
    return remark;
  }

  public void setRemark(String remark) {
    this.remark = remark;
  }

  public Set<FuncResource> getResources() {
    return resources;
  }

  public void setResources(Set<FuncResource> resources) {
    this.resources = resources;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public MenuProfile getProfile() {
    return profile;
  }

  public void setProfile(MenuProfile profile) {
    this.profile = profile;
  }

  public Menu getParent() {
    return parent;
  }

  public void setParent(Menu parent) {
    this.parent = parent;
  }

  public List<Menu> getChildren() {
    return children;
  }

  public void setChildren(List<Menu> children) {
    this.children = children;
  }

  public String getDescription() {
    return Strings.concat("[", code, "]", title);
  }

  @Override
  public String toString() {
    return getDescription();
  }

}
