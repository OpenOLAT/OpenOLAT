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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.jcodec.api.FrameGrab;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;
import org.json.JSONObject;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.image.Crop;
import org.olat.core.commons.services.image.ImageService;
import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.VFSTranscodingService;
import org.olat.core.commons.services.vfs.model.VFSMetadataImpl;
import org.olat.core.commons.services.video.JCodecHelper;
import org.olat.core.commons.services.video.MovieService;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.httpclient.HttpClientService;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSSuccess;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.VideoTaskCourseNode;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.ResourceEvaluation;
import org.olat.modules.audiovideorecording.AVModule;
import org.olat.modules.video.VideoAssessmentService;
import org.olat.modules.video.VideoComment;
import org.olat.modules.video.VideoComments;
import org.olat.modules.video.VideoFormat;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoMarker;
import org.olat.modules.video.VideoMarkers;
import org.olat.modules.video.VideoMeta;
import org.olat.modules.video.VideoMetadataSearchParams;
import org.olat.modules.video.VideoModule;
import org.olat.modules.video.VideoQuestion;
import org.olat.modules.video.VideoQuestions;
import org.olat.modules.video.VideoSegmentCategory;
import org.olat.modules.video.VideoSegments;
import org.olat.modules.video.VideoToOrganisation;
import org.olat.modules.video.VideoTranscoding;
import org.olat.modules.video.model.SearchVideoInCollectionParams;
import org.olat.modules.video.model.TranscodingCount;
import org.olat.modules.video.model.VideoCommentsImpl;
import org.olat.modules.video.model.VideoMarkersImpl;
import org.olat.modules.video.model.VideoMetaImpl;
import org.olat.modules.video.model.VideoQuestionsImpl;
import org.olat.modules.video.model.VideoSegmentsImpl;
import org.olat.modules.video.spi.youtube.YoutubeProvider;
import org.olat.modules.video.spi.youtube.model.YoutubeMetadata;
import org.olat.modules.video.ui.VideoChapterTableRow;
import org.olat.modules.video.ui.VideoHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryDataDeletable;
import org.olat.repository.RepositoryEntryImportExport;
import org.olat.repository.RepositoryEntryImportExport.RepositoryEntryImport;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryManager;
import org.olat.repository.manager.RepositoryEntryDAO;
import org.olat.resource.OLATResource;
import org.olat.resource.references.Reference;
import org.olat.resource.references.ReferenceManager;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.io.Files;

/**
 * Manager for Videoressource
 * 
 * @author dfurrer, dirk.furrer@frentix.com, http://www.frentix.com
 *
 */
@Service("videoManager")
public class VideoManagerImpl implements VideoManager, RepositoryEntryDataDeletable {
	
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
	private static final String FILENAME_SEGMENTS_XML = "segments.xml";
	private static final String FILENAME_COMMENTS_XML = "comments.xml";
	private static final String FILENAME_QUESTIONS_XML = "questions.xml";
	private static final String FILENAME_VIDEO_METADATA_XML = "video_metadata.xml";
	private static final String FILENAME_THUMBNAIL_PREFIX = "thumbnail_";

	private static final String DIRNAME_MASTER = "master";
	private static final String DIRNAME_QUESTIONS = "qti21";
	private static final String DIRNAME_THUMBNAILS = "thumbnails";
	private static final String DIRNAME_COMMENT_MEDIA = "commentmedia";
	
	public static final String TRACK = "track_";

