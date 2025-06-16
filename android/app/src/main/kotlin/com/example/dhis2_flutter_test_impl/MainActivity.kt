package com.example.dhis2_flutter_test_impl

import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.D2Configuration
import org.hisp.dhis.android.core.D2Manager
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.user.User

class MainActivity: FlutterActivity() {
    private val CHANNEL = "dhis2.login.channel"

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

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val d2Configuration = D2Configuration.builder()
                                .appName("dhis2_flutter_app")
                                .appVersion("1.0.0")
                                .readTimeoutInSeconds(120)
                                .connectTimeoutInSeconds(120)
                                .writeTimeoutInSeconds(120)
                                .context(applicationContext.applicationContext)
                                .build()

                            D2Manager.blockingInstantiateD2(d2Configuration)

                            val d2Instance: D2 = D2Manager.getD2()

                            val user: User = d2Instance.userModule().logIn(username, password, serverUrl).blockingGet()

                            withContext(Dispatchers.Main) {
                                result.success("Login successful for user: ${user.username()}")
                            }
                        } catch (e: D2Error) {
                            e.printStackTrace()
                            val errorCode = e.errorCode()?.name ?: "UNKNOWN_DHIS2_ERROR"
                            val errorDescription = e.errorDescription() ?: e.message ?: "DHIS2 Login Failed"
                            withContext(Dispatchers.Main) {
                                result.error(errorCode, errorDescription, e.httpErrorCode())
                            }
                        } catch (e: IllegalStateException) {
                            e.printStackTrace()
                            withContext(Dispatchers.Main) {
                                result.error("D2_NOT_INITIALIZED", "DHIS2 SDK not initialized: ${e.message}", null)
                            }
                        }
                        catch (e: Exception) {
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
                            if (D2Manager.isD2Instantiated()) {
                                val d2Instance = D2Manager.getD2()
                                d2Instance.userModule().logOut()?.blockingAwait()
                            }
                            withContext(Dispatchers.Main) {
                                result.success("Logout successful or D2 not instantiated")
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
                    var isLoggedIn = false
                    if (D2Manager.isD2Instantiated()) {
                        try {
                            val d2Instance = D2Manager.getD2()
                            isLoggedIn = d2Instance.userModule()?.isLogged()?.blockingGet() ?: false
                        } catch (e: IllegalStateException) {
                            isLoggedIn = false
                        }
                    }
                    result.success(isLoggedIn)
                }
                else -> {
                    result.notImplemented()
                }
            }
        }
    }
}
