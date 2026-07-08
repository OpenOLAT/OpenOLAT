/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.position;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.TaggingService;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.ApplicationRefereeStats;
import org.olat.modules.selectus.model.MailLogInfos;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionRole;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.modules.selectus.model.category.ApplicationCategoryInfos;
import org.olat.modules.selectus.ui.PositionApplicationsExcelDataModel;
import org.olat.modules.selectus.ui.PositionApplicationsPDFDataModel;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.events.DeletePositionAnonymousEvent;
import org.olat.modules.selectus.ui.events.DeletePositionPermanentlyEvent;
import org.olat.modules.selectus.ui.rejection.PositionRejectionEmailPdfDataModel;
import org.olat.modules.selectus.ui.resources.ArchiveMediaResource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22.09.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionConfirmDeleteController extends FormBasicController {

	private FormLink deleteLink;
	private FormLink exportLink;
	private FormLink deleteAnonymousButton;

	private Position position;
	private final boolean withAnonymous;
	private final RecruitingPositionSecurityCallback secCallback;
	
	@Autowired
	private TaggingService taggingService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService erFrontendManager;
	
	public PositionConfirmDeleteController(UserRequest ureq, WindowControl wControl,
			Position position, RecruitingPositionSecurityCallback secCallback) {
		super(ureq, wControl, "confirm_delete", Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.position = position;
		this.secCallback = secCallback;
		withAnonymous = recruitingModule.isDeleteAnonymous()
				&& !PositionStatus.reporting.name().equals(position.getStatus());
		initForm(ureq);
	}

	public Position getPosition() {
		return position;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			String shortTitle = position.getMLShortTitle(getLocale());
			String text = translate("confirm.delete", new String[]{ StringHelper.escapeHtml(shortTitle) });
			layoutCont.contextPut("message", text);
			
			if(withAnonymous) {
				layoutCont.contextPut("hint1", translate("delete.position.anonymous.hint1"));
				layoutCont.contextPut("hint2", translate("delete.position.anonymous.hint2"));
			}
		}
		
		String deleteI18n = withAnonymous ? "delete.position.permanently" : "delete";
		deleteLink = uifactory.addFormLink("delete", deleteI18n, null, formLayout, Link.BUTTON);
		deleteLink.setElementCssClass("o_sel_delete_position");
		deleteAnonymousButton = uifactory.addFormLink("delete.anonymous", "delete.position.anonymous", null, formLayout, Link.BUTTON);
		deleteAnonymousButton.setElementCssClass("o_sel_anonym_position");
		deleteAnonymousButton.setVisible(withAnonymous);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		exportLink = uifactory.addFormLink("rejection.backup", formLayout, Link.BUTTON);
		exportLink.setElementCssClass("o_sel_export_backup");
		exportLink.setIconLeftCSS("o_icon o_filetype_pdf");
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(deleteLink == source) {
			if(withAnonymous) {
				fireEvent(ureq, new DeletePositionPermanentlyEvent(position));
			} else {
				doDeletePermanently();
				fireEvent(ureq, Event.DONE_EVENT);
			}
		} else if(deleteAnonymousButton == source) {
			fireEvent(ureq, new DeletePositionAnonymousEvent(position));
		} else if(exportLink == source) {
			exportPdf(ureq);
		}
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	private void exportPdf(UserRequest ureq) {
		List<MailLogInfos> rejectionLog = erFrontendManager.getMailLog(position);
		PositionRejectionEmailPdfDataModel rejectionLogModel = new PositionRejectionEmailPdfDataModel(rejectionLog, getTranslator());

		PositionRole[] ratingRoles = recruitingModule.getRolesAllowedToRate();
		List<ApplicationLight> applications = erFrontendManager.getApplications(position);
		List<Identity> committee = erFrontendManager.getCommittee(position, ratingRoles);
		List<UserRating> ratings = erFrontendManager.getRatings(position, committee);
		
		Map<Long,ApplicationRefereeStats> appKeyToRefereeStats = new HashMap<>();
		if(recruitingModule.isReferenceEnabled()
				&& (position.isExpertRecommendationEnabled() || position.isRefereeRecommendationEnabled()
						|| (recruitingModule.isComparativeAssessmentExpertsEnabled() && position.isComparativeAssessmentExpertEnabled()))) {
			List<ApplicationRefereeStats> refereeStats = erFrontendManager.getApplicationReviewerStats(position);
			for(ApplicationRefereeStats stats:refereeStats) {
				appKeyToRefereeStats.put(stats.getKey(), stats);
			}
		}
		
		Map<Long,List<ApplicationCategoryInfos>> appToCategories = taggingService.getApplicationToCategories(position, true);

		PositionApplicationsPDFDataModel applicationPdfModel
			= new PositionApplicationsPDFDataModel(getIdentity(), position, applications, ratings, getTranslator());
		PositionApplicationsExcelDataModel excelDataModel
			= new PositionApplicationsExcelDataModel(committee.size(), position, applications, ratings, appKeyToRefereeStats,
					null, null, appToCategories, secCallback, getTranslator());

		ArchiveMediaResource resource = new ArchiveMediaResource(getIdentity(), position, rejectionLogModel,
				applicationPdfModel, excelDataModel, getTranslator(), getLocale());
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}
	
	private void doDeletePermanently() {
		position = erFrontendManager.getPosition(position.getKey());
		erFrontendManager.deletePosition(position, getIdentity());
		logAudit("Position deleted: " + position.toStringFull(), null);
	}
}
