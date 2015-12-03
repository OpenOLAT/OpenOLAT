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

import java.util.Calendar;
import java.util.Date;

import org.olat.NewControllerFactory;
import org.olat.core.CoreSpringFactory;
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
import org.olat.portfolio.EPLoggingAction;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.structel.EPStructuredMap;
import org.olat.portfolio.model.structel.PortfolioStructureMap;
import org.olat.repository.RepositoryEntry;
import org.olat.util.logging.activity.LoggingResourceable;

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
	private final EPFrontendManager ePFMgr;
	
	private final PortfolioCourseNode courseNode;
	private final ModuleConfiguration config;
	private PortfolioStructureMap copy;
	private PortfolioStructureMap template;
	private final OLATResourceable courseOres;
	
	private FormLink newMapLink;
	private FormLink selectMapLink;
	private StaticTextElement newMapMsgEl;
	private FormLayoutContainer infosContainer;
	private FormLayoutContainer assessmentInfosContainer;

	private Formatter formatter;

	private UserCourseEnvironment userCourseEnv;

	private StaticTextElement deadlineDateText;
	
	public PortfolioCourseNodeRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			PortfolioCourseNode courseNode) {
		super(ureq, wControl, "run");
		
		this.courseNode = courseNode;
		this.config = courseNode.getModuleConfiguration();
		this.userCourseEnv = userCourseEnv;
		
		Long courseResId = userCourseEnv.getCourseEnvironment().getCourseResourceableId();
		courseOres = OresHelper.createOLATResourceableInstance(CourseModule.class, courseResId);
		
		ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean("epFrontendManager");
		formatter = Formatter.getInstance(getLocale());
		
		RepositoryEntry mapEntry = courseNode.getReferencedRepositoryEntry();
		if(mapEntry != null) {
			template = (PortfolioStructureMap) ePFMgr.loadPortfolioStructure(mapEntry.getOlatResource());
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
		
		if(template != null) {
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
		copy = ePFMgr.loadPortfolioStructureMap(getIdentity(), template, courseOres, courseNode.getIdent(), null);
		if(copy == null) {
			String title = StringHelper.escapeHtml(template.getTitle());
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
		} else {
			if(selectMapLink == null) {
				selectMapLink = uifactory.addFormLink("select", "select.mymap", "select.mymap", infosContainer, Link.LINK);
				selectMapLink.setElementCssClass("o_sel_ep_select_map");
			} else {
				selectMapLink.setVisible(true);
			}
			String copyTitle = StringHelper.escapeHtml(copy.getTitle());
			((Link)selectMapLink.getComponent()).setCustomDisplayText(copyTitle);
			
			// show results, when already handed in
			EPStructuredMap structuredMap = (EPStructuredMap)copy;
			String copyDate = "";
			if(structuredMap.getCopyDate() != null) {
				copyDate = formatter.formatDateAndTime(structuredMap.getCopyDate());
				uifactory.addStaticTextElement("map.copyDate", copyDate, infosContainer);			
			}
			String returnDate = "";
			if(structuredMap.getReturnDate() != null) {
				returnDate = formatter.formatDateAndTime(structuredMap.getReturnDate());
				uifactory.addStaticTextElement("map.returnDate", returnDate, infosContainer);

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
			// show absolute deadline when task is taken. nothing if taken map still has a deadline configured.
			if (deadlineDateText != null && structuredMap.getDeadLine() != null) {
				String deadline = formatter.formatDateAndTime(structuredMap.getDeadLine());
				deadlineDateText.setValue(deadline);
				deadlineDateText.setLabel("map.deadline.absolut.label", null);
			}			
		}
		
		if(selectMapLink != null) selectMapLink.setVisible(copy != null);
		if(newMapLink != null) newMapLink.setVisible(copy == null);
		if(newMapMsgEl != null) newMapMsgEl.setVisible(copy == null);
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
			copy = ePFMgr.assignStructuredMapToUser(getIdentity(), template, courseOres, courseNode.getIdent(), null, getDeadline());
			if(copy != null) {
				showInfo("map.copied", StringHelper.escapeHtml(template.getTitle()));
				ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapPortfolioOres(copy));
				ThreadLocalUserActivityLogger.log(EPLoggingAction.EPORTFOLIO_TASK_STARTED, getClass());
			}
			updateUI();
		} else if (source == selectMapLink) {
			String resourceUrl = "[HomeSite:" + getIdentity().getKey() + "][Portfolio:0][EPStructuredMap:" + copy.getKey() + "]";
			BusinessControl bc = BusinessControlFactory.getInstance().createFromString(resourceUrl);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
			NewControllerFactory.getInstance().launch(ureq, bwControl);
		}
	}
	
	private Date getDeadline() {
		String type = (String)config.get(PortfolioCourseNodeConfiguration.DEADLINE_TYPE);
		if(StringHelper.containsNonWhitespace(type)) {
			switch(DeadlineType.valueOf(type)) {
				case none: return null;
				case absolut: 
					Date date = (Date)config.get(PortfolioCourseNodeConfiguration.DEADLINE_DATE);
					return date;
				case relative:
					Calendar cal = Calendar.getInstance();
					cal.setTime(new Date());
					boolean applied = applyRelativeToDate(cal, PortfolioCourseNodeConfiguration.DEADLINE_MONTH, Calendar.MONTH, 1);
					applied |= applyRelativeToDate(cal, PortfolioCourseNodeConfiguration.DEADLINE_WEEK, Calendar.DATE, 7);
					applied |= applyRelativeToDate(cal, PortfolioCourseNodeConfiguration.DEADLINE_DAY, Calendar.DATE, 1);
					if(applied) {
						return cal.getTime();
					}
					return null;
				default: return null;
			}
		}
		return null;
	}
	
	private boolean applyRelativeToDate(Calendar cal, String time, int calTime, int factor) {
		String t = (String)config.get(time);
		if(StringHelper.containsNonWhitespace(t)) {
			int timeToApply;
			try {
				timeToApply = Integer.parseInt(t) * factor;
			} catch (NumberFormatException e) {
				logWarn("Not a number: " + t, e);
				return false;
			}
			cal.add(calTime, timeToApply);
			return true;
		}
		return false;
	}
}
