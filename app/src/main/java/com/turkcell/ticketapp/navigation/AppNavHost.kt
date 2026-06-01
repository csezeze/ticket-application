package com.turkcell.ticketapp.navigation


import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.turkcell.core.domain.AuthRepository
import com.turkcell.ticketapp.screen.CheckinScreen
import com.turkcell.ticketapp.screen.EventDetailScreen
import com.turkcell.ticketapp.screen.HomeScreen
import com.turkcell.ticketapp.screen.LoginScreen
import com.turkcell.ticketapp.screen.MyPurchasesScreen
import com.turkcell.ticketapp.screen.MyTicketsScreen
import com.turkcell.ticketapp.screen.RegisterScreen
import com.turkcell.ticketapp.screen.TicketDetailScreen
import org.koin.compose.koinInject

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    authRepository: AuthRepository = koinInject()
) {
    val isLoggedIn by produceState<Boolean?>(initialValue = null, authRepository) {
        authRepository.isLoggedIn.collect { loggedIn ->
            value = loggedIn
        }
    }

    if (isLoggedIn == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn == true) Home else Login
    ) {
        composable<Login> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Home) {
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
                    navController.popBackStack()
                }
            )
        }

        composable<EventDetail> { backStackEntry ->
            val eventDetail = backStackEntry.toRoute<EventDetail>()
            EventDetailScreen(
                eventId = eventDetail.id,
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

        composable<TicketDetail> { backStackEntry ->
            val ticketDetail = backStackEntry.toRoute<TicketDetail>()
            TicketDetailScreen(
                ticketId = ticketDetail.id,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
