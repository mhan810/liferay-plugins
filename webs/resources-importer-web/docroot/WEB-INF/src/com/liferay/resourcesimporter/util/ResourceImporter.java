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

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.servlet.util.ServletContextUtil;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portlet.documentlibrary.model.DLFolderConstants;

import java.io.InputStream;

import java.net.URL;
import java.net.URLConnection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Raymond Augé
 * @author Ryan Park
 * @author Paul Shemansky
 */
public class ResourceImporter extends FileSystemImporter {

	@Override
	public void importResources() throws Exception {
		doImportResources();
	}

	@Override
	protected void addDDMStructures(
			String parentStructureId, String structuresDirName)
		throws Exception {

		Set<String> resourcePaths = servletContext.getResourcePaths(
			resourcesDir.concat(structuresDirName));

		if (resourcePaths == null) {
			return;
		}

		for (String resourcePath : resourcePaths) {
			if (resourcePath.endsWith(StringPool.SLASH)) {
				continue;
			}

			String name = FileUtil.getShortFileName(resourcePath);

			URL url = servletContext.getResource(resourcePath);

			URLConnection urlConnection = url.openConnection();

			doAddDDMStructures(
				parentStructureId, name, urlConnection.getInputStream());
		}
	}

	@Override
	protected void addDDMTemplates(
			String ddmStructureKey, String templatesDirName)
		throws Exception {

		Set<String> resourcePaths = servletContext.getResourcePaths(
			resourcesDir.concat(templatesDirName));

		if (resourcePaths == null) {
			return;
		}

		for (String resourcePath : resourcePaths) {
			if (resourcePath.endsWith(StringPool.SLASH)) {
				continue;
			}

			String name = FileUtil.getShortFileName(resourcePath);

			URL url = servletContext.getResource(resourcePath);

			URLConnection urlConnection = url.openConnection();

			doAddDDMTemplates(
				ddmStructureKey, name, urlConnection.getInputStream());
		}
	}

	@Override
	protected void addDLFileEntries(String fileEntriesDirName)
		throws Exception {

		String documentLibraryResourcePath =
			resourcesDir.substring(0, resourcesDir.lastIndexOf("/")).concat(
				fileEntriesDirName);

		List<String> directoryPaths = new ArrayList<String>();
		List<String> resourcePaths = new ArrayList<String>();
		ServletContextUtil.listDirectoryAndResourcePaths(
			servletContext, directoryPaths, resourcePaths,
			documentLibraryResourcePath, true);

		Collections.sort(directoryPaths);

		for (String directory : directoryPaths) {
			createDirectory(directory);
		}

		Collections.sort(resourcePaths);

		for (String resourcePath : resourcePaths) {
			String path = FileUtil.getPath(resourcePath);

			Long parentFolderId = null;

			if (Validator.isNull(path)) {
				parentFolderId = DLFolderConstants.DEFAULT_PARENT_FOLDER_ID;
			}
			else {
				parentFolderId = _folderIds.get(path + "/");

				if (parentFolderId == null) {
					throw new Exception(
						"No Folder Created For Resource Path :" + path);
				}
			}

			String fileName = resourcePath.substring(
				resourcePath.lastIndexOf("/") + 1);

			String fullResourcePath =
				documentLibraryResourcePath + "/" + resourcePath;

			InputStream fileInputStream = servletContext.getResourceAsStream(
				fullResourcePath);

			if (fileInputStream == null) {
				throw new Exception("ResourceNotFoundException:" +
					fullResourcePath);
			}

			doAddDLFileEntry(parentFolderId, fileName, fileInputStream);
		}
	}

	@Override
	protected void addJournalArticles(
			String ddmStructureKey, String ddmTemplateKey,
			String articlesDirName)
		throws Exception {

		Set<String> resourcePaths = servletContext.getResourcePaths(
			resourcesDir.concat(articlesDirName));

		if (resourcePaths == null) {
			return;
		}

		for (String resourcePath : resourcePaths) {
			if (resourcePath.endsWith(StringPool.SLASH)) {
				continue;
			}

			String name = FileUtil.getShortFileName(resourcePath);

			URL url = servletContext.getResource(resourcePath);

			URLConnection urlConnection = url.openConnection();

			doAddJournalArticles(
				ddmStructureKey, ddmTemplateKey, name,
				urlConnection.getInputStream());
		}
	}

	protected void createDirectory(String fullPath)
		throws PortalException, SystemException {

		String actualPath = fullPath.substring(0, fullPath.lastIndexOf("/"));

		int parentFolderEndIndex = actualPath.lastIndexOf("/");

		String parentDirectory = actualPath.substring(
			0, parentFolderEndIndex + 1);

		Long parentFolderId = DLFolderConstants.DEFAULT_PARENT_FOLDER_ID;

		if (parentDirectory != null) {
			parentFolderId = _folderIds.get(parentDirectory);

			if (parentFolderId == null) {
				parentFolderId = DLFolderConstants.DEFAULT_PARENT_FOLDER_ID;
			}
		}

		String directory = actualPath.substring(parentFolderEndIndex + 1);

		long dlFolderId = addDLFolder(directory, parentFolderId);

		_folderIds.put(fullPath, dlFolderId);
	}

	@Override
	protected InputStream getInputStream(String fileName) throws Exception {
		URL url = servletContext.getResource(resourcesDir.concat(fileName));

		if (url == null) {
			return null;
		}

		URLConnection urlConnection = url.openConnection();

		return urlConnection.getInputStream();
	}

	private Map<String, Long> _folderIds = new HashMap<String, Long>();

}