/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.ui;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.FormSection;
import org.olat.core.gui.components.form.flexible.impl.elements.DeleteFileElementEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.TextMode;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.creditpoint.CreditPointModule;
import org.olat.modules.creditpoint.CreditPointService;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.creditpoint.CreditPointSystemStatus;
import org.olat.modules.creditpoint.CurriculumElementCreditPointConfiguration;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementFileType;
import org.olat.modules.curriculum.CurriculumElementManagedFlag;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.TaughtBy;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LecturesBlockSearchParameters;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.author.MediaContainerFilter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Nov 26, 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class EditCurriculumElementInfosController extends FormBasicController {
	
	private static final Set<String> imageMimeTypes = Set.of("image/gif", "image/jpg", "image/jpeg", "image/png");
	private static final Set<String> videoMimeTypes = Set.of("video/mp4");
	private static final int picUploadlimitKB = 5120;
	private static final int movieUploadlimitKB = 102400;
	
	private static final String OUTLINE_KEY = "outline";
	private static final String EVENTS_KEY = "events";
	private static final String MEET_TEACHERS_KEY = "meetteachers";
	private static final String CERTIFICATE_KEY = "certificate";
	private static final String CREDIT_POINTS_KEY = "creditpoints";

	private TextElement teaserEl;
	private FileElement imageEl;
	private FormToggle withMovieEl;
	private RichTextElement descriptionEl;
	private TextElement authorsEl;
	private TextElement mainLanguageEl;
	private TextElement expenditureOfWorkEl;
	private MultipleSelectionElement showInfoEl;
	private MultipleSelectionElement taughtByEl;
	private TextElement creditPointsEl;
	private SingleSelection creditPointSystemEl;
	private FormLayoutContainer creditPointCont;
	private RichTextElement objectivesEl;
	private RichTextElement requirementsEl;
	private RichTextElement creditsEl;
	private FileElement videoEl;
	
	private final boolean canEdit;
	private CurriculumElement element;
	private final boolean isRootElement;
	private final List<CreditPointSystem> systems;
	private CurriculumElementCreditPointConfiguration creditPointConfig;
	private VFSContainer mediaContainer;
	
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private CreditPointModule creditPointModule;
	@Autowired
	private CreditPointService creditPointService;

	public EditCurriculumElementInfosController(UserRequest ureq, WindowControl wControl, CurriculumElement element,
			CurriculumSecurityCallback secCallback) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.element = element;
		this.isRootElement = element.getParent() == null;

		canEdit = secCallback.canEditCurriculumElementSettings(element);
		creditPointConfig = creditPointService.getConfiguration(element);
		systems = creditPointService.getCreditPointSystems(ureq.getUserSession().getRoles());
		
		mediaContainer = curriculumService.getMediaContainer(element);
		if (mediaContainer != null && mediaContainer.getName().equals("media")) {
			mediaContainer = mediaContainer.getParentContainer();
			mediaContainer.setDefaultItemFilter(new MediaContainerFilter(mediaContainer));
		}
		
		initForm(ureq);
	}
	
	public CurriculumElement getCurriculumElement() {
		return element;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("curriculum.element.infos");
		setFormInfo("curriculum.element.infos.desc");
		
		UserSession usess = ureq.getUserSession();
		
		imageEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "rentry.pic", "rentry.pic", formLayout);
		imageEl.setExampleKey("rentry.pic.example", new String[] {RepositoryManager.PICTURE_WIDTH + "x" + (RepositoryManager.PICTURE_HEIGHT)});
		imageEl.limitToMimeType(imageMimeTypes, "error.mimetype", new String[]{ imageMimeTypes.toString()} );
		imageEl.setMaxUploadSizeKB(picUploadlimitKB, null, null);
		imageEl.setPreview(usess, true);
		imageEl.setShowInputIfFileUploaded(false);
		imageEl.addActionListener(FormEvent.ONCHANGE);
		VFSLeaf imageLeaf = curriculumService.getCurriculumElemenFile(element, CurriculumElementFileType.teaserImage);
		if (imageLeaf instanceof LocalFileImpl imageFile) {
			imageEl.setPreview(usess, true);
			imageEl.setInitialFile(imageFile.getBasefile());
		}
		imageEl.setEnabled(canEdit && !CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.image));
		imageEl.setDeleteEnabled(imageEl.isEnabled());

		if (isRootElement) {
			VFSLeaf videoLeaf = curriculumService.getCurriculumElemenFile(element, CurriculumElementFileType.teaserVideo);

			withMovieEl = uifactory.addToggleButton("with.teaser.movie", "cif.with.teaser.movie", translate("on"), translate("off"), formLayout);
			withMovieEl.setEnabled(canEdit && !CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.video));
			withMovieEl.addActionListener(FormEvent.ONCHANGE);
			withMovieEl.toggle(videoLeaf != null);

			videoEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "rentry.movie", "rentry.movie", formLayout);
			videoEl.setExampleKey("rentry.movie.example", new String[] {"3:2"});
			videoEl.limitToMimeType(videoMimeTypes, "error.mimetype", new String[]{ videoMimeTypes.toString()} );
			videoEl.setMaxUploadSizeKB(movieUploadlimitKB, null, null);
			videoEl.setPreview(usess, true);
			videoEl.setShowInputIfFileUploaded(false);
			videoEl.addActionListener(FormEvent.ONCHANGE);
			if(videoLeaf instanceof LocalFileImpl videoFile) {
				videoEl.setPreview(usess, true);
				videoEl.setInitialFile(videoFile.getBasefile());
			}
			videoEl.setEnabled(canEdit && !CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.video));
			videoEl.setDeleteEnabled(videoEl.isEnabled());
			videoEl.setVisible(withMovieEl.isOn());

			teaserEl = uifactory.addTextElement("cif.teaser", "cif.teaser", 150, element.getTeaser(), formLayout);
			teaserEl.setEnabled(canEdit && !CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.teaser));

			String desc = element.getDescription() != null ? element.getDescription() : "";
			descriptionEl = uifactory.addRichTextElementForStringData("cif.description", "cif.description",
					desc, 10, -1, false, mediaContainer, null, formLayout, usess, getWindowControl());
			descriptionEl.setEnabled(canEdit && !CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.description));
			descriptionEl.getEditorConfiguration().setFileBrowserUploadRelPath("media");
			descriptionEl.getEditorConfiguration().setPathInStatusBar(false);
			
			FormSection factsCont = uifactory.addFormSection("facts", translate("details.facts"), formLayout, FormSection.Level.SUB_TITLE);

			authorsEl = uifactory.addTextElement("cif.authors", "cif.authors", 150, element.getAuthors(), factsCont);
			authorsEl.setEnabled(canEdit && !CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.authors));

			mainLanguageEl = uifactory.addTextElement("cif.mainLanguage", "cif.mainLanguage", 150, element.getMainLanguage(), factsCont);
			mainLanguageEl.setEnabled(canEdit && !CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.mainLanguage));

			expenditureOfWorkEl = uifactory.addTextElement("cif.expenditureOfWork", "cif.expenditureOfWork",150, element.getExpenditureOfWork(), factsCont);
			expenditureOfWorkEl.setExampleKey("details.expenditureOfWork.example", null);
			expenditureOfWorkEl.setEnabled(canEdit && !CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.expenditureOfWork));

			FormSection displayCont = uifactory.addFormSection("display", translate("cif.display.settings"), formLayout, FormSection.Level.SUB_TITLE);

			SelectionValues showInfoPK = new SelectionValues();
			showInfoPK.add(SelectionValues.entry(OUTLINE_KEY, translate("infos.outline")));
			showInfoPK.add(SelectionValues.entry(EVENTS_KEY, translate("cif.events")));
			showInfoPK.add(SelectionValues.entry(MEET_TEACHERS_KEY, translate("cif.meet.your.teachers")));
			showInfoPK.add(SelectionValues.entry(CERTIFICATE_KEY, translate("details.certificate")));
			if(creditPointModule.isEnabled()) {
				showInfoPK.add(SelectionValues.entry(CREDIT_POINTS_KEY, translate("details.benefits.credit.points")));
			}
			showInfoEl = uifactory.addCheckboxesVertical("show.info", "cif.display.on.info.page", displayCont,
					showInfoPK.keys(), showInfoPK.values(), 1);
			showInfoEl.setHelpText(translate("cif.display.on.info.page.help"));
			showInfoEl.addActionListener(FormEvent.ONCLICK);
			showInfoEl.setEnabled(canEdit);
			showInfoEl.setEnabled(OUTLINE_KEY, canEdit && !CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.showOutline));
			showInfoEl.setEnabled(EVENTS_KEY, canEdit && !CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.showLectures));
			showInfoEl.setEnabled(MEET_TEACHERS_KEY, canEdit && !CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.taughtBy));
			showInfoEl.setEnabled(CERTIFICATE_KEY, canEdit && !CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.showCertificate));
			showInfoEl.setEnabled(CREDIT_POINTS_KEY, canEdit && !CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.showCreditPoints));
			boolean showCertificate = element.isShowCertificateBenefit();
			boolean showCreditPoints = element.isShowCreditPointsBenefit();
			boolean meetTeachers = !element.getTaughtBys().isEmpty();
			showInfoEl.select(OUTLINE_KEY, element.isShowOutline());
			showInfoEl.select(EVENTS_KEY, element.isShowLectures());
			showInfoEl.select(MEET_TEACHERS_KEY, meetTeachers);
			showInfoEl.select(CERTIFICATE_KEY, showCertificate);
			if(creditPointModule.isEnabled()) {
				showInfoEl.select(CREDIT_POINTS_KEY, showCreditPoints);
			}

			Map<TaughtBy,Integer> taughtByCounts = getTaughtByCounts();
			SelectionValues taughtBySV = new SelectionValues();
			TaughtBy.ALL.forEach(taughtBy -> taughtBySV.add(SelectionValues.entry(
					taughtBy.name(),
					translate("cif.taught.by." + taughtBy.name(), String.valueOf(taughtByCounts.getOrDefault(taughtBy, Integer.valueOf(0)))))));
			taughtByEl = uifactory.addCheckboxesVertical("taught.by", "cif.taught.by", displayCont, taughtBySV.keys(), taughtBySV.values(), 1);
			taughtByEl.setHelpText(translate("cif.taught.by.help"));
			taughtByEl.setEnabled(canEdit && !CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.taughtBy));
			element.getTaughtBys().forEach(taughtBy -> taughtByEl.select(taughtBy.name(), true));
			taughtByEl.setVisible(meetTeachers);

			// Credit points
			creditPointCont = uifactory.addInlineFormLayout("details.benefits.credit.points", "details.benefits.credit.points", displayCont);
			creditPointCont.setMandatory(true);
			String points = creditPointConfig == null || creditPointConfig.getCreditPoints() == null
					? null
					: creditPointConfig.getCreditPoints().toString();
			creditPointsEl = uifactory.addTextElement("credit.points", null, 6, points, creditPointCont);

			SelectionValues systemPK = new SelectionValues();
			CreditPointSystem selectedSystem = creditPointConfig == null ? null : creditPointConfig.getCreditPointSystem();
			for(CreditPointSystem system:systems) {
				if(system.getStatus() == CreditPointSystemStatus.active || system.equals(selectedSystem)) {
					systemPK.add(SelectionValues.entry(system.getKey().toString(), system.getName() + " " + system.getLabel()));
				}
			}
			creditPointSystemEl = uifactory.addDropdownSingleselect("credit.point.system", null, creditPointCont,
					systemPK.keys(), systemPK.values());
			if(selectedSystem != null && systemPK.containsKey(creditPointConfig.getCreditPointSystem().getKey().toString())) {
				creditPointSystemEl.select(creditPointConfig.getCreditPointSystem().getKey().toString(), true);
			}
			creditPointCont.setVisible(creditPointModule.isEnabled() && showCreditPoints);

			FormSection detailsCont = uifactory.addFormSection("details", translate("cif.details"), formLayout, FormSection.Level.SUB_TITLE);
			detailsCont.setCollapsible(true);
			detailsCont.setCollapsed(true);
			detailsCont.setPersistedStatusId(ureq, "curriculum.element.infos.details");

			String obj = element.getObjectives() != null ? element.getObjectives() : "";
			objectivesEl = uifactory.addRichTextElementForStringData("cif.objectives", "cif.objectives",
					obj, 10, -1, false, mediaContainer, null, detailsCont, usess, getWindowControl());
			objectivesEl.setEnabled(!CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.objectives));
			objectivesEl.getEditorConfiguration().setFileBrowserUploadRelPath("media");
			objectivesEl.getEditorConfiguration().setSimplestTextModeAllowed(TextMode.multiLine);
			objectivesEl.setEnabled(canEdit && !CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.objectives));

			String req = element.getRequirements() != null ? element.getRequirements() : "";
			requirementsEl = uifactory.addRichTextElementForStringData("cif.requirements", "cif.requirements",
					req, 10, -1,  false, mediaContainer, null, detailsCont, usess, getWindowControl());
			requirementsEl.setEnabled(!CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.requirements));
			requirementsEl.getEditorConfiguration().setFileBrowserUploadRelPath("media");
			requirementsEl.getEditorConfiguration().setSimplestTextModeAllowed(TextMode.multiLine);
			requirementsEl.setMaxLength(2000);
			requirementsEl.setEnabled(canEdit && !CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.requirements));

			String cred = element.getCredits() != null ? element.getCredits() : "";
			creditsEl = uifactory.addRichTextElementForStringData("cif.credits", "cif.credits",
					cred, 10, -1,  false, mediaContainer, null, detailsCont, usess, getWindowControl());
			creditsEl.setEnabled(!CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.credits));
			creditsEl.getEditorConfiguration().setFileBrowserUploadRelPath("media");
			creditsEl.getEditorConfiguration().setSimplestTextModeAllowed(TextMode.multiLine);
			creditsEl.setMaxLength(2000);
			creditsEl.setEnabled(canEdit && !CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.credits));
		}
		
		if (canEdit) {
			FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			formLayout.add(buttonsCont);
			uifactory.addFormSubmitButton("save", buttonsCont);
		}
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk &= CurriculumHelper.validateTextElement(teaserEl, false, 200);
		allOk &= CurriculumHelper.validateTextElement(descriptionEl, false, 80000);
		allOk &= CurriculumHelper.validateTextElement(authorsEl, false, 200);
		allOk &= CurriculumHelper.validateTextElement(mainLanguageEl, false, 200);
		allOk &= CurriculumHelper.validateTextElement(expenditureOfWorkEl, false, 200);
		allOk &= CurriculumHelper.validateTextElement(objectivesEl, false, 2000);
		allOk &= CurriculumHelper.validateTextElement(requirementsEl, false, 2000);
		allOk &= CurriculumHelper.validateTextElement(creditsEl, false, 2000);
		
		if(creditPointCont != null && creditPointCont.isVisible()) {
			allOk &= CurriculumHelper.validateIntegerElement(creditPointsEl, true);
			allOk &= CurriculumHelper.validateElement(creditPointSystemEl);
		}

		if(taughtByEl != null) {
			taughtByEl.clearError();
			if(taughtByEl.isVisible() && taughtByEl.isEnabled() && taughtByEl.getSelectedKeys().isEmpty()) {
				taughtByEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}
		}

		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(showInfoEl == source) {
			Collection<String> selectedInfo = showInfoEl.getSelectedKeys();
			taughtByEl.setVisible(selectedInfo.contains(MEET_TEACHERS_KEY));
			creditPointCont.setVisible(selectedInfo.contains(CREDIT_POINTS_KEY));
		} else if (source == withMovieEl) {
			videoEl.setVisible(withMovieEl.isOn());
		} else if (source == imageEl) {
			if (DeleteFileElementEvent.DELETE.equals(event.getCommand())) {
				imageEl.setInitialFile(null);
				if (imageEl.getUploadFile() != null) {
					imageEl.reset();
				}
				imageEl.clearError();
				markDirty();
			} else if (imageEl.isUploadSuccess()) {
				imageEl.clearError();
				markDirty();
			}
		} else if (source == videoEl) {
			if (DeleteFileElementEvent.DELETE.equals(event.getCommand())) {
				videoEl.setInitialFile(null);
				if (videoEl.getUploadFile() != null) {
					videoEl.reset();
				}
				videoEl.clearError();
				markDirty();
			} else if (videoEl.isUploadSuccess()) {
				videoEl.clearError();
				markDirty();
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		element = curriculumService.getCurriculumElement(element);
		
		if (isRootElement) {
			element.setTeaser(teaserEl.getValue());
			element.setDescription(descriptionEl.getValue());
			element.setAuthors(authorsEl.getValue());
			element.setMainLanguage(mainLanguageEl.getValue());
			element.setExpenditureOfWork(expenditureOfWorkEl.getValue());
			Collection<String> selectedInfo = showInfoEl.getSelectedKeys();
			element.setShowOutline(selectedInfo.contains(OUTLINE_KEY));
			element.setShowLectures(selectedInfo.contains(EVENTS_KEY));
			element.setShowCertificateBenefit(selectedInfo.contains(CERTIFICATE_KEY));
			element.setShowCreditPointsBenefit(creditPointModule.isEnabled()
					? selectedInfo.contains(CREDIT_POINTS_KEY)
					: element.isShowCreditPointsBenefit());
			element.setObjectives(objectivesEl.getValue());
			element.setRequirements(requirementsEl.getValue());
			element.setCredits(creditsEl.getValue());
			element.setTaughtBys(selectedInfo.contains(MEET_TEACHERS_KEY)
					? taughtByEl.getSelectedKeys().stream().map(TaughtBy::valueOf).collect(Collectors.toSet())
					: Set.of());
			element = curriculumService.updateCurriculumElement(getIdentity(), element);
			
			commitCreditPointConfiguration();
		}
		
		if (imageEl.getUploadFile() != null) {
			curriculumService.storeCurriculumElemenFile(element, CurriculumElementFileType.teaserImage, imageEl.getUploadFile(), imageEl.getUploadFileName(), getIdentity());
		} else if (imageEl.getInitialFile() == null) {
			curriculumService.deleteCurriculumElemenFile(element, CurriculumElementFileType.teaserImage);
		}
		
		if (isRootElement) {
			if (!withMovieEl.isOn()) {
				curriculumService.deleteCurriculumElemenFile(element, CurriculumElementFileType.teaserVideo);
			} else if (videoEl.getUploadFile() != null) {
				curriculumService.storeCurriculumElemenFile(element, CurriculumElementFileType.teaserVideo, videoEl.getUploadFile(), videoEl.getUploadFileName(), getIdentity());
			} else if (videoEl.getInitialFile() == null) {
				curriculumService.deleteCurriculumElemenFile(element, CurriculumElementFileType.teaserVideo);
			}
		}
		
		element = curriculumService.getCurriculumElement(element);
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void commitCreditPointConfiguration() {
		creditPointConfig = creditPointService.getConfiguration(element);
		if(creditPointCont.isVisible() && StringHelper.containsNonWhitespace(creditPointsEl.getValue()) && creditPointSystemEl.isOneSelected()) {
			creditPointConfig.setEnabled(true);
			creditPointConfig.setCreditPoints(new BigDecimal(creditPointsEl.getValue()));
			CreditPointSystem system = getSelectedCreditPointSystem();
			creditPointConfig.setCreditPointSystem(system);
		} else {
			creditPointConfig.setEnabled(false);
			creditPointConfig.setCreditPoints(null);
			creditPointConfig.setCreditPointSystem(null);
		}
		creditPointConfig = creditPointService.updateConfiguration(creditPointConfig);
	}
	
	private CreditPointSystem getSelectedCreditPointSystem() {
		if(!creditPointSystemEl.isOneSelected()) return null;
		String selectedKey = creditPointSystemEl.getSelectedKey();
		return systems.stream()
				.filter(sys -> selectedKey.equals(sys.getKey().toString()))
				.findFirst().orElse(null);
	}
	
	private Map<TaughtBy, Integer> getTaughtByCounts() {
		Map<TaughtBy, Integer> taughtByCount = new HashMap<>(TaughtBy.ALL.size());
		List<CurriculumElementMembership> memberships = curriculumService.getCurriculumElementMemberships(List.of(element));
		
		for (CurriculumElementMembership membership : memberships) {
			if (membership.isCoach()) {
				incrementTaughtBy(taughtByCount, TaughtBy.coaches);
			}
			if (membership.isRepositoryEntryOwner()) {
				incrementTaughtBy(taughtByCount, TaughtBy.owners);
			}
		}
		
		Integer taughtByTeachers = Integer.valueOf(0);
		if (lectureModule.isEnabled()) {
			LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
			searchParams.setLectureConfiguredRepositoryEntry(false);
			searchParams.setCurriculumElementPath(element.getMaterializedPathKeys());
			List<LectureBlock> lectureBlocks = lectureService.getLectureBlocks(searchParams, -1, Boolean.TRUE);
			taughtByTeachers = lectureService.getTeachers(lectureBlocks).size();
		}
		taughtByCount.put(TaughtBy.teachers, taughtByTeachers);
		
		return taughtByCount;
	}
	
	private void incrementTaughtBy(Map<TaughtBy, Integer> taughtByCount, TaughtBy taughtBy) {
		Integer count = taughtByCount.computeIfAbsent(taughtBy, key -> Integer.valueOf(0));
		count = count + 1;
		taughtByCount.put(taughtBy, count);
	}

}
