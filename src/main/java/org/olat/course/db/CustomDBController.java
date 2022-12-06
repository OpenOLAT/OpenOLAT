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
package org.olat.course.db;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.helpers.Settings;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.ExportUtil;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerExecutor;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorkbookResource;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.properties.Property;
import org.olat.restapi.security.RestSecurityHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Controller to manage a custom DB, export, delete...
 * 
 * <P>
 * Initial Date:  7 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CustomDBController extends FormBasicController {

	private FormLink addDatabase;
	private List<FormLink> resetDbs = new ArrayList<>();
	private List<FormLink> deleteDbs = new ArrayList<>();
	private List<FormLink> exportDbs = new ArrayList<>();
	private FormLayoutContainer dbListLayout;
	
	private CustomDBAddController addController;
	private CloseableModalController cmc;
	
	private final Long courseKey;
	private final boolean readOnly;
	
	@Autowired
	private CourseDBManager courseDbManager;
	
	public CustomDBController(UserRequest ureq, WindowControl wControl, Long courseKey, boolean readOnly) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.courseKey = courseKey;
		this.readOnly = readOnly;
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("customDb.custom_db");
		setFormTitleIconCss("o_icon o_icon_coursedb");
		
		addDatabase = uifactory.addFormLink("command.new_db", formLayout, Link.BUTTON);
		addDatabase.setVisible(!readOnly);

		dbListLayout = FormLayoutContainer.createDefaultFormLayout("dbListLayout", getTranslator());
		formLayout.add(dbListLayout);
		updateDBList(dbListLayout);
	}
	
	public void updateUI() {
		FormItem[] items = new FormItem[dbListLayout.getFormComponents().size()];
		items = dbListLayout.getFormComponents().values().toArray(items);
		for(FormItem item:items) {
			dbListLayout.remove(item);
		}
		initialPanel.setDirty(true);
		updateDBList(dbListLayout);
	}
	
	private void updateDBList(FormItemContainer formLayout) {
		ICourse course = CourseFactory.loadCourse(courseKey);
		CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
		CourseNode rootNode = ((CourseEditorTreeNode)course.getEditorTreeModel().getRootNode()).getCourseNode();
		Property p = cpm.findCourseNodeProperty(rootNode, null, null, CustomDBMainController.CUSTOM_DB);
		
		List<String> databases = new ArrayList<>();
		if(p != null && p.getTextValue() != null) {
			String[] dbs = p.getTextValue().split(":");
			for(String db:dbs) {
				databases.add(db);
			}
		}
			
		List<String> currentlyUsed = courseDbManager.getUsedCategories(course);
		for(String db:currentlyUsed) {
			if(!databases.contains(db)) {
				databases.add(db);
			}
		}
			
		int count = 0;
		for(String db:databases) {
			if(!StringHelper.containsNonWhitespace(db)) continue;
			
			uifactory.addStaticTextElement("category_" + count, "customDb.category", db, formLayout);
			String url = Settings.getServerContextPathURI() + RestSecurityHelper.SUB_CONTEXT + "/repo/courses/" + courseKey + "/db/" + db;
			uifactory.addStaticTextElement("url_" + count, "customDb.url", url, formLayout);
			
			final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout_" + count, getTranslator());
			formLayout.add(buttonLayout);

			FormLink resetDb = uifactory.addFormLink("db-reset_" + count, "customDb.reset", "customDb.reset", buttonLayout, Link.BUTTON_SMALL);
			resetDb.setUserObject(db);
			resetDb.setVisible(!readOnly);
			resetDbs.add(resetDb);
			FormLink deleteDb = uifactory.addFormLink("db-delete_" + count, "delete", "delete", buttonLayout, Link.BUTTON_SMALL);
			deleteDb.setUserObject(db);
			deleteDb.setVisible(!readOnly);
			deleteDbs.add(deleteDb);
			FormLink exportDb = uifactory.addFormLink("db-export_" + count, "customDb.export", "customDb.export", buttonLayout, Link.BUTTON_SMALL);
			exportDb.setUserObject(db);
			exportDbs.add(exportDb);
			
			count++;
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//do nothing as default
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (resetDbs.contains(source)) {
			resetDb((String)source.getUserObject());
		} else if (deleteDbs.contains(source)) {
			deleteDb((String)source.getUserObject());
		} else if (exportDbs.contains(source)) {
			exportDb(ureq, (String)source.getUserObject());
		} else if (source == addDatabase) {
			removeAsListenerAndDispose(addController);
			addController = new CustomDBAddController(ureq, getWindowControl());
			listenTo(addController);
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), addController.getInitialComponent());
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == addController) {
			if(event == Event.DONE_EVENT) {
				String category = addController.getCategory();
				addCustomDb(category);
				updateUI();
			}
			cmc.deactivate();
			disposeAddController();
		}
	}
	
	private void addCustomDb(final String category) {
		final ICourse course = CourseFactory.loadCourse(courseKey);
		CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(course, new SyncerExecutor() {
			@Override
			public void execute() {
				CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
				CourseNode rootNode = ((CourseEditorTreeNode)course.getEditorTreeModel().getRootNode()).getCourseNode();
				Property p = cpm.findCourseNodeProperty(rootNode, null, null, CustomDBMainController.CUSTOM_DB);
				if(p == null) {
					p = cpm.createCourseNodePropertyInstance(rootNode, null, null, CustomDBMainController.CUSTOM_DB, null, null, null, category);
					cpm.saveProperty(p);
				} else {
					String currentDbs = p.getTextValue();
					p.setTextValue(currentDbs + ":" + category);
					cpm.updateProperty(p);
				}
			}
		});
	}
	
	private void disposeAddController() {
		removeAsListenerAndDispose(addController);
		removeAsListenerAndDispose(cmc);
		addController = null;
		cmc = null;
	}
	
	private void resetDb(String category) {
		ICourse course = CourseFactory.loadCourse(courseKey);
		courseDbManager.reset(course, category);
	}
	
	private void deleteDb(String category) {
		ICourse course = CourseFactory.loadCourse(courseKey);
		courseDbManager.reset(course, category);
		deleteCustomDb(course, category);
		updateUI();
	}
	
	private void exportDb(UserRequest ureq, final String category) {
		final ICourse course = CourseFactory.loadCourse(courseKey);
    	String label = ExportUtil.createFileNameWithTimeStamp("DBS_" + course.getCourseTitle(), "xls");

    	MediaResource export =  new OpenXMLWorkbookResource(label) {
			@Override
			protected void generate(OutputStream out) {
				try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
					List<CourseDBEntry> content = courseDbManager.getValues(course, null, category, null);

					OpenXMLWorksheet exportSheet = workbook.nextWorksheet();
					
					//create the headers
					Row headerRow = exportSheet.newRow();
					headerRow.addCell(0, translate("customDb.category"), workbook.getStyles().getHeaderStyle());
					headerRow.addCell(1, translate("customDb.entry.identity"), workbook.getStyles().getHeaderStyle());
					headerRow.addCell(2, translate("customDb.entry.name"), workbook.getStyles().getHeaderStyle());
					headerRow.addCell(3, translate("customDb.entry.value"), workbook.getStyles().getHeaderStyle());

					for (CourseDBEntry entry:content) {
						User user = entry.getIdentity().getUser();
						String name = user.getProperty(UserConstants.FIRSTNAME, null) + " " + user.getProperty(UserConstants.LASTNAME, null); 

						Row dataRow = exportSheet.newRow();
						dataRow.addCell(0, entry.getCategory(), null);
						dataRow.addCell(1, name, null);
						if(StringHelper.containsNonWhitespace(entry.getName())) {
							dataRow.addCell(2, entry.getName(), null);
						}
						if(entry.getValue() != null) {
							dataRow.addCell(3, entry.getValue().toString(), null);
						}
					}
				} catch (IOException e) {
					logError("", e);
				}
			}
		};
		ureq.getDispatchResult().setResultingMediaResource(export);
	}

	private void deleteCustomDb(final ICourse course, final String category) {
		CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(course, new SyncerExecutor() {
			@Override
			public void execute() {
				CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
				CourseNode rootNode = ((CourseEditorTreeNode)course.getEditorTreeModel().getRootNode()).getCourseNode();
				Property p = cpm.findCourseNodeProperty(rootNode, null, null, CustomDBMainController.CUSTOM_DB);
				if(p != null && p.getTextValue() != null) {
					String[] dbs = p.getTextValue().split(":");
					StringBuilder currentDbs = new StringBuilder();
					for(String db:dbs) {
						if(!db.equals(category)) {
							currentDbs.append(db).append(':');
						}
					}
					p.setTextValue(currentDbs.toString());
					cpm.updateProperty(p);
				}
			}
		});
	}
}