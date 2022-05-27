package com.nhnextsoft.qrcode.ui.feature.entry

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.nhnextsoft.qrcode.ui.feature.NavigationScreens
import com.nhnextsoft.qrcode.ui.screen.generate.GenerateContract
import com.nhnextsoft.qrcode.ui.screen.generate.GenerateScreen
import com.nhnextsoft.qrcode.ui.screen.generate.GenerateViewModel
import com.nhnextsoft.qrcode.ui.screen.history.HistoryContract
import com.nhnextsoft.qrcode.ui.screen.history.HistoryScreen
import com.nhnextsoft.qrcode.ui.screen.history.HistoryViewModel
import com.nhnextsoft.qrcode.ui.screen.historycreateresult.HistoryCreateResult
import com.nhnextsoft.qrcode.ui.screen.historycreateresult.HistoryCreateResultViewModel
import com.nhnextsoft.qrcode.ui.screen.scancode.ScanCodeContract
import com.nhnextsoft.qrcode.ui.screen.scancode.ScanCodeScreen
import com.nhnextsoft.qrcode.ui.screen.scancode.ScanCodeViewModel
import com.nhnextsoft.qrcode.ui.screen.scanresult.ScanResultContract
import com.nhnextsoft.qrcode.ui.screen.scanresult.ScanResultScreen
import com.nhnextsoft.qrcode.ui.screen.scanresult.ScanResultViewModel
import com.nhnextsoft.qrcode.ui.screen.settings.SettingScreen
import com.nhnextsoft.qrcode.ui.screen.settings.SettingViewModel
import com.nhnextsoft.qrcode.ui.theme.ColorButton
import com.nhnextsoft.qrcode.ui.theme.ColorMain
import com.nhnextsoft.qrcode.ui.theme.QRCodeTheme
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber


@ExperimentalAnimationApi
@AndroidEntryPoint
class QrEntryPointCodeActivity : ComponentActivity() {

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, QrEntryPointCodeActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            QRCodeTheme(darkTheme = false) {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    QrCodeMainScreen()
                }
            }
        }
    }
}

@ExperimentalAnimationApi
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    QRCodeTheme {
        QrCodeMainScreen()
    }
}


@ExperimentalAnimationApi
@Composable
private fun QrCodeMainScreen() {
    val navController = rememberNavController()


    val bottomNavigationItems = listOf(
        NavigationScreens.ScanCode,
//        NavigationScreens.GenerateCode,
        NavigationScreens.History,
//        NavigationScreens.Setting,
    )
    navController.enableOnBackPressed(false)
    var isShowDialogExit by remember { mutableStateOf(false) }
    val bottomBarState = rememberSaveable { (mutableStateOf(true)) }
    val context = LocalContext.current
    Scaffold(
        bottomBar = {
            QrCodeAppBottomNavigation(navController, bottomNavigationItems, bottomBarState)
        },
    ) { innerPadding ->
        QrCodeMainScreenNavigationConfigurations(
            navController = navController,
            onBackHandler = {
            }
        ) { state ->
            bottomBarState.value = state
        }
    }

    navController.addOnDestinationChangedListener { controller, destination, arguments ->
        val params = Bundle()
    }
}

@ExperimentalAnimationApi
@Composable
fun QrCodeAppBottomNavigation(
    navController: NavHostController,
    listBottomNavigationScreens: List<NavigationScreens>,
    bottomBarState: MutableState<Boolean>,
) {

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    Timber.d("bottomBarState ${bottomBarState.value}")
    val iconColorNormal = ColorMain
    val iconColorSelected = ColorButton
    AnimatedVisibility(visible = bottomBarState.value,
        content = {
            Column(modifier = Modifier.fillMaxWidth()) {
                BottomNavigation(backgroundColor = Color.White, elevation = 5.dp) {
                    Timber.d("currentRoute $currentRoute")
                    listBottomNavigationScreens.forEach { screen ->
                        BottomNavigationItem(
                            icon = {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = screen.iconResourceId),
                                    contentDescription = "screenName-${screen.router}",
                                )
                            },
                            selected = currentRoute == screen.router,
                            selectedContentColor = iconColorSelected,
                            unselectedContentColor = iconColorNormal,
                            alwaysShowLabel = false,
                            onClick = {
                                if (currentRoute != screen.router) {
                                    navController.navigate(screen.router)
                                }
                            }
                        )
                    }
                }
            }

        })

}

