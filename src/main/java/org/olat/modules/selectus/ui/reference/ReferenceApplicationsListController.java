/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.reference;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingTableOption;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.events.SelectApplicationEvent;
import org.olat.modules.selectus.ui.reference.ApplicationChooserDataModel.AppCols;

/**
 * 
 * Initial date: 8 mars 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReferenceApplicationsListController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private ReferenceApplicationsListDataModel tableModel;
	
	private final Position position;
	private final List<Application> applicationsList;
	
	@Autowired
	private RecruitingModule recruitingModule;
	
	public ReferenceApplicationsListController(UserRequest ureq, WindowControl wControl,
			Position position, List<Application> applicationsList) {
		super(ureq, wControl, "applications", Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.position = position;
		this.applicationsList = applicationsList;
		
		initForm(ureq);
	}
	
	public int indexOf(Application application) {
		List<Application> applications = tableModel.getObjects();
		return applications.indexOf(application);
	}
	
	public Application getApplication(int index) {
		List<Application> applications = tableModel.getObjects();
		if(index >= 0 && index < applications.size()) {
			return applications.get(index);
		}
		return null;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("applications.list.description");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		initColumnModel(AppCols.title, recruitingModule.getTableReferenceToApplicationTitleOption(), columnsModel);
		initColumnModel(AppCols.firstName, recruitingModule.getTableReferenceToApplicationFirstNameOption(), columnsModel);
		initColumnModel(AppCols.lastName, recruitingModule.getTableReferenceToApplicationLastNameOption(), columnsModel);
		if(position.isApplicationProject()) {
			initColumnModel(AppCols.projectTitle, recruitingModule.getTableReferenceToProjectTitleOption(), columnsModel);
		}

		tableModel = new ReferenceApplicationsListDataModel(columnsModel, getLocale());
		tableModel.setObjects(applicationsList);
		tableEl = uifactory.addTableElement(getWindowControl(), "applications", tableModel, 25, true, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
	}
	
	private void initColumnModel(AppCols field, RecruitingTableOption option, FlexiTableColumnModel columnsModel) {
		if(!option.isDisabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(option.isVisible(), field, "select"));
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				Application application = tableModel.getObject(se.getIndex());
				if(application != null) {
					fireEvent(ureq, new SelectApplicationEvent(application));
				}
			}	
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	

	

}
