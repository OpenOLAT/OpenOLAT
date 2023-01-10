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
package org.olat.core.commons.services.video;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.jcodec.api.FrameGrab;
import org.jcodec.common.Codec;
import org.jcodec.common.VideoCodecMeta;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.model.Picture;
import org.jcodec.containers.mp4.boxes.MovieBox;
import org.jcodec.containers.mp4.demuxer.MP4Demuxer;
import org.jcodec.scale.AWTUtil;
import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.image.spi.ImageHelperImpl;
import org.olat.core.commons.services.thumbnail.CannotGenerateThumbnailException;
import org.olat.core.commons.services.thumbnail.FinalSize;
import org.olat.core.commons.services.thumbnail.ThumbnailSPI;
import org.olat.core.commons.services.video.spi.FLVParser;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.WorkThreadInformations;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.ims.cp.ui.VFSCPNamedItem;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 04.04.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("movieService")
public class MovieServiceImpl implements MovieService, ThumbnailSPI {
	
	private static final Logger log = Tracing.createLoggerFor(MovieServiceImpl.class);

	private static final List<String> extensions = new ArrayList<>();
	private static final List<Codec> supportedCodecs = new ArrayList<>();
	static {
		// supported file extensions
		extensions.add("mp4");
		extensions.add("m4v");
		extensions.add("mov");
		// supported fourCC for H264 codec
		supportedCodecs.add(Codec.codecByFourcc("avc1"));
		supportedCodecs.add(Codec.codecByFourcc("davc"));
		supportedCodecs.add(Codec.codecByFourcc("h264"));
		supportedCodecs.add(Codec.codecByFourcc("x264"));
		supportedCodecs.add(Codec.codecByFourcc("vssh"));
	}
	
	
	
	@Override
	public List<String> getExtensions() {
		return extensions;
	}

	@Override
	public Size getSize(VFSLeaf media, String suffix) {
		File file = null;
		if(media instanceof VFSCPNamedItem) {
			media = ((VFSCPNamedItem)media).getDelegate();
		}
		if(media instanceof LocalFileImpl) {
			file = ((LocalFileImpl)media).getBasefile();
		}
		if(file == null) {
			return null;
		}

		if(extensions.contains(suffix)) {
			try(RandomAccessFile accessFile = new RandomAccessFile(file, "r");
					FileChannel ch = accessFile.getChannel();
					FileChannelWrapper in = new FileChannelWrapper(ch);
					MP4Demuxer demuxer1 = MP4Demuxer.createMP4Demuxer(in)) {
				
				MovieBox movieBox = demuxer1.getMovie();
				org.jcodec.common.model.Size size = movieBox.getDisplaySize();
				// Case 1: standard case, get dimension from movie
				int w = size.getWidth();
				int h = size.getHeight();
				
				// Case 2: landscape movie from iOS: width and height is negative, no dunny why
				if (w < 0 && h < 0) {
					w = 0 - w;
					h = 0 - h;
				}
				if (w == 0) {
					// Case 3: portrait movie from iOS: movie dimensions are not set, but there 
					// something in the track box.
					try {
						// This code is the way it is just because I don't know
						// how to safely read the rotation/portrait/landscape
						// flag of the movie. Those mp4 guys are really
						// secretive folks, did not find any documentation about
						// this. Best guess.
						VideoCodecMeta meta = demuxer1.getVideoTrack().getMeta().getVideoCodecMeta();
						org.jcodec.common.model.Size size2 = meta.getSize();
						w = size2.getHeight();
						h = size2.getWidth();
					} catch(Exception e) {
						log.debug("can not get size from box {}", e.getMessage());
					}
				}
				return new Size(w, h, false);
			} catch (Exception | AssertionError e) {
				log.error("Cannot extract size of: {}", media, e);
			}
		} else if(suffix.equals("flv")) {
			try(InputStream stream = new FileInputStream(file)) {
				FLVParser infos = new FLVParser();
				infos.parse(stream);
				if(infos.getWidth() > 0 && infos.getHeight() > 0) {
					int w = infos.getWidth();
					int h = infos.getHeight();
					return new Size(w, h, false);
				}
			} catch (Exception e) {
				log.error("Cannot extract size of: {}", media, e);
			}
		}

		return null;
	}

