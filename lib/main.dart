import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import 'generated/dhis2_api.g.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'DHIS2 Login Test',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        primarySwatch: Colors.blue,
        visualDensity: VisualDensity.adaptivePlatformDensity,
      ),
      home: const LoginPage(),
    );
  }
}

class LoginPage extends StatefulWidget {
  const LoginPage({super.key});

  @override
  State<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  final Dhis2LoginApi _dhis2Api = Dhis2LoginApi();

  final _formKey = GlobalKey<FormState>();
  final _serverUrlController = TextEditingController();
  final _usernameController = TextEditingController();
  final _passwordController = TextEditingController();

  String _loginStatus = 'Not logged in';
  bool _isLoading = false;
  bool _isLoggedIn = false;

  @override
  void initState() {
    super.initState();
    _serverUrlController.text = 'https://play.im.dhis2.org/stable-2-41-4-1';
    _usernameController.text = 'admin';
    _passwordController.text = 'district';

    _checkLoginStatus();
  }

  Future<void> _checkLoginStatus() async {
    try {
      final bool isLoggedIn = await _dhis2Api.isUserLoggedIn();
      setState(() {
        _isLoggedIn = isLoggedIn;
        _loginStatus = isLoggedIn ? 'User is logged in' : 'Not logged in';
      });
    } on PlatformException catch (e) {
      setState(() {
        _loginStatus = "Failed to check status: '${e.message}'.";
      });
    }
  }

  Future<void> _login() async {
    if (_formKey.currentState!.validate()) {
      setState(() {
        _isLoading = true;
        _loginStatus = 'Logging in...';
      });

      try {
        final credentials = LoginCredentials(
          serverUrl: _serverUrlController.text.trim(),
          username: _usernameController.text.trim(),
          password: _passwordController.text,
        );

        final LoginResult result = await _dhis2Api.login(credentials);

        setState(() {
          _loginStatus = result.successMessage;
          _isLoggedIn = true;
        });
      } on PlatformException catch (e) {
        setState(() {
          _loginStatus = "Login Failed: ${e.message}";
          _isLoggedIn = false;
        });
      } catch (e) {
        setState(() {
          _loginStatus = "An unexpected error occurred: $e";
          _isLoggedIn = false;
        });
      } finally {
        setState(() {
          _isLoading = false;
        });
      }
    }
  }

  Future<void> _logout() async {
    setState(() => _isLoading = true);
    try {
      await _dhis2Api.logout();
      setState(() {
        _loginStatus = 'Logout successful';
        _isLoggedIn = false;
      });
    } on PlatformException catch (e) {
      setState(() => _loginStatus = "Logout Failed: ${e.message}");
    } finally {
      setState(() => _isLoading = false);
    }
  }

  @override
  void dispose() {
    _serverUrlController.dispose();
    _usernameController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('DHIS2 Android Sdk in Flutter'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(32.0),
        child: Form(
          key: _formKey,
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: <Widget>[
              TextFormField(
                controller: _serverUrlController,
                decoration: const InputDecoration(labelText: 'Server URL'),
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'Please enter server URL';
                  }
                  if (!value.startsWith('http://') &&
                      !value.startsWith('https://')) {
                    return 'URL must start with http:// or https://';
                  }
                  return null;
                },
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _usernameController,
                decoration: const InputDecoration(labelText: 'Username'),
                validator: (value) => value == null || value.isEmpty
                    ? 'Please enter username'
                    : null,
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _passwordController,
                decoration: const InputDecoration(labelText: 'Password'),
                obscureText: true,
                validator: (value) => value == null || value.isEmpty
                    ? 'Please enter password'
                    : null,
              ),
              const SizedBox(height: 24),
              if (_isLoading)
                const Center(child: CircularProgressIndicator())
              else
                ElevatedButton(
                  onPressed: _isLoggedIn ? _logout : _login,
                  child: Text(_isLoggedIn ? 'Logout' : 'Login'),
                ),
              const SizedBox(height: 20),
              Text(
                _loginStatus,
                textAlign: TextAlign.center,
                style: TextStyle(
                  color: _loginStatus.contains('Failed') ||
                          _loginStatus.contains('Not logged in')
                      ? Colors.red
                      : Colors.green,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
