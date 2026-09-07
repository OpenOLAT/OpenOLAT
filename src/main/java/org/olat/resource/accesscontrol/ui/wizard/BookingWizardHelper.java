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
package org.olat.resource.accesscontrol.ui.wizard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.ui.EvaluationFormExecutionController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.OfferToSurvey;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.OrderStatus;
import org.olat.resource.accesscontrol.ui.AccessEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Decides whether a booking runs as a wizard, and builds it.
 *
 * Initial date: 2 Sep 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Service
public class BookingWizardHelper {

	@Autowired
	private DB dbInstance;
	@Autowired
	private ACService acService;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private EvaluationFormManager evaluationFormManager;

	/**
	 * @return The forms attached to the offer, ordered by position. Empty if
	 *         the booking of this offer does not need the wizard.
	 */
	public List<OfferToSurvey> loadOrderedForms(Offer offer) {
		List<OfferToSurvey> offerToSurveys = new ArrayList<>(acService.loadOfferToSurveys(offer));
		offerToSurveys.sort(Comparator.comparingInt(OfferToSurvey::getPos));
		return offerToSurveys;
	}

	public BookingContext createBookingContext(OfferAccess offerAccess, Identity bookedIdentity, Identity doer,
			OrderStatus orderStatus, List<OfferToSurvey> offerToSurveys) {
		return new BookingContext(offerAccess, bookedIdentity, doer, orderStatus, offerToSurveys);
	}

	/**
	 * @param titleI18nKey One of the "wizard.title.*" keys of this package,
	 *            picked by the offer's access method, e.g.
	 *            "wizard.title.free" for a freely available offer.
	 */
	public StepsMainRunController startBookingWizard(UserRequest ureq, WindowControl wControl, BookingContext bookingContext,
			Step startStep, String titleI18nKey, String finishButtonText) {
		BookingFinishCallback finish = new BookingFinishCallback(bookingContext);

		String title = buildWizardTitle(ureq, titleI18nKey, bookingContext.getOfferAccess().getOffer());
		StepsMainRunController wizardCtrl = new StepsMainRunController(ureq, wControl, startStep, finish, null, title, "o_ac_booking_wizard");
		wizardCtrl.setFinishText(finishButtonText);
		wizardCtrl.getRunContext().put(BookingContext.RUN_CONTEXT_KEY, bookingContext);
		return wizardCtrl;
	}

	/**
	 * @return ACCESS_OK_EVENT/ACCESS_FAILED_EVENT if the wizard finished, null if it was only closed.
	 */
	public AccessEvent getBookingWizardResult(StepsMainRunController bookingWizardCtrl, Event event) {
		if (event != Event.CHANGED_EVENT && event != Event.DONE_EVENT) {
			return null;
		}
		BookingContext bookingContext = (BookingContext) bookingWizardCtrl.getRunContext().get(BookingContext.RUN_CONTEXT_KEY);
		return bookingContext.isAccessible() ? AccessEvent.ACCESS_OK_EVENT : AccessEvent.ACCESS_FAILED_EVENT;
	}

	private String buildWizardTitle(UserRequest ureq, String titleI18nKey, Offer offer) {
		String implementationTitle = StringHelper.escapeHtml(offer.getResourceDisplayName());
		String reference = loadReference(offer.getResource());
		String subject = implementationTitle;
		if (StringHelper.containsNonWhitespace(reference)) {
			subject += " &middot; <span class=\"o_muted\">" + StringHelper.escapeHtml(reference) + "</span>";
		}
		Translator translator = Util.createPackageTranslator(BookingWizardHelper.class, ureq.getLocale());
		return translator.translate(titleI18nKey, subject);
	}

	private String loadReference(OLATResource resource) {
		CurriculumElement curriculumElement = curriculumService.getCurriculumElement(resource);
		if (curriculumElement != null) {
			return curriculumElement.getIdentifier();
		}
		RepositoryEntry repositoryEntry = repositoryManager.lookupRepositoryEntry(resource, false);
		if (repositoryEntry != null) {
			return repositoryEntry.getExternalRef();
		}
		return null;
	}

	public void saveFormResponses(UserRequest ureq, StepsRunContext runContext, BookingContext bookingContext, Order order, Identity executor) {
		for (OfferToSurvey offerToSurvey : bookingContext.getOfferToSurveys()) {
			EvaluationFormSurvey survey = bookingContext.reload(offerToSurvey.getSurvey());
			EvaluationFormParticipation participation = acService.createOfferSurveyParticipation(survey, order, executor);
			EvaluationFormSession session = evaluationFormManager.createSession(participation);
			EvaluationFormExecutionController executionCtrl = (EvaluationFormExecutionController) runContext.get("form." + survey.getKey());
			executionCtrl.saveResponses(ureq, session);
			evaluationFormManager.finishSession(session);
		}
		dbInstance.commit();
	}

}
