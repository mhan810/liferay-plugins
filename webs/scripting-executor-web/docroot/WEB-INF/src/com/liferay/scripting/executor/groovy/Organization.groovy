/*
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

package com.liferay.scripting.executor.groovy

import com.liferay.portal.kernel.util.StringPool
import com.liferay.portal.kernel.util.Validator
import com.liferay.portal.model.ListTypeConstants
import com.liferay.portal.model.OrganizationConstants
import com.liferay.portal.service.OrganizationLocalServiceUtil
import com.liferay.portal.service.ServiceContext

/**
 * @author Michael C. Han
 */
class Organization {

    Organization(String organizationName) {
        name = organizationName;
    }

    Organization(String organizationName, String parentOrgName) {
        name = organizationName;
        parentOrganizationName = parentOrgName;
    }

    void create(ScriptingContext scriptingContext) {

        try {
            liferayOrganization = OrganizationLocalServiceUtil.getOrganization(
                    scriptingContext.companyId, name);
        } catch (e) {
            liferayOrganization = null;
        }

        if (liferayOrganization != null) {
            return;
        }

        def parentOrganizationId =
            OrganizationConstants.DEFAULT_PARENT_ORGANIZATION_ID;

        if (Validator.isNotNull(parentOrganizationName)) {
            def parentOrganization =
                OrganizationLocalServiceUtil.getOrganization(
                        scriptingContext.companyId, parentOrganizationName);

            if (parentOrganization != null) {
                parentOrganizationId = parentOrganization.getOrganizationId();
            }
        }

        liferayOrganization = OrganizationLocalServiceUtil.addOrganization(
                scriptingContext.defaultUserId, (long) parentOrganizationId,
                name,
                OrganizationConstants.TYPE_REGULAR_ORGANIZATION, false, 0, 0,
                ListTypeConstants.ORGANIZATION_STATUS_DEFAULT, StringPool.BLANK,
                false, scriptingContext.serviceContext);
    }

    com.liferay.portal.model.Organization liferayOrganization;
    String name;
    String parentOrganizationName;

}
