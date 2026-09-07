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
package org.olat.resource.accesscontrol.ui;

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.forms.EvaluationFormParticipationStatus;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.repository.ui.RepositoryEntryInfoCardController;
import org.olat.repository.ui.RepositoryEntryInfoHeaderController;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 5 Sep 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class OfferSurveyExecutionInfoCardController extends FormBasicController {

	private RepositoryEntryInfoHeaderController headerCtrl;

	private final EvaluationFormSurvey survey;
	private final String offerLabel;
	private final String orderNr;
	private final Date submissionDate;
	private final EvaluationFormParticipationStatus status;

	@Autowired
	private CurriculumService curriculumService;

	public OfferSurveyExecutionInfoCardController(UserRequest ureq, WindowControl wControl, EvaluationFormSurvey survey,
			String offerLabel, String orderNr, Date submissionDate, EvaluationFormParticipationStatus status) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.survey = survey;
		this.offerLabel = offerLabel;
		this.orderNr = orderNr;
		this.submissionDate = submissionDate;
		this.status = status;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String infoPage = Util.getPackageVelocityRoot(RepositoryEntryInfoCardController.class) + "/repo_info.html";
		FormLayoutContainer infoCont = FormLayoutContainer.createCustomFormLayout("itemsCont", getTranslator(), infoPage);
		infoCont.setRootForm(mainForm);
		formLayout.add(infoCont);

		initHeader(ureq, infoCont);
		initInfos(infoCont);
	}

	private void initHeader(UserRequest ureq, FormItemContainer formLayout) {
		headerCtrl = new RepositoryEntryInfoHeaderController(ureq, getWindowControl(), mainForm);
		listenTo(headerCtrl);
		formLayout.add("header", headerCtrl.getInitialFormItem());

		CurriculumElement curriculumElement = resolveCurriculumElement();
		headerCtrl.setTitle(curriculumElement != null ? curriculumElement.getDisplayName() : survey.getFormEntry().getDisplayname());
		headerCtrl.getExternalRef(curriculumElement != null ? curriculumElement.getIdentifier() : null);
		headerCtrl.setType(translate("offer.survey.execution.implementation"));
	}

	private CurriculumElement resolveCurriculumElement() {
		Long resourceKey = survey.getIdentifier().getOLATResourceable().getResourceableId();
		OLATResource resource = OLATResourceManager.getInstance().findResourceById(resourceKey);
		return resource != null ? curriculumService.getCurriculumElement(resource) : null;
	}

	private void initInfos(FormItemContainer formLayout) {
		String itemPage = Util.getPackageVelocityRoot(RepositoryEntryInfoCardController.class) + "/repo_info_items.html";
		FormLayoutContainer itemsCont = FormLayoutContainer.createCustomFormLayout("itemsCont", getTranslator(), itemPage);
		itemsCont.setRootForm(mainForm);
		formLayout.add("items", itemsCont);

		uifactory.addStaticTextElement("offer.survey.offer.column", offerLabel, itemsCont);
		if (orderNr != null) {
			uifactory.addStaticTextElement("offer.survey.participation.order", orderNr, itemsCont);
		}
		if (submissionDate != null) {
			String formattedDate = Formatter.getInstance(getLocale()).formatDateAndTime(submissionDate);
			uifactory.addStaticTextElement("offer.survey.participation.submission.date", formattedDate, itemsCont);
		}
		uifactory.addStaticTextElement("offer.survey.participation.status", translate(statusI18nKey()), itemsCont);
	}

	private String statusI18nKey() {
		return switch (status) {
			case prepared -> "offer.survey.participation.status.prepared";
			case done -> "offer.survey.participation.status.done";
			case canceled -> "offer.survey.participation.status.canceled";
		};
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

}
