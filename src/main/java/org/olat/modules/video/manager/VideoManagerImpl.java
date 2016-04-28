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

package org.olat.modules.video.manager;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.math.RoundingMode;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;

import org.jcodec.api.FrameGrab;
import org.jcodec.common.FileChannelWrapper;
import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.commons.services.video.MovieService;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.ResourceEvaluation;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoModule;
import org.olat.modules.video.model.VideoMetadata;
import org.olat.modules.video.model.VideoQualityVersion;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manager for Videoressource
 * 
 * @author dfurrer, dirk.furrer@frentix.com, http://www.frentix.com
 *
 */
@Service("videoManager")
public class VideoManagerImpl implements VideoManager {
	public static final String FILETYPE_MP4 = "mp4";
	private static final String FILENAME_POSTER_JPG = "poster.jpg";
	private static final String FILENAME_VIDEO_MP4 = "video.mp4";
	private static final String FILENAME_OPTIMIZED_VIDEO_METADATA_XML = "optimizedVideo_metadata.xml";
	private static final String FILENAME_VIDEO_METADATA_XML = "video_metadata.xml";
	
	@Autowired
	private FileResourceManager fileResourceManager;
	@Autowired
	private MovieService movieService;
	@Autowired
	private VideoModule videoModule;
	@Autowired 
	private RepositoryManager repositoryManager;
	@Autowired
	private TaskExecutorManager taskManager;
	
	private static final OLog log = Tracing.createLoggerFor(VideoManagerImpl.class);

	/**
	 * return the resolution of the video in size format from its metadata
	 */
	@Override
	public Size getVideoSize(OLATResource video) {
		Size size = null;
		VideoMetadata metaData = readVideoMetadataFile(video);
		if (metaData == null || metaData.getSize() == null) {
			// unknown size, set to most common 4:3 aspect ratio
			size = new Size(800, 600, false);
			setVideoSize(video, size);			
		} else {
			size = metaData.getSize();
		}
		
		return size;
	}

	/**
	 * set the resolution of a video in metadata
	 */
	@Override
	public void setVideoSize(OLATResource video, Size size){
		VideoMetadata metaData = readVideoMetadataFile(video);
		metaData.setSize(size);
		writeVideoMetadataFile(metaData, video);
	}

	/**
	 * get the configured posterframe
	 */
	@Override
	public VFSLeaf getPosterframe(OLATResource video) {
		String posterframePath = readVideoMetadataFile(video).getPosterframe();
		VFSLeaf posterFrame = resolve(video,posterframePath);
		return posterFrame;
	}

	/**
	 * set a specific VFSLeaf as posterframe in video metadata
	 */
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

		VFSLeaf newPoster = VFSManager.resolveOrCreateLeafFromPath(fileResourceManager.getFileResourceMedia(video), FILENAME_POSTER_JPG);

		if(!newPoster.isSame(posterframe)){
			VFSManager.copyContent(posterframe, newPoster);
		}
		metaData.setPosterframe(newPoster.getName());
		writeVideoMetadataFile(metaData, video);
		
