package org.olat.modules.video.model;

public class TranscodingCount {
	
	private Long count;
	private Integer resolution;

	public TranscodingCount(Long count, Integer resolution) {
		this.count = count;
		this.resolution = resolution;
	}

	public int getCount() {
		return  Math.toIntExact(count);
	}

	public int getResolution() {
		return resolution;
	}
	
	

}
