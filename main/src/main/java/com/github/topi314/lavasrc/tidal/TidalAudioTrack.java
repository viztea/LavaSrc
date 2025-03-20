package com.github.topi314.lavasrc.tidal;

import com.github.topi314.lavasrc.LavaSrcTools;
import com.github.topi314.lavasrc.mirror.MirroringAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.io.SeekableInputStream;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.InternalAudioTrack;

public class TidalAudioTrack extends MirroringAudioTrack {

	public final TidalSourceManager sourceManager;

	public TidalAudioTrack(AudioTrackInfo trackInfo, String albumName, String albumUrl, String artistUrl, String artistArtworkUrl, TidalSourceManager sourceManager) {
		super(trackInfo, sourceManager.mirroringResources, LavaSrcTools.getInterfaces(sourceManager), albumName, albumUrl, artistUrl, artistArtworkUrl, null, false);
		this.sourceManager = sourceManager;
	}

	@Override
	public TidalSourceManager getSourceManager() {
		return sourceManager;
	}

	@Override
	protected InternalAudioTrack createAudioTrack(AudioTrackInfo trackInfo, SeekableInputStream stream) {
		throw new UnsupportedOperationException("Previews are not supported by Tidal");
	}

	@Override
	protected AudioTrack makeShallowClone() {
		return new TidalAudioTrack(this.trackInfo, null, null, null, null, this.sourceManager);
	}

}
