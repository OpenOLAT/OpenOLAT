/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.lecture.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.vfs.model.VFSThumbnailInfos;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionSource;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.ExternalLinkItem;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.EmptyPanelItem;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.BusinessGroupQueryParams;
import org.olat.group.model.StatisticsBusinessGroupRow;
import org.olat.ims.lti13.LTI13Service;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementInfos;
import org.olat.modules.curriculum.model.CurriculumElementInfosSearchParams;
import org.olat.modules.curriculum.model.CurriculumElementRefImpl;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LectureBlockRow;
import org.olat.modules.lecture.ui.LectureListDetailsParticipantsGroupDataModel.GroupCols;
import org.olat.modules.lecture.ui.LectureListRepositoryConfig.Visibility;
import org.olat.modules.lecture.ui.component.IconDecoratorCellRenderer;
import org.olat.modules.lecture.ui.component.LectureBlockParticipantGroupExcludeRenderer;
import org.olat.modules.lecture.ui.component.LectureBlockRollCallBasicStatusCellRenderer;
import org.olat.modules.lecture.ui.component.LectureBlockStatusCellRenderer;
import org.olat.modules.lecture.ui.component.OpenOnlineMeetingEvent;
import org.olat.modules.lecture.ui.event.EditLectureBlockRowEvent;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.ui.component.TaxonomyLevelSelectionSource;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryEntryRuntimeType;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.RepositoryEntryImageMapper;
import org.olat.user.PortraitUser;
import org.olat.user.UserInfoProfileConfig;
import org.olat.user.UserInfoProfileController;
import org.olat.user.UserPortraitService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 sept. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureListDetailsController extends FormBasicController {
	
	private FormLink editButton;
	private FormLink openEntryLink;
	private FormItem openOnlineMeetingButton;
	
	private FlexiTableElement tableEl;
	private StaticTextElement participantsEl;
	private LectureListDetailsParticipantsGroupDataModel tableModel;
	
	private int counter = 0;
	private final LectureBlockRow row;
	private MapperKey mapperThumbnailKey;
	private CurriculumElement curriculumElement;
	private final UserInfoProfileConfig profileConfig;
	private final boolean allowRepositoryEntry;
	private final RepositoryEntry repositoryEntry;
	private final boolean lectureManagementManaged;
	private final boolean taxonomyEnabled;
	private final LectureListRepositoryConfig config;
	private RepositoryEntryImageMapper mapperThumbnail;
	
	private ToolsController toolsCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;

	@Autowired
	private DB dbInstance;
	@Autowired
	private MapperService mapperService;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private UserPortraitService userPortraitService;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private CurriculumModule curriculumModule;
	@Autowired
	private RepositoryModule repositoryModule;
	
	public LectureListDetailsController(UserRequest ureq, WindowControl wControl, LectureBlockRow row, Form rootForm,
			LectureListRepositoryConfig config, boolean lectureManagementManaged, boolean inRepoEntry, boolean taxonomyEnabled) {
		super(ureq, wControl, LAYOUT_CUSTOM, "lecture_details_view", rootForm);
		this.row = row;
		this.config = config;
		this.lectureManagementManaged = lectureManagementManaged;
		this.taxonomyEnabled = taxonomyEnabled;
		profileConfig = userPortraitService.createProfileConfig();
		
		repositoryEntry = row.getEntry() != null ? repositoryService.loadByKey(row.getEntry().key()) : null;
		if(repositoryEntry != null) {
			mapperThumbnail = RepositoryEntryImageMapper.mapper210x140();
			mapperThumbnailKey = mapperService.register(null, RepositoryEntryImageMapper.MAPPER_ID_210_140, mapperThumbnail);
		}
		
		if(row.getCurriculumElement() != null) {
			curriculumElement = curriculumService
					.getCurriculumElement(new CurriculumElementRefImpl(row.getCurriculumElement().key()));
		}
		allowRepositoryEntry = !inRepoEntry && (curriculumElement == null
				|| curriculumElement.getType() == null
				|| curriculumElement.getType().getMaxRepositoryEntryRelations() != 0);// 1 or more, or -1 for infinity
		
		initForm(ureq);
		loadGroupsModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(!lectureManagementManaged && row.canEdit()) {
			editButton = uifactory.addFormLink("edit", "edit", "edit", formLayout, Link.BUTTON);
			editButton.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
			editButton.setElementCssClass("o_lecture_details_edit");
		}
		
		LectureBlock lectureBlock = row.getLectureBlock();
		if(lectureBlock.getTeamsMeeting() != null || lectureBlock.getBBBMeeting() != null) {
			openOnlineMeetingButton = uifactory.addFormLink("open.online.meeting", "open.online.meeting", "online.room", formLayout, Link.BUTTON);
			openOnlineMeetingButton.setFormLayout("vertical");
		} else if(StringHelper.containsNonWhitespace(lectureBlock.getMeetingUrl())) {
			ExternalLinkItem button = uifactory.addExternalLink("open.external.meeting", "open.external.meeting", lectureBlock.getMeetingUrl(), "_blank", formLayout);
			button.setName(translate("open.online.meeting"));
			button.setLabel("online.room", null);
			button.setCssClass("btn btn-default");
		}
		
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			layoutCont.contextPut("lectureBlockKey", lectureBlock.getKey());
			layoutCont.contextPut("title", lectureBlock.getTitle());
			if(config.withDetailsExternalRef()) {
				layoutCont.contextPut("externalRef", lectureBlock.getExternalRef());
			}
			
			String badge = LectureBlockStatusCellRenderer.getStatusLabel(lectureBlock, row.isNextScheduled(), getTranslator());
			layoutCont.contextPut("lectureBlockStatusBadge", badge);
			if(config.withRollCall() != Visibility.NO) {
				String rollCallBadge = LectureBlockRollCallBasicStatusCellRenderer.getStatusLabel(lectureBlock, getTranslator());
				layoutCont.contextPut("rollCallStatusBadge", rollCallBadge);
			}
			
			if(allowRepositoryEntry) {
				initFormReferencedCourses(layoutCont);
			}
			initFormSubjects(formLayout);
			initFormTeachers(layoutCont, ureq);
			initFormMetadata(formLayout);
			initFormParticipantsGroupTable(formLayout);
		}
	}
	
	private void initFormParticipantsGroupTable(FormItemContainer formLayout) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GroupCols.title));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GroupCols.numParticipants,
				new IconDecoratorCellRenderer("o_icon o_icon-fw o_icon_user")));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GroupCols.status,
				new LectureBlockParticipantGroupExcludeRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new ActionsColumnModel(GroupCols.tools));
		
		tableModel = new LectureListDetailsParticipantsGroupDataModel(columnsModel, getLocale());
		
		tableEl = uifactory.addTableElement(getWindowControl(), "participantsGroupTable", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setCustomizeColumns(false);
		tableEl.setVisible(config.withDetailsParticipantsGroups());
	}
	
	private void initFormReferencedCourses(FormLayoutContainer formLayout) {
		if(repositoryEntry == null) {		
			EmptyPanelItem emptyTeachersList = uifactory.addEmptyPanel("course.empty", null, formLayout);
			emptyTeachersList.setTitle(translate("lecture.no.course.assigned.title"));
			emptyTeachersList.setIconCssClass("o_icon o_icon-lg o_CourseModule_icon");
		} else {
			final String url = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(getRepositoryEntryPath());
			
			openEntryLink = uifactory.addFormLink("entry.open", repositoryEntry.getDisplayname(), null, formLayout, Link.LINK | Link.NONTRANSLATED);		
			openEntryLink.setEscapeMode(EscapeMode.html);
			openEntryLink.setUrl(url);
			
			VFSThumbnailInfos thumbnail = mapperThumbnail.getRepositoryThumbnail(repositoryEntry);
			if (thumbnail != null) {
				String thumbnailUrl = RepositoryEntryImageMapper.getImageURL(mapperThumbnailKey.getUrl(), thumbnail.metadata(), thumbnail.thumbnailMetadata());
				formLayout.contextPut("thumbnailUrl",thumbnailUrl);
			}
			
			formLayout.contextPut("entryKey", repositoryEntry.getKey());
			formLayout.contextPut("entryUrl", url);
			if(StringHelper.containsNonWhitespace(repositoryEntry.getExternalRef())) {
				formLayout.contextPut("entryExternalRef", repositoryEntry.getExternalRef());
			}
		}
	}
	
	private String getRepositoryEntryPath() {
		return "[RepositoryEntry:" + repositoryEntry.getKey() + "]";
	}

	private void initFormSubjects(FormItemContainer formLayout) {
		if (!taxonomyEnabled) {
			return;
		}

		Collection<TaxonomyRef> taxonomyRefs = getTaxonomyRefs();
		if (taxonomyRefs.isEmpty() || row.getSubjects() == null || row.getSubjects().isEmpty()) {
			return;
		}
		
		if (row.getLectureBlock().getTaxonomyLevels() == null || row.getLectureBlock().getTaxonomyLevels().isEmpty()) {
			return;
		}
		
		ObjectSelectionSource source = new TaxonomyLevelSelectionSource(getLocale(), row.getSubjects(), List::of, null, null);
		ObjectSelectionElement taxonomyLevelEl = uifactory.addObjectSelectionElement("lecture.subjects",
				"lecture.subjects", formLayout, getWindowControl(), true, source);
		taxonomyLevelEl.setEnabled(false);
	}

	private Collection<TaxonomyRef> getTaxonomyRefs() {
		Set<TaxonomyRef> taxonomyRefs = new HashSet<>();
		if (curriculumElement != null) {
			taxonomyRefs.addAll(curriculumModule.getTaxonomyRefs());
		} else {
			if (row.getLectureBlock() != null && row.getLectureBlock().getCurriculumElement() != null) {
				taxonomyRefs.addAll(curriculumModule.getTaxonomyRefs());
			}
		}
		taxonomyRefs.addAll(repositoryModule.getTaxonomyRefs());
		return taxonomyRefs;
	}

	private void initFormTeachers(FormLayoutContainer formLayout, UserRequest ureq) {
		List<String> profilesIds = new ArrayList<>();
		if(row.getTeachersList() == null || row.getTeachersList().isEmpty()) {
			EmptyPanelItem emptyTeachersList = uifactory.addEmptyPanel("teacher.empty", "lecture.teacher", formLayout);
			emptyTeachersList.setTitle(translate("lecture.no.teacher.assigned.title"));
			emptyTeachersList.setIconCssClass("o_icon o_icon-lg o_icon_user");
			profilesIds.add(emptyTeachersList.getName());
		} else {
			List<Identity> teachers = row.getTeachersList();
			for(Identity teacher:teachers) {
				PortraitUser teacherPortraitUser = userPortraitService.createPortraitUser(getLocale(), teacher);
				UserInfoProfileController profile = new UserInfoProfileController(ureq, getWindowControl(), profileConfig, teacherPortraitUser);
				listenTo(profile);
				
				String profileId = "profile_" + teacher.getKey();
				profilesIds.add(profileId);
				formLayout.put(profileId, profile.getInitialComponent());
			}
		}
		formLayout.contextPut("profilesIds", profilesIds);
	}
	
	private void initFormMetadata(FormItemContainer formLayout) {	
		Formatter formatter = Formatter.getInstance(getLocale());
		Date startDate = row.getLectureBlock().getStartDate();
		uifactory.addStaticTextElement("lecture.date", "lecture.date", formatter.formatDateWithDay(startDate), formLayout);
		
		Date endDate = row.getLectureBlock().getEndDate();
		String time = translate("lecture.from.to.format.short",
				formatter.formatTimeShort(startDate), formatter.formatTimeShort(endDate));
		uifactory.addStaticTextElement("lecture.time", "lecture.time", time, formLayout);

		if(config.withDetailsUnits() && row.getLectureBlock().getPlannedLecturesNumber() > 1) {
			String units = Integer.toString(row.getLectureBlock().getPlannedLecturesNumber());
			uifactory.addStaticTextElement("lecture.units", "lecture.units", units, formLayout);
		}
		
		String location = row.getLocation();
		if(StringHelper.containsNonWhitespace(location)) {
			location = "<i class=\"o_icon o_icon-fw o_icon_location\"> </i> " + StringHelper.escapeHtml(location);
			uifactory.addStaticTextElement("lecture.location", "lecture.location", location, formLayout);
		}
		
		String participants = Long.toString(row.getNumOfParticipants());
		participants = "<i class=\"o_icon o_icon-fw o_icon_user\"> </i> " + participants;
		participantsEl = uifactory.addStaticTextElement("lecture.participants", "lecture.participants", participants, formLayout);
		
		String description = row.getLectureBlock().getDescription();
		if(StringHelper.containsNonWhitespace(description)) {
			description = Formatter.escWithBR(StringHelper.xssScan(description)).toString();
			uifactory.addStaticTextElement("lecture.desc", "lecture.descr", description, formLayout);
		}
		
		String preparation = row.getLectureBlock().getPreparation();
		if(StringHelper.containsNonWhitespace(preparation)) {
			preparation = Formatter.escWithBR(StringHelper.xssScan(preparation)).toString();
			uifactory.addStaticTextElement("lecture.preparation", "lecture.preparation", preparation, formLayout);
		}
		
		String compulsory  = row.getLectureBlock().isCompulsory() ? translate("yes") : translate("no");
		uifactory.addStaticTextElement("lecture.compulsory", "lecture.compulsory", compulsory, formLayout);
	}
	
	private void loadGroupsModel() {
		List<LectureBlockParticipantGroupRow> groupList = new ArrayList<>();
		List<Group> selectedGroups = lectureService.getLectureBlockToGroups(row.getLectureBlock());
		
		if(row.getEntry() != null && row.getEntry().key() != null && repositoryEntry != null) {
			boolean curriculumType = (repositoryEntry.getRuntimeType() == RepositoryEntryRuntimeType.curricular);
			
			Group defaultGroup = repositoryService.getDefaultGroup(repositoryEntry);
			int participants = repositoryService.countMembers(repositoryEntry, RepositoryEntryRelationType.defaultGroup, GroupRoles.participant.name());
			if(!curriculumType || (selectedGroups.contains(defaultGroup) && participants > 0)) {
				groupList.add(decorateRow(new LectureBlockParticipantGroupRow(repositoryEntry, defaultGroup,
					participants, !selectedGroups.contains(defaultGroup))));
			}
		
			BusinessGroupQueryParams params = new BusinessGroupQueryParams();
			params.setTechnicalTypes(List.of(BusinessGroup.BUSINESS_TYPE, LTI13Service.LTI_GROUP_TYPE));
			List<StatisticsBusinessGroupRow> businessGroups = businessGroupService.findBusinessGroupsFromRepositoryEntry(params, null, repositoryEntry);
			for(StatisticsBusinessGroupRow businessGroup:businessGroups) {
				boolean included = selectedGroups.contains(businessGroup.getBaseGroup());
				if(!curriculumType || included) {
					groupList.add(decorateRow(new LectureBlockParticipantGroupRow(businessGroup, !included)));
				}
			}
			
			CurriculumElementInfosSearchParams searchParams = CurriculumElementInfosSearchParams.searchElementsOf(null, repositoryEntry);
			List<CurriculumElementInfos> elementsInfos = curriculumService.getCurriculumElementsWithInfos(searchParams);
			for(CurriculumElementInfos elementInfos:elementsInfos) {
				boolean included = selectedGroups.contains(elementInfos.curriculumElement().getGroup());
				groupList.add(decorateRow(new LectureBlockParticipantGroupRow(elementInfos, !included)));
			}
		} else if(row.getCurriculumElement() != null && row.getCurriculumElement().key() != null) {
			CurriculumElementRef curriculumElementRef = new CurriculumElementRefImpl(row.getCurriculumElement().key());
			CurriculumElementInfosSearchParams searchParams = CurriculumElementInfosSearchParams
					.searchElements(null, List.of(curriculumElementRef));
			List<CurriculumElementInfos> elementsInfos = curriculumService.getCurriculumElementsWithInfos(searchParams);
			for(CurriculumElementInfos elementInfos:elementsInfos) {
				boolean excluded = !selectedGroups.contains(elementInfos.curriculumElement().getGroup());
				groupList.add(decorateRow(new LectureBlockParticipantGroupRow(elementInfos, excluded)));
			}
		}
		
		tableModel.setObjects(groupList);
		tableEl.reset(true, true, true);
	}
	
	private LectureBlockParticipantGroupRow decorateRow(LectureBlockParticipantGroupRow groupRow) {
		String openLinkName = "detailsopen-" + counter++;
		String title = StringHelper.escapeHtml(groupRow.getTitle());
		String externalRef = groupRow.getExternalRef();
		if(StringHelper.containsNonWhitespace(externalRef)) {
			title += " <small>" + translate("lecture.separator") + " " + StringHelper.escapeHtml(externalRef) + "</small>";
		}
		
		String businessPath = getBusinessPath(groupRow);
		FormLink openLink = uifactory.addFormLink(openLinkName, "open", title, null, flc, Link.LINK | Link.NONTRANSLATED);
		openLink.setUrl(BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(businessPath));
		openLink.setIconLeftCSS(groupRow.getIconCssClass());
		openLink.setUserObject(groupRow);
		groupRow.setTitleLink(openLink);
		
		FormLink toolsLink = ActionsColumnModel.createLink(uifactory, getTranslator(), "detailstool");
		toolsLink.setUserObject(groupRow);
		groupRow.setToolsLink(toolsLink);
		return groupRow;
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(toolsCalloutCtrl == source) {
			cleanUp();
		} else if(toolsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				if(toolsCalloutCtrl != null) {
					toolsCalloutCtrl.deactivate();
					cleanUp();
				}
			}
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		toolsCalloutCtrl = null;
		toolsCtrl = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(editButton == source) {
			fireEvent(ureq, new EditLectureBlockRowEvent(row));
		} else if(openEntryLink == source) {
			doOpenRepositoryEntry(ureq);
		} else if(openOnlineMeetingButton == source) {
			doOpenOnlineMeeting(ureq);
		} else if(source instanceof FormLink link) {
			String cmd = link.getCmd();
			if("detailstool".equals(cmd) && link.getUserObject() instanceof LectureBlockParticipantGroupRow groupRow) {
				doOpenTools(ureq, groupRow, link);
			} else if("open".equals(cmd) && link.getUserObject() instanceof LectureBlockParticipantGroupRow groupRow) {
				doOpen(ureq, groupRow);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doOpenTools(UserRequest ureq, LectureBlockParticipantGroupRow groupRow, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), groupRow);
		listenTo(toolsCtrl);
	
		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private void doOpenOnlineMeeting(UserRequest ureq) {
		fireEvent(ureq, new OpenOnlineMeetingEvent(row.getLectureBlock()));
	}
	
	private void doOpenRepositoryEntry(UserRequest ureq) {
		String businessPath = getRepositoryEntryPath();
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private void doOpen(UserRequest ureq, LectureBlockParticipantGroupRow groupRow) {
		String businessPath = getBusinessPath(groupRow);
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private String getBusinessPath(LectureBlockParticipantGroupRow groupRow) {
		String businessPath;
		if(groupRow.getEntry() != null) {
			businessPath = "[RepositoryEntry:" + groupRow.getEntry().getKey() + "]";
		} else if(groupRow.getBusinessGroup() != null) {
			businessPath = "[BusinessGroup:" + groupRow.getBusinessGroup().getKey() + "]";	
		} else if(groupRow.getCurriculumElement() != null) {
			CurriculumElementInfos element = groupRow.getCurriculumElement();
			businessPath = "[CurriculumAdmin:0][Curriculum:" + element.curriculum().getKey()
					+ "][CurriculumElement:" + element.getKey() + "]";	
		} else {
			businessPath = null;
		}
		return businessPath;
	}
	
	private void doExclude(UserRequest ureq, LectureBlockParticipantGroupRow groupRow) {
		groupRow.setExcluded(true);
		doSaveGroups(ureq);
	}
	
	private void doInclude(UserRequest ureq, LectureBlockParticipantGroupRow groupRow) {
		groupRow.setExcluded(false);
		doSaveGroups(ureq);
	}
	
	private void doSaveGroups(UserRequest ureq) {
		LectureBlock lectureBlock = lectureService.getLectureBlock(row.getLectureBlock());
		
		long numOfParticipants = 0;
		List<Group> groups = new ArrayList<>();
		List<LectureBlockParticipantGroupRow> groupRows = tableModel.getObjects();
		for(LectureBlockParticipantGroupRow groupRow:groupRows) {
			if(!groupRow.isExcluded() && groupRow.getGroup() != null) {
				groups.add(groupRow.getGroup());
				numOfParticipants += groupRow.getNumOfParticipants();
			}
		}
		
		lectureBlock = lectureService.save(lectureBlock, groups);
		row.setLectureBlock(lectureBlock);
		row.setNumOfParticipants(numOfParticipants);
		participantsEl.setValue("<i class=\"o_icon o_icon-fw o_icon_user\"> </i> " + numOfParticipants);
		
		dbInstance.commit();
		loadGroupsModel();
		
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private class ToolsController extends BasicController {
		
		private Link openLink;
		private Link excludeLink;
		private Link includeLink;
		
		private final LectureBlockParticipantGroupRow groupRow;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, LectureBlockParticipantGroupRow groupRow) {
			super(ureq, wControl);
			this.groupRow = groupRow;
			
			VelocityContainer mainVC = createVelocityContainer("lectures_details_tools");
			
			String businessPath = getBusinessPath(groupRow);
			if(businessPath != null) {
				String openI18nKey = "open.course";
				if(groupRow.getBusinessGroup() != null) {
					openI18nKey = "open.group";
				}
				if(groupRow.getCurriculumElement() != null) {
					openI18nKey = "open.curriculum.element";
				}
				openLink = LinkFactory.createLink("open", openI18nKey, getTranslator(), mainVC, this, Link.LINK);
				openLink.setIconLeftCSS("o_icon o_icon-fw o_icon_external_link");
				openLink.setUrl(BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(businessPath));
				openLink.setNewWindow(true, false);
			}

			if(groupRow.isExcluded()) {
				includeLink = LinkFactory.createLink("include.participants", "include.participants", getTranslator(), mainVC, this, Link.LINK);
				includeLink.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
			} else {
				excludeLink = LinkFactory.createLink("exclude.participants", "exclude.participants", getTranslator(), mainVC, this, Link.LINK);
				excludeLink.setIconLeftCSS("o_icon o_icon-fw o_icon_invalidate");
			}
			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(openLink == source) {
				doOpen(ureq, groupRow);
			} else if(includeLink == source) {
				doInclude(ureq, groupRow);
			} else if(excludeLink == source) {
				doExclude(ureq, groupRow);
			}
		}
	}
}
