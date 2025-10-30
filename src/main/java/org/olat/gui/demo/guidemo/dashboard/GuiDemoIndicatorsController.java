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
package org.olat.gui.demo.guidemo.dashboard;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.indicators.IndicatorsComponent;
import org.olat.core.gui.components.indicators.IndicatorsFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.progressbar.ProgressBar;
import org.olat.core.gui.components.progressbar.ProgressBar.LabelAlignment;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.gui.demo.guidemo.GuiDemoFlexiTablesController;
import org.olat.user.PortraitSize;
import org.olat.user.PortraitUser;
import org.olat.user.UserPortraitComponent;
import org.olat.user.UserPortraitFactory;
import org.olat.user.UserPortraitService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Oct 29, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class GuiDemoIndicatorsController extends BasicController {

	private static final String CMD_INDICATOR = "indicator";
	private VelocityContainer mainVC;
	
	@Autowired
	private UserPortraitService userPortraitService;

	public GuiDemoIndicatorsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(GuiDemoFlexiTablesController.class, getLocale(), getTranslator()));
		
		mainVC = createVelocityContainer("indicators");
		putInitialPanel(mainVC);
		
		createFiguresIndicators();
		createComponentsIndicators();
	}

	private void createFiguresIndicators() {
		IndicatorsComponent figuresComp = IndicatorsFactory.createComponent("figures", mainVC);
		
		Link keyIndicator = createIndicatorLink("3", translate("select.3"), "3");
		figuresComp.setKeyIndicator(keyIndicator);
		
		Link indicatorLink4 = createIndicatorLink("4", translate("select.4"), "4");
		indicatorLink4.setEnabled(false);
		
		List<Component> focusIndicators = List.of(
				createIndicatorLink("1", translate("select.1"), "1"),
				createIndicatorLink("2", translate("select.2"), "22"),
				indicatorLink4,
				createIndicatorLink("5", null, "500"),
				createIndicatorLink("6", translate("select.6"), "6g")
			);
		figuresComp.setFocusIndicators(focusIndicators);
	}
	
	private Link createIndicatorLink(String name, String label, String figure) {
		Link indicatorLink = IndicatorsFactory.createIndicatorLink(name, CMD_INDICATOR, label, figure, this);
		indicatorLink.setAriaRole(Link.ARIA_ROLE_BUTTON);
		indicatorLink.setUserObject(new LabelFigure(label, figure));
		return indicatorLink;
	}

	private void createComponentsIndicators() {
		IndicatorsComponent componentsComp = IndicatorsFactory.createComponent("components", mainVC);
		
		PortraitUser portraitUser = userPortraitService.createPortraitUser(getLocale(), getIdentity());
		
		UserPortraitComponent userPortrait1 = UserPortraitFactory.createUserPortrait("user-portrait-1", mainVC, getLocale());
		userPortrait1.setSize(PortraitSize.large);
		userPortrait1.setPortraitUser(portraitUser);
		componentsComp.setKeyIndicator(userPortrait1);
		
		UserPortraitComponent userPortrait2 = UserPortraitFactory.createUserPortrait("user-portrait-2", mainVC, getLocale());
		userPortrait2.setSize(PortraitSize.medium);
		userPortrait2.setPortraitUser(portraitUser);
		Component withLabelComp = IndicatorsFactory.createIndiatorComponent(translate("indicators.component.with.label"), userPortrait2);
		
		UserPortraitComponent userPortrait3 = UserPortraitFactory.createUserPortrait("user-portrait-3", mainVC, getLocale());
		userPortrait3.setSize(PortraitSize.small);
		userPortrait3.setPortraitUser(portraitUser);
		Link asLinkComp = IndicatorsFactory.createIndicatorLink("3", CMD_INDICATOR, translate("indicators.component.as.link"), userPortrait3, this);
		asLinkComp.setUserObject(new LabelFigure(translate("indicators.component.as.link"), StringHelper.escapeHtml(portraitUser.getDisplayName())));
		
		ProgressBar progressComp = new ProgressBar("progress");
		progressComp.setLabelAlignment(LabelAlignment.none);
		progressComp.setWidthInPercent(true);
		progressComp.setWidth(100);
		progressComp.setMax(100f);
		progressComp.setActual(80f);
		
		componentsComp.setFocusIndicators(List.of(
				withLabelComp,
				asLinkComp,
				progressComp
			));
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link link) {
			if (CMD_INDICATOR.equals(link.getCommand())) {
				if (link.getUserObject() instanceof LabelFigure labelFigure) {
					doShowIndicatorMessage(labelFigure);
				}
			}
		}
	}

	private void doShowIndicatorMessage(LabelFigure labelFigure) {
		showInfo("show.label.value", new String[] { StringHelper.blankIfNull(labelFigure.label()), StringHelper.blankIfNull(labelFigure.figure()) } );
	}
	
	record LabelFigure(String label, String figure) {}


}
