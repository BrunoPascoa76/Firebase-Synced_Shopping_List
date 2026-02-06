package com.bruno.shoppinglist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.bruno.shoppinglist.ui.ErrorScreen
import com.bruno.shoppinglist.ui.HomeScreen
import com.bruno.shoppinglist.ui.theme.ShoppingListTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val auth = FirebaseAuth.getInstance()

        setContent {
            ShoppingListTheme {
                var currentUser by remember { mutableStateOf(auth.currentUser) }

                if (currentUser == null) {
                    LoginScreen(onLoginSuccess = {
                        // 3. Update the state! This triggers the UI to switch to HomeScreen
                        currentUser = auth.currentUser
                    })
                } else {
                    HomeScreen(user = currentUser!!)
                }
            }
        }
    }
}

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {}
){
    val context= LocalContext.current
    val scope = rememberCoroutineScope()
    val authManager = remember { AuthManager(context) }

    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }

    val attemptSignIn = {
        isLoading = true
        hasError = false
        scope.launch {
            val user = authManager.googleSignIn()
            if (user != null) {
                onLoginSuccess()
            } else {
                isLoading = false
                hasError = true
            }
        }
    }

    LaunchedEffect(Unit) {
        attemptSignIn()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            // Show a progress indicator while the Google Bottom Sheet is active
            CircularProgressIndicator()
        } else if (hasError) {
            // Use the ErrorScreen we created earlier
            ErrorScreen(
                errorMessage = stringResource(R.string.ErrorScreen_LoginErrorMessage),
                onRetry = { attemptSignIn() }
            )
        }
    }
}
