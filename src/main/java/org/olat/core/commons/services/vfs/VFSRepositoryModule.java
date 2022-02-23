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
package org.olat.core.commons.services.vfs;

import java.io.File;
import java.nio.file.Path;

import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 11 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class VFSRepositoryModule extends AbstractSpringModule {
	
	private static final String MIGRATED_VFS = "vfs.migrated";
	
	private boolean migrated;
	
	@Value("${vfs.largefiles.upperborder}")
	private long upperBorder;
	@Value("${vfs.largefiles.lowerborder}")
	private long lowerBorder;

	@Value("${zip.min.inflate.ratio:0.01}")
	private double zipMinInflateRatio;
	@Value("${zip.max.entries:32000}")
	private int zipMaxEntries;
	
	@Autowired
	public VFSRepositoryModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}
	
	@Override
	public void init() {
		updateProperties();
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}
	
	private void updateProperties() {
		String migratedObj = getStringPropertyValue(MIGRATED_VFS, true);
		if(StringHelper.containsNonWhitespace(migratedObj)) {
			migrated = "true".equals(migratedObj);
		}
	}
	
	public boolean isMigrated() {
		return migrated;
	}

	public void setMigrated(boolean migrated) {
		this.migrated = migrated;
		setStringProperty(MIGRATED_VFS, migrated ? "true" : "false", true);
	}

	public static final VFSStatus canVersion(File file) {
		VFSStatus canMeta = canMeta(file);
		if(canMeta == VFSConstants.YES) {
			// version only works with metadata but not in: portfolio, scorm, forum...
			Path bFile = file.toPath();
			Path bcRoot = FolderConfig.getCanonicalRootPath();
			return !bFile.startsWith(bcRoot.resolve("forum"))
					&& !bFile.startsWith(bcRoot.resolve("portfolio"))
					&& !bFile.startsWith(bcRoot.resolve("scorm"))
					&& !bFile.startsWith(bcRoot.resolve("certificates"))
					&& !bFile.startsWith(bcRoot.resolve("qtiassessment"))
					&& !bFile.startsWith(bcRoot.resolve("transcodedVideos"))
					&& !bFile.startsWith(bcRoot.resolve("qpool"))
					? VFSConstants.YES : VFSConstants.NO;
		}
		return canMeta;
	}
	
	public static final VFSStatus canMeta(File file) {
		Path bFile = file.toPath();
		Path bcRoot = FolderConfig.getCanonicalRootPath();
		String filename = file.getName();
		return bFile.startsWith(bcRoot)
				&& !bFile.startsWith(FolderConfig.getCanonicalMetaRootPath())
				&& !bFile.startsWith(FolderConfig.getCanonicalVersionRootPath())
				&& !bFile.startsWith(FolderConfig.getCanonicalTmpPath())
				&& !bFile.startsWith(bcRoot.resolve("bulkassessment"))
				&& !file.isHidden()
				&& !filename.startsWith("._oo_")
				&& !filename.equals("CourseConfig.xml")
				&& !filename.equals(".DS_Store")
				&& !filename.equals("__MACOSX")
				? VFSConstants.YES : VFSConstants.NO;
	}
	
	public long getUpperBorder() {
		return upperBorder;
	}
	
	public long getLowerBorder() {
		return lowerBorder;
	}

	public double getZipMinInflateRatio() {
		return zipMinInflateRatio;
	}

	public void setZipMinInflateRatio(double zipMinInflateRatio) {
		this.zipMinInflateRatio = zipMinInflateRatio;
	}

	public int getZipMaxEntries() {
		return zipMaxEntries;
	}

	public void setZipMaxEntries(int zipMaxEntries) {
		this.zipMaxEntries = zipMaxEntries;
	}
}
