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
package org.olat.modules.quality.ui;

import static org.olat.modules.quality.ui.QualityUIFactory.formatTopic;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.SessionFilterFactory;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.ui.EvaluationFormFigure;
import org.olat.modules.forms.ui.EvaluationFormFormatter;
import org.olat.modules.forms.ui.EvaluationFormReportsController;
import org.olat.modules.forms.ui.ReportSegment;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionView;
import org.olat.modules.quality.QualityDataCollectionViewSearchParams;
import org.olat.modules.quality.QualitySecurityCallback;
import org.olat.modules.quality.QualityService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20.07.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DataCollectionReportController extends AbstractDataCollectionEditController {
	
	private Controller reportHeaderCtrl;
	private Controller reportsCtrl;
	
	@Autowired
	private QualityService qualityService;
	@Autowired
	private EvaluationFormManager evaluationFormManager;

	public DataCollectionReportController(UserRequest ureq, WindowControl wControl, QualitySecurityCallback secCallback,
			TooledStackedPanel stackPanel, QualityDataCollection dataCollection) {
		super(ureq, wControl, secCallback, stackPanel, dataCollection, "report");
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		QualityDataCollectionViewSearchParams searchParams = new QualityDataCollectionViewSearchParams();
		searchParams.setDataCollectionRef(dataCollection);
		QualityDataCollectionView dataCollectionView = qualityService.loadDataCollections(getTranslator(), searchParams, 0, -1).get(0);
		reportHeaderCtrl = new DataCollectionReportHeaderController(ureq, getWindowControl(), dataCollectionView);
		List<EvaluationFormFigure> figures = new ArrayList<>();
		figures.add(new EvaluationFormFigure(translate("data.collection.figures.title"), dataCollectionView.getTitle()));
		figures.add(new EvaluationFormFigure(translate("data.collection.figures.topic"), formatTopic(dataCollectionView)));
		if (StringHelper.containsNonWhitespace(dataCollectionView.getPreviousTitle())) {
			figures.add(new EvaluationFormFigure(translate("data.collection.figures.previous.title"), dataCollectionView.getPreviousTitle()));
		}
		String period = EvaluationFormFormatter.period(dataCollectionView.getStart(), dataCollectionView.getDeadline(),
				getLocale());
		figures.add(new EvaluationFormFigure(translate("data.collection.figures.period"), period));
		
		EvaluationFormSurvey survey = evaluationFormManager.loadSurvey(dataCollection, null);
		Form form = evaluationFormManager.loadForm(survey.getFormEntry());
		SessionFilter filter = SessionFilterFactory.create(survey);
		
		reportsCtrl = new EvaluationFormReportsController(ureq, getWindowControl(), form,
				filter, ReportSegment.OVERVIEW, reportHeaderCtrl.getInitialComponent(), figures);
		flc.put("report", reportsCtrl.getInitialComponent());
	}
	
	@Override
	protected void updateUI(UserRequest ureq) {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		removeAsListenerAndDispose(reportHeaderCtrl);
		removeAsListenerAndDispose(reportsCtrl);
		reportHeaderCtrl = null;
		reportsCtrl = null;
	}
}
