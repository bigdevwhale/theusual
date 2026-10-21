package app.theusual.ui

import android.widget.Toast
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.theusual.R
import app.theusual.container
import app.theusual.data.Cafe
import app.theusual.data.Settings
import app.theusual.data.Visit
import app.theusual.domain.Rules
import app.theusual.ru
import app.theusual.ui.art.PacketLook
import app.theusual.ui.art.PacketState
import app.theusual.ui.board.BoardScreen
import app.theusual.ui.cafe.CafeScreen
import app.theusual.ui.components.rememberNow
import app.theusual.ui.edit.EditScreen
import app.theusual.ui.onboarding.WelcomeScreen
import app.theusual.ui.settings.SettingsScreen
import app.theusual.ui.share.ShareScreen
import app.theusual.ui.theme.U
import app.theusual.ui.visit.VisitScreen
import app.theusual.ui.visit.WindowScreen
import app.theusual.ui.year.YearScreen
import kotlinx.coroutines.launch

/** Where a café stands with you. */
enum class Standing { REGULAR, VISITED, WANT, CLOSED }

/** Everything written down, plus what follows from it, at one moment. Screens read through here. */
@Immutable
class Home(
    val cafes: List<Cafe>,
    val visits: List<Visit>,
    val settings: Settings,
    val now: Long,
    val ru: Boolean,
) {
    private val byCafe = visits.groupBy { it.cafeId }
    private val byId = cafes.associateBy { it.id }

    fun cafe(id: Long) = byId[id]

    /** A café's cups, the latest first. */
    fun visits(id: Long): List<Visit> = byCafe[id].orEmpty().sortedByDescending { it.at }
    fun count(id: Long) = byCafe[id]?.size ?: 0
    fun last(id: Long): Visit? = byCafe[id]?.maxByOrNull { it.at }
    fun visit(id: Long) = visits.firstOrNull { it.id == id }
    fun window(id: Long) = byCafe[id].orEmpty().sumOf { it.windowMin }

    /** When a café first became yours: the date of its tenth cup. */
    fun regularSince(id: Long): Long? = byCafe[id]?.sortedBy { it.at }?.getOrNull(Rules.REGULAR - 1)?.at

    fun standing(c: Cafe): Standing = when {
        c.closed -> Standing.CLOSED
        Rules.regular(count(c.id)) -> Standing.REGULAR
        count(c.id) > 0 -> Standing.VISITED
        else -> Standing.WANT
    }

    fun usual(c: Cafe) = Rules.usual(c, visits(c.id))

    /** Away from home: the packet gets the city's stamp. */
    fun away(c: Cafe) = c.city.isNotBlank() && settings.city.isNotBlank() && !c.city.trim().equals(settings.city.trim(), ignoreCase = true)

    fun look(c: Cafe, sugar: String) = PacketLook(
        name = c.name, sub = if (standing(c) == Standing.WANT && c.from.isNotBlank()) "" else c.sub, pattern = c.patternOf, palette = c.palette,
        city = if (away(c)) c.city else "",
        state = when (standing(c)) {
            Standing.CLOSED -> PacketState.CLOSED
            Standing.WANT -> PacketState.GHOST
            else -> PacketState.PLAIN
        },
        sugar = sugar,
    )

    /** The board, in order: yours by cups, then the others by the latest cup, then the ones to try, then the closed. */
    val board: List<Cafe> = run {
        val regular = cafes.filter { standing(it) == Standing.REGULAR }.sortedByDescending { count(it.id) }
        val visited = cafes.filter { standing(it) == Standing.VISITED }.sortedByDescending { last(it.id)?.at ?: 0 }
        val want = cafes.filter { standing(it) == Standing.WANT }.sortedByDescending { it.addedAt }
        val closed = cafes.filter { it.closed }.sortedByDescending { it.closedAt }
        regular + visited + want + closed
    }

    val open: List<Cafe> = cafes.filter { !it.closed }

    /** The latest cup of all, and where it was drunk. */
    val latest: Pair<Visit, Cafe>? = visits.maxByOrNull { it.at }?.let { v -> byId[v.cafeId]?.let { v to it } }
    val windowMin: Int = visits.sumOf { it.windowMin }
}

