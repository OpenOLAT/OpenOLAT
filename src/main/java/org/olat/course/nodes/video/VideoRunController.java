package org.olat.course.nodes.video;

import java.io.File;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.course.nodes.TitledWrapperHelper;
import org.olat.course.nodes.VideoCourseNode;
import org.olat.course.nodes.cp.CPRunController;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.video.ui.VideoDisplayController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

public class VideoRunController extends BasicController {
	private static final OLog log = Tracing.createLoggerFor(CPRunController.class);

	private ModuleConfiguration config;
	private File videoRoot;
	private Panel main;
	
	private VideoDisplayController videoDispCtr;
	private VideoCourseNode videoNode;
	

	@Autowired
	private RepositoryManager repositoryManager;
	
	/**
	 * Constructor for single page run controller 
	 * @param wControl
	 * @param ureq
	 * @param userCourseEnv
	 * @param videoNode
	 */
	public VideoRunController(ModuleConfiguration config, WindowControl wControl, UserRequest ureq, VideoCourseNode videoNode) {
		super(ureq,wControl);
		
		// assertion to make sure the moduleconfig is valid
		if (!VideoEditController.isModuleConfigValid(config)) throw new AssertException("videorun controller had an invalid module config:"	+ config.toString());
		this.config = config;
		this.videoNode = videoNode;
		addLoggingResourceable(LoggingResourceable.wrap(videoNode));

		// jump to either the forum or the folder if the business-launch-path says so.
		BusinessControl bc = getWindowControl().getBusinessControl();
		ContextEntry ce = bc.popLauncherContextEntry();
		if ( ce != null ) { // a context path is left for me
			if(log.isDebug()) log.debug("businesscontrol (for further jumps) would be:"+bc);
			OLATResourceable popOres = ce.getOLATResourceable();
			if(log.isDebug()) log.debug("OLATResourceable=" + popOres);
			String typeName = popOres.getResourceableTypeName();
			// typeName format: 'path=/test1/test2/readme.txt'
			// First remove prefix 'path='
			String path = typeName.substring("path=".length());
			if  (path.length() > 0) {
			  if(log.isDebug()) log.debug("direct navigation to container-path=" + path);
//			  this.nodecmd = path;
			}
		}
		
		
		main = new Panel("videorunmain");
		doLaunch(ureq);
		putInitialPanel(main);
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == videoDispCtr){
			if(VideoDisplayController.ENDED_EVENT.equals(event)){
				//TODO: catch even fired when video ended
			}
		}
	}
	
	/**
	 * 
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	@Override
	protected void doDispose() {
		if (videoDispCtr != null) {
			videoDispCtr.dispose();
			videoDispCtr = null;
		}	
	}
	
	private void doLaunch(UserRequest ureq){
		VelocityContainer myContent = createVelocityContainer("run");
		if (videoRoot == null) {
			RepositoryEntry re = VideoEditController.getVideoReference(config, false);
			if (re == null) {
				showError(VideoEditController.NLS_ERROR_VIDEOREPOENTRYMISSING);
				return;
			}


		}
		switch(config.getStringValue(VideoEditController.CONFIG_KEY_DESCRIPTION_SELECT,"none")){
		case "resourceDescription":
				videoDispCtr = new VideoDisplayController(ureq, getWindowControl(), videoNode.getReferencedRepositoryEntry(), config.getBooleanSafe(VideoEditController.CONFIG_KEY_AUTOPLAY), config.getBooleanSafe(VideoEditController.CONFIG_KEY_COMMENTS), config.getBooleanSafe(VideoEditController.CONFIG_KEY_RATING), videoNode.getIdent(), false, false, "");
				break;
		case "customDescription":
				videoDispCtr = new VideoDisplayController(ureq, getWindowControl(), videoNode.getReferencedRepositoryEntry(), config.getBooleanSafe(VideoEditController.CONFIG_KEY_AUTOPLAY), config.getBooleanSafe(VideoEditController.CONFIG_KEY_COMMENTS), config.getBooleanSafe(VideoEditController.CONFIG_KEY_RATING), videoNode.getIdent(), true, false, config.getStringValue(VideoEditController.CONFIG_KEY_DESCRIPTION_CUSTOMTEXT));
				break;
		case "none":
				videoDispCtr = new VideoDisplayController(ureq, getWindowControl(), videoNode.getReferencedRepositoryEntry(), config.getBooleanSafe(VideoEditController.CONFIG_KEY_AUTOPLAY), config.getBooleanSafe(VideoEditController.CONFIG_KEY_COMMENTS), config.getBooleanSafe(VideoEditController.CONFIG_KEY_RATING), videoNode.getIdent(), true, false, "");
				break;
		}
		
		videoDispCtr.addControllerListener(this);
		
		myContent.put("videoDisplay", videoDispCtr.getInitialComponent());
		main.setContent(myContent);
	}
	

	
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq) {
		NodeRunConstructionResult ncr;

			Controller ctrl = TitledWrapperHelper.getWrapper(ureq, getWindowControl(), this, videoNode, "o_cp_icon");
			ncr = new NodeRunConstructionResult(ctrl);
		
		return ncr;
	}
}
