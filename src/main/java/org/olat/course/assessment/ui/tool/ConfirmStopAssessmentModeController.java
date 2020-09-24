package org.olat.course.assessment.ui.tool;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentModeCoordinationService;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.modules.dcompensation.DisadvantageCompensationService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 sept. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmStopAssessmentModeController extends FormBasicController {
	
	private final AssessmentMode mode;

	@Autowired
	private DB dbInstance;
	@Autowired
	private AssessmentModeManager assessmentModeManager;
	@Autowired
	private DisadvantageCompensationService disadvantageCompensationService;
	@Autowired
	private AssessmentModeCoordinationService assessmentModeCoordinationService;
	
	public ConfirmStopAssessmentModeController(UserRequest ureq, WindowControl wControl, AssessmentMode mode) {
		super(ureq, wControl, "confirm_stop");
		this.mode = mode;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			String name = StringHelper.escapeHtml(mode.getName());
			layoutCont.contextPut("msg", translate("confirm.stop.text.details", new String[] { name }));
			int numOfDisadvantagedUsers = hasDisadvantageCompensations();
			if(numOfDisadvantagedUsers == 1) {
				layoutCont.contextPut("compensationMsg", translate("confirm.stop.text.compensations"));
			} else if(numOfDisadvantagedUsers > 1) {
				layoutCont.contextPut("compensationMsg", translate("confirm.stop.text.compensations.plural",
						new String[] { Integer.toString(numOfDisadvantagedUsers) }));
			}
		}

		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("stop", formLayout);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	private int hasDisadvantageCompensations() {
		String nodes = mode.getElementList();
		List<String> nodeList = StringHelper.containsNonWhitespace(nodes) ? Arrays.asList(nodes.split("[,]")) : null;
		List<IdentityRef> disadvantagedIdentities = disadvantageCompensationService
				.getActiveDisadvantagedUsers(mode.getRepositoryEntry(), nodeList);
		Set<Long> assessedIdentityKeys = assessmentModeManager.getAssessedIdentityKeys(mode);
		
		int count = 0;
		for(IdentityRef disadvantagedIdentity:disadvantagedIdentities) {
			if(assessedIdentityKeys.contains(disadvantagedIdentity.getKey())) {
				count++;
			}
		}
		return count;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		AssessmentMode reloadedMode = assessmentModeManager.getAssessmentModeById(mode.getKey());
		assessmentModeCoordinationService.stopAssessment(reloadedMode);
		dbInstance.commit();
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
