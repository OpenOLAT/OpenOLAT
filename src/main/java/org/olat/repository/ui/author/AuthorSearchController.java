
package org.olat.repository.ui.author;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
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
import org.olat.ims.qti.fileresource.SurveyFileResource;
import org.olat.ims.qti.fileresource.TestFileResource;
import org.olat.portfolio.EPTemplateMapResource;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;

/**
 * 
 * Initial date: 02.05.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AuthorSearchController extends FormBasicController{

	private TextElement id; // only for admins
	private TextElement displayName;
	private TextElement author;
	private TextElement description;
	private TextElement externalId;
	private TextElement externalRef;
	private SingleSelection types;
	private FormLink searchButton;
	
	private String[] limitTypes;
	private boolean isAdmin;
	private final boolean managedEnabled;

	public AuthorSearchController(UserRequest ureq, WindowControl wControl, boolean isAdmin, Form form) {
		super(ureq, wControl, LAYOUT_CUSTOM, "search", form);
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		this.isAdmin = isAdmin;
		managedEnabled = CoreSpringFactory.getImpl(RepositoryModule.class).isManagedRepositoryEntries();
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer leftContainer = FormLayoutContainer.createDefaultFormLayout("left_1", getTranslator());
		leftContainer.setRootForm(mainForm);
		formLayout.add(leftContainer);

		displayName = uifactory.addTextElement("cif_displayname", "cif.displayname", 255, "", leftContainer);
		displayName.setElementCssClass("o_sel_repo_search_displayname");
		displayName.setFocus(true);
		
		author = uifactory.addTextElement("cif_author", "cif.author", 255, "", formLayout);
		author.setElementCssClass("o_sel_repo_search_author");

		description = uifactory.addTextElement("cif_description", "cif.description", 255, "", leftContainer);
		description.setElementCssClass("o_sel_repo_search_description");

		List<String> typeList = getResources();
		String[] typeKeys = typeList.toArray(new String[typeList.size()]);
		String[] typeValues = getTranslatedResources(typeList);
		types = uifactory.addDropdownSingleselect("cif.type", "cif.type", leftContainer, typeKeys, typeValues, null);
		
		
		FormLayoutContainer rightContainer = FormLayoutContainer.createDefaultFormLayout("right_1", getTranslator());
		rightContainer.setRootForm(mainForm);
		formLayout.add(rightContainer);
		
		id = uifactory.addTextElement("cif_id", "cif.id", 12, "", rightContainer);
		id.setElementCssClass("o_sel_repo_search_id");
		id.setVisible(isAdmin);
		id.setRegexMatchCheck("\\d*", "search.id.format");
		
		externalId = uifactory.addTextElement("cif_extid", "cif.externalid", 128, "", rightContainer);
		externalId.setElementCssClass("o_sel_repo_search_external_id");
		externalId.setVisible(managedEnabled);

		externalRef = uifactory.addTextElement("cif_extref", "cif.externalref", 128, "", rightContainer);
		externalRef.setElementCssClass("o_sel_repo_search_external_ref");
		externalRef.setVisible(managedEnabled);

		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		searchButton = uifactory.addFormLink("search", buttonLayout, Link.BUTTON);
	}

	@Override
	protected void doDispose() {
		//
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
	public Long getId() {
		if (!hasId()) {
			return null;
		}
		return new Long(id.getValue());
	}
	
	public String getExternalId() {
		return externalId.getValue();
	}
	
	public String getExternalRef() {
		return externalRef.getValue();
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
	 * @return Descritpion field value.
	 */
	public String getDescription() {
		return description.getValue();
	}

	/**
	 * @return Limiting type selections.
	 */
	public String getRestrictedType() {
		if(types.isOneSelected()) {
			return types.getSelectedKey();
		} else if (limitTypes != null && limitTypes.length > 0) {
			return limitTypes[0];
		}
		return null;
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		if (displayName.isEmpty() && author.isEmpty() && description.isEmpty() && (id != null && id.isEmpty())
				&& externalId.isEmpty() && externalRef.isEmpty())	{
			showWarning("cif.error.allempty", null);
			return false;
		}
		return true;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireSearchEvent(ureq);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent (ureq, Event.CANCELLED_EVENT); 
	}
	
	@Override
	protected void formInnerEvent (UserRequest ureq, FormItem source, FormEvent event) {
		if (source == searchButton) {
			fireSearchEvent(ureq);
		}
	}
	
	private void fireSearchEvent(UserRequest ureq) {
		SearchEvent e = new SearchEvent();
		e.setId(getId());
		e.setAuthor(getAuthor());
		e.setDisplayname(getDisplayName());
		e.setExternalId(getExternalId());
		e.setExternalRef(getExternalRef());
		e.setType(getRestrictedType());
		fireEvent(ureq, e);
	}

	private String[] getTranslatedResources(List<String> resources) {
		List<String> l = new ArrayList<String>();
		for(String key: resources){
			l.add(translate(key));
		}
		return l.toArray(new String[0]);
	}
	
	private List<String> getResources() {
		List<String> resources = new ArrayList<String>();
		resources.add(CourseModule.getCourseTypeName());
		resources.add(ImsCPFileResource.TYPE_NAME);
		resources.add(ScormCPFileResource.TYPE_NAME);
		resources.add(WikiResource.TYPE_NAME);
		resources.add(PodcastFileResource.TYPE_NAME);
		resources.add(BlogFileResource.TYPE_NAME);
		resources.add(TestFileResource.TYPE_NAME);
		resources.add(SurveyFileResource.TYPE_NAME);
		resources.add(EPTemplateMapResource.TYPE_NAME);
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