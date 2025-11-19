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
package org.olat.ims.qti21.ui.assessment;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.CourseNode;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.RepositoryEntryInfoCardController;

/**
 * 
 * Initial date: Nov 11, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CorrectionIdentityRepositoryEntryPrintController extends RepositoryEntryInfoCardController {

	private final CourseNode courseNode;
	private final AssessmentTestSession assessmentTestSession;
	private final String userLableI18n;
	private final String userValue;

	public CorrectionIdentityRepositoryEntryPrintController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, CourseNode courseNode, AssessmentTestSession assessmentTestSession,
			String userLableI18n, String userValue) {
		super(ureq, wControl, entry);
		this.courseNode = courseNode;
		this.assessmentTestSession = assessmentTestSession;
		this.userLableI18n = userLableI18n;
		this.userValue = userValue;
		
		initForm(ureq);
	}

	@Override
	protected void addInfoItems(FormLayoutContainer itemsCont) {
		addCourseAndNodeId(itemsCont);
		addCourseNode(itemsCont);
		addTest(itemsCont);
		addTestAndRunId(itemsCont);
		addUser(itemsCont);
	}

	private void addCourseAndNodeId(FormLayoutContainer itemsCont) {
		String courseAndNodeid = String.valueOf(entry.getKey()) + " / " + courseNode.getIdent();
		uifactory.addStaticTextElement("print.course.and.node.id", courseAndNodeid, itemsCont);
	}

	private void addCourseNode(FormLayoutContainer itemsCont) {
		uifactory.addStaticTextElement("print.course.node", courseNode.getLongTitle(), itemsCont);
	}

	private void addTest(FormLayoutContainer itemsCont) {
		RepositoryEntry testEntry = assessmentTestSession.getTestEntry();
		String testEntryName = StringHelper.escapeHtml(testEntry.getDisplayname());
		if (StringHelper.containsNonWhitespace(testEntry.getExternalRef())) {
			testEntryName += " · <span class=\"o_muted\">";
			testEntryName += StringHelper.escapeHtml(testEntry.getExternalRef());
			testEntryName += "</span>";
		}
		uifactory.addStaticTextElement("print.test.resource", testEntryName, itemsCont);
	}
	
	private void addTestAndRunId(FormLayoutContainer itemsCont) {
		String courseAndNodeid = String.valueOf(entry.getKey()) + " / " + String.valueOf(assessmentTestSession.getKey());
		uifactory.addStaticTextElement("print.test.and.run.id", courseAndNodeid, itemsCont);
	}

	private void addUser(FormLayoutContainer itemsCont) {
		if (StringHelper.containsNonWhitespace(userValue)) {
			uifactory.addStaticTextElement(userLableI18n, StringHelper.escapeHtml(userValue), itemsCont);
		}
	}

}
