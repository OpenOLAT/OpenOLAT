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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.group.ui.wizard;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.choice.Choice;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.table.GenericObjectArrayTableDataModel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.choice.ChoiceController;
import org.olat.core.gui.control.generic.wizard.WizardController;
import org.olat.core.gui.media.CleanupAfterDeliveryFileMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Roles;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.resource.OLATResource;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Description:<br>
 * Wizard for choosing the format of the member list archive. <p>
 * 
 * First step: choose the interest groups/areas <p>
 * Second step: choose the columns for the user info (e.g. username, firstname, lastname, ...) <p>
 * Third step: choose the output format type, either output all members in a single .xls file,
 * or create a zip with a .xls file per group. <p>
 * Fourth step: Download file and cleanup temp file upon dispose.
 * 
 * <P>
 * Initial Date:  30.07.2007 <br>
 * @author Lavinia Dumitrescu
 */
public class MemberListWizardController extends BasicController {
					
	private OLATResource resource;
	private ChoiceController colsChoiceController;
	private Choice groupsOrAreaChoice;		
	private ChoiceController outputChoiceController;
	
	private Panel main;
	private WizardController wizardController;
	private int wizardSteps = 4;
	
	public final static String GROUPS_MEMBERS = "g_m";
	public final static String AREAS_MEMBERS = "a_m";
	private String wizardType = GROUPS_MEMBERS; //default
	
	private VelocityContainer velocityContainer2;
	private VelocityContainer velocityContainer3;
	private VelocityContainer velocityContainer4;
	private Link backToFirstChoice;
	private Link backToSecondChoice;
	private Link showFileLink;
	
	private List<String> columList;
	private List<BusinessGroup> groupList;		
	private List<BGArea> areaList;		
	private String archiveType;
	private MediaResource archiveMediaResource;
	private static final String usageIdentifyer = MemberListWizardController.class.getCanonicalName();
	private Translator propertyHandlerTranslator;
	
	private final BusinessGroupService businessGroupService;
	private final BGAreaManager areaManager;
	
	/**
	 * 
	 * @param ureq
	 * @param wControl
	 * @param context
	 * @param type
	 */
	public MemberListWizardController(UserRequest ureq, WindowControl wControl, OLATResource resource, String type) {
		super(ureq, wControl);
		
		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		areaManager = CoreSpringFactory.getImpl(BGAreaManager.class);
		this.resource = resource;
		propertyHandlerTranslator = UserManager.getInstance().getPropertyHandlerTranslator(getTranslator());
		
		if(GROUPS_MEMBERS.equals(type) || AREAS_MEMBERS.equals(type)) {
		  this.wizardType = type;
		}
		
		columList = new ArrayList<String>();
		groupList = new ArrayList<BusinessGroup>();	
		areaList = new ArrayList<BGArea>();
		main = new Panel("main");
				
		//init wizard step 1
		groupsOrAreaChoice = new Choice("groupsOrAreaChoice", getTranslator());
		groupsOrAreaChoice.setTableDataModel(getGroupOrAreaChoiceTableDataModel(resource));
		groupsOrAreaChoice.addListener(this);
		groupsOrAreaChoice.setSubmitKey("next");
		
    //init wizard step 2		
		boolean singleSelection = false;
		boolean layoutVertical = true;
		String[] keys = getColsChoiceKeys(ureq);	
		String[] selectedKeys = getFirstN(keys, 4);
		colsChoiceController = new ChoiceController(ureq, getWindowControl(), keys, getTranslatedKeys(propertyHandlerTranslator,keys), selectedKeys, singleSelection, layoutVertical, "next");		
		this.listenTo(colsChoiceController);
				
		wizardController = new WizardController(ureq, wControl, wizardSteps);		
		this.listenTo(wizardController);
		wizardController.setWizardTitle(translate("memberlistwizard.title"));		
		if (GROUPS_MEMBERS.equals(wizardType)) {
			wizardController.setNextWizardStep(translate("memberlistwizard.groupchoice"), groupsOrAreaChoice);
		} else if (AREAS_MEMBERS.equals(wizardType)) {
			wizardController.setNextWizardStep(translate("memberlistwizard.areachoice"), groupsOrAreaChoice);
		}	
		main.setContent(wizardController.getInitialComponent());		
		this.putInitialPanel(main);
		
    // step 2		
		velocityContainer2 = this.createVelocityContainer("listWizardStep2");
		backToFirstChoice = LinkFactory.createLinkBack(velocityContainer2,this); 
    // step 3		
		velocityContainer3 = this.createVelocityContainer("listWizardStep3");
		backToSecondChoice = LinkFactory.createLinkBack(velocityContainer3,this);
		//last step		
		velocityContainer4 = this.createVelocityContainer("listWizardStep4");
		showFileLink = LinkFactory.createButton("showfile", velocityContainer4, this);
		//mark that this link starts a download
		LinkFactory.markDownloadLink(showFileLink);
	}
	
