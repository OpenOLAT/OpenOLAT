/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.olat.core.commons.services.video.spi;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.xml.sax.SAXException;

/**
 * <p>Code from Tika project. It has a slight change which break the flow if the height and width
 * are found. It speed up a lot the code and we don't want all metadatas but these two.</p>
 * 
 * <p>
 * Parser for metadata contained in Flash Videos (.flv). Resources:
 * http://osflash.org/flv and for AMF:
 * http://download.macromedia.com/pub/labs/amf/amf0_spec_121207.pdf
 * <p>
 * This parser is capable of extracting the general metadata from header as well
 * as embedded metadata.
 * <p>
 * Known keys for metadata (from file header):
 * <ol>
 * <li>hasVideo: true|false
 * <li>hasSound: true|false
 * </ol>
 * <p>
 * In addition to the above values also metadata that is inserted in to the
 * actual stream will be picked. Usually there are keys like:
 * hasKeyframes, lastkeyframetimestamp, audiocodecid, keyframes, filepositions,
 * hasMetadata, audiosamplerate, videodatarate metadatadate, videocodecid,
 * metadatacreator, audiosize, hasVideo, height, audiosamplesize, framerate,
 * hasCuePoints width, cuePoints, lasttimestamp, canSeekToEnd, datasize,
 * duration, videosize, filesize, audiodatarate, hasAudio, stereo audiodelay
 */
public class FLVParser {

    private static int TYPE_METADATA = 0x12;
    private static byte MASK_AUDIO = 1;
    private static byte MASK_VIDEO = 4;
    
    private boolean hasVideo;
    private boolean hasAudio;
    private int width = 0;
    private int height = 0;
 
    public boolean isHasVideo() {
		return hasVideo;
	}

	public boolean isHasAudio() {
		return hasAudio;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

    private long readUInt32(DataInputStream input) throws IOException {
        return input.readInt() & 0xFFFFFFFFL;
    }

    private int readUInt24(DataInputStream input) throws IOException {
        int uint = input.read()<<16;
        uint += input.read()<<8;
        uint += input.read(); 
        return uint;
    }

    private Object readAMFData(DataInputStream input, int type)
            throws IOException {
        if (type == -1) {
            type = input.readUnsignedByte();
        }
        switch (type) {
        case 0:
            return input.readDouble();
        case 1:
            return input.readUnsignedByte() == 1;
        case 2:
            return readAMFString(input);
        case 3:
            return readAMFObject(input);
        case 8:
            return readAMFEcmaArray(input);
        case 10:
            return readAMFStrictArray(input);
        case 11:
            final Date date = new Date((long) input.readDouble());
            input.readShort(); // time zone
            return date;
        case 13:
            return "UNDEFINED";
        default:
            return null;
        }
    }

    private Object readAMFStrictArray(DataInputStream input) throws IOException {
        long count = readUInt32(input);
        ArrayList<Object> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(readAMFData(input, -1));
        }
        return list;
    }


    private String readAMFString(DataInputStream input) throws IOException {
        int size = input.readUnsignedShort();
        byte[] chars = new byte[size];
        input.readFully(chars);
        return new String(chars);
    }

    private Object readAMFObject(DataInputStream input) throws IOException {
        HashMap<String, Object> array = new HashMap<>();
        while (true) {
            String key = readAMFString(input);
            int dataType = input.read();
            if (dataType == 9) { // object end marker
                break;
            }
            array.put(key, readAMFData(input, dataType));
        }
        return array;
    }

    private Object readAMFEcmaArray(DataInputStream input) throws IOException {
        long size = readUInt32(input);
        HashMap<String, Object> array = new HashMap<>();
        for (int i = 0; i < size; i++) {
            String key = readAMFString(input);
            int dataType = input.read();
            array.put(key, readAMFData(input, dataType));
        }
        return array;
    }

    private boolean checkSignature(DataInputStream fis) throws IOException {
        return fis.read() == 'F' && fis.read() == 'L' && fis.read() == 'V';
    }
    
    public void parse(InputStream stream)
    	    throws IOException, SAXException {
    	parseIntern(stream);
    }

	private void parseIntern(InputStream stream)
    throws IOException, SAXException {
        
    	DataInputStream datainput = new DataInputStream(stream);
        if (!checkSignature(datainput)) {
            throw new IOException("FLV signature not detected");
        }

        // header
        int version = datainput.readUnsignedByte();
        if (version != 1) {
            // should be 1, perhaps this is not flv?
            throw new IOException("Unpexpected FLV version: " + version);
        }

        int typeFlags = datainput.readUnsignedByte();

        long len = readUInt32(datainput);
        if (len != 9) {
            // we only know about format with header of 9 bytes
            throw new IOException("Unpexpected FLV header length: " + len);
        }

        long sizePrev = readUInt32(datainput);
        if (sizePrev != 0) {
            // should be 0, perhaps this is not flv?
            throw new IOException(
                    "Unpexpected FLV first previous block size: " + sizePrev);
        }

        hasVideo = (typeFlags & MASK_VIDEO) != 0;
        hasAudio = (typeFlags & MASK_AUDIO) != 0;

       
        // flv tag stream follows...
        while (true) {
            int type = datainput.read();
            if (type == -1) {
                // EOF
                break;
            }

            int datalen = readUInt24(datainput); //body length
            readUInt32(datainput); // timestamp
            readUInt24(datainput); // streamid

            if (type == TYPE_METADATA) {
                // found metadata Tag, read content to buffer
                byte[] metaBytes = new byte[datalen];
                for (int readCount = 0; readCount < datalen;) {
                    int r = stream.read(metaBytes, readCount, datalen - readCount);
                    if(r!=-1) {
                        readCount += r;

                    } else {
                        break;
                    }
                }

                ByteArrayInputStream is = new ByteArrayInputStream(metaBytes);

                DataInputStream dis = new DataInputStream(is);

                Object data = null;

                for (int i = 0; i < 2; i++) {
                    data = readAMFData(dis, -1);
                }

                if (data instanceof Map) {
                    // TODO if there are multiple metadata values with same key (in
                    // separate AMF blocks, we currently loose previous values)
                    @SuppressWarnings("unchecked")
					Map<String, Object> extractedMetadata = (Map<String, Object>) data;
                    for (Entry<String, Object> entry : extractedMetadata.entrySet()) {
                        if (entry.getValue() == null) {
                            continue;
                        }
                        if("height".equals(entry.getKey())) {
                        	String h = entry.getValue().toString();
                        	height = Math.round(Float.parseFloat(h));
                        } else if("width".equals(entry.getKey())) {
                        	String w = entry.getValue().toString();
                        	width = Math.round(Float.parseFloat(w));
                        }
                    }
                    //we only want the height and the width
                    //we have them, get out of this lengthy file
                    if(width > 0 && height > 0) {
                    	break;
                    }
                }

            } else {
                // Tag was not metadata, skip over data we cannot handle
                for (int i = 0; i < datalen; i++) {
                    datainput.readByte();
                }
            }

            sizePrev = readUInt32(datainput); // previous block size
            if (sizePrev != datalen + 11) {
                // file was corrupt or we could not parse it...
                break;
            }
        }
    }
}