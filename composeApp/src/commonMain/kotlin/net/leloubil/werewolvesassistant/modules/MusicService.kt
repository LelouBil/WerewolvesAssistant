package net.leloubil.werewolvesassistant.modules

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemTemporaryDirectory
import net.leloubil.werewolvesassistant.modules.MusicStatus.Progress.*
import org.koin.core.annotation.Named
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Singleton
import org.openani.mediamp.MediampPlayer
import org.openani.mediamp.PlaybackState
import org.openani.mediamp.features.AudioLevelController
import org.openani.mediamp.metadata.duration
import org.openani.mediamp.playUri
import org.openani.mediamp.togglePause
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

sealed interface TrackMetadata {
    data class FullMetadata(val title: String) : TrackMetadata
}


sealed interface Track {
    data class Url(val url: String) : Track
    data class Data(val data: ByteArray) : Track {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Data

            if (!data.contentEquals(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            return data.contentHashCode()
        }
    }
}

data class PlayingTrack(val metadata: TrackMetadata, val duration: Duration)


sealed interface MusicStatus {
    data object NoMusic : MusicStatus
    sealed class Progress {
        abstract val duration: Duration?

        data class Playing(override val duration: Duration?) : Progress()
        data class Paused(override val duration: Duration?) : Progress()
    }

    data class HasMusic(val trackInfo: PlayingTrack, val progress: Progress) : MusicStatus
}

interface MusicService {

    val status: StateFlow<MusicStatus>

    val isLooping: StateFlow<Boolean>

    fun setLooping(isLooping: Boolean)

    fun playReplacing(info: Track, metadata: TrackMetadata)

    fun resume()
    fun pause()

}


@Named
annotation class BackgroundMusicPlayer


@Named
annotation class SFXPlayer

@Singleton(
    binds = [MusicService::class],
    createdAtStart = true,
)
@BackgroundMusicPlayer
class BackgroundMusicPlayerImpl(@Provided context: ContextWrapper) :
    MusicServiceImpl(context, true, volumeDefault = 0.2f)


@Singleton(
    binds = [MusicService::class],
    createdAtStart = true,
)
@SFXPlayer
class SFXPlayerImpl(@Provided context: ContextWrapper) : MusicServiceImpl(context, false, volumeDefault = 0.2f)

sealed class MusicServiceImpl(private val context: ContextWrapper, loopDefault: Boolean, val volumeDefault: Float) :
    MusicService {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val player = MediampPlayer(context.context, scope.coroutineContext)

    private var metadata: MutableStateFlow<TrackMetadata?> = MutableStateFlow(null)

    private val _isLooping = MutableStateFlow(loopDefault)
    override val isLooping: StateFlow<Boolean> = _isLooping

    override fun setLooping(isLooping: Boolean) {
        _isLooping.value = isLooping
    }

    init {
        scope.launch {
            player.playbackState.collect {
                if (isLooping.value && it == PlaybackState.FINISHED) {
                    player.seekTo(0)
                    player.resume()
                }
            }
        }
    }


    override val status: StateFlow<MusicStatus> = combine(
        player.playbackState.filter { it != PlaybackState.READY},
        player.currentPositionMillis,
        player.mediaProperties,
        metadata
    ) { state, millisProgress, mediaProps, mediaMeta ->
        if (mediaProps == null || mediaMeta == null) {
            return@combine MusicStatus.NoMusic
        }
        val trackInfo = PlayingTrack(mediaMeta, mediaProps.duration)
        val musicProg = millisProgress.milliseconds
        println("state: $state")
        val prog = when (state) {
            PlaybackState.PLAYING -> {
                Playing(musicProg)
            }

            PlaybackState.READY -> error("Should not happen")
            PlaybackState.PAUSED -> Paused(musicProg)
            PlaybackState.PAUSED_BUFFERING -> Paused(musicProg)
            PlaybackState.FINISHED -> Paused(musicProg)
            PlaybackState.ERROR -> Paused(musicProg)
        }
        MusicStatus.HasMusic(trackInfo, prog)

    }.stateIn(scope, SharingStarted.Eagerly, MusicStatus.NoMusic)

    override fun playReplacing(info: Track, metadata: TrackMetadata) {
        scope.launch {
            player.features[AudioLevelController.Key]?.let {
                it.setVolume(volumeDefault * it.maxVolume) // todo debug
            }
            this@MusicServiceImpl.metadata.value = metadata
            when (info) {
                is Track.Data -> {
                    val path = Path(SystemTemporaryDirectory, "werewolves_music_cache")
                    SystemFileSystem.createDirectories(path)
                    val musicPath = Path(path, info.data.contentHashCode().toString())
                    if (!SystemFileSystem.exists(musicPath)) {
                        SystemFileSystem.sink(musicPath).buffered().use {
                            it.write(info.data)
                        }
                    }
                    player.playUri(musicPath.toString())
                }

                is Track.Url -> player.playUri(info.url)
            }
//            player.playUri(info.uri)
        }
    }

    override fun resume() {
        player.resume()
    }

    override fun pause() {
        val value = status.value
        if (value is MusicStatus.HasMusic && value.progress is Playing) {
            player.togglePause()
        }
    }


}
