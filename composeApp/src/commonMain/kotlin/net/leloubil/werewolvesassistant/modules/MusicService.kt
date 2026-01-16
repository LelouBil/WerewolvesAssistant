package net.leloubil.werewolvesassistant.modules

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Singleton
import org.openani.mediamp.MediampPlayer
import org.openani.mediamp.PlaybackState
import org.openani.mediamp.features.AudioLevelController
import org.openani.mediamp.metadata.duration
import org.openani.mediamp.playUri
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

sealed interface TrackMetadata {
    data class FullMetadata(val title: String) : TrackMetadata
}


data class UrlTrack(val uri: String)
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

    fun playReplacing(info: UrlTrack, metadata: TrackMetadata)

    fun resume()
    fun pause()

}


@Singleton
class MusicServiceImpl(@Provided private val context: ContextWrapper) : MusicService {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val player = MediampPlayer(context.context, scope.coroutineContext)

    private var metadata: MutableStateFlow<TrackMetadata?> = MutableStateFlow(null)

    private val _isLooping = MutableStateFlow(true)
    override val isLooping: StateFlow<Boolean> = _isLooping

    override fun setLooping(isLooping: Boolean) {
        _isLooping.value = isLooping
    }

    init {
        println("ha")
        scope.launch {
            player.playbackState.collect {
                if (it == PlaybackState.FINISHED) {
                    player.seekTo(0)
                    player.resume()
                }
            }
        }
    }


    override val status: StateFlow<MusicStatus> = combine(
        player.playbackState,
        player.currentPositionMillis,
        player.mediaProperties,
        metadata
    ) { state, millisProgress, mediaProps, mediaMeta ->
        if (mediaProps == null || mediaMeta == null) {
            return@combine MusicStatus.NoMusic
        }
        val trackInfo = PlayingTrack(mediaMeta, mediaProps.duration)
        val musicProg = millisProgress.milliseconds
        val prog = if (state == PlaybackState.PLAYING) {
            MusicStatus.Progress.Playing(musicProg)
        } else {
            MusicStatus.Progress.Paused(musicProg)
        }
        MusicStatus.HasMusic(trackInfo, prog)

    }.stateIn(scope, SharingStarted.Eagerly, MusicStatus.NoMusic)

    override fun playReplacing(info: UrlTrack, metadata: TrackMetadata) {
        scope.launch {
            player.features[AudioLevelController.Key]?.let {
                it.setVolume(0.2f * it.maxVolume) // todo
            }
            this@MusicServiceImpl.metadata.value = metadata
            player.playUri(info.uri)
        }
    }

    override fun resume() {
        player.resume()
    }

    override fun pause() {
        if (player.playbackState.value == PlaybackState.PLAYING) {
            player.pause()
        }
    }


}
