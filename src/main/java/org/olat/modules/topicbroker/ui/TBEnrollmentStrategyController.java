/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.topicbroker.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.panel.IconPanelItem;
import org.olat.core.gui.components.panel.IconPanelLabelTextContent;
import org.olat.core.gui.components.panel.IconPanelLabelTextContent.LabelText;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBEnrollmentStrategyConfig;
import org.olat.modules.topicbroker.TBEnrollmentStrategyType;
import org.olat.modules.topicbroker.ui.events.TBEnrollmentProcessRunEvent;

/**
 * 
 * Initial date: Jul 2, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBEnrollmentStrategyController extends FormBasicController {
	
	private IconPanelItem iconPanel;
	private IconPanelLabelTextContent weightinglContent;
	private IconPanelLabelTextContent customContent;
	private FormLink editLink;
	private FormLink editAdditionalLink;
	
	private CloseableModalController cmc;
	private TBEnrollmentStrategyEditController strategyEditCtrl;
	
	private final TBBroker broker;
	private TBEnrollmentStrategyConfig strategyConfig;

	public TBEnrollmentStrategyController(UserRequest ureq, WindowControl wControl, Form form, TBBroker broker) {
		super(ureq, wControl, LAYOUT_CUSTOM, "strategy_config", form);
		this.broker = broker;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		iconPanel = uifactory.addIconPanel("strategy", null, formLayout);
		
		weightinglContent = new IconPanelLabelTextContent("weighting");
		iconPanel.setContent(weightinglContent);
		customContent = new IconPanelLabelTextContent("costom");
		customContent.setColumnWidth(6);
		
		editLink = uifactory.addFormLink("edit", "edit", "enrollment.strategy.edit", null, formLayout, Link.BUTTON);
		editLink.setGhost(true);
		editLink.setIconLeftCSS("o_icon o_icon-fw o_icon_tb_strategy");
		iconPanel.addLink(editLink);
		
		editAdditionalLink = uifactory.addFormLink("edit.additional", "edit.additional", "enrollment.strategy.edit", null, formLayout, Link.BUTTON);
		editAdditionalLink.setGhost(true);
		editAdditionalLink.setIconLeftCSS("o_icon o_icon-fw o_icon_tb_strategy");
		iconPanel.addAdditionalLink(editAdditionalLink);
	}
	
	public TBEnrollmentStrategyConfig getStrategyConfig() {
		return strategyConfig;
	}

	public void setStrategyConfig(TBEnrollmentStrategyConfig strategyConfig) {
		this.strategyConfig = strategyConfig;
		updateUI();
	}

	private void updateUI() {
		iconPanel.setIconCssClass("o_icon " + TBUIFactory.getTypeIconCss(strategyConfig.getType()));
		iconPanel.setTitle(TBUIFactory.getTranslatedType(getTranslator(), strategyConfig.getType()));
		
		List<LabelText> labelTexts = new ArrayList<>(4);
		labelTexts.add(new IconPanelLabelTextContent.LabelText(translate("enrollment.strategy.overview.weighting"), ""));
		labelTexts.add(new IconPanelLabelTextContent.LabelText(translate("enrollment.strategy.overview.enrollments"), TBUIFactory.getTranslatedWeight(getTranslator(), strategyConfig.getMaxEnrollmentsWeight())));
		labelTexts.add(new IconPanelLabelTextContent.LabelText(translate("enrollment.strategy.overview.priorities"), TBUIFactory.getTranslatedWeight(getTranslator(), strategyConfig.getMaxPrioritiesWeight())));
		labelTexts.add(new IconPanelLabelTextContent.LabelText(translate("enrollment.strategy.overview.topics"), TBUIFactory.getTranslatedWeight(getTranslator(), strategyConfig.getMaxTopicsWeight())));
		weightinglContent.setLabelTexts(labelTexts);
		
		boolean custom = TBEnrollmentStrategyType.custom == strategyConfig.getType();
		if (custom) {
			weightinglContent.setColumnWidth(6);
			editLink.setVisible(false);
			iconPanel.setAdditionalContent(customContent);
			editAdditionalLink.setVisible(true);
			
			List<LabelText> customLabelTexts = new ArrayList<>(4);
			customLabelTexts.add(new IconPanelLabelTextContent.LabelText(translate("enrollment.strategy.overview.fine.tuning"), ""));
			customLabelTexts.add(new IconPanelLabelTextContent.LabelText(translate("enrollment.strategy.function.function"), TBUIFactory.getTranslatedFunction(getTranslator(), strategyConfig.getMaxPrioritiesFunction())));
			customLabelTexts.add(new IconPanelLabelTextContent.LabelText(translate("enrollment.strategy.function.break.point"), TBUIFactory.getTranslatedBreakPoint(getTranslator(), strategyConfig.getMaxPriorityBreakPoint())));
			if (strategyConfig.getMaxPrioritiesFunctionAfter() != null) {
				TBUIFactory.getTranslatedFunction(getTranslator(), strategyConfig.getMaxPrioritiesFunctionAfter());
				customLabelTexts.add(new IconPanelLabelTextContent.LabelText(translate("enrollment.strategy.function.function.after"), TBUIFactory.getTranslatedFunction(getTranslator(), strategyConfig.getMaxPrioritiesFunctionAfter())));
			}
			customContent.setLabelTexts(customLabelTexts);
		} else {
			weightinglContent.setColumnWidth(3);
			editLink.setVisible(true);
			iconPanel.setAdditionalContent(null);
			editAdditionalLink.setVisible(false);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (strategyEditCtrl == source) {
			if (event == TBEnrollmentProcessRunEvent.EVENT) {
				setStrategyConfig(strategyEditCtrl.getStrategyConfig());
				fireEvent(ureq, event);
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(strategyEditCtrl);
		removeAsListenerAndDispose(cmc);
		strategyEditCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == editLink || source == editAdditionalLink) {
			doEditStrategyConfig(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doEditStrategyConfig(UserRequest ureq) {
		if (guardModalController(strategyEditCtrl)) return;
		
		strategyEditCtrl = new TBEnrollmentStrategyEditController(ureq, getWindowControl(), broker, strategyConfig);
		listenTo(strategyEditCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				strategyEditCtrl.getInitialComponent(), true, translate("enrollment.strategy.edit"), true);
		listenTo(cmc);
		cmc.activate();
	}

}