	@Override
	public long getDuration(VFSLeaf media, String suffix) {
		File file = null;
		if(media instanceof VFSCPNamedItem) {
			media = ((VFSCPNamedItem)media).getDelegate();
		}
		if(media instanceof LocalFileImpl) {
			file = ((LocalFileImpl)media).getBasefile();
		}
		if(file == null) {
			return -1;
		}

		if(extensions.contains(suffix)) {
			try(RandomAccessFile accessFile = new RandomAccessFile(file, "r");
				FileChannel ch = accessFile.getChannel();
				FileChannelWrapper in = new FileChannelWrapper(ch);
				MP4Demuxer demuxer1 = MP4Demuxer.createMP4Demuxer(in)) {
				
				MovieBox movie = demuxer1.getMovie();
				long duration = movie.getDuration();
				int timescale = movie.getTimescale();
				if (timescale < 1) {
					timescale = 1;
				}				
				// Simple calculation. Ignore NTSC and other issues for now
				return 1000L * duration / timescale;
			} catch (Exception | AssertionError e) {
				log.error("Cannot extract duration of: {}", media, e);
			}
		}

		return -1;
	}
	
	@Override
	public long getFrameCount(VFSLeaf media, String suffix) {
		File file = null;
		if(media instanceof VFSCPNamedItem) {
			media = ((VFSCPNamedItem)media).getDelegate();
		}
		if(media instanceof LocalFileImpl) {
			file = ((LocalFileImpl)media).getBasefile();
		}
		if(file == null) {
			return -1;
		}

		if(extensions.contains(suffix)) {
			try(RandomAccessFile accessFile = new RandomAccessFile(file, "r");
					FileChannel ch = accessFile.getChannel();
					FileChannelWrapper in = new FileChannelWrapper(ch);
					MP4Demuxer demuxer1 = MP4Demuxer.createMP4Demuxer(in)) {
				return demuxer1.getVideoTrack().getMeta().getTotalFrames();
			} catch (Exception | AssertionError e) {
				log.error("Cannot extract num. of frames of: {}", media, e);
			}
		}

		return -1;
	}

	@Override
	public boolean isMP4(VFSLeaf media, String fileName) {
		File file = null;
		if(media instanceof VFSCPNamedItem) {
			media = ((VFSCPNamedItem)media).getDelegate();
		}
		if(media instanceof LocalFileImpl) {
			file = ((LocalFileImpl)media).getBasefile();
		}
		if(file == null) {
			return false;
		}
		String suffix = FileUtils.getFileSuffix(fileName);
		if(extensions.contains(suffix)) {
			try(RandomAccessFile accessFile = new RandomAccessFile(file, "r");
					FileChannel ch = accessFile.getChannel();
					FileChannelWrapper in = new FileChannelWrapper(ch);
					MP4Demuxer demuxer1 = MP4Demuxer.createMP4Demuxer(in)) {
				Codec codec = demuxer1.getVideoTrack().getMeta().getCodec();
				if (supportedCodecs.contains(codec)) {
					return true;
				} 
				log.info("Movie file:: {} has correct suffix:: {} but fourCC:: {} not in our list of supported codecs.", fileName, suffix, codec);
			} catch (Exception | Error e) {
				// anticipated exception, is not an mp4 file
			}
		}
		return false;
	}

	
	@Override
	public FinalSize generateThumbnail(VFSLeaf file, VFSLeaf thumbnailFile, int maxWidth, int maxHeight, boolean fill)
	throws CannotGenerateThumbnailException {
		FinalSize size = null;
		if(file instanceof LocalFileImpl && thumbnailFile instanceof LocalFileImpl) {
			try {
				WorkThreadInformations.setInfoFiles(null, file);
				WorkThreadInformations.set("Generate thumbnail (video) VFSLeaf=" + file);
				
				File movieFile = ((LocalFileImpl)file).getBasefile();
				Size movieSize = getSize(file, "mp4");
				File scaledImage = ((LocalFileImpl)thumbnailFile).getBasefile();
				Picture picture = FrameGrab.getFrameFromFile(movieFile, 20);
				BufferedImage frame = AWTUtil.toBufferedImage(picture);
				frame = JCodecHelper.scale(movieSize, picture, frame);
				Size scaledSize = ImageHelperImpl.calcScaledSize(frame, maxWidth, maxHeight);
				if(ImageHelperImpl.writeTo(frame, scaledImage, scaledSize, "jpeg")) {
					size = new FinalSize(scaledSize.getWidth(), scaledSize.getHeight());
				}
			//NullPointerException can be thrown if the jcodec cannot handle the codec of the movie
			//ArrayIndexOutOfBoundsException
			} catch (Exception | AssertionError e) {
				log.error("", e);
			} finally {
				WorkThreadInformations.unset();
			}
		}
		return size;
	}
	

}