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
package org.olat.course.nodes.video;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.core.logging.AssertException;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSContainerMapper;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.VideoCourseNode;
import org.olat.fileresource.types.VideoFileResource;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.ui.VideoDisplayController;
import org.olat.modules.video.ui.VideoDisplayOptions;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Editcontroller of videonode
 * 
 * @author dfakae, dirk.furrer@frentix.com, http://www.frentix.com
 *
 */
public class VideoEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {

	public static final String PANE_TAB_VIDEOCONFIG = "pane.tab.videoconfig";
	private static final String[] paneKeys = { PANE_TAB_VIDEOCONFIG};

	public static final String NLS_ERROR_VIDEOREPOENTRYMISSING = "error.videorepoentrymissing";
	private static final String NLS_COMMAND_CHOOSEVIDEO = "command.choosevideo";
	private static final String NLS_COMMAND_CREATEVID = "command.createvideo";
	private static final String NLS_COMMAND_CHANGEVID = "command.changevideo";

	public static final String CONFIG_KEY_REPOSITORY_SOFTKEY = "reporef";
	public static final String CONFIG_KEY_AUTOPLAY = "autoplay";
	public static final String CONFIG_KEY_COMMENTS = "comments";
	public static final String CONFIG_KEY_RATING = "rating";
	public static final String CONFIG_KEY_FORWARD_SEEKING_RESTRICTED = "forwardSeekingRestricted";
	public static final String CONFIG_KEY_TITLE = "title";
	public static final String CONFIG_KEY_DESCRIPTION_SELECT = "descriptionSelect";
	public static final String CONFIG_KEY_DESCRIPTION_SELECT_NONE = "none";
	public static final String CONFIG_KEY_DESCRIPTION_SELECT_RESOURCE = "resourceDescription";
	public static final String CONFIG_KEY_DESCRIPTION_SELECT_CUSTOM = "customDescription";
	
	
	public static final String CONFIG_KEY_DESCRIPTION_CUSTOMTEXT = "descriptionText";

	private static final String VC_CHOSENVIDEO = "chosenvideo";
	private static final String NLS_NO_VIDEO_CHOSEN = "no.video.chosen";

	private Link previewLink;
	private Link chooseVideoButton;
	private Link changeVideoButton;
	
	private Panel main;
	private TabbedPane myTabbedPane;
	private VelocityContainer videoConfigurationVc;

	private final VideoCourseNode videoNode;
	private final ModuleConfiguration config;
	private RepositoryEntry repositoryEntry;

	private CloseableModalController cmc;
	private VideoDisplayController previewCtrl;
	private VideoOptionsForm videoOptionsCtrl;
	private ReferencableEntriesSearchController searchController;

	public VideoEditController(VideoCourseNode videoNode, UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		this.videoNode = videoNode;
		this.config = videoNode.getModuleConfiguration();
		
		main = new Panel("videomain");

		videoConfigurationVc = createVelocityContainer("edit");
		chooseVideoButton = LinkFactory.createButtonSmall(NLS_COMMAND_CREATEVID, videoConfigurationVc, this);
		chooseVideoButton.setElementCssClass("o_sel_cp_choose_repofile");
		changeVideoButton = LinkFactory.createButtonSmall(NLS_COMMAND_CHANGEVID, videoConfigurationVc, this);
		changeVideoButton.setElementCssClass("o_sel_cp_change_repofile");

		if (config.get(CONFIG_KEY_REPOSITORY_SOFTKEY) != null) {
			// fetch repository entry to display the repository entry title of
			// the chosen cp
			repositoryEntry = getVideoReference(config, false);
			if (repositoryEntry == null) { 
				// we cannot display the entries name, because the repository
				// entry had been deleted between the time when it was chosen
				// here, and now
				showError(NLS_ERROR_VIDEOREPOENTRYMISSING);
				videoConfigurationVc.contextPut("showPreviewButton", Boolean.FALSE);
				videoConfigurationVc.contextPut(VC_CHOSENVIDEO, translate("no.video.chosen"));
			} else {
				videoConfigurationVc.contextPut("showPreviewButton", Boolean.TRUE);
				String displayname = StringHelper.escapeHtml(repositoryEntry.getDisplayname());
				previewLink = LinkFactory.createCustomLink("command.preview", "command.preview", displayname, Link.NONTRANSLATED, videoConfigurationVc, this);
				previewLink.setTitle(getTranslator().translate("command.preview"));
				previewLink.setIconLeftCSS("o_icon o_icon-fw o_icon_preview");
				previewLink.setEnabled(true);
			}
			videoConfigurationVc.contextPut("showOptions", Boolean.TRUE);
			VideoOptionsForm videoOptions = new VideoOptionsForm(ureq, getWindowControl(), repositoryEntry, config);
			videoConfigurationVc.put("videoOptions", videoOptions.getInitialComponent());
			listenTo(videoOptions);
		} else {
			// no valid config yet
			videoConfigurationVc.contextPut("showPreviewButton", Boolean.FALSE);
			videoConfigurationVc.contextPut("showOptions", Boolean.FALSE);
			videoConfigurationVc.contextPut(VC_CHOSENVIDEO, translate(NLS_NO_VIDEO_CHOSEN));
		}

		main.setContent(videoConfigurationVc);
	}