	/**
	 * 
	 * @param keys
	 * @param n
	 * @return an array with the first n elements of the input array
	 */
	private String[] getFirstN(String[] keys, int n) {			
		if(n<0 || n>keys.length) {
			n = keys.length;
		}
		String[] selKeys = new String[n];
		for(int i=0; i<n; i++) {
			selKeys[i] = keys[i];
		}
		return selKeys;
	}
	
	private String[] getColsChoiceKeys(UserRequest ureq) {	
		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = (roles.isAuthor() || roles.isGroupManager() || roles.isUserManager() || roles.isOLATAdmin());	
		List<UserPropertyHandler> userPropertyHandlers = UserManager.getInstance().getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
		Iterator<UserPropertyHandler> propertyIterator =  userPropertyHandlers.iterator();
		ArrayList<String> array = new ArrayList<String>();
		//add username first, next the user properties
		array.add("username");
		while(propertyIterator.hasNext()) {
			array.add(propertyIterator.next().i18nColumnDescriptorLabelKey());
		}
		String[] keys = new String[array.size()];
		keys = array.toArray(keys);
		return keys;		
	}
	
	private String[] getTranslatedKeys(Translator keyTranslator,String[] keys) {
		int size = keys.length;
		String[] translated = new String[size];
		for(int i=0; i<size; i++) {
			translated[i] = keyTranslator.translate(keys[i]);
		}
		return translated;
	}
	
	
	/**
	 * Creates a <code>Choice</code> <code>TableDataModel</code> for the group/area choice. <br>
	 * It contains two columns: booleans (true per default) on the first column, and ObjectWrappers for the
	 * second column.
	 * @param context
	 * @return a GenericObjectArrayTableDataModel instead of a TableDataModel since it has to provide a setValueAt method.
	 */
	private GenericObjectArrayTableDataModel getGroupOrAreaChoiceTableDataModel(OLATResource resource) {
		List<Object[]> objectArrays = new ArrayList<Object[]>();
		if (GROUPS_MEMBERS.equals(wizardType)) {
			List<BusinessGroup> groups = businessGroupService.findBusinessGroups(null, resource, 0, -1);
			Collections.sort(groups, new Comparator<BusinessGroup>() {
				@Override
				public int compare(BusinessGroup g1, BusinessGroup g2) {
					return g1.getName().compareTo(g2.getName());
				}
			});
			for (BusinessGroup group : groups) {
				Object[] groupChoiceRowData = new Object[2];
				groupChoiceRowData[0] = new Boolean(true);
				groupChoiceRowData[1] = new ObjectWrapper(group);
				objectArrays.add(groupChoiceRowData);
			}
		} else if (AREAS_MEMBERS.equals(wizardType)) {
			List<BGArea> areas = areaManager.findBGAreasInContext(resource);
			Collections.sort(areas, new Comparator<BGArea>() {
				@Override
				public int compare(BGArea a1, BGArea a2) {
					return a1.getName().compareTo(a2.getName());
				}
			});
			for (BGArea area:areas) {
				Object[] groupChoiceRowData = new Object[2];
				groupChoiceRowData[0] = new Boolean(true);
				groupChoiceRowData[1] = new ObjectWrapper(area);
				objectArrays.add(groupChoiceRowData);
			}
		}
		GenericObjectArrayTableDataModel tableModel = new GenericObjectArrayTableDataModel(objectArrays, 2);
		return tableModel;
	}
		
	
	public void event(UserRequest ureq, Component source, Event event) {
		// default wizard will listen to cancel wizard event
		wizardController.event(ureq, source, event);
		// wizard steps events
		if (source == groupsOrAreaChoice) {
			if (event == Choice.EVNT_VALIDATION_OK) {
				List<Integer> selRows = groupsOrAreaChoice.getSelectedRows();
				if (selRows.size() == 0) {
					if (GROUPS_MEMBERS.equals(wizardType)) {						
						this.showError("error.selectatleastonegroup");
					} else if (AREAS_MEMBERS.equals(wizardType)) {						
						this.showError("error.selectatleastonearea");
					}
				} else {
					if (GROUPS_MEMBERS.equals(wizardType)) {						
						this.setGroupList(getSelectedValues(groupsOrAreaChoice));
					} else if (AREAS_MEMBERS.equals(wizardType)) {						
						this.setAreaList(getSelectedValues(groupsOrAreaChoice));				
					}								
					velocityContainer2.put("colsChoice", colsChoiceController.getInitialComponent());				
					wizardController.setNextWizardStep(translate("memberlistwizard.colchoice"), velocityContainer2);					
				}
			}
		} else if (source == backToFirstChoice) {
			syncTableModelWithSelection(groupsOrAreaChoice);
			if (GROUPS_MEMBERS.equals(wizardType)) {
				wizardController.setBackWizardStep(translate("memberlistwizard.groupchoice"), groupsOrAreaChoice);
			} else if (AREAS_MEMBERS.equals(wizardType)) {
				wizardController.setBackWizardStep(translate("memberlistwizard.areachoice"), groupsOrAreaChoice);
			}
		} else if (source == backToSecondChoice) {			
			wizardController.setBackWizardStep(translate("memberlistwizard.colchoice"), velocityContainer2);		
		} else if (source == showFileLink) {			
			ureq.getDispatchResult().setResultingMediaResource(this.getArchiveMediaResource());
		}
	}
	
