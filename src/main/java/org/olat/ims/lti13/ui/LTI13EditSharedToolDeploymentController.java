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
package org.olat.ims.lti13.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.ims.lti13.LTI13Platform;
import org.olat.ims.lti13.LTI13Service;
import org.olat.ims.lti13.LTI13SharedToolDeployment;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LTI13EditSharedToolDeploymentController extends FormBasicController {
	
	private TextElement deploymentIdEl;
	private SingleSelection platformsEl;
	
	private RepositoryEntry entry;
	private BusinessGroup businessGroup;
	private LTI13SharedToolDeployment deployment;
	private final List<LTI13Platform> platforms;
	
	@Autowired
	private LTI13Service lti13Service;
	
	public LTI13EditSharedToolDeploymentController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, BusinessGroup businessGroup) {
		super(ureq, wControl);
		this.entry = entry;
		this.businessGroup = businessGroup;
		platforms = lti13Service.getPlatforms();
		initForm(ureq);
	}
	
	public LTI13EditSharedToolDeploymentController(UserRequest ureq, WindowControl wControl,
			LTI13SharedToolDeployment deployment) {
		super(ureq, wControl);
		this.deployment = deployment;
		platforms = lti13Service.getPlatforms();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		KeyValues platformsValues = new KeyValues();
		for(LTI13Platform platform:platforms) {
			platformsValues.add(KeyValues.entry(platform.getKey().toString(), label(platform)));
		}
		
		platformsEl = uifactory.addDropdownSingleselect("deployment.platform", formLayout,
				platformsValues.keys(), platformsValues.values(), null);
		if(deployment != null && platformsValues.containsKey(deployment.getKey().toString())) {
			platformsEl.select(deployment.getKey().toString(), true);
			platformsEl.setEnabled(false);
		} else if(platformsValues.size() > 0) {
			platformsEl.select(platformsValues.keys()[0], true);
		}
		
		String deploymentId = deployment == null ? null : deployment.getDeploymentId();
		deploymentIdEl = uifactory.addTextElement("deployment.id", 255, deploymentId, formLayout);
		deploymentIdEl.setMandatory(true);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("save", buttonsCont);
	}
	
	private String label(LTI13Platform platform) {
		StringBuilder sb = new StringBuilder(64);
		if(StringHelper.containsNonWhitespace(platform.getName())) {
			sb.append(platform.getName())
			  .append(" (").append(platform.getIssuer()).append(")");
		} else {
			sb.append(platform.getIssuer());
		}
		return sb.toString();
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		deploymentIdEl.clearError();
		if(!StringHelper.containsNonWhitespace(deploymentIdEl.getValue())) {
			deploymentIdEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else if(!validateUniqueDeployment()) {
			deploymentIdEl.setErrorKey("error.unique.deployment", null);
			allOk &= false;
		}
		
		return allOk;
	}
	
	private boolean validateUniqueDeployment() {
		String deploymentId = deploymentIdEl.getValue();
		LTI13Platform selectedPlatform = getSelectedPlatform();
		LTI13SharedToolDeployment savedDeployment = lti13Service.getSharedToolDeployment(deploymentId, selectedPlatform);
		return savedDeployment == null || savedDeployment.equals(deployment);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String deploymentId = deploymentIdEl.getValue();
		
		if(deployment == null) {
			LTI13Platform selectedPlatform = getSelectedPlatform();
			selectedPlatform = lti13Service.getPlatformByKey(selectedPlatform.getKey());
			lti13Service.createSharedToolDeployment(deploymentId, selectedPlatform, entry, businessGroup);
		} else {
			deployment.setDeploymentId(deploymentIdEl.getValue());
			deployment = lti13Service.updateSharedToolDeployment(deployment);
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private LTI13Platform getSelectedPlatform() {
		String selectedKey = platformsEl.getSelectedKey();
		LTI13Platform selectedPlatform = null;
		for(LTI13Platform platform:platforms) {
			if(selectedKey.equals(platform.getKey().toString())) {
				selectedPlatform = platform;
			}
		}
		return selectedPlatform;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
