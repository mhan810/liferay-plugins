/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
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

package com.liferay.portal.workflow.kaleo.runtime.notification;

import com.liferay.portal.kernel.bi.rules.Fact;
import com.liferay.portal.kernel.bi.rules.Query;
import com.liferay.portal.kernel.bi.rules.RulesEngineUtil;
import com.liferay.portal.kernel.bi.rules.RulesResourceRetriever;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.resource.StringResourceRetriever;
import com.liferay.portal.kernel.scripting.ScriptingUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.RoleConstants;
import com.liferay.portal.model.User;
import com.liferay.portal.model.UserGroupGroupRole;
import com.liferay.portal.model.UserGroupRole;
import com.liferay.portal.service.RoleLocalServiceUtil;
import com.liferay.portal.service.UserGroupGroupRoleLocalServiceUtil;
import com.liferay.portal.service.UserGroupRoleLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.workflow.kaleo.definition.RecipientType;
import com.liferay.portal.workflow.kaleo.model.KaleoInstance;
import com.liferay.portal.workflow.kaleo.model.KaleoInstanceToken;
import com.liferay.portal.workflow.kaleo.model.KaleoNotificationRecipient;
import com.liferay.portal.workflow.kaleo.model.KaleoTaskAssignmentInstance;
import com.liferay.portal.workflow.kaleo.model.KaleoTaskInstanceToken;
import com.liferay.portal.workflow.kaleo.runtime.ExecutionContext;
import com.liferay.portal.workflow.kaleo.runtime.util.ClassLoaderUtil;
import com.liferay.portal.workflow.kaleo.runtime.util.RulesContextBuilder;
import com.liferay.portal.workflow.kaleo.runtime.util.ScriptingContextBuilderUtil;
import com.liferay.portal.workflow.kaleo.util.WorkflowContextUtil;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Michael C. Han
 */
public abstract class BaseNotificationSender implements NotificationSender {

	@Override
	public void sendNotification(
			List<KaleoNotificationRecipient> kaleoNotificationRecipients,
			String defaultSubject, String notificationMessage,
			ExecutionContext executionContext)
		throws NotificationMessageSenderException {

		try {
			Set<NotificationRecipient> notificationRecipients =
				getNotificationRecipients(
					kaleoNotificationRecipients, executionContext);

			if (notificationRecipients.isEmpty()) {
				return;
			}

			doSendNotification(
				notificationRecipients, defaultSubject, notificationMessage,
				executionContext);
		}
		catch (Exception e) {
			throw new NotificationMessageSenderException(
				"Unable to send notification message", e);
		}
	}

	protected void addAssignedRecipients(
			Set<NotificationRecipient> notificationRecipients,
			int emailRecipientType, ExecutionContext executionContext)
		throws Exception {

		KaleoTaskInstanceToken kaleoTaskInstanceToken =
			executionContext.getKaleoTaskInstanceToken();

		if (kaleoTaskInstanceToken == null) {
			return;
		}

		List<KaleoTaskAssignmentInstance> kaleoTaskAssignmentInstances =
			kaleoTaskInstanceToken.getKaleoTaskAssignmentInstances();

		for (KaleoTaskAssignmentInstance kaleoTaskAssignmentInstance :
				kaleoTaskAssignmentInstances) {

			String assigneeClassName =
				kaleoTaskAssignmentInstance.getAssigneeClassName();

			if (assigneeClassName.equals(User.class.getName())) {
				addUserNotificationRecipient(
					notificationRecipients,
					kaleoTaskAssignmentInstance.getAssigneeClassPK(),
					emailRecipientType, executionContext);
			}
			else {
				long roleId = kaleoTaskAssignmentInstance.getAssigneeClassPK();

				Role role = RoleLocalServiceUtil.getRole(roleId);

				addRoleRecipientAddresses(
					notificationRecipients, roleId, role.getType(),
					emailRecipientType, executionContext);
			}
		}
	}

