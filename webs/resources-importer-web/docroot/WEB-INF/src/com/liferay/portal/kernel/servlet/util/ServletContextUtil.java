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

package com.liferay.portal.kernel.servlet.util;

import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;

/**
 * @author Paul Shemansky
 */
public class ServletContextUtil {

	/**
	 * @param servletContext
	 *            - The ServletContext you want to iterate through.
	 * @param resourceList
	 *            - An empty List which will be populated with paths, during
	 *            traversal; this will include both directories and files, in
	 *            unsorted order.
	 * @param path
	 *            - The root path to start at within the servlet resources
	 *            hierarchy.
	 * @param useRelativePath
	 *            - whether or not to return full paths from root "/" in each of
	 *            the returned resource entries.
	 */
	public static void listAllResourcePaths(
		ServletContext servletContext, List<String> resourceList, String path,
		boolean useRelativePath) {

		_listAllResources(
			servletContext, null, resourceList, path, path, useRelativePath);
	}

	/**
	 * @param servletContext
	 *            - The ServletContext you want to iterate through.
	 * @param resourceDirectoryPaths
	 *            - An empty List which will be populated with paths of
	 *            directories within the context, during traversal; this will
	 *            include only directories, in unsorted order.
	 * @param resourceFilePaths
	 *            - An empty List which will be populated with paths of files
	 *            within the context, during traversal; this will include only
	 *            file entries, in unsorted order.
	 * @param path
	 *            - The root path to start at within the servlet resources
	 *            hierarchy.
	 * @param useRelativePath
	 *            - whether or not to return full paths from root "/" in each of
	 *            the returned resource entries.
	 */
	public static void listDirectoryAndResourcePaths(
		ServletContext servletContext, List<String> resourceDirectoryPaths,
		List<String> resourceFilePaths, String path, boolean useRelativePath) {

		_listAllResources(
			servletContext, resourceDirectoryPaths, resourceFilePaths, path,
			path, useRelativePath);
	}

	private static List<String> _listAllResources(
		ServletContext servletContext, List<String> directoryList,
		List<String> resourceList, String rootPath, String currentPath,
		boolean useRelativePath) {

		Set<String> childPaths = servletContext.getResourcePaths(currentPath);

		for (String childPath : childPaths) {
			String entryName = null;

			if (useRelativePath) {
				entryName = childPath.substring(rootPath.length() - 1);
			}
			else {
				entryName = childPath;
			}

			if (childPath.endsWith("/")) {
				if (directoryList != null) {
					directoryList.add(entryName);
				}
				else {
					resourceList.add(entryName);
				}

				_listAllResources(
					servletContext, directoryList, resourceList, rootPath,
					childPath, useRelativePath);
			}
			else {
				resourceList.add(entryName);
			}
		}

		return resourceList;
	}
}