package com.example.dhis2_flutter_test_impl

import io.flutter.embedding.android.FlutterActivity
import androidx.annotation.NonNull
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.D2Factory
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.user.User

class MainActivity: FlutterActivity() {
    private val CHANNEL = "dhis2.login.channel"
    private var d2: D2? = null

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler {
            call, result ->
            when (call.method) {
                "login" -> {
                    val serverUrl = call.argument<String>("serverUrl")
                    val username = call.argument<String>("username")
                    val password = call.argument<String>("password")

                    if (serverUrl == null || username == null || password == null) {
                        result.error("INVALID_ARGS", "Missing arguments for login", null)
                        return@setMethodCallHandler
                    }

                    // Perform DHIS2 login on a background thread
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            // It's important to get a D2 instance first.
                            // D2Factory.instantiateD2 is blocking, hence Dispatchers.IO
                            d2 = D2Factory.blockingInstantiateD2(serverUrl, null, applicationContext)

                            // Attempt login
                            val user: User = d2!!.userModule().logIn(username, password).blockingGet()

                            // If login is successful, user object will not be null
                            withContext(Dispatchers.Main) {
                                result.success("Login successful for user: ${user.username()}")
                            }
                        } catch (e: D2Error) {
                            e.printStackTrace()
                            // D2Error contains valuable information about the failure
                            val errorCode = e.errorCode()?.name ?: "UNKNOWN_DHIS2_ERROR"
                            val errorDescription = e.errorDescription() ?: e.message ?: "DHIS2 Login Failed"
                            withContext(Dispatchers.Main) {
                                result.error(errorCode, errorDescription, e.httpErrorCode())
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            withContext(Dispatchers.Main) {
                                result.error("NATIVE_ERROR", "An unexpected error occurred: ${e.message}", null)
                            }
                        }
                    }
                }
                "logout" -> {
                     CoroutineScope(Dispatchers.IO).launch {
                        try {
                            d2?.userModule()?.logOut()?.blockingAwait()
                            d2 = null // Clear the d2 instance
                            withContext(Dispatchers.Main) {
                                result.success("Logout successful")
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            withContext(Dispatchers.Main) {
                                 result.error("LOGOUT_ERROR", "Logout failed: ${e.message}", null)
                            }
                        }
                     }
                }
                "isUserLoggedIn" -> {
                    val isLoggedIn = d2?.userModule()?.isLogged()?.blockingGet() ?: false
                    result.success(isLoggedIn)
                }
                else -> {
                    result.notImplemented()
                }
            }
        }
    }
}