	@Override
	public void addTabs(TabbedPane tabbedPane) {
		myTabbedPane = tabbedPane;
		tabbedPane.addTab(translate(PANE_TAB_VIDEOCONFIG), main);
	}

	@Override
	public String[] getPaneKeys() {
		return paneKeys;
	}

	@Override
	public TabbedPane getTabbedPane() {
		return myTabbedPane;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == chooseVideoButton || source == changeVideoButton) {
			doSearch(ureq);
		} else if(source == previewLink) {
			doPreview(ureq);
		}
	}

	@Override
	public void event(UserRequest urequest, Controller source, Event event) {
		if (source == searchController) {
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				// -> close closeable modal controller
				doSelectResource(urequest, searchController.getSelectedEntry());
				cmc.deactivate();
				cleanUp();
			}
		} else if(cmc == source) {
			cleanUp();
		}

		if (event == NodeEditController.NODECONFIG_CHANGED_EVENT){
			fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
		}
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(searchController);
		removeAsListenerAndDispose(previewCtrl);
		removeAsListenerAndDispose(cmc);
		searchController = null;
		previewCtrl = null;
		cmc = null;
	}
	
	private void doSearch(UserRequest ureq) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(searchController);
		
		searchController = new ReferencableEntriesSearchController(getWindowControl(), ureq,
				new String[] {VideoFileResource.TYPE_NAME}, translate(NLS_COMMAND_CHOOSEVIDEO),
				true, false, false, false, false, false);
		listenTo(searchController);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), searchController.getInitialComponent(), true, translate(NLS_COMMAND_CHOOSEVIDEO));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doSelectResource(UserRequest ureq, RepositoryEntry entry) {
		repositoryEntry = entry;
		if (repositoryEntry != null) {
			setVideoReference(repositoryEntry, config);
			videoConfigurationVc.contextPut("showPreviewButton", Boolean.TRUE);
			String displayname = StringHelper.escapeHtml(repositoryEntry.getDisplayname());
			previewLink = LinkFactory.createCustomLink("command.preview", "command.preview", displayname, Link.NONTRANSLATED, videoConfigurationVc, this);
			previewLink.setIconLeftCSS("o_icon o_icon-fw o_icon_preview");
			previewLink.setTitle(getTranslator().translate("command.preview"));
			// fire event so the updated config is saved by the editormaincontroller
			fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);

			videoConfigurationVc.contextPut("showOptions", Boolean.TRUE);
			
			removeAsListenerAndDispose(videoOptionsCtrl);
			videoOptionsCtrl = new VideoOptionsForm(ureq, getWindowControl(), repositoryEntry, config);
			videoConfigurationVc.put("videoOptions", videoOptionsCtrl.getInitialComponent());
			listenTo(videoOptionsCtrl);
		}
	}
	
	private void doPreview(UserRequest ureq) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(previewCtrl);
		
		VideoDisplayOptions options = videoNode.getVideoDisplay(repositoryEntry, true);
		previewCtrl = new VideoDisplayController(ureq, getWindowControl(), repositoryEntry, null, null, options);
		listenTo(previewCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), previewCtrl.getInitialComponent(), true, translate(NLS_COMMAND_CHOOSEVIDEO));
		listenTo(cmc);
		cmc.activate();
	}

	public static RepositoryEntry getVideoReference(ModuleConfiguration config, boolean strict) {
		if (config == null) {
			if (strict) throw new AssertException("missing config in Video");
			else return null;
		}
		String repoSoftkey = (String) config.get(VideoEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
		if (repoSoftkey == null) {
			if (strict) throw new AssertException("invalid config when being asked for references");
			else return null;
		}
		RepositoryManager rm = RepositoryManager.getInstance();
		return rm.lookupRepositoryEntryBySoftkey(repoSoftkey, strict);
	}

	/**
	 * @param moduleConfiguration
	 * @return boolean
	 */
	public static boolean isModuleConfigValid(ModuleConfiguration moduleConfiguration) {
		boolean isValid = moduleConfiguration.get(CONFIG_KEY_REPOSITORY_SOFTKEY) != null;
		if (isValid) {
			Object repoEntry = getVideoReference(moduleConfiguration, false);
			if (repoEntry == null) {
				isValid = false;
				removeVideoReference(moduleConfiguration);
			}
		}
		
		return isValid;
	}

	/**
	 * Set the referenced repository entry.
	 *
	 * @param re
	 * @param moduleConfiguration
	 */
	public static void setVideoReference(RepositoryEntry re, ModuleConfiguration moduleConfiguration) {
		moduleConfiguration.set(CONFIG_KEY_REPOSITORY_SOFTKEY, re.getSoftkey());
	}

	public static void removeVideoReference(ModuleConfiguration moduleConfiguration){
		moduleConfiguration.remove(CONFIG_KEY_REPOSITORY_SOFTKEY);
	}
}

