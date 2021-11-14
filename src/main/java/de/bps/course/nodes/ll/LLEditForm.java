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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.course.nodes.ll;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.controllers.linkchooser.LinkChooserController;
import org.olat.core.commons.controllers.linkchooser.URLChoosenEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.ValidationError;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.ItemValidatorProvider;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.helpers.Settings;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.editor.NodeEditController;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.modules.ModuleConfiguration;

import de.bps.course.nodes.LLCourseNode;

/**
 * Description:<br>
 * Edit form for link lists.
 *
 * <P>
 * Initial Date: 17.11.2008 <br>
 *
 * @author Marcel Karras (toka@freebits.de)
 */
public class LLEditForm extends FormBasicController {
	
	private static final String BLANK_KEY  = "_blank";
	private static final String SELF_KEY  = "_self";

	private ModuleConfiguration moduleConfig;
	private List<TextElement> lTargetInputList;
	private List<SingleSelection> lHtmlTargetInputList;
	private List<TextElement> lDescriptionInputList;
	private List<TextElement> lCommentInputList;
	private List<FormLink> lDelButtonList;
	private List<FormLink> lCustomMediaButtonList;
	private List<LLModel> linkList;
	private List<FormLink> lAddButtonList;
	private long counter = 0;
	private LinkChooserController mediaChooserController;
	private CloseableModalController mediaDialogBox;
	private LLModel currentLink;
	private final CourseEnvironment courseEnv;

