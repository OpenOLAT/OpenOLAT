package org.olat.modules.curriculum.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.curriculum.Curriculum;

/**
 * 
 * Initial date: 19 juin 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditCurriculumOverviewController extends BasicController {

	private TabbedPane tabPane;
	
	private EditCurriculumController editMetadataCtrl;
	private CurriculumUserManagementController userManagementCtrl;
	
	private Curriculum curriculum;
	
	public EditCurriculumOverviewController(UserRequest ureq, WindowControl wControl, Curriculum curriculum) {
		super(ureq, wControl);
		this.curriculum = curriculum;
		
		VelocityContainer mainVC = createVelocityContainer("curriculum_overview");
		
		tabPane = new TabbedPane("tabs", getLocale());
		tabPane.addListener(this);
		
		editMetadataCtrl = new EditCurriculumController(ureq, getWindowControl(), curriculum);
		listenTo(editMetadataCtrl);
		tabPane.addTab(translate("curriculum.metadata"), editMetadataCtrl);
		initTabPane();
		
		mainVC.put("tabs", tabPane);
		putInitialPanel(mainVC);
	}
	
	private void initTabPane() {
		tabPane.addTab(translate("tab.user.management"), uureq -> {
			userManagementCtrl = new CurriculumUserManagementController(uureq, getWindowControl(), curriculum);
			listenTo(userManagementCtrl);
			return userManagementCtrl.getInitialComponent();
		});
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
}
