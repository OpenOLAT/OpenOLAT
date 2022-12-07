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
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.Logger;
import org.jcodec.api.FrameGrab;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.image.Crop;
import org.olat.core.commons.services.image.ImageService;
import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.model.VFSMetadataImpl;
import org.olat.core.commons.services.video.JCodecHelper;
import org.olat.core.commons.services.video.MovieService;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.httpclient.HttpClientService;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSStatus;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.ResourceEvaluation;
import org.olat.modules.video.VideoFormat;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoMarkers;
import org.olat.modules.video.VideoMeta;
import org.olat.modules.video.VideoMetadataSearchParams;
import org.olat.modules.video.VideoModule;
import org.olat.modules.video.VideoQuestion;
import org.olat.modules.video.VideoQuestions;
import org.olat.modules.video.VideoTranscoding;
import org.olat.modules.video.model.TranscodingCount;
import org.olat.modules.video.model.VideoMarkersImpl;
import org.olat.modules.video.model.VideoMetaImpl;
import org.olat.modules.video.model.VideoQuestionsImpl;
import org.olat.modules.video.spi.youtube.YoutubeProvider;
import org.olat.modules.video.spi.youtube.model.YoutubeMetadata;
import org.olat.modules.video.ui.VideoChapterTableRow;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryImportExport;
import org.olat.repository.RepositoryEntryImportExport.RepositoryEntryImport;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;
import org.quartz.JobKey;
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
	
	private static final Logger log = Tracing.createLoggerFor(VideoManagerImpl.class);
	
	private static final String CR = System.lineSeparator();
	private static final String ENCODING = "utf-8";
	protected static final String DIRNAME_REPOENTRY = "repoentry";
	public static final String FILETYPE_MP4 = "mp4";
	private static final String FILETYPE_JPG = "jpg";
	private static final String FILENAME_POSTER_JPG = "poster.jpg";
	private static final String FILENAME_VIDEO_MP4 = "video.mp4";
	private static final String FILENAME_CHAPTERS_VTT = "chapters.vtt";
	private static final String FILENAME_MARKERS_XML = "markers.xml";
	private static final String FILENAME_QUESTIONS_XML = "questions.xml";
	private static final String FILENAME_VIDEO_METADATA_XML = "video_metadata.xml";
	
	private static final String DIRNAME_MASTER = "master";
	private static final String DIRNAME_QUESTIONS = "qti21";
	
	public static final String TRACK = "track_";

	private static final SimpleDateFormat displayDateFormat = new SimpleDateFormat("HH:mm:ss");
	private static final SimpleDateFormat vttDateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
	
	private final JobKey videoJobKey = new JobKey("videoTranscodingJobDetail", Scheduler.DEFAULT_GROUP);

	@Autowired
	private DB dbInstance;
	@Autowired
	private MovieService movieService;
	@Autowired
	private VideoModule videoModule;
	@Autowired
	private YoutubeProvider youtubeProvider;
	@Autowired 
	private RepositoryManager repositoryManager;
	@Autowired
	private VideoTranscodingDAO videoTranscodingDao;
	@Autowired
	private VideoMetadataDAO videoMetadataDao;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	@Autowired
	private Scheduler scheduler;
	@Autowired
	private ImageService imageHelper;
	@Autowired
	private HttpClientService httpClientService;

	/**
	 * get the configured posterframe
	 */
	@Override
	public VFSLeaf getPosterframe(OLATResource videoResource) {
		return resolveFromMasterContainer(videoResource, FILENAME_POSTER_JPG);
	}

	/**
	 * set a specific VFSLeaf as posterframe in video metadata
	 */
	@Override
	public void setPosterframe(OLATResource videoResource, VFSLeaf posterframe, Identity changedBy){
		VFSContainer masterContainer = getMasterContainer(videoResource);
		VFSLeaf newPoster = VFSManager.resolveOrCreateLeafFromPath(masterContainer, FILENAME_POSTER_JPG);
		VFSManager.copyContent(posterframe, newPoster, true, changedBy);
		
		// Update also repository entry image, use new posterframe
		VFSLeaf posterImage = (VFSLeaf)masterContainer.resolve(FILENAME_POSTER_JPG);
		if (posterImage != null) {
			RepositoryEntry repoEntry = repositoryManager.lookupRepositoryEntry(videoResource, true);
			repositoryManager.setImage(posterImage, repoEntry, changedBy);
		}
	}
	
	/**
	 * Sets the posterframe resize uploadfile. Tries to fit image to dimensions of video.
	 *
	 * @param videoResource the video resource
	 * @param posterframe the newPosterFile
	 * @param changedBy
	 */
	@Override
	public void setPosterframeResizeUploadfile(OLATResource videoResource, VFSLeaf newPosterFile, Identity changedBy) {
		VideoMeta videoMetadata = getVideoMetadata(videoResource);
		Size posterRes = imageHelper.getSize(newPosterFile, FILETYPE_JPG);
		// file size needs to be bigger than target resolution, otherwise use image as it comes
		if (posterRes != null 
				&& posterRes.getHeight() != 0 
				&& posterRes.getWidth() != 0
				&& posterRes.getHeight() >= videoMetadata.getHeight() 
				&& posterRes.getWidth() >= videoMetadata.getWidth()) {
			VFSLeaf oldPosterFile = getPosterframe(videoResource);
			if(oldPosterFile != null) {
				oldPosterFile.delete();
			}
			VFSContainer masterContainer = getMasterContainer(videoResource);
			LocalFileImpl newPoster = (LocalFileImpl) masterContainer.createChildLeaf(FILENAME_POSTER_JPG);
			// to shrink image file, resolution ratio needs to be equal, otherwise crop from top left corner
			if (posterRes.getHeight() / posterRes.getWidth() == videoMetadata.getHeight() / videoMetadata.getWidth()) {
				imageHelper.scaleImage(newPosterFile, newPoster, videoMetadata.getWidth(), videoMetadata.getHeight(), true);
			} else {
				Crop cropSelection = new Crop(0, 0, videoMetadata.getHeight(), videoMetadata.getWidth());
				imageHelper.cropImage(((LocalFileImpl) newPosterFile).getBasefile(), newPoster.getBasefile(), cropSelection);
			}
		} else {
			setPosterframe(videoResource, newPosterFile, changedBy);
		}
	}

	@Override
	public void deletePosterframe(OLATResource videoResource) {
		VFSLeaf oldPosterFile = getPosterframe(videoResource);
		if(oldPosterFile != null) {
			oldPosterFile.delete();
		}
		RepositoryEntry repoEntry = repositoryManager.lookupRepositoryEntry(videoResource, true);
		repositoryManager.deleteImage(repoEntry);
	}

	/**
	 * get a specific subtitle-track of the videoresource
	 */
	@Override
	public VFSLeaf getTrack(OLATResource videoResource, String lang) {
		String vttPath = TRACK + lang + DOT + FILETYPE_VTT;
		VFSLeaf vttLeaf = resolveFromMasterContainer(videoResource, vttPath);
		if (vttLeaf != null) {
			return vttLeaf;
		}
		String srtPath = TRACK + lang + DOT + FILETYPE_SRT;
		VFSLeaf srtLeaf = resolveFromMasterContainer(videoResource, srtPath);
		if (srtLeaf != null) {
			return srtLeaf;
		}
		return null;
	}
	
	/**
	 * remove a specific track from the videoresource
	 */
	@Override
	public void removeTrack(OLATResource videoResource, String lang){
		VFSContainer vfsContainer = getMasterContainer(videoResource);
		for (VFSItem item : vfsContainer.getItems(new TrackFilter())) {
			if (item.getName().contains(lang)) {
				item.delete();
			}
		}
	}
	
	/**
	 * get all tracks saved in the video metadata as map
	 */
	@Override
	public Map<String, VFSLeaf> getAllTracks(OLATResource videoResource) {
		Map<String, VFSLeaf> tracks = new HashMap<>();
		VFSContainer vfsContainer = getMasterContainer(videoResource);
		List<VFSItem> trackItems = vfsContainer.getItems(new TrackFilter());
		for (VFSItem item : trackItems) {
			String itemname = item.getName();
			int indexUnderscore = itemname.indexOf('_');
			int indexPoint = itemname.indexOf('.');
			// Why -1? because -1 + 1 -> 0 and it's allowed
			if(indexUnderscore >= -1 && indexPoint > indexUnderscore) {
				String key = itemname.substring(indexUnderscore + 1, indexPoint);
				tracks.put(key, resolveFromMasterContainer(videoResource, itemname));
			}
		}
		return tracks;
	}
	
	/**
	 * return the chapter file as a VFSLeaf
	 */
	@Override
	public boolean hasChapters(OLATResource videoResource){
		VFSContainer vfsContainer = getMasterContainer(videoResource);
		VFSLeaf webvtt = (VFSLeaf) vfsContainer.resolve(FILENAME_CHAPTERS_VTT);
		return (webvtt != null && webvtt.getSize() > 0);
	}

	/**
	 * write the the given frame at frameNumber in the frame leaf
	 * @param videoResource videoresource
	 * @param frameNumber the frameNumber at which the frame should be taken from
	 * @param frame the VFSLeaf to write the picked image to
	 */
	@Override
	public boolean getFrame(VFSLeaf video, int frameNumber, VFSLeaf frame) {
		File videoFile = ((LocalFileImpl)video).getBasefile();
		
		Size movieSize = movieService.getSize(video, FILETYPE_MP4);

		try (FileChannelWrapper in = NIOUtils.readableChannel(videoFile)) {
			FrameGrab frameGrab = FrameGrab.createFrameGrab(in).seekToFrameSloppy(frameNumber);
			OutputStream frameOutputStream = frame.getOutputStream(false);

			Picture picture = frameGrab.getNativeFrame();
			BufferedImage bufImg = AWTUtil.toBufferedImage(picture);
			bufImg = JCodecHelper.scale(movieSize, picture, bufImg);
			ImageIO.write(bufImg, "JPG", frameOutputStream);

			// close everything to prevent resource leaks
			frameOutputStream.close();

			return true;
		} catch (Exception | AssertionError e) {
			log.error("Could not get frame::{} for video::{}", frameNumber, videoFile.getAbsolutePath(), e);
			return false;
		} 
	}
	
	@Override
	public boolean getFrameWithFilter(VFSLeaf video, Size movieSize, int frameNumber, long duration, VFSLeaf frame) {
		File videoFile = ((LocalFileImpl)video).getBasefile();
		BufferedImage bufImg = null;
		boolean imgBlack = true;
		int countBlack = 0;
		
		try (FileChannelWrapper in = NIOUtils.readableChannel(videoFile)) {
			OutputStream frameOutputStream = frame.getOutputStream(false);
			FrameGrab frameGrab = FrameGrab.createFrameGrab(in).seekToFrameSloppy(frameNumber);

			Picture picture = frameGrab.getNativeFrame();
			bufImg = AWTUtil.toBufferedImage(picture);

			int xmin = bufImg.getMinX();
			int ymin = bufImg.getMinY();
			int xmax = xmin + bufImg.getWidth();
			int ymax = ymin + bufImg.getHeight();
			int pixelCount = bufImg.getWidth() * bufImg.getHeight();

			for (int x = xmin; x < xmax; x++) {
				for (int y = ymin; y < ymax; y++) {
					int rgb = bufImg.getRGB(x, y);
//					int alpha = (0xff000000 & rgb) >>> 24;
					int r = (0x00ff0000 & rgb) >> 16;
					int g = (0x0000ff00 & rgb) >> 8;
					int b = (0x000000ff & rgb);
					if (r < 30 && g < 30 && b < 30) {
						countBlack++;
					}
				}
			}
			if (countBlack > (int) (0.7F * pixelCount)) {
				imgBlack = true;
			} else {
				imgBlack = false;
				bufImg = JCodecHelper.scale(movieSize, picture, bufImg);
				ImageIO.write(bufImg, "JPG", frameOutputStream);
			}
			// avoid endless loop
			if (frameNumber > duration) {
				imgBlack = false;
			} 
			// close everything to prevent resource leaks
			frameOutputStream.close();

			return imgBlack;
		} catch (Exception | AssertionError e) {
			log.error("Could not get frame: {} for video: {}", frameNumber, videoFile.getAbsolutePath(), e);
			return false;
		}
	}

	/**
	 * get the File of the videoresource 
	 */
	@Override
	public File getVideoFile(OLATResource videoResource) {
		VFSContainer masterContainer = getMasterContainer(videoResource);
		VFSItem videoFile = masterContainer.resolve(FILENAME_VIDEO_MP4);
		return (videoFile instanceof LocalFileImpl) ? ((LocalFileImpl)videoFile).getBasefile() : null;
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

	@Override
	public String toPodcastVideoUrl(String url) {
		try {
			int index = url.indexOf("/Pages/Viewer.aspx?");
			if(index >= 0) {
				int idIndex = url.indexOf("id=", index);
				if(idIndex >= 0) {
					String start = url.substring(0, index);
					
					int idEnd = url.indexOf('&', idIndex);
					if(idEnd < 0) {
						idEnd = url.length();
					}
					
					String id = url.substring(idIndex + 3, idEnd);
					url = start + "/Podcast/StreamInBrowser/" + id + ".mp4";
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return url;
	}

	@Override
	public void startTranscodingProcessIfEnabled(OLATResource video) {
		if (videoModule.isTranscodingEnabled()) {
			startTranscodingProcess(video);
		}
	}
	
	@Override
	public VideoTranscoding retranscodeFailedVideoTranscoding(VideoTranscoding videoTranscoding) {
		return videoTranscodingDao.updateTranscodingStatus(videoTranscoding);
	}
	
	@Override
	public void startTranscodingProcess(OLATResource video) {
		List<VideoTranscoding> existingTranscodings = getVideoTranscodings(video);
		VideoMeta videoMetadata = getVideoMetadata(video);
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
				scheduler.triggerJob(videoJobKey);
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
		return videoTranscodingDao.getVideoTranscodings(video);
	}
	
	@Override
	public VideoTranscoding getVideoTranscoding(Long key) {
		return videoTranscodingDao.getVideoTranscoding(key);
	}

	@Override
	public List<VideoTranscoding> getAllVideoTranscodings() {
		return videoTranscodingDao.getAllVideoTranscodings();
	}
	
	@Override 
	public List<TranscodingCount> getAllVideoTranscodingsCount() {
		return videoTranscodingDao.getAllVideoTranscodingsCount();
	}
	
	@Override 
	public List<TranscodingCount> getAllVideoTranscodingsCountSuccess(int errorcode) {
		return videoTranscodingDao.getAllVideoTranscodingsCountSuccess(errorcode);
	}
	
	@Override 
	public List<TranscodingCount> getAllVideoTranscodingsCountFails(int errorcode) {
		return videoTranscodingDao.getAllVideoTranscodingsCountFails(errorcode);
	}
	
	@Override
	public List<VideoTranscoding> getOneVideoResolution(int resolution) {
		return videoTranscodingDao.getOneVideoResolution(resolution);
	}

	@Override
	public String getAspectRatio(int width, int height) {
		DecimalFormat df = new DecimalFormat("#.##");
		df.setRoundingMode(RoundingMode.FLOOR);
		String ratioCalculated = df.format(width / (height + 1.0));
		String ratioString;
		
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
		return (knownResolution ? translator.translate("quality.resolution." + resolution) : resolution + "p");
	}
	
	@Override
	public boolean hasMasterContainer (OLATResource videoResource) {
		VFSContainer baseContainer =  FileResourceManager.getInstance().getFileResourceRootImpl(videoResource);
		VFSItem masterContainer = baseContainer.resolve(DIRNAME_MASTER);
		return masterContainer instanceof VFSContainer && masterContainer.exists();		
	}
	
	@Override
	public VFSContainer getMasterContainer(OLATResource videoResource) {
		VFSContainer baseContainer =  FileResourceManager.getInstance().getFileResourceRootImpl(videoResource);
		return VFSManager.resolveOrCreateContainerFromPath(baseContainer, DIRNAME_MASTER);
	}
	
	@Override
	public VFSContainer getTranscodingContainer(OLATResource videoResource) {
		VFSContainer baseContainer = videoModule.getTranscodingBaseContainer();
		return VFSManager.getOrCreateContainer(baseContainer, String.valueOf(videoResource.getResourceableId()));
	}
	
	
	@Override
	public VFSLeaf getMasterVideoFile(OLATResource videoResource) {
		VFSContainer masterContainer = getMasterContainer(videoResource);
		return (VFSLeaf) masterContainer.resolve(FILENAME_VIDEO_MP4);
	}
	
	@Override
	public VideoExportMediaResource getVideoExportMediaResource(RepositoryEntry repoEntry) {
		OLATResource videoResource = repoEntry.getOlatResource();
		LocalFolderImpl baseContainer= FileResourceManager.getInstance().getFileResourceRootImpl(videoResource);
		// 1) dump repo entry metadata to resource folder
		LocalFolderImpl repoentryContainer = (LocalFolderImpl)VFSManager.resolveOrCreateContainerFromPath(baseContainer, DIRNAME_REPOENTRY); 
		RepositoryEntryImportExport importExport = new RepositoryEntryImportExport(repoEntry, repoentryContainer.getBasefile());
		importExport.exportDoExportProperties();
		// 2) dump video metadata to resource folder
		VideoMeta videoMeta = getVideoMetadata(videoResource);
		if(videoMeta != null) {
			VFSLeaf videoMetaFile = VFSManager.resolveOrCreateLeafFromPath(repoentryContainer, FILENAME_VIDEO_METADATA_XML);
			VideoMetaXStream.toXml(videoMetaFile, videoMeta);
		}
		// 3) package everything in resource folder to streaming zip resource
		return new VideoExportMediaResource(baseContainer, repoEntry.getDisplayname());
	}

	@Override
	public void validateVideoExportArchive(File file,  ResourceEvaluation eval) {
		try(ZipFile zipFile = new ZipFile(file)) {
			ZipEntry repoMetadataEntry = zipFile.getEntry(DIRNAME_REPOENTRY + "/" + RepositoryEntryImportExport.PROPERTIES_FILE);
			RepositoryEntryImport repoMetadata = null;
			if (repoMetadataEntry != null) {
				eval.setValid(true);
				repoMetadata = readMetadata(zipFile, repoMetadataEntry);
				if (repoMetadata != null) {
					eval.setDisplayname(repoMetadata.getDisplayname());
				}
			}
		} catch (Exception e) {
			log.error("Error while checking for video resource archive", e);
		}
	}
	
	private RepositoryEntryImport readMetadata(ZipFile zipFile, ZipEntry entry) {
		try(InputStream repoMetaDataStream = zipFile.getInputStream(entry);) {
			return RepositoryEntryImportExport.getConfiguration(repoMetaDataStream);
		} catch(Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	@Override
	public VideoMeta importFromMasterFile(RepositoryEntry repoEntry, VFSLeaf masterVideo, Identity initialAuthor) {
		OLATResource videoResource = repoEntry.getOlatResource();
		
		// 1) copy master video to final destination with standard name
		VFSContainer masterContainer = getMasterContainer(videoResource);
		VFSLeaf targetFile = VFSManager.resolveOrCreateLeafFromPath(masterContainer, FILENAME_VIDEO_MP4);
		VFSManager.copyContent(masterVideo, targetFile, true, initialAuthor);
		masterVideo.delete();

		// calculate video duration
		long duration = movieService.getDuration(targetFile, FILETYPE_MP4);
		if (duration != -1) {
			repoEntry.setExpenditureOfWork(Formatter.formatTimecode(duration));
		}
		// generate a poster image, use 20th frame as a default
		VFSLeaf posterResource = VFSManager.resolveOrCreateLeafFromPath(masterContainer, FILENAME_POSTER_JPG);
		getFrame(targetFile, 20, posterResource);

		// 2) Set poster image for repo entry
		VFSLeaf posterImage = (VFSLeaf)masterContainer.resolve(FILENAME_POSTER_JPG);
		if (posterImage != null) {
			repositoryManager.setImage(posterImage, repoEntry, initialAuthor);
		}
		
		VideoMeta meta = null;
		if(targetFile != null) {
			createVideoMetadata(repoEntry, targetFile.getSize(), targetFile.getName());
			dbInstance.commit();
			meta = updateVideoMetadata(videoResource, targetFile, initialAuthor);
		}		
		return meta;
	}
	
	@Override
	public VideoMeta importFromExportArchive(RepositoryEntry repoEntry, VFSLeaf exportArchive, Identity initialAuthor) {
		OLATResource videoResource = repoEntry.getOlatResource();
		// 1) unzip archive
		VFSContainer baseContainer= FileResourceManager.getInstance().getFileResourceRootImpl(videoResource);
		ZipUtil.unzip(exportArchive, baseContainer);
		exportArchive.delete();
		
		// 2) update metadata from the repo entry export
		VideoMeta meta = null;
		LocalFolderImpl repoentryContainer = (LocalFolderImpl) baseContainer.resolve(DIRNAME_REPOENTRY); 
		if (repoentryContainer != null) {
			// repo metadata
			RepositoryEntryImportExport importExport = new RepositoryEntryImportExport(repoentryContainer.getBasefile());
			importExport.setRepoEntryPropertiesFromImport(repoEntry);
			// video metadata
			VFSItem videoMetaFile = repoentryContainer.resolve(FILENAME_VIDEO_METADATA_XML); 
			if(videoMetaFile instanceof VFSLeaf) {
				VideoMeta videoMeta = VideoMetaXStream.fromXml((VFSLeaf)videoMetaFile);
				meta = videoMetadataDao.copyVideoMetadata(repoEntry, videoMeta);
			}
			// now delete the import folder, not used anymore
			repoentryContainer.delete();
		}
		
		// 3) Set poster image for repo entry
		VFSContainer masterContainer = getMasterContainer(videoResource);
		VFSLeaf posterImage = (VFSLeaf)masterContainer.resolve(FILENAME_POSTER_JPG);
		if (posterImage != null) {
			repositoryManager.setImage(posterImage, repoEntry, initialAuthor);
		}
		
		dbInstance.commit();

		VFSLeaf videoFile = getMasterVideoFile(videoResource);
		if(videoFile != null) {
			if(meta == null) {
				meta = createVideoMetadata(repoEntry, videoFile.getSize(), videoFile.getName());
				dbInstance.commit();
			} else if(meta.getVideoFormat() == null) {
				meta = checkUnkownVideoFormat(meta);
				dbInstance.commit();
			}
			
			// check if these are default settings
			if(meta != null && meta.getWidth() == 800 && meta.getHeight() == 600) {
				meta = updateVideoMetadata(videoResource, videoFile, initialAuthor);
			}
			
			VFSMetadata vfsMetadata = videoFile.getMetaInfo();
			if(vfsMetadata instanceof VFSMetadataImpl) {
				((VFSMetadataImpl)vfsMetadata).setFileInitializedBy(initialAuthor);
				vfsRepositoryService.updateMetadata(vfsMetadata);
			}
		}
		return meta;
	}
	
	@Override
	public Size getVideoResolutionFromOLATResource (OLATResource videoResource) {
		VFSContainer masterContainer = getMasterContainer(videoResource);
		VFSLeaf targetFile = (VFSLeaf) masterContainer.resolve(FILENAME_VIDEO_MP4);
		Size videoSize = movieService.getSize(targetFile, FILETYPE_MP4);
		if (videoSize == null) {
			videoSize = new Size(800, 600, false);
		}
		return videoSize;
	}
	
	@Override
	public void exchangePoster(OLATResource videoResource, Identity changedBy) {
		VFSContainer masterContainer = getMasterContainer(videoResource);
		VFSLeaf videoFile = VFSManager.resolveOrCreateLeafFromPath(masterContainer, FILENAME_VIDEO_MP4);
		VFSLeaf posterResource = VFSManager.resolveOrCreateLeafFromPath(masterContainer, FILENAME_POSTER_JPG);
		getFrame(videoFile, 20, posterResource);
		// Update also repository entry image, use new posterframe
		VFSLeaf posterImage = (VFSLeaf)masterContainer.resolve(FILENAME_POSTER_JPG);
		if (posterImage != null) {
			RepositoryEntry repoEntry = repositoryManager.lookupRepositoryEntry(videoResource, true);
			repositoryManager.setImage(posterImage, repoEntry, changedBy);
		}
	}
	
	@Override
	public VideoMeta updateVideoMetadata(VideoMeta meta) {
		return videoMetadataDao.updateVideoMetadata(meta);
	}

	@Override
	public VideoMeta updateVideoMetadata(OLATResource videoResource, VFSLeaf uploadVideo, Identity changedBy) {	
		VideoMeta meta = getVideoMetadata(videoResource);

		Size dimensions = movieService.getSize(uploadVideo, VideoManagerImpl.FILETYPE_MP4);
		// update video duration
		long duration = movieService.getDuration(uploadVideo, VideoTranscoding.FORMAT_MP4);

		if (duration != -1) {
			String length = Formatter.formatTimecode(duration);
			meta.setSize(uploadVideo.getSize());
			meta.setWidth(dimensions.getWidth());
			meta.setHeight(dimensions.getHeight());
			
			VideoFormat format = VideoFormat.valueOfFilename(uploadVideo.getName());
			meta.setVideoFormat(format);
			meta.setLength(length);
		}
		return updateVideoMetadata(meta);
	}
	
	@Override
	public VideoMeta checkUnkownVideoFormat(VideoMeta meta) {
		if(meta == null || meta.getVideoResource() == null || StringHelper.containsNonWhitespace(meta.getUrl())) return meta;
		
		VFSLeaf video = getMasterVideoFile(meta.getVideoResource());
		if(video != null) {
			VideoFormat format = VideoFormat.valueOfFilename(video.getName());
			meta.setVideoFormat(format);
			meta = updateVideoMetadata(meta);
		}
		return meta;
	}

	@Override
	public RepositoryEntry updateVideoMetadata(RepositoryEntry entry, Long durationInSeconds, Identity changedBy) {
		if(durationInSeconds == null) return entry;
		
		long durationInMillis = durationInSeconds.longValue() * 1000l;
		
		String durationStr = Formatter.formatTimecode(durationInMillis);
		entry = repositoryManager.setExpenditureOfWork(entry, durationStr);
		
		VideoMeta meta = getVideoMetadata(entry.getOlatResource());
		if(meta != null && meta.getVideoResource() != null) {
			meta.setLength(durationStr);
			videoMetadataDao.updateVideoMetadata(meta);
		}
		return entry;
	}

	@Override
	public RepositoryEntry updateVideoMetadata(RepositoryEntry entry, String url, VideoFormat format, Identity changedBy) {
		OLATResource videoResource = entry.getOlatResource();
		VideoMeta meta = videoMetadataDao.getVideoMetadata(videoResource);
		meta.setUrl(url);
		meta.setVideoFormat(format);
		if(format == VideoFormat.mp4 || format == VideoFormat.panopto) {
			VFSLeaf videoFile = downloadTmpVideo(videoResource, url);
			if(videoFile.exists() && videoFile.getSize() > 0) {
				meta.setSize(videoFile.getSize());
	
				Size dimensions = movieService.getSize(videoFile, "mp4");
				if(dimensions != null) {
					meta.setWidth(dimensions.getWidth());
					meta.setHeight(dimensions.getHeight());
				}
				
				long duration = movieService.getDuration(videoFile, "mp4");
				if(duration > 0) {
					String length = Formatter.formatTimecode(duration);
					meta.setLength(length);
					entry = repositoryManager.setExpenditureOfWork(entry, length);
				}
			} else {
				meta.setSize(0l);
				meta.setWidth(800);
				meta.setHeight(600);
			}
			
			if(videoFile.exists()) {
				videoFile.deleteSilently();
			}
		} else if(format == VideoFormat.youtube && youtubeProvider.isEnabled()) {
			YoutubeMetadata metadata = youtubeProvider.getSnippet(url);
			if(metadata != null) {
				if(StringHelper.containsNonWhitespace(metadata.getThumbnailUrl())) {
					uploadPoster(entry, metadata.getThumbnailUrl(), changedBy);
				}
				
				
				if(metadata.getDuration() > 0) {
					String length = Formatter.formatTimecode(metadata.getDuration());
					meta.setLength(length);
					entry = repositoryManager.setExpenditureOfWork(entry, length);
				}
			}

		} else {
			meta.setSize(0l);
			meta.setWidth(800);
			meta.setHeight(600);
		}
		videoMetadataDao.updateVideoMetadata(meta);
		return entry;
	}
	
	
	private void uploadPoster(RepositoryEntry entry, String url, Identity changedBy) {
		VFSLeaf oldPosterFile = getPosterframe(entry.getOlatResource());
		if(oldPosterFile != null) {
			oldPosterFile.delete();
		}

		VFSContainer masterContainer = getMasterContainer(entry.getOlatResource());
		VFSLeaf posterFile = masterContainer.createChildLeaf(FILENAME_POSTER_JPG);
		
		if(url.contains(" ")) {
			url = url.replace(" ", "%20");
		}
		HttpGet get = new HttpGet(url);
		get.addHeader("Accept", "image/jpg");
		
		try(CloseableHttpClient httpClient = httpClientService.createThreadSafeHttpClient(true);
				CloseableHttpResponse response = httpClient.execute(get)) {
			download(response, posterFile);	
		} catch(Exception e) {
			log.error("", e);
		}
		
		// Update also repository entry image, use new posterframe
		RepositoryEntry repoEntry = repositoryManager.lookupRepositoryEntry(entry.getOlatResource(), true);
		repositoryManager.setImage(posterFile, repoEntry, changedBy);
	}
	
	@Override
	public VFSLeaf downloadTmpVideo(OLATResource videoResource, VideoMeta videoMetadata) {
		return downloadTmpVideo(videoResource, videoMetadata.getUrl());
	}
	
	private VFSLeaf downloadTmpVideo(OLATResource videoResource, String url) {
		VFSContainer baseContainer =  FileResourceManager.getInstance().getFileResourceRootImpl(videoResource);
		VFSContainer tmpContainer = VFSManager.getOrCreateContainer(baseContainer, "download");
		
		VFSItem videoItem = tmpContainer.resolve(FILENAME_VIDEO_MP4);
		if(videoItem != null) {
			videoItem.deleteSilently();
		}
		VFSLeaf videoFile = tmpContainer.createChildLeaf(FILENAME_VIDEO_MP4);
		
		if(url.contains(" ")) {
			url = url.replace(" ", "%20");
		}
		HttpGet get = new HttpGet(url);
		get.addHeader("Accept", "video/mp4");
		
		try(CloseableHttpClient httpClient = httpClientService.createThreadSafeHttpClient(true);
				CloseableHttpResponse response = httpClient.execute(get)) {
			download(response, videoFile);	
		} catch(Exception e) {
			log.error("", e);
		}
		// make sure that a metadata is created
		vfsRepositoryService.getMetadataFor(videoFile);
		return videoFile;
	}

	private void download(CloseableHttpResponse response, VFSLeaf file) {
		try(InputStream in=response.getEntity().getContent();
				OutputStream out=file.getOutputStream(false)) {
			FileUtils.copy(in, out);
		} catch(Exception e) {
			log.error("", e);
		}	
	}

	@Override
	public VideoTranscoding updateVideoTranscoding(VideoTranscoding videoTranscoding) {
		return videoTranscodingDao.updateTranscoding(videoTranscoding);
	}

	@Override
	public void copyVideo(RepositoryEntry sourceEntry, RepositoryEntry targetEntry) {
		OLATResource sourceResource = sourceEntry.getOlatResource();
		OLATResource targetResource = targetEntry.getOlatResource();
		// 1) Copy files on disk
		File sourceFileroot = FileResourceManager.getInstance().getFileResourceRootImpl(sourceResource).getBasefile();
		File targetFileroot = FileResourceManager.getInstance().getFileResourceRootImpl(targetResource).getBasefile();
		FileUtils.copyDirContentsToDir(sourceFileroot, targetFileroot, false, "copyVideoResource");
		// 2) Copy metadata
		VideoMetaImpl sourceMeta = getVideoMetadata(sourceResource);
		if(sourceMeta != null) {
			sourceMeta = videoMetadataDao.copyVideoMetadata(targetEntry, sourceMeta);
		}
		// 3) Trigger transcoding in background
		if (videoModule.isTranscodingEnabled() && sourceMeta != null && !StringHelper.containsNonWhitespace(sourceMeta.getUrl())) {
			startTranscodingProcess(targetResource);
		}
	}
	
	@Override
	public boolean deleteVideoTranscodings(OLATResource videoResource) {
		videoTranscodingDao.deleteVideoTranscodings(videoResource);
		VFSContainer container = getTranscodingContainer(videoResource);
		VFSStatus deleteStatus;
		if(container == null) {
			deleteStatus = VFSConstants.YES;
		} else {
			deleteStatus = container.delete();
		}
		return deleteStatus == VFSConstants.YES;
	}
	
	@Override
	public boolean deleteVideoMetadata(OLATResource videoResource) {
		int deleted = videoMetadataDao.deleteVideoMetadata(videoResource);
		return 0 < deleted;
	}

	@Override
	public List<VideoTranscoding> getVideoTranscodingsPendingAndInProgress() {
		return videoTranscodingDao.getVideoTranscodingsPendingAndInProgress();
	}
	
	@Override
	public List<VideoTranscoding> getFailedVideoTranscodings() {
		return videoTranscodingDao.getFailedVideoTranscodings();
	}
	
	@Override
	public void deleteVideoTranscoding(VideoTranscoding videoTranscoding) {
		videoTranscodingDao.deleteVideoTranscoding(videoTranscoding);
		VFSContainer container = getTranscodingContainer(videoTranscoding.getVideoResource());
		VFSLeaf videoFile = (VFSLeaf) container.resolve(videoTranscoding.getResolution() + FILENAME_VIDEO_MP4);
		if( videoFile != null ) {
			videoFile.delete();
		}
	}

	@Override
	public List<Integer> getMissingTranscodings(OLATResource videoResource){
		//get resolutions which are turned on in the videomodule
		int[] configuredResolutions = videoModule.getTranscodingResolutions();
		//turn the int[]-Array into a List
		List<Integer> configResList = IntStream.of(configuredResolutions).boxed().collect(Collectors.toList());
		List<VideoTranscoding> videoTranscodings = getVideoTranscodings(videoResource);

		for(VideoTranscoding videoTranscoding:videoTranscodings){
			Integer resolution = videoTranscoding.getResolution();
			configResList.remove(resolution);
		}
		
		return configResList;
	}
	
	@Override
	public VideoTranscoding createTranscoding(OLATResource video, int resolution,String format) {
		return videoTranscodingDao.createVideoTranscoding(video, resolution, format);
	}
	
	@Override
	public void saveChapters (List<VideoChapterTableRow> chapters, OLATResource videoResource){
		displayDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		vttDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		VFSContainer vfsContainer = getMasterContainer(videoResource);
		VFSLeaf webvtt = (VFSLeaf) vfsContainer.resolve(FILENAME_CHAPTERS_VTT);		
		if (webvtt == null) {
			webvtt = vfsContainer.createChildLeaf(FILENAME_CHAPTERS_VTT);
		}		

		if (chapters.isEmpty()){
			webvtt.delete();
			return;
		}

		StringBuilder vttString = new StringBuilder("WEBVTT").append(CR);
		for (int i = 0; i < chapters.size(); i++) {
			vttString.append(CR).append("Chapter "+ (i+1)).append(CR);
			vttString.append(vttDateFormat.format(chapters.get(i).getBegin()));
			vttString.append(" --> ");
			vttString.append(vttDateFormat.format(chapters.get(i).getEnd())).append(CR);
			vttString.append(chapters.get(i).getChapterName().replaceAll(CR, " "));
			vttString.append(CR);
		}

		try(OutputStream bos = new BufferedOutputStream(webvtt.getOutputStream(false))) {
			FileUtils.save(bos, vttString.toString(), ENCODING);
		} catch (IOException e) {
			log.error("chapter.vtt could not be saved for videoResource::{}", videoResource, e);
		}
	}
	
	/**
	 * reads an existing webvtt file to provide for display and to further process.
	 *
	 * @param List<VideoChapterTableRow> chapters the chapters
	 * @param OLATResource videoResource the video resource
	 */
	@Override
	public List<VideoChapterTableRow> loadChapters(OLATResource videoResource) {
		List<VideoChapterTableRow> chapters = new ArrayList<>();
		displayDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		vttDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		VFSContainer vfsContainer = getMasterContainer(videoResource);
		VFSLeaf webvtt = (VFSLeaf) vfsContainer.resolve(FILENAME_CHAPTERS_VTT);

		if (webvtt != null && webvtt.exists()) {
			try(BufferedReader webvttReader = new BufferedReader(new InputStreamReader(webvtt.getInputStream()))) {
				String thisLine;
				String regex = " --> ";
				
				while ((thisLine = webvttReader.readLine()) != null) {
					if (thisLine.contains(regex)) {
						String[] interval = thisLine.split(regex);
						Date begin = vttDateFormat.parse(interval[0]);
						Date end = vttDateFormat.parse(interval[1]);

						StringBuilder chapterTitle = new StringBuilder();
						String title;
						
						while ((title = webvttReader.readLine()) != null) {
							if (title.isEmpty() || title.contains(regex))
								break;
							chapterTitle.append(title).append(CR);
						}
						chapters.add(new VideoChapterTableRow(chapterTitle.toString().replaceAll(CR, " "),
								displayDateFormat.format(begin), begin, end));
					}
				}
			} catch (Exception e) {
				log.error("Unable to load WEBVTT File for resource::" + videoResource,e);
			}
		}
		return chapters;
	}
	
	@Override
	public VideoMarkers loadMarkers(OLATResource videoResource) {
		VFSContainer vfsContainer = getMasterContainer(videoResource);
		VFSItem markersItem = vfsContainer.resolve(FILENAME_MARKERS_XML);
		if(markersItem instanceof VFSLeaf) {
			VFSLeaf markersLeaf = (VFSLeaf)markersItem;
			try(InputStream in=markersLeaf.getInputStream()) {
				return VideoXStream.fromXml(in, VideoMarkers.class);
			} catch(IOException e) {
				log.error("", e);
			}
		}
		return new VideoMarkersImpl();
	}

	@Override
	public void saveMarkers(VideoMarkers markers, OLATResource videoResource) {
		VFSContainer vfsContainer = getMasterContainer(videoResource);
		VFSItem markersItem = vfsContainer.resolve(FILENAME_MARKERS_XML);
		if(markersItem == null) {
			markersItem = vfsContainer.createChildLeaf(FILENAME_MARKERS_XML);
		}
		if(markersItem instanceof VFSLeaf) {
			VFSLeaf markersLeaf = (VFSLeaf)markersItem;
			try(OutputStream out=markersLeaf.getOutputStream(false)) {
				VideoXStream.toXml(out, markers);
			} catch(IOException e) {
				log.error("", e);
			}
		}
	}
	
	@Override
	public VideoQuestions loadQuestions(OLATResource videoResource) {
		VFSContainer vfsContainer = getMasterContainer(videoResource);
		VFSItem questionsItem = vfsContainer.resolve(FILENAME_QUESTIONS_XML);
		if(questionsItem instanceof VFSLeaf) {
			VFSLeaf questionsLeaf = (VFSLeaf)questionsItem;
			try(InputStream in=questionsLeaf.getInputStream()) {
				return VideoXStream.fromXml(in, VideoQuestions.class);
			} catch(IOException e) {
				log.error("", e);
			}
		}
		return new VideoQuestionsImpl();
	}

	@Override
	public void saveQuestions(VideoQuestions questions, OLATResource videoResource) {
		VFSContainer vfsContainer = getMasterContainer(videoResource);
		VFSItem questionsItem = vfsContainer.resolve(FILENAME_QUESTIONS_XML);
		if(questionsItem == null) {
			questionsItem = vfsContainer.createChildLeaf(FILENAME_QUESTIONS_XML);
		}
		if(questionsItem instanceof VFSLeaf) {
			VFSLeaf questionsLeaf = (VFSLeaf)questionsItem;
			try(OutputStream out=questionsLeaf.getOutputStream(false)) {
				VideoXStream.toXml(out, questions);
			} catch(IOException e) {
				log.error("", e);
			}
		}
	}

	@Override
	public File getAssessmentDirectory(OLATResource videoResource) {
		File baseDir =  FileResourceManager.getInstance().getFileResourceRoot(videoResource);
		File assessmentDir = new File(baseDir, DIRNAME_QUESTIONS);
		if(!assessmentDir.exists()) {
			assessmentDir.mkdirs();
		}
		return assessmentDir;
	}
	
	@Override
	public File getQuestionDirectory(OLATResource videoResource, VideoQuestion question) {
		File baseDir = getAssessmentDirectory(videoResource);
		File questionDir = new File(baseDir, question.getQuestionRootPath());
		if(!questionDir.exists()) {
			questionDir.mkdirs();
		}
		return questionDir;
	}

	@Override
	public VFSContainer getQuestionContainer(OLATResource videoResource, VideoQuestion question) {
		VFSContainer videoContainer = FileResourceManager.getInstance().getFileResourceRootImpl(videoResource);
		VFSItem item = videoContainer.resolve(DIRNAME_QUESTIONS);
		if(item == null) {
			item = videoContainer.createChildContainer(DIRNAME_QUESTIONS);
		}
		
		if(item instanceof VFSContainer) {
			VFSContainer container = (VFSContainer)item;
			VFSItem questionContainer = container.resolve(question.getQuestionRootPath());
			if(questionContainer == null) {
				questionContainer = container.createChildContainer(question.getQuestionRootPath());
			}
			
			if(questionContainer instanceof VFSContainer) {
				return (VFSContainer)questionContainer;
			}
		}
		return null;
	}
	
	@Override
	public long getVideoFrameCount(VFSLeaf video) {
		return movieService.getFrameCount(video, FILETYPE_MP4);
	}

	@Override
	public long getVideoDuration(OLATResource videoResource){
		VFSContainer masterContainer = getMasterContainer(videoResource);
		VFSLeaf video = (VFSLeaf)masterContainer.resolve(FILENAME_VIDEO_MP4);	
		return movieService.getDuration(video, FILETYPE_MP4);
	}

	@Override
	public List<VideoMeta> getVideoMetadata(VideoMetadataSearchParams searchParams) {
		return videoMetadataDao.getVideoMetadata(searchParams);
	}
	
	@Override
	public boolean hasVideoMetadata(OLATResource videoResource) {
		return videoMetadataDao.getVideoMetadata(videoResource) != null;
	}
	
	@Override
	public VideoMetaImpl getVideoMetadata(OLATResource videoResource) {
		VideoMetaImpl meta = videoMetadataDao.getVideoMetadata(videoResource);
		if (meta == null) {
			return new VideoMetaImpl(800, 600, 5000);
		}
		return meta;
	}
	
	@Override 
	public VideoMeta createVideoMetadata(RepositoryEntry repoEntry, long size, String fileName) {
		VideoFormat format = VideoFormat.valueOfFilename(fileName);
		return videoMetadataDao.createVideoMetadata(repoEntry, size, null, format); 
	}
	
	@Override
	public VideoMeta createVideoMetadata(RepositoryEntry repoEntry, String url, VideoFormat format) {
		return videoMetadataDao.createVideoMetadata(repoEntry, -1l, url, format); 
	}

	@Override
	public List<RepositoryEntry> getAllVideoRepoEntries(String typename) {
		return videoMetadataDao.getAllVideoRepoEntries(typename);
	}

	@Override
	public boolean hasVideoFile(OLATResource videoResource) {
		VFSContainer masterContainer = getMasterContainer(videoResource);
		LocalFileImpl videoFile = (LocalFileImpl) masterContainer.resolve(FILENAME_VIDEO_MP4);	
		return videoFile != null && videoFile.exists();
	}
	
	private static class TrackFilter implements VFSItemFilter {
		@Override
		public boolean accept(VFSItem vfsItem) {
			if(vfsItem instanceof VFSLeaf) {
				String name = vfsItem.getName();
				String suffix = FileUtils.getFileSuffix(name);
				return !name.startsWith(".")
						&& (VideoManager.FILETYPE_SRT.equals(suffix) || VideoManager.FILETYPE_VTT.equals(suffix));
			}
			return false;
		}
	}
}
