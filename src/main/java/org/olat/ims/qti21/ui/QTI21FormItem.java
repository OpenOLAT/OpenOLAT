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
package org.olat.ims.qti21.ui;

import java.net.URI;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.ims.qti21.RequestTimestampContext;

import uk.ac.ed.ph.jqtiplus.running.TestSessionController;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

/**
 * 
 * Initial date: 11.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21FormItem extends FormItemImpl {
	
	private final QTI21Component component;
	
	public QTI21FormItem(String name) {
		super(name);
		component = new QTI21Component(name + "_cmp", this);
	}
	
	public URI getAssessmentObjectUri() {
		return component.getAssessmentObjectUri();
	}

	public void setAssessmentObjectUri(URI assessmentObjectUri) {
		component.setAssessmentObjectUri(assessmentObjectUri);
	}

	public TestSessionController getTestSessionController() {
		return component.getTestSessionController();
	}

	public void setTestSessionController(TestSessionController testSessionController) {
		component.setTestSessionController(testSessionController);
	}
	
	public RequestTimestampContext getRequestTimestampContext() {
		return component.getRequestTimestampContext();
	}

	public void setRequestTimestampContext(RequestTimestampContext requestTimestampContext) {
		component.setRequestTimestampContext(requestTimestampContext);
	}

	public ResourceLocator getResourceLocator() {
		return component.getResourceLocator();
	}

	public void setResourceLocator(ResourceLocator resourceLocator) {
		component.setResourceLocator(resourceLocator);
	}

	@Override
	protected Component getFormItemComponent() {
		return component;
	}

	@Override
	protected void rootFormAvailable() {
		//
	}
	
	private static final String SELECT_ITEM = "/select-item/";

	@Override
	public void evalFormRequest(UserRequest ureq) {
		String uri = ureq.getModuleURI();
		if(uri.startsWith(SELECT_ITEM)) {
			String sub = uri.substring(SELECT_ITEM.length());
			QTI21FormEvent event = new QTI21FormEvent("select-item", sub, this);
			getRootForm().fireFormEvent(ureq, event);
		}
	}

	@Override
	public void reset() {
		//
	}
}