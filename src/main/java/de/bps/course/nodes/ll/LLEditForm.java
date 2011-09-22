/**
 * 
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * 
 * Copyright (c) 2005-2009 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 *
 * All rights reserved.
 */
package de.bps.course.nodes.ll;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.ValidationError;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.form.flexible.impl.elements.ItemValidatorProvider;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.helpers.Settings;
import org.olat.course.editor.NodeEditController;
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

	private ModuleConfiguration moduleConfig;
	private FormSubmit subm;
	private List<TextElement> lTargetInputList;
	private List<MultipleSelectionElement> lHtmlTargetInputList;
	private List<TextElement> lDescriptionInputList;
	private List<TextElement> lCommentInputList;
	private List<FormLink> lDelButtonList;
	private List<LLModel> linkList;
	private List<FormLink> lAddButtonList;
	private FormLayoutContainer titleContainer;
	private long counter = 0;

	public LLEditForm(UserRequest ureq, WindowControl wControl, ModuleConfiguration moduleConfig) {
		super(ureq, wControl);
		this.moduleConfig = moduleConfig;
		// read existing links from config
		linkList = new ArrayList<LLModel>((List<LLModel>) moduleConfig.get(LLCourseNode.CONF_LINKLIST));
		// list of all link target text fields
		this.lTargetInputList = new ArrayList<TextElement>(linkList.size());
		// list of all link html target text fields
		this.lHtmlTargetInputList = new ArrayList<MultipleSelectionElement>(linkList.size());
		// list of all link description text fields
		this.lDescriptionInputList = new ArrayList<TextElement>(linkList.size());
		// list of all link comment text fields
		this.lCommentInputList = new ArrayList<TextElement>(linkList.size());
	// list of all link add action buttons
		this.lAddButtonList = new ArrayList<FormLink>(linkList.size());
		// list of all link deletion action buttons
		this.lDelButtonList = new ArrayList<FormLink>(linkList.size());
		initForm(this.flc, this, ureq);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doDispose() {
	// nothing to dispose
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void formOK(UserRequest ureq) {
		// read data from form elements
		for (int i = 0; i < lTargetInputList.size(); i++) {
			LLModel link = (LLModel) lTargetInputList.get(i).getUserObject();
			String linkValue = lTargetInputList.get(i).getValue();
			if(!linkValue.contains("://")) {
				linkValue = "http://".concat(linkValue.trim());
				lTargetInputList.get(i).setValue(linkValue);
			}
			link.setTarget(linkValue);
			boolean selected = lHtmlTargetInputList.get(i).isSelected(0);
			if(linkValue.startsWith(Settings.getServerContextPathURI())) {
				selected = false;
				lHtmlTargetInputList.get(i).select(BLANK_KEY, selected);
			}
			link.setHtmlTarget(selected ? "_blank" : "_self");
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
					lDescriptionInputList.get(0).setValue("");
					lCommentInputList.get(0).setValue("");
				} else {
					final LLModel link = (LLModel) ((FormLink) source).getUserObject();
					removeFormLink(link);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unused")
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		titleContainer = FormLayoutContainer.createCustomFormLayout("titleLayout", getTranslator(), velocity_root + "/editForm.html");
		formLayout.add(titleContainer);
		// create gui elements for all links
		for (int i = 0; i < linkList.size(); i++) {
			LLModel link = linkList.get(i);
			addNewFormLink(i, link);
		}

		titleContainer.contextPut("linkList", linkList);
		titleContainer.contextPut("lTargetInputList", lTargetInputList);
		titleContainer.contextPut("lHtmlTargetInputList", lHtmlTargetInputList);
		titleContainer.contextPut("lDescriptionInputList", lDescriptionInputList);
		titleContainer.contextPut("lCommentInputList", lCommentInputList);
		titleContainer.contextPut("lAddButtonList", lAddButtonList);
		titleContainer.contextPut("lDelButtonList", lDelButtonList);

		subm = new FormSubmit("subm", "submit");

		formLayout.add(subm);

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
		TextElement lTarget = uifactory.addTextElement("target" + counter, null, -1, link.getTarget(), titleContainer);
		lTarget.clearError();
		lTarget.setDisplaySize(40);
		lTarget.setMandatory(true);
		lTarget.setNotEmptyCheck("ll.table.target.error");
		lTarget.setItemValidatorProvider(new ItemValidatorProvider() {
			public boolean isValidValue(String value, ValidationError validationError, @SuppressWarnings("unused") Locale locale) {
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
		lTarget.addActionListener(this, FormEvent.ONCHANGE);
		lTarget.setUserObject(link);
		lTargetInputList.add(index, lTarget);
		//add html target
		MultipleSelectionElement htmlTargetSelection = uifactory.addCheckboxesHorizontal("html_target" + counter, titleContainer, new String[]{BLANK_KEY}, new String[]{""}, null);
		htmlTargetSelection.setUserObject(link);
		htmlTargetSelection.select(BLANK_KEY, "_blank".equals(link.getHtmlTarget()));
		lHtmlTargetInputList.add(index, htmlTargetSelection);
		
		// add link description
		TextElement lDescription = uifactory.addTextElement("description" + counter, null, -1, link.getDescription(), titleContainer);
		lDescription.clearError();
		lDescription.setDisplaySize(20);
		lDescription.setNotEmptyCheck("ll.table.description.error");
		lDescription.setMandatory(true);
		lDescription.setUserObject(link);
		lDescriptionInputList.add(index, lDescription);
		
		// add link comment
		TextElement lComment =uifactory.addTextElement("comment" + counter, null, -1, link.getComment(), titleContainer);
		lComment.setDisplaySize(20);
		lComment.setUserObject(link);
		lCommentInputList.add(index, lComment);
		
		// add link add action button
		FormLink addButton = new FormLinkImpl("add" + counter, "add" + counter, "ll.table.add", Link.BUTTON_SMALL);
		addButton.setUserObject(link);
		titleContainer.add(addButton);
		lAddButtonList.add(index, addButton);
		// add link deletion action button
		FormLink delButton = new FormLinkImpl("delete" + counter, "delete" + counter, "ll.table.delete", Link.BUTTON_SMALL);
		delButton.setUserObject(link);
		titleContainer.add(delButton);
		lDelButtonList.add(index, delButton);
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
		titleContainer.remove(lTargetInputList.remove(i));
		for (i = 0; i < lHtmlTargetInputList.size(); i++) {
			if (lHtmlTargetInputList.get(i).getUserObject().equals(link)) {
				break;
			}
		}
		titleContainer.remove(lHtmlTargetInputList.remove(i));
		for (i = 0; i < lDescriptionInputList.size(); i++) {
			if (lDescriptionInputList.get(i).getUserObject().equals(link)) {
				break;
			}
		}
		titleContainer.remove(lDescriptionInputList.remove(i));
		for (i = 0; i < lCommentInputList.size(); i++) {
			if (lCommentInputList.get(i).getUserObject().equals(link)) {
				break;
			}
		}
		titleContainer.remove(lCommentInputList.remove(i));
		for (i = 0; i < lAddButtonList.size(); i++) {
			if (lAddButtonList.get(i).getUserObject().equals(link)) {
				break;
			}
		}
		titleContainer.remove(lAddButtonList.remove(i));
		for (i = 0; i < lDelButtonList.size(); i++) {
			if (lDelButtonList.get(i).getUserObject().equals(link)) {
				break;
			}
		}
		titleContainer.remove(lDelButtonList.remove(i));
	}
}
