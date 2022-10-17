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

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.controllers.linkchooser.CustomMediaChooserFactory;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.media.ForbiddenMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;

import de.bps.course.nodes.LLCourseNode;

/**
 * Description:<br>
 * Run controller for link list nodes.
 *
 * <P>
 * Initial Date: 05.11.2008 <br>
 *
 * @author Marcel Karras (toka@freebits.de)
 */
public class LLRunController extends BasicController {


	public LLRunController(UserRequest ureq, WindowControl wControl, ModuleConfiguration moduleConfig, LLCourseNode llCourseNode,
			UserCourseEnvironment userCourseEnv, boolean showLinkComments) {
		super(ureq, wControl);
		VelocityContainer runVC = createVelocityContainer("run");
		
		@SuppressWarnings("unchecked")
		final List<LLModel> linkList = (List<LLModel>) moduleConfig.get(LLCourseNode.CONF_LINKLIST);
		runVC.contextPut("linkList", linkList);
		runVC.contextPut("showLinkComments", Boolean.valueOf(showLinkComments));
		
		boolean hasInternLinks = hasInternLinks(linkList);
		if(hasInternLinks) {
			CourseEnvironment courseEnv = userCourseEnv.getCourseEnvironment();
			VFSContainer courseContainer = courseEnv.getCourseFolderContainer();
			Mapper customMapper = null;
			if (CoreSpringFactory.containsBean(CustomMediaChooserFactory.class.getName())) {
				CustomMediaChooserFactory customMediaChooserFactory = (CustomMediaChooserFactory)CoreSpringFactory.getBean(CustomMediaChooserFactory.class.getName());
				customMapper = customMediaChooserFactory.getMapperInstance();
			}
			String mapperID = courseEnv.getCourseResourceableId() + "/" + llCourseNode.getIdent();
			String mapperBaseUrl = registerCacheableMapper(ureq, mapperID, new LLMapper(linkList, customMapper, courseContainer));
			runVC.contextPut("mapperBaseUrl", mapperBaseUrl);
		}
		putInitialPanel(runVC);
	}
	
	private boolean hasInternLinks(List<LLModel> linkList) {
		if(linkList != null && !linkList.isEmpty()) {
			for(LLModel link:linkList) {
				if(link.isIntern()) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// nothing to do here
	}
	
	public static class LLMapper implements Mapper {
		private final List<LLModel> linkList;
		private final Mapper customMediaMapper;
		private final VFSContainer courseContainer;
		
		public LLMapper(List<LLModel> linkList, Mapper customMediaMapper, VFSContainer courseContainer) {
			this.linkList = linkList;
			this.customMediaMapper = customMediaMapper;
			this.courseContainer = courseContainer;
		}

		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			boolean ok = false;
			for(LLModel link:linkList) {
				if(relPath.equals(link.getTarget())) {
					ok = true;
					break;
				}
			}
			
			if(ok) {
				//is this a file in course directory
				VFSItem item = courseContainer.resolve(relPath);
				if(item instanceof VFSLeaf) {
					return new VFSMediaResource((VFSLeaf)item);
				} else if(customMediaMapper != null) {
					return customMediaMapper.handle(relPath, request);
				}
				return new NotFoundMediaResource();
			} else {
				return new ForbiddenMediaResource();
			}
		}
	}
}
