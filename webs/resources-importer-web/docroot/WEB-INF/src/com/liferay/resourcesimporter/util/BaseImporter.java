/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.resourcesimporter.util;

import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.GroupConstants;
import com.liferay.portal.model.Layout;
import com.liferay.portal.model.LayoutConstants;
import com.liferay.portal.model.LayoutSetPrototype;
import com.liferay.portal.model.LayoutTypePortlet;
import com.liferay.portal.model.User;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portal.service.LayoutLocalServiceUtil;
import com.liferay.portal.service.LayoutSetPrototypeLocalServiceUtil;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;

/**
 * @author Brian Wing Shun Chan
 * @author Raymond Augé
 */
public abstract class BaseImporter implements Importer {

	@Override
	public void afterPropertiesSet()
		throws Exception {

		if (companyId == 0) {
			companyId = PortalUtil.getDefaultCompanyId();
		}

		User user = UserLocalServiceUtil.getDefaultUser(companyId);

		userId = user.getUserId();

		Group group = null;

		if (targetClassName.equals(LayoutSetPrototype.class.getName())) {
			LayoutSetPrototype layoutSetPrototype =
				getLayoutSetPrototype(companyId, targetValue);

			if (layoutSetPrototype != null) {
				existing = true;
			}
			else {
				layoutSetPrototype =
					LayoutSetPrototypeLocalServiceUtil.addLayoutSetPrototype(
						userId, companyId, getTargetValueMap(),
						StringPool.BLANK, true, true, new ServiceContext());
			}

			group = layoutSetPrototype.getGroup();

			privateLayout = true;
			targetClassPK = layoutSetPrototype.getLayoutSetPrototypeId();
		}
		else if (targetClassName.equals(Group.class.getName())) {
			if (targetValue.equals(GroupConstants.GUEST)) {
				group =
					GroupLocalServiceUtil.getGroup(
						companyId, GroupConstants.GUEST);

				List<Layout> layouts =
					LayoutLocalServiceUtil.getLayouts(
						group.getGroupId(), false,
						LayoutConstants.DEFAULT_PARENT_LAYOUT_ID, false, 0, 1);

				if (!layouts.isEmpty()) {
					Layout layout = layouts.get(0);

					LayoutTypePortlet layoutTypePortlet =
						(LayoutTypePortlet) layout.getLayoutType();

					List<String> portletIds = layoutTypePortlet.getPortletIds();

					if (portletIds.size() != 2) {
						existing = true;
					}

					for (String portletId : portletIds) {
						if (!portletId.equals("47") && !portletId.equals("58")) {

							existing = true;
						}
					}
				}
			}
			else {
				group =
					GroupLocalServiceUtil.fetchGroup(companyId, targetValue);

				if (group != null && deleteExistingGroupBeforeImport) {
					long existingGroupId = group.getGroupId();
					if (_log.isTraceEnabled()) {
						_log.trace("deleteExistingGroupBeforeImport=" +
							deleteExistingGroupBeforeImport);
						_log.trace("Deleting group :" + existingGroupId);
					}
					GroupLocalServiceUtil.deleteGroup(existingGroupId);
					if (_log.isTraceEnabled()) {
						_log.trace("Deleted group :" + existingGroupId);
					}
					group = null;
				}

				if (group != null) {
					existing = true;
				}
				else {
					group =
						GroupLocalServiceUtil.addGroup(
							userId, GroupConstants.DEFAULT_PARENT_GROUP_ID,
							StringPool.BLANK, 0,
							GroupConstants.DEFAULT_LIVE_GROUP_ID, targetValue,
							StringPool.BLANK, GroupConstants.TYPE_SITE_OPEN,
							null, true, true, new ServiceContext());
				}
			}

			privateLayout = false;
			targetClassPK = group.getGroupId();
		}

		if (group != null) {
			groupId = group.getGroupId();
			if (_log.isTraceEnabled()) {
				_log.trace("Importing to :" + groupId);
			}
		}
	}

	@Override
	public long getGroupId() {

		return groupId;
	}

	@Override
	public long getTargetClassPK() {

		return targetClassPK;
	}

	public Map<Locale, String> getTargetValueMap() {

		Map<Locale, String> targetValueMap = new HashMap<Locale, String>();

		Locale locale = LocaleUtil.getDefault();

		targetValueMap.put(locale, targetValue);

		return targetValueMap;
	}

	@Override
	public boolean isExisting() {

		return existing;
	}

	@Override
	public void setCompanyId(long companyId) {

		this.companyId = companyId;
	}

	@Override
	public void setResourcesDir(String resourcesDir) {

		this.resourcesDir = resourcesDir;
	}

	@Override
	public void setServletContext(ServletContext servletContext) {

		this.servletContext = servletContext;
	}

	@Override
	public void setServletContextName(String servletContextName) {

		this.servletContextName = servletContextName;
	}

	@Override
	public void setTargetClassName(String targetClassName) {

		this.targetClassName = targetClassName;
	}

	@Override
	public void setTargetValue(String targetValue) {

		this.targetValue = targetValue;
	}

	protected LayoutSetPrototype getLayoutSetPrototype(
		long companyId, String name)
		throws Exception {

		Locale locale = LocaleUtil.getDefault();

		List<LayoutSetPrototype> layoutSetPrototypes =
			LayoutSetPrototypeLocalServiceUtil.search(
				companyId, null, QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);

		for (LayoutSetPrototype layoutSetPrototype : layoutSetPrototypes) {
			if (name.equals(layoutSetPrototype.getName(locale))) {
				return layoutSetPrototype;
			}
		}

		return null;
	}

	protected boolean isDeleteExistingGroupBeforeImport() {

		return deleteExistingGroupBeforeImport;
	}

	protected void setDeleteExistingGroupBeforeImport(
		boolean deleteExistingGroupBeforeImport) {

		this.deleteExistingGroupBeforeImport = deleteExistingGroupBeforeImport;
	}

	protected long companyId;
	protected boolean existing;
	protected long groupId;
	protected boolean privateLayout;
	protected String resourcesDir;
	protected ServletContext servletContext;
	protected String servletContextName;
	protected String targetClassName;
	protected long targetClassPK;
	protected String targetValue;
	protected long userId;
	protected boolean deleteExistingGroupBeforeImport = false;
	protected Log _log = LogFactoryUtil.getLog(getClass());
}
