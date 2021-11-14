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
package org.olat.user.ui.admin.bulk.tempuser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.admin.user.imp.TransientIdentity;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.model.FindNamedIdentityCollection;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.login.auth.OLATAuthManager;
import org.olat.login.validation.SyntaxValidator;
import org.olat.login.validation.ValidationResult;
import org.olat.user.ui.admin.bulk.tempuser.TemporaryUsersOverviewTableModel.TransientIdentityCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 janv. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TemporaryUsersOverviewController extends StepFormBasicController {
	
	private FlexiTableElement tableEl;
	private TemporaryUsersOverviewTableModel tableModel;
	
	private final CreateTemporaryUsers createTemporaryUsers;
	private final SyntaxValidator usernameSyntaxValidator;

	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private OLATAuthManager olatAuthManager;
	
	public TemporaryUsersOverviewController(UserRequest ureq, WindowControl wControl, Form form,
			CreateTemporaryUsers createTemporaryUsers, StepsRunContext stepsRunContext) {
		super(ureq, wControl, form, stepsRunContext, LAYOUT_CUSTOM, "overview");
		usernameSyntaxValidator = olatAuthManager.createUsernameSytaxValidator();
		this.createTemporaryUsers = createTemporaryUsers;
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TransientIdentityCols.status, new CreateErrorFlexiCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TransientIdentityCols.username));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TransientIdentityCols.firstname));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TransientIdentityCols.lastname));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TransientIdentityCols.expiration, new DateFlexiCellRenderer(getLocale())));

		tableModel = new TemporaryUsersOverviewTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 128, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
	}
	
	private void loadModel() {
		List<TransientIdentity> identities = createTemporaryUsers.getProposedIdentities();
		List<String> identList = identities.stream()
				.map(TransientIdentity::getName)
				.collect(Collectors.toList());
		FindNamedIdentityCollection identityCollection = securityManager
				.findAndCollectIdentitiesBy(identList);
		
		List<TemporaryUserRow> rows = new ArrayList<>(identities.size());
		for(TransientIdentity identity:identities) {
			boolean alreadyExists = identityCollection.getNameToIdentities().containsKey(identity.getName());
			String errorMsg = validateUsername(identity.getName(), identity);
			rows.add(new TemporaryUserRow(identity, alreadyExists, errorMsg));
			
		}
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private String validateUsername(String username, Identity userIdentity) {
		if (StringHelper.containsNonWhitespace(username)) {
			ValidationResult validationResult = usernameSyntaxValidator.validate(username, userIdentity);
			if (!validationResult.isValid()) {
				return validationResult.getInvalidDescriptions().get(0).getText(getLocale());
			}
		}
		return null;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		tableEl.clearError();
		List<TransientIdentity> validIdentities = getValidIdentities();
		if(validIdentities.isEmpty()) {
			tableEl.setErrorKey("error.no.user.to.create", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		List<TransientIdentity> validIdentities = getValidIdentities();
		createTemporaryUsers.setValidatedIdentities(validIdentities);
		fireEvent (ureq, StepsEvent.ACTIVATE_NEXT);
	}
	
	private List<TransientIdentity> getValidIdentities() {
		List<TemporaryUserRow> rows = tableModel.getObjects();
		return rows.stream()
			.filter(r -> !r.isAlreadyExists() && !StringHelper.containsNonWhitespace(r.getErrorMsg()))
			.map(TemporaryUserRow::getIdentity)
			.collect(Collectors.toList());
	}
}
