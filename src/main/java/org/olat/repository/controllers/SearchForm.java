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

package org.olat.repository.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.course.CourseModule;
import org.olat.fileresource.types.AnimationFileResource;
import org.olat.fileresource.types.BlogFileResource;
import org.olat.fileresource.types.DocFileResource;
import org.olat.fileresource.types.FileResource;
import org.olat.fileresource.types.GlossaryResource;
import org.olat.fileresource.types.ImageFileResource;
import org.olat.fileresource.types.ImsCPFileResource;
import org.olat.fileresource.types.MovieFileResource;
import org.olat.fileresource.types.PdfFileResource;
import org.olat.fileresource.types.PodcastFileResource;
import org.olat.fileresource.types.PowerpointFileResource;
import org.olat.fileresource.types.ScormCPFileResource;
import org.olat.fileresource.types.SharedFolderFileResource;
import org.olat.fileresource.types.SoundFileResource;
import org.olat.fileresource.types.WikiResource;
import org.olat.fileresource.types.XlsFileResource;
import org.olat.repository.RepositoryService;

/**
 * Initial Date:  08.07.2003
 *
 * @author Mike Stock
 * 
 * Comment: The search form captures data to search
 * for a repository entry. The form can be restricted
 * to a special type
 * 
 */
public class SearchForm extends FormBasicController {

	private TextElement id;
	private TextElement displayName;
	private TextElement author;
	private TextElement description;
	private SelectionElement typesSelection;
	private MultipleSelectionElement types;
	private FormLink searchButton;
	
	private String[] limitTypes;
	private boolean withCancel;
	private boolean isAdminSearch = false;
	
	/**
	 * Generic search form.
	 * @param name Internal form name.
	 * @param translator Translator
	 * @param withCancel Display a cancel button?
	 * @param isAdmin Is calling identity an administrator? If yes, allow search by ID
	 * @param limitType Limit searches to a specific type.
	 * @param limitUser Limit searches to a specific user.
	 */
	public SearchForm(UserRequest ureq, WindowControl wControl, boolean withCancel, String[] limitTypes) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));		
		this.withCancel = withCancel;
		this.limitTypes = limitTypes;
		initForm(ureq);
		update();
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		if (displayName.isEmpty() && author.isEmpty() && description.isEmpty() && (id != null && id.isEmpty())) {
			showWarning("cif.error.allempty");
			return false;
		}
		return true;
	}
	
	/**
	 * @return Is ID field available?
	 */
	public boolean hasId() {
		return (id != null && !id.isEmpty());
	}
	
	/**
	 * @return Return value of ID field.
	 */
	public String getId() {
		return id.getValue();
	}

	/**
	 * @return Display name filed value.
	 */
	public String getDisplayName() {
		return displayName.getValue();
	}

	/**
	 * @return Author field value.
	 */
	public String getAuthor() {
		return author.getValue();
	}

	/**
	 * @return Description field value.
	 */
	public String getDescription() {
		return description.getValue();
	}

	/**
	 * @return Limiting type selections.
	 */
	public Collection<String> getRestrictedTypes() {
		
		if (limitTypes != null && limitTypes.length > 0) {
			return new HashSet<>(Arrays.asList(limitTypes));
		}
		
		return types.getSelectedKeys();
	}

	public void setVisible(boolean onoff) {
		flc.setVisible(onoff);
	}

	public boolean isAdminSearch() {
		return isAdminSearch;
	}

	public void setAdminSearch(boolean isAdminSearch) {
		this.isAdminSearch = isAdminSearch;
	}

	@Override
	protected void formOK (UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT); 
	}
	
	@Override
	protected void formCancelled (UserRequest ureq) {
		flc.reset();
		fireEvent (ureq, Event.CANCELLED_EVENT); 
	}
	
	private void update () {
		if (limitTypes != null && limitTypes.length > 0) {
			typesSelection.setVisible(false);
			types.setVisible(false);
		} else {
			types.setVisible(typesSelection.isSelected(0));
			types.uncheckAll();
			flc.setDirty(true);
		}
	}
	
	@Override
	protected void formInnerEvent (UserRequest ureq, FormItem source, FormEvent event) {
		if (source == searchButton) {
			flc.getRootForm().submit(ureq);
		} else if (source == typesSelection && event.getCommand().equals("ONCLICK")) {
			update();
		}
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		displayName = uifactory.addTextElement("cif_displayname", "cif.displayname", 255, "", formLayout);
		displayName.setElementCssClass("o_sel_repo_search_displayname");
		displayName.setFocus(true);
		
		author = uifactory.addTextElement("cif_author", "cif.author", 255, "", formLayout);
		author.setElementCssClass("o_sel_repo_search_author");

		description = uifactory.addTextElement("cif_description", "cif.description", 255, "", formLayout);
		description.setElementCssClass("o_sel_repo_search_description");
		
		id = uifactory.addTextElement("cif_id", "cif.id", 128, "", formLayout);
		id.setElementCssClass("o_sel_repo_search_id");

		typesSelection = uifactory.addCheckboxesVertical("search.limit.type", formLayout, new String[]{"xx"}, new String[]{""}, 1);
		typesSelection.addActionListener(FormEvent.ONCLICK);
		typesSelection.setElementCssClass("o_sel_repo_search_type_limit");
		
		String[] typeKeys = getResources().toArray(new String[0]);
		String[] iconCSS = getCssResources(getResources());
		types = uifactory.addCheckboxesVertical("cif_types", "cif.type", formLayout, typeKeys, getTranslatedResources(getResources()), null, iconCSS, 1);
		types.setElementCssClass("o_sel_repo_search_types");
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		
		searchButton = uifactory.addFormLink("search", buttonLayout, Link.BUTTON);
		if (withCancel) {
			uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		}
		
		update();
	}
	
	private String[] getTranslatedResources(List<String> resources) {
		List<String> l = new ArrayList<>();
		for(String key: resources){
			l.add(translate(key));
		}
		return l.toArray(new String[0]);
	}
	
	private String[] getCssResources(List<String> resources) {
		String[] cssClasses= new String[resources.size()];
		int count = 0;
		for(String key: resources) {
			StringBuilder sb = new StringBuilder();
			sb.append("o_icon o_icon-fw o_").append(key.replace(".", "-"))
			  .append("_icon o_sel_repo_search_")
			  .append(key.toLowerCase().replace(".", "_"));
			cssClasses[count++] = sb.toString();
		}
		return cssClasses;
	}
	
	private List<String> getResources() {
		List<String> resources = new ArrayList<>();
		resources.add(CourseModule.getCourseTypeName());
		resources.add(ImsCPFileResource.TYPE_NAME);
		resources.add(ScormCPFileResource.TYPE_NAME);
		resources.add(WikiResource.TYPE_NAME);
		resources.add(PodcastFileResource.TYPE_NAME);
		resources.add(BlogFileResource.TYPE_NAME);
		resources.add(SharedFolderFileResource.TYPE_NAME);
		resources.add(GlossaryResource.TYPE_NAME);
		resources.add(PdfFileResource.TYPE_NAME);
		resources.add(XlsFileResource.TYPE_NAME);
		resources.add(PowerpointFileResource.TYPE_NAME);
		resources.add(DocFileResource.TYPE_NAME);
		resources.add(AnimationFileResource.TYPE_NAME);
		resources.add(ImageFileResource.TYPE_NAME);
		resources.add(SoundFileResource.TYPE_NAME);
		resources.add(MovieFileResource.TYPE_NAME);
		resources.add(FileResource.GENERIC_TYPE_NAME);
		return resources;
	}

}