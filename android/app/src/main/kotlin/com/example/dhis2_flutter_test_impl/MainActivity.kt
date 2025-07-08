package com.example.dhis2_flutter_test_impl

import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.D2Configuration
import org.hisp.dhis.android.core.D2Manager
import org.hisp.dhis.android.core.maintenance.D2Error

class MainActivity : FlutterActivity(), Dhis2LoginApi {

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        Dhis2LoginApi.setUp(flutterEngine.dartExecutor.binaryMessenger, this)
    }

    override fun login(credentials: LoginCredentials, callback: (Result<LoginResult>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val d2Configuration = D2Configuration.builder()
                    .appName("dhis2_flutter_app")
                    .appVersion("1.0.0")
                    .context(applicationContext.applicationContext)
                    .build()
                D2Manager.blockingInstantiateD2(d2Configuration)

                val user = D2Manager.getD2().userModule()
                    .logIn(credentials.username, credentials.password, credentials.serverUrl)
                    .blockingGet()

                val result = LoginResult(
                    username = user.username()!!,
                    successMessage = "Login successful for user: ${user.username()}"
                )
                callback(Result.success(result))

            } catch (e: Exception) {
                val cause = e.cause
                if (cause is D2Error) {
                    callback(Result.failure(Exception("Login Failed: ${cause.errorDescription()}")))
                } else {
                    callback(Result.failure(Exception("An unexpected native error occurred: ${e.message}")))
                }
            }
        }
    }

    override fun isUserLoggedIn(callback: (Result<Boolean>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val isLoggedIn = if (D2Manager.isD2Instantiated()) {
                D2Manager.getD2().userModule().isLogged().blockingGet() ?: false
            } else {
                false
            }
            callback(Result.success(isLoggedIn))
        }
    }

    override fun logout(callback: (Result<Unit>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (D2Manager.isD2Instantiated()) {
                    D2Manager.getD2().userModule().logOut().blockingAwait()
                }
                callback(Result.success(Unit))
            } catch (e: Exception) {
                callback(Result.failure(Exception("Logout failed: ${e.message}")))
            }
        }
    }
}