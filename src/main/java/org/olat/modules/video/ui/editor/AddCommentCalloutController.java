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
package org.olat.modules.video.ui.editor;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * Initial date: 2023-03-01<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class AddCommentCalloutController extends BasicController {
	public static final Event TEXT_EVENT = new Event("text");
	public static final Event RECORD_VIDEO_EVENT = new Event("record.video");
	public static final Event IMPORT_FILE_EVENT = new Event("import.file");
	public static final Event IMPORT_URL_EVENT = new Event("import.url");

	private final Link textLink;
	private final Link recordVideoLink;
	private final Link importFileLink;
	private final Link importUrlLink;

	public AddCommentCalloutController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		VelocityContainer mainVC = createVelocityContainer("add_comment");

		textLink = LinkFactory.createLink("comment.add.text", "text", getTranslator(), mainVC, this, Link.LINK);
		textLink.setIconLeftCSS("o_icon o_icon-fw o_icon_text");
		mainVC.put("text", textLink);

		mainVC.contextPut("showSeparator", false);

		recordVideoLink = LinkFactory.createLink("comment.add.record.video", "record", getTranslator(), mainVC, this, Link.LINK);
		recordVideoLink.setIconLeftCSS("o_icon o_icon-fw o_icon_video_record");
		//mainVC.put("record", recordVideoLink);

		importFileLink = LinkFactory.createLink("comment.add.import.file", "importFile", getTranslator(), mainVC, this, Link.LINK);
		importFileLink.setIconLeftCSS("o_icon o_icon-fw o_icon_upload");
		//mainVC.put("importFile", importFileLink);

		importUrlLink = LinkFactory.createLink("comment.add.import.url", "importUrl", getTranslator(), mainVC, this, Link.LINK);
		importUrlLink.setIconLeftCSS("o_icon o_icon-fw o_icon_upload");
		//mainVC.put("importUrl", importUrlLink);

		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (textLink == source) {
			fireEvent(ureq, TEXT_EVENT);
		} else if (recordVideoLink == source) {
			fireEvent(ureq, RECORD_VIDEO_EVENT);
		} else if (importFileLink == source) {
			fireEvent(ureq, IMPORT_FILE_EVENT);
		} else if (importUrlLink == source) {
			fireEvent(ureq, IMPORT_URL_EVENT);
		}
	}
}
