<%--
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
--%>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<%@ page import="com.liferay.portal.kernel.resiliency.mpi.MPIHelperUtil" %><%@
page import="com.liferay.portal.kernel.resiliency.spi.SPI" %><%@
page import="com.liferay.portal.kernel.resiliency.spi.agent.SPIAgentFactoryUtil" %><%@
page import="com.liferay.portal.kernel.resiliency.spi.provider.SPIProvider" %><%@
page import="com.liferay.portal.kernel.util.PropsKeys" %><%@
page import="com.liferay.portal.kernel.util.SystemProperties" %><%@
page import="com.liferay.portal.kernel.util.Validator" %><%@
page import="com.liferay.portal.model.Portlet" %><%@
page import="com.liferay.portal.resiliency.console.portlet.PortletUtil" %>

<%@ page import="java.util.List" %>

<portlet:defineObjects />