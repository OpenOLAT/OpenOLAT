package org.olat.modules.video.managers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import org.jcodec.api.FrameGrab;
import org.jcodec.common.FileChannelWrapper;
import org.olat.core.commons.services.image.Size;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.fileresource.FileResourceManager;
import org.olat.modules.video.models.VideoMetadata;
import org.olat.resource.OLATResource;
import org.springframework.stereotype.Service;

@Service("videoManager")
public class VideoManagerImpl extends VideoManager {
	private FileResourceManager fileResourceManager = FileResourceManager.getInstance();
	private RandomAccessFile randomAccessFile;

	public VideoManagerImpl() {
		INSTANCE = this;
	}

	@Override
	public Size getVideoSize(OLATResource video) {
		return readVideoMetadataFile(video).getSize();
	}

	@Override
	public void setVideoSize(OLATResource video, Size size){
		VideoMetadata metaData = readVideoMetadataFile(video);
		metaData.setSize(size);
		writeVideoMetadataFile(metaData, video);
	}

	@Override
	public VFSLeaf getPosterframe(OLATResource video) {
		String posterframePath = readVideoMetadataFile(video).getPosterframe();
		VFSLeaf posterFrame = resolve(video,posterframePath);
		return posterFrame;
	}

	@Override
	public void setPosterframe(OLATResource video, VFSLeaf posterframe){
		VideoMetadata metaData = readVideoMetadataFile(video);
		String oldPath = metaData.getPosterframe();
		if(oldPath != null){
			VFSLeaf oldPoster = resolve(video, metaData.getPosterframe());
			if(oldPoster != null){
				oldPoster.delete();
			}
		}

		VFSLeaf newPoster = VFSManager.resolveOrCreateLeafFromPath(fileResourceManager.getFileResourceMedia(video), "/poster.jpg");

		if(!newPoster.isSame(posterframe)){
		VFSManager.copyContent(posterframe, newPoster);
		}
		metaData.setPosterframe(newPoster.getName());
		writeVideoMetadataFile(metaData, video);

	}

	@Override
	public void setTitle(OLATResource video, String title){
		VideoMetadata metaData = readVideoMetadataFile(video);
		metaData.setTitle(title);
		writeVideoMetadataFile(metaData, video);
	}

	@Override
	public String getTitle(OLATResource video) {
		return readVideoMetadataFile(video).getTitle();
	}

	@Override
	public void addTrack(OLATResource video, String lang, VFSLeaf trackFile){
		VideoMetadata metaData = readVideoMetadataFile(video);
		metaData.addTrack(lang, trackFile.getName());
		writeVideoMetadataFile(metaData, video);
	}

	@Override
	public HashMap<String, VFSLeaf> getAllTracks(OLATResource video) {
		VideoMetadata metaData = readVideoMetadataFile(video);
		HashMap<String, VFSLeaf> tracks = new HashMap<String, VFSLeaf>();
		for(Entry<String, String> trackEntry : metaData.getAllTracks().entrySet()){
			tracks.put(trackEntry.getKey(), resolve(video, trackEntry.getValue()));
		}
		return tracks;
	}

	@Override
	public VFSLeaf getTrack(OLATResource video, String lang) {
		VideoMetadata metaData = readVideoMetadataFile(video);
		return resolve(video, metaData.getTrack(lang));
	}

	@Override
	public void removeTrack(OLATResource video, String lang){
		VideoMetadata metaData = readVideoMetadataFile(video);
		resolve(video, metaData.getTrack(lang)).delete();
		metaData.removeTrack(lang);
		writeVideoMetadataFile(metaData, video);
	}

	@Override
	public void setCommentsEnabled(OLATResource video, boolean isEnabled) {
		VideoMetadata metaData = readVideoMetadataFile(video);
		metaData.setCommentsEnabled(isEnabled);
		writeVideoMetadataFile(metaData, video);
	}

	@Override
	public boolean getCommentsEnabled(OLATResource video) {
		VideoMetadata metaData = readVideoMetadataFile(video);
		return metaData.getCommentsEnabled();
	}