@Composable
private fun QrCodeMainScreenNavigationConfigurations(
    navController: NavHostController,
    onBackHandler: () -> Unit,
    buttonStateChange: ((Boolean)) -> Unit,
) {
    NavHost(navController, startDestination = NavigationScreens.ScanCode.router) {
        composable(route = NavigationScreens.ScanCode.router) {
            BackHandler(true) { onBackHandler.invoke() }
            QrCodeScanningDestination(navController)
            buttonStateChange.invoke(true)
        }
        composable(route = NavigationScreens.GenerateCode.router) {
            BackHandler(true) { onBackHandler.invoke() }
            QrCodeGenerateCodeDestination(navController,
                NavigationScreens.GenerateCode.router)
            buttonStateChange.invoke(true)
        }
        composable(route = NavigationScreens.History.router) {
            BackHandler(true) { onBackHandler.invoke() }
            val historyEntry = remember {
                navController.getBackStackEntry(NavigationScreens.History.router)
            }
            val historyViewModel = hiltViewModel<HistoryViewModel>(historyEntry)
            QrCodeHistoryDestination(navController, historyViewModel)
            buttonStateChange.invoke(true)
        }
        composable(route = NavigationScreens.Setting.router) {
            BackHandler(true) { onBackHandler.invoke() }
            QrCodeSettingDestination(navController)
            buttonStateChange.invoke(true)
        }
        composable(
            route = NavigationScreens.ScanResult.router + "/{${NavigationScreens.Arg.HISTORY_SCAN_ID}}" + "/{${NavigationScreens.Arg.HISTORY_SCAN_SHOW_IMAGE}}",
            arguments = listOf(
                navArgument(NavigationScreens.Arg.HISTORY_SCAN_ID) {
                    type = NavType.LongType
                },
                navArgument(NavigationScreens.Arg.HISTORY_SCAN_SHOW_IMAGE) {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) {
            BackHandler(false) {}
            QrCodeScanResultScreenDestination(navController)
            buttonStateChange.invoke(false)
        }

        composable(
            route = NavigationScreens.HistoryCreate.router + "/{${NavigationScreens.Arg.HISTORY_CREATE_ID}}",
            arguments = listOf(navArgument(NavigationScreens.Arg.HISTORY_CREATE_ID) {
                type = NavType.LongType
            })
        ) {
            BackHandler(false) {}
            QrCodeHistoryCreateResultDestination(navController)
            buttonStateChange.invoke(false)
        }
    }
}


/**
 *
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun QrCodeScanningDestination(navController: NavHostController) {
    val viewModel: ScanCodeViewModel = hiltViewModel()
    val state = viewModel.viewState.value
    ScanCodeScreen(
        state = state,
        effectFlow = viewModel.effect,
        onEventSent = { event -> viewModel.setEvent(event) },
        onNavigationRequested = { navigationEffect ->
            Timber.d("onNavigationRequested $navigationEffect")
            when (navigationEffect) {
                is ScanCodeContract.Effect.Navigation.NavigateToScanResult -> {
                    navController.navigate(NavigationScreens.ScanResult.router + "/" + navigationEffect.historyScanId + "/" + true)
                }
            }
        }
    )
}


/**
 *
 */
@Composable
private fun QrCodeHistoryDestination(
    navController: NavHostController,
    historyViewModel: HistoryViewModel,
) {
    val state = historyViewModel.viewState.value
    HistoryScreen(
        state = state,
        effectFlow = historyViewModel.effect,
        onEventSent = { event -> historyViewModel.setEvent(event) },
        onNavigationRequested = { navigationEffect ->
            Timber.d("onNavigationRequested $navigationEffect")
            when (navigationEffect) {
                is HistoryContract.Effect.Navigation.NavigateToScanResult -> {
                    navController.navigate(NavigationScreens.ScanResult.router + "/" + navigationEffect.historyScanId + "/" + false)
                }
                is HistoryContract.Effect.Navigation.NavigateToCreateHistory -> {
                    navController.navigate(NavigationScreens.HistoryCreate.router + "/" + navigationEffect.historyCreateId)
                }
            }
        }
    )
}

/**
 *
 */
@Composable
private fun QrCodeGenerateCodeDestination(navController: NavHostController, screenName: String) {
    val viewModel: GenerateViewModel = hiltViewModel()
    val state = viewModel.viewState.value
    GenerateScreen(
        state = state,
        effectFlow = viewModel.effect,
        onEventSent = { event -> viewModel.setEvent(event) },
        onNavigationRequested = { navigationEffect ->
            when (navigationEffect) {
                is GenerateContract.Effect.Navigation.NavigateToCreateHistory -> {
                    navController.popBackStack()
                    navController.navigate(NavigationScreens.HistoryCreate.router + "/" + navigationEffect.historyCreateId)
                }

            }
        }
    )
}

@Composable
private fun QrCodeSettingDestination(navController: NavHostController) {
    val viewModel: SettingViewModel = hiltViewModel()
    val state = viewModel.viewState.value
    SettingScreen(
        state = state,
        effectFlow = viewModel.effect,
        onEventSent = { event -> viewModel.setEvent(event) },
        onNavigationRequested = {

        }
    )
}

@Composable
private fun QrCodeScanResultScreenDestination(navController: NavHostController) {
    val viewModel: ScanResultViewModel = hiltViewModel()
    val state = viewModel.viewState.value
    ScanResultScreen(
        state = state,
        effectFlow = viewModel.effect,
        onEventSent = { event -> viewModel.setEvent(event) },
        onNavigationRequested = { navigationEffect ->
            when (navigationEffect) {
                is ScanResultContract.Effect.Navigation.NavigateToScanNow -> {
                    navController.popBackStack()
                    navController.navigate(NavigationScreens.ScanCode.router)
                }
            }
        }
    )
}

@Composable
fun QrCodeHistoryCreateResultDestination(navController: NavHostController) {
    val viewModel: HistoryCreateResultViewModel = hiltViewModel()
    val state = viewModel.viewState.value
    HistoryCreateResult(
        state = state,
        effectFlow = viewModel.effect,
        onEventSent = { event -> viewModel.setEvent(event) },
        onNavigationRequested = { navigationEffect ->
            when (navigationEffect) {
                is ScanResultContract.Effect.Navigation.NavigateToBackHistory -> {
                    navController.clearBackStack(NavigationScreens.HistoryCreate.router)
                    navController.navigate(NavigationScreens.History.router)
                }
            }
        }
    )
}



