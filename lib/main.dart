import 'package:cowin_appoinment/page/home_page.dart';
import 'package:cowin_appoinment/page/message.dart';
import 'package:cowin_appoinment/utility/utility.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_messaging/firebase_messaging.dart';

Future<void> _firebaseMessagingBackgroundHandler(RemoteMessage message) async {
  // If you're going to use other Firebase services in the background, such as Firestore,
  // make sure you call `initializeApp` before using other Firebase services.
  await Firebase.initializeApp();
  print('Handling a background message ${message.messageId}');
}

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Firebase.initializeApp();
  FirebaseMessaging.onBackgroundMessage(_firebaseMessagingBackgroundHandler);
  await FirebaseMessaging.instance.setForegroundNotificationPresentationOptions(
    alert: true,
    badge: true,
    sound: true,
  );

  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Vaccinate.me',
      debugShowCheckedModeBanner: false,
      theme: ThemeData.light().copyWith(
          primaryColor: Colors.indigo,
          accentColor: Colors.black,
          backgroundColor: Colors.white30,
          buttonTheme: ButtonThemeData(
              buttonColor: Colors.amber, disabledColor: Colors.black),
          buttonColor: Colors.indigo),
      darkTheme: ThemeData.dark().copyWith(
          primaryColor: Colors.black,
          accentColor: Colors.indigo,
          backgroundColor: Colors.black,
          canvasColor: Colors.black,
          buttonColor: Colors.white,
          textTheme: TextTheme(
            bodyText2: TextStyle(color: Colors.white),
            bodyText1: TextStyle(color: Colors.white),
            headline1: TextStyle(color: Colors.white),
            headline2: TextStyle(color: Colors.white),
            headline4: TextStyle(color: Colors.white),
            headline3: TextStyle(color: Colors.white),
            headline5: TextStyle(color: Colors.white),
            headline6: TextStyle(color: Colors.white),
            button: TextStyle(color: Colors.white),
          ),
          buttonTheme: ButtonThemeData(
              buttonColor: Colors.indigo, textTheme: ButtonTextTheme.primary)),
      // NOTE: Optional - use themeMode to specify the startup theme
      themeMode: ThemeMode.system,
      routes: {
        '/': (context) => HomePage(title: 'Vaccinate.me'),
        '/message': (context) => MessageView(),
      },
    );
  }
}
