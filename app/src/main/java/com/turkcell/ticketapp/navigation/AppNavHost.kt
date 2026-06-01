package com.turkcell.ticketapp.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.turkcell.ticketapp.screen.EventDetailScreen
import com.turkcell.ticketapp.screen.HomeScreen
import com.turkcell.ticketapp.screen.LoginScreen
import com.turkcell.ticketapp.screen.MyTicketsScreen
import com.turkcell.ticketapp.screen.RegisterScreen
import com.turkcell.ticketapp.screen.TicketDetailScreen

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Login
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
