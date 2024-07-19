/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.topicbroker.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorDisplayInfo;
import org.olat.core.commons.services.doceditor.DocEditorOpenInfo;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.topicbroker.TBCustomField;
import org.olat.modules.topicbroker.TBCustomFieldType;
import org.olat.modules.topicbroker.TBGroupRestrictionInfo;
import org.olat.modules.topicbroker.TBTopic;
import org.olat.modules.topicbroker.TopicBrokerService;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 6 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBTopicDescriptionController extends BasicController {
	
	private static final String CMD_OPEN_GROUP = "open.group";
	private static final String CMD_OPEN_FILE = "open.file";
	
	private Controller docEditorCtrl;
	
	@Autowired
	private TopicBrokerService topicBrokerService;
	@Autowired
	private DocEditorService docEditorService;

	protected TBTopicDescriptionController(UserRequest ureq, WindowControl wControl, TBTopic topic,
			List<TBGroupRestrictionInfo> groupRestrictions, List<TBCustomField> customFields) {
		super(ureq, wControl);
		VelocityContainer mainVC = createVelocityContainer("topic_description");
		putInitialPanel(mainVC);

		mainVC.contextPut("description", TBUIFactory.formatPrettyText(topic.getDescription(), null));
		mainVC.contextPut("participantRange", TBUIFactory.getParticipantRange(getTranslator(), topic));
		
		if (groupRestrictions != null && !groupRestrictions.isEmpty()) {
			Collections.sort(groupRestrictions,  (i1, i2) -> i1.getGroupName().compareToIgnoreCase(i2.getGroupName()));
			List<String> groupLinkNames = new ArrayList<>(groupRestrictions.size());
			for (TBGroupRestrictionInfo groupInfo : groupRestrictions) {
				Link link = LinkFactory.createCustomLink("grp_" + groupInfo.getGroupKey(), CMD_OPEN_GROUP, null,
						Link.LINK + Link.NONTRANSLATED, mainVC, this);
				link.setCustomDisplayText(StringHelper.escapeHtml(groupInfo.getGroupName()));
				link.setIconLeftCSS("o_icon o_icon-fw o_icon_group");
				if (groupInfo.isGroupAvailable()) {
					link.setUrl(BusinessControlFactory.getInstance()
							.getAuthenticatedURLFromBusinessPathString("[BusinessGroup:" + groupInfo.getGroupKey() + "]"));
				} else {
					link.setEnabled(false);
				}
				link.setUserObject(groupInfo.getGroupKey());
				groupLinkNames.add(link.getComponentName());
			}
			mainVC.contextPut("groups", groupLinkNames);
		}
		
		if (customFields != null && !customFields.isEmpty()) {
			Collections.sort(customFields,
					(c1, c2) -> Integer.compare(c1.getDefinition().getSortOrder(), c2.getDefinition().getSortOrder()));

			List<CustomFieldItem> items = new ArrayList<>(customFields.size());
			mainVC.contextPut("customFields", items);
			for (TBCustomField customField : customFields) {
				if (TBCustomFieldType.text == customField.getDefinition().getType()) {
					if (StringHelper.containsNonWhitespace(customField.getText())) {
						items.add(new CustomFieldItem(customField.getDefinition().getName(),
								TBUIFactory.formatPrettyText(customField.getText(), null), null));
					}
				} else if (TBCustomFieldType.file == customField.getDefinition().getType()) {
					if (customField.getVfsMetadata() != null) {
						VFSLeaf topicLeaf = topicBrokerService.getTopicLeaf(topic,
								customField.getDefinition().getIdentifier());
						if (topicLeaf != null && topicLeaf.exists()) {
							String linkName = "openfile_" + topic.getKey() + "_" + customField.getKey();
							Link link = LinkFactory.createCustomLink(linkName, CMD_OPEN_FILE, null,
									Link.LINK + Link.NONTRANSLATED, mainVC, this);
							link.setCustomDisplayText(topicLeaf.getName());
							link.setIconLeftCSS(
									"o_icon o_icon-fw " + CSSHelper.createFiletypeIconCssClassFor(topicLeaf.getName()));

							DocEditorDisplayInfo editorInfo = docEditorService.getEditorInfo(getIdentity(),
									ureq.getUserSession().getRoles(), topicLeaf, customField.getVfsMetadata(), false,
									DocEditorService.MODES_EDIT);
							if (editorInfo.isNewWindow()) {
								link.setNewWindow(true, true);
							}
							link.setUserObject(topicLeaf);

							items.add(new CustomFieldItem(customField.getDefinition().getName(), null, linkName));
						}
					}
				}
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (docEditorCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(docEditorCtrl);
		docEditorCtrl = null;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link link) {
			if (CMD_OPEN_GROUP.equals(link.getCommand()) && link.getUserObject() instanceof Long groupKey) {
				doOpenGroup(ureq, groupKey);
			} else if (CMD_OPEN_FILE.equals(link.getCommand()) && link.getUserObject() instanceof VFSLeaf topicLeaf) {
				doOpenOrDownload(ureq, topicLeaf);
			}
		}
	}
	
	private void doOpenGroup(UserRequest ureq, Long groupKey) {
		String businessPath = "[BusinessGroup:" + groupKey + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}

	private void doOpenOrDownload(UserRequest ureq, VFSLeaf topicLeaf) {
		if (topicLeaf == null || !topicLeaf.exists()) {
			showWarning("error.file.does.not.exist");
			return;
		}
		
		DocEditorDisplayInfo editorInfo = docEditorService.getEditorInfo(getIdentity(),
				ureq.getUserSession().getRoles(), topicLeaf, topicLeaf.getMetaInfo(),
				false, DocEditorService.MODES_VIEW);
		if (editorInfo.isEditorAvailable()) {
			doOpenFile(ureq, topicLeaf);
		} else {
			doDownload(ureq, topicLeaf);
		}
	}
	
	private void doOpenFile(UserRequest ureq, VFSLeaf topicLeaf) {
		DocEditorConfigs configs = DocEditorConfigs.builder().build(topicLeaf);
		DocEditorOpenInfo docEditorOpenInfo = docEditorService.openDocument(ureq, getWindowControl(), configs,
				DocEditorService.MODES_VIEW);
		docEditorCtrl = listenTo(docEditorOpenInfo.getController());
	}
	
	private void doDownload(UserRequest ureq, VFSLeaf topicLeaf) {
		VFSMediaResource resource = new VFSMediaResource(topicLeaf);
		resource.setDownloadable(true);
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}
	
	public static final class CustomFieldItem {
		
		private final String name;
		private final String text;
		private final String fileLinkName;
		
		public CustomFieldItem(String name, String text, String fileLinkName) {
			this.name = name;
			this.text = text;
			this.fileLinkName = fileLinkName;
		}

		public String getName() {
			return name;
		}

		public String getText() {
			return text;
		}

		public String getFileLinkName() {
			return fileLinkName;
		}
		
	}

}