	protected void addRoleRecipientAddresses(
			Set<NotificationRecipient> notificationRecipients, long roleId,
			int roleType, int emailRecipientType,
			ExecutionContext executionContext)
		throws Exception {

		List<User> users = getRoleUsers(roleId, roleType, executionContext);

		for (User user : users) {
			if (user.isActive()) {
				NotificationRecipient notificationRecipient =
					new NotificationRecipient(user, emailRecipientType);

				notificationRecipients.add(notificationRecipient);
			}
		}
	}

	protected void addScriptRecipientAddresses(
			Set<NotificationRecipient> notificationRecipients,
			String recipientScript, String recipientScriptingLanguage,
			String recipientScriptRequiredContextsString,
			int emailRecipientType, ExecutionContext executionContext)
		throws Exception {

		String[] recipientScriptRequiredContexts = StringUtil.split(
			recipientScriptRequiredContextsString);

		ClassLoader[] classLoaders = ClassLoaderUtil.getClassLoaders(
			recipientScriptRequiredContexts);

		if (NotificationConstants.RECIPIENT_SCRIPT_LANGUAGE.
				hasScriptingLanguage(recipientScriptingLanguage)) {

			Map<String, ?> results = null;

			if (NotificationConstants.RECIPIENT_SCRIPT_LANGUAGE.DRL.equals(
					recipientScriptingLanguage)) {

				List<Fact<?>> facts = RulesContextBuilder.buildRulesContext(
					executionContext);

				RulesResourceRetriever rulesResourceRetriever =
					new RulesResourceRetriever(
						new StringResourceRetriever(recipientScript));

				Query query = Query.createStandardQuery();

				results = RulesEngineUtil.execute(
					rulesResourceRetriever, facts, query, classLoaders);
			}
			else {
				Map<String, Object> inputObjects =
					ScriptingContextBuilderUtil.buildScriptingContext(
						executionContext);

				results = ScriptingUtil.eval(
					null, inputObjects, _outputNames,
					recipientScriptingLanguage, recipientScript, classLoaders);
			}

			Map<String, Serializable> resultsWorkflowContext =
				(Map<String, Serializable>)results.get(
					WorkflowContextUtil.WORKFLOW_CONTEXT_NAME);

			WorkflowContextUtil.mergeWorkflowContexts(
				executionContext, resultsWorkflowContext);

			User user = (User)results.get(USER_RECIPIENT);

			if (user != null) {
				if (user.isActive()) {
					NotificationRecipient notificationRecipient =
						new NotificationRecipient(user, emailRecipientType);

					notificationRecipients.add(notificationRecipient);
				}
			}
			else {
				List<Role> roles = (List<Role>)results.get(ROLES_RECIPIENT);

				for (Role role : roles) {
					addRoleRecipientAddresses(
						notificationRecipients, role.getRoleId(),
						role.getType(), emailRecipientType, executionContext);
				}
			}
		}
	}

	protected void addUserNotificationRecipient(
			Set<NotificationRecipient> notificationRecipients, long userId,
			int emailRecipientType, ExecutionContext executionContext)
		throws Exception {

		if (userId <= 0) {
			KaleoInstanceToken kaleoInstanceToken =
				executionContext.getKaleoInstanceToken();

			KaleoInstance kaleoInstance = kaleoInstanceToken.getKaleoInstance();

			userId = kaleoInstance.getUserId();
		}

		User user = UserLocalServiceUtil.getUser(userId);

		if (user.isActive()) {
			NotificationRecipient notificationRecipient =
				new NotificationRecipient(user, emailRecipientType);

			notificationRecipients.add(notificationRecipient);
		}
	}

	protected abstract void doSendNotification(
			Set<NotificationRecipient> notificationRecipients,
			String defaultSubject, String notificationMessage,
			ExecutionContext executionContext)
		throws Exception;

