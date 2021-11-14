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
package org.olat.course.assessment.ui.mode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.Group;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentMode.Status;
import org.olat.course.assessment.AssessmentModeCoordinationService;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupOrder;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This is a simplified version of the assessment mode editor.
 * 
 * Initial date: 7 juin 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentModeForLectureEditController extends FormBasicController {

	private FormLink chooseElementsButton;
	private TextElement nameEl;
	private RichTextElement descriptionEl;
	private StaticTextElement chooseElementsCont;
	
	private CloseableModalController cmc;
	private DialogBoxController confirmCtrl;
	private ChooseElementsController chooseElementsCtrl;
	
	private List<String> elementKeys;
	
	private AssessmentMode assessmentMode;
	private final LectureBlock lectureBlock;
	private final OLATResourceable courseOres;
	
	@Autowired
	private LectureService lectureService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private AssessmentModeManager assessmentModeMgr;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private AssessmentModeCoordinationService modeCoordinationService;
	
	public AssessmentModeForLectureEditController(UserRequest ureq, WindowControl wControl,
			OLATResourceable courseOres, AssessmentMode assessmentMode) {
		super(ureq, wControl);
		this.courseOres = OresHelper.clone(courseOres);
		if(assessmentMode.getKey() == null) {
			this.assessmentMode = assessmentMode;
		} else {
			this.assessmentMode = assessmentModeMgr.getAssessmentModeById(assessmentMode.getKey());
		}
		lectureBlock = this.assessmentMode.getLectureBlock();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_assessment_mode_edit_form");
		setFormContextHelp("Assessment mode");

		if(StringHelper.containsNonWhitespace(assessmentMode.getName())) {
			setFormTitle("form.mode.title", new String[]{ assessmentMode.getName() });
		} else {
			setFormTitle("form.mode.title.add");
		}
		setFormDescription("form.mode.description");
		
		ICourse course = CourseFactory.loadCourse(courseOres);
		if(StringHelper.containsNonWhitespace(assessmentMode.getStartElement())) {
			CourseNode startElement = course.getRunStructure().getNode(assessmentMode.getStartElement());
			if(startElement == null) {
				setFormWarning("warning.missing.start.element");
			}
		}
		
		if(StringHelper.containsNonWhitespace(assessmentMode.getElementList())) {
			String elements = assessmentMode.getElementList();
			for(String element:elements.split(",")) {
				CourseNode node = course.getRunStructure().getNode(element);
				if(node == null) {
					setFormWarning("warning.missing.element");
				}
			}
		}
		
		Status status = assessmentMode.getStatus();
		String name = assessmentMode.getName();
		nameEl = uifactory.addTextElement("mode.name", "mode.name", 255, name, formLayout);
		nameEl.setElementCssClass("o_sel_assessment_mode_name");
		nameEl.setMandatory(true);
		nameEl.setEnabled(status != Status.followup && status != Status.end);
		
		String desc = assessmentMode.getDescription();
		descriptionEl = uifactory.addRichTextElementForStringData("mode.description", "mode.description",
				desc, 6, -1, false, null, null, formLayout, ureq.getUserSession(), getWindowControl());
		descriptionEl.getEditorConfiguration().setPathInStatusBar(false);
		descriptionEl.setEnabled(status != Status.followup && status != Status.end);
		
		String dateAndTime = getDateAndTime();
		uifactory.addStaticTextElement("date.and.time", dateAndTime, formLayout);
		
		String members = getMembers();
		uifactory.addStaticTextElement("mode.target", members, formLayout);
		
		elementKeys = new ArrayList<>();
		StringBuilder elementSb = new StringBuilder();
		if(StringHelper.containsNonWhitespace(assessmentMode.getElementList())) {
			CourseEditorTreeModel treeModel = course.getEditorTreeModel();
			for(String element:assessmentMode.getElementList().split(",")) {
				String courseNodeName = getCourseNodeName(element, treeModel);
				if(StringHelper.containsNonWhitespace(courseNodeName)) {
					elementKeys.add(element);
					if(elementSb.length() > 0) elementSb.append(", ");
					elementSb.append(courseNodeName);
				}
			}
		}
		chooseElementsCont = uifactory.addStaticTextElement("chooseElements", "choose.start.element", elementSb.toString(), formLayout);
		chooseElementsCont.setMandatory(true);

		chooseElementsButton = uifactory.addFormLink("choose.elements", formLayout, Link.BUTTON);
		chooseElementsButton.setEnabled(status != Status.end);

		//ips
		String ipList = assessmentMode.getIpList();
		TextAreaElement ipListEl = uifactory.addTextAreaElement("mode.ips.list", "mode.ips.list", 4096, 4, 60, false, false, ipList, formLayout);
		ipListEl.setVisible(assessmentMode.isRestrictAccessIps());
		ipListEl.setEnabled(false);
		
		String key = assessmentMode.getSafeExamBrowserKey();
		TextAreaElement safeExamBrowserKeyEl = uifactory.addTextAreaElement("safeexamkey", "mode.safeexambrowser.key", 4096, 6, 60, false, false, key, formLayout);
		safeExamBrowserKeyEl.setVisible(assessmentMode.isSafeExamBrowser());
		safeExamBrowserKeyEl.setEnabled(false);
		
		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("button", getTranslator());
		formLayout.add(buttonCont);
		uifactory.addFormCancelButton("cancel", buttonCont, ureq, getWindowControl());
		if(status != Status.end) {
			uifactory.addFormSubmitButton("save", buttonCont);
		}
	}
	
	public AssessmentMode getAssessmentMode() {
		return assessmentMode;
	}
	
	private String getDateAndTime() {
		return new AssessmentModeHelper(getTranslator()).getBeginEndDate(assessmentMode);
	}
	
	private String getMembers() {
		StringBuilder sb = new StringBuilder();
		RepositoryEntry entry = lectureBlock.getEntry();
		List<Group> selectedGroups = lectureService.getLectureBlockToGroups(lectureBlock);
		
		// course
		Group entryBaseGroup = repositoryService.getDefaultGroup(entry);
		if(selectedGroups.contains(entryBaseGroup)) {
			sb.append(translate("mode.target.course", new String[] { StringHelper.escapeHtml(entry.getDisplayname()) }));
		}
		
		// business groups
		int numOfBusinessGroups = 0;
		StringBuilder gpString = new StringBuilder();
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		List<BusinessGroup> businessGroups = businessGroupService.findBusinessGroups(params, entry, 0, -1, BusinessGroupOrder.nameAsc);
		for(BusinessGroup businessGroup:businessGroups) {
			if(selectedGroups.contains(businessGroup.getBaseGroup())) {
				if(gpString.length() > 0) gpString.append(", ");
				gpString.append(StringHelper.escapeHtml(businessGroup.getName()));
				numOfBusinessGroups++;
			}
		}
		
		if(numOfBusinessGroups > 0) {
			if(sb.length() > 0) sb.append(" ");
			String i18n = numOfBusinessGroups == 1 ? "mode.target.business.group" : "mode.target.business.groups";
			sb.append(translate(i18n, new String[] { StringHelper.escapeHtml(sb.toString()) }));
		}
		
		// curriculum elements
		int numOfCurriculums = 0;
		StringBuilder curString = new StringBuilder();
		List<CurriculumElement> elements = curriculumService.getCurriculumElements(entry);
		for(CurriculumElement element:elements) {
			if(selectedGroups.contains(element.getGroup())) {
				if(curString.length() > 0) curString.append(", ");
				curString.append(StringHelper.escapeHtml(element.getDisplayName()));
				numOfCurriculums++;
			}
		}
		
		if(numOfCurriculums > 0) {
			if(sb.length() > 0) sb.append(" ");
			String i18n = numOfCurriculums == 1 ? "mode.target.curriculum.element" : "mode.target.curriculum.elements";
			sb.append(translate(i18n, new String[] { StringHelper.escapeHtml(curString.toString()) }));
		}
		return sb.toString();
	}
	
	private String getCourseNodeName(String ident, CourseEditorTreeModel treeModel) {
		String name = null;
		CourseNode courseNode = treeModel.getCourseNode(ident);
		if(courseNode != null) {
			name = courseNode.getShortTitle();
		}
		return name;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(chooseElementsCtrl == source) {
			if(Event.DONE_EVENT == event || Event.CHANGED_EVENT == event) {
				doSetElements(chooseElementsCtrl.getSelectedKeys());
				flc.setDirty(true);
			}
			cmc.deactivate();
			cleanUp();
		} else if(confirmCtrl == source) {
			if(DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				save(ureq, true);
			}
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(chooseElementsCtrl);
		removeAsListenerAndDispose(cmc);
		chooseElementsCtrl = null;
		cmc = null;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		nameEl.clearError();
		if(StringHelper.containsNonWhitespace(nameEl.getValue())) {
			//too long
		} else {
			nameEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		chooseElementsCont.clearError();
		if(elementKeys.isEmpty()) {
			chooseElementsCont.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Date begin = assessmentMode.getBegin();
		Date end = assessmentMode.getEnd();
		int followupTime = assessmentMode.getFollowupTime();
		int leadTime = assessmentMode.getLeadTime();

		Status currentStatus = assessmentMode.getStatus();

		Status nextStatus = modeCoordinationService.evaluateStatus(begin, leadTime, end, followupTime);
		if(currentStatus == nextStatus) {
			save(ureq, true);
		} else {
			String title = translate("confirm.status.change.title");

			String text;
			switch(nextStatus) {
				case none: text = translate("confirm.status.change.none"); break;
				case leadtime: text = translate("confirm.status.change.leadtime"); break;
				case assessment: text = translate("confirm.status.change.assessment"); break;
				case followup: text = translate("confirm.status.change.followup"); break;
				case end: text = translate("confirm.status.change.end"); break;
				default: text = "ERROR";
			}
			confirmCtrl = activateOkCancelDialog(ureq, title, text, confirmCtrl);
		}
	}
	
	private void save(UserRequest ureq, boolean forceStatus) {
		if(assessmentMode.getKey() != null) {
			assessmentMode = assessmentModeMgr.getAssessmentModeById(assessmentMode.getKey());
		} else {
			AssessmentMode concurrentAssessmentMode = assessmentModeMgr.getAssessmentMode(lectureBlock);
			if(concurrentAssessmentMode != null) {
				assessmentMode = concurrentAssessmentMode;
			}
		}

		assessmentMode.setName(nameEl.getValue());
		assessmentMode.setDescription(descriptionEl.getValue());
		
		String targetKey = AssessmentMode.Target.courseAndGroups.name();// all in once
		assessmentMode.setTargetAudience(AssessmentMode.Target.valueOf(targetKey));

		boolean elementRestrictions = !elementKeys.isEmpty();
		assessmentMode.setRestrictAccessElements(elementRestrictions);
		if(elementRestrictions) {
			StringBuilder sb = new StringBuilder();
			for(String elementKey:elementKeys) {
				if(sb.length() > 0) sb.append(",");
				sb.append(elementKey);
			}
			assessmentMode.setElementList(sb.toString());
		} else {
			assessmentMode.setElementList(null);
		}

		//mode need to be persisted for the following relations
		if(assessmentMode.getKey() == null) {
			assessmentMode = assessmentModeMgr.persist(assessmentMode);
		}
		
		assessmentModeMgr.syncAssessmentModeToLectureBlock(assessmentMode);
		assessmentMode = assessmentModeMgr.merge(assessmentMode, forceStatus);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		 if(chooseElementsButton == source) {
			doChooseElements(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doChooseElements(UserRequest ureq) {
		if(guardModalController(chooseElementsCtrl)) return;

		chooseElementsCtrl = new ChooseElementsController(ureq, getWindowControl(), elementKeys, courseOres);
		listenTo(chooseElementsCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", chooseElementsCtrl.getInitialComponent(),
				true, translate("popup.chooseelements"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doSetElements(List<String> elements) {
		elementKeys = elements;
		
		StringBuilder elementSb = new StringBuilder();
		if(!elementKeys.isEmpty()) {
			ICourse course = CourseFactory.loadCourse(courseOres);
			CourseEditorTreeModel treeModel = course.getEditorTreeModel();
			for(String element:elementKeys) {
				String courseNodeName = getCourseNodeName(element, treeModel);
				if(StringHelper.containsNonWhitespace(courseNodeName)) {
					if(elementSb.length() > 0) elementSb.append(", ");
					elementSb.append(courseNodeName);
				}
			}
		}
		chooseElementsCont.setValue(elementSb.toString());
	}
}