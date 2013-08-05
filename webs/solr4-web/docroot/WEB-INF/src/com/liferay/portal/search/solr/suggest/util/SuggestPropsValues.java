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

package com.liferay.portal.search.solr.suggest.util;

import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.util.portlet.PortletProps;

/**
 * @author Daniela Zapata Riesco
 */
public class SuggestPropsValues {

	public static final String[] SOLR_SPELL_CHECKER_GROUPS = StringUtil.split(PortletProps.get(SuggestPropsKeys.SOLR_SPELL_CHECKER_GROUPS)); public static final String[] SOLR_SPELL_CHECKER_SUPPORTED_LOCALES = StringUtil.split(PortletProps.get(SuggestPropsKeys.SOLR_SPELL_CHECKER_SUPPORTED_LOCALES));

}