	public LLEditForm(UserRequest ureq, WindowControl wControl, ModuleConfiguration moduleConfig, CourseEnvironment courseEnv) {
		super(ureq, wControl, "editForm");
		this.moduleConfig = moduleConfig;
		// read existing links from config
		linkList = new ArrayList<>(moduleConfig.getList(LLCourseNode.CONF_LINKLIST, LLModel.class));
		// list of all link target text fields
		lTargetInputList = new ArrayList<>(linkList.size());
		// list of all link html target text fields
		lHtmlTargetInputList = new ArrayList<>(linkList.size());
		// list of all link description text fields
		lDescriptionInputList = new ArrayList<>(linkList.size());
		// list of all link comment text fields
		lCommentInputList = new ArrayList<>(linkList.size());
		// list of all link add action buttons
		lAddButtonList = new ArrayList<>(linkList.size());
		// list of all link deletion action buttons
		lDelButtonList = new ArrayList<>(linkList.size());
		//list of all custom media buttons
		lCustomMediaButtonList = new ArrayList<>(linkList.size());
		
		this.courseEnv = courseEnv;

		initForm(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// read data from form elements
		for (int i = 0; i < lTargetInputList.size(); i++) {
			LLModel link = (LLModel) lTargetInputList.get(i).getUserObject();
			String linkValue = lTargetInputList.get(i).getValue();
			if(link.isIntern()) {
				if(!linkValue.contains("://") && !linkValue.startsWith("/")) {
					linkValue = "/".concat(linkValue.trim());
					lTargetInputList.get(i).setValue(linkValue);
				}
			} else if(!linkValue.contains("://")) {
				linkValue = "http://".concat(linkValue.trim());
				lTargetInputList.get(i).setValue(linkValue);
			}
			link.setTarget(linkValue);
			boolean blank = lHtmlTargetInputList.get(i).isSelected(0);
			if(linkValue.startsWith(Settings.getServerContextPathURI())) {
				// links to OO pages open in same window
				blank = false;
				lHtmlTargetInputList.get(i).select(SELF_KEY, true);
			}
			link.setHtmlTarget(blank ? BLANK_KEY : SELF_KEY);
			link.setDescription(lDescriptionInputList.get(i).getValue());
			link.setComment(lCommentInputList.get(i).getValue());
		}
		moduleConfig.set(LLCourseNode.CONF_LINKLIST, linkList);
		// Inform all listeners about the changes
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof TextElement) {
			int i = lTargetInputList.indexOf(source);
			if (i >= 0) {
				String linkValue = ((TextElement)source).getValue();
				if(!linkValue.contains("://")) {
					linkValue = "http://".concat(linkValue.trim());
				}
				boolean selected = lHtmlTargetInputList.get(i).isSelected(0);
				if(selected && linkValue.startsWith(Settings.getServerContextPathURI())) {
					lHtmlTargetInputList.get(i).select(BLANK_KEY, false);
				}
			}
		}
		else if (source.getComponent() instanceof Link) {
			if (lAddButtonList.contains(source)) {
				// add a new form link
				final LLModel link = (LLModel) ((FormLink) source).getUserObject();
				final LLModel newLink = new LLModel();
				linkList.add(linkList.indexOf(link) + 1, newLink);
				addNewFormLink(linkList.indexOf(link) + 1, newLink);
			} else if (lDelButtonList.contains(source)) {
				// special case: only one line existent
				if (linkList.size() == 1) {
					// clear this line
					lTargetInputList.get(0).setValue("");
					lTargetInputList.get(0).setEnabled(true);
					lDescriptionInputList.get(0).setValue("");
					lCommentInputList.get(0).setValue("");
				} else {
					final LLModel link = (LLModel) ((FormLink) source).getUserObject();
					removeFormLink(link);
				}
			} else if (lCustomMediaButtonList.contains(source)) {
				currentLink = (LLModel) ((FormLink) source).getUserObject();
				doChooseMedia(ureq);
			}
		}
		super.formInnerEvent(ureq, source, event);
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

	private void doChooseMedia(UserRequest ureq) {
		removeAsListenerAndDispose(mediaDialogBox);
		removeAsListenerAndDispose(mediaChooserController);
		
		VFSContainer courseContainer = courseEnv.getCourseFolderContainer();
		mediaChooserController = new LinkChooserController(ureq, getWindowControl(), courseContainer, null, null, null, false, "", null, null, true);
		listenTo(mediaChooserController);
		
		mediaDialogBox = new CloseableModalController(getWindowControl(), translate("close"),
				mediaChooserController.getInitialComponent(), mediaChooserController.getTitle());
		mediaDialogBox.activate();
		listenTo(mediaDialogBox);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == mediaDialogBox) {
			removeAsListenerAndDispose(mediaDialogBox);
			removeAsListenerAndDispose(mediaChooserController);
			mediaDialogBox = null;
			mediaChooserController = null;
		} else if (source == mediaChooserController) {
			if(event instanceof URLChoosenEvent) {
				URLChoosenEvent choosenEvent = (URLChoosenEvent)event;
				String url = choosenEvent.getURL();
				if(url.startsWith(Settings.getServerContextPathURI())) {
					//doesn't allow absolute path -> the mapper check if the link is in the list!
					url = url.substring(Settings.getServerContextPathURI().length());
				}
				currentLink.setTarget(url);
				currentLink.setIntern(true);
				currentLink.setHtmlTarget(SELF_KEY);
				if(StringHelper.containsNonWhitespace(choosenEvent.getDisplayName())) {
					currentLink.setDescription(choosenEvent.getDisplayName());
				}

				int index = 0;
				for(TextElement targetEl:lTargetInputList) {
					if(currentLink.equals(targetEl.getUserObject())) {
						targetEl.setValue(url);
						targetEl.setEnabled(false);
						lDescriptionInputList.get(index).setValue(currentLink.getDescription());
						lHtmlTargetInputList.get(index).select(SELF_KEY, true);
						break;
					}
					index++;
				}
			}
			mediaDialogBox.deactivate();
			removeAsListenerAndDispose(mediaDialogBox);
			removeAsListenerAndDispose(mediaChooserController);
			mediaDialogBox = null;
			mediaChooserController = null;
		}
		
		super.event(ureq, source, event);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// create gui elements for all links
		for (int i = 0; i < linkList.size(); i++) {
			LLModel link = linkList.get(i);
			addNewFormLink(i, link);
		}

		flc.contextPut("linkList", linkList);
		flc.contextPut("lTargetInputList", lTargetInputList);
		flc.contextPut("lHtmlTargetInputList", lHtmlTargetInputList);
		flc.contextPut("lDescriptionInputList", lDescriptionInputList);
		flc.contextPut("lCommentInputList", lCommentInputList);
		flc.contextPut("lAddButtonList", lAddButtonList);
		flc.contextPut("lDelButtonList", lDelButtonList);
		flc.contextPut("lCustomMediaButtonList", lCustomMediaButtonList);
		
		uifactory.addFormSubmitButton("submit", formLayout);
	}

	/**
	 * Get the module configuration.
	 *
	 * @return ModuleConfiguration
	 */
	public ModuleConfiguration getModuleConfiguration() {
		moduleConfig.setConfigurationVersion(2);
		return moduleConfig;
	}

