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
package org.olat.course.nodes.dialog.ui;

import java.util.List;

import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.CourseLoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.course.CourseModule;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.dialog.DialogElement;
import org.olat.course.nodes.dialog.DialogElementsManager;
import org.olat.course.nodes.dialog.DialogSecurityCallback;
import org.olat.course.nodes.dialog.security.SecurityCallbackFactory;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.ui.ForumController;
import org.olat.user.UserManager;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DialogElementController extends BasicController implements Activateable2 {
	
	private final Link downloadLink;
	private final VelocityContainer mainVC;
	
	private final ForumController forumCtr;
	
	private final DialogElement element;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private DialogElementsManager dialogElmsMgr;
	
	public DialogElementController(UserRequest ureq, WindowControl wControl, DialogElement element,
			UserCourseEnvironment userCourseEnv, CourseNode courseNode, DialogSecurityCallback secCallback) {
		super(ureq, wControl);
		this.element = element;
		Forum forum = element.getForum();

		boolean isGuestOnly = ureq.getUserSession().getRoles().isGuestOnly();
		
		if (!isGuestOnly) {
			SubscriptionContext subsContext = CourseModule.createSubscriptionContext(
					userCourseEnv.getCourseEnvironment(), courseNode, forum.getKey().toString());
			secCallback = SecurityCallbackFactory.create(secCallback, subsContext);
		}
		
		forumCtr = new ForumController(ureq, wControl, forum, secCallback, !isGuestOnly);
		listenTo(forumCtr);
		
		mainVC = createVelocityContainer("discussion");
		
		downloadLink = LinkFactory.createLink("download", "download", getTranslator(), mainVC, this, Link.LINK | Link.NONTRANSLATED);
		downloadLink.setCustomDisplayText(StringHelper.escapeHtml(element.getFilename()));
		downloadLink.setIconLeftCSS("o_icon o_icon-fw " + CSSHelper.createFiletypeIconCssClassFor(element.getFilename()));
		downloadLink.setTarget("_blank");
		
		mainVC.contextPut("filename", StringHelper.escapeHtml(element.getFilename()));
		if(element.getSize() != null && element.getSize().longValue() > 0) {
			mainVC.contextPut("size", Formatter.formatBytes(element.getSize().longValue()));
		}
		String author = userManager.getUserDisplayName(element.getAuthor());
		mainVC.contextPut("author", StringHelper.escapeHtml(author));
		
		mainVC.put("forum", forumCtr.getInitialComponent());
		putInitialPanel(mainVC);
		
		addToHistory(ureq, OresHelper.createOLATResourceableInstance("Element", element.getKey()), null);
	}
	
	public DialogElement getElement() {
		return element;
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(downloadLink == source) {
			doDownload(ureq);
		}
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String name = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Message".equals(name)) {
			forumCtr.activate(ureq, entries, state);
		}
	}

	private void doDownload(UserRequest ureq) {
		VFSLeaf file = dialogElmsMgr.getDialogLeaf(element);
		if(file != null) {
			ureq.getDispatchResult().setResultingMediaResource(new VFSMediaResource(file));
			ThreadLocalUserActivityLogger.log(CourseLoggingAction.DIALOG_ELEMENT_FILE_DOWNLOADED, getClass(),
					LoggingResourceable.wrapBCFile(element.getFilename()));
		} else {
			ureq.getDispatchResult().setResultingMediaResource(new NotFoundMediaResource());
			logError("No file to discuss: " + element, null);
		}
	}
}