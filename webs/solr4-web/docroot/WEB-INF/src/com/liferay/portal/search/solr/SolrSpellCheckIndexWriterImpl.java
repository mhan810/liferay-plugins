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

package com.liferay.portal.search.solr;

import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.kernel.search.SpellCheckIndexWriter;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.search.solr.spell.ScopedIndexWriter;

import java.util.Set;

/**
 * @author Michael C. Han
 */
public class SolrSpellCheckIndexWriterImpl implements SpellCheckIndexWriter {

	public void indexDictionaries(SearchContext searchContext)
		throws SearchException {

		_scopedIndexWriter.deleteDocuments();

		for (String supportedLocale : _supportedLocales) {
			searchContext.setLocale(LocaleUtil.fromLanguageId(supportedLocale));

			indexDictionary(searchContext);
		}
	}

	public void indexDictionary(SearchContext searchContext)
		throws SearchException {

		_scopedIndexWriter.indexDictionary(searchContext);
	}

	public void setScopedIndexWriter(ScopedIndexWriter scopedIndexWriter) {
		_scopedIndexWriter = scopedIndexWriter;
	}

	public void setSupportedLocales(Set<String> supportedLocales) {
		_supportedLocales = supportedLocales;
	}

	private ScopedIndexWriter _scopedIndexWriter;
	private Set<String> _supportedLocales;

}