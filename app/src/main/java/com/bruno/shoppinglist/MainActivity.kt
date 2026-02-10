package com.bruno.shoppinglist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bruno.shoppinglist.ui.ErrorScreen
import com.bruno.shoppinglist.ui.HomeScreen
import com.bruno.shoppinglist.ui.ListDetailsScreen
import com.bruno.shoppinglist.ui.theme.ShoppingListTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ShoppingListTheme {
                val navController = rememberNavController()


                NavHost(
                    navController = navController,
                    startDestination = "auth"
                ) {
                    // Home Screen Route
                    composable("home") {
                        HomeScreen(navController = navController)
                    }
                    composable("auth") {
                        LoginScreen(navController) {
                            navController.navigate("home")
                        }
                    }

                    // Details Screen Route with ID Argument
                    composable(
                        route = "list/{listId}",
                        arguments = listOf(navArgument("listId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val listId = backStackEntry.arguments?.getString("listId") ?: ""
                        ListDetailsScreen(
                            listId = listId,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(
    navController: NavController,
    onLoginSuccess: (NavController) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authManager = remember { AuthManager(context) }

    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }

    val attemptSignIn = {
        isLoading = true
        hasError = false
        scope.launch {
            try {
                val user = authManager.googleSignIn()
                if (user != null) {
                    onLoginSuccess(navController)
                } else {
                    isLoading = false // Sign in failed/cancelled
                }
            } catch (_: Exception) {
                isLoading = false
                hasError = true
            }
        }
    }

    LaunchedEffect(Unit) {
        if (authManager.getCurrentUser() != null) {
            onLoginSuccess(navController)
        } else {
            isLoading = false
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            // Show a progress indicator while the Google Bottom Sheet is active
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { attemptSignIn() },
                modifier = Modifier
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                elevation = ButtonDefaults.buttonElevation(4.dp),
            ) {
                // Add the Google Icon
                // Make sure you have a google icon in your res/drawable folder
                // If not, you can use Icons.Default.AccountCircle as a placeholder
                Icon(
                    painter = painterResource(id = R.drawable.google_logo), // Update with your drawable name
                    contentDescription = "Google Logo",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Unspecified // Keeps the original logo colors if it's a multi-color SVG
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = stringResource(R.string.GooglePrompt),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        if (hasError) {
            // Use the ErrorScreen we created earlier
            ErrorScreen(
                errorMessage = stringResource(R.string.ErrorScreen_LoginErrorMessage),
                onRetry = { attemptSignIn() }
            )
        }
    }
}
