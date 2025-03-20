package com.github.topi314.lavasrc.mirror;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import com.github.topi314.lavasrc.ExtendedAudioTrack;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.tools.io.PersistentHttpStream;
import com.sedmelluq.discord.lavaplayer.tools.io.SeekableInputStream;
import com.sedmelluq.discord.lavaplayer.track.*;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MirroringAudioTrack extends ExtendedAudioTrack {

	private static final Logger log = LoggerFactory.getLogger(MirroringAudioTrack.class);

	protected final Supplier<HttpInterface> httpInterfaces;

	protected final MirroringResources resources;

	@SuppressWarnings("removal")
	@Deprecated(forRemoval = true)
	protected MirroringAudioSourceManager sourceManager;

	@SuppressWarnings("removal")
	@Deprecated(forRemoval = true)
	public MirroringAudioTrack(AudioTrackInfo trackInfo, String albumName, String albumUrl, String artistUrl, String artistArtworkUrl, String previewUrl, boolean isPreview, MirroringAudioSourceManager sourceManager) {
		this(trackInfo, sourceManager.getResources(), sourceManager.httpInterfaceManager::getInterface, albumName, albumUrl, artistUrl, artistArtworkUrl, previewUrl, isPreview);
	}

	public MirroringAudioTrack(
		AudioTrackInfo trackInfo,
		MirroringResources mirroringResources,
		Supplier<HttpInterface> httpInterfaces,
		@Nullable String albumName,
		@Nullable String albumUrl,
		@Nullable String artistUrl,
		@Nullable String artistArtworkUrl,
		@Nullable String previewUrl,
		boolean isPreview
	) {
		super(trackInfo, albumName, albumUrl, artistUrl, artistArtworkUrl, previewUrl, isPreview);
		this.httpInterfaces = httpInterfaces;
		this.resources = mirroringResources;
	}

	protected abstract InternalAudioTrack createAudioTrack(AudioTrackInfo trackInfo, SeekableInputStream inputStream);

	@Override
	public void process(LocalAudioTrackExecutor executor) throws Exception {
		if (this.isPreview) {
			if (this.previewUrl == null) {
				throw new FriendlyException("No preview url found", FriendlyException.Severity.COMMON, new IllegalArgumentException());
			}
			try (var httpInterface = this.httpInterfaces.get()) {
				try (var stream = new PersistentHttpStream(httpInterface, new URI(this.previewUrl), this.trackInfo.length)) {
					processDelegate(createAudioTrack(this.trackInfo, stream), executor);
				}
			}
			return;
		}
		var track = this.resources.getResolver().apply(this);

		if (track instanceof AudioPlaylist) {
			var tracks = ((AudioPlaylist) track).getTracks();
			if (tracks.isEmpty()) {
				throw new TrackNotFoundException("No tracks found in playlist or search result for track");
			}
			track = tracks.get(0);
		}
		if (track instanceof InternalAudioTrack) {
			((InternalAudioTrack) track).setUserData(this.getUserData());
			var internalTrack = (InternalAudioTrack) track;
			log.debug("Loaded track mirror from {} {}({}) ", internalTrack.getSourceManager().getSourceName(), internalTrack.getInfo().title, internalTrack.getInfo().uri);
			processDelegate(internalTrack, executor);
			return;
		}
		throw new TrackNotFoundException("No mirror found for track");
	}

	@Override
	public AudioSourceManager getSourceManager() {
		return this.sourceManager;
	}

	public AudioItem loadItem(String query) {
		var cf = new CompletableFuture<AudioItem>();
		this.resources.getAudioPlayerManager().loadItem(query, new AudioLoadResultHandler() {

			@Override
			public void trackLoaded(AudioTrack track) {
				log.debug("Track loaded: {}", track.getIdentifier());
				cf.complete(track);
			}

			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				log.debug("Playlist loaded: {}", playlist.getName());
				cf.complete(playlist);
			}

			@Override
			public void noMatches() {
				log.debug("No matches found for: {}", query);
				cf.complete(AudioReference.NO_TRACK);
			}

			@Override
			public void loadFailed(FriendlyException exception) {
				log.debug("Failed to load: {}", query);
				cf.completeExceptionally(exception);
			}
		});
		return cf.join();
	}

}
