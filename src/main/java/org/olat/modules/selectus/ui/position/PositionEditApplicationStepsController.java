/**

 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.position;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionApplicationAttributeTabEnum;
import org.olat.modules.selectus.model.position.TabConfiguration;
import org.olat.modules.selectus.model.position.TabsConfiguration.Tab;
import org.olat.modules.selectus.ui.PositionController;

/**
 * 
 * 
 * Initial date: 27 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionEditApplicationStepsController extends BasicController implements PositionEditableController {

	private TabbedPane tabPane;
	private Link enableStepsLink;
	
	private int projectTab;
	private int academicalBackgroundTab;

	private Position position;
	private final boolean readOnly;
	
	private final PositionEditRefereesStepController refereesCtrl;
	private PositionDocumentsConfigurationController documentsCtrl;
	private final PositionEditInstructionsStepController instructionsCtrl;
	private final PositionEditDataProtectionStepController dataProtectionCtrl;
	private PositionEditReviewAndSubmitStepController reviewAndSubmitCtrl;
	private PositionEditConfirmationStepController confirmationCtrl;
	
	private PositionEditAdditionalAttributesController projectAttributesCtrl;
	private PositionEditAdditionalAttributesController personalDataAttributesCtrl;
	private PositionEditAdditionalAttributesController academicalBackgroundAttributesCtrl;
	private final List<PositionEditAdditionalAttributesController> customDataAttributesCtrlList = new ArrayList<>();
	
	private CloseableModalController cmc; 
	private PositionEditApplicationEditStepsController enableStepsCtrl;
	
	@Autowired
	private RecruitingModule recruitingModule;
	
	public PositionEditApplicationStepsController(UserRequest ureq, WindowControl wControl, Position position, boolean readOnly) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.position = position;
		this.readOnly = readOnly;
		
		instructionsCtrl = new PositionEditInstructionsStepController(ureq, getWindowControl(), position, readOnly);
		listenTo(instructionsCtrl);
		dataProtectionCtrl = new PositionEditDataProtectionStepController(ureq, getWindowControl(), position);
		listenTo(dataProtectionCtrl);
		personalDataAttributesCtrl = new PositionEditAdditionalAttributesController(ureq, getWindowControl(), position,
				PositionApplicationAttributeTabEnum.personalData, readOnly);
		listenTo(personalDataAttributesCtrl);
		academicalBackgroundAttributesCtrl = new PositionEditAdditionalAttributesController(ureq, getWindowControl(), position,
				PositionApplicationAttributeTabEnum.academicalBackground, readOnly);
		listenTo(academicalBackgroundAttributesCtrl);
		documentsCtrl = new PositionDocumentsConfigurationController(ureq, getWindowControl(), position, readOnly);
		listenTo(documentsCtrl);
		projectAttributesCtrl = new PositionEditAdditionalAttributesController(ureq, getWindowControl(), position,
				PositionApplicationAttributeTabEnum.project, readOnly);
		listenTo(projectAttributesCtrl);
		refereesCtrl = new PositionEditRefereesStepController(ureq, getWindowControl(), position, readOnly);
		listenTo(refereesCtrl);
		reviewAndSubmitCtrl = new PositionEditReviewAndSubmitStepController(ureq, getWindowControl(), position, readOnly);
		listenTo(reviewAndSubmitCtrl);
		confirmationCtrl = new PositionEditConfirmationStepController(ureq, getWindowControl(), position, readOnly);
		listenTo(confirmationCtrl);
		
		tabPane = new TabbedPane("stepsTabPane", getLocale());
		tabPane.setElementCssClass("o_sel_edit_position_steps");
		//TODO selectus tabPane.setRendererType(TabbedPaneRendererType.wizard);
		tabPane.addListener(this);
		
		initTabs(ureq);
		
		VelocityContainer mainVC = createVelocityContainer("edit_application");
		mainVC.put("tabs", tabPane);
		
		enableStepsLink = LinkFactory.createButton("edit.steps", mainVC, this);
		enableStepsLink.setIconLeftCSS("o_icon o_icon_edit");
		mainVC.put("edit.steps", enableStepsLink);
		putInitialPanel(mainVC);
	}
	
	private void initTabs(UserRequest ureq) {
		int selectedPane = tabPane.getSelectedPane();
		tabPane.removeAll();
		
		tabPane.addTab(translate("edit.step.instructions"), instructionsCtrl);
		tabPane.addTab(translate("edit.step.data.protections"), dataProtectionCtrl);
		tabPane.addTab(translate("edit.step.personal.data"), personalDataAttributesCtrl);
		if(recruitingModule.isApplicationAcademicalBackgroundEnabled(position)) {
			academicalBackgroundTab = tabPane.addTab(translate("edit.step.academical.background"), academicalBackgroundAttributesCtrl);
			tabPane.setEnabled(academicalBackgroundTab, position.isApplicationAcademicalBackground());
		}
		if(recruitingModule.isApplicationProjectEnabled() && position.isApplicationProject()) {
			projectTab = tabPane.addTab(translate("edit.step.project"), projectAttributesCtrl);
			tabPane.setEnabled(projectTab, position.isApplicationProject());
		}
		
		customDataAttributesCtrlList.clear();
		if(recruitingModule.isPositionCustomStepsEnabled()) {
			List<Tab> customTabs = position.getCustomTabsList();
			for(Tab tab:customTabs) {
				TabConfiguration configuration = position.getTabConfiguration(tab);
				if(!configuration.isDisabled()) {
					PositionEditAdditionalAttributesController customDataAttributesCtrl = new PositionEditAdditionalAttributesController(ureq, getWindowControl(),
							position, tab.attributesTab(), readOnly);
					listenTo(customDataAttributesCtrl);
					String title = configuration.getTitle();
					if(!StringHelper.containsNonWhitespace(title)) {
						title = configuration.getTitleDe();
					}
					customDataAttributesCtrlList.add(customDataAttributesCtrl);
					int customTab = tabPane.addTab(title, customDataAttributesCtrl);
					if(configuration.isDisabled()) {
						tabPane.setEnabled(customTab, false);
					}
				}
			}
		}
		
		tabPane.addTab(translate("edit.step.documents"), documentsCtrl);
		if(recruitingModule.isReferenceEnabled()) {
			tabPane.addTab(translate("edit.step.references"), refereesCtrl);
		}
		tabPane.addTab(translate("edit.step.review.and.submit"), reviewAndSubmitCtrl);
		tabPane.addTab(translate("edit.step.confirmation"), confirmationCtrl);
		
		if(selectedPane >= 0 && selectedPane < tabPane.getTabCount()
				&& tabPane.isEnabled(selectedPane)) {
			tabPane.setSelectedPane(ureq, selectedPane);
		} else {
			tabPane.setSelectedPane(ureq, 0);
		}
	}
	
	@Override
	public Position getPosition() {
		return position;
	}

	@Override
	public void updatePosition(Position updatedPosition) {
		this.position = updatedPosition;
		
		instructionsCtrl.updatePosition(updatedPosition);
		dataProtectionCtrl.updatePosition(updatedPosition);
		personalDataAttributesCtrl.updatePosition(updatedPosition);
		academicalBackgroundAttributesCtrl.updatePosition(updatedPosition);
		documentsCtrl.updatePosition(updatedPosition);
		projectAttributesCtrl.updatePosition(updatedPosition);
		refereesCtrl.updatePosition(updatedPosition);
		reviewAndSubmitCtrl.updatePosition(updatedPosition);
		confirmationCtrl.updatePosition(updatedPosition);
		for(PositionEditAdditionalAttributesController customDataAttributesCtrl:customDataAttributesCtrlList) {
			customDataAttributesCtrl.updatePosition(updatedPosition);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == enableStepsCtrl) {
			position = enableStepsCtrl.getPosition();
			initTabs(ureq);
			cmc.deactivate();
			cleanUp();
			if(event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				fireEvent(ureq, event);
			}
		} else if(source instanceof PositionEditableController) {
			position = ((PositionEditableController)source).getPosition();
			fireEvent(ureq, event);
		} else if(source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(enableStepsCtrl);
		removeAsListenerAndDispose(cmc);
		enableStepsCtrl = null;
		cmc = null;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(enableStepsLink == source) {
			doEnableSteps(ureq);
		} else if(tabPane == source) {
			if(tabPane.getSelectedController() == instructionsCtrl) {
				instructionsCtrl.updatePosition(position);
			}
		}
	}
	
	private void doEnableSteps(UserRequest ureq) {
		enableStepsCtrl = new PositionEditApplicationEditStepsController(ureq, getWindowControl(), position, readOnly);
		listenTo(enableStepsCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", enableStepsCtrl.getInitialComponent(), translate("edit.steps"));
		listenTo(cmc);
		cmc.activate();
	}
}
