/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.course.nodes.portfolio;

import java.util.Date;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.DTabs;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseModule;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.nodes.PortfolioCourseNode;
import org.olat.course.nodes.portfolio.PortfolioCourseNodeConfiguration.DeadlineType;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.home.site.HomeSite;
import org.olat.modules.ModuleConfiguration;
import org.olat.portfolio.EPLoggingAction;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.structel.EPStructuredMap;
import org.olat.portfolio.model.structel.PortfolioStructureMap;
import org.olat.repository.RepositoryEntry;
import org.olat.util.logging.activity.LoggingResourceable;

import com.ibm.icu.util.Calendar;

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

	private Formatter formatter;

	private UserCourseEnvironment userCourseEnv;

	private StaticTextElement deadlineDateText;
	
	public PortfolioCourseNodeRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			NodeEvaluation ne, PortfolioCourseNode courseNode) {
		super(ureq, wControl);
		
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
		Object text = config.get(PortfolioCourseNodeConfiguration.NODE_TEXT);
		String explanation = (text instanceof String) ? (String)text : "";
		uifactory.addStaticTextElement("explanation.text", explanation, flc);
		
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
			deadlineDateText = uifactory.addStaticTextElement("deadline", deadLineLabel, deadLineInfo, formLayout);			
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
			String title = template.getTitle();
			String msg = translate("map.available", new String[]{ title });
			if(newMapMsgEl == null) {
				newMapMsgEl = uifactory.addStaticTextElement("map.available", msg, flc);
			}
			newMapMsgEl.setLabel(null, null);
			
			FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			buttonGroupLayout.setRootForm(mainForm);
			flc.add(buttonGroupLayout);
			if(newMapLink == null) {
				newMapLink = uifactory.addFormLink("map.new", buttonGroupLayout, Link.BUTTON);
			}
		} else {
			if(selectMapLink == null) {
				selectMapLink = uifactory.addFormLink("select", "select.mymap", "select.mymap", flc, Link.LINK);
			} else {
				selectMapLink.setVisible(true);
			}
			((Link)selectMapLink.getComponent()).setCustomDisplayText(copy.getTitle());
			
			// show results, when already handed in
			EPStructuredMap structuredMap = (EPStructuredMap)copy;
			String copyDate = "";
			if(structuredMap.getCopyDate() != null) {
				copyDate = formatter.formatDateAndTime(structuredMap.getCopyDate());
				uifactory.addStaticTextElement("map.copyDate", copyDate, flc);			
			}
			String returnDate = "";
			if(structuredMap.getReturnDate() != null) {
				returnDate = formatter.formatDateAndTime(structuredMap.getReturnDate());
				uifactory.addStaticTextElement("map.returnDate", returnDate, flc);

				SpacerElement spacer = uifactory.addSpacerElement("space", flc, false);
				// show rating
				// create an identenv with no roles, no attributes, no locale
				IdentityEnvironment ienv = new IdentityEnvironment();
				ienv.setIdentity(getIdentity());
				// Fetch all score and passed and calculate score accounting for the entire course
				ScoreAccounting scoreAccounting = userCourseEnv.getScoreAccounting();
				scoreAccounting.evaluateAll();			
				ScoreEvaluation scoreEval = scoreAccounting.evalCourseNode(courseNode);
				Float score = scoreEval.getScore();
				if (score != null) uifactory.addStaticTextElement("map.score", String.valueOf(score), flc);
				Boolean passed = scoreEval.getPassed();
				if (passed==null) uifactory.addStaticTextElement("map.not.rated.yet", "", flc);
				else if (passed) uifactory.addStaticTextElement("map.passed", "", flc);
				else uifactory.addStaticTextElement("map.not.passed", "", flc);
				
				// get comment
				AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
				String comment = am.getNodeComment(courseNode, getIdentity());
				if (comment != null) uifactory.addStaticTextElement("map.comment", comment, flc);
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
				showInfo("map.copied", template.getTitle());
				ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapPortfolioOres(copy));
				ThreadLocalUserActivityLogger.log(EPLoggingAction.EPORTFOLIO_TASK_STARTED, getClass());
			}
			updateUI();
		} else if (source == selectMapLink) {
			String activationCmd = copy.getClass().getSimpleName() + ":" + copy.getResourceableId();
			DTabs dts = (DTabs)Windows.getWindows(ureq).getWindow(ureq).getAttribute("DTabs");
			dts.activateStatic(ureq, HomeSite.class.getName(), activationCmd);
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
