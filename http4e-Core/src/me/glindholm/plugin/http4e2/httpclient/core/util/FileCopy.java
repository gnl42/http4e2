/*
 *  Copyright 2017 Eclipse HttpClient (http4e) https://nextinterfaces.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package me.glindholm.plugin.http4e2.httpclient.core.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * @author Atanas Roussev (https://nextinterfaces.com)
 */
public class FileCopy {

	public static void copy(final String from, final String to, final List<String> excludeList) throws IOException {
		final File src = new File(from);

		if (excludeList != null && excludeList.contains(src.getName())) {
			return;
		}
		// System.out.println("-- Copying: " + from + " to: " + to);

		if (!src.exists()) {
			throw new RuntimeException("File doesn't exist '" + from + "'");
		}
		if (src.isDirectory()) {
			final File dirCreate = new File(to);
			if (!dirCreate.exists()) {
				dirCreate.mkdirs();
			}
			final File[] srcFiles = src.listFiles();
			for (final File srcF : srcFiles) {
				if (srcF.isDirectory()) {
					FileCopy.copy(srcF.getPath(), to, excludeList);
				} else {
					final File fileCreate = new File(to + File.separator + srcF.getName());
					FileCopy.copyFile(srcF, fileCreate);
				}
			}

		} else {
			final File fileCreate = new File(to);
			fileCreate.getParentFile().mkdirs();
			FileCopy.copyFile(src, fileCreate);
		}
	}

	public static void copyFile(final File src, final File dist) throws IOException {
		Files.copy(src.toPath(), dist.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}

}
