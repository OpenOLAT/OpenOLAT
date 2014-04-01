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
package org.olat.portfolio.ui.artefacts.view;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.portfolio.model.structel.PortfolioStructure;

/**
 * 
 * Description:<br>
 * a view only controller for reflexion on artefact and on link
 * 
 * <P>
 * Initial Date:  8 nov. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class EPReflexionViewController extends FormBasicController {
	
	private RichTextElement artRef;
	private String artefactReflexion = "";
	private EPFrontendManager ePFMgr;
	private String linkReflexion = "";
	private boolean canSeeArtefactReflexion;
	private RichTextElement linkRef;

	public EPReflexionViewController(UserRequest ureq, WindowControl wControl, AbstractArtefact artefact, PortfolioStructure structure) {
		super(ureq, wControl, FormBasicController.LAYOUT_VERTICAL);
		ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean("epFrontendManager");
		this.linkReflexion = ePFMgr.getReflexionForArtefactToStructureLink(artefact, structure);
		this.canSeeArtefactReflexion = artefact.getAuthor().getKey().equals(ureq.getIdentity().getKey());
		if (canSeeArtefactReflexion){
			this.artefactReflexion = artefact.getReflexion();
		}
		
		initForm(ureq);
	}


	/**
	 * @see org.olat.core.gui.control.generic.wizard.StepFormBasicController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("artefact.reflexion.view.descr");

		if (canSeeArtefactReflexion){
			artRef = uifactory.addRichTextElementForStringDataMinimalistic("reflexion.artefact", "reflexion.artefact", artefactReflexion, 12, -1,
					formLayout, getWindowControl());
			artRef.setEnabled(false);
		}
		
		if (StringHelper.containsNonWhitespace(linkReflexion)){
			linkRef = uifactory.addRichTextElementForStringDataMinimalistic("reflexion.link", "reflexion.link", linkReflexion, 12, -1,
					formLayout, getWindowControl());
			linkRef.setEnabled(false);
		}
		
	}
	
	/**
	 * @see org.olat.core.gui.control.generic.wizard.StepFormBasicController#doDispose()
	 */
	@Override
	protected void doDispose() {
		//nothing
	}

	/**
	 * @see org.olat.core.gui.control.generic.wizard.StepFormBasicController#formOK(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void formOK(UserRequest ureq) {
		//do nothing
	}
}