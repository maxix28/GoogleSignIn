package com.example.googlesingin

import android.app.Activity.RESULT_OK
import android.content.IntentSender
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.googlesingin.presentation.sign_in.GoogleAuthUiClient
import com.example.googlesingin.presentation.sign_in.ProfileScreen
import com.example.googlesingin.presentation.sign_in.SignInScreen
import com.example.googlesingin.presentation.sign_in.SignInViewModel
import com.example.googlesingin.ui.theme.GoogleSingInTheme
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    private val googleAuthUiClient by lazy{
        GoogleAuthUiClient(
        applicationContext,
            Identity.getSignInClient(applicationContext)
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GoogleSingInTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "sign_in"){
                        composable("sign_in"){
                            val viewModel = viewModel<SignInViewModel>()
                            val state by viewModel.state.collectAsStateWithLifecycle()
//                            LaunchedEffect(key1 = Unit ){
//                                if (googleAuthUiClient.getSingedInUser() != null){
//                                    navController.navigate("profile")
//
//                                }
//                            }


                            val launcher = rememberLauncherForActivityResult(contract =
                            ActivityResultContracts.StartIntentSenderForResult()
                                , onResult = {result->
                                    if(result.resultCode == RESULT_OK){
                                        lifecycleScope.launch{
                                            val signInResult = googleAuthUiClient.signInWithIntent(
                                                intent = result.data ?: return@launch
                                            )
                                            viewModel.onSignInResult(signInResult)
                                        }

                                    }
                                }
                            )
                            
                            
                            LaunchedEffect(key1 = state.isSignInSuccessful ){
                                if(state.isSignInSuccessful){
                                    Toast.makeText(
                                        applicationContext, "Sign in Successfull", Toast.LENGTH_LONG
                                    ).show()

                                    navController.navigate("profile")
                                    viewModel.resetState()
                                }
                            }
                            
                            SignInScreen(state,
                                {
                                    lifecycleScope.launch {
                                        val sinsnInIntentSender = googleAuthUiClient.signIn()
                                        launcher.launch(
                                            IntentSenderRequest.Builder(
                                                sinsnInIntentSender?: return@launch
                                            ).build()
                                        )

                                    }
                                })


                        }
                        
                        composable(route= "profile" ){
                            ProfileScreen(userData = googleAuthUiClient.getSingedInUser() ) {
                                lifecycleScope.launch{
                                    googleAuthUiClient.signIn()
                                    Toast.makeText(applicationContext,"Signed out",Toast.LENGTH_LONG).show()

                                    navController.popBackStack()
                                }

                            }




                        }

                    }

                }
            }
        }
    }
}



//
//@Composable
//fun myApp(){
//    val navController = rememberNavController()
//    NavHost(navController = navController, startDestination = "sign_in"){
//        composable("sign_in"){
//            val viewModel = viewModel<SignInViewModel>()
//            val state by viewModel.state.collectAsStateWithLifecycle()
//
//
//            val launcher = rememberLauncherForActivityResult(contract =
//                ActivityResultContracts.StartIntentSenderForResult()
//                , onResult = {result->
//                    if(result.resultCode == RESULT_OK){
//                         lifecycleScope.launch{
//
//                        }
//
//                    }
//                })
//        }
//    }
//
//}
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GoogleSingInTheme {
        Greeting("Android")
    }
}