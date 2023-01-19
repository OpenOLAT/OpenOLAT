/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.user.ui.admin.authentication;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.admin.user.UserChangePasswordController;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Util;
import org.olat.login.auth.AuthenticationProviderSPI;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date:  Aug 27, 2004
 *
 * @author Mike Stock
 */
public class UserAuthenticationsEditorController extends FormBasicController {
	
	private FormLink editNickNameLink;
	private FormLink addAuthenticationLink;
	private StaticTextElement nickNameEl;
	private FlexiTableElement tableEl;
	private AuthenticationsTableDataModel tableModel;

	private CloseableModalController cmc;
	private DialogBoxController confirmationDialog;
	private UserAuthenticationAddController addCtrl;
	private UserAuthenticationEditController editCtrl;
	private UserNickNameEditController editNickNameCtrl;
	
	private Identity changeableIdentity;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;

	/**
	 * @param ureq
	 * @param wControl
	 * @param changeableIdentity
	 */
	public UserAuthenticationsEditorController(UserRequest ureq, WindowControl wControl, Identity changeableIdentity) { 
		super(ureq, wControl, "authentications", Util.createPackageTranslator(UserChangePasswordController.class, ureq.getLocale()));
		
		this.changeableIdentity = changeableIdentity;
		initForm(ureq);
		reloadModel();
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		nickNameEl = uifactory.addStaticTextElement("username", "", formLayout);
		nickNameEl.getComponent().setSpanAsDomReplaceable(true);
		editNickNameLink =  uifactory.addFormLink("edit", formLayout);
		editNickNameLink.setIconLeftCSS("o_icon o_icon_edit");
		
		addAuthenticationLink =  uifactory.addFormLink("add.authentication", formLayout, Link.BUTTON);
		addAuthenticationLink.setIconLeftCSS("o_icon o_icon_add");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AuthenticationCols.provider));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AuthenticationCols.issuer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AuthenticationCols.login));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AuthenticationCols.credential));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.action", translate("delete"), "delete"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.action", AuthenticationCols.edit.ordinal(), "edit",
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("edit"), "edit"), null)));
		
		tableModel = new AuthenticationsTableDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
	}
	
	public void reloadModel() {
		changeableIdentity = securityManager.loadIdentityByKey(changeableIdentity.getKey());
		loadModel();
	}
	
	/**
	 * Rebuild the authentications table data model
	 */
	private void loadModel() {
		String nickName = changeableIdentity.getUser().getProperty(UserConstants.NICKNAME, getLocale());
		nickNameEl.setValue(nickName);

		Map<String, AuthenticationProviderSPI> providers = CoreSpringFactory.getBeansOfType(AuthenticationProviderSPI.class);
		List<Authentication> authentications = securityManager.getAuthentications(changeableIdentity);
		List<UserAuthenticationRow> rows = new ArrayList<>(authentications.size());
		for(Authentication authentication:authentications) {
			AuthenticationProviderSPI provider = getProvider(authentication, providers);
			boolean canChange = provider != null && provider.canChangeAuthenticationUsername(authentication.getProvider());
			rows.add(new UserAuthenticationRow(authentication, provider, canChange));
		}
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private AuthenticationProviderSPI getProvider(Authentication authentication, Map<String, AuthenticationProviderSPI> providers) {
		for(AuthenticationProviderSPI provider:providers.values()) {
			List<String> names = provider.getProviderNames();
			if(names.contains(authentication.getProvider())) {
				return provider;
			}	
		}
		return null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(editNickNameLink == source) {
			doEditNickName(ureq);
		} else if(this.addAuthenticationLink == source) {
			doAdd(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("delete".equals(se.getCommand())) {
					doConfirmDelete(ureq, tableModel.getObject(se.getIndex()));
				} else if("edit".equals(se.getCommand())) {
					doEdit(ureq, tableModel.getObject(se.getIndex()));
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == confirmationDialog) {
			if (DialogBoxUIFactory.isYesEvent(event)) { 
				doDelete((Authentication)confirmationDialog.getUserObject());
			}
		} else if(addCtrl == source || editCtrl == source || editNickNameCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				reloadModel();
				fireEvent(ureq, Event.DONE_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editNickNameCtrl);
		removeAsListenerAndDispose(editCtrl);
		removeAsListenerAndDispose(addCtrl);
		removeAsListenerAndDispose(cmc);
		editNickNameCtrl = null;
		editCtrl = null;
		addCtrl = null;
		cmc = null;
	}
	
	private void doEditNickName(UserRequest ureq) {
		editNickNameCtrl = new UserNickNameEditController(ureq, getWindowControl(),
				changeableIdentity, tableModel.getObjects());
		listenTo(editNickNameCtrl);
		
		String title = translate("edit.title");
		cmc = new CloseableModalController(getWindowControl(), "close", editNickNameCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doEdit(UserRequest ureq, UserAuthenticationRow row) {
		editCtrl = new UserAuthenticationEditController(ureq, getWindowControl(),
				row.getAuthentication(), row.getProvider());
		listenTo(editCtrl);
		
		String title = translate("edit.title");
		cmc = new CloseableModalController(getWindowControl(), "close", editCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAdd(UserRequest ureq) {
		addCtrl = new UserAuthenticationAddController(ureq, getWindowControl(), changeableIdentity);
		listenTo(addCtrl);
		
		String title = translate("add.authentication.title", userManager.getUserDisplayName(changeableIdentity));
		cmc = new CloseableModalController(getWindowControl(), "close", addCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmDelete(UserRequest ureq, UserAuthenticationRow row) {
		String fullname = userManager.getUserDisplayName(changeableIdentity);
		String msg = translate("authedit.delete.confirm", row.getAuthentication().getProvider(), fullname);
		confirmationDialog = activateYesNoDialog(ureq, null, msg, confirmationDialog);
		confirmationDialog.setUserObject(row.getAuthentication());
	}
	
	private void doDelete(Authentication auth) {
		securityManager.deleteAuthentication(auth);
		getWindowControl().setInfo(getTranslator().translate("authedit.delete.success", 
				auth.getProvider(), changeableIdentity.getName()));
		loadModel();
	}

	public enum AuthenticationCols implements FlexiSortableColumnDef {
		provider("table.auth.provider"),
		issuer("table.auth.issuer"),
		login("table.auth.login"),
		credential("table.auth.credential"),
		edit("edit");
		
		private final String i18nKey;
		
		private AuthenticationCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return this != edit;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
	
	private static class AuthenticationsTableDataModel extends DefaultFlexiTableDataModel<UserAuthenticationRow>
	implements SortableFlexiTableDataModel<UserAuthenticationRow> {
		
		private static final AuthenticationCols[] COLS = AuthenticationCols.values();
		
		private final Locale locale;

		public AuthenticationsTableDataModel(FlexiTableColumnModel columnsModel, Locale locale) {
			super(columnsModel);
			this.locale = locale;
		}

		@Override
		public void sort(SortKey orderBy) {
			if(orderBy != null) {
				List<UserAuthenticationRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
				super.setObjects(rows);
			}
		}

		@Override
		public final Object getValueAt(int row, int col) {
			UserAuthenticationRow authRow = getObject(row);
			return getValueAt(authRow, col);
		}

		@Override
		public Object getValueAt(UserAuthenticationRow row, int col) {
			Authentication auth = row.getAuthentication();
			switch (COLS[col]) {
				case provider: return auth.getProvider();
				case issuer: return getIssuer(auth);
				case login: return auth.getAuthusername();
				case credential: return auth.getCredential();
				case edit: return row.isCanEditAuthenticationUsername();
				default: return "error";
			}
		}
		
		private String getIssuer(Authentication auth) {
			if(auth.getIssuer() == null || BaseSecurity.DEFAULT_ISSUER.equals(auth.getIssuer())) {
				return null;
			}
			return auth.getIssuer();
		}
	}
}
