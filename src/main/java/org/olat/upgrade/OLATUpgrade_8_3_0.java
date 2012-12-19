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
package org.olat.upgrade;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.olat.core.util.WebappHelper;

/**
 * Description:<br>
 * upgrade code for OLAT 7.1.0 -> OLAT 7.1.1
 * - fixing invalid structures being built by synchronisation, see OLAT-6316 and OLAT-6306
 * - merges all yet found data to last valid node 
 * 
 * <P>
 * Initial Date: 24.03.2011 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, www.frentix.com
 */
public class OLATUpgrade_8_3_0 extends OLATUpgrade {

	private static final String TASK_MOVE_QTI_EDITOR_TMP = "Move qtieditor tmp";
	private static final String VERSION = "OLAT_8.3.0";


	public OLATUpgrade_8_3_0() {
		super();
	}

	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public boolean doPreSystemInitUpgrade(UpgradeManager upgradeManager) {
		return false;
	}

	@Override
	public boolean doPostSystemInitUpgrade(UpgradeManager upgradeManager) {
		UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
		if (uhd == null) {
			// has never been called, initialize
			uhd = new UpgradeHistoryData();
		} else {
			if (uhd.isInstallationComplete()) {
				return false;
			}
		}
		
		boolean allOk = moveQTIEditorTmp(upgradeManager, uhd);
		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.audit("Finished OLATUpgrade_8_3_0 successfully!");
		} else {
			log.audit("OLATUpgrade_8_3_0 not finished, try to restart OpenOLAT!");
		}
		return allOk;
	}
	
	private boolean moveQTIEditorTmp(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		
		if (!uhd.getBooleanDataValue(TASK_MOVE_QTI_EDITOR_TMP)) {
			try {
				File current = new File(WebappHelper.getUserDataRoot()	+ "/tmp/qtieditor/");
				if(current.exists()) {
					File destDir = new File(WebappHelper.getUserDataRoot()	+ "/qtieditor/");
					destDir.mkdirs();
					FileUtils.copyDirectory(current, destDir);
					allOk = compareRecursive(current, destDir);
					if(allOk) {
						FileUtils.deleteDirectory(current);
					}
				}
				uhd.setBooleanDataValue(TASK_MOVE_QTI_EDITOR_TMP, allOk);
				upgradeManager.setUpgradesHistory(uhd, VERSION);
			} catch (IOException e) {
				log.error("", e);
				return false;
			}
		}
		return allOk;
	}
	
	private boolean compareRecursive(File tmp, File qtieditor) {
		File[] tmpFiles = tmp.listFiles(new SystemFilenameFilter());
		String[] editorFiles = tmp.list(new SystemFilenameFilter());
		
		Set<String> editorFileSet = new HashSet<String>();
		for(String editorFile:editorFiles) {
			editorFileSet.add(editorFile);
		}
		
		for(File tmpFile:tmpFiles) {
			String tmpName = tmpFile.getName();
			if(!editorFileSet.contains(tmpName)) {
				return false;
			}
			if(tmpFile.isDirectory()) {
				compareRecursive(tmpFile, new File(qtieditor, tmpName));
			}
		}
		return true;
	}
	
	private static class SystemFilenameFilter implements FilenameFilter {
		@Override
		public boolean accept(File dir, String name) {
			return name != null && !name.startsWith(".");
		}
	}
}