/** Everything a screen can open. */
class Go(private val nav: NavHostController) {
    fun cafe(id: Long) = nav.navigate("cafe/$id") { launchSingleTop = true }
    fun add() = nav.navigate("edit/0") { launchSingleTop = true }
    fun edit(id: Long) = nav.navigate("edit/$id") { launchSingleTop = true }
    fun visit(cafeId: Long) = nav.navigate("visit/$cafeId") { launchSingleTop = true }
    fun window(visitId: Long) = nav.navigate("window/$visitId") { launchSingleTop = true }
    fun share(id: Long) = nav.navigate("share/$id") { launchSingleTop = true }
    fun year() = nav.navigate("year") { launchSingleTop = true }
    fun settings() = nav.navigate("settings") { launchSingleTop = true }
    fun back() { nav.popBackStack() }
    fun board() { nav.popBackStack("board", false) }

    /** A café just written down: its page, instead of the form. */
    fun added(id: Long) = nav.navigate("cafe/$id") { popUpTo("board") }
}

@Composable
fun TheUsualRoot(settings: Settings) {
    val context = ctx()
    val container = context.container
    val nav = rememberNavController()
    val start = remember { if (settings.onboarded) "board" else "welcome" }

    val dao = container.db.dao()
    val cafes by dao.observeCafes().collectAsStateWithLifecycle(initialValue = emptyList())
    val visits by dao.observeVisits().collectAsStateWithLifecycle(initialValue = emptyList())
    val now = rememberNow(60_000)
    val ru = context.ru
    val home = remember(cafes, visits, settings, now, ru) { Home(cafes, visits, settings, now, ru) }
    val go = remember(nav) { Go(nav) }

    // Onboarded elsewhere (the demo seed): leave the welcome screen.
    val route = nav.currentBackStackEntryAsState().value?.destination?.route
    LaunchedEffect(settings.onboarded, route) {
        if (settings.onboarded && route == "welcome") nav.navigate("board") { popUpTo("welcome") { inclusive = true } }
    }
    // A packet shared in by a friend: onto the board, and open its page.
    val packet by container.packet.collectAsStateWithLifecycle()
    LaunchedEffect(packet, settings.onboarded) {
        val p = packet ?: return@LaunchedEffect
        if (!settings.onboarded) return@LaunchedEffect
        container.packet.value = null
        container.scope.launch {
            val id = container.diary.receive(p)
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                val who = p.from.ifBlank { context.getString(R.string.a_friend) }
                Toast.makeText(context, context.getString(R.string.packet_received, who), Toast.LENGTH_LONG).show()
                go.cafe(id)
            }
        }
    }

    Box(Modifier.fillMaxSize().background(U.c.bg)) {
        NavHost(
            navController = nav,
            startDestination = start,
            modifier = Modifier.background(U.c.bg),
            enterTransition = { fadeIn(tween(260)) },
            exitTransition = { fadeOut(tween(180)) },
            popEnterTransition = { fadeIn(tween(260)) },
            popExitTransition = { fadeOut(tween(180)) },
        ) {
            val id = listOf(navArgument("id") { type = NavType.LongType })
            composable("welcome") { WelcomeScreen { nav.navigate("board") { popUpTo("welcome") { inclusive = true } } } }
            composable("board") { BoardScreen(home, go) }
            composable("cafe/{id}", arguments = id) { e -> CafeScreen(e.arguments?.getLong("id") ?: 0L, home, go) }
            composable("edit/{id}", arguments = id) { e -> EditScreen(e.arguments?.getLong("id") ?: 0L, home, go) }
            composable("visit/{id}", arguments = id) { e -> VisitScreen(e.arguments?.getLong("id") ?: 0L, home, go) }
            composable("window/{id}", arguments = id) { e -> WindowScreen(e.arguments?.getLong("id") ?: 0L, home, go) }
            composable("share/{id}", arguments = id) { e -> ShareScreen(e.arguments?.getLong("id") ?: 0L, home, go) }
            composable("year") { YearScreen(home, go) }
            composable("settings") { SettingsScreen(home, go) }
        }
    }
}
