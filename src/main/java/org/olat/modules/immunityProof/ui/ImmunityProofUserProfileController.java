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
package org.olat.modules.immunityProof.ui;

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.immunityProof.ImmunityProof;
import org.olat.modules.immunityProof.ImmunityProofModule.ImmunityProofLevel;
import org.olat.modules.immunityProof.ImmunityProofService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 09.09.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ImmunityProofUserProfileController extends FormBasicController {

	private ImmunityProof immunityProof;
	
	private FormLink addImmunityProofButton;
	private FormLink deleteImmunityProofButton;
	
	private CloseableModalController cmc;
	private ImmunityProofCreateController immunityProofCreateController;
	private ImmunityProofDeleteConfirmController deleteConfirmController;
	
	@Autowired
	private ImmunityProofService immunityProofService;
	
	public ImmunityProofUserProfileController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "immunity_proof_user_profile");
		
		setTranslator(Util.createPackageTranslator(ImmunityProof.class, getLocale(), getTranslator()));
		
		initForm(ureq);
		loadData();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		deleteImmunityProofButton = uifactory.addFormLink("delete.immunity.proof", formLayout, Link.BUTTON);
		deleteImmunityProofButton.setIconLeftCSS("o_icon o_icon_lg o_icon_clear_all");
		addImmunityProofButton = uifactory.addFormLink("add.immunity.proof", formLayout, Link.BUTTON);
		addImmunityProofButton.setIconLeftCSS("o_icon o_icon_lg o_icon_add");
		addImmunityProofButton.setPrimary(true);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (deleteImmunityProofButton == source) {
			doAskForRemoval(ureq);
		} else if (addImmunityProofButton == source) {
			doAddImmunityProof(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == immunityProofCreateController) {
			if (event.equals(Event.DONE_EVENT)) {
				flc.setDirty(true);
				loadData();
			}
			
			cleanUp();
		} if (source == deleteConfirmController) {
			if (event.equals(Event.DONE_EVENT)) {
				immunityProofService.deleteImmunityProof(getIdentity());
				flc.setDirty(true);
				loadData();
			}
			
			cleanUp();
		} else if (source == cmc) {
			
			
			cleanUp();
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// Nothing to do here
	}

	@Override
	protected void doDispose() {
		
	}
	
	private void cleanUp() {
		if (cmc != null && cmc.isCloseable()) {
			cmc.deactivate();
		}
		
		removeAsListenerAndDispose(immunityProofCreateController);
		removeAsListenerAndDispose(cmc);
		
		immunityProofCreateController = null;
		cmc = null;
	}
	
	private void doAddImmunityProof(UserRequest ureq) {
		immunityProofCreateController = new ImmunityProofCreateController(ureq, getWindowControl(), getIdentity(), false);
		listenTo(immunityProofCreateController);
		
		cmc = new CloseableModalController(getWindowControl(), translate("cancel"), immunityProofCreateController.getInitialComponent(), true, translate("add.immunity.proof"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAskForRemoval(UserRequest ureq) {
		deleteConfirmController = new ImmunityProofDeleteConfirmController(ureq, getWindowControl());
		listenTo(deleteConfirmController);
		
		cmc = new CloseableModalController(getWindowControl(), translate("cancel"), deleteConfirmController.getInitialComponent(), true, translate("delete.immunity.proof"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void loadData() {
		reset();
		
		this.immunityProof = immunityProofService.getImmunityProof(getIdentity());
		ImmunityProofLevel status = ImmunityProofLevel.none;
		String validUntil = null;
		
		if (immunityProof != null) {
			if (immunityProof.getSafeDate().after(new Date())) {
				addImmunityProofButton.setVisible(false);
				validUntil = Formatter.getInstance(getLocale()).formatDate(immunityProof.getSafeDate());
				status = immunityProof.isValidated() ? ImmunityProofLevel.validated : ImmunityProofLevel.claimed;
			} 
		}
		
		if (status.equals(ImmunityProofLevel.none)) {
			deleteImmunityProofButton.setVisible(false);
		}
		
		User user = getIdentity().getUser();
		flc.contextPut("user", user.getFirstName() + " " + user.getLastName());
		flc.contextPut("locale", getLocale().toLanguageTag());
		flc.contextPut("status", status);
		flc.contextPut("validUntil", validUntil);
		
		String birthDate = user.getProperty(UserConstants.BIRTHDAY, getLocale());
		if (StringHelper.containsNonWhitespace(birthDate)) {
			flc.contextPut("birthdate", birthDate);
		}
	}
	
	private void reset() {		
		addImmunityProofButton.setVisible(true);
		deleteImmunityProofButton.setVisible(true);
	}

	

}
