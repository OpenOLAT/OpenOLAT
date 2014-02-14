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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.TransformerException;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.olat.NewControllerFactory;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormCancel;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.course.nodes.CheckListCourseNode;
import org.olat.course.nodes.cl.CheckboxManager;
import org.olat.course.nodes.cl.model.AssessmentBatch;
import org.olat.course.nodes.cl.model.AssessmentData;
import org.olat.course.nodes.cl.model.Checkbox;
import org.olat.course.nodes.cl.model.CheckboxList;
import org.olat.course.nodes.cl.model.DBCheck;
import org.olat.course.nodes.cl.ui.CheckboxAssessmentDataModel.Cols;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupMembership;
import org.olat.group.BusinessGroupService;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.model.RepositoryEntryMembership;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 07.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CheckListAssessmentController extends FormBasicController implements ControllerEventListener {
	
	protected static final String USER_PROPS_ID = CheckListAssessmentController.class.getCanonicalName();

	private static final String[] onKeys = new String[] { "on" };
	private static final String[] onValues = new String[] { "" };
	
	private final Date dueDate;
	private final Boolean closeAfterDueDate;
	private final OLATResourceable courseOres;
	private final CheckListCourseNode courseNode;
	private final ModuleConfiguration config;
	private final UserCourseEnvironment userCourseEnv;
	private final boolean isAdministrativeUser;

	private FormSubmit saveButton;
	private FormCancel cancelButton;
	private FormLink pdfExport, editButton;
	private CheckListAssessmentDataModel model;
	private FlexiTableElement table;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final CheckboxList checkboxList;
	
	private CloseableModalController cmc;
	private AssessedIdentityOverviewController editCtrl;
	
	private final UserManager userManager;
	private final BaseSecurity securityManager;
	private final CheckboxManager checkboxManager;
	private final RepositoryManager repositoryManager;
	private final BusinessGroupService businessGroupService;
	
	
	/**
	 * Use this constructor to launch the checklist.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param courseNode
	 */
	public CheckListAssessmentController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			OLATResourceable courseOres, CheckListCourseNode courseNode) {
		super(ureq, wControl, "assessment_list");

		userManager = CoreSpringFactory.getImpl(UserManager.class);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		checkboxManager = CoreSpringFactory.getImpl(CheckboxManager.class);
		repositoryManager = CoreSpringFactory.getImpl(RepositoryManager.class);
		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		BaseSecurityModule securityModule = CoreSpringFactory.getImpl(BaseSecurityModule.class);
		
		this.courseOres = courseOres;
		this.courseNode = courseNode;
		this.userCourseEnv = userCourseEnv;
		config = courseNode.getModuleConfiguration();
		checkboxList = (CheckboxList)config.get(CheckListCourseNode.CONFIG_KEY_CHECKBOX);
		Roles roles = ureq.getUserSession().getRoles();
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);
		
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
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			if(dueDate != null) {
				layoutCont.contextPut("dueDate", dueDate);
			}
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if(isAdministrativeUser) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.username.i18nKey(), Cols.username.ordinal()));
		}
		
		int i=0;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			int colIndex = CheckListAssessmentDataModel.USER_PROPS_OFFSET + i++;
			if (userPropertyHandler == null) continue;
			
			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(USER_PROPS_ID , userPropertyHandler);
			if(visible) {
				FlexiColumnModel col;
				if(UserConstants.FIRSTNAME.equals(propName)
						|| UserConstants.LASTNAME.equals(propName)) {
					col = new StaticFlexiColumnModel(userPropertyHandler.i18nColumnDescriptorLabelKey(),
							colIndex, userPropertyHandler.getName(), true, propName,
							new StaticFlexiCellRenderer(userPropertyHandler.getName(), new TextFlexiCellRenderer()));
				} else {
					col = new DefaultFlexiColumnModel(true, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, true, propName);
				}
				columnsModel.addFlexiColumnModel(col);
			}
		}
		
		List<Checkbox> boxList = checkboxList.getList();
		int j = 0;
		for(Checkbox box:boxList) {
			int colIndex = CheckListAssessmentDataModel.CHECKBOX_OFFSET + j++;
			String colName = "checkbox_" + colIndex;
			DefaultFlexiColumnModel column = new DefaultFlexiColumnModel(true, colName, colIndex, true, colName);
			column.setHeaderLabel(box.getTitle());
			columnsModel.addFlexiColumnModel(column);
		}

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.totalPoints.i18nKey(), Cols.totalPoints.ordinal(), true, "points"));
		columnsModel.addFlexiColumnModel(new StaticFlexiColumnModel("edit.checkbox", translate("edit.checkbox"), "edit"));
		
		String[] keys = null;
		String[] values = null;
		if(userCourseEnv instanceof UserCourseEnvironmentImpl) {
			UserCourseEnvironmentImpl env = (UserCourseEnvironmentImpl)userCourseEnv;
			List<BusinessGroup> coachedGroups = env.getCoachedGroups();
			keys = new String[coachedGroups.size() + 1];
			values = new String[coachedGroups.size() + 1];
			
			keys[0] = "all";
			values[0] = translate("filter.all");
			for(int k=0; k<coachedGroups.size(); k++) {
				BusinessGroup group = coachedGroups.get(k);
				keys[k+1] = group.getKey().toString();
				values[k+1] = group.getName();
			}
		}
		
		List<CheckListAssessmentRow> datas = loadDatas();
		model = new CheckListAssessmentDataModel(datas, columnsModel);
		table = uifactory.addTableElement(ureq, getWindowControl(), "checkbox-list", model, getTranslator(), formLayout);
		table.setFilterKeysAndValues("participants", keys, values);
		table.setExportEnabled(true);
		
		pdfExport = uifactory.addFormLink("pdf.export", formLayout, Link.BUTTON);
		editButton = uifactory.addFormLink("edit", formLayout, Link.BUTTON);
		saveButton = uifactory.addFormSubmitButton("save", formLayout);
		saveButton.getComponent().setSpanAsDomReplaceable(true);
		saveButton.setVisible(false);
		cancelButton = uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		cancelButton.setVisible(false);
	}
	
	private List<CheckListAssessmentRow> loadDatas() {
		if(!(userCourseEnv instanceof UserCourseEnvironmentImpl)) {
			return Collections.emptyList();
		}

		UserCourseEnvironmentImpl env = (UserCourseEnvironmentImpl)userCourseEnv;
		CheckboxList list = (CheckboxList)config.get(CheckListCourseNode.CONFIG_KEY_CHECKBOX);
		List<Checkbox> checkboxList = list.getList();

		List<SecurityGroup> secGroups = new ArrayList<SecurityGroup>();
		Map<Long,Long> groupToSecGroupKey = new HashMap<Long,Long>();

		RepositoryEntry re = env.getCourseRepositoryEntry();
		boolean courseTutor = securityManager.isIdentityInSecurityGroup(getIdentity(), re.getTutorGroup());
		
		Set<Long> missingIdentityKeys = new HashSet<>();
		if(courseTutor) {
			secGroups.add(re.getParticipantGroup());
			List<RepositoryEntryMembership> repoMemberships = repositoryManager.getRepositoryEntryMembership(re);
			for(RepositoryEntryMembership repoMembership:repoMemberships) {
				if(repoMembership.getParticipantRepoKey() == null) continue;
				missingIdentityKeys.add(repoMembership.getIdentityKey());
			}
		}

		List<BusinessGroup> coachedGroups = env.getCoachedGroups();
		for(BusinessGroup group:coachedGroups) {
			secGroups.add(group.getPartipiciantGroup());
			groupToSecGroupKey.put(group.getKey(), group.getPartipiciantGroup().getKey());
		}

		List<AssessmentData> dataList = checkboxManager.getAssessmentDatas(courseOres, courseNode.getIdent(), secGroups);
		List<CheckListAssessmentRow> boxList = getAssessmentDataViews(dataList, checkboxList);
		Map<Long,CheckListAssessmentRow> identityToView = new HashMap<>();
		for(CheckListAssessmentRow box:boxList) {
			identityToView.put(box.getIdentityKey(), box);
			missingIdentityKeys.remove(box.getIdentityKey());
		}
		
		List<BusinessGroupMembership> memberships = businessGroupService.getBusinessGroupsMembership(coachedGroups);
		for(BusinessGroupMembership membership:memberships) {
			if(!membership.isParticipant()) continue;
			Long identityKey = membership.getIdentityKey();
			if(!identityToView.containsKey(identityKey)) {
				missingIdentityKeys.add(identityKey);
			}
		}

		List<Identity> missingIdentities = securityManager.loadIdentityByKeys(missingIdentityKeys);
		for(Identity missingIdentity:missingIdentities) {
			CheckListAssessmentRow view = new CheckListAssessmentRow(missingIdentity, null, null, userPropertyHandlers, getLocale());
			identityToView.put(missingIdentity.getKey(), view);
		}
		
		for(BusinessGroupMembership membership:memberships) {
			if(!membership.isParticipant()) continue;
			CheckListAssessmentRow view = identityToView.get(membership.getIdentityKey());
			if(view != null) {
				view.addGroupKey(membership.getGroupKey());
			}
		}
		
		List<CheckListAssessmentRow> views = new ArrayList<>();
		views.addAll(identityToView.values());
		return views;
	}
	
	private List<CheckListAssessmentRow> getAssessmentDataViews(List<AssessmentData> datas, List<Checkbox> checkbox) {
		
		List<CheckListAssessmentRow> dataViews = new ArrayList<>();
		
		int numOfcheckbox = checkbox.size();
		Map<String,Integer> indexed = new HashMap<String,Integer>();
		for(int i=numOfcheckbox; i-->0; ) {
			indexed.put(checkbox.get(i).getCheckboxId(), new Integer(i));
		}
		
		for(AssessmentData data:datas) {
			Boolean[] checkBool = new Boolean[numOfcheckbox];
			float totalPoints = 0.0f;
			for(DBCheck check:data.getChecks()) {
				Float score = check.getScore();
				if(score != null) {
					totalPoints += score.floatValue();
				}
				
				if(check.getChecked() == null) continue;
				
				Integer index = indexed.get(check.getCheckbox().getCheckboxId());
				if(index != null) {
					int i = index.intValue();
					if(i >= 0 && i<numOfcheckbox) {
						checkBool[i] = check.getChecked();
					}
				}
			}
			CheckListAssessmentRow row = new CheckListAssessmentRow(data.getIdentity(), checkBool, totalPoints, userPropertyHandlers, getLocale());
			dataViews.add(row);
		}
		return dataViews;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		saveButton.setVisible(false);
		cancelButton.setVisible(false);
		editButton.setVisible(true);
		doSave();
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		saveButton.setVisible(false);
		cancelButton.setVisible(false);
		editButton.setVisible(true);
		doDisableEditingMode();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(table == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if("edit".equals(cmd)) {
					CheckListAssessmentRow row = model.getObject(se.getIndex());
					doOpenEdit(ureq, row);
				} else if(UserConstants.FIRSTNAME.equals(cmd) || UserConstants.LASTNAME.equals(cmd)) {
					CheckListAssessmentRow row = model.getObject(se.getIndex());
					doOpenIdentity(ureq, row);
				}
			}
		} else if(pdfExport == source) {
			doExportPDF(ureq);
		} else if(editButton == source) {
			saveButton.setVisible(true);
			cancelButton.setVisible(true);
			editButton.setVisible(false);
			doEdit();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(editCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editCtrl);
		removeAsListenerAndDispose(cmc);
		editCtrl = null;
		cmc = null;
	}

	@Override
	protected void doDispose() {
		//
	}
	
	private void doEdit() {
		boolean edit = table.isEditMode();
		if(edit) {
			doDisableEditingMode();
		} else {
			List<CheckListAssessmentRow> rows = model.getBackedUpRows();
			int numOfCheckbox = checkboxList.getNumOfCheckbox();
			
			for(CheckListAssessmentRow row:rows) {
				Boolean[] checked = row.getChecked();
				MultipleSelectionElement[] checkedEls = new MultipleSelectionElement[numOfCheckbox];
				for(int i=0; i<numOfCheckbox; i++) {
					String checkName = "c" + i + "-" + row.getIdentityKey();
					checkedEls[i] = uifactory.addCheckboxesHorizontal(checkName, null, flc, onKeys, onValues, null);
					if(checked != null && i<checked.length && checked[i] != null) {
						checkedEls[i].select(onKeys[0], checked[i].booleanValue());
					}
				}
				row.setCheckedEl(checkedEls);
			}
			table.setEditMode(true);
		}
	}
	
	private void doSave() {
		int numOfCheckbox = checkboxList.getNumOfCheckbox();
		List<CheckListAssessmentRow> rows = model.getBackedUpRows();
		List<AssessmentBatch> batchElements = new ArrayList<>();
		for(CheckListAssessmentRow row:rows) {
			Boolean[] checked = row.getChecked();
			Boolean[] editedChecked = new Boolean[numOfCheckbox];
			MultipleSelectionElement[] checkedEls = row.getCheckedEl();
			if(checkedEls != null) {
				for(int i=0; i<numOfCheckbox; i++) {
					MultipleSelectionElement checkEl = checkedEls[i];
					boolean editedValue = checkEl.isAtLeastSelected(1);
					editedChecked[i] = new Boolean(editedValue);
					
					boolean currentValue;
					if(checked != null && checked.length > 0 && i<checked.length && checked[i] != null) {
						currentValue = checked[i].booleanValue();
					} else {
						currentValue = false;
					}
					
					if(editedValue != currentValue) {
						Checkbox checkbox = checkboxList.getList().get(i);
						String checkboxId = checkbox.getCheckboxId();
						batchElements.add(new AssessmentBatch(row.getIdentityKey(), checkboxId, checkbox.getPoints(), editedValue));
					}

					flc.remove(checkEl);
				}
			}
			row.setCheckedEl(null);
			row.setChecked(editedChecked);
		}
		doDisableEditingMode();
		checkboxManager.check(courseOres, courseNode.getIdent(), batchElements);
	}

	
	private void doDisableEditingMode() {
		table.setEditMode(false);
		List<CheckListAssessmentRow> rows = model.getBackedUpRows();
		for(CheckListAssessmentRow row:rows) {
			MultipleSelectionElement[] checkedEls = row.getCheckedEl();
			if(checkedEls != null) {
				for(MultipleSelectionElement checkEl:checkedEls) {
					flc.remove(checkEl);
				}
			}
			row.setCheckedEl(null);
		}
	}
	
	private void doExportPDF(UserRequest ureq) {
		try {
			String name = courseNode.getShortTitle();
			CheckboxPDFExport pdfExport = new CheckboxPDFExport(name, getTranslator(), userPropertyHandlers);
			pdfExport.setAuthor(userManager.getUserDisplayName(getIdentity()));
			pdfExport.setTitle(courseNode.getShortTitle());
			pdfExport.setSubject(courseNode.getLongTitle());
			pdfExport.setObjectives(courseNode.getLearningObjectives());
			pdfExport.create(checkboxList, model);
			ureq.getDispatchResult().setResultingMediaResource(pdfExport);
		} catch (IOException | COSVisitorException | TransformerException e) {
			logError("", e);
		}
	}
	
	private void doOpenIdentity(UserRequest ureq, CheckListAssessmentRow row) {
		String businessPath = "[Identity:" + row.getIdentityKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private void doOpenEdit(UserRequest ureq, CheckListAssessmentRow row) {
		if(editCtrl != null) return;
		
		Identity assessedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
		editCtrl = new AssessedIdentityOverviewController(ureq, getWindowControl(), assessedIdentity, courseOres, courseNode);
		listenTo(editCtrl);

		String title = courseNode.getShortTitle();
		Component content = editCtrl.getInitialComponent();
		cmc = new CloseableModalController(getWindowControl(), "close", content, true, title);
		listenTo(cmc);
		cmc.activate();
	}
}