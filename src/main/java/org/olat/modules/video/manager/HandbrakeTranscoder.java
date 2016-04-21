package org.olat.modules.video.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import org.olat.core.CoreSpringFactory;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.fileresource.FileResourceManager;
import org.olat.modules.video.VideoManager;
import org.olat.resource.OLATResource;

public class HandbrakeTranscoder implements Transcoder {
	private OLog logger = Tracing.createLoggerFor(this.getClass());
	private VideoManager videoManager = CoreSpringFactory.getImpl(VideoManager.class);
	private FileResourceManager fileResourceManager = FileResourceManager.getInstance();

	@Override
	public boolean transcodeVideoRessource(OLATResource video, Map<String, String> params){
		File file = videoManager.getVideoFile(video);
		File videoResourceFileroot = fileResourceManager.getFileResourceRoot(video);
		File optimizedFolder = new File(videoResourceFileroot, "optimizedVideoData");
		optimizedFolder.mkdirs();

		ArrayList<String> cmd = new ArrayList<String>();

		cmd.add("HandBrakeCLI");
		cmd.add("-i "+file.getAbsolutePath());
		cmd.add("-o "+optimizedFolder.getAbsolutePath()+"/optimized_"+file.getName());
		cmd.add("--optimize");
		cmd.add("--preset Normal");

		ProcessBuilder pb = new ProcessBuilder(cmd);
		pb.directory(optimizedFolder);

		pb.redirectErrorStream(true);
		pb.inheritIO();

		 try {
				logger.info("+--------------------------HANDBRAKE STARTS TRANSCODING------------------------------------+");
	            Runtime.getRuntime().exec("HandBrakeCLI "+" -i "+file.getAbsolutePath()+" -o "+optimizedFolder.getAbsolutePath()+"/optimized_"+file.getName()+" --optimize"+" --preset Normal");
				logger.info("+---------------------------HANDBRAKE TRANSCODING DONE-------------------------------------+");
				return true;
		 } catch (Exception e) {
	            System.err.println("Unable to do videotranscoding");
				return false;
	     }
	}

	private boolean transcodeVideoRessourceLow(OLATResource video) {
		// TODO Auto-generated method stub
		return false;
	}


	private boolean transcodeVideoRessourceMid(OLATResource video) {
		// TODO Auto-generated method stub
		return false;
	}


	private boolean transcodeVideoRessourceHi(OLATResource video) {
		// TODO Auto-generated method stub
		return false;
	}

}