class VideoOptionsForm extends FormBasicController{

	/**
	 * Simple form for the Videooptions
	 *
	 * @author Dirk Furrer
	 */
	@Autowired	
	protected VideoManager videoManager;

	private SelectionElement videoComments;
	private SelectionElement videoRating;
	private SelectionElement videoAutoplay;
	private SelectionElement videoForwardSeekingRestricted;
	private SelectionElement title;
	private SingleSelection description;
	private RichTextElement descriptionField;
	private StaticTextElement descriptionRepoField;
	private boolean titleEnabled;
	private boolean commentsEnabled;
	private boolean ratingEnabled;
	private boolean autoplay;
	private boolean forwardSeekingRestricted;
	
	private String mediaRepoBaseUrl;
	private final RepositoryEntry repoEntry;
	private final ModuleConfiguration config;

	VideoOptionsForm(UserRequest ureq, WindowControl wControl, RepositoryEntry repoEntry, ModuleConfiguration moduleConfiguration) {
		super(ureq, wControl);
		this.config = moduleConfiguration;
		this.repoEntry = repoEntry;
		
		commentsEnabled = config.getBooleanSafe(VideoEditController.CONFIG_KEY_COMMENTS);
		ratingEnabled = config.getBooleanSafe(VideoEditController.CONFIG_KEY_RATING);
		autoplay = config.getBooleanSafe(VideoEditController.CONFIG_KEY_AUTOPLAY);
		forwardSeekingRestricted = config.getBooleanSafe(VideoEditController.CONFIG_KEY_FORWARD_SEEKING_RESTRICTED);
		titleEnabled = config.getBooleanSafe(VideoEditController.CONFIG_KEY_TITLE);
		
		RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(repoEntry);
		VFSContainer mediaContainer = handler.getMediaContainer(repoEntry);
		if(mediaContainer != null) {
			mediaRepoBaseUrl = registerMapper(ureq, new VFSContainerMapper(mediaContainer.getParentContainer()));
		}
		
		initForm(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		config.setBooleanEntry(VideoEditController.CONFIG_KEY_COMMENTS, videoComments.isSelected(0));
		config.setBooleanEntry(VideoEditController.CONFIG_KEY_RATING, videoRating.isSelected(0));
		config.setBooleanEntry(VideoEditController.CONFIG_KEY_AUTOPLAY, videoAutoplay.isSelected(0));
		config.setBooleanEntry(VideoEditController.CONFIG_KEY_FORWARD_SEEKING_RESTRICTED, videoForwardSeekingRestricted.isSelected(0));
		config.setBooleanEntry(VideoEditController.CONFIG_KEY_TITLE, title.isSelected(0));
		config.setStringValue(VideoEditController.CONFIG_KEY_DESCRIPTION_SELECT, description.getSelectedKey());
		if("customDescription".equals(description.getSelectedKey())) {
			config.setStringValue(VideoEditController.CONFIG_KEY_DESCRIPTION_CUSTOMTEXT, descriptionField.getValue());
		}
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//add checkboxes for displayoptions
		videoComments = uifactory.addCheckboxesHorizontal("videoComments", "video.config.comments", formLayout, new String[]{"xx"}, new String[]{null});
		videoComments.select("xx",commentsEnabled);
		videoRating = uifactory.addCheckboxesHorizontal("videoRating", "video.config.rating", formLayout, new String[]{"xx"}, new String[]{null});
		videoRating.select("xx",ratingEnabled);
		
		uifactory.addSpacerElement("spacer1", formLayout, false);		
		videoAutoplay = uifactory.addCheckboxesHorizontal("videoAutoplay", "video.config.autoplay", formLayout, new String[]{"xx"}, new String[]{null});
		videoAutoplay.select("xx",autoplay);
		videoForwardSeekingRestricted = uifactory.addCheckboxesHorizontal("videoForwardSeekingAllowed", "video.config.forwardSeekingRestricted", formLayout, new String[]{"xx"}, new String[]{null});
		videoForwardSeekingRestricted.select("xx",forwardSeekingRestricted);

		String[] descriptionkeys = new String[]{ VideoEditController.CONFIG_KEY_DESCRIPTION_SELECT_NONE, VideoEditController.CONFIG_KEY_DESCRIPTION_SELECT_RESOURCE, VideoEditController.CONFIG_KEY_DESCRIPTION_SELECT_CUSTOM};
		String[] descriptionValues = new String[]{ translate("description.none"), translate("description.resource"), translate("description.custom") };

		uifactory.addSpacerElement("spacer2", formLayout, false);
		title = uifactory.addCheckboxesHorizontal("title", "video.config.title", formLayout, new String[]{"xx"}, new String[]{null});
		title.select("xx",titleEnabled);
		//add textfield for custom description
		description = uifactory.addDropdownSingleselect("video.config.description", formLayout, descriptionkeys, descriptionValues, null);
		description.addActionListener(FormEvent.ONCHANGE);
		description.select(config.getStringValue(VideoEditController.CONFIG_KEY_DESCRIPTION_SELECT, VideoEditController.CONFIG_KEY_DESCRIPTION_SELECT_NONE), true);
		String desc = repoEntry.getDescription();
		descriptionField = uifactory.addRichTextElementForStringDataMinimalistic("description", "", desc, -1, -1, formLayout, getWindowControl());
		descriptionRepoField = uifactory.addStaticTextElement("description.repo", "", "", formLayout);

		updateDescriptionField();
		uifactory.addFormSubmitButton("submit", formLayout);
		//init options-config
		config.setBooleanEntry(VideoEditController.CONFIG_KEY_COMMENTS, videoComments.isSelected(0));
		config.setBooleanEntry(VideoEditController.CONFIG_KEY_RATING, videoRating.isSelected(0));
		config.setBooleanEntry(VideoEditController.CONFIG_KEY_AUTOPLAY, videoAutoplay.isSelected(0));
		config.setStringValue(VideoEditController.CONFIG_KEY_DESCRIPTION_SELECT, description.getSelectedKey());
		if(VideoEditController.CONFIG_KEY_DESCRIPTION_SELECT_CUSTOM.equals(description.getSelectedKey())) {
			config.setStringValue(VideoEditController.CONFIG_KEY_DESCRIPTION_CUSTOMTEXT, descriptionField.getValue());
		}
	}

	/**
	 * Update visibility of the textfield for entering custom description
	 */
	private void updateDescriptionField() {
		String selectDescOption = description.getSelectedKey();
		if("none".equals(selectDescOption)) {
			descriptionField.setVisible(false);
			descriptionRepoField.setVisible(false);
		} else if(VideoEditController.CONFIG_KEY_DESCRIPTION_SELECT_RESOURCE.equals(selectDescOption)) {
			descriptionField.setVisible(false);
			descriptionField.setEnabled(false);
			
			String text = repoEntry.getDescription();
			if(StringHelper.containsNonWhitespace(text)) {
				text = StringHelper.xssScan(text);
				if(mediaRepoBaseUrl != null) {
					text = FilterFactory.getBaseURLToMediaRelativeURLFilter(mediaRepoBaseUrl).filter(text);
				}
				text = Formatter.formatLatexFormulas(text);
			}
			descriptionRepoField.setValue(text);
			descriptionRepoField.setVisible(true);
		} else if(VideoEditController.CONFIG_KEY_DESCRIPTION_SELECT_CUSTOM.equals(selectDescOption)) {
			descriptionField.setVisible(true);
			descriptionField.setEnabled(true);
			descriptionField.setValue(config.getStringValue(VideoEditController.CONFIG_KEY_DESCRIPTION_CUSTOMTEXT, ""));
			descriptionRepoField.setVisible(false);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == description){
			updateDescriptionField();
		}
	}
	
	@Override
	protected void doDispose() {
		//
	}
}
