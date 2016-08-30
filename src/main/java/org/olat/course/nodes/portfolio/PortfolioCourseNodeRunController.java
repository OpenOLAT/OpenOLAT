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

package org.olat.course.nodes.portfolio;

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
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseModule;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.nodes.PortfolioCourseNode;
import org.olat.course.nodes.portfolio.PortfolioCourseNodeConfiguration.DeadlineType;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.PortfolioLoggingAction;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.handler.BinderTemplateResource;
import org.olat.portfolio.EPLoggingAction;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.structel.EPStructuredMap;
import org.olat.portfolio.model.structel.PortfolioStructureMap;
import org.olat.repository.RepositoryEntry;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Portfolio run controller. You can take a map if you are in some learning
 * groups of the course. The controller check if there is a deadline for
 * the map and if yes, set it.
 * 
 * <P>
 * Initial Date:  6 oct. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PortfolioCourseNodeRunController extends FormBasicController {

	private PortfolioStructureMap copyMap;
	private PortfolioStructureMap templateMap;
	
	private Binder copyBinder;
	private Binder templateBinder;
	
	private final PortfolioCourseNode courseNode;
	private final ModuleConfiguration config;
	private final OLATResourceable courseOres;
	
	private FormLink newMapLink;
	private FormLink selectMapLink;
	private StaticTextElement newMapMsgEl;
	private FormLayoutContainer infosContainer;
	private FormLayoutContainer assessmentInfosContainer;

	private Formatter formatter;

	private UserCourseEnvironment userCourseEnv;

	private StaticTextElement deadlineDateText;
	
	@Autowired
	private EPFrontendManager ePFMgr;
	@Autowired
	private PortfolioService portfolioService;
	
	public PortfolioCourseNodeRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			PortfolioCourseNode courseNode) {
		super(ureq, wControl, "run");
		
		this.courseNode = courseNode;
		this.config = courseNode.getModuleConfiguration();
		this.userCourseEnv = userCourseEnv;
		
		Long courseResId = userCourseEnv.getCourseEnvironment().getCourseResourceableId();
		courseOres = OresHelper.createOLATResourceableInstance(CourseModule.class, courseResId);
		
		formatter = Formatter.getInstance(getLocale());
		
		RepositoryEntry mapEntry = courseNode.getReferencedRepositoryEntry();
		if(mapEntry != null) {
			if(BinderTemplateResource.TYPE_NAME.equals(mapEntry.getOlatResource().getResourceableTypeName())) {
				templateBinder = portfolioService.getBinderByResource(mapEntry.getOlatResource());
			} else {
				templateMap = (PortfolioStructureMap) ePFMgr.loadPortfolioStructure(mapEntry.getOlatResource());
			}
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
		
		Object text = config.get(PortfolioCourseNodeConfiguration.NODE_TEXT);
		String explanation = (text instanceof String) ? (String)text : "";
		if(StringHelper.containsNonWhitespace(explanation)) {
			uifactory.addStaticTextElement("explanation.text", explanation, infosContainer);
		}
		
		String deadlineconfig = (String)config.get(PortfolioCourseNodeConfiguration.DEADLINE_TYPE);
		if (!DeadlineType.none.name().equals(deadlineconfig) && deadlineconfig!=null){
			// show deadline-config
			String deadLineLabel = "map.deadline." + deadlineconfig + ".label";
			String deadLineInfo = "";
			if (deadlineconfig.equals(DeadlineType.absolut.name())){
				Formatter f = Formatter.getInstance(getLocale());
				deadLineInfo = f.formatDate((Date)config.get(PortfolioCourseNodeConfiguration.DEADLINE_DATE));
			} else {
				deadLineInfo = getDeadlineRelativeInfo();
			}
			deadlineDateText = uifactory.addStaticTextElement("deadline", deadLineLabel, deadLineInfo, infosContainer);			
		}
		
		if(templateMap != null || templateBinder != null) {
			updateUI();
		}
	}
	
	private String getDeadlineRelativeInfo(){
		String[] args = new String[3];
		String month = (String)config.get(PortfolioCourseNodeConfiguration.DEADLINE_MONTH);
		if (StringHelper.containsNonWhitespace(month)) args[0] = translate("map.deadline.info.month", month);
		else args[0] = "";
		String week = (String)config.get(PortfolioCourseNodeConfiguration.DEADLINE_WEEK);
		if (StringHelper.containsNonWhitespace(week)) args[1] = translate("map.deadline.info.week", week);
		else args[1] = "";
		String day = (String)config.get(PortfolioCourseNodeConfiguration.DEADLINE_DAY);
		if (StringHelper.containsNonWhitespace(day)) args[2] = translate("map.deadline.info.day", day);
		else args[2] = "";
		String deadLineInfo = translate("map.deadline.info", args);
		return deadLineInfo;
	}
	
	protected void updateUI() {
		if(templateMap != null) {
			copyMap = ePFMgr.loadPortfolioStructureMap(getIdentity(), templateMap, courseOres, courseNode.getIdent(), null);
		} else if(templateBinder != null) {
			RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			copyBinder = portfolioService.getBinder(getIdentity(), templateBinder, courseEntry, courseNode.getIdent());
		}
		
		if(copyMap == null && copyBinder == null) {
			updateEmptyUI();
		} else {
			updateSelectedUI();
		}	

		if(selectMapLink != null) {
			selectMapLink.setVisible(copyMap != null || copyBinder != null);
		}
		if(newMapLink != null) {
			newMapLink.setVisible(copyMap == null && copyBinder == null);
		}
		if(newMapMsgEl != null) {
			newMapMsgEl.setVisible(copyMap == null && copyBinder == null);
		}
	}
	
	private void updateEmptyUI() {
		String title = "";
		if(templateMap != null) {
			title = StringHelper.escapeHtml(templateMap.getTitle());
		} else if(templateBinder != null) {
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
		
		if(copyMap != null) {
			updateSelectedMapUI();
		} else if(copyBinder != null) {
			updateSelectedBinderUI();
		}
	}

	private void updateSelectedBinderUI() {
		String copyTitle = StringHelper.escapeHtml(copyBinder.getTitle());
		((Link)selectMapLink.getComponent()).setCustomDisplayText(copyTitle);
		
		updateCopyDate(copyBinder.getCopyDate());
		updateAssessmentInfos(copyBinder.getReturnDate());
		updateDeadlineText(copyBinder.getDeadLine());
	}

	private void updateSelectedMapUI() {	
		String copyTitle = StringHelper.escapeHtml(copyMap.getTitle());
		((Link)selectMapLink.getComponent()).setCustomDisplayText(copyTitle);
		
		// show results, when already handed in
		EPStructuredMap structuredMap = (EPStructuredMap)copyMap;
		updateCopyDate(structuredMap.getCopyDate());
		updateAssessmentInfos(structuredMap.getReturnDate());
		updateDeadlineText(structuredMap.getDeadLine());
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

			// Fetch all score and passed and calculate score accounting for the entire course
			ScoreAccounting scoreAccounting = userCourseEnv.getScoreAccounting();
			scoreAccounting.evaluateAll();			
			ScoreEvaluation scoreEval = scoreAccounting.evalCourseNode(courseNode);

			//score
			assessmentInfosContainer.contextPut("hasScoreField", new Boolean(courseNode.hasScoreConfigured()));
			if(courseNode.hasScoreConfigured()) {
				Float score = scoreEval.getScore();
				Float minScore = courseNode.getMinScoreConfiguration();
				Float maxScore = courseNode.getMaxScoreConfiguration();
				assessmentInfosContainer.contextPut("scoreMin", AssessmentHelper.getRoundedScore(minScore));
				assessmentInfosContainer.contextPut("scoreMax", AssessmentHelper.getRoundedScore(maxScore));
				assessmentInfosContainer.contextPut("score", AssessmentHelper.getRoundedScore(score));
			}

			//passed
			assessmentInfosContainer.contextPut("hasPassedField", new Boolean(courseNode.hasPassedConfigured()));
			if(courseNode.hasPassedConfigured()) {
				Boolean passed = scoreEval.getPassed();
				assessmentInfosContainer.contextPut("passed", passed);
				assessmentInfosContainer.contextPut("hasPassedValue", new Boolean(passed != null));
				Float cutValue = courseNode.getCutValueConfiguration();
				assessmentInfosContainer.contextPut("passedCutValue", AssessmentHelper.getRoundedScore(cutValue));
			}

			// get comment
			AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
			String comment = am.getNodeComment(courseNode, getIdentity());
			assessmentInfosContainer.contextPut("hasCommentField", new Boolean(comment != null));
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
			RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			Date deadline = courseNode.getDeadline();
			if(templateMap != null) {
				copyMap = ePFMgr.assignStructuredMapToUser(getIdentity(), templateMap, courseEntry, courseNode.getIdent(), null, deadline);
				if(copyMap != null) {
					showInfo("map.copied", StringHelper.escapeHtml(templateMap.getTitle()));
					ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapPortfolioOres(copyMap));
					ThreadLocalUserActivityLogger.log(EPLoggingAction.EPORTFOLIO_TASK_STARTED, getClass());
				}
			} else if(templateBinder != null) {
				copyBinder = portfolioService.assignBinder(getIdentity(), templateBinder, courseEntry, courseNode.getIdent(), deadline);
				if(copyBinder != null) {
					showInfo("map.copied", StringHelper.escapeHtml(templateBinder.getTitle()));
					ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrap(copyBinder));
					ThreadLocalUserActivityLogger.log(PortfolioLoggingAction.PORTFOLIO_TASK_STARTED, getClass());
				}
			}
			
			updateUI();
		} else if (source == selectMapLink) {
			String resourceUrl;
			if(copyMap != null) {
				resourceUrl = "[HomeSite:" + getIdentity().getKey() + "][Portfolio:0][EPStructuredMap:" + copyMap.getKey() + "]";
			} else if(copyBinder != null) {
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