		// Update also repository entry image, use new posterframe
		VFSContainer mediaContainer = getMediaContainer(video);
		VFSLeaf posterImage = (VFSLeaf)mediaContainer.resolve(FILENAME_POSTER_JPG);
		if (posterImage != null) {
			RepositoryEntry repoEntry = repositoryManager.lookupRepositoryEntry(video, true);
			repositoryManager.setImage(posterImage, repoEntry);
		}

	}

	/**
	 * set the title of the video in metadata
	 */
	@Override
	public void setTitle(OLATResource video, String title){
		VideoMetadata metaData = readVideoMetadataFile(video);
		metaData.setTitle(title);
		writeVideoMetadataFile(metaData, video);
	}

	/**
	 * get the title of the video
	 */
	@Override
	public String getTitle(OLATResource video) {
		return readVideoMetadataFile(video).getTitle();
	}

	/**
	 * add a subtitle-track to the videoresource
	 */
	@Override
	public void addTrack(OLATResource video, String lang, VFSLeaf trackFile){
		VideoMetadata metaData = readVideoMetadataFile(video);
		metaData.addTrack(lang, trackFile.getName());
		writeVideoMetadataFile(metaData, video);
	}

	/**
	 * get a specific subtitle-track of the videoresource
	 */
	@Override
	public VFSLeaf getTrack(OLATResource video, String lang) {
		VideoMetadata metaData = readVideoMetadataFile(video);
		return resolve(video, metaData.getTrack(lang));
	}
	
	/**
	 * remove a specific track from the videoresource
	 */
	@Override
	public void removeTrack(OLATResource video, String lang){
		VideoMetadata metaData = readVideoMetadataFile(video);
		resolve(video, metaData.getTrack(lang)).delete();
		metaData.removeTrack(lang);
		writeVideoMetadataFile(metaData, video);
	}
	
	/**
	 * get all tracks saved in the video metadata as map
	 */
	@Override
	public HashMap<String, VFSLeaf> getAllTracks(OLATResource video) {
		VideoMetadata metaData = readVideoMetadataFile(video);
		HashMap<String, VFSLeaf> tracks = new HashMap<String, VFSLeaf>();
		for(Entry<String, String> trackEntry : metaData.getAllTracks().entrySet()){
			tracks.put(trackEntry.getKey(), resolve(video, trackEntry.getValue()));
		}
		return tracks;
	}

	/**
	 * write the the given frame at frameNumber in the frame leaf
	 * @param video videoresource
	 * @param frameNumber the frameNumber at which the frame should be taken from
	 * @param frame the VFSLeaf to write the picked image to
	 */
	@Override
	public boolean getFrame(OLATResource video, int frameNumber, VFSLeaf frame) {
		File videoFile = ((LocalFileImpl)getMasterVideoFile(video)).getBasefile();
		
		try (RandomAccessFile randomAccessFile = new RandomAccessFile(videoFile, "r")) {
			FileChannel ch = randomAccessFile.getChannel();
			FileChannelWrapper in = new FileChannelWrapper(ch);
			FrameGrab frameGrab = new FrameGrab(in).seekToFrameSloppy(frameNumber);
			OutputStream frameOutputStream = frame.getOutputStream(true);

			BufferedImage bufImg = frameGrab.getFrame();
			ImageIO.write(bufImg, "JPG", frameOutputStream);

			// close everything to prevent resource leaks
			frameOutputStream.close();
			in.close();
			ch.close();

			return true;
		} catch (Exception e) {
			return false;
		} 
	}
	
	/**
	 * set descriptiontext which is stored in the metadata of the videoresource
	 * @param video videoresource
	 * @param text descriptiontext
	 */
	@Override
	public void setDescription(OLATResource video, String text) {
		VideoMetadata metaData = readVideoMetadataFile(video);
		metaData.setDescription(text);
		writeVideoMetadataFile(metaData, video);
	}
	
	/**
	 * get the the descriptiontext stored in the metadata of the videoresource
	 */
	@Override
	public String getDescription(OLATResource video) {
		VideoMetadata metaData = readVideoMetadataFile(video);
		return metaData.getDescription();
	}

	/**
	 * get the File of the videoresource 
	 */
	@Override
	public File getVideoFile(OLATResource video) {
		VFSContainer mediaContainer = getMediaContainer(video);
		LocalFileImpl videoFile = (LocalFileImpl) mediaContainer.resolve(FILENAME_VIDEO_MP4);
		return videoFile.getBasefile();
	}


	/**
	 * resolve the given path to a videoresource file and return it
	 * @param video corresponding videoresource
	 * @param path path to the videofile
	 * @return VFSLeaf of videofile of resource
	 */
	private VFSLeaf resolve(OLATResource video, String path){
		VFSItem item = VFSManager.resolveFile(fileResourceManager.getFileResourceMedia(video), path);
		if(item instanceof VFSLeaf){
			return (VFSLeaf) item;
		}else{
			return null;
		}
	}

	/**
	 * write the metdatadata-xml in the videoresource folder
	 * @param metaData
	 * @param video
	 */
	private void writeVideoMetadataFile(VideoMetadata metaData, OLATResource video){
		File videoResourceFileroot = FileResourceManager.getInstance().getFileResourceRootImpl(video).getBasefile();
		File metaDataFile = new File(videoResourceFileroot,FILENAME_VIDEO_METADATA_XML);
		XStreamHelper.writeObject(XStreamHelper.createXStreamInstance(), metaDataFile, metaData);
	}

	/**
	 * return the metdatadata-xml in the videoresource folder
	 * @param video
	 * @return
	 */
	private VideoMetadata readVideoMetadataFile(OLATResource video){
		File videoResourceFileroot = FileResourceManager.getInstance().getFileResourceRootImpl(video).getBasefile();
		File metaDataFile = new File(videoResourceFileroot, FILENAME_VIDEO_METADATA_XML);
		return (VideoMetadata) XStreamHelper.readObject(XStreamHelper.createXStreamInstance(), metaDataFile);
	}
	
	@Override
	public void startTranscodingProcess(OLATResource video) {
		//TODO: check for existing version, add option to force rebuild of all versions
		Size size = getVideoSize(video);
		int height = size.getHeight();
		//TODO: GUI to admin console to manage transcoding resolutions
		int[] resolutions = videoModule.getTranscodingResolutions();
		for (int resolution : resolutions) {
			if (height < resolution) {
				continue;
			}
			VideoQualityVersion version = addNewVersionForTranscoding(video, resolution);
			VideoTranscodingTask task = new VideoTranscodingTask(video, version);
			taskManager.execute(task, null, video, null, new Date());		
		}
		// start transcoding immediately
		taskManager.executeTaskToDo();
	}
	
	
	@Override
	public List<VideoQualityVersion> getQualityVersions(OLATResource video){
		VFSContainer optimizedDataContainer = getTranscodingContainer(video);
		VFSLeaf optimizedMetadataFile = VFSManager.resolveOrCreateLeafFromPath(optimizedDataContainer, FILENAME_OPTIMIZED_VIDEO_METADATA_XML);
		
		List<VideoQualityVersion> versions;
		
		if (optimizedMetadataFile.getSize() == 0) {
			versions = new ArrayList<VideoQualityVersion>();
		} else {
			Object fileContent = XStreamHelper.readObject(XStreamHelper.createXStreamInstance(), optimizedMetadataFile);
			versions = (List<VideoQualityVersion>) fileContent;
		}
		return versions;
	}

	@Override
	public String getAspectRatio(Size videoSize) {
		DecimalFormat df = new DecimalFormat("#.##");
		df.setRoundingMode(RoundingMode.FLOOR);
		String ratioCalculated = null;
		String ratioString = "unknown";
		
		if (videoSize.getHeight() != 0) {
			ratioCalculated = df.format(videoSize.getWidth() / (videoSize.getHeight() + 1.0));
		}
		switch (ratioCalculated) {
		case "1.2": 
			ratioString = "6:5 Fox Movietone";
			break;
		case "1.25": 
			ratioString = "5:4 TV";
			break;
		case "1.33": 
			ratioString = "4:3 TV";
			break;
		case "1.37": 
			ratioString = "11:8 Academy standard film";
			break;
		case "1.41": 
			ratioString = "A4";
			break;
		case "1.43": 
			ratioString = "IMAX";
			break;
		case "1.5": 
			ratioString = "3:2 35mm";
			break;
		case "1.6": 
			ratioString = "16:10 Computer";
			break;
		case "1.61": 
			ratioString = "16.18:10 The golden ratio";
			break;
		case "1.66": 
			ratioString = "5:3 Super 16mm";
			break;
		case "1.77": 
			ratioString = "16:9 HD video";
			break;
		case "1.78": 
			ratioString = "16:9 HD video";
			break;
		case "1.85": 
			ratioString = "1.85:1 Widescreen cinema";
			break;
		case "2.35": 
			ratioString = "2.35:1 Widescreen cinema";
			break;
		case "2.39": 
			ratioString = "2.39:1 Widescreen cinema";
			break;
		case "2.41": 
			ratioString = "2.414:1 The silver ratio";		
			break;
		default :
			ratioString = videoSize.getWidth() + ":" + videoSize.getHeight();
		}
		return ratioString;
	}
	
	@Override
	public VFSContainer getMediaContainer(OLATResource videoResource) {
		return FileResourceManager.getInstance().getFileResourceMedia(videoResource);
	}

	
	@Override
	public VFSContainer getTranscodingContainer(OLATResource videoResource) {
		VFSContainer baseContainer = videoModule.getTranscodingBaseContainer();
		VFSContainer resourceTranscodingContainer = VFSManager.getOrCreateContainer(baseContainer,
				String.valueOf(videoResource.getResourceableId()));
		return resourceTranscodingContainer;
	}
	
	
	@Override
	public VFSLeaf getMasterVideoFile(OLATResource videoResource) {
		VFSContainer mediaContainer = getMediaContainer(videoResource);
		VFSLeaf videoFile = (VFSLeaf) mediaContainer.resolve(FILENAME_VIDEO_MP4);
		return videoFile;
	}
	
	@Override
	public VideoExportMediaResource getVideoExportMediaResource(OLATResource videoResource) {
		VFSContainer baseContainer= FileResourceManager.getInstance().getFileResourceRootImpl(videoResource);
		String title = getTitle(videoResource);
		VideoExportMediaResource exportResource = new VideoExportMediaResource(baseContainer, title);
		return exportResource;
	}

	@Override
	public void validateVideoExportArchive(File file,  ResourceEvaluation eval) {
		ZipFile zipFile;
		try {
			zipFile = new ZipFile(file);
			// 1) Check if it contains a metadata file
			ZipEntry metadataEntry = zipFile.getEntry(VideoManagerImpl.FILENAME_VIDEO_METADATA_XML);
			InputStream metaDataStream = zipFile.getInputStream(metadataEntry);
			VideoMetadata videoMetadata = (VideoMetadata) XStreamHelper.readObject(XStreamHelper.createXStreamInstance(), metaDataStream);
			zipFile.close();
			if (videoMetadata != null) {
				// 2) copy some metadata to the evaluation to be applied to the repo entry
				eval.setDisplayname(videoMetadata.getTitle());
				eval.setDescription(videoMetadata.getDescription());
				eval.setValid(true);
			}
		} catch (Exception e) {
			log.error("Error while checking for video resource archive", e);
		}
	}
	
	@Override
	public boolean importFromMasterFile(RepositoryEntry repoEntry, VFSLeaf masterVideo) {
		OLATResource videoResource = repoEntry.getOlatResource();
		
		// 1) copy master video to final destination with standard name
		VFSContainer mediaContainer = getMediaContainer(videoResource);
		VFSLeaf targetFile = VFSManager.resolveOrCreateLeafFromPath(mediaContainer, FILENAME_VIDEO_MP4);
		VFSManager.copyContent(masterVideo, targetFile);
		masterVideo.delete();

		// 2) generate Metadata file
		VideoMetadata metaData = new VideoMetadata(videoResource);
		metaData.setTitle(repoEntry.getDisplayname());
		metaData.setDescription(repoEntry.getDescription());
		// calculate video size
		Size videoSize = movieService.getSize(getMasterVideoFile(videoResource),FILETYPE_MP4);
		metaData.setSize(videoSize);
		// generate a poster image, use 20th frame as a default
		VFSLeaf posterResource = VFSManager.resolveOrCreateLeafFromPath(
				FileResourceManager.getInstance().getFileResourceMedia(videoResource), FILENAME_POSTER_JPG);
		getFrame(videoResource, 20, posterResource);
		metaData.setPosterframe(FILENAME_POSTER_JPG);
		// finally safe to disk
		writeVideoMetadataFile(metaData, videoResource);

		// 4) Set poster image for repo entry
		VFSLeaf posterImage = (VFSLeaf)mediaContainer.resolve(FILENAME_POSTER_JPG);
		if (posterImage != null) {
			repositoryManager.setImage(posterImage, repoEntry);
		}
		
		// 5) start transcoding process
		if (videoModule.isTranscodingEnabled()) {
			startTranscodingProcess(videoResource);
		}
		
		return true;
	}

	@Override
	public boolean importFromExportArchive(RepositoryEntry repoEntry, VFSLeaf exportArchive) {
		OLATResource videoResource = repoEntry.getOlatResource();
		// 1) unzip archive
		VFSContainer baseContainer= FileResourceManager.getInstance().getFileResourceRootImpl(videoResource);
		ZipUtil.unzip(exportArchive, baseContainer);
		exportArchive.delete();
		
		// 2) update metadata from the repo entry (maybe changed during import
		VideoMetadata metaData = readVideoMetadataFile(videoResource);
		String title = repoEntry.getDisplayname();
		boolean dirty = false;
		if (title != null && !title.equals(metaData.getTitle())) {
			metaData.setTitle(title);
			dirty = true;
		}
		String desc = repoEntry.getDescription();
		if (desc != null && !title.equals(metaData.getDescription())) {
			metaData.setDescription(desc);
			dirty = true;
		}
		if (dirty) {
			writeVideoMetadataFile(metaData, videoResource);
		}
		// 3) Set poster image for repo entry
		VFSContainer mediaContainer = getMediaContainer(videoResource);
		VFSLeaf posterImage = (VFSLeaf)mediaContainer.resolve(FILENAME_POSTER_JPG);
		if (posterImage != null) {
			repositoryManager.setImage(posterImage, repoEntry);
		}
		// 3) start transcoding process
		if (videoModule.isTranscodingEnabled()) {
			startTranscodingProcess(videoResource);
		}

		return true;
	}

	
	@Override
	public VideoQualityVersion addNewVersionForTranscoding(OLATResource video, int resolution) {
		List<VideoQualityVersion> versions = getQualityVersions(video);
		VideoQualityVersion version = new VideoQualityVersion(resolution, null, null, VideoManagerImpl.FILETYPE_MP4);
		version.setTranscodingStatus(VideoQualityVersion.TRANSCODING_STATUS_WAITING);
		versions.add(version);
		// Store on disk
		VFSContainer optimizedDataContainer = getTranscodingContainer(video);
		VFSLeaf optimizedMetadataFile = VFSManager.resolveOrCreateLeafFromPath(optimizedDataContainer, FILENAME_OPTIMIZED_VIDEO_METADATA_XML);
		XStreamHelper.writeObject(XStreamHelper.createXStreamInstance(), optimizedMetadataFile, versions);
		
		return version;
	}

	@Override
	public void updateVersion(OLATResource video, VideoQualityVersion updatedVersion) {
		//TODO: fix concurrency issues, not multithread safe
		List<VideoQualityVersion> versions = getQualityVersions(video);
		boolean found = false;
		for (VideoQualityVersion existingVersion : versions) {
			if (updatedVersion.getResolution() == existingVersion.getResolution()) {
				// update properties
				existingVersion.setDimension(updatedVersion.getDimension());
				existingVersion.setFileSize(updatedVersion.getFileSize());
				existingVersion.setFormat(updatedVersion.getFormat());
				existingVersion.setTranscodingStatus(updatedVersion.getTranscodingStatus());
				found = true;
				break;
			}
		}
		if (!found) {
			versions.add(updatedVersion);
		}
		// Store on disk
		VFSContainer optimizedDataContainer = getTranscodingContainer(video);
		VFSLeaf optimizedMetadataFile = VFSManager.resolveOrCreateLeafFromPath(optimizedDataContainer, FILENAME_OPTIMIZED_VIDEO_METADATA_XML);
		XStreamHelper.writeObject(XStreamHelper.createXStreamInstance(), optimizedMetadataFile, versions);
	}

	@Override
	public void copyVideo(OLATResource sourceResource, OLATResource targetResource) {
		// 1) Copy files on disk
		File sourceFileroot = FileResourceManager.getInstance().getFileResourceRootImpl(sourceResource).getBasefile();
		File targetFileroot = FileResourceManager.getInstance().getFileResourceRootImpl(targetResource).getBasefile();
		FileUtils.copyDirContentsToDir(sourceFileroot, targetFileroot, false, "copyVideoResource");
		// 2) Trigger transcoding in background
		if (videoModule.isTranscodingEnabled()) {
			startTranscodingProcess(targetResource);
		}
	}

}
