package com.turkcell.ticketapp.navigation


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.turkcell.core.domain.AuthRepository
import com.turkcell.core.domain.UserRole
import com.turkcell.ticketapp.screen.CheckinScreen
import com.turkcell.ticketapp.screen.EventDetailScreen
import com.turkcell.ticketapp.screen.HomeScreen
import com.turkcell.ticketapp.screen.LoginScreen
import com.turkcell.ticketapp.screen.MyPurchasesScreen
import com.turkcell.ticketapp.screen.MyTicketsScreen
import com.turkcell.ticketapp.screen.RegisterScreen
import com.turkcell.ticketapp.screen.TicketDetailScreen
import kotlinx.coroutines.flow.combine
import org.koin.compose.koinInject

private data class AuthNavigationState(
    val isLoggedIn: Boolean,
    val userRole: UserRole?
)

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    authRepository: AuthRepository = koinInject()
) {
    val authState by produceState(
        initialValue = AuthNavigationState(isLoggedIn = false, userRole = null),
        authRepository
    ) {
        combine(
            authRepository.isLoggedIn,
            authRepository.currentUserRole
        ) { loggedIn, userRole ->
            AuthNavigationState(
                isLoggedIn = loggedIn,
                userRole = userRole
            )
        }.collect { state ->
            value = state
        }
    }

    LaunchedEffect(authState) {
        val isOnLogin = navController.currentBackStackEntry
            ?.destination
            ?.route == Login::class.qualifiedName

        if (authState.isLoggedIn && isOnLogin) {
            navController.navigate(destinationForRole(authState.userRole)) {
                popUpTo(Login) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Login
    ) {
        composable<Login> {
            LoginScreen(
                onLoginSuccess = { userRole ->
                    navController.navigate(destinationForRole(userRole)) {
                        popUpTo(Login) {
                            inclusive = true
                        }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Register)
                }
            )
        }

        composable<Register> {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable<Home> {
            HomeScreen(
                onEventClick = { eventId ->
                    navController.navigate(EventDetail(eventId))
                },
                onMyTicketsClick = {
                    navController.navigate(MyTickets)
                },
                onMyPurchasesClick = {
                    navController.navigate(MyPurchases)
                },
                onCheckinClick = {
                    navController.navigate(Checkin)
                },
                onLogoutSuccess = {
                    navController.navigate(Login) {
                        popUpTo(Home) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<MyTickets> {
            MyTicketsScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onTicketClick = { ticketId ->
                    navController.navigate(TicketDetail(ticketId))
                }
            )
        }

        composable<MyPurchases> {
            MyPurchasesScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onPaymentSuccess = {
                    navController.navigate(MyTickets) {
                        popUpTo(MyPurchases) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<Checkin> {
            CheckinScreen(
                onBackClick = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Login) {
                            popUpTo(Checkin) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable<EventDetail> {
            EventDetailScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onPaymentSuccess = {
                    navController.navigate(MyTickets) {
                        popUpTo(Home) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<TicketDetail> {
            TicketDetailScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

private fun destinationForRole(userRole: UserRole?): Any {
    return when (userRole) {
        UserRole.STAFF,
        UserRole.ADMIN -> Checkin
        UserRole.USER,
        null -> Home
    }
}
