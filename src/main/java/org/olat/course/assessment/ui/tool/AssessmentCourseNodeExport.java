/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.assessment.ui.tool;

import java.util.List;
import java.util.Locale;
import java.util.zip.ZipOutputStream;

import org.olat.admin.user.imp.TransientIdentity;
import org.olat.core.commons.services.taskexecutor.manager.PersistentTaskProgressCallback;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.util.SyntheticUserRequest;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.UserSession;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.environment.CourseEnvironment;

/**
 * 
 * Initial date: Dec 15, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class AssessmentCourseNodeExport {
	
	protected String startPoint;
	protected volatile boolean cancelled = false;
	protected PersistentTaskProgressCallback progress;
	
	protected Identity doer;
	protected List<Identity> identities;
	protected CourseNode courseNode;
	protected final boolean withPdfs;
	protected final CourseEnvironment courseEnv;
	protected final boolean withNonParticipants;
	protected final WindowControl windowControl;
	protected UserRequest ureq;
	
	public AssessmentCourseNodeExport(Identity doer, CourseEnvironment courseEnv,
			CourseNode courseNode, List<Identity> identities, boolean withNonParticipants, boolean withPdfs, Locale locale,
			WindowControl windowControl) {
		this.doer = doer;
		this.courseNode = courseNode;
		this.identities = identities;
		this.courseEnv = courseEnv;
		this.withPdfs = withPdfs;
		this.windowControl = windowControl;
		this.withNonParticipants = withNonParticipants;
		
		ureq = new SyntheticUserRequest(new TransientIdentity(), locale, new UserSession());
		ureq.getUserSession().setRoles(Roles.userRoles());
	}
	
	public boolean isCancelled() {
		if(cancelled) return true;
		
		boolean interrupted = Thread.interrupted();
		if(interrupted) {
			cancelled = true;
		}
		return interrupted;
	}
	
	public void setStartPoint(String point) {
		this.startPoint = point;
	}

	/**
	 * @param zout  
	 */
	public void export(ZipOutputStream zout, PersistentTaskProgressCallback progress) {
		this.progress = progress;
	}

}
