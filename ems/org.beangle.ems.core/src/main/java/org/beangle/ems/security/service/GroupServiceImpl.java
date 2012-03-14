/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
 */
package org.beangle.ems.security.service;

import java.sql.Date;
import java.util.List;

import org.beangle.collection.CollectUtils;
import org.beangle.dao.Operation;
import org.beangle.dao.impl.AbstractHierarchyService;
import org.beangle.dao.query.builder.OqlBuilder;
import org.beangle.ems.security.Group;
import org.beangle.ems.security.GroupMember;
import org.beangle.ems.security.User;
import org.beangle.ems.security.event.GroupCreationEvent;
import org.beangle.ems.security.model.GroupBean;
import org.beangle.ems.security.model.GroupMemberBean;

/**
 * @author chaostone
 * @version $Id: GroupServiceImpl.java Jul 29, 2011 1:58:51 AM chaostone $
 */
public class GroupServiceImpl extends AbstractHierarchyService<GroupBean, Group> implements GroupService {
	private UserService userService;

	public void createGroup(User owner, Group group) {
		group.setUpdatedAt(new Date(System.currentTimeMillis()));
		group.setCreatedAt(new Date(System.currentTimeMillis()));
		group.setOwner(owner);
		group.getMembers().add(new GroupMemberBean(group, owner, GroupMember.Ship.MANAGER));
		entityDao.saveOrUpdate(group);
		publish(new GroupCreationEvent(group));
	}

	/**
	 * 超级管理员不能删除
	 */
	public void removeGroup(User manager, List<Group> groups) {
		List<Object> saved = CollectUtils.newArrayList();
		List<Object> removed = CollectUtils.newArrayList();

		for (final Group group : groups) {
			if (group.getOwner().equals(manager) || userService.isRoot(manager)) {
				if (null != group.getParent()) {
					group.getParent().getChildren().remove(group);
					saved.add(group.getParent());
				}
				removed.add(group);
			}
		}
		entityDao.execute(Operation.saveOrUpdate(saved).remove(removed));
	}

	public void moveGroup(Group group, Group parent, int indexno) {
		move((GroupBean) group, (GroupBean) parent, indexno);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected List<GroupBean> getTopNodes(GroupBean m) {
		OqlBuilder builder = OqlBuilder.from(Group.class, "g");
		builder.where("g.parent is null");
		return entityDao.search(builder);
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

}