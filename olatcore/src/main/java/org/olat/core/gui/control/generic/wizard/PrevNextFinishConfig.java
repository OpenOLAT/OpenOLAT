package org.olat.core.gui.control.generic.wizard;

public class PrevNextFinishConfig {

	/**
	 * do not use the values to decide about visibililty
	 */
	public final static PrevNextFinishConfig NOOP = new PrevNextFinishConfig(false, false, false);
	public final static PrevNextFinishConfig NEXT_FINISH = new PrevNextFinishConfig(false,true,true);
	public final static PrevNextFinishConfig BACK_FINISH = new PrevNextFinishConfig(true,false,true);
	public final static PrevNextFinishConfig BACK_NEXT = new PrevNextFinishConfig(true,true,false);
	public final static PrevNextFinishConfig BACK = new PrevNextFinishConfig(true,false,false);
	public final static PrevNextFinishConfig NEXT = new PrevNextFinishConfig(false,true,false);
	public static final PrevNextFinishConfig BACK_NEXT_FINISH = new PrevNextFinishConfig(true,true,true);
	
	private boolean backIsEnabled;
	private boolean nextIsEnabled;
	private boolean finishIsEnabled;

	
	public PrevNextFinishConfig( boolean enableBack, boolean enableNext, boolean enableFinish){
		this.backIsEnabled = enableBack;
		this.nextIsEnabled = enableNext;
		this.finishIsEnabled = enableFinish;
	}

	public boolean isBackIsEnabled() {
		return backIsEnabled;
	}

	public boolean isNextIsEnabled() {
		return nextIsEnabled;
	}

	public boolean isFinishIsEnabled() {
		return finishIsEnabled;
	}

	@Override
	public String toString() {
		return "back:"+backIsEnabled+" | next:"+nextIsEnabled+" | finish:"+finishIsEnabled;
	}
	
}