	private static final String YOUTUBE_OEMBED_URL = "https://www.youtube.com/oembed?format=json&url=";
	private static final String VIMEO_OEMBED_URL =  "https://vimeo.com/api/oembed.json?url=";
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
	private RepositoryEntryDAO repositoryEntryDao;
	@Autowired
	private VideoTranscodingDAO videoTranscodingDao;
	@Autowired
	private VideoCollectionQuery videoCollectionQuery;
	@Autowired
	private VideoMetadataDAO videoMetadataDao;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	@Autowired
	private VideoToOrganisationDAO videoToOrganisationDao;
	@Autowired
	private Scheduler scheduler;
	@Autowired
	private ImageService imageHelper;
	@Autowired
	private HttpClientService httpClientService;
	@Autowired
	private ReferenceManager referenceManager;
	@Autowired
	private VideoAssessmentService videoAssessmentService;
	@Autowired
	private AVModule avModule;

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
	 * @param newPosterFile the newPosterFile
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
				oldPosterFile.deleteSilently();
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
			oldPosterFile.deleteSilently();
		}
		RepositoryEntry repoEntry = repositoryManager.lookupRepositoryEntry(videoResource, true);
		repositoryManager.deleteImage(repoEntry);
	}
	
	@Override
	public List<Organisation> getVideoOrganisations(RepositoryEntryRef re) {
		return videoToOrganisationDao.getOrganisations(re);
	}

	@Override
	public RepositoryEntry setVideoCollection(RepositoryEntry re, boolean videoCollection, List<Organisation> organisations) {
		RepositoryEntry reloadedRe = repositoryEntryDao.loadForUpdate(re);
		if(reloadedRe == null) {
			return null;
		}
		reloadedRe.setVideoCollection(videoCollection);
		RepositoryEntry updatedRe = repositoryEntryDao.updateAndCommit(reloadedRe);
		List<VideoToOrganisation> relations = videoToOrganisationDao.getVideoToOrganisation(updatedRe);
		Map<Organisation,VideoToOrganisation> relationsMap = relations.stream()
				.collect(Collectors.toMap(VideoToOrganisation::getOrganisation, rel -> rel, (u, v) -> u));
		
		List<VideoToOrganisation> relationsToDelete = new ArrayList<>(relations);
		if(videoCollection) {
			// Only add if in collection, if not delete all relations to organisations
			for(Organisation organisation:organisations) {
				VideoToOrganisation relation = relationsMap.get(organisation);
				if(relation == null) {
					videoToOrganisationDao.createVideoToOrganisation(updatedRe, organisation);
				} else {
					relationsToDelete.remove(relation);
				}
			}
		}
		
		for(VideoToOrganisation relationToDelete:relationsToDelete) {
			videoToOrganisationDao.deleteVideoToOrganisation(relationToDelete);
		}
		dbInstance.commit();
		return updatedRe;
	}

	@Override
	public int countVideosInCollection(SearchVideoInCollectionParams params) {
		return videoCollectionQuery.countVideos(params);
	}

	@Override
	public List<RepositoryEntry> getVideosInCollection(SearchVideoInCollectionParams params, int firstResult, int maxResults) {
		return videoCollectionQuery.searchVideos(params, firstResult, maxResults);
	}

	@Override
	public boolean deleteRepositoryEntryData(RepositoryEntry re) {
		List<VideoToOrganisation> relations = videoToOrganisationDao.getVideoToOrganisation(re);
		if(relations != null && !relations.isEmpty()) {
			for(VideoToOrganisation relation:relations) {
				videoToOrganisationDao.deleteVideoToOrganisation(relation);
			}
		}
		return true;
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
	 * remove a specific language track from the videoresource
	 */
	@Override
	public void removeTrack(OLATResource videoResource, String lang){
		VFSContainer vfsContainer = getMasterContainer(videoResource);
		for (VFSItem item : vfsContainer.getItems(new TrackFilter())) {
			if (item.getName().contains(lang)) {
				item.deleteSilently();
			}
		}
	}
	
	/**
	 * get all language tracks saved in the video metadata as map 
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
	 * @param video the source video
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

	@Override
	public boolean isInUse(RepositoryEntry videoEntry) {
		if (videoEntry == null) {
			return false;
		}
		List<Reference> references = referenceManager.getReferencesTo(videoEntry.getOlatResource());
		for (Reference reference : references) {
			if ("CourseModule".equals(reference.getSource().getResourceableTypeName())) {
				ICourse course = CourseFactory.loadCourse(reference.getSource().getResourceableId());
				RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
				String courseNodeSubIdent = reference.getUserdata();
				CourseNode courseNode = course.getEditorTreeModel().getCourseNode(courseNodeSubIdent);
				if (courseNode instanceof VideoTaskCourseNode) {
					long nbTaskSessions = videoAssessmentService.countTaskSessions(courseEntry, courseNodeSubIdent);
					if (nbTaskSessions > 0) {
						return true;
					}
				}
			}
		}

		return false;
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
	public String getHandBrakeCliExecutable() {
		return avModule.getHandBrakeCliCommandPath();
	}

	@Override
	public String getImportInfoString(Locale locale) {
		if (!avModule.isOptimizeMemoryForVideos()) {
			return null;
		}
		Translator translator = Util.createPackageTranslator(VideoHelper.class, locale);
		return translator.translate("admin.config.master.video.file.optimization.upload.warning");
	}

	@Override
	public long numberOfVideoMasterFilesReadyForOptimization() {
		List<VideoMeta> videoMetas = getAllMp4VideoMetadata();
		Map<OLATResource, List<VideoTranscoding>> transcodingsByResource = getVideoTranscodingsByResource(videoMetas);

		return videoMetas.stream()
				.filter(m -> getTranscodingForOptimization(m, transcodingsByResource.get(m.getVideoResource())) != null)
				.count();
	}

	private Map<OLATResource, List<VideoTranscoding>> getVideoTranscodingsByResource(List<VideoMeta> videoMetas) {
		List<OLATResource> olatResources = videoMetas.stream().map(VideoMeta::getVideoResource).toList();
		Map<OLATResource, List<VideoTranscoding>> result = olatResources.stream()
				.collect(Collectors.toMap(Function.identity(), olatResource -> new ArrayList<>()));

		List<VideoTranscoding> videoTranscodings = getAllVideoTranscodings();
		for (VideoTranscoding videoTranscoding : videoTranscodings) {
			if (result.containsKey(videoTranscoding.getVideoResource())) {
				result.get(videoTranscoding.getVideoResource()).add(videoTranscoding);
			}
		}
		return result;
	}

	private VideoTranscoding getTranscodingForOptimization(VideoMeta videoMeta, List<VideoTranscoding> transcodings) {
		if (transcodings == null || transcodings.isEmpty()) {
			log.debug("No transcodings for video {}", videoMeta.getVideoResource().getResourceableId());
			return null;
		}

		if (transcodings.stream().anyMatch(vt -> vt.getStatus() != VideoTranscoding.TRANSCODING_STATUS_DONE)) {
			log.debug("Waiting to optimize: transcoding still in progress or failed for video {}",
					videoMeta.getVideoResource().getResourceableId());
			return null;
		}
		VideoTranscoding transcodingWithSameResolution = transcodings.stream()
				.filter(vt -> videoMeta.getWidth() == vt.getWidth() && videoMeta.getHeight() == vt.getHeight())
				.findFirst().orElse(null);
		if (transcodingWithSameResolution == null) {
			log.debug("No transcoding for optimization with the required resolution {} found for video {}",
					videoMeta.getHeight(), videoMeta.getVideoResource().getResourceableId());

			return null;
		}
		if (transcodingWithSameResolution.getSize() > videoMeta.getSize()) {
			log.debug("No optimization using transcoding for video {} because size would not improve",
					videoMeta.getVideoResource().getResourceableId());
			return null;
		}

		log.debug("Transcoding found for video {} with resolution {}",
				videoMeta.getVideoResource().getResourceableId(), transcodingWithSameResolution.getResolution());

		return transcodingWithSameResolution;
	}

	@Override
	public void optimizeMemoryForVideo(OLATResource videoResource) {
		if (!avModule.isOptimizeMemoryForVideos()) {
			log.debug("Optimization of videos disabled for video {}", videoResource.getResourceableId());
			return;
		}

		VideoMeta videoMeta = getVideoMetadata(videoResource);
		List<VideoTranscoding> videoTranscodings = getVideoTranscodings(videoResource);
		VideoTranscoding transcodingForOptimization = getTranscodingForOptimization(videoMeta, videoTranscodings);
		if (transcodingForOptimization == null) {
			return;
		}
		replaceMasterVideoWithTranscoding(videoResource, transcodingForOptimization);
	}

	private void replaceMasterVideoWithTranscoding(OLATResource videoResource,
												   VideoTranscoding transcodingForOptimization) {
		VFSLeaf transcodingLeaf = getTranscodingLeaf(transcodingForOptimization);
		if (transcodingLeaf == null) {
			log.error("Could not find transcoding file {} for video {}", transcodingForOptimization.getResolution(),
					videoResource.getResourceableId());
			return;
		}
		Long transcodingSize = transcodingLeaf.getSize();
		VFSLeaf masterLeaf = getMasterVideoFile(videoResource);
		Long masterSize = masterLeaf.getSize();

		if (masterLeaf instanceof LocalFileImpl masterFile && transcodingLeaf instanceof LocalFileImpl transcodingFile) {
			masterFile.getBasefile().delete();
			try {
				Files.move(transcodingFile.getBasefile(), masterFile.getBasefile());
				updateVideoMetadata(videoResource, transcodingForOptimization);
				updateMetadata(masterLeaf, transcodingForOptimization);
				videoTranscodingDao.deleteVideoTranscoding(transcodingForOptimization);
				if (log.isDebugEnabled()) {
					log.debug("Replaced master video (size={}) with transcoded video (size={}) for video {}",
							masterSize, transcodingSize, videoResource.getResourceableId());
				}
			} catch (IOException e) {
				log.error("Could not replace master with transcoded file for video {}",
						videoResource.getResourceableId());
			}
		}
	}

	@Override
	public void optimizeMemoryForVideos() {
		if (!avModule.isOptimizeMemoryForVideos()) {
			log.debug("Optimization of videos disabled");
			return;
		}

		List<VideoMeta> videoMetas = getAllMp4VideoMetadata();
		Map<OLATResource, List<VideoTranscoding>> transcodingsByResource = getVideoTranscodingsByResource(videoMetas);

		for (VideoMeta videoMeta : videoMetas) {
			VideoTranscoding transcodingForOptimization = getTranscodingForOptimization(videoMeta,
					transcodingsByResource.get(videoMeta.getVideoResource()));
			if (transcodingForOptimization == null) {
				continue;
			}
			replaceMasterVideoWithTranscoding(videoMeta.getVideoResource(), transcodingForOptimization);
		}
	}

	private void updateMetadata(VFSLeaf leaf, VideoTranscoding videoTranscoding) {
		VFSMetadata metadata = leaf.getMetaInfo();
		if (metadata instanceof VFSMetadataImpl metadataImpl) {
			metadataImpl.setFileSize(videoTranscoding.getSize());
			vfsRepositoryService.updateMetadata(metadata);
		}
	}

	private void updateVideoMetadata(OLATResource videoResource, VideoTranscoding videoTranscoding) {
		VideoMeta videoMeta = getVideoMetadata(videoResource);
		videoMeta.setSize(videoTranscoding.getSize());
		videoMeta.setWidth(videoTranscoding.getWidth());
		videoMeta.setHeight(videoTranscoding.getHeight());
		updateVideoMetadata(videoMeta);
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
			dbInstance.commitAndCloseSession();
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
	public VFSContainer getThumbnailsContainer(OLATResource videoResource) {
		VFSContainer baseContainer = FileResourceManager.getInstance().getFileResourceRootImpl(videoResource);
		return VFSManager.resolveOrCreateContainerFromPath(baseContainer, DIRNAME_THUMBNAILS);
	}

	@Override
	public void deleteThumbnails(OLATResource videoResource) {
		VFSContainer thumbnailsContainer = getThumbnailsContainer(videoResource);
		if (thumbnailsContainer != null) {
			thumbnailsContainer.deleteSilently();
		}
	}

	@Override
	public VFSContainer getCommentMediaContainer(OLATResource videoResource) {
		VFSContainer baseContainer = FileResourceManager.getInstance().getFileResourceRootImpl(videoResource);
		return VFSManager.resolveOrCreateContainerFromPath(baseContainer, DIRNAME_COMMENT_MEDIA);
	}

	@Override
	public VFSContainer getTranscodingContainer(OLATResource videoResource) {
		VFSContainer baseContainer = videoModule.getTranscodingBaseContainer();
		if(baseContainer == null) {
			return null;
		}
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
		masterVideo.deleteSilently();

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
		exportArchive.deleteSilently();
		
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
			repoentryContainer.deleteSilently();
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
		} else if(format == VideoFormat.youtube) {
			if (youtubeProvider.isEnabled()) {
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
				meta.setLength(null);
				entry = repositoryManager.setExpenditureOfWork(entry, null);
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
			oldPosterFile.deleteSilently();
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
		VFSSuccess deleteStatus;
		if(container == null) {
			deleteStatus = VFSSuccess.SUCCESS;
		} else {
			deleteStatus = container.deleteSilently();
		}
		return deleteStatus == VFSSuccess.SUCCESS;
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
		VFSLeaf videoFile = getTranscodingLeaf(videoTranscoding);
		if( videoFile != null ) {
			videoFile.deleteSilently();
		}
	}

	private VFSLeaf getTranscodingLeaf(VideoTranscoding videoTranscoding) {
		return (VFSLeaf) getTranscodingContainer(videoTranscoding.getVideoResource()).resolve(getTranscodingPath(videoTranscoding));
	}

	private String getTranscodingPath(VideoTranscoding videoTranscoding) {
		return videoTranscoding.getResolution() + FILENAME_VIDEO_MP4;
	}

	@Override
	public List<Integer> getMissingTranscodings(OLATResource videoResource, boolean checkForOptimization){
		//get resolutions which are turned on in the videomodule
		int[] configuredResolutions = videoModule.getTranscodingResolutions();
		//turn the int[]-Array into a List
		List<Integer> configResList = IntStream.of(configuredResolutions).boxed().collect(Collectors.toList());
		List<VideoTranscoding> videoTranscodings = getVideoTranscodings(videoResource);

		for(VideoTranscoding videoTranscoding:videoTranscodings){
			Integer resolution = videoTranscoding.getResolution();
			configResList.remove(resolution);
		}

		if (checkForOptimization && avModule.isOptimizeMemoryForVideos()) {
			boolean allDone = videoTranscodings.stream().allMatch(t -> t.getStatus() == VideoTranscoding.TRANSCODING_STATUS_DONE);
			if (allDone) {
				VideoMeta videoMeta = getVideoMetadata(videoResource);
				Integer resolution = videoMeta.getHeight();
				configResList.remove(resolution);
			}
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
			webvtt.deleteSilently();
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
	 * @param videoResource the video resource
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
				return replaceMissingStyles(VideoXStream.fromXml(in, VideoMarkers.class));
			} catch(IOException e) {
				log.error("", e);
			}
		}
		return new VideoMarkersImpl();
	}

	private VideoMarkers replaceMissingStyles(VideoMarkers markers) {
		for (VideoMarker marker : markers.getMarkers()) {
			marker.setStyle(replaceMissingColor(marker.getStyle()));
		}
		return markers;
	}

	private String replaceMissingColor(String color) {
		if (color == null) {
			return null;
		}
		if (color.endsWith("_gray") && !color.endsWith("_lightgray")) {
			return color.replace("_gray", "_lightgray");
		} else if (color.endsWith("_blue") && !color.endsWith("_lightblue")) {
			return color.replace("_blue", "_lightblue");
		} else if (color.endsWith("_green") && !color.endsWith("_lightgreen")) {
			return color.replace("_green", "_lightgreen");
		}
		return color;
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
	public VideoSegments loadSegments(OLATResource olatResource) {
		VFSContainer vfsContainer = getMasterContainer(olatResource);
		VFSItem segmentsItem = vfsContainer.resolve(FILENAME_SEGMENTS_XML);
		if (segmentsItem instanceof VFSLeaf segmentsLeaf) {
			try (InputStream in = segmentsLeaf.getInputStream()) {
				return replaceMissingColorsAndStandardize(VideoXStream.fromXml(in, VideoSegments.class));
			} catch (IOException e) {
				log.error("", e);
			}
		}
		return new VideoSegmentsImpl();
	}

	private VideoSegments replaceMissingColorsAndStandardize(VideoSegments segments) {
		for (VideoSegmentCategory category : segments.getCategories()) {
			category.setColor(VideoModule.getColorFromMarkerStyle(replaceMissingColor(category.getColor())));
		}
		return segments;
	}

	@Override
	public void saveSegments(VideoSegments segments, OLATResource olatResource) {
		VFSContainer vfsContainer = getMasterContainer(olatResource);
		VFSItem segmentsItem = vfsContainer.resolve(FILENAME_SEGMENTS_XML);
		if (segmentsItem == null) {
			segmentsItem = vfsContainer.createChildLeaf(FILENAME_SEGMENTS_XML);
		}
		if (segmentsItem instanceof VFSLeaf segmentsLeaf) {
			try (OutputStream out = segmentsLeaf.getOutputStream(false)) {
				VideoXStream.toXml(out, segments);
			} catch (IOException e) {
				log.error("", e);
			}
		}
	}

	@Override
	public VideoComments loadComments(OLATResource olatResource) {
		VFSContainer vfsContainer = getMasterContainer(olatResource);
		VFSItem commentsItem = vfsContainer.resolve(FILENAME_COMMENTS_XML);
		if (commentsItem instanceof VFSLeaf commentsLeaf) {
			try (InputStream in = commentsLeaf.getInputStream()) {
				return standardizeColors(VideoXStream.fromXml(in, VideoComments.class));
			} catch (IOException e) {
				log.error("", e);
			}
		}
		return new VideoCommentsImpl();
	}

	private VideoComments standardizeColors(VideoComments comments) {
		for (VideoComment comment : comments.getComments()) {
			comment.setColor(VideoModule.getColorFromMarkerStyle(comment.getColor()));
		}
		return comments;
	}

	@Override
	public void saveComments(VideoComments comments, OLATResource olatResource) {
		VFSContainer vfsContainer = getMasterContainer(olatResource);
		VFSItem commentsItem = vfsContainer.resolve(FILENAME_COMMENTS_XML);
		if (commentsItem == null) {
			commentsItem = vfsContainer.createChildLeaf(FILENAME_COMMENTS_XML);
		}
		if (commentsItem instanceof VFSLeaf commentsLeaf) {
			try (OutputStream out = commentsLeaf.getOutputStream(false)) {
				VideoXStream.toXml(out, comments);
			} catch (IOException e) {
				log.error("", e);
			}
		}
	}

	@Override
	public void deleteUnusedCommentFiles(VideoComments comments, OLATResource olatResource) {
		VFSContainer commentsMediaContainer = getCommentMediaContainer(olatResource);
		Set<String> foundFileNames = commentsMediaContainer.getItems().stream().map(VFSItem::getName)
						.collect(Collectors.toSet());
		Set<String> referencedFileNames = comments.getComments().stream().map(VideoComment::getFileName)
				.filter(Objects::nonNull).collect(Collectors.toSet());
		Set<String> referencedMasterFileNames = comments.getComments().stream().map(VideoComment::getFileName)
				.filter(Objects::nonNull).map(fn -> VFSTranscodingService.masterFilePrefix + fn)
				.collect(Collectors.toSet());
		for (String foundFileName : foundFileNames) {
			if (!referencedFileNames.contains(foundFileName) && !referencedMasterFileNames.contains(foundFileName)) {
				VFSItem itemToDelete = commentsMediaContainer.resolve(foundFileName);
				if (itemToDelete != null && itemToDelete.exists()) {
					itemToDelete.deleteSilently();
				}
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
				return replaceMissingStyles(VideoXStream.fromXml(in, VideoQuestions.class));
			} catch(IOException e) {
				log.error("", e);
			}
		}
		return new VideoQuestionsImpl();
	}

	private VideoQuestions replaceMissingStyles(VideoQuestions questions) {
		for (VideoQuestion question : questions.getQuestions()) {
			question.setStyle(replaceMissingColor(question.getStyle()));
		}
		return questions;
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

	private List<VideoMeta> getAllMp4VideoMetadata() {
		VideoMetadataSearchParams searchParams = new VideoMetadataSearchParams();
		searchParams.setUrlNull(Boolean.TRUE);
		return getVideoMetadata(searchParams);
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

	@Override
	public Pair<String, String> lookUpTitleAndDescription(String url) {
		if (!StringHelper.containsNonWhitespace(url)) {
			return ImmutablePair.nullPair();
		}

		VideoFormat videoFormat = VideoFormat.valueOfUrl(url);
		if (videoFormat == null) {
			return ImmutablePair.nullPair();
		}

		String oembedUrl = getOembedUrl(videoFormat, url);
		if (oembedUrl == null) {
			return ImmutablePair.nullPair();
		}

		JSONObject oembedJson = getOembedJson(oembedUrl);
		if (oembedJson == null) {
			return ImmutablePair.nullPair();
		}

		return new ImmutablePair<>(oembedJson.optString("title"), oembedJson.optString("description"));
	}

	private String getOembedUrl(VideoFormat videoFormat, String url) {
		if (videoFormat == VideoFormat.youtube) {
			return YOUTUBE_OEMBED_URL + StringHelper.urlEncodeUTF8(url);
		} else if (videoFormat == VideoFormat.vimeo) {
			return VIMEO_OEMBED_URL + StringHelper.urlEncodeUTF8(url);
		} else {
			return null;
		}
	}

	@Override
	public String lookUpThumbnail(String url, VFSContainer targetContainer, String targetUuid) {
		if (!StringHelper.containsNonWhitespace(url)) {
			return null;
		}

		VideoFormat videoFormat = VideoFormat.valueOfUrl(url);
		if (videoFormat == null) {
			log.info("Unknown video format: {}", url);
			return null;
		}

		String oembedUrl = getOembedUrl(videoFormat, url);
		if (oembedUrl == null) {
			log.debug("No oEmbed URL available for: {}", url);
			return null;
		}

		JSONObject oembedJson = getOembedJson(oembedUrl);
		if (oembedJson == null) {
			log.info("Could not download oEmbed information for: {}", url);
			return null;
		}

		String thumbnailUrl = oembedJson.optString("thumbnail_url");
		if (!StringHelper.containsNonWhitespace(thumbnailUrl)) {
			log.info("Could not find thumbnail_url in oEmbed data '{}' for url '{}'", oembedUrl, url);
			return null;
		}

		return downloadThumbnail(thumbnailUrl, targetContainer, targetUuid);
	}

	private String downloadThumbnail(String thumbnailUrl, VFSContainer targetContainer, String targetUuid) {
		String thumbnailFileName = null;

		HttpGet downLoadRequest = new HttpGet(thumbnailUrl);
		try (CloseableHttpClient httpClient = httpClientService.createHttpClient();
			 CloseableHttpResponse httpResponse = httpClient.execute(downLoadRequest);) {
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				HttpEntity httpEntity = httpResponse.getEntity();
				String extension = getExtension(httpEntity.getContentType(), thumbnailUrl);
				if (StringHelper.containsNonWhitespace(extension)) {
					String targetFileName = FILENAME_THUMBNAIL_PREFIX + targetUuid + "." + extension;
					VFSLeaf targetLeaf = targetContainer.createChildLeaf(targetFileName);
					InputStream content = httpEntity.getContent();
					if (VFSManager.copyContent(content, targetLeaf, null)) {
						thumbnailFileName = targetFileName;
					}
				}
			} else {
				log.warn("Oembed service did not return thumbnail for url '{}'", thumbnailUrl);
			}
		} catch (Exception e) {
			log.error("Oembed service did not return thumbnail for url '{}'", thumbnailUrl, e);
		}

		return thumbnailFileName;
	}

	private String getExtension(Header contentType, String thumbnailUrl) {
		if (contentType == null) {
			return null;
		}
		String contentTypeValue = contentType.getValue();
		String contentTypeSuffix = getExtensionFromContentType(contentTypeValue);
		if (StringHelper.containsNonWhitespace(contentTypeSuffix)) {
			return contentTypeSuffix;
		}
		String fileNameSuffix = FileUtils.getFileSuffix(thumbnailUrl);
		if (StringHelper.containsNonWhitespace(fileNameSuffix) && fileNameSuffix.length() <= 4) {
			return fileNameSuffix;
		}
		return null;
	}

	private String getExtensionFromContentType(String contentType) {
		return switch (contentType) {
			case "image/gif" -> "gif";
			case "image/jpg", "image/jpeg" -> "jpg";
			case "image/png" -> "png";
			default -> null;
		};
	}

	private JSONObject getOembedJson(String oembedUrl) {
		try (CloseableHttpClient httpClient = httpClientService.createHttpClient()) {
			HttpGet get = new HttpGet(oembedUrl);
			HttpResponse response = httpClient.execute(get);
			int status = response.getStatusLine().getStatusCode();
			if (status == 200) {
				String content = EntityUtils.toString(response.getEntity());
				return new JSONObject(content);
			} else {
				log.info("Oembed service returned status {} for url '{}'", status, oembedUrl);
				return null;
			}
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}

	/**
	 * This filter returns true for all VTT and SRT language files of kind
	 * "subtitles". The filter will skip chapter files that are technically also VTT
	 * but are of kind "chapters"
	 */
	private static class TrackFilter implements VFSItemFilter {
		@Override
		public boolean accept(VFSItem vfsItem) {
			if(vfsItem instanceof VFSLeaf) {
				String name = vfsItem.getName();
				String suffix = FileUtils.getFileSuffix(name);
				return !name.startsWith(".")
						&& (VideoManager.FILETYPE_SRT.equals(suffix) || VideoManager.FILETYPE_VTT.equals(suffix))
						&& !FILENAME_CHAPTERS_VTT.equals(name);
			}
			return false;
		}
	}
}