	/**
	 * 
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if(source == wizardController) {
			if (event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, event);
			}
		}	else if (source == outputChoiceController) {
			if (event == Event.DONE_EVENT) {
				List<String> selected = outputChoiceController.getSelectedEntries();
				if(selected.size()==0) {					
					this.showError("error.selectonevalue");
				} else {					
					this.setArchiveType(selected.iterator().next());
					
					File outputFile = archiveMembers(ureq);
					velocityContainer4.contextPut("filename", outputFile.getName());
					wizardController.setWizardTitle(translate("memberlistwizard.finished.title"));
					wizardController.setNextWizardStep(translate("memberlistwizard.finished"), velocityContainer4);					
					this.setArchiveMediaResource(new CleanupAfterDeliveryFileMediaResource(outputFile));
				}
			}
		} else if (source == colsChoiceController) {
			if (event == Event.DONE_EVENT) {
				List<String> selected = colsChoiceController.getSelectedEntries();
				if (selected.size() == 0) {					
					this.showError("error.selectatleastonecolumn");
				} else {					
					this.setColumList(selected);
					
					boolean singleSelection = true;
					boolean layoutVertical = true;
					String[] keys = new String[]{"memberlistwizard.archive.type.filePerGroupOrAreaInclGroupMembership", "memberlistwizard.archive.type.filePerGroupOrArea", "memberlistwizard.archive.type.allInOne"};
					String[] translatedKeys = new String[]{translate("memberlistwizard.archive.type.filePerGroupOrAreaInclGroupMembership"), translate("memberlistwizard.archive.type.filePerGroupOrArea"),translate("memberlistwizard.archive.type.allInOne")};
					String[] selectedKeys = new String[]{"memberlistwizard.archive.type.allInOne"};
					outputChoiceController = new ChoiceController(ureq, getWindowControl(), keys, translatedKeys, selectedKeys, singleSelection, layoutVertical, "next");					
					this.listenTo(outputChoiceController);
					velocityContainer3.put("outputChoice", outputChoiceController.getInitialComponent());					
					wizardController.setNextWizardStep(translate("memberlistwizard.outputchoice"), velocityContainer3);
				}
			}			
		}
	}
	
	
	/**
	 * Calls the archiveMembers method on <code>BusinessGroupArchiver</code>.
	 * @return the output file.
	 */
	private File archiveMembers(UserRequest ureq) {
		File outputFile = null;
		List<String> columnList = getColumList();
		List<BusinessGroup> groupList = getGroupList();
		String archiveType = getArchiveType();
		List<BGArea> areaList = getAreaList();
		
		Locale userLocale = ureq.getLocale();
    String charset = UserManager.getInstance().getUserCharset(ureq.getIdentity());
		
		if(GROUPS_MEMBERS.equals(wizardType)) {
			outputFile = businessGroupService.archiveGroupMembers(resource, columnList, groupList, archiveType, userLocale, charset);	
		} else if(AREAS_MEMBERS.equals(wizardType)) {
			outputFile = areaManager.archiveAreaMembers(resource, columnList, areaList, archiveType, userLocale, charset);
		}			
		return outputFile;
	}	
	
