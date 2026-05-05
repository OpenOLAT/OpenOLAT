/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.OrganisationUnit;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.position.TabConfiguration;
import org.olat.modules.selectus.model.position.TabsConfiguration.Tab;
import org.olat.modules.selectus.ui.position.TabsConfigurationDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  13 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ApplicationAppliedController extends BasicController {

	private VelocityContainer mainVC;
	
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;

	public ApplicationAppliedController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("app_applied");
		mainVC.contextPut("officeMail", recruitingModule.getOfficeMail());
		
		putInitialPanel(mainVC);
	}
	
	public void setApplication(Application application) {
		OrganisationUnit organisationSettings = this.recruitingService.getOrganisationUnit(application.getPosition());
		String titleAndName = salutationGenerator.getTitleFullname(application, getLocale());
		String titleLastName = salutationGenerator.getTitleLastName(application, getLocale());
		String positionMail = recruitingModule.getStaffMail(application.getPosition(), organisationSettings);
		String[] names = new String[] {
			titleAndName, 							// 0
			"",										// 1
			application.getPerson().getFirstName(),	// 2
			application.getPerson().getLastName(),	// 3
			titleLastName,							// 4
			positionMail 							// 5
		};
		mainVC.contextPut("names", names);
		
		Position position = application.getPosition();
		TabConfiguration configuration = position.getTabConfiguration(Tab.confirmation);
		String message = configuration == null ? null : configuration.getHelp(getLocale());
		if(StringHelper.containsNonWhitespace(message)) {
			TabsConfigurationDelegate delegate = new TabsConfigurationDelegate(Tab.confirmation);
			message = delegate.render(message, application, salutationGenerator, getTranslator());
			mainVC.contextPut("message", message);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