	@Override
	public void setRatingEnabled(OLATResource video, boolean isEnabled) {
		VideoMetadata metaData = readVideoMetadataFile(video);
		metaData.setRatingEnabled(isEnabled);
		writeVideoMetadataFile(metaData, video);
	}

	@Override
	public boolean getRatingEnabled(OLATResource video) {
		VideoMetadata metaData = readVideoMetadataFile(video);
		return metaData.getRatingEnabled();
	}

	@Override
	public boolean getFrame(OLATResource video, int frameNumber, VFSLeaf frame) throws IOException{
		File rootFolder = fileResourceManager.getFileResourceRoot(video);
		File metaDataFile = new File(rootFolder, "media");
		File videoFile = new File(metaDataFile, "video.mp4");

		randomAccessFile = new RandomAccessFile(videoFile, "r");
		FileChannel ch = randomAccessFile.getChannel();
		FileChannelWrapper in = new FileChannelWrapper(ch);
		try{
			FrameGrab frameGrab = new FrameGrab(in).seekToFrameSloppy(frameNumber);
			OutputStream frameOutputStream = frame.getOutputStream(true);

			BufferedImage bufImg = frameGrab.getFrame();
			ImageIO.write(bufImg, "JPG", frameOutputStream);

			return true;
		}catch(	Exception e){
			return false;
		}
		//TODO: throw right exception
	}

	@Override
	public void setDescription(OLATResource video, String text) {
		VideoMetadata metaData = readVideoMetadataFile(video);
		metaData.setDescription(text);
		writeVideoMetadataFile(metaData, video);
	}

	@Override
	public String getDescription(OLATResource video) {
		VideoMetadata metaData = readVideoMetadataFile(video);
		return metaData.getDescription();
	}

	@Override
	public File getVideoFile(OLATResource video) {
		File rootFolder = fileResourceManager.getFileResourceRoot(video);
		File metaDataFile = new File(rootFolder, "media");
		File videoFile = new File(metaDataFile, "video.mp4");
		return videoFile;
	}


	private VFSLeaf resolve(OLATResource video, String path){
		VFSItem item = VFSManager.resolveFile(fileResourceManager.getFileResourceMedia(video), path);
		if(item instanceof VFSLeaf){
			return (VFSLeaf) item;
		}else{
			return null;
		}
	}

	private void writeVideoMetadataFile(VideoMetadata metaData, OLATResource video){
		File videoResourceFileroot = FileResourceManager.getInstance().getFileResourceRootImpl(video).getBasefile();
		File metaDataFile = new File(videoResourceFileroot,"video_metadata.xml");
		XStreamHelper.writeObject(XStreamHelper.createXStreamInstance(), metaDataFile, metaData);
	}

	private VideoMetadata readVideoMetadataFile(OLATResource video){
		File videoResourceFileroot = FileResourceManager.getInstance().getFileResourceRootImpl(video).getBasefile();
		File metaDataFile = new File(videoResourceFileroot, "video_metadata.xml");
		return (VideoMetadata) XStreamHelper.readObject(XStreamHelper.createXStreamInstance(), metaDataFile);
	}

	@Override
	public boolean optimizeVideoRessource(OLATResource video) {
		File file = getVideoFile(video);
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
				logInfo("+--------------------------HANDBRAKE STARTS TRANSCODING------------------------------------+");
	            Runtime.getRuntime().exec("HandBrakeCLI "+" -i "+file.getAbsolutePath()+" -o "+optimizedFolder.getAbsolutePath()+"/optimized_"+file.getName()+" --optimize"+" --preset Normal");
				logInfo("+---------------------------HANDBRAKE TRANSCODING DONE-------------------------------------+");
				return true;
		 } catch (Exception e) {
	            System.err.println("Unable to do videotranscoding");
				return false;
	     }

		 
//		try {
//			logInfo("+--------------------------HANDBRAKE STARTS TRANSCODING------------------------------------+");
//			Process process = pb.start();
//			process.waitFor();
//			logInfo("+---------------------------HANDBRAKE TRANSCODING DONE-------------------------------------+");
//			return true;
//		} catch (Exception e) {
//			return false;
//		}

	}

}
