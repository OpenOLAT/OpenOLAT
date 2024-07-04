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
package org.olat.modules.webFeed.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.AbstractFeedCourseNode;
import org.olat.modules.ModuleConfiguration;

/**
 * Initial date: Jul 03, 2024
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class FeedMetadataConfigController extends FormBasicController {

	private static final String[] keys = new String[]{"on"};
	private static final String[] values = new String[]{""};

	private MultipleSelectionElement showFeedDescEl;
	private MultipleSelectionElement showFeedTitleEl;
	private MultipleSelectionElement showFeedImageEl;

	private final ModuleConfiguration moduleConfig;

	public FeedMetadataConfigController(UserRequest ureq, WindowControl wControl, ModuleConfiguration moduleConfig) {
		super(ureq, wControl);
		this.moduleConfig = moduleConfig;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer metaCont = FormLayoutContainer.createDefaultFormLayout("feed_metadata_config", getTranslator());
		metaCont.setRootForm(mainForm);
		setFormTitle("feed.metadata.conf.title");
		formLayout.add(metaCont);

		showFeedDescEl = uifactory.addCheckboxesHorizontal("feed.show.desc", "feed.show.desc", formLayout, keys, values);
		boolean showTitle = moduleConfig.getBooleanSafe(AbstractFeedCourseNode.CONFIG_KEY_SHOW_FEED_DESC);
		showFeedDescEl.select(keys[0], showTitle);
		showFeedDescEl.addActionListener(FormEvent.ONCHANGE);

		showFeedTitleEl = uifactory.addCheckboxesHorizontal("feed.show.title", "feed.show.title", formLayout, keys, values);
		boolean showDesc = moduleConfig.getBooleanSafe(AbstractFeedCourseNode.CONFIG_KEY_SHOW_FEED_TITLE);
		showFeedTitleEl.select(keys[0], showDesc);
		showFeedTitleEl.addActionListener(FormEvent.ONCHANGE);

		showFeedImageEl = uifactory.addCheckboxesHorizontal("feed.show.image", "feed.show.image", formLayout, keys, values);
		boolean showImage = moduleConfig.getBooleanSafe(AbstractFeedCourseNode.CONFIG_KEY_SHOW_FEED_IMAGE);
		showFeedImageEl.select(keys[0], showImage);
		showFeedImageEl.addActionListener(FormEvent.ONCHANGE);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof MultipleSelectionElement) {
			if (source == showFeedDescEl) {
				AbstractFeedCourseNode.setShowFeedDesc(moduleConfig, showFeedDescEl.isAtLeastSelected(1));
			} else if (source == showFeedTitleEl) {
				AbstractFeedCourseNode.setShowFeedTitle(moduleConfig, showFeedTitleEl.isAtLeastSelected(1));
			} else if (source == showFeedImageEl) {
				AbstractFeedCourseNode.setShowFeedImage(moduleConfig, showFeedImageEl.isAtLeastSelected(1));
			}
			fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
		}
		super.formInnerEvent(ureq, source, event);
	}
}
