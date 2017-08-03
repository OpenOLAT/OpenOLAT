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
package org.olat.ims.qti21.ui.components;

import static org.olat.ims.qti21.ui.QTIWorksAssessmentTestEvent.Event.mark;
import static org.olat.ims.qti21.ui.QTIWorksAssessmentTestEvent.Event.selectItem;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.util.StringHelper;
import org.olat.ims.qti21.ui.QTIWorksAssessmentTestEvent;

import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.running.TestSessionController;

/**
 * 
 * Initial date: 01.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentTreeFormItem extends AssessmentObjectFormItem {
	
	private final AssessmentTreeComponent component;
	private final AssessmentTestComponent testComponent;
	
	public AssessmentTreeFormItem(String name, AssessmentTestComponent testComponent, FormSubmit submitButton) {
		super(name, submitButton);
		this.testComponent = testComponent;
		component = new AssessmentTreeComponent(name + "_cmp", this);
	}

	@Override
	public AssessmentTreeComponent getComponent() {
		return component;
	}
	
	public boolean getShowTitles() {
		return component.isShowTitles();
	}
	
	public void setShowTitles(boolean showTitles) {
		component.setShowTitles(showTitles);
	}

	public ResolvedAssessmentTest getResolvedAssessmentTest() {
		return component.getResolvedAssessmentTest();
	}

	public void setResolvedAssessmentTest(ResolvedAssessmentTest resolvedAssessmentTest) {
		component.setResolvedAssessmentTest(resolvedAssessmentTest);
	}

	public TestSessionController getTestSessionController() {
		return component.getTestSessionController();
	}

	public void setTestSessionController(TestSessionController testSessionController) {
		component.setTestSessionController(testSessionController);
	}

	@Override
	protected Component getFormItemComponent() {
		return component;
	}

	@Override
	protected void rootFormAvailable() {
		//
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		String uri = ureq.getModuleURI();
		if(uri == null) {
			QTIWorksAssessmentTestEvent event = null;
			String cmd = ureq.getParameter("cid");
			if(StringHelper.containsNonWhitespace(cmd)) {
				switch(QTIWorksAssessmentTestEvent.Event.valueOf(cmd)) {
					case selectItem: {
						String selectedItem = ureq.getParameter("item");
						event = new QTIWorksAssessmentTestEvent(selectItem, selectedItem, this);
						getRootForm().fireFormEvent(ureq, event);
						component.setDirty(true);
						testComponent.setDirty(true);
						break;
					}
					case mark: {
						String selectedItem = ureq.getParameter("item");
						event = new QTIWorksAssessmentTestEvent(mark, selectedItem, this);
						getRootForm().fireFormEvent(ureq, event);
						break;
					}
					default: break;
				}
			}
		}
	}

	@Override
	public void reset() {
		//
	}
}
