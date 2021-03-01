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
package org.olat.course.nodes.document;

import org.olat.core.CoreSpringFactory;
import org.olat.course.noderight.NodeRightService;
import org.olat.course.nodes.DocumentCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Initial date: 3 Sep 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DocumentSecurityCallbackFactory {
	
	public static final DocumentSecurityCallback createSecurityCallback(DocumentCourseNode courseNode,
			UserCourseEnvironment userCourseEnvironment) {
		ModuleConfiguration configs = courseNode.getModuleConfiguration();
		
		NodeRightService nodeRightService = CoreSpringFactory.getImpl(NodeRightService.class);
		
		boolean edit = userCourseEnvironment.isCourseReadOnly()
				? false
				: nodeRightService.isGranted(configs, userCourseEnvironment, DocumentCourseNode.EDIT);
		
		boolean download = nodeRightService.isGranted(configs, userCourseEnvironment, DocumentCourseNode.DOWNLOAD);
		
		return new DocumentSecurityCallbackImpl(download, edit);
	}
	
	private static final class DocumentSecurityCallbackImpl implements DocumentSecurityCallback {
		
		private final boolean download;
		private final boolean edit;
		
		private DocumentSecurityCallbackImpl(boolean download, boolean edit) {
			this.download = download;
			this.edit = edit;
		}

		@Override
		public boolean canDownload() {
			return download;
		}
		
		@Override
		public boolean canEdit() {
			return edit;
		}
		
	}

}
