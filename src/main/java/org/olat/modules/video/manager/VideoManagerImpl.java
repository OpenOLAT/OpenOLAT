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
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.IntStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;

import org.jcodec.api.FrameGrab;
import org.jcodec.common.FileChannelWrapper;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.video.MovieService;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSStatus;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.ResourceEvaluation;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoMetadata;
import org.olat.modules.video.VideoModule;
import org.olat.modules.video.VideoTranscoding;
import org.olat.modules.video.model.VideoMetadataImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryImportExport;
import org.olat.repository.RepositoryEntryImportExport.RepositoryEntryImport;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
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
	protected static final String DIRNAME_REPOENTRY = "repoentry";
	public static final String FILETYPE_MP4 = "mp4";
	private static final String FILENAME_POSTER_JPG = "poster.jpg";
	private static final String FILENAME_VIDEO_MP4 = "video.mp4";
	private static final String FILENAME_VIDEO_METADATA_XML = "video_metadata.xml";
	private static final String DIRNAME_MASTER = "master";

	@Autowired
	private MovieService movieService;
	@Autowired
	private VideoModule videoModule;
	@Autowired 
	private RepositoryManager repositoryManager;
	@Autowired
	private VideoTranscodingDAO videoTranscodingDao;
	@Autowired
	private Scheduler scheduler;
	
	private static final OLog log = Tracing.createLoggerFor(VideoManagerImpl.class);

	/**
	 * get the configured posterframe
	 */
	@Override
	public VFSLeaf getPosterframe(OLATResource videoResource) {
		VFSLeaf posterFrame = resolveFromMasterContainer(videoResource, FILENAME_POSTER_JPG);
		return posterFrame;
	}

	/**
	 * set a specific VFSLeaf as posterframe in video metadata
	 */
	@Override
	public void setPosterframe(OLATResource videoResource, VFSLeaf posterframe){
		VFSContainer masterContainer = getMasterContainer(videoResource);
		VFSLeaf newPoster = VFSManager.resolveOrCreateLeafFromPath(masterContainer, FILENAME_POSTER_JPG);
		VFSManager.copyContent(posterframe, newPoster);
		
		// Update also repository entry image, use new posterframe
		VFSLeaf posterImage = (VFSLeaf)masterContainer.resolve(FILENAME_POSTER_JPG);
		if (posterImage != null) {
			RepositoryEntry repoEntry = repositoryManager.lookupRepositoryEntry(videoResource, true);
			repositoryManager.setImage(posterImage, repoEntry);
		}
	}

	/**
	 * add a subtitle-track to the videoresource
	 */
	@Override
	public void addTrack(OLATResource videoResource, String lang, VFSLeaf trackFile){
		VideoMetadata metaData = readVideoMetadataFile(videoResource);
		metaData.addTrack(lang, trackFile.getName());
		writeVideoMetadataFile(metaData, videoResource);
	}

	/**
	 * get a specific subtitle-track of the videoresource
	 */
	@Override
	public VFSLeaf getTrack(OLATResource videoResource, String lang) {
		VideoMetadata metaData = readVideoMetadataFile(videoResource);
		return resolveFromMasterContainer(videoResource, metaData.getTrack(lang));
	}
	
	/**
	 * remove a specific track from the videoresource
	 */
	@Override
	public void removeTrack(OLATResource videoResource, String lang){
		VideoMetadata metaData = readVideoMetadataFile(videoResource);
		resolveFromMasterContainer(videoResource, metaData.getTrack(lang)).delete();
		metaData.removeTrack(lang);
		writeVideoMetadataFile(metaData, videoResource);
	}
	
	/**
	 * get all tracks saved in the video metadata as map
	 */
	@Override
	public HashMap<String, VFSLeaf> getAllTracks(OLATResource videoResource) {
		VideoMetadata metaData = readVideoMetadataFile(videoResource);
		HashMap<String, VFSLeaf> tracks = new HashMap<String, VFSLeaf>();
		for(Entry<String, String> trackEntry : metaData.getAllTracks().entrySet()){
			tracks.put(trackEntry.getKey(), resolveFromMasterContainer(videoResource, trackEntry.getValue()));
		}
		return tracks;
	}

	/**
	 * write the the given frame at frameNumber in the frame leaf
	 * @param videoResource videoresource
	 * @param frameNumber the frameNumber at which the frame should be taken from
	 * @param frame the VFSLeaf to write the picked image to
	 */
	@Override
	public boolean getFrame(OLATResource videoResource, int frameNumber, VFSLeaf frame) {
		File videoFile = ((LocalFileImpl)getMasterVideoFile(videoResource)).getBasefile();
		
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
	 * get the File of the videoresource 
	 */
	@Override
	public File getVideoFile(OLATResource videoResource) {
		VFSContainer masterContainer = getMasterContainer(videoResource);
		LocalFileImpl videoFile = (LocalFileImpl) masterContainer.resolve(FILENAME_VIDEO_MP4);
		return videoFile.getBasefile();
	}


	/**
	 * Resolve the given path to a file in the master directory and return it
	 * 
	 * @param videoResource
	 *            corresponding videoresource
	 * @param path
	 *            path to the videofile
	 * @return VFSLeaf of videofile of resource
	 */
	private VFSLeaf resolveFromMasterContainer(OLATResource videoResource, String path){
		VFSContainer masterContainer = getMasterContainer(videoResource);
		VFSItem item = masterContainer.resolve(path);
		if(item instanceof VFSLeaf){
			return (VFSLeaf) item;
		}else{
			return null;
		}
	}

	/**
	 * Write the metdatadata-xml in the videoresource folder
	 * @param metaData
	 * @param videoResource
	 */
	private void writeVideoMetadataFile(VideoMetadata metaData, OLATResource videoResource){
		VFSContainer baseContainer= FileResourceManager.getInstance().getFileResourceRootImpl(videoResource);
		VFSLeaf metaDataFile = VFSManager.resolveOrCreateLeafFromPath(baseContainer, FILENAME_VIDEO_METADATA_XML);
		XStreamHelper.writeObject(XStreamHelper.createXStreamInstance(), metaDataFile, metaData);
	}

	@Override
	public VideoMetadata readVideoMetadataFile(OLATResource videoResource){
		VFSContainer baseContainer= FileResourceManager.getInstance().getFileResourceRootImpl(videoResource);
		VFSLeaf metaDataFile = VFSManager.resolveOrCreateLeafFromPath(baseContainer, FILENAME_VIDEO_METADATA_XML);
		return (VideoMetadata) XStreamHelper.readObject(XStreamHelper.createXStreamInstance(), metaDataFile);
	}
	
	@Override
	public void startTranscodingProcess(OLATResource video) {
		List<VideoTranscoding> existingTranscodings = getVideoTranscodings(video);
		VideoMetadata videoMetadata = readVideoMetadataFile(video);
		int height = videoMetadata.getHeight();
		// 1) setup transcoding job for original file size
		createTranscodingIfNotCreatedAlready(video, height, VideoTranscoding.FORMAT_MP4, existingTranscodings);
		// 2) setup transcoding jobs for all configured sizes below the original size
		int[] resolutions = videoModule.getTranscodingResolutions();
		for (int resolution : resolutions) {
			if (height <= resolution) {
				continue;
			}
			createTranscodingIfNotCreatedAlready(video, resolution, VideoTranscoding.FORMAT_MP4, existingTranscodings);
		}
		// 3) Start transcoding immediately, force job execution
		if (videoModule.isTranscodingLocal()) {
			try {
				JobDetail detail = scheduler.getJobDetail("videoTranscodingJobDetail", Scheduler.DEFAULT_GROUP);
				scheduler.triggerJob(detail.getName(), detail.getGroup());
			} catch (SchedulerException e) {
				log.error("Error while starting video transcoding job", e);
			}			
		}
	}
	
	/**
	 * Helper to check if a transcoding already exists and only create if not
	 * @param video
	 * @param resolution
	 * @param format
	 * @param existingTranscodings
	 */
	private void createTranscodingIfNotCreatedAlready(OLATResource video, int resolution, String format, List<VideoTranscoding> existingTranscodings) {
		boolean found = false;
		for (VideoTranscoding videoTranscoding : existingTranscodings) {
			if (videoTranscoding.getResolution() == resolution) {
				found = true;
				break;
			}
		}
		if (!found) {
			videoTranscodingDao.createVideoTranscoding(video, resolution, format);
		}		
	}

	
	@Override
	public List<VideoTranscoding> getVideoTranscodings(OLATResource video){
		List<VideoTranscoding> videoTranscodings = videoTranscodingDao.getVideoTranscodings(video);
		return videoTranscodings;
	}
	

	@Override
	public String getAspectRatio(int width, int height) {
		DecimalFormat df = new DecimalFormat("#.##");
		df.setRoundingMode(RoundingMode.FLOOR);
		String ratioCalculated = df.format(width / (height + 1.0));
		String ratioString = "unknown";
		
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
			ratioString = width + ":" + height;
		}
		return ratioString;
	}
	
	@Override
	public String getDisplayTitleForResolution(int resolution, Translator translator) {
		int[] resolutions = videoModule.getTranscodingResolutions();
		boolean knownResolution = IntStream.of(resolutions).anyMatch(x -> x == resolution);
		String title = (knownResolution ? translator.translate("quality.resolution." + resolution) : resolution + "p");
		return title;
	}
	
	
	@Override
	public VFSContainer getMasterContainer(OLATResource videoResource) {
		VFSContainer baseContainer =  FileResourceManager.getInstance().getFileResourceRootImpl(videoResource);
		VFSContainer masterContainer = VFSManager.resolveOrCreateContainerFromPath(baseContainer, DIRNAME_MASTER);
		return masterContainer;
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
		VFSContainer masterContainer = getMasterContainer(videoResource);
		VFSLeaf videoFile = (VFSLeaf) masterContainer.resolve(FILENAME_VIDEO_MP4);
		return videoFile;
	}
	
	@Override
	public VideoExportMediaResource getVideoExportMediaResource(RepositoryEntry repoEntry) {
		OLATResource videoResource = repoEntry.getOlatResource();
		OlatRootFolderImpl baseContainer= FileResourceManager.getInstance().getFileResourceRootImpl(videoResource);
		// 1) dump repo entry metadata to resource folder
		LocalFolderImpl repoentryContainer = (LocalFolderImpl)VFSManager.resolveOrCreateContainerFromPath(baseContainer, DIRNAME_REPOENTRY); 
		RepositoryEntryImportExport importExport = new RepositoryEntryImportExport(repoEntry, repoentryContainer.getBasefile());
		importExport.exportDoExportProperties();
		// 2) package everything in resource folder to streaming zip resource
		VideoExportMediaResource exportResource = new VideoExportMediaResource(baseContainer, repoEntry.getDisplayname());
		return exportResource;
	}

	@Override
	public void validateVideoExportArchive(File file,  ResourceEvaluation eval) {
		ZipFile zipFile;
		try {
			zipFile = new ZipFile(file);
			// 1) Check if it contains a metadata file
			ZipEntry metadataEntry = zipFile.getEntry(VideoManagerImpl.FILENAME_VIDEO_METADATA_XML);
			VideoMetadata videoMetadataImpl = null;
			if (metadataEntry != null) {
				InputStream metaDataStream = zipFile.getInputStream(metadataEntry);
				videoMetadataImpl = (VideoMetadata) XStreamHelper.readObject(XStreamHelper.createXStreamInstance(), metaDataStream);
				if (videoMetadataImpl != null) {
					eval.setValid(true);
				}
			}
			// 2) Propose title from repo metadata
			ZipEntry repoMetadataEntry = zipFile.getEntry(DIRNAME_REPOENTRY + "/" + RepositoryEntryImportExport.PROPERTIES_FILE);
			RepositoryEntryImport repoMetadata = null;
			if (repoMetadataEntry != null) {
				InputStream repoMetaDataStream = zipFile.getInputStream(repoMetadataEntry);
				repoMetadata = RepositoryEntryImportExport.getConfiguration(repoMetaDataStream);
				if (repoMetadata != null) {
					eval.setDisplayname(repoMetadata.getDisplayname());
				}
			}
			
			zipFile.close();
		} catch (Exception e) {
			log.error("Error while checking for video resource archive", e);
		}
	}
	
	@Override
	public boolean importFromMasterFile(RepositoryEntry repoEntry, VFSLeaf masterVideo) {
		OLATResource videoResource = repoEntry.getOlatResource();
		
		// 1) copy master video to final destination with standard name
		VFSContainer masterContainer = getMasterContainer(videoResource);
		VFSLeaf targetFile = VFSManager.resolveOrCreateLeafFromPath(masterContainer, FILENAME_VIDEO_MP4);
		VFSManager.copyContent(masterVideo, targetFile);
		masterVideo.delete();

		// 2) generate Metadata file
		VideoMetadata metaData = new VideoMetadataImpl();
		// calculate video size
		Size videoSize = movieService.getSize(targetFile, FILETYPE_MP4);
		if (videoSize != null) {
			metaData.setWidth(videoSize.getWidth());
			metaData.setHeight(videoSize.getHeight());			
		} else {
			metaData.setWidth(600);
			metaData.setHeight(800);						
		}
		// generate a poster image, use 20th frame as a default
		VFSLeaf posterResource = VFSManager.resolveOrCreateLeafFromPath(masterContainer, FILENAME_POSTER_JPG);
		getFrame(videoResource, 20, posterResource);
		// finally safe to disk
		writeVideoMetadataFile(metaData, videoResource);

		// 4) Set poster image for repo entry
		VFSLeaf posterImage = (VFSLeaf)masterContainer.resolve(FILENAME_POSTER_JPG);
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
		
		// 2) update metadata from the repo entry export
		LocalFolderImpl repoentryContainer = (LocalFolderImpl) baseContainer.resolve(DIRNAME_REPOENTRY); 
		if (repoentryContainer != null) {
			RepositoryEntryImportExport importExport = new RepositoryEntryImportExport(repoentryContainer.getBasefile());
			importExport.setRepoEntryPropertiesFromImport(repoEntry);
			// now delete the import folder, not used anymore
			repoentryContainer.delete();
		}
		
		// 3) Set poster image for repo entry
		VFSContainer masterContainer = getMasterContainer(videoResource);
		VFSLeaf posterImage = (VFSLeaf)masterContainer.resolve(FILENAME_POSTER_JPG);
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
	public VideoTranscoding updateVideoTranscoding(VideoTranscoding videoTranscoding) {
		return videoTranscodingDao.updateTranscoding(videoTranscoding);
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
	
	@Override
	public boolean deleteVideoTranscodings(OLATResource videoResource) {
		videoTranscodingDao.deleteVideoTranscodings(videoResource);
		VFSStatus deleteStatus = getTranscodingContainer(videoResource).delete();
		return (deleteStatus == VFSConstants.YES ? true : false);
	}

	@Override
	public List<VideoTranscoding> getVideoTranscodingsPendingAndInProgress() {
		return videoTranscodingDao.getVideoTranscodingsPendingAndInProgress();
	}


}