	protected Set<NotificationRecipient> getNotificationRecipients(
			List<KaleoNotificationRecipient> kaleoNotificationRecipients,
			ExecutionContext executionContext)
		throws Exception {

		Set<NotificationRecipient> notificationRecipients = new HashSet<>();

		if (kaleoNotificationRecipients.isEmpty()) {
			addAssignedRecipients(
				notificationRecipients,
				NotificationConstants.EMAIL_RECIPIENT_TYPE.NOT_APPLICABLE.type,
				executionContext);

			return notificationRecipients;
		}

		for (KaleoNotificationRecipient kaleoNotificationRecipient :
				kaleoNotificationRecipients) {

			String recipientClassName =
				kaleoNotificationRecipient.getRecipientClassName();

			if (recipientClassName.equals(RecipientType.ADDRESS.name())) {
				String address = kaleoNotificationRecipient.getAddress();
				int emailRecipientType =
					kaleoNotificationRecipient.getEmailRecipientType();

				NotificationRecipient notificationRecipient =
					new NotificationRecipient(address, emailRecipientType);

				notificationRecipients.add(notificationRecipient);
			}
			else if (recipientClassName.equals(
						RecipientType.ASSIGNEES.name())) {

				addAssignedRecipients(
					notificationRecipients,
					kaleoNotificationRecipient.getEmailRecipientType(),
					executionContext);
			}
			else if (recipientClassName.equals(Role.class.getName())) {
				addRoleRecipientAddresses(
					notificationRecipients,
					kaleoNotificationRecipient.getRecipientClassPK(),
					kaleoNotificationRecipient.getRecipientRoleType(),
					kaleoNotificationRecipient.getEmailRecipientType(),
					executionContext);
			}
			else if (recipientClassName.equals(RecipientType.SCRIPT.name())) {
				addScriptRecipientAddresses(
					notificationRecipients,
					kaleoNotificationRecipient.getRecipientScript(),
					kaleoNotificationRecipient.getRecipientScriptLanguage(),
					kaleoNotificationRecipient.
						getRecipientScriptRequiredContexts(),
					kaleoNotificationRecipient.getEmailRecipientType(),
					executionContext);
			}
			else if (recipientClassName.equals(User.class.getName())) {
				addUserNotificationRecipient(
					notificationRecipients,
					kaleoNotificationRecipient.getRecipientClassPK(),
					kaleoNotificationRecipient.getEmailRecipientType(),
					executionContext);
			}
			else {
				if (_log.isWarnEnabled()) {
					_log.warn(
						"Unsupported recipient " + kaleoNotificationRecipient);
				}
			}
		}

		return notificationRecipients;
	}

	protected List<User> getRoleUsers(
			long roleId, int roleType, ExecutionContext executionContext)
		throws Exception {

		if (roleType == RoleConstants.TYPE_REGULAR) {
			return UserLocalServiceUtil.getInheritedRoleUsers(
				roleId, QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);
		}

		List<User> users = new ArrayList<>();

		KaleoInstanceToken kaleoInstanceToken =
			executionContext.getKaleoInstanceToken();

		List<UserGroupRole> userGroupRoles =
			UserGroupRoleLocalServiceUtil.getUserGroupRolesByGroupAndRole(
				kaleoInstanceToken.getGroupId(), roleId);

		for (UserGroupRole userGroupRole : userGroupRoles) {
			users.add(userGroupRole.getUser());
		}

		List<UserGroupGroupRole> userGroupGroupRoles =
			UserGroupGroupRoleLocalServiceUtil.
				getUserGroupGroupRolesByGroupAndRole(
					kaleoInstanceToken.getGroupId(), roleId);

		for (UserGroupGroupRole userGroupGroupRole : userGroupGroupRoles) {
			users.addAll(
				UserLocalServiceUtil.getUserGroupUsers(
					userGroupGroupRole.getUserGroupId()));
		}

		return users;
	}

	protected static final String ROLES_RECIPIENT = "roles";

	protected static final String USER_RECIPIENT = "user";

	private static Log _log = LogFactoryUtil.getLog(
		BaseNotificationSender.class);

	private static Set<String> _outputNames = new HashSet<>();

	static {
		_outputNames.add(ROLES_RECIPIENT);
		_outputNames.add(USER_RECIPIENT);
		_outputNames.add(WorkflowContextUtil.WORKFLOW_CONTEXT_NAME);
	}

}