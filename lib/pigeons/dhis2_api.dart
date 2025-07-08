import 'package:pigeon/pigeon.dart';

@ConfigurePigeon(PigeonOptions(
  dartOut: 'lib/generated/dhis2_api.g.dart',
  dartPackageName: 'dhis2_flutter_test_impl',
  kotlinOut:
  'android/app/src/main/kotlin/com/example/dhis2_flutter_test_impl/Dhis2Api.g.kt',
  kotlinOptions: KotlinOptions(
    package: 'com.example.dhis2_flutter_test_impl',
    errorClassName: 'Dhis2PigeonError',
  ),
))

class LoginCredentials {
  LoginCredentials({
    required this.serverUrl,
    required this.username,
    required this.password,
  });
  final String serverUrl;
  final String username;
  final String password;
}

class LoginResult {
  LoginResult({required this.username, required this.successMessage});
  final String username;
  final String successMessage;
}

@HostApi()
abstract class Dhis2LoginApi {
  @async
  LoginResult login(LoginCredentials credentials);

  @async
  bool isUserLoggedIn();

  @async
  void logout();
}