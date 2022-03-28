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
package org.olat.course.nodes.cl.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DownloadLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.CourseLoggingAction;
import org.olat.core.logging.activity.StringResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.prefs.Preferences;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.assessment.ui.tool.AssessmentParticipantViewController;
import org.olat.course.auditing.UserNodeAuditManager;
import org.olat.course.highscore.ui.HighScoreRunController;
import org.olat.course.nodes.CheckListCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.cl.CheckboxManager;
import org.olat.course.nodes.cl.model.Checkbox;
import org.olat.course.nodes.cl.model.CheckboxList;
import org.olat.course.nodes.cl.model.DBCheck;
import org.olat.course.nodes.cl.model.DBCheckbox;
import org.olat.course.nodes.ms.DocumentsMapper;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.Role;
import org.olat.modules.grade.ui.GradeUIFactory;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 04.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CheckListRunController extends FormBasicController implements ControllerEventListener, Activateable2 {
	
	private AssessmentParticipantViewController assessmentParticipantViewCtrl;
	
	private final Date dueDate;
	private final boolean preview;
	private final Boolean closeAfterDueDate;
	private final CheckboxList checkboxList;
	private final AssessmentConfig assessmentConfig;
	
	private static final String[] onKeys = new String[]{ "on" };

	private final ModuleConfiguration config;
	private final CheckListCourseNode courseNode;
	private final OLATResourceable courseOres;
	private final UserCourseEnvironment userCourseEnv;
	
	@Autowired
	private CheckboxManager checkboxManager;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	
	public CheckListRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			OLATResourceable courseOres, CheckListCourseNode courseNode, boolean preview) {
		super(ureq, wControl, "run", Util.createPackageTranslator(CourseNode.class, ureq.getLocale()));
		setTranslator(Util.createPackageTranslator(GradeUIFactory.class, getLocale(), getTranslator()));
		
		this.preview = preview;
		this.courseNode = courseNode;
		this.courseOres = courseOres;
		this.userCourseEnv = userCourseEnv;
		this.assessmentConfig = courseAssessmentService.getAssessmentConfig(courseNode);
		
		config = courseNode.getModuleConfiguration();
		CheckboxList configCheckboxList = (CheckboxList)config.get(CheckListCourseNode.CONFIG_KEY_CHECKBOX);
		if(configCheckboxList == null) {
			checkboxList = new CheckboxList();
			checkboxList.setList(Collections.<Checkbox>emptyList());
		} else {
			checkboxList = configCheckboxList;
		}
		closeAfterDueDate = (Boolean)config.get(CheckListCourseNode.CONFIG_KEY_CLOSE_AFTER_DUE_DATE);
		if(closeAfterDueDate != null && closeAfterDueDate.booleanValue()) {
			dueDate = (Date)config.get(CheckListCourseNode.CONFIG_KEY_DUE_DATE);
		} else {
			dueDate = null;
		}

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		boolean readOnly = isReadOnly();
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("readOnly", Boolean.valueOf(readOnly));
			if(dueDate != null) {
				layoutCont.contextPut("dueDate", dueDate);
				layoutCont.contextPut("in-due-date", isPanelOpen(ureq, "due-date", true));
				if(dueDate.compareTo(new Date()) < 0) {
					layoutCont.contextPut("afterDueDate", Boolean.TRUE);
				}
			}
			
			if (courseNode.getModuleConfiguration().getBooleanSafe(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD,false)){
				HighScoreRunController highScoreCtr = new HighScoreRunController(ureq, getWindowControl(),
						userCourseEnv, courseNode, mainForm);
				if (highScoreCtr.isViewHighscore()) {
					Component highScoreComponent = highScoreCtr.getInitialComponent();
					layoutCont.put("highScore", highScoreComponent);							
				}
			}
			
			List<DBCheck> checks;
			if(preview) {
				checks = new ArrayList<>();
			} else {
				checks = checkboxManager.loadCheck(getIdentity(), courseOres, courseNode.getIdent());
			}
			Map<String, DBCheck> uuidToCheckMap = new HashMap<>();
			for(DBCheck check:checks) {
				uuidToCheckMap.put(check.getCheckbox().getCheckboxId(), check);
			}
			
			List<Checkbox> list = checkboxList.getList();
			List<CheckboxWrapper> wrappers = new ArrayList<>(list.size());
			for(Checkbox checkbox:list) {
				DBCheck check = uuidToCheckMap.get(checkbox.getCheckboxId());
				CheckboxWrapper wrapper = forgeCheckboxWrapper(checkbox, check, readOnly, formLayout);
				layoutCont.add(wrapper.getCheckboxEl());
				wrappers.add(wrapper);
			}
			layoutCont.contextPut("checkboxList", wrappers);
			
			exposeUserDataToVC(ureq, layoutCont);
		}
	}
	
	private void exposeUserDataToVC(UserRequest ureq, FormLayoutContainer layoutCont) {
		AssessmentEvaluation assessmentEval = preview
				? AssessmentEvaluation.EMPTY_EVAL
				: courseAssessmentService.getAssessmentEvaluation(courseNode, userCourseEnv);
		
		removeAsListenerAndDispose(assessmentParticipantViewCtrl);
		assessmentParticipantViewCtrl = new AssessmentParticipantViewController(ureq, getWindowControl(), assessmentEval, assessmentConfig);
		listenTo(assessmentParticipantViewCtrl);
		layoutCont.put("assessment", assessmentParticipantViewCtrl.getInitialComponent());
		
		boolean resultsVisible = assessmentEval.getUserVisible() != null && assessmentEval.getUserVisible().booleanValue();
		if(resultsVisible) {
			if(assessmentConfig.hasComment()) {
				StringBuilder comment = Formatter.stripTabsAndReturns(assessmentEval.getComment());
				layoutCont.contextPut("comment", StringHelper.xssScan(comment));
				layoutCont.contextPut("incomment", isPanelOpen(ureq, "comment", true));
			}
			if(assessmentConfig.hasIndividualAsssessmentDocuments()) {
				List<File> docs = courseAssessmentService.getIndividualAssessmentDocuments(courseNode, userCourseEnv);
				String mapperUri = registerCacheableMapper(ureq, null, new DocumentsMapper(docs));
				layoutCont.contextPut("docsMapperUri", mapperUri);
				layoutCont.contextPut("docs", docs);
				layoutCont.contextPut("inassessmentDocuments", isPanelOpen(ureq, "assessmentDocuments", true));
			}
		} else {
			layoutCont.contextPut("comment", null);
			layoutCont.contextPut("docs", null);
		}
		
		layoutCont.contextPut("withScore", Boolean.valueOf(Mode.none != assessmentConfig.getScoreMode()));
		String infoTextUser = (String) config.get(MSCourseNode.CONFIG_KEY_INFOTEXT_USER);
		if(StringHelper.containsNonWhitespace(infoTextUser)) {
			layoutCont.contextPut(MSCourseNode.CONFIG_KEY_INFOTEXT_USER, infoTextUser);
			layoutCont.contextPut("indisclaimer", isPanelOpen(ureq, "disclaimer", true));
		}
		
		UserNodeAuditManager am = userCourseEnv.getCourseEnvironment().getAuditManager();
		String userLog = preview ? "" : am.getUserNodeLog(courseNode, userCourseEnv.getIdentityEnvironment().getIdentity());
		layoutCont.contextPut("log", StringHelper.escapeHtml(userLog));
	}
	
	private CheckboxWrapper forgeCheckboxWrapper(Checkbox checkbox, DBCheck check, boolean readOnly, FormItemContainer formLayout) {
		String[] values = new String[]{ translate(checkbox.getLabel().i18nKey()) };
		
		boolean canCheck = CheckboxReleaseEnum.userAndCoach.equals(checkbox.getRelease());
		
		String boxId = "box_" + checkbox.getCheckboxId();
		MultipleSelectionElement el = uifactory
				.addCheckboxesHorizontal(boxId, null, formLayout, onKeys, values);
		el.setEnabled(canCheck && !readOnly && !userCourseEnv.isCourseReadOnly());
		el.addActionListener(FormEvent.ONCHANGE);

		DownloadLink downloadLink = null;
		if(StringHelper.containsNonWhitespace(checkbox.getFilename())) {
			VFSContainer container = checkboxManager.getFileContainer(userCourseEnv.getCourseEnvironment(), courseNode);
			VFSItem item = container.resolve(checkbox.getFilename());
			if(item instanceof VFSLeaf) {
				String name = "file_" + checkbox.getCheckboxId();
				downloadLink = uifactory.addDownloadLink(name, checkbox.getFilename(), null, (VFSLeaf)item, formLayout);
			}
		}
		
		CheckboxWrapper wrapper = new CheckboxWrapper(checkbox, downloadLink, el);
		el.setUserObject(wrapper);
		if(check != null && check.getChecked() != null && check.getChecked().booleanValue()) {
			el.select(onKeys[0], true);
			wrapper.setDbCheckbox(check.getCheckbox());
			wrapper.setScore(check.getScore());
		}
		if(downloadLink != null) {
			downloadLink.setUserObject(wrapper);
		}
		
		return wrapper;
	}
	
	private boolean isReadOnly() {
		return (closeAfterDueDate != null && closeAfterDueDate.booleanValue()
				&& dueDate != null && dueDate.before(new Date()));
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof MultipleSelectionElement) {
			MultipleSelectionElement boxEl = (MultipleSelectionElement)source;
			CheckboxWrapper wrapper = (CheckboxWrapper)boxEl.getUserObject();
			if(wrapper != null) {
				boolean checked = boxEl.isAtLeastSelected(1);
				if(doCheck(ureq, wrapper, checked)) {
					fireEvent(ureq, Event.CHANGED_EVENT);
				}
			}
		} else if("ONCLICK".equals(event.getCommand())) {
			String cmd = ureq.getParameter("fcid");
			String panelId = ureq.getParameter("panel");
			if(StringHelper.containsNonWhitespace(cmd) && StringHelper.containsNonWhitespace(panelId)) {
				saveOpenPanel(ureq, panelId, "show".equals(cmd));
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private boolean doCheck(UserRequest ureq, CheckboxWrapper wrapper, boolean checked) {
		if(preview) {
			return false;
		}
		return doPersistCheck(ureq, wrapper, checked);
	}
	
	private boolean doPersistCheck(UserRequest ureq, CheckboxWrapper wrapper, boolean checked) {
		DBCheckbox theOne;
		if(wrapper.getDbCheckbox() == null) {
			String uuid = wrapper.getCheckbox().getCheckboxId();
			theOne = checkboxManager.loadCheckbox(courseOres, courseNode.getIdent(), uuid);
		} else {
			theOne = wrapper.getDbCheckbox();
		}
		
		if(theOne == null) {
			//only warning because this happen in course preview
			logWarn("A checkbox is missing: " + courseOres + " / " + courseNode.getIdent(), null);
		} else {
			Float score;
			if(checked) {
				score = wrapper.getCheckbox().getPoints();
			} else {
				score = 0f;
			}

			checkboxManager.check(theOne, getIdentity(), score, Boolean.valueOf(checked));
			//make sure all results is on the database before calculating some scores
			//manager commit already 
			
			courseNode.updateScoreEvaluation(getIdentity(), userCourseEnv, getIdentity(), Role.user, getLocale());
			
			Checkbox checkbox = wrapper.getCheckbox();
			logUpdateCheck(checkbox.getCheckboxId(), checkbox.getTitle());
		}
		
		exposeUserDataToVC(ureq, flc);
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseNode);
		return Mode.none != assessmentConfig.getScoreMode() || Mode.none != assessmentConfig.getPassedMode();
	}
	
	private void logUpdateCheck(String checkboxId, String boxTitle) {
		ThreadLocalUserActivityLogger.log(CourseLoggingAction.CHECKLIST_CHECK_UPDATED, getClass(), 
				LoggingResourceable.wrapNonOlatResource(StringResourceableType.checkbox, checkboxId, boxTitle));
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//nothin to do
	}
	
	private boolean isPanelOpen(UserRequest ureq, String panelId, boolean def) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		Boolean showConfig  = (Boolean) guiPrefs.get(CheckListRunController.class, getOpenPanelId(panelId));
		return showConfig == null ? def : showConfig.booleanValue();
	}
	
	private void saveOpenPanel(UserRequest ureq, String panelId, boolean newValue) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		if (guiPrefs != null) {
			guiPrefs.commit(CheckListRunController.class, getOpenPanelId(panelId), Boolean.valueOf(newValue));
		}
		flc.getFormItemComponent().getContext().put("in-".concat(panelId), Boolean.valueOf(newValue));
	}
	
	private String getOpenPanelId(String panelId) {
		return panelId + "::" + userCourseEnv.getCourseEnvironment().getCourseResourceableId() + "::" + courseNode.getIdent();
	}
	
	public static class CheckboxWrapper {
		
		private final Checkbox checkbox;
		private final DownloadLink downloadLink;
		private final MultipleSelectionElement checkboxEl;
		private DBCheckbox dbCheckbox;
		private Float score = null;
		
		public CheckboxWrapper(Checkbox checkbox, DownloadLink downloadLink, MultipleSelectionElement checkboxEl) {
			this.checkboxEl = checkboxEl;
			this.downloadLink = downloadLink;
			this.checkbox = checkbox;
		}

		public void setScore(Float score) {
			this.score = score;
		}

		public String getScore() {
			return AssessmentHelper.getRoundedScore(score);
		}

		public Checkbox getCheckbox() {
			return checkbox;
		}
		
		/**
		 * This value is lazy loaded and can be null!
		 * @return
		 */
		public DBCheckbox getDbCheckbox() {
			return dbCheckbox;
		}

		public void setDbCheckbox(DBCheckbox dbCheckbox) {
			this.dbCheckbox = dbCheckbox;
		}

		public String getTitle() {
			return checkbox.getTitle();
		}
		
		public boolean isPointsAvailable() {
			return checkbox.getPoints() != null;
		}
		
		public String getPoints() {
			return AssessmentHelper.getRoundedScore(checkbox.getPoints());
		}
		
		public String getDescription() {
			String desc = StringHelper.xssScan(checkbox.getDescription());
			return Formatter.formatLatexFormulas(desc);
		}
		
		public MultipleSelectionElement getCheckboxEl() {
			return checkboxEl;
		}
		
		public String getCheckboxElName() {
			return checkboxEl.getName();
		}
		
		public boolean hasDownload() {
			return StringHelper.containsNonWhitespace(checkbox.getFilename()) && downloadLink != null;
		}
		
		public String getDownloadName() {
			return downloadLink.getComponent().getComponentName();
		}
	}
}