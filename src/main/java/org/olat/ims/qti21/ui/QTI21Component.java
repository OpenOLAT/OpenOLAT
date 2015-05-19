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
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.render.ValidationResult;
import org.olat.ims.qti21.RequestTimestampContext;

import uk.ac.ed.ph.jqtiplus.running.TestSessionController;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

/**
 * 
 * Initial date: 10.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21Component extends AbstractComponent {
	
	private static final QTI21ComponentRenderer RENDERER = new QTI21ComponentRenderer();
	
	private URI assessmentObjectUri;
	private ResourceLocator resourceLocator;
	private TestSessionController testSessionController;
	private CandidateSessionContext candidateSessionContext;
	private RequestTimestampContext requestTimestampContext;
	
	private final QTI21FormItem qtiItem;
	
	public QTI21Component(String name, QTI21FormItem qtiItem) {
		super(name);
		this.qtiItem = qtiItem;
	}

	public QTI21FormItem getQtiItem() {
		return qtiItem;
	}

	public ResourceLocator getResourceLocator() {
		return resourceLocator;
	}

	public void setResourceLocator(ResourceLocator resourceLocator) {
		this.resourceLocator = resourceLocator;
	}

	public TestSessionController getTestSessionController() {
		return testSessionController;
	}

	public void setTestSessionController(TestSessionController testSessionController) {
		this.testSessionController = testSessionController;
	}
	
	public CandidateSessionContext getCandidateSessionContext() {
		return candidateSessionContext;
	}

	public void setCandidateSessionContext(CandidateSessionContext candidateSessionContext) {
		this.candidateSessionContext = candidateSessionContext;
	}

	public RequestTimestampContext getRequestTimestampContext() {
		return requestTimestampContext;
	}

	public void setRequestTimestampContext(RequestTimestampContext requestTimestampContext) {
		this.requestTimestampContext = requestTimestampContext;
	}

	public URI getAssessmentObjectUri() {
		return assessmentObjectUri;
	}

	public void setAssessmentObjectUri(URI assessmentObjectUri) {
		this.assessmentObjectUri = assessmentObjectUri;
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		//
	}

	@Override
	public void validate(UserRequest ureq, ValidationResult vr) {
		super.validate(ureq, vr);
		vr.getJsAndCSSAdder().addRequiredStaticJsFile("assessment/rendering/javascript/QtiWorksRendering.js");
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}
