package net.leloubil.werewolvesassistant.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.composeunstyled.Text
import kotlinx.coroutines.launch
import net.leloubil.werewolvesassistant.modules.*
import net.leloubil.werewolvesassistant.ui.theme.Button
import org.koin.compose.koinInject
import org.koin.core.qualifier.named
import werewolvesassistant.composeapp.generated.resources.Res

@Composable
fun MusicPlayer(music: SoundsMusic) {
    val player = koinInject<MusicService>(named<BackgroundMusicPlayer>())
    val scope = rememberCoroutineScope()

    Button(onClick = {
        scope.launch {
            player.playReplacing(Track.Data(Res.readBytes(music.path)), TrackMetadata.FullMetadata(music.name))
        }
    }) {
        Text("Play music")
    }
}

@Composable
fun MusicPlayer(music: SoundsSFX, onClick: () -> Unit = {}) {
    val player = koinInject<MusicService>(named<SFXPlayer>())
    val scope = rememberCoroutineScope()

    Button(onClick = {
        scope.launch {
            player.playReplacing(Track.Data(Res.readBytes(music.path)), TrackMetadata.FullMetadata(music.name))
        }
        onClick()
    }) {
        Text("Play SFX")
    }
}


enum class SoundsMusic(val path: String) {
    FOREST("files/sounds/music/normalized/forest.mp3"),
    HARP("files/sounds/music/normalized/harp.mp3"),
    MIDNIGHT("files/sounds/music/normalized/midnight.mp3"),
    MOONLIGHT("files/sounds/music/normalized/moonlight.mp3"),
    PRESSURE("files/sounds/music/normalized/pressure.mp3"),
    TREASON("files/sounds/music/normalized/treason.mp3"),
    WHO_SHOULD_WE_KILL("files/sounds/music/normalized/whoshouldwekill.mp3")
}

enum class SoundsSFX(val path: String) {
    ACTION_REQUIRE("files/sounds/sfx/normalized/action_require.mp3"),
    BEGIN_DAY("files/sounds/sfx/normalized/begin_day.mp3"),
    BIP("files/sounds/sfx/normalized/bip.mp3"),
    LIGHTNING("files/sounds/sfx/normalized/lightning.mp3"),
    POPUP("files/sounds/sfx/normalized/popup.mp3"),
    SHOTGUN("files/sounds/sfx/normalized/shotgun.mp3"),
    VICTORY("files/sounds/sfx/normalized/victory.mp3"),
    DEFEAT("files/sounds/sfx/normalized/defeat.mp3"),
    WEREWOLVES_TIME("files/sounds/sfx/normalized/werewolves_time.mp3")
}