	/**
	 * Gets the list of the values in the second column of the tableDataModel of the input "choice",
	 * where the first column value is true.
	 * @param choice
	 * @return a list with the selected values of the input choice component.
	 */
	private List getSelectedValues(Choice choice) {	
		List<Object> selValues = new ArrayList<Object>();
		List<Integer> selRowsIndexes = choice.getSelectedRows();
		int numRows = choice.getTableDataModel().getRowCount();
		for(int i=0; i<numRows; i++) {
			if(selRowsIndexes.size() == 0) {
				boolean booleanValue = ((Boolean)choice.getTableDataModel().getValueAt(i, 0)).booleanValue();
				if(booleanValue) {
					ObjectWrapper objWrapper = (ObjectWrapper)choice.getTableDataModel().getValueAt(i, 1); 
					selValues.add(objWrapper.getWrappedObj());
				}
			} else if(selRowsIndexes.contains(new Integer(i))) {
				ObjectWrapper objWrapper = (ObjectWrapper)choice.getTableDataModel().getValueAt(i, 1); 
				selValues.add(objWrapper.getWrappedObj());
			}					
		}		
		return selValues;
	}
	
	/**
	 * Synchronizes the Choice's tableDataModel with its selection/removed status.
	 * @param choice
	 */
	private void syncTableModelWithSelection(Choice choice) {
		GenericObjectArrayTableDataModel tableDataModel = (GenericObjectArrayTableDataModel)choice.getTableDataModel();
		List<Integer> removedRowsIndexes = choice.getRemovedRows();
		if(removedRowsIndexes.size()>0) {
			int numRows = choice.getTableDataModel().getRowCount();
			for(int i=0; i<numRows; i++) {
				if(removedRowsIndexes.contains(new Integer(i))) {				  
				  tableDataModel.setValueAt(new Boolean(false), i, 0);
				}
			}
		}		
	}
	
	private String getArchiveType() {
		return archiveType;
	}
	
	private void setArchiveType(String archiveType) {
		this.archiveType = archiveType;
	}

	private List<String> getColumList() {
		return columList;
	}
	
	private void setColumList(List<String> columList) {
		this.columList = columList;		
	}
	
	private List<BusinessGroup> getGroupList() {
		return groupList;
	}	
	
	private void setGroupList(List<BusinessGroup> groupList) {
		this.groupList = groupList;
	}
	
	private List<BGArea> getAreaList() {
		return areaList;
	}

	private void setAreaList(List<BGArea> areaList) {
		this.areaList = areaList;
	}	
	
	private MediaResource getArchiveMediaResource() {
		return archiveMediaResource;
	}

	private void setArchiveMediaResource(MediaResource archiveMediaResource) {
		this.archiveMediaResource = archiveMediaResource;
	}
	
	protected void doDispose() {
		//child controllers registrered with listenTo() are disposed in BasicController
	}
		
	/**
	 * 
	 * Description:<br>
	 * Wraps <code>BusinessGroup</code>, <code>BGArea</code>, and Strings. <p>
	 * If more objects types to wrap adapt the toString method.
	 * 
	 * <P>
	 * Initial Date:  30.07.2007 <br>
	 * @author Lavinia Dumitrescu
	 */
	private class ObjectWrapper {
		private Object wrappedObj;
		
		public ObjectWrapper(Object wrappedObj) {
			this.wrappedObj = wrappedObj;
		}
		
		public String toString() {
			if(wrappedObj instanceof BusinessGroup) {
				return ((BusinessGroup)wrappedObj).getName();
			} else if (wrappedObj instanceof BGArea) {
				return ((BGArea)wrappedObj).getName();
			} else if (wrappedObj instanceof String) {
				return translate((String)wrappedObj);
			} else {
				return wrappedObj.toString();
			}
		}
		public Object getWrappedObj() {
			return wrappedObj;
		}
	}
}
