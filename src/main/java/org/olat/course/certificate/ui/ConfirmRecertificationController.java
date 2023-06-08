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
package org.olat.course.certificate.ui;

import java.util.Date;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.ResetCourseDataHelper;
import org.olat.modules.assessment.Role;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28 mars 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmRecertificationController extends FormBasicController {
	
	private CourseEnvironment courseEnv;
	private final Certificate lastCertificate;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CertificatesManager certificatesManager;
	
	public ConfirmRecertificationController(UserRequest ureq, WindowControl wControl, CourseEnvironment courseEnv, RepositoryEntry repositoryEntry) {
		super(ureq, wControl, "confirm_recertification");
		this.courseEnv = courseEnv;
		lastCertificate = certificatesManager.getLastCertificate(getIdentity(), repositoryEntry.getOlatResource().getKey());
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			Formatter formatter = Formatter.getInstance(getLocale());
			Date validityDate = lastCertificate.getNextRecertificationDate();
			long days = DateUtils.countDays(ureq.getRequestTimestamp(), validityDate);
			
			String[] args = new String[] {
				formatter.formatDate(lastCertificate.getCreationDate()),
				formatter.formatDate(validityDate),
				Long.toString(days)	
			};
			
			String msg;
			if(days == 1) {
				msg = translate("recertification.modal.info.validity.day", args);
			} else if(days > 1) {
				msg = translate("recertification.modal.info.validity.days", args);
			} else {
				msg = translate("recertification.modal.info.invalid", args);
			}
			layoutCont.contextPut("msg", msg);
		}

		uifactory.addFormSubmitButton("start.recertification", formLayout);
		uifactory.addFormCancelButton("last.attempt", formLayout, ureq, getWindowControl());
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		ResetCourseDataHelper resetHelper = new ResetCourseDataHelper(courseEnv);
		resetHelper.resetCourse(getIdentity(), getIdentity(), Role.user);
		dbInstance.commitAndCloseSession();
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
