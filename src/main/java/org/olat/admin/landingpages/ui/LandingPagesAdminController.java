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

import static org.olat.admin.landingpages.model.Rule.ADMIN;
import static org.olat.admin.landingpages.model.Rule.AUTHOR;
import static org.olat.admin.landingpages.model.Rule.GROUP_MGR;
import static org.olat.admin.landingpages.model.Rule.POOL_MGR;
import static org.olat.admin.landingpages.model.Rule.RSRC_MGR;
import static org.olat.admin.landingpages.model.Rule.USER_MGR;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.olat.admin.landingpages.LandingPagesModule;
import org.olat.admin.landingpages.model.Rule;
import org.olat.admin.landingpages.model.Rules;
import org.olat.admin.landingpages.ui.RulesDataModel.RCols;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiColumnModel;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
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
	private static final String[] roleKeys = new String[]{ "none",
		AUTHOR, USER_MGR, GROUP_MGR, RSRC_MGR, POOL_MGR, ADMIN
	};
	private final String[] roleValues;
	private final String[] attrKeys;
	private final String[] attrValues;

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
		
		roleValues = new String[roleKeys.length];
		for(int i=0; i<roleKeys.length; i++) {
			roleValues[i] = translate(roleKeys[i]);
		}
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		List<UserPropertyHandler> userPropertyHandlers = userManager
				.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);
		
		int numOfProperties = userPropertyHandlers.size();
		attrKeys = new String[numOfProperties];
		attrValues = new String[numOfProperties];
		for(int i=0; i<numOfProperties; i++) {
			UserPropertyHandler handler = userPropertyHandlers.get(i);
			attrKeys[i] = handler.getName();
			attrValues[i] = translate(handler.i18nFormElementLabelKey());
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
		
		columnsModel.addFlexiColumnModel(new StaticFlexiColumnModel("up", -1, "up",
				new StaticFlexiCellRenderer(" ", "up", "o_icon_move_up o_icon-lg")));
		columnsModel.addFlexiColumnModel(new StaticFlexiColumnModel("down", -1, "down",
				new StaticFlexiCellRenderer(" ", "down", "o_icon_move_down o_icon-lg")));
		columnsModel.addFlexiColumnModel(new StaticFlexiColumnModel("add", translate("add"), "add"));
		columnsModel.addFlexiColumnModel(new StaticFlexiColumnModel("delete", translate("delete"), "delete"));

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
		uifactory.addFormSubmitButton("save", buttonLayout);
	}
	
	@Override
	protected void doDispose() {
		//
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
		
		DropdownItem chooser = new DropdownItem("chooser-" + i, RCols.landingPageChooser.i18nKey(), getTranslator());
		chooser.setButton(true);
		chooser.setEmbbeded(true);
		fillChooser(wrapper, chooser, formLayout);
		wrapper.setLandingPageChooser(chooser);
		return wrapper;
	}
	
	private void fillChooser(RuleWrapper rule, DropdownItem chooser, FormItemContainer formLayout) {
		int i = counter.incrementAndGet();
		for(LandingPages lp:LandingPages.values()) {
			FormLink link = uifactory.addFormLink(lp.name() + "-" + i, lp.name(), lp.i18nKey(), null, formLayout, Link.LINK);
			link.setUserObject(rule);
			chooser.addElement(link);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		int rowCount = model.getRowCount();
		List<Rule> ruleList = new ArrayList<Rule>(rowCount);
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
			if("catalog".equals(cmd)) {
				//do choose catalog
			} else if("repo".equals(cmd)) {
				
			} else {
				LandingPages lp = LandingPages.landingPageFromCmd(cmd);
				if(lp != null) {
					rule.getLandingPageEl().setValue(lp.businessPath());
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
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
}
