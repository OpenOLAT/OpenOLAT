package org.olat.course.nodes.video;

import java.util.HashMap;
import java.util.Map;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.core.logging.AssertException;
import org.olat.core.util.StringHelper;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.condition.Condition;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.VideoCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.fileresource.types.VideoFileResource;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.video.manager.VideoManager;
import org.olat.modules.video.ui.VideoDisplayController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.olat.resource.OLATResource;

public class VideoEditController  extends ActivateableTabbableDefaultController implements ControllerEventListener {

	public static final String PANE_TAB_VIDEOCONFIG = "pane.tab.videoconfig";
	private static final String PANE_TAB_ACCESSIBILITY = "pane.tab.accessibility";
	final static String[] paneKeys = { PANE_TAB_VIDEOCONFIG, PANE_TAB_ACCESSIBILITY };

	// NLS support:
	public static final String NLS_ERROR_VIDEOREPOENTRYMISSING = "error.videorepoentrymissing";
	private static final String NLS_CONDITION_ACCESSIBILITY_TITLE = "condition.accessibility.title";
	private static final String NLS_COMMAND_CHOOSEVIDEO = "command.choosevideo";
	private static final String NLS_COMMAND_CREATEVID = "command.createvideo";
	private static final String NLS_COMMAND_CHANGEVID = "command.changevideo";

	public static final String CONFIG_KEY_REPOSITORY_SOFTKEY = "reporef";
	public static final String CONFIG_KEY_AUTOPLAY = "autoplay";
	public static final String CONFIG_KEY_COMMENTS = "comments";
	public static final String CONFIG_KEY_RATING = "rating";
	public static final String CONFIG_KEY_DESCRIPTION_SELECT = "descriptionSelect";
	public static final String CONFIG_KEY_DESCRIPTION_CUSTOMTEXT = "descriptionText";

	private static final String VC_CHOSENVIDEO = "chosenvideo";
	private static final String NLS_NO_VIDEO_CHOSEN = "no.video.chosen";

	protected FormUIFactory uifactory = FormUIFactory.getInstance();

	private Panel main;
	private VelocityContainer videoConfigurationVc;

	private ModuleConfiguration config;
	private RepositoryEntry re;

	private ReferencableEntriesSearchController searchController;

	private Link previewLink;
	private Link chooseVideoButton;
	private Link changeVideoButton;

	private ConditionEditController accessibilityCondContr;
	private Controller previewCtr;
	private TabbedPane myTabbedPane;
	private CloseableModalController cmc;

	public VideoEditController(VideoCourseNode videoNode, UserRequest ureq, WindowControl wControl,  BreadcrumbPanel stackPanel,ICourse course, UserCourseEnvironment euce) {
		super(ureq, wControl);
		this.config = videoNode.getModuleConfiguration();
		main = new Panel("videomain");

		videoConfigurationVc = createVelocityContainer("edit");
		chooseVideoButton = LinkFactory.createButtonSmall(NLS_COMMAND_CREATEVID, videoConfigurationVc, this);
		chooseVideoButton.setElementCssClass("o_sel_cp_choose_repofile");
		changeVideoButton = LinkFactory.createButtonSmall(NLS_COMMAND_CHANGEVID, videoConfigurationVc, this);
		changeVideoButton.setElementCssClass("o_sel_cp_change_repofile");

		if (config.get(CONFIG_KEY_REPOSITORY_SOFTKEY) != null) {
			// fetch repository entry to display the repository entry title of the chosen cp
						re = getVideoReference(config, false);
						if (re == null) { // we cannot display the entries name, because the
							// repository entry had been deleted between the time when it was chosen here, and now
							showError(NLS_ERROR_VIDEOREPOENTRYMISSING);
							videoConfigurationVc.contextPut("showPreviewButton", Boolean.FALSE);
							videoConfigurationVc.contextPut(VC_CHOSENVIDEO, translate("no.video.chosen"));
						} else {
							videoConfigurationVc.contextPut("showPreviewButton", Boolean.TRUE);
							String displayname = StringHelper.escapeHtml(re.getDisplayname());
							previewLink = LinkFactory.createCustomLink("command.preview", "command.preview", displayname, Link.NONTRANSLATED, videoConfigurationVc, this);
							previewLink.setTitle(getTranslator().translate("command.preview"));
							previewLink.setEnabled(true);
						}
						videoConfigurationVc.contextPut("showOptions", Boolean.TRUE);
						VideoOptionsForm videoOptions = new VideoOptionsForm(ureq, getWindowControl(), re.getOlatResource(), config);
						videoConfigurationVc.put("videoOptions", videoOptions.getInitialComponent());
						listenTo(videoOptions);
		} else {
			// no valid config yet
			videoConfigurationVc.contextPut("showPreviewButton", Boolean.FALSE);
			videoConfigurationVc.contextPut("showOptions", Boolean.FALSE);
			videoConfigurationVc.contextPut(VC_CHOSENVIDEO, translate(NLS_NO_VIDEO_CHOSEN));
		}



		// Accessibility precondition
		Condition accessCondition = videoNode.getPreConditionAccess();
		accessibilityCondContr = new ConditionEditController(ureq, getWindowControl(),
		accessCondition, AssessmentHelper.getAssessableNodes(course.getEditorTreeModel(), videoNode), euce);
		listenTo(accessibilityCondContr);

		main.setContent(videoConfigurationVc);
	}

