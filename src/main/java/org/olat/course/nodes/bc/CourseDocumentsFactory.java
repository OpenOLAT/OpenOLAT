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
package org.olat.course.nodes.bc;

import org.olat.admin.quota.QuotaConstants;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.course.config.CourseConfig;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 1 Oct 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseDocumentsFactory {
	
	public static final String FOLDER_NAME = "coursedocuments";
	private static final String SUBSCRIPTION_SUBIDENTIFIER = "documents";
	
	public static VFSContainer getFileContainer(CourseEnvironment courseEnv) {
		String documentsPath = courseEnv.getCourseConfig().getDocumentsPath();
		return getFileContainer(courseEnv, documentsPath);
	}
	
	public static VFSContainer getFileContainer(CourseEnvironment courseEnv, String documentsPath) {
		if (documentsPath == null) {
			return VFSManager.resolveOrCreateContainerFromPath(courseEnv.getCourseBaseContainer(), FOLDER_NAME);
		}
		VFSItem documentsItem = courseEnv.getCourseFolderContainer().resolve(documentsPath);
		if (documentsItem instanceof VFSContainer) {
			return (VFSContainer)documentsItem;
		}
		return null;
	}
	
	public static String getFileDirectory(CourseEnvironment courseEnv) {
		String documentsPath = courseEnv.getCourseConfig().getDocumentsPath();
		if (documentsPath == null) {
			return courseEnv.getCourseBaseContainer().getRelPath() + "/" + FOLDER_NAME;
		}
		return courseEnv.getCourseFolderContainer().getRelPath() + documentsPath;
	}
	
	public static SubscriptionContext getSubscriptionContext(RepositoryEntry courseEntry) {
		return new SubscriptionContext(courseEntry, SUBSCRIPTION_SUBIDENTIFIER);
	}
	
	public static VFSSecurityCallback getSecurityCallback(UserCourseEnvironment userCourseEnv) {
		return getSecurityCallback(userCourseEnv, false, null);
	}
	
	public static VFSSecurityCallback getSecurityCallback(UserCourseEnvironment userCourseEnv, boolean isGuestOnly,
			SubscriptionContext subContext) {
		String folderPath = getFileDirectory(userCourseEnv.getCourseEnvironment());
		return isReadOnly(userCourseEnv, isGuestOnly)
				? new ReadOnlyCallback(subContext)
				: new ReadWriteCallback(subContext, folderPath);
	}
	
	private static boolean isReadOnly(UserCourseEnvironment userCourseEnv, boolean isGuestOnly) {
		return userCourseEnv.isCourseReadOnly()
				|| isGuestOnly
				|| isParticipantOnly(userCourseEnv)
				|| isRessourceFolderReadOnly(userCourseEnv);
	}
	
	private static boolean isParticipantOnly(UserCourseEnvironment userCourseEnv) {
		return userCourseEnv.isParticipant() && !userCourseEnv.isAdmin() && !userCourseEnv.isCoach();
	}
	
	private static boolean isRessourceFolderReadOnly(UserCourseEnvironment userCourseEnv) {
		CourseConfig courseConfig = userCourseEnv.getCourseEnvironment().getCourseConfig();
		return courseConfig.getDocumentsPath() != null
				&& courseConfig.getDocumentsPath().startsWith("/_sharedfolder")
				&& courseConfig.isSharedFolderReadOnlyMount();
	}

	private static class ReadWriteCallback implements VFSSecurityCallback {
		
		private SubscriptionContext subsContext;
		private Quota quota;
		private final String folderPath;
		private final String defaultQuota;
		
		public ReadWriteCallback(SubscriptionContext subsContext, String folderPath) {
			this.subsContext = subsContext;
			this.folderPath = folderPath;
			this.defaultQuota = QuotaConstants.IDENTIFIER_DEFAULT_DOCUMENTS;
		}

		@Override
		public boolean canRead() {
			return true;
		}

		@Override
		public boolean canWrite() {
			return true;
		}

		@Override
		public boolean canCreateFolder() {
			return true;
		}

		@Override
		public boolean canDelete() {
			return true;
		}

		@Override
		public boolean canList() {
			return true;
		}

		@Override
		public boolean canCopy() {
			return true;
		}

		@Override
		public boolean canDeleteRevisionsPermanently() {
			return true;
		}

		@Override
		public Quota getQuota() {
			if(quota == null) {
				QuotaManager qm = CoreSpringFactory.getImpl(QuotaManager.class);
				Quota q = qm.getCustomQuota(folderPath);
				if (q == null) {
					Quota defQuota = qm.getDefaultQuota(defaultQuota);
					q = qm.createQuota(folderPath, defQuota.getQuotaKB(), defQuota.getUlLimitKB());
				}
				setQuota(q);
			}
			return quota;
		}

		@Override
		public void setQuota(Quota quota) {
			this.quota = quota;
		}

		@Override
		public SubscriptionContext getSubscriptionContext() {
			return subsContext;
		}

	}

	private static class ReadOnlyCallback implements VFSSecurityCallback {
		
		private SubscriptionContext subsContext;

		public ReadOnlyCallback(SubscriptionContext subsContext) {
			super();
			this.subsContext = subsContext;
		}

		@Override
		public boolean canRead() {
			return true;
		}

		@Override
		public boolean canWrite() {
			return false;
		}

		@Override
		public boolean canCreateFolder() {
			return false;
		}

		@Override
		public boolean canDelete() {
			return false;
		}

		@Override
		public boolean canList() {
			return false;
		}

		@Override
		public boolean canCopy() {
			return false;
		}

		@Override
		public boolean canDeleteRevisionsPermanently() {
			return false;
		}

		@Override
		public Quota getQuota() {
			return null;
		}

		@Override
		public void setQuota(Quota quota) {
			//
		}

		@Override
		public SubscriptionContext getSubscriptionContext() {
			return subsContext;
		}

	}
}