	/**
	 * Add a new form link line to the list of link elements.
	 *
	 * @param link the link model object
	 */
	private void addNewFormLink(int index, final LLModel link) {
		// add link target
		TextElement lTarget = uifactory.addTextElement("target" + counter, null, -1, link.getTarget(), flc);
		lTarget.setPlaceholderKey("target.example", null);
		lTarget.clearError();
		lTarget.setEnabled(!link.isIntern());
		lTarget.setDisplaySize(40);
		lTarget.setMandatory(true);
		lTarget.setNotEmptyCheck("ll.table.target.error");
		lTarget.setItemValidatorProvider(new ItemValidatorProvider() {
			public boolean isValidValue(String value, ValidationError validationError, Locale locale) {
				try {
					if (!value.contains("://")) {
						value = "http://".concat(value);
					}
					new URL(value);
				} catch (MalformedURLException e) {
					validationError.setErrorKey("ll.table.target.error.format");
					return false;
				}
				return true;
			}
		});
		lTarget.addActionListener(FormEvent.ONCHANGE);
		lTarget.setUserObject(link);
		lTargetInputList.add(index, lTarget);
		//add html target
		SingleSelection htmlTargetSelection = uifactory.addDropdownSingleselect("html_target" + counter, null, flc,
				new String[]{BLANK_KEY, SELF_KEY}, new String[]{translate("ll.table.html_target"), translate("ll.table.html_target.self")}, null);
		htmlTargetSelection.setUserObject(link);
		htmlTargetSelection.select((SELF_KEY.equals(link.getHtmlTarget()) ? SELF_KEY : BLANK_KEY), true);
		lHtmlTargetInputList.add(index, htmlTargetSelection);
		
		// add link description
		TextElement lDescription = uifactory.addTextElement("description" + counter, null, -1, link.getDescription(), flc);
		lDescription.clearError();
		lDescription.setDisplaySize(20);
		lDescription.setNotEmptyCheck("ll.table.description.error");
		lDescription.setMandatory(true);
		lDescription.setPlaceholderKey("ll.table.description", null);
		lDescription.setUserObject(link);
		lDescriptionInputList.add(index, lDescription);
		
		// add link comment
		TextElement lComment =uifactory.addTextAreaElement("comment" + counter, null, -1, 2, 50, true, false, link.getComment(), flc);
		lComment.setPlaceholderKey("ll.table.comment", null);
		lComment.setDisplaySize(20);
		lComment.setUserObject(link);
		lCommentInputList.add(index, lComment);
		
		// add link add action button
		FormLink addButton = new FormLinkImpl("add" + counter, "add" + counter, "", Link.BUTTON_SMALL + Link.NONTRANSLATED);
		addButton.setUserObject(link);
		addButton.setDomReplacementWrapperRequired(false);
		addButton.setIconLeftCSS("o_icon o_icon-lg o_icon-fw o_icon_add");
		flc.add(addButton);
		lAddButtonList.add(index, addButton);
		// add link deletion action button
		FormLink delButton = new FormLinkImpl("delete" + counter, "delete" + counter, "", Link.BUTTON_SMALL + Link.NONTRANSLATED);
		delButton.setUserObject(link);
		delButton.setDomReplacementWrapperRequired(false);
		delButton.setIconLeftCSS("o_icon o_icon-lg o_icon-fw o_icon_delete_item");
		flc.add(delButton);
		lDelButtonList.add(index, delButton);
		// custom media action button
		FormLink mediaButton = new FormLinkImpl("media" + counter, "media" + counter, "  ", Link.NONTRANSLATED);
		mediaButton.setIconLeftCSS("o_icon o_icon_browse o_icon-lg");
		mediaButton.setDomReplacementWrapperRequired(false);
		mediaButton.setUserObject(link);
		flc.add(mediaButton);
		lCustomMediaButtonList.add(index, mediaButton);
		
		// increase the counter to enable unique component names
		counter++;
	}

	private void removeFormLink(final LLModel link) {
		// remove link from model list
		linkList.remove(link);
		// remove link gui elements
		int i;
		for (i = 0; i < lTargetInputList.size(); i++) {
			if (lTargetInputList.get(i).getUserObject().equals(link)) {
				break;
			}
		}
		flc.remove(lTargetInputList.remove(i));
		for (i = 0; i < lHtmlTargetInputList.size(); i++) {
			if (lHtmlTargetInputList.get(i).getUserObject().equals(link)) {
				break;
			}
		}
		flc.remove(lHtmlTargetInputList.remove(i));
		for (i = 0; i < lDescriptionInputList.size(); i++) {
			if (lDescriptionInputList.get(i).getUserObject().equals(link)) {
				break;
			}
		}
		flc.remove(lDescriptionInputList.remove(i));
		for (i = 0; i < lCommentInputList.size(); i++) {
			if (lCommentInputList.get(i).getUserObject().equals(link)) {
				break;
			}
		}
		flc.remove(lCommentInputList.remove(i));
		for (i = 0; i < lAddButtonList.size(); i++) {
			if (lAddButtonList.get(i).getUserObject().equals(link)) {
				break;
			}
		}
		flc.remove(lAddButtonList.remove(i));
		for (i = 0; i < lDelButtonList.size(); i++) {
			if (lDelButtonList.get(i).getUserObject().equals(link)) {
				break;
			}
		}
		flc.remove(lDelButtonList.remove(i));
		for (i = 0; i < lCustomMediaButtonList.size(); i++) {
			if (lCustomMediaButtonList.get(i).getUserObject().equals(link)) {
				break;
			}
		}
		flc.remove(lCustomMediaButtonList.remove(i));
	}
}