	@Override
	public void addTabs(TabbedPane tabbedPane) {
		myTabbedPane = tabbedPane;

		tabbedPane.addTab(translate(PANE_TAB_ACCESSIBILITY), accessibilityCondContr.getWrappedDefaultAccessConditionVC(translate(NLS_CONDITION_ACCESSIBILITY_TITLE)));
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
			removeAsListenerAndDispose(searchController);
			searchController = new ReferencableEntriesSearchController(getWindowControl(), ureq, new String[] {VideoFileResource.TYPE_NAME}, translate(NLS_COMMAND_CHOOSEVIDEO), true, false, false, false);
			listenTo(searchController);

			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(
					getWindowControl(), translate("close"), searchController.getInitialComponent(), true, translate(NLS_COMMAND_CHOOSEVIDEO)
			);
			listenTo(cmc);
			cmc.activate();
		}
		if(source == previewLink){
			VideoDisplayController previewController = null;
			switch(config.getStringValue(VideoEditController.CONFIG_KEY_DESCRIPTION_SELECT)){

			case "resourceDescription":
					previewController = new VideoDisplayController(ureq, getWindowControl(), re, config.getBooleanSafe(VideoEditController.CONFIG_KEY_AUTOPLAY), config.getBooleanSafe(VideoEditController.CONFIG_KEY_COMMENTS), config.getBooleanSafe(VideoEditController.CONFIG_KEY_RATING), "", false,false, "");
					break;
			case "customDescription":
					previewController = new VideoDisplayController(ureq, getWindowControl(), re, config.getBooleanSafe(VideoEditController.CONFIG_KEY_AUTOPLAY), config.getBooleanSafe(VideoEditController.CONFIG_KEY_COMMENTS), config.getBooleanSafe(VideoEditController.CONFIG_KEY_RATING), "", true,false, config.getStringValue(VideoEditController.CONFIG_KEY_DESCRIPTION_CUSTOMTEXT));
					break;
			case "none":
					previewController = new VideoDisplayController(ureq, getWindowControl(), re, config.getBooleanSafe(VideoEditController.CONFIG_KEY_AUTOPLAY), config.getBooleanSafe(VideoEditController.CONFIG_KEY_COMMENTS), config.getBooleanSafe(VideoEditController.CONFIG_KEY_RATING), "", true,false, "");
					break;
			}
			cmc = new CloseableModalController(
					getWindowControl(), translate("close"), previewController.getInitialComponent(), true, translate(NLS_COMMAND_CHOOSEVIDEO)
			);
			listenTo(cmc);
			cmc.activate();
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest urequest, Controller source, Event event) {
		if (source == searchController) {
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				// search controller done
				// -> close closeable modal controller
				cmc.deactivate();
				re = searchController.getSelectedEntry();
				if (re != null) {
					setVideoReference(re, config);
					videoConfigurationVc.contextPut("showPreviewButton", Boolean.TRUE);
					String displayname = StringHelper.escapeHtml(re.getDisplayname());
					previewLink = LinkFactory.createCustomLink("command.preview", "command.preview", displayname, Link.NONTRANSLATED, videoConfigurationVc, this);
					previewLink.setCustomEnabledLinkCSS("o_preview");
					previewLink.setTitle(getTranslator().translate("command.preview"));
					// fire event so the updated config is saved by the editormaincontroller
					fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);

					videoConfigurationVc.contextPut("showOptions", Boolean.TRUE);
					VideoOptionsForm videoOptions = new VideoOptionsForm(urequest, getWindowControl(), re.getOlatResource(), config);
					videoConfigurationVc.put("videoOptions", videoOptions.getInitialComponent());
					listenTo(videoOptions);
				}
			}
		}

		if (event == NodeEditController.NODECONFIG_CHANGED_EVENT){
			fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
		}
	}
	@Override
	protected void doDispose() {
		//child controllers registered with listenTo() get disposed in BasicController
		if (previewCtr != null) {
			previewCtr.dispose();
			previewCtr = null;
		}
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
		RepositoryEntry entry = rm.lookupRepositoryEntryBySoftkey(repoSoftkey, strict);
		// entry can be null only if !strict
		return entry;
	}

	/**
	 * @param moduleConfiguration
	 * @return boolean
	 */
	public static boolean isModuleConfigValid(ModuleConfiguration moduleConfiguration) {
		return (moduleConfiguration.get(CONFIG_KEY_REPOSITORY_SOFTKEY) != null);
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
	protected VideoManager videoManager = CoreSpringFactory.getImpl(VideoManager.class);

	private OLATResource video;
	private SelectionElement videoComments;
	private SelectionElement videoRating;
	private SelectionElement videoAutoplay;
	private SingleSelection description;
	private RichTextElement descriptionField;
	private boolean commentsEnabled;
	private boolean ratingEnabled;
	private boolean autoplay;
	private ModuleConfiguration config;



	VideoOptionsForm(UserRequest ureq, WindowControl wControl, OLATResource video, ModuleConfiguration moduleConfiguration) {
		super(ureq, wControl);
		this.config = moduleConfiguration;
		this.video = video;
		this.commentsEnabled = config.getBooleanSafe(VideoEditController.CONFIG_KEY_COMMENTS);
		this.ratingEnabled = config.getBooleanSafe(VideoEditController.CONFIG_KEY_RATING);
		this.autoplay = config.getBooleanSafe(VideoEditController.CONFIG_KEY_AUTOPLAY);
		initForm(ureq);
	}

	public boolean isVideoComments() {
		return videoComments.isSelected(0);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		config.setBooleanEntry(VideoEditController.CONFIG_KEY_COMMENTS, videoComments.isSelected(0));
		config.setBooleanEntry(VideoEditController.CONFIG_KEY_RATING, videoRating.isSelected(0));
		config.setBooleanEntry(VideoEditController.CONFIG_KEY_AUTOPLAY, videoAutoplay.isSelected(0));
		config.setStringValue(VideoEditController.CONFIG_KEY_DESCRIPTION_SELECT, description.getSelectedKey());
		if(description.getSelectedKey() == "customDescription"){
			config.setStringValue(VideoEditController.CONFIG_KEY_DESCRIPTION_CUSTOMTEXT, descriptionField.getValue());
		}
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		videoComments = uifactory.addCheckboxesHorizontal("videoComments", "video.config.comments", formLayout, new String[]{"xx"}, new String[]{null});
		videoComments.select("xx",commentsEnabled);
		videoRating = uifactory.addCheckboxesHorizontal("videoRating", "video.config.rating", formLayout, new String[]{"xx"}, new String[]{null});
		videoRating.select("xx",ratingEnabled);
		videoAutoplay = uifactory.addCheckboxesHorizontal("videoAutoplay", "video.config.autoplay", formLayout, new String[]{"xx"}, new String[]{null});
		videoAutoplay.select("xx",autoplay);

		Map<String, String> descriptionOptions = new HashMap<String, String>();
		descriptionOptions.put("none" ,"none");//TODO: internationalize
		descriptionOptions.put("resourceDescription", "Resource description");
		descriptionOptions.put("customDescription", "custom description");

		description = uifactory.addDropdownSingleselect("video.config.description", formLayout, descriptionOptions.keySet().toArray(new String[3]), descriptionOptions.values().toArray(new String[3]), new String[3]);
		description.addActionListener(FormEvent.ONCHANGE);
		description.select(config.getStringValue(VideoEditController.CONFIG_KEY_DESCRIPTION_SELECT,"none"), true);
		descriptionField = uifactory.addRichTextElementForStringDataMinimalistic("description", "", videoManager.getDescription(video), -1, -1, formLayout, getWindowControl());
		updateDescriptionField();
		uifactory.addFormSubmitButton("submit", formLayout);
	}

	private void updateDescriptionField(){
		switch(description.getSelected()){
		case 2:
			descriptionField.setVisible(false);
			break;
		case 1:
			descriptionField.setVisible(true);
			descriptionField.setValue(videoManager.getDescription(video));
			descriptionField.setEnabled(false);
			break;
		case 0:
			descriptionField.setVisible(true);
			descriptionField.setValue(config.getStringValue(VideoEditController.CONFIG_KEY_DESCRIPTION_CUSTOMTEXT, ""));
			descriptionField.setEnabled(true);
			break;
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
