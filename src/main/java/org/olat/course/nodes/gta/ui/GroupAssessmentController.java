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
package org.olat.course.nodes.gta.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.media.FileMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.assessment.ui.tool.AssessmentForm.DocumentWrapper;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.ui.GroupAssessmentModel.Cols;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.ui.AssessmentForm;
import org.olat.modules.grade.GradeModule;
import org.olat.modules.grade.GradeScale;
import org.olat.modules.grade.GradeScoreRange;
import org.olat.modules.grade.GradeService;
import org.olat.modules.grade.ui.GradeUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GroupAssessmentController extends FormBasicController {

	private static final String KEY_VISIBLE = "visible";
	private static final String KEY_HIDDEN = "hidden";
	private static final String[] onKeys = new String[] { "on" };
	private static final String[] onValues = new String[] { "" };
	
	private FlexiTableElement table;
	private GroupAssessmentModel model;
	private FormLink saveAndDoneButton;
	private TextElement groupScoreEl, groupCommentEl;
	private MultipleSelectionElement groupApplyGradeEl;
	private SingleSelection userVisibilityEl;
	private MultipleSelectionElement groupPassedEl, applyToAllEl;
	private FormLayoutContainer groupDocsLayoutCont;
	private FileElement groupUploadDocsEl;
	
	private EditAssessmentDocumentController editAssessmentDocsCtrl;
	private CloseableCalloutWindowController assessmentDocsCalloutCtrl;
	private EditCommentController editCommentCtrl;
	private CloseableCalloutWindowController commentCalloutCtrl;
	
	private final List<UserPropertyHandler> userPropertyHandlers;

	private final UserCourseEnvironment coachCourseEnv;
	private Float cutValue;
	private final boolean withScore, withGrade, withAutoGrade, withPassed, withDocs, withComment;
	private final GTACourseNode gtaNode;
	private final RepositoryEntry courseEntry;
	private final BusinessGroup assessedGroup;
	private File assessmentDocsTmpDir;
	private int counter = 0;
	private final List<Long> duplicateMemberKeys;
	
	
	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private GradeModule gradeModule;
	@Autowired
	private GradeService gradeService;
	
	public GroupAssessmentController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry,
			GTACourseNode courseNode, BusinessGroup assessedGroup, UserCourseEnvironment coachCourseEnv) {
		super(ureq, wControl, "assessment_per_group");
		setTranslator(Util.createPackageTranslator(AssessmentForm.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(GradeUIFactory.class, getLocale(), getTranslator()));
		this.gtaNode = courseNode;
		this.courseEntry = courseEntry;
		this.assessedGroup = assessedGroup;
		this.coachCourseEnv = coachCourseEnv;

		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseNode);
		withScore = Mode.none != assessmentConfig.getScoreMode();
		withGrade = withScore && assessmentConfig.hasGrade() && gradeModule.isEnabled();
		withAutoGrade = withGrade && assessmentConfig.isAutoGrade();
		withPassed = Mode.none != assessmentConfig.getPassedMode();
		if(withPassed && !withGrade) {
			cutValue = assessmentConfig.getCutValue();
		}
		withDocs = assessmentConfig.hasIndividualAsssessmentDocuments();
		if (withDocs) {
			 assessmentDocsTmpDir = FileUtils.createTempDir("gtaassessmentdocs", null, null);
		}
		withComment = assessmentConfig.hasComment();
		
		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(GTACoachedGroupGradingController.USER_PROPS_ID, isAdministrativeUser);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		List<IdentityRef> duplicates = gtaManager.getDuplicatedMemberships(courseNode);
		duplicateMemberKeys = new ArrayList<>(duplicates.size());
		for(IdentityRef duplicate:duplicates) {
			duplicateMemberKeys.add(duplicate.getKey());
		}
		
		initForm(ureq);
		ModelInfos modelInfos = loadModel();
		updateGUI(modelInfos);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		FormLayoutContainer groupGradingCont = FormLayoutContainer.createDefaultFormLayout("groupGrading", getTranslator());
		groupGradingCont.setRootForm(mainForm);
		formLayout.add(groupGradingCont);
		
		applyToAllEl = uifactory.addCheckboxesHorizontal("applytoall", "group.apply.toall", groupGradingCont, onKeys, onValues);
		applyToAllEl.addActionListener(FormEvent.ONCHANGE);
		applyToAllEl.setElementCssClass("o_sel_course_gta_apply_to_all");
		
		if(withScore) {
			String pointVal = "";
			groupScoreEl = uifactory.addTextElement("pointgroup", "group.score", 5, pointVal, groupGradingCont);
			groupScoreEl.setElementCssClass("o_sel_course_gta_group_score");
		}
		
		if(withGrade && !withAutoGrade) {
			groupApplyGradeEl = uifactory.addCheckboxesVertical("grade.apply", groupGradingCont, onKeys, onValues, 1);
		}
		
		if(withPassed && cutValue == null && !withGrade) {
			groupPassedEl = uifactory.addCheckboxesHorizontal("checkgroup", "group.passed", groupGradingCont, onKeys, onValues);
			groupPassedEl.setElementCssClass("o_sel_course_gta_group_passed");
		}
		
		if(withDocs) {
			String mapperUri = registerCacheableMapper(ureq, null, new DocumentMapper());
			String page = velocity_root + "/individual_assessment_docs.html"; 
			groupDocsLayoutCont = FormLayoutContainer.createCustomFormLayout("assessment.docs", getTranslator(), page);
			groupDocsLayoutCont.setLabel("assessment.docs", null);
			groupDocsLayoutCont.contextPut("mapperUri", mapperUri);
			groupGradingCont.add(groupDocsLayoutCont);
			
			groupUploadDocsEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "assessment.docs2", null, groupGradingCont);
			groupUploadDocsEl.addActionListener(FormEvent.ONCHANGE);
		}
		
		if(withComment) {
			String comment = "";
			groupCommentEl = uifactory.addTextAreaElement("usercomment", "group.comment", 2500, 5, 40, true, false, comment, groupGradingCont);
			groupCommentEl.setElementCssClass("o_sel_course_gta_group_comment");
		}
		
		boolean canChangeUserVisibility = coachCourseEnv.isAdmin()
				|| coachCourseEnv.getCourseEnvironment().getRunStructure().getRootNode().getModuleConfiguration().getBooleanSafe(STCourseNode.CONFIG_COACH_USER_VISIBILITY);
		
		if(canChangeUserVisibility && (withPassed || withScore || withComment || withDocs)) {
			SelectionValues visibilitySV = new SelectionValues();
			visibilitySV.add(new SelectionValue(KEY_HIDDEN, translate("user.visibility.hidden"), translate("user.visibility.hidden.desc"), "o_icon o_icon_results_hidden", null, true));
			visibilitySV.add(new SelectionValue(KEY_VISIBLE, translate("user.visibility.visible"), translate("user.visibility.visible.desc"), "o_icon o_icon_results_visible", null, true));
			userVisibilityEl = uifactory.addCardSingleSelectHorizontal("user.visibility.release", groupGradingCont, visibilitySV.keys(),
					visibilitySV.values(), visibilitySV.descriptions(), visibilitySV.icons());
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();

		int i=0;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			int colIndex = GTACoachedGroupGradingController.USER_PROPS_OFFSET + i++;
			if (userPropertyHandler == null) continue;
			
			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(GTACoachedGroupGradingController.USER_PROPS_ID , userPropertyHandler);
			if(visible) {
				FlexiColumnModel col;
				if(UserConstants.FIRSTNAME.equals(propName)
						|| UserConstants.LASTNAME.equals(propName)) {
					col = new DefaultFlexiColumnModel(userPropertyHandler.i18nColumnDescriptorLabelKey(),
							colIndex, userPropertyHandler.getName(), true, propName,
							new StaticFlexiCellRenderer(userPropertyHandler.getName(), new TextFlexiCellRenderer()));
				} else {
					col = new DefaultFlexiColumnModel(true, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, true, propName);
				}
				columnsModel.addFlexiColumnModel(col);
			}
		}
		
		if(withScore) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.scoreEl.i18nKey(), Cols.scoreEl.ordinal()));
		}
		
		if (withGrade && !withAutoGrade) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.applyGradeEl.i18nKey(), Cols.applyGradeEl.ordinal()));
		}
		
		if(withPassed && cutValue == null && !withGrade) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.passedEl.i18nKey(), Cols.passedEl.ordinal()));
		}

		if(withDocs) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.assessmentDocsEl.i18nKey(), Cols.assessmentDocsEl.ordinal()));
		}

		if(withComment) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.commentEl.i18nKey(), Cols.commentEl.ordinal()));
		}
		
		model = new GroupAssessmentModel(userPropertyHandlers, getLocale(), columnsModel);
		table = uifactory.addTableElement(getWindowControl(), "group-list", model, getTranslator(), formLayout);
		table.setCustomizeColumns(true);
		table.setEditMode(true);
		table.setAndLoadPersistedPreferences(ureq, "gtagroup-assessment-v2");

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		saveAndDoneButton = uifactory.addFormLink("save.done", buttonsCont, Link.BUTTON);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	private void updateGUI(ModelInfos modelInfos) {
		if(modelInfos.isSame()) {
			applyToAllEl.select(onKeys[0], true);
			table.setVisible(false);
			model.getObjects()
				.stream()
				.filter(row -> row.getPassedEl() != null)	
				.forEach(row -> row.getPassedEl().setVisible(false));
			
			if(groupPassedEl != null) {
				groupPassedEl.setEvaluationOnlyVisible(true);
				groupPassedEl.setVisible(true);
				Boolean passed = modelInfos.getPassed();
				groupPassedEl.select(onKeys[0], passed != null && passed.booleanValue());
			}
			if(groupScoreEl != null) {
				groupScoreEl.setVisible(true);
				Float score = modelInfos.getScore();
				if(score != null) {
					String scoreVal = AssessmentHelper.getRoundedScore(score);
					groupScoreEl.setValue(scoreVal);
				} else {
					groupScoreEl.setValue("");
				}
			}
			
			if(groupApplyGradeEl != null) {
				groupApplyGradeEl.setVisible(true);
			}
			if(userVisibilityEl != null) {
				userVisibilityEl.setVisible(true);
				if(modelInfos.getUserVisible() == null || modelInfos.getUserVisible().booleanValue()) {
					userVisibilityEl.select(KEY_VISIBLE, true);
				} else {
					userVisibilityEl.select(KEY_HIDDEN, true);
				}
			}
			
			if(groupDocsLayoutCont != null) {
				groupDocsLayoutCont.setVisible(true);
				for (File assessmentDoc : modelInfos.getAssessmentDocs()) {
					File targetFile = new File(assessmentDocsTmpDir, assessmentDoc.getName());
					FileUtils.copyFileToFile(assessmentDoc, targetFile, false);
					updateAssessmentDocsUI();
				}
			}
			if(groupUploadDocsEl != null) {
				groupUploadDocsEl.setVisible(true);
			}
			if(groupCommentEl != null) {
				groupCommentEl.setVisible(true);
				String comment = modelInfos.getComment();
				if(comment != null) {
					groupCommentEl.setValue(comment);
				}
			}
		} else {
			applyToAllEl.select(onKeys[0], false);
			table.setVisible(true);
			model.getObjects()
				.stream()
				.filter(row -> row.getPassedEl() != null)
				.forEach(row -> row.getPassedEl().setVisible(true));
			
			if(groupPassedEl != null) {
				groupPassedEl.setVisible(false);
			}
			if(groupScoreEl != null) {
				groupScoreEl.setVisible(false);
			}
			if(groupApplyGradeEl != null) {
				groupApplyGradeEl.setVisible(false);
			}
			if(userVisibilityEl != null) {
				userVisibilityEl.setVisible(false);
			}
			if(groupDocsLayoutCont != null) {
				groupDocsLayoutCont.setVisible(false);
			}
			if(groupUploadDocsEl != null) {
				groupUploadDocsEl.setVisible(false);
			}
			if(groupCommentEl != null) {
				groupCommentEl.setVisible(false);
			}
		}
		
		if(StringHelper.containsNonWhitespace(modelInfos.getDuplicates())) {
			String warning = translate("error.duplicate.memberships", new String[]{ gtaNode.getShortTitle(), modelInfos.getDuplicates()});
			flc.contextPut("duplicateWarning", warning);
		} else {
			flc.contextRemove("duplicateWarning");
		}
	}
	
	private void updateAssessmentDocsUI() {
		if(groupDocsLayoutCont == null) return;
		
		File[] documents = assessmentDocsTmpDir.listFiles();
		List<DocumentWrapper> wrappers = new ArrayList<>(documents.length);
		for (File document : documents) {
			DocumentWrapper wrapper = new DocumentWrapper(document);
			wrappers.add(wrapper);
			
			FormLink deleteButton = uifactory.addFormLink("delete_doc_" + (++counter), "delete", null, groupDocsLayoutCont, Link.BUTTON_XSMALL);
			deleteButton.setUserObject(wrappers);
			wrapper.setDeleteButton(deleteButton);
		}
		groupDocsLayoutCont.contextPut("documents", wrappers);
	}
	
	private void doDeleteGroupAssessmentDoc(File document) {
		FileUtils.deleteFile(document);
		updateAssessmentDocsUI();
	}
	
	/**
	 * 
	 * @return True if all results are the same
	 */
	private ModelInfos loadModel() {
		//load participants, load datas
		ICourse course = CourseFactory.loadCourse(courseEntry);
		List<Identity> identities = businessGroupService.getMembers(assessedGroup, GroupRoles.participant.name());
		
		Map<Identity, AssessmentEntry> identityToEntryMap = new HashMap<>();
		List<AssessmentEntry> entries = course.getCourseEnvironment()
				.getAssessmentManager().getAssessmentEntries(assessedGroup, gtaNode);
		for(AssessmentEntry entry:entries) {
			identityToEntryMap.put(entry.getIdentity(), entry);
		}
		
		int count = 0;
		boolean same = true;
		StringBuilder duplicateWarning = new StringBuilder();
		Float scoreRef = null;
		Boolean passedRef = null;
		List<File> assessmentDocsRef = null;
		String assessmentdDocsHashRef = null;
		String commentRef = null;
		Boolean userVisibleRef = null;
		
		List<AssessmentRow> rows = new ArrayList<>(identities.size());
		for(Identity identity:identities) {
			AssessmentEntry entry = identityToEntryMap.get(identity);
			
			ScoreEvaluation scoreEval = null;
			if(withScore || withGrade || withPassed) {
				scoreEval = courseAssessmentService.toAssessmentEvaluation(entry, gtaNode);
				if (scoreEval == null) {
					scoreEval = ScoreEvaluation.EMPTY_EVALUATION;
				}
			}
			
			String comment = null;
			if(withComment && entry != null) {
				comment = entry.getComment();
			}

			boolean duplicate = duplicateMemberKeys.contains(identity.getKey());
			if(duplicate) {
				if(duplicateWarning.length() > 0) duplicateWarning.append(", ");
				duplicateWarning.append(StringHelper.escapeHtml(userManager.getUserDisplayName(identity)));
			}

			AssessmentRow row = new AssessmentRow(identity, duplicate);
			rows.add(row);
			
			if(withScore) {
				Float score = scoreEval.getScore();
				String pointVal = AssessmentHelper.getRoundedScore(score);
				TextElement pointEl = uifactory.addTextElement("point" + count, null, 5, pointVal, flc);
				pointEl.setDisplaySize(5);
				row.setScoreEl(pointEl);
				if(count == 0) {
					scoreRef = score;
				} else if(!same(scoreRef, score)) {
					same = false;
				}
			}
			
			if(withGrade) {
				String grade = scoreEval.getGrade();
				row.setGrade(grade);
				
				MultipleSelectionElement gradeEl = uifactory.addCheckboxesHorizontal("grade" + count, null, flc, onKeys, onValues);
				gradeEl.setEvaluationOnlyVisible(true);
				if(StringHelper.containsNonWhitespace(grade)) {
					gradeEl.select(onKeys[0], true);
					gradeEl.setEnabled(false);
				}
				row.setApplyGradeEl(gradeEl);
			}
			
			if(withPassed && cutValue == null) {
				Boolean passed = scoreEval.getPassed();
				MultipleSelectionElement passedEl = uifactory.addCheckboxesHorizontal("check" + count, null, flc, onKeys, onValues);
				passedEl.setEvaluationOnlyVisible(true);
				if(passed != null && passed.booleanValue()) {
					passedEl.select(onKeys[0], passed.booleanValue());
				}
				row.setPassedEl(passedEl);
				if(count == 0) {
					passedRef = passed;
				} else if(!same(passedRef, passed)) {
					same = false;
				}
			}
			
			if(withDocs) {
				FormLink assessmentDocsLink = uifactory.addFormLink("docs-" + CodeHelper.getRAMUniqueID(), "assessment.docs", "assessment.docs", null, flc, Link.LINK);
				assessmentDocsLink.setUserObject(row);
				row.setAssessmentDocsEditLink(assessmentDocsLink);
				
				UserCourseEnvironment userCourseEnv = row.getUserCourseEnvironment(course);
				List<File> currentAssessmentDocs = courseAssessmentService.getIndividualAssessmentDocuments(gtaNode, userCourseEnv);
				String assessmentdDocsHash = getAssessmentdDocsHashRef(currentAssessmentDocs);
				
				if(currentAssessmentDocs.isEmpty()) {
					assessmentDocsLink.setIconLeftCSS("o_icon o_filetype_file");
				} else {
					assessmentDocsLink.setIconLeftCSS("o_icon o_icon_files");
				}
				
				if(count == 0) {
					assessmentDocsRef = currentAssessmentDocs;
					assessmentdDocsHashRef = assessmentdDocsHash;
				} else if(!same(assessmentdDocsHashRef, assessmentdDocsHash)) {
					same = false;
				}
			}
			
			if(withComment) {
				FormLink commentLink = uifactory.addFormLink("comment-" + CodeHelper.getRAMUniqueID(), "comment", "comment", null, flc, Link.LINK);
				if(StringHelper.containsNonWhitespace(comment)) {
					commentLink.setIconLeftCSS("o_icon o_icon_comments");
				} else {
					commentLink.setIconLeftCSS("o_icon o_icon_comments_none");
				}
				commentLink.setUserObject(row);
				row.setComment(comment);
				row.setCommentEditLink(commentLink);

				if(count == 0) {
					commentRef = comment;
				} else if(!same(commentRef, comment)) {
					same = false;
				}
			}
			
			if(withScore || withPassed) {
				Boolean userVisible = scoreEval.getUserVisible();
				if(userVisible == null) {
					userVisible = Boolean.TRUE;
				}
				
				if(count == 0) {
					userVisibleRef = userVisible;
				} else if(!same(userVisibleRef, userVisible)) {
					same = false;
				}
			}
			if (entry != null) {
				row.setUserVisibility(entry.getUserVisibility());
			}
			
			count++;
		}
		
		model.setObjects(rows);
		table.reset();
		
		return new ModelInfos(same, scoreRef, passedRef, assessmentDocsRef, commentRef, userVisibleRef, duplicateWarning.toString());
	}
	
	private boolean same(Object reference, Object value) {
		boolean same = true;
		if((reference == null && value != null)
				|| (reference != null && value == null)
				|| (value != null && reference != null && !value.equals(reference))) {
			same = false;
		}
		return same;
	}
	
	private String getAssessmentdDocsHashRef(List<File> currentAssessmentDocs) {
		// Best effort algorithm to compare by file names and size
		return currentAssessmentDocs.stream()
				.sorted(Comparator.comparing(File::getName))
				.map(file -> file.getName() + "," + file.getName())
				.collect(Collectors.joining(","));
	}

	@Override
	protected void doDispose() {
		if(assessmentDocsTmpDir != null && assessmentDocsTmpDir.exists()) {
			FileUtils.deleteDirsAndFiles(assessmentDocsTmpDir, true, true);
		}
        super.doDispose();
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(assessmentDocsCalloutCtrl == source) {
			cleanUp();
		} else if(editAssessmentDocsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				table.reset();
			}
			assessmentDocsCalloutCtrl.deactivate();
			cleanUp();
		} else if(commentCalloutCtrl == source) {
			cleanUp();
		} else if(editCommentCtrl == source) {
			if(event == Event.DONE_EVENT) {
				table.reset();
			}
			commentCalloutCtrl.deactivate();
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(assessmentDocsCalloutCtrl);
		removeAsListenerAndDispose(editAssessmentDocsCtrl);
		removeAsListenerAndDispose(commentCalloutCtrl);
		removeAsListenerAndDispose(editCommentCtrl);
		assessmentDocsCalloutCtrl = null;
		editAssessmentDocsCtrl = null;
		commentCalloutCtrl = null;
		editCommentCtrl = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(applyToAllEl == source) {
			boolean allGroup = applyToAllEl.isAtLeastSelected(1);
			table.setVisible(!allGroup);
			model.getObjects()
				.stream()
				.filter(row -> row.getPassedEl() != null)	
				.forEach(row -> row.getPassedEl().setVisible(!allGroup));
			
			if(groupPassedEl != null) {
				groupPassedEl.setVisible(allGroup);
			}
			if(groupScoreEl != null) {
				groupScoreEl.setVisible(allGroup);
			}
			if(groupApplyGradeEl != null) {
				groupApplyGradeEl.setVisible(allGroup);
			}
			if(userVisibilityEl != null) {
				userVisibilityEl.setVisible(allGroup);
			}
			if(groupDocsLayoutCont != null) {
				groupDocsLayoutCont.setVisible(allGroup);
			}
			if(groupUploadDocsEl != null) {
				groupUploadDocsEl.setVisible(allGroup);
			}
			if(groupCommentEl != null) {
				groupCommentEl.setVisible(allGroup);
			}
		} else if(groupUploadDocsEl == source) {
			if(groupUploadDocsEl.getUploadFile() != null && StringHelper.containsNonWhitespace(groupUploadDocsEl.getUploadFileName())) {
				groupUploadDocsEl.moveUploadFileTo(assessmentDocsTmpDir);
				updateAssessmentDocsUI();
				groupUploadDocsEl.reset();
			}
		} else if(source == saveAndDoneButton) {
			if(validateFormLogic(ureq)) {
				applyChanges(true);
				fireEvent(ureq, Event.CLOSE_EVENT);
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("assessment.docs".equals(link.getCmd())) {
				AssessmentRow row = (AssessmentRow)link.getUserObject();
				doEditAssessmentDocs(ureq, row);
			} else if("comment".equals(link.getCmd())) {
				AssessmentRow row = (AssessmentRow)link.getUserObject();
				doEditComment(ureq, row);
			} else if(link.getCmd() != null && link.getCmd().startsWith("delete_doc_")) {
				DocumentWrapper wrapper = (DocumentWrapper)link.getUserObject();
				doDeleteGroupAssessmentDoc(wrapper.getDocument());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		if(withScore) {
			if(applyToAllEl.isAtLeastSelected(1)) {
				allOk &= validateScore(groupScoreEl);
			} else {
				List<AssessmentRow> rows = model.getObjects();	
				for(AssessmentRow row:rows) {
					allOk &= validateScore(row.getScoreEl());
				}
			}
		}
	
		return allOk;
	}
	
	private boolean validateScore(TextElement scoreEl) {
		boolean allOk = true;
		
		scoreEl.clearError();
		String value = scoreEl.getValue();
		if(StringHelper.containsNonWhitespace(value)) {
			try {
				float score = Float.parseFloat(value);
				if(score < 0.0f) {
					scoreEl.setErrorKey("error.score.format", null);
					allOk &= false;
				}
			} catch (NumberFormatException e) {
				scoreEl.setErrorKey("error.score.format", null);
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		applyChanges(false);
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void applyChanges(boolean setAsDone) {
		List<AssessmentRow> rows = model.getObjects();
		Boolean userVisible = null;
		if (userVisibilityEl != null) {
			userVisible = Boolean.valueOf(userVisibilityEl.isOneSelected() && userVisibilityEl.isKeySelected(KEY_VISIBLE));
		}
		if(applyToAllEl.isAtLeastSelected(1)) {
			applyChangesForTheWholeGroup(rows, setAsDone, userVisible);
		} else {
			applyChangesForEveryMemberGroup(rows, setAsDone, userVisible);
		}
	}
	
	private void applyChangesForEveryMemberGroup(List<AssessmentRow> rows, boolean setAsDone, Boolean userVisible) {
		ICourse course = CourseFactory.loadCourse(courseEntry);

		NavigableSet<GradeScoreRange> gradeScoreRanges = null;
		if (withGrade) {
			GradeScale gradeScale = gradeService.getGradeScale(courseEntry, gtaNode.getIdent());
			gradeScoreRanges = gradeService.getGradeScoreRanges(gradeScale, getLocale());
		}
		
		for(AssessmentRow row:rows) {
			UserCourseEnvironment userCourseEnv = row.getUserCourseEnvironment(course);
			
			Float score = null;
			if(withScore) {
				String value = row.getScoreEl().getValue();
				if(StringHelper.containsNonWhitespace(value)) {
					score = Float.parseFloat(value);
				}
			}
			
			Boolean passed = null;
			String grade = null;
			String performanceClassIdent = null;
			if(withGrade && score != null && (withAutoGrade || row.getApplyGradeEl().isAtLeastSelected(1))) {
				GradeScoreRange gradeScoreRange = gradeService.getGradeScoreRange(gradeScoreRanges, score);
				grade = gradeScoreRange.getGrade();
				performanceClassIdent = gradeScoreRange.getPerformanceClassIdent();
				if (withPassed) {
					passed = Boolean.valueOf(gradeScoreRange.isPassed());
				}
				
			}
			
			if(withPassed && !withGrade) {
				if(cutValue == null) {
					passed = row.getPassedEl().isSelected(0);
				} else if(score != null) {
					passed = (score.floatValue() >= cutValue.floatValue()) ? Boolean.TRUE	: Boolean.FALSE;
				}
			}
			
			Boolean newUserVisible = userVisible != null? userVisible: row.getUserVisibility();
			ScoreEvaluation newScoreEval;
			if(setAsDone) {
				newScoreEval = new ScoreEvaluation(score, grade, performanceClassIdent, passed, AssessmentEntryStatus.done, newUserVisible, null, null,null, null);
			} else {
				newScoreEval = new ScoreEvaluation(score, grade, performanceClassIdent, passed, null, newUserVisible, null, null, null, null);
			}
			courseAssessmentService.updateScoreEvaluation(gtaNode, newScoreEval, userCourseEnv, getIdentity(), false, Role.coach);
			
			if(withComment) {
				String comment = row.getComment();
				if(StringHelper.containsNonWhitespace(comment)) {
					courseAssessmentService.updatedUserComment(gtaNode, comment, userCourseEnv, getIdentity());
				}
			}
		}
	}
	
	private void applyChangesForTheWholeGroup(List<AssessmentRow> rows, boolean setAsDone, Boolean userVisible) {
		ICourse course = CourseFactory.loadCourse(courseEntry);
		
		Float score = null;
		if(withScore) {
			String scoreValue = groupScoreEl.getValue();
			if(StringHelper.containsNonWhitespace(scoreValue)) {
				score = Float.parseFloat(scoreValue);
			}
		}
		
		GradeScoreRange gradeScoreRange = null;
		if(withGrade && score != null) {
			GradeScale gradeScale = gradeService.getGradeScale(courseEntry, gtaNode.getIdent());
			NavigableSet<GradeScoreRange> gradeScoreRanges = gradeService.getGradeScoreRanges(gradeScale, getLocale());
			gradeScoreRange = gradeService.getGradeScoreRange(gradeScoreRanges, score);
		}
		
		Boolean groupPassed = null;
		if(withPassed && !withGrade) {
			if(cutValue == null) {
				groupPassed = groupPassedEl.isSelected(0);
			} else if(score != null) {
				groupPassed = (score.floatValue() >= cutValue.floatValue()) ? Boolean.TRUE : Boolean.FALSE;
			}
		}

		for(AssessmentRow row:rows) {
			String grade = null;
			String performanceClassIdent = null;
			Boolean passed = groupPassed;
			if (withGrade && gradeScoreRange != null 
					&& (withAutoGrade || groupApplyGradeEl.isAtLeastSelected(1) || StringHelper.containsNonWhitespace(row.getGrade()))) {
				grade = gradeScoreRange.getGrade();
				performanceClassIdent = gradeScoreRange.getPerformanceClassIdent();
				if (withPassed) {
					groupPassed = Boolean.valueOf(gradeScoreRange.isPassed());
				}
			}
			
			Boolean newUserVisible = userVisible != null? userVisible: row.getUserVisibility();
			UserCourseEnvironment userCourseEnv = row.getUserCourseEnvironment(course);
			ScoreEvaluation newScoreEval;
			if(setAsDone) {
				newScoreEval = new ScoreEvaluation(score, grade, performanceClassIdent, passed, AssessmentEntryStatus.done, newUserVisible, null, null, null, null);
			} else {
				newScoreEval = new ScoreEvaluation(score, grade, performanceClassIdent, passed, null, newUserVisible, null, null, null, null);
			}
			courseAssessmentService.updateScoreEvaluation(gtaNode, newScoreEval, userCourseEnv, getIdentity(), false, Role.coach);
		}
		
		if (withDocs) {
			File[] assessmentDocs = assessmentDocsTmpDir.listFiles();
			for (AssessmentRow row:rows) {
				UserCourseEnvironment userCourseEnv = row.getUserCourseEnvironment(course);
				List<File> currentAssessmentDocs = courseAssessmentService.getIndividualAssessmentDocuments(gtaNode, userCourseEnv);
				for (File currentAssessmentDoc : currentAssessmentDocs) {
					courseAssessmentService.removeIndividualAssessmentDocument(gtaNode, currentAssessmentDoc,
							userCourseEnv, getIdentity());
				}
				for (File assessmentDoc : assessmentDocs) {
					courseAssessmentService.addIndividualAssessmentDocument(gtaNode, assessmentDoc,
							assessmentDoc.getName(), userCourseEnv, getIdentity());
				}
			}
		}
		
		if(withComment) {
			String comment = groupCommentEl.getValue();
			if(comment != null) {
				for(AssessmentRow row:rows) {
					UserCourseEnvironment userCourseEnv = row.getUserCourseEnvironment(course);
					courseAssessmentService.updatedUserComment(gtaNode, comment, userCourseEnv, getIdentity());
				}
			}
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doEditAssessmentDocs(UserRequest ureq, AssessmentRow row) {
		removeAsListenerAndDispose(assessmentDocsCalloutCtrl);
		
		OLATResource courseOres = courseEntry.getOlatResource();
		editAssessmentDocsCtrl = new EditAssessmentDocumentController(ureq, getWindowControl(), courseOres, gtaNode, row, false);
		listenTo(editAssessmentDocsCtrl);
		assessmentDocsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				editAssessmentDocsCtrl.getInitialComponent(), row.getAssessmentDocsEditLink().getFormDispatchId(),
				"", true, "");
		listenTo(assessmentDocsCalloutCtrl);
		assessmentDocsCalloutCtrl.activate();
	}
	
	private void doEditComment(UserRequest ureq, AssessmentRow row) {
		removeAsListenerAndDispose(commentCalloutCtrl);
		
		OLATResource courseOres = courseEntry.getOlatResource();
		editCommentCtrl = new EditCommentController(ureq, getWindowControl(), courseOres, gtaNode, row);
		listenTo(editCommentCtrl);
		commentCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				editCommentCtrl.getInitialComponent(), row.getCommentEditLink().getFormDispatchId(),
				"", true, "");
		listenTo(commentCalloutCtrl);
		commentCalloutCtrl.activate();
	}
	
	public static class ModelInfos {
		
		private final String duplicates;
		private final boolean same;
		private final Float score;
		private final Boolean passed;
		private final List<File> assessmentDocs;
		private final String comment;
		private final Boolean userVisible;
		
		public ModelInfos(boolean same, Float score, Boolean passed, List<File> assessmentDocs, String comment,
				Boolean userVisible, String duplicates) {
			this.same = same;
			this.score = score;
			this.passed = passed;
			this.assessmentDocs = assessmentDocs;
			this.comment = comment;
			this.userVisible = userVisible;
			this.duplicates = duplicates;
		}

		public boolean isSame() {
			return same;
		}

		public Float getScore() {
			return score;
		}

		public Boolean getPassed() {
			return passed;
		}
		
		public List<File> getAssessmentDocs() {
			return assessmentDocs;
		}

		public String getComment() {
			return comment;
		}

		public Boolean getUserVisible() {
			return userVisible;
		}

		public String getDuplicates() {
			return duplicates;
		}
	}
	
	public class DocumentMapper implements Mapper {

		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			if(StringHelper.containsNonWhitespace(relPath)) {
				if(relPath.startsWith("/")) {
					relPath = relPath.substring(1, relPath.length());
				}
			
				@SuppressWarnings("unchecked")
				List<DocumentWrapper> wrappers = (List<DocumentWrapper>)groupDocsLayoutCont.contextGet("documents");
				if(wrappers != null) {
					for(DocumentWrapper wrapper:wrappers) {
						if(relPath.equals(wrapper.getFilename())) {
							return new FileMediaResource(wrapper.getDocument(), true);
						}
					}
				}
			}
			return new NotFoundMediaResource();
		}
	}
}
