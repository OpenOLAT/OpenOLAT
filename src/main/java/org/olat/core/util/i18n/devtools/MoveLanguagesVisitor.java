/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.util.i18n.devtools;

import java.io.File;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.FileVisitor;
import org.olat.core.util.i18n.I18nManager;

/**
 * <P>
 * Initial Date: 19.11.2008 <br>
 * 
 * @author Roman Haag, frentix GmbH, roman.haag@frentix.com
 */
public class MoveLanguagesVisitor implements FileVisitor {
	private static final Logger log = Tracing.createLoggerFor(MoveLanguagesVisitor.class);
	private String basePath;
	private File targetDir;
	private Locale moveLanguage;
	private boolean doMoveNoCopy;

	/**
	 * Packag scope constructor
	 * 
	 * @param basePathConfig The base path to be subtracted from the file name to
	 *          get the classname
	 */
	public MoveLanguagesVisitor(String basePathConfig, String targetPath, Locale moveLocale, boolean doMoveNoCopy) {
		basePath = basePathConfig;
		this.targetDir = new File(targetPath);
		moveLanguage = moveLocale;
		this.doMoveNoCopy = doMoveNoCopy;
	}

	@Override
	public void visit(File file) {
		if (file.isFile()) { // regular file
			String toBeChechedkFilName = file.getName();
			I18nManager i18nMgr = I18nManager.getInstance();
			String computedFileName = i18nMgr.buildI18nFilename(moveLanguage);
			// match?
			if (toBeChechedkFilName.equals(computedFileName)) {
				File parentFile = file.getParentFile();
				String pPath = parentFile.getPath();
				String relTargetPath = "";
				if (!basePath.equals(pPath)) {
					String res = pPath.substring(basePath.length() + 1); 
					relTargetPath = relTargetPath + "/" + res;
				}
				File targetFile = new File(targetDir, relTargetPath);
				if (doMoveNoCopy) {
					FileUtils.moveFileToDir(file, targetFile);
					log.info("moving " + file.toString() + " to " + targetFile.getAbsolutePath());
				} else {
					FileUtils.copyFileToDir(file, targetFile, "move i18n file");
					log.info("copying " + file.toString() + " to " + targetFile.getAbsolutePath());
				}
			}
		}
		// else ignore
	}

}
