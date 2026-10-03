package io.github.rwx.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import io.github.rwx.ui.generated.resources.Res
import io.github.rwx.ui.generated.resources.add
import io.github.rwx.ui.generated.resources.addPerson
import io.github.rwx.ui.generated.resources.apply
import io.github.rwx.ui.generated.resources.back
import io.github.rwx.ui.generated.resources.campaign
import io.github.rwx.ui.generated.resources.challenge
import io.github.rwx.ui.generated.resources.change_team
import io.github.rwx.ui.generated.resources.close
import io.github.rwx.ui.generated.resources.`continue`
import io.github.rwx.ui.generated.resources.delete
import io.github.rwx.ui.generated.resources.disable
import io.github.rwx.ui.generated.resources.discord
import io.github.rwx.ui.generated.resources.display
import io.github.rwx.ui.generated.resources.exit
import io.github.rwx.ui.generated.resources.filter
import io.github.rwx.ui.generated.resources.gameplay
import io.github.rwx.ui.generated.resources.github
import io.github.rwx.ui.generated.resources.help
import io.github.rwx.ui.generated.resources.`import`
import io.github.rwx.ui.generated.resources.`interface`
import io.github.rwx.ui.generated.resources.license
import io.github.rwx.ui.generated.resources.map
import io.github.rwx.ui.generated.resources.mods
import io.github.rwx.ui.generated.resources.multiplayer
import io.github.rwx.ui.generated.resources.options
import io.github.rwx.ui.generated.resources.qq
import io.github.rwx.ui.generated.resources.refresh
import io.github.rwx.ui.generated.resources.replay
import io.github.rwx.ui.generated.resources.room_password
import io.github.rwx.ui.generated.resources.sandbox
import io.github.rwx.ui.generated.resources.save
import io.github.rwx.ui.generated.resources.search
import io.github.rwx.ui.generated.resources.send
import io.github.rwx.ui.generated.resources.settings
import io.github.rwx.ui.generated.resources.skirmish
import io.github.rwx.ui.generated.resources.sort
import io.github.rwx.ui.generated.resources.start
import io.github.rwx.ui.generated.resources.surrender
import io.github.rwx.ui.generated.resources.survival
import io.github.rwx.ui.generated.resources.team
import io.github.rwx.ui.generated.resources.theme
import io.github.rwx.ui.generated.resources.version
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

internal val Icon.drawable: DrawableResource
    get() = when (this) {
        Icon.Add -> Res.drawable.add
        Icon.AddPerson -> Res.drawable.addPerson
        Icon.Apply -> Res.drawable.apply
        Icon.Back -> Res.drawable.back
        Icon.Campaign -> Res.drawable.campaign
        Icon.Challenge -> Res.drawable.challenge
        Icon.ChangeTeam -> Res.drawable.change_team
        Icon.Close -> Res.drawable.close
        Icon.Continue -> Res.drawable.`continue`
        Icon.Delete -> Res.drawable.delete
        Icon.Disable -> Res.drawable.disable
        Icon.Display -> Res.drawable.display
        Icon.Discord -> Res.drawable.discord
        Icon.Exit -> Res.drawable.exit
        Icon.Filter -> Res.drawable.filter
        Icon.Gameplay -> Res.drawable.gameplay
        Icon.Github -> Res.drawable.github
        Icon.Help -> Res.drawable.help
        Icon.Import -> Res.drawable.`import`
        Icon.Interface -> Res.drawable.`interface`
        Icon.License -> Res.drawable.license
        Icon.Map -> Res.drawable.map
        Icon.Mods -> Res.drawable.mods
        Icon.Multiplayer -> Res.drawable.multiplayer
        Icon.Options -> Res.drawable.options
        Icon.Password -> Res.drawable.room_password
        Icon.Refresh -> Res.drawable.refresh
        Icon.Sandbox -> Res.drawable.sandbox
        Icon.Save -> Res.drawable.save
        Icon.Search -> Res.drawable.search
        Icon.Send -> Res.drawable.send
        Icon.Settings -> Res.drawable.settings
        Icon.Skirmish -> Res.drawable.skirmish
        Icon.Sort -> Res.drawable.sort
        Icon.Start -> Res.drawable.start
        Icon.Surrender -> Res.drawable.surrender
        Icon.Survival -> Res.drawable.survival
        Icon.Team -> Res.drawable.team
        Icon.ColorScheme -> Res.drawable.theme
        Icon.Qq -> Res.drawable.qq
        Icon.Replay -> Res.drawable.replay
        Icon.Version -> Res.drawable.version
    }

@Composable
internal fun AssetIcon(icon: Icon, size: Dp, tint: Color, modifier: Modifier = Modifier) {
    Image(
        painterResource(icon.drawable),
        contentDescription = null,
        colorFilter = ColorFilter.tint(tint),
        modifier = modifier.size(size).testTag("icon:${icon.name}"),
    )
}
