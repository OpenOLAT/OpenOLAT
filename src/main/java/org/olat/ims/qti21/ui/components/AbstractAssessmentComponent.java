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

import java.net.URI;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.control.JSAndCSSAdder;
import org.olat.core.gui.render.ValidationResult;
import org.olat.ims.qti21.ui.CandidateSessionContext;

import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

/**
 * 
 * Initial date: 04.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractAssessmentComponent extends AbstractComponent {
	
	private URI assessmentObjectUri;
	private ResourceLocator resourceLocator;
	private CandidateSessionContext candidateSessionContext;
	
	public AbstractAssessmentComponent(String name) {
		super(name);
	}
	
	public URI getAssessmentObjectUri() {
		return assessmentObjectUri;
	}

	public void setAssessmentObjectUri(URI assessmentObjectUri) {
		this.assessmentObjectUri = assessmentObjectUri;
	}
	
	public ResourceLocator getResourceLocator() {
		return resourceLocator;
	}

	public void setResourceLocator(ResourceLocator resourceLocator) {
		this.resourceLocator = resourceLocator;
	}
	
	public CandidateSessionContext getCandidateSessionContext() {
		return candidateSessionContext;
	}

	public void setCandidateSessionContext(CandidateSessionContext candidateSessionContext) {
		this.candidateSessionContext = candidateSessionContext;
	}
	
	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		//
	}
	
	@Override
	public void validate(UserRequest ureq, ValidationResult vr) {
		super.validate(ureq, vr);

		JSAndCSSAdder jsa = vr.getJsAndCSSAdder();
		jsa.addRequiredStaticJsFile("assessment/rendering/javascript/QtiWorksRendering.js");
		jsa.addRequiredStaticJsFile("assessment/rendering/javascript/AsciiMathInputController.js");
		jsa.addRequiredStaticJsFile("assessment/rendering/javascript/UpConversionAjaxController.js");
		
		jsa.addRequiredStaticJsFile("js/jquery/maphilight/jquery.maphilight.js");
		jsa.addRequiredStaticJsFile("js/jquery/ui/jquery-ui-1.11.4.custom.dnd.min.js");
	}

}
