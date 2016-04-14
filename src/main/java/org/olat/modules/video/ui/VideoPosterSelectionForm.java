package org.olat.modules.video.ui;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

import org.jcodec.common.FileChannelWrapper;
import org.jcodec.containers.mp4.demuxer.MP4Demuxer;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FolderEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.helpers.Settings;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.fileresource.FileResourceManager;
import org.olat.modules.video.manager.MediaMapper;
import org.olat.modules.video.manager.VideoManager;
import org.olat.resource.OLATResource;

public class VideoPosterSelectionForm extends BasicController {
	protected FormUIFactory uifactory = FormUIFactory.getInstance();
	long remainingSpace;
	private VFSContainer videoResourceFileroot;
	private VFSContainer metaDataFolder;
	private VideoManager videoManager = CoreSpringFactory.getImpl(VideoManager.class);
	VelocityContainer proposalLayout = createVelocityContainer("video_poster_proposal");

	private Map<String, String> generatedPosters;
	private Map<Link, VFSLeaf> buttons = new HashMap<Link, VFSLeaf>();



	public VideoPosterSelectionForm(UserRequest ureq, WindowControl wControl, OLATResource videoResource) {
		super(ureq, wControl);

		videoResourceFileroot =  new LocalFolderImpl(FileResourceManager.getInstance().getFileResourceRootImpl(videoResource).getBasefile());
		metaDataFolder = VFSManager.getOrCreateContainer(videoResourceFileroot, "media");
		generatedPosters = new HashMap<String, String>();


		long duration =1000;

			RandomAccessFile accessFile;
			try {
				accessFile = new RandomAccessFile(videoManager.getVideoFile(videoResource),"r");
				FileChannel ch = accessFile.getChannel();
				FileChannelWrapper in = new FileChannelWrapper(ch);
				MP4Demuxer demuxer1 = new MP4Demuxer(in);
				duration = demuxer1.getVideoTrack().getFrameCount();
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


		long firstThirdDuration = duration/3;
		for(int x=0; x<=duration;x+=firstThirdDuration ){
			try {

				VFSContainer proposalContainer = VFSManager.getOrCreateContainer(metaDataFolder, "proposalPosters");
				VFSLeaf posterProposal = proposalContainer.createChildLeaf("proposalPoster"+x+".jpg");
				if(posterProposal == null){
					posterProposal = (VFSLeaf) proposalContainer.resolve("/proposalPoster"+x+".jpg");
				}else{
				videoManager.getFrame(videoResource, x, posterProposal);
				}
				MediaMapper mediaMapper = new MediaMapper(proposalContainer);
				String mediaUrl = registerMapper(ureq, mediaMapper);
				String serverUrl = Settings.createServerURI();
				proposalLayout.contextPut("serverUrl", serverUrl);

				Link button = LinkFactory.createButton(String.valueOf(x), proposalLayout, this);
				button.setCustomDisplayText(translate("poster.select"));
				buttons.put(button, posterProposal);
//				.addFormLink(posterProposal.getName(), "selectPoster", "track.delete", "track.delete", null, Link.BUTTON);

				generatedPosters.put(mediaUrl+"/proposalPoster"+x+".jpg", String.valueOf(x));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		proposalLayout.contextPut("pics", generatedPosters);


		putInitialPanel(proposalLayout);
	}





	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		for(Link button: buttons.keySet()){
			if(source == button){
				fireEvent(ureq, new FolderEvent(FolderEvent.UPLOAD_EVENT, buttons.get(button)));
			}
		}
	}
}