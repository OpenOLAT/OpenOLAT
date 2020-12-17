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

package org.olat.modules.portfolio.ui;

import java.util.Date;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.PortfolioLoggingAction;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.handler.BinderTemplateResource;
import org.olat.repository.RepositoryEntry;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BinderPickerController extends FormBasicController {

	private Binder copyBinder;
	private Binder templateBinder;
	private RepositoryEntry templateEntry;
	
	private FormLink newMapLink;
	private FormLink selectMapLink;
	private StaticTextElement newMapMsgEl;
	private FormLayoutContainer infosContainer;
	private FormLayoutContainer assessmentInfosContainer;

	private Formatter formatter;

	private StaticTextElement deadlineDateText;
	
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private PortfolioService portfolioService;
	
	public BinderPickerController(UserRequest ureq, WindowControl wControl, RepositoryEntry templateEntry) {
		super(ureq, wControl, "run");
		this.templateEntry = templateEntry;
		
		formatter = Formatter.getInstance(getLocale());
		if(templateEntry != null && BinderTemplateResource.TYPE_NAME.equals(templateEntry.getOlatResource().getResourceableTypeName())) {
			templateBinder = portfolioService.getBinderByResource(templateEntry.getOlatResource());
		}

		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		infosContainer = FormLayoutContainer.createDefaultFormLayout("infos", getTranslator());
		formLayout.add(infosContainer);
		
		String assessmentPage = velocity_root + "/assessment_infos.html";
		assessmentInfosContainer = FormLayoutContainer.createCustomFormLayout("assessmentInfos", getTranslator(), assessmentPage);
		assessmentInfosContainer.setVisible(false);
		formLayout.add(assessmentInfosContainer);
		
		if(templateBinder != null) {
			updateUI();
		}
	}
	
	protected void updateUI() {
		if(templateBinder != null) {
			copyBinder = portfolioService.getBinder(getIdentity(), templateBinder, templateEntry, null);
		}
		
		if(copyBinder == null) {
			updateEmptyUI();
		} else {
			updateSelectedUI();
		}	

		if(selectMapLink != null) {
			selectMapLink.setVisible(copyBinder != null);
		}
		if(newMapLink != null) {
			newMapLink.setVisible(copyBinder == null);
		}
		if(newMapMsgEl != null) {
			newMapMsgEl.setVisible(copyBinder == null);
		}
	}
	
	private void updateEmptyUI() {
		String title = "";
		if(templateBinder != null) {
			title = StringHelper.escapeHtml(templateBinder.getTitle());
		}

		String msg = translate("map.available", new String[]{ title });
		if(newMapMsgEl == null) {
			newMapMsgEl = uifactory.addStaticTextElement("map.available", msg, infosContainer);
		}
		newMapMsgEl.setLabel(null, null);
		
		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonGroupLayout.setRootForm(mainForm);
		infosContainer.add(buttonGroupLayout);
		if(newMapLink == null) {
			newMapLink = uifactory.addFormLink("map.new", buttonGroupLayout, Link.BUTTON);
			newMapLink.setElementCssClass("o_sel_ep_new_map_template");
		}
	}
	
	private void updateSelectedUI() {
		if(selectMapLink == null) {
			selectMapLink = uifactory.addFormLink("select", "select.mymap", "select.mymap", infosContainer, Link.LINK);
			selectMapLink.setElementCssClass("o_sel_ep_select_map");
		} else {
			selectMapLink.setVisible(true);
		}
		
		if(copyBinder != null) {
			updateSelectedBinderUI();
		}
	}

	private void updateSelectedBinderUI() {
		String copyTitle = StringHelper.escapeHtml(copyBinder.getTitle());
		selectMapLink.getComponent().setCustomDisplayText(copyTitle);
		
		updateCopyDate(copyBinder.getCopyDate());
		updateAssessmentInfos(copyBinder.getReturnDate());
		updateDeadlineText(copyBinder.getDeadLine());
	}

	private void updateCopyDate(Date copyDate) {
		if(copyDate != null) {
			String copyDateStr = formatter.formatDateAndTime(copyDate);
			uifactory.addStaticTextElement("map.copyDate", copyDateStr, infosContainer);			
		}
	}
	
	/**
	 * Show absolute deadline when task is taken. nothing if taken map still has a deadline configured.
	 * @param deadline
	 */
	private void updateDeadlineText(Date deadlineDate) {
		if (deadlineDateText != null && deadlineDate != null) {
			String deadline = formatter.formatDateAndTime(deadlineDate);
			deadlineDateText.setValue(deadline);
			deadlineDateText.setLabel("map.deadline.absolut.label", null);
		}
	}
	
	private void updateAssessmentInfos(Date returnDate) {
		if(returnDate != null) {
			String rDate = formatter.formatDateAndTime(returnDate);
			uifactory.addStaticTextElement("map.returnDate", rDate, infosContainer);

			AssessmentEntry assessmentEntry = assessmentService.loadAssessmentEntry(getIdentity(), templateEntry, null, templateEntry);

			assessmentInfosContainer.contextPut("hasScoreField", Boolean.FALSE);
			/* score
			if(courseNode.hasScoreConfigured()) {
				Float score = scoreEval.getScore();
				Float minScore = courseNode.getMinScoreConfiguration();
				Float maxScore = courseNode.getMaxScoreConfiguration();
				assessmentInfosContainer.contextPut("scoreMin", AssessmentHelper.getRoundedScore(minScore));
				assessmentInfosContainer.contextPut("scoreMax", AssessmentHelper.getRoundedScore(maxScore));
				assessmentInfosContainer.contextPut("score", AssessmentHelper.getRoundedScore(score));
			}
			*/

			//passed
			assessmentInfosContainer.contextPut("hasPassedField", Boolean.TRUE);
			//if(courseNode.hasPassedConfigured()) {
				Boolean passed = assessmentEntry.getPassed();
				assessmentInfosContainer.contextPut("passed", passed);
				assessmentInfosContainer.contextPut("hasPassedValue", Boolean.valueOf(passed != null));
				//Float cutValue = courseNode.getCutValueConfiguration();
				//assessmentInfosContainer.contextPut("passedCutValue", AssessmentHelper.getRoundedScore(cutValue));
			//}

			// get comment
			String comment = assessmentEntry.getComment();
			assessmentInfosContainer.contextPut("hasCommentField", Boolean.valueOf(comment != null));
			if (comment != null) {
				assessmentInfosContainer.contextPut("comment", comment);
			}
			assessmentInfosContainer.setVisible(true);
		} else {
			assessmentInfosContainer.setVisible(false);
		}
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == newMapLink) {
			if(templateBinder != null) {
				copyBinder = portfolioService.assignBinder(getIdentity(), templateBinder, templateEntry, null, null);
				if(copyBinder != null) {
					showInfo("map.copied", StringHelper.escapeHtml(templateBinder.getTitle()));
					ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrap(copyBinder));
					ThreadLocalUserActivityLogger.log(PortfolioLoggingAction.PORTFOLIO_TASK_STARTED, getClass());
				}
			}
			
			updateUI();
		} else if (source == selectMapLink) {
			String resourceUrl;
			if(copyBinder != null) {
				resourceUrl = "[HomeSite:" + getIdentity().getKey() + "][PortfolioV2:0][MyBinders:0][Binder:" + copyBinder.getKey() + "]";
			} else {
				return;
			}
			BusinessControl bc = BusinessControlFactory.getInstance().createFromString(resourceUrl);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
			NewControllerFactory.getInstance().launch(ureq, bwControl);
		}
	}
}
