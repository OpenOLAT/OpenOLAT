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
package org.olat.admin.landingpages.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.olat.admin.landingpages.LandingPagesModule;
import org.olat.admin.landingpages.model.RoleToRule;
import org.olat.admin.landingpages.model.Rule;
import org.olat.admin.landingpages.model.Rules;
import org.olat.admin.landingpages.ui.RulesDataModel.RCols;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.util.Util;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 15.05.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LandingPagesAdminController extends FormBasicController {
	
	private static final String USER_PROPS_ID = LandingPagesModule.class.getName();

	private RulesDataModel model;
	private FlexiTableElement tableEl;
	
	private String[] roleKeys;
	private String[] roleValues;
	private final String[] attrKeys;
	private final String[] attrValues;

	private FormSubmit saveButton;
	private ChooserController chooserCtrl;
	private CloseableCalloutWindowController chooserCalloutCtrl;

	@Autowired
	private UserManager userManager;
	@Autowired
	private LandingPagesModule lpModule;
	@Autowired
	private BaseSecurityModule securityModule;
	
	private AtomicInteger counter = new AtomicInteger();
	
	public LandingPagesAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "rules");
		setTranslator(Util.createPackageTranslator(UserPropertyHandler.class, getLocale(), getTranslator()));
		
		RoleToRule[] roles = RoleToRule.values();
		roleKeys = new String[roles.length + 1];
		roleValues = new String[roles.length + 1];
		roleKeys[0] = "none";
		roleValues[0] = translate("none");
		for(int i=0; i<roles.length; i++) {
			roleKeys[i + 1] = roles[i].name();
			roleValues[i + 1] = translate("roles." + roles[i].role() + "s");
		}
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		List<UserPropertyHandler> userPropertyHandlers = userManager
				.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);
		
		int numOfProperties = userPropertyHandlers.size();
		attrKeys = new String[numOfProperties + 1];
		attrValues = new String[numOfProperties + 1];
		attrKeys[0] = "";
		attrValues[0] = "-";
		for(int i=0; i<numOfProperties; i++) {
			UserPropertyHandler handler = userPropertyHandlers.get(i);
			attrKeys[i+1] = handler.getName();
			attrValues[i+1] = translate(handler.i18nFormElementLabelKey());
		}
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("landingpages.title");

		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RCols.position.i18nKey(), RCols.position.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RCols.role.i18nKey(), RCols.role.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RCols.userAttributeKey.i18nKey(), RCols.userAttributeKey.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RCols.userAttributeValue.i18nKey(), RCols.userAttributeValue.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RCols.landingPage.i18nKey(), RCols.landingPage.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RCols.landingPageChooser.i18nKey(), RCols.landingPageChooser.ordinal()));
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("up", -1, "up",
				new StaticFlexiCellRenderer("", "up", "o_icon o_icon-lg o_icon_move_up",translate("up"))));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("down", -1, "down",
				new StaticFlexiCellRenderer("", "down", "o_icon o_icon-lg o_icon_move_down", translate("down"))));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("add", translate("add"), "add"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("delete", translate("delete"), "delete"));

		//pack the rules
		Rules rules = lpModule.getRules();
		List<Rule> ruleList = new ArrayList<>(rules.getRules());
		if(ruleList.isEmpty()) {
			ruleList.add(new Rule());
		}
		
		List<RuleWrapper> wrappers = new ArrayList<>();
		int i = 0;
		for(Rule rule:ruleList) {
			wrappers.add(initRuleWrapper(++i, rule, formLayout));
		}
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("rules", wrappers);
		}
		model = new RulesDataModel(columnsModel, wrappers);
		tableEl = uifactory.addTableElement(getWindowControl(), "rules", model, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
		tableEl.setRendererType(FlexiTableRendererType.classic);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		saveButton = uifactory.addFormSubmitButton("save", buttonLayout);
	}
	
	private RuleWrapper initRuleWrapper(int pos, Rule rule, FormItemContainer formLayout) {
		int i = counter.incrementAndGet();
		
		RuleWrapper wrapper = new RuleWrapper(rule);
		wrapper.setPosition(pos);
		
		SingleSelection roleEl = uifactory.addDropdownSingleselect("role-" + i, null, formLayout, roleKeys, roleValues, null);
		String role = rule.getRole();
		for(int j=roleKeys.length; j-->0; ) {
			if(roleKeys[j].equals(role)) {
				roleEl.select(roleKeys[j], true);
			}
		}
		wrapper.setRoleEl(roleEl);
		
		SingleSelection attrNameEl = uifactory.addDropdownSingleselect("attr-key-" + i, null, formLayout, attrKeys, attrValues, null);
		String userAttributeKey = rule.getUserAttributeKey();
		for(int j=attrKeys.length; j-->0; ) {
			if(attrKeys[j].equals(userAttributeKey)) {
				attrNameEl.select(attrKeys[j], true);
			}
		}
		wrapper.setAttrNameEl(attrNameEl);
		
		TextElement valEl = uifactory.addTextElement("attr-val-" + i, null, 256, "", formLayout);
		valEl.setValue(rule.getUserAttributeValue());
		wrapper.setAttrValueEl(valEl);
		
		TextElement landingPageEl = uifactory.addTextElement("l-page-" + i, null, 256, "", formLayout);
		landingPageEl.setValue(rule.getLandingPath());
		wrapper.setLandingPageEl(landingPageEl);
		formLayout.add(landingPageEl);

		FormLink chooser = uifactory.addFormLink("chooser-" + i, "chooser", RCols.landingPageChooser.i18nKey(), null, formLayout, Link.BUTTON);
		chooser.setIconRightCSS("o_icon o_icon_caret");
		chooser.setUserObject(wrapper);
		wrapper.setLandingPageChooser(chooser);
		return wrapper;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		int rowCount = model.getRowCount();
		List<Rule> ruleList = new ArrayList<>(rowCount);
		for(int i=0; i<rowCount; i++) {
			ruleList.add(model.getObject(i).save());
		}

		Rules rules = new Rules();
		rules.setRules(ruleList);
		lpModule.setRules(rules);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				RuleWrapper row = model.getObject(se.getIndex());
				if("up".equals(se.getCommand())) {
					moveUp(row);
				} else if("down".equals(se.getCommand())) {
					moveDown(row);
				} else if("add".equals(se.getCommand())) {
					addRow(row);
				} else if("delete".equals(se.getCommand())) {
					deleteRow(row);
				}
				tableEl.reset();
				tableEl.getComponent().setDirty(true);
			}
		} else if(source instanceof FormLink && source.getUserObject() instanceof RuleWrapper) {
			RuleWrapper rule = (RuleWrapper)source.getUserObject();
			String cmd = ((FormLink)source).getCmd();
			if("chooser".equals(cmd)) {
				FormLink link = (FormLink)source;
				openChooser(ureq, rule, link);
			}
			saveButton.getComponent().setDirty(false);
			source.getComponent().setDirty(false);
			tableEl.getComponent().setDirty(false);
			flc.setDirty(false);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(chooserCtrl == source) {
			if(event == Event.DONE_EVENT) {
				LandingPages lp = chooserCtrl.getSelectedLandingPage();
				RuleWrapper rule = chooserCtrl.getRow();
				if(lp != null) {
					rule.getLandingPageEl().setValue(lp.businessPath());
					saveButton.getComponent().setDirty(true);
				}
			}
			chooserCalloutCtrl.deactivate();
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(chooserCtrl);
		removeAsListenerAndDispose(chooserCalloutCtrl);
		chooserCtrl = null;
		chooserCalloutCtrl = null;
	}

	private void openChooser(UserRequest ureq, RuleWrapper row, FormLink link) {
		removeAsListenerAndDispose(chooserCtrl);
		removeAsListenerAndDispose(chooserCalloutCtrl);

		chooserCtrl = new ChooserController(ureq, getWindowControl(), row);
		listenTo(chooserCtrl);

		chooserCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				chooserCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(chooserCalloutCtrl);
		chooserCalloutCtrl.activate();
	}
	
	private void addRow(RuleWrapper row) {
		List<RuleWrapper> rows = model.getObjects();
		int currentIndex = rows.indexOf(row);
		RuleWrapper newRule = initRuleWrapper(1, new Rule(), flc);
		if(currentIndex >= 0 && currentIndex < rows.size() - 1) {
			rows.add(currentIndex + 1, newRule);
		} else {
			rows.add(newRule);
		}
		model.setObjects(reOrder(rows));
	}
	
	private void deleteRow(RuleWrapper row) {
		List<RuleWrapper> rows = model.getObjects();
		rows.remove(row);
		if(rows.isEmpty()) {
			Rule rule = new Rule();
			rows.add(initRuleWrapper(1, rule, flc));
		}
		model.setObjects(reOrder(rows));
	}
	
	private void moveUp(RuleWrapper row) {
		List<RuleWrapper> rows = model.getObjects();
		int currentIndex = rows.indexOf(row);
		if(currentIndex > 0) {
			rows.remove(currentIndex);
			rows.add(currentIndex - 1, row);
		}
		model.setObjects(reOrder(rows));
	}
	
	private void moveDown(RuleWrapper row) {
		List<RuleWrapper> rows = model.getObjects();
		int currentIndex = rows.indexOf(row);
		if(currentIndex >= 0 && currentIndex + 1 < rows.size()) {
			rows.remove(currentIndex);
			rows.add(currentIndex + 1, row);
		}
		model.setObjects(reOrder(rows));
	}
	
	private List<RuleWrapper> reOrder(List<RuleWrapper> rows) {
		int i=0;
		for(RuleWrapper row:rows) {
			row.setPosition(++i);
		}
		return rows;
	}
	
	private class ChooserController extends BasicController {
		
		private final RuleWrapper row;
		private LandingPages selectedLandingPage;
		
		public ChooserController(UserRequest ureq, WindowControl wControl, RuleWrapper row) {
			super(ureq, wControl);
			this.row = row;
			VelocityContainer mainVC = createVelocityContainer("chooser");
			
			int i = counter.incrementAndGet();
			List<String> links = new ArrayList<>();
			for(LandingPages lp:LandingPages.values()) {
				String name = lp.name() + "-" + i;
				Link link = LinkFactory.createLink(name, lp.name(), getTranslator(), mainVC, this, Link.LINK | Link.NONTRANSLATED);
				link.setCustomDisplayText(translate(lp.i18nKey()));
				link.setUserObject(lp);
				mainVC.put(name, link);
				links.add(name);
			}
			mainVC.contextPut("links", links);
			putInitialPanel(mainVC);
		}
		
		public RuleWrapper getRow() {
			return row;
		}
		
		public LandingPages getSelectedLandingPage() {
			return selectedLandingPage;
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if(source instanceof Link) {
				Link link = (Link)source;
				if(link.getUserObject() instanceof LandingPages) {
					selectedLandingPage = (LandingPages)link.getUserObject();
				}
			}
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}
}
