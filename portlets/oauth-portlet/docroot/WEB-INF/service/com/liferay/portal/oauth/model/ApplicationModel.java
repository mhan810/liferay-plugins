/**
 * Copyright (c) 2000-2012 Liferay, Inc. All rights reserved.
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

package com.liferay.portal.oauth.model;

import com.liferay.portal.kernel.bean.AutoEscape;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.AuditedModel;
import com.liferay.portal.model.BaseModel;
import com.liferay.portal.model.CacheModel;
import com.liferay.portal.service.ServiceContext;

import com.liferay.portlet.expando.model.ExpandoBridge;

import java.io.Serializable;

import java.util.Date;

/**
 * The base model interface for the Application service. Represents a row in the &quot;OAuth_Application&quot; database table, with each column mapped to a property of this class.
 *
 * <p>
 * This interface and its corresponding implementation {@link com.liferay.portal.oauth.model.impl.ApplicationModelImpl} exist only as a container for the default property accessors generated by ServiceBuilder. Helper methods and all application logic should be put in {@link com.liferay.portal.oauth.model.impl.ApplicationImpl}.
 * </p>
 *
 * @author Brian Wing Shun Chan
 * @see Application
 * @see com.liferay.portal.oauth.model.impl.ApplicationImpl
 * @see com.liferay.portal.oauth.model.impl.ApplicationModelImpl
 * @generated
 */
public interface ApplicationModel extends AuditedModel, BaseModel<Application> {
	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify or reference this interface directly. All methods that expect a application model instance should use the {@link Application} interface instead.
	 */

	/**
	 * Returns the primary key of this application.
	 *
	 * @return the primary key of this application
	 */
	public long getPrimaryKey();

	/**
	 * Sets the primary key of this application.
	 *
	 * @param primaryKey the primary key of this application
	 */
	public void setPrimaryKey(long primaryKey);

	/**
	 * Returns the application ID of this application.
	 *
	 * @return the application ID of this application
	 */
	public long getApplicationId();

	/**
	 * Sets the application ID of this application.
	 *
	 * @param applicationId the application ID of this application
	 */
	public void setApplicationId(long applicationId);

	/**
	 * Returns the company ID of this application.
	 *
	 * @return the company ID of this application
	 */
	public long getCompanyId();

	/**
	 * Sets the company ID of this application.
	 *
	 * @param companyId the company ID of this application
	 */
	public void setCompanyId(long companyId);

	/**
	 * Returns the user ID of this application.
	 *
	 * @return the user ID of this application
	 */
	public long getUserId();

	/**
	 * Sets the user ID of this application.
	 *
	 * @param userId the user ID of this application
	 */
	public void setUserId(long userId);

	/**
	 * Returns the user uuid of this application.
	 *
	 * @return the user uuid of this application
	 * @throws SystemException if a system exception occurred
	 */
	public String getUserUuid() throws SystemException;

	/**
	 * Sets the user uuid of this application.
	 *
	 * @param userUuid the user uuid of this application
	 */
	public void setUserUuid(String userUuid);

	/**
	 * Returns the user name of this application.
	 *
	 * @return the user name of this application
	 */
	@AutoEscape
	public String getUserName();

	/**
	 * Sets the user name of this application.
	 *
	 * @param userName the user name of this application
	 */
	public void setUserName(String userName);

	/**
	 * Returns the create date of this application.
	 *
	 * @return the create date of this application
	 */
	public Date getCreateDate();

	/**
	 * Sets the create date of this application.
	 *
	 * @param createDate the create date of this application
	 */
	public void setCreateDate(Date createDate);

	/**
	 * Returns the modified date of this application.
	 *
	 * @return the modified date of this application
	 */
	public Date getModifiedDate();

	/**
	 * Sets the modified date of this application.
	 *
	 * @param modifiedDate the modified date of this application
	 */
	public void setModifiedDate(Date modifiedDate);

	/**
	 * Returns the name of this application.
	 *
	 * @return the name of this application
	 */
	@AutoEscape
	public String getName();

	/**
	 * Sets the name of this application.
	 *
	 * @param name the name of this application
	 */
	public void setName(String name);

	/**
	 * Returns the description of this application.
	 *
	 * @return the description of this application
	 */
	@AutoEscape
	public String getDescription();

	/**
	 * Sets the description of this application.
	 *
	 * @param description the description of this application
	 */
	public void setDescription(String description);

	/**
	 * Returns the website of this application.
	 *
	 * @return the website of this application
	 */
	@AutoEscape
	public String getWebsite();

	/**
	 * Sets the website of this application.
	 *
	 * @param website the website of this application
	 */
	public void setWebsite(String website);

	/**
	 * Returns the call back u r l of this application.
	 *
	 * @return the call back u r l of this application
	 */
	@AutoEscape
	public String getCallBackURL();

	/**
	 * Sets the call back u r l of this application.
	 *
	 * @param callBackURL the call back u r l of this application
	 */
	public void setCallBackURL(String callBackURL);

	/**
	 * Returns the access level of this application.
	 *
	 * @return the access level of this application
	 */
	public int getAccessLevel();

	/**
	 * Sets the access level of this application.
	 *
	 * @param accessLevel the access level of this application
	 */
	public void setAccessLevel(int accessLevel);

	/**
	 * Returns the consumer key of this application.
	 *
	 * @return the consumer key of this application
	 */
	@AutoEscape
	public String getConsumerKey();

	/**
	 * Sets the consumer key of this application.
	 *
	 * @param consumerKey the consumer key of this application
	 */
	public void setConsumerKey(String consumerKey);

	/**
	 * Returns the consumer secret of this application.
	 *
	 * @return the consumer secret of this application
	 */
	@AutoEscape
	public String getConsumerSecret();

	/**
	 * Sets the consumer secret of this application.
	 *
	 * @param consumerSecret the consumer secret of this application
	 */
	public void setConsumerSecret(String consumerSecret);

	/**
	 * Returns the logo ID of this application.
	 *
	 * @return the logo ID of this application
	 */
	public long getLogoId();

	/**
	 * Sets the logo ID of this application.
	 *
	 * @param logoId the logo ID of this application
	 */
	public void setLogoId(long logoId);

	public boolean isNew();

	public void setNew(boolean n);

	public boolean isCachedModel();

	public void setCachedModel(boolean cachedModel);

	public boolean isEscapedModel();

	public Serializable getPrimaryKeyObj();

	public void setPrimaryKeyObj(Serializable primaryKeyObj);

	public ExpandoBridge getExpandoBridge();

	public void setExpandoBridgeAttributes(ServiceContext serviceContext);

	public Object clone();

	public int compareTo(Application application);

	public int hashCode();

	public CacheModel<Application> toCacheModel();

	public Application toEscapedModel();

	public String toString();

	public String toXmlString();
}