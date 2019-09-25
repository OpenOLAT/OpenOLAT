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
package org.olat.course.nodes.feed.blog;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.ICourse;
import org.olat.course.nodes.AbstractFeedCourseNode;
import org.olat.course.nodes.feed.FeedNodeEditController;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.fileresource.types.BlogFileResource;
import org.olat.modules.webFeed.ui.blog.BlogUIFactory;

/**
 * The blog course node edit controller.
 * 
 * <P>
 * Initial Date: Mar 31, 2009 <br>
 * 
 * @author gwassmann
 */
public class BlogNodeEditController extends FeedNodeEditController {

	public BlogNodeEditController(AbstractFeedCourseNode courseNode, ICourse course, UserCourseEnvironment uce,
			UserRequest ureq, WindowControl control) {
		super(ureq, control, courseNode, course, uce, BlogUIFactory.getInstance(ureq.getLocale()),
				BlogFileResource.TYPE_NAME, "Knowledge Transfer#_blog");
	}
}
