/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.feedback.appsfeedback;

import java.util.Date;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.model.ApplicationsFeedbackConfiguration;
import org.olat.modules.selectus.model.Position;

/**
 * 
 * Initial date: 29 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MemberFeedbackRow {
	
	private final FormLink editLink;
	private final ApplicationFeedback feedback;
	private final Application application;
	private final Position position;
	private final Organisation organisation;
	private final ApplicationsFeedbackConfiguration config;
	
	public MemberFeedbackRow(ApplicationFeedback feedback, FormLink editLink) {
		this.feedback = feedback;
		this.config = feedback.getConfiguration();
		this.editLink = editLink;
		this.application = feedback.getApplication();
		this.position = application.getPosition();
		this.organisation = position.getOrganisation();
	}

	public ApplicationFeedback getFeedback() {
		return feedback;
	}
	
	public Application getApplication() {
		return application;
	}

	public Position getPosition() {
		return position;
	}

	public Organisation getOrganisation() {
		return organisation;
	}
	
	public Date getDeadline() {
		Date deadline = feedback.getDeadline();
		if(deadline == null) {
			deadline = config.getDeadline();
		}
		return deadline;
	}
	
	public boolean hasComment() {
		return StringHelper.containsNonWhitespace(feedback.getComment());
	}

	public FormLink getEditLink() {
		return editLink;
	}

}
