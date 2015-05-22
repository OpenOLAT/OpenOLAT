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
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.render.ValidationResult;
import org.olat.ims.qti21.ui.CandidateSessionContext;

import uk.ac.ed.ph.jqtiplus.running.ItemSessionController;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

/**
 * 
 * Initial date: 10.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentItemComponent extends AbstractComponent {
	
	private static final AssessmentItemComponentRenderer RENDERER = new AssessmentItemComponentRenderer();
	
	private URI assessmentObjectUri;
	private ResourceLocator resourceLocator;
	private ItemSessionController itemSessionController;
	private CandidateSessionContext candidateSessionContext;
	
	private final AssessmentItemFormItem qtiItem;
	
	public AssessmentItemComponent(String name, AssessmentItemFormItem qtiItem) {
		super(name);
		this.qtiItem = qtiItem;
	}

	public AssessmentItemFormItem getQtiItem() {
		return qtiItem;
	}

	public ResourceLocator getResourceLocator() {
		return resourceLocator;
	}

	public void setResourceLocator(ResourceLocator resourceLocator) {
		this.resourceLocator = resourceLocator;
	}

	public ItemSessionController getItemSessionController() {
		return itemSessionController;
	}

	public void setItemSessionController(ItemSessionController itemSessionController) {
		this.itemSessionController = itemSessionController;
	}
	
	public CandidateSessionContext getCandidateSessionContext() {
		return candidateSessionContext;
	}

	public void setCandidateSessionContext(CandidateSessionContext candidateSessionContext) {
		this.candidateSessionContext = candidateSessionContext;
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
