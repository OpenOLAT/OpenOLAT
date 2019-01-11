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
package org.olat.course.nodes.gta;

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.editor.htmleditor.WysiwygFactory;
import org.olat.core.gui.control.generic.iframe.DeliveryOptions;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.io.SystemFileFilter;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 09.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaskHelper {
	
	private static final DecimalFormat formatFileSize = new DecimalFormat("#0.#", new DecimalFormatSymbols(Locale.ENGLISH));
	
	public static String format(double value) {
		synchronized(formatFileSize) {
			return formatFileSize.format(value);
		}
	}
	
	public static DeliveryOptions getStandardDeliveryOptions() {
		DeliveryOptions config = new DeliveryOptions();
		config.setjQueryEnabled(Boolean.TRUE);
		config.setOpenolatCss(Boolean.TRUE);
		return config;
	}
	
	public static int countDocuments(File directory) {
		int count = 0;
		if(directory.isDirectory()) {
			count = directory.listFiles(SystemFileFilter.FILES_ONLY).length;
		}
		return count;
	}
	
	public static File[] getDocuments(File directory) {
		File[] files = null;
		if(directory.isDirectory()) {
			files = directory.listFiles(SystemFileFilter.FILES_ONLY);
		}
		return files;
	}
	
	public static boolean hasDocuments(File directory) {
		int count = 0;
		if(directory.isDirectory()) {
			count = directory.listFiles(SystemFileFilter.FILES_ONLY).length;
		}
		return count > 0;
	}
	
	public static boolean inOrNull(Task task, TaskProcess... steps) {
		if(task == null) return true;
		
		if(steps != null && steps.length > 0) {
			for(int i=steps.length; i-->0; ) {
				if(steps[i] != null && steps[i] == task.getTaskStatus()) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public static FilesLocked getDocumentsLocked(VFSContainer documentsContainer, File[] documents) {
		StringBuilder by = new StringBuilder();
		StringBuilder files = new StringBuilder();
		
		boolean locked = false;
		for(File submittedDocument:documents) {
			VFSLeaf fileLeaf = (VFSLeaf)documentsContainer.resolve(submittedDocument.getName());
			OLATResourceable lockResourceable = WysiwygFactory.createLockResourceable(fileLeaf);
			String lockTocken = WysiwygFactory.createLockToken(documentsContainer, submittedDocument.getName());
			if(CoordinatorManager.getInstance().getCoordinator().getLocker()
				.isLocked(lockResourceable, lockTocken)) {

				locked |= true;
				Identity lockedBy = CoordinatorManager.getInstance().getCoordinator()
						.getLocker().getLockedBy(lockResourceable, lockTocken);
				
				String fullname = "???";
				if(lockedBy != null) {
					fullname = CoreSpringFactory.getImpl(UserManager.class).getUserDisplayName(lockedBy);
				}
				if(by.length() > 0) {
					by.append(", ");
					files.append(", ");
				}
				by.append(fullname);
				files.append(submittedDocument.getName());
			}
		}
		
		if(locked) {
			return new FilesLocked(by.toString(), files.toString());
		}
		return null;
	}
	
	public static class FilesLocked {
		private String lockedBy;
		private String lockedFiles;
		
		public FilesLocked(String lockedBy, String lockedFiles) {
			this.lockedBy = lockedBy;
			this.lockedFiles = lockedFiles;
		}

		public String getLockedBy() {
			return lockedBy;
		}

		public void setLockedBy(String lockedBy) {
			this.lockedBy = lockedBy;
		}

		public String getLockedFiles() {
			return lockedFiles;
		}

		public void setLockedFiles(String lockedFiles) {
			this.lockedFiles = lockedFiles;
		}
	}
}
