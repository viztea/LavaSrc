package com.github.topi314.lavasrc.applemusic;

import com.github.topi314.lavasrc.LavaSrcTools;
import com.github.topi314.lavasrc.mirror.MirroringAudioTrack;
import com.sedmelluq.discord.lavaplayer.container.mpeg.MpegAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.io.SeekableInputStream;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.InternalAudioTrack;

public class AppleMusicAudioTrack extends MirroringAudioTrack {

	public final AppleMusicSourceManager sourceManager;

	public AppleMusicAudioTrack(AudioTrackInfo trackInfo, String albumName, String albumUrl, String artistUrl, String artistArtworkUrl, String previewUrl, boolean isPreview, AppleMusicSourceManager sourceManager) {
		super(trackInfo, sourceManager.mirroringResources, LavaSrcTools.getInterfaces(sourceManager), albumName, albumUrl, artistUrl, artistArtworkUrl, previewUrl, isPreview);
		this.sourceManager = sourceManager;
	}

	@Override
	public AppleMusicSourceManager getSourceManager() {
		return sourceManager;
	}

	@Override
	protected InternalAudioTrack createAudioTrack(AudioTrackInfo trackInfo, SeekableInputStream stream) {
		return new MpegAudioTrack(trackInfo, stream);
	}

	@Override
	protected AudioTrack makeShallowClone() {
		return new AppleMusicAudioTrack(this.trackInfo, null, null, null, null, null, false, this.sourceManager);
	}

}
