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

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.controllers.linkchooser.CustomMediaChooserController;
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

	private VelocityContainer runVC;
	

	public LLRunController(UserRequest ureq, WindowControl wControl, ModuleConfiguration moduleConfig, LLCourseNode llCourseNode,
			UserCourseEnvironment userCourseEnv, boolean showLinkComments) {
		super(ureq, wControl);
		this.runVC = this.createVelocityContainer("run");
		final List<LLModel> linkList = (List<LLModel>) moduleConfig.get(LLCourseNode.CONF_LINKLIST);
		this.runVC.contextPut("linkList", linkList);
		this.runVC.contextPut("showLinkComments", Boolean.valueOf(showLinkComments));
		
		
		CourseEnvironment courseEnv = userCourseEnv.getCourseEnvironment();
		VFSContainer courseContainer = courseEnv.getCourseFolderContainer();
		Mapper customMapper = null;
		if (CoreSpringFactory.containsBean(CustomMediaChooserController.class.getName())) {
			CustomMediaChooserController customMediaChooserFactory = (CustomMediaChooserController) CoreSpringFactory.getBean(CustomMediaChooserController.class.getName());
			customMapper = customMediaChooserFactory.getMapperInstance(courseContainer, null, null);
		}
		String mapperID = courseEnv.getCourseResourceableId() + "/" + llCourseNode.getIdent();
		String mapperBaseUrl = registerCacheableMapper(mapperID, new LLMapper(linkList, customMapper, courseContainer));
		
		runVC.contextPut("mapperBaseUrl", mapperBaseUrl);
		
		putInitialPanel(runVC);
	}

	@Override
	protected void doDispose() {
		// nothing to dispose here

	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// nothing to do here
	}
	
	public class LLMapper implements Mapper {
		private final List<LLModel> linkList;
		private final Mapper customMediaMapper;
		private final VFSContainer courseContainer;
		
		public LLMapper(List<LLModel> linkList, Mapper customMediaMapper, VFSContainer courseContainer) {
			this.linkList = linkList;
			this.customMediaMapper = customMediaMapper;
			this.courseContainer = courseContainer;
		}

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
				return new NotFoundMediaResource(relPath);
			} else {
				return new ForbiddenMediaResource(relPath);
			}
		}
	}
}
