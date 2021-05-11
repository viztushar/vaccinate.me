import 'package:cowin_appoinment/page/message.dart';
import 'package:cowin_appoinment/utility/utility.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:shared_preferences/shared_preferences.dart';

class HomePage extends StatefulWidget {
  HomePage({Key key, this.title}) : super(key: key);

  final String title;
  @override
  _HomePageState createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  static const platform =
      const MethodChannel('dev.viztushar.vaccinateme/check');

  bool searchbypin = true;
  bool searchbyDistrict = false;
  bool eighteenPlus = false;
  bool fortyfivePlus = false;
  bool covishield = false;
  bool covaxin = false;
  bool free = false;
  bool paid = false;
  bool service = false;
  String selectedstate = null;
  String selectedDistrict = null;
  String selectedCheckVaccineAvailability =
      Utility().checkVaccineAvailability[2];
  TextEditingController pinController = TextEditingController();
  TextEditingController emailController = TextEditingController();
  List<Map<String, Object>> district = [];
  List<Map<String, Object>> state = Utility().state;
  final _formPinKey = new GlobalKey<FormState>();
  final _formWhatsAppKey = new GlobalKey<FormState>();
  SharedPreferences prefs;

  @override
  void initState() {
    super.initState();

    Future.delayed(Duration(milliseconds: 300), () async {
      await _firebaseConfig();
      prefs = await SharedPreferences.getInstance();
      print(prefs.getString("searchBy"));
      if (prefs.getString("searchBy").toString().compareTo('pin') == 0) {
        searchbypin = true;
        searchbyDistrict = false;
      } else if (prefs.getString("searchBy").toString().compareTo('pin') != 0) {
        searchbypin = false;
        searchbyDistrict = true;
      }
      if (prefs.getString("pincode") != null)
        pinController.text = prefs.getString("pincode");

      if (prefs.getBool("18") != null) eighteenPlus = prefs.getBool("18");
      if (prefs.getBool("45") != null) fortyfivePlus = prefs.getBool("45");
      if (prefs.getBool("covishield") != null)
        covishield = prefs.getBool("covishield");
      if (prefs.getBool("covaxin") != null) covaxin = prefs.getBool("covaxin");
      if (prefs.getBool("free") != null) free = prefs.getBool("free");
      if (prefs.getBool("paid") != null) paid = prefs.getBool("paid");
      if (prefs.getString("checkAvailability") != null) {
        selectedCheckVaccineAvailability = Utility().checkVaccineAvailability[
            Utility().checkVaccineAvailability.indexOf(Utility()
                .getStringINTime(prefs.getString("checkAvailability")))];
      } else {
        await prefs.setString("checkAvailability", "600000");
      }

      if (prefs.getString("state") != null) {
        print(prefs.getString("state"));
        selectedstate = prefs.getString("state");
        district.addAll(Utility().getDistrict(Utility().state[Utility()
            .state
            .indexWhere((element) =>
                element['state_id'].toString() == prefs.getString("state"))]));

        if (prefs.getString("district") != null)
          selectedDistrict = prefs.getString("district");
      }

      print(prefs.getBool("service"));
      if (prefs.getBool("service") != null) service = prefs.getBool("service");

      setState(() {});
    });
  }

  _firebaseConfig() async {
    prefs = await SharedPreferences.getInstance();
    FirebaseMessaging.instance.getToken().then((value) async {
      print("fcm:- " + value);
      await prefs.setString("fcmToken", value);
    });

    FirebaseMessaging.instance
        .getInitialMessage()
        .then((RemoteMessage message) {
      if (message != null) {
        Navigator.pushNamed(context, '/message',
            arguments: MessageArguments(message, true));
      }
    });

    FirebaseMessaging.onMessage.listen((RemoteMessage message) {
      RemoteNotification notification = message.notification;
      AndroidNotification android = message.notification?.android;
      if (notification != null && android != null) {
        Navigator.pushNamed(context, '/message',
            arguments: MessageArguments(message, true));
      }
    });

    FirebaseMessaging.onMessageOpenedApp.listen((RemoteMessage message) {
      print('A new onMessageOpenedApp event was published!');
      Navigator.pushNamed(context, '/message',
          arguments: MessageArguments(message, true));
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: ListView(
        children: [
          Padding(
            padding: const EdgeInsets.all(18.0),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.start,
              children: <Widget>[
                Text(
                  'Search for vaccination slots • Get notified when available.',
                  style: TextStyle(
                    //color: Colors.black,
                    fontSize: 18,
                    fontStyle: FontStyle.normal,
                    fontWeight: FontWeight.w800,
                  ),
                  maxLines: 2,
                  softWrap: true,
                  textAlign: TextAlign.center,
                ),
                SizedBox(
                  height: 8,
                ),
                Row(
                  children: [
                    Expanded(
                      flex: 1,
                      child: GestureDetector(
                        onTap: () async {
                          await prefs.setString("searchBy", "pin");
                          HapticFeedback.mediumImpact();
                          setState(() {
                            searchbypin = true;
                            searchbyDistrict = false;
                          });
                        },
                        child: Chip(
                          shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(30)),
                          padding: const EdgeInsets.symmetric(
                              vertical: 10, horizontal: 10),
                          side: BorderSide(
                              color: searchbypin == true
                                  ? Colors.indigo
                                  : Colors.black,
                              width: 0.6,
                              style: BorderStyle.solid),
                          label: Padding(
                            padding: const EdgeInsets.all(8.0),
                            child: Text(
                              'Pincode',
                              style: TextStyle(
                                  color: searchbypin == true
                                      ? Colors.white
                                      : Colors.black),
                            ),
                          ),
                          backgroundColor: searchbypin == true
                              ? Colors.indigo
                              : Colors.white,
                        ),
                      ),
                    ),
                    Expanded(
                      flex: 1,
                      child: GestureDetector(
                        onTap: () async {
                          await prefs.setString("searchBy", "district");
                          HapticFeedback.mediumImpact();
                          setState(() {
                            searchbypin = false;
                            searchbyDistrict = true;
                          });
                        },
                        child: Chip(
                          shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(30)),
                          padding: const EdgeInsets.symmetric(
                              vertical: 10, horizontal: 10),
                          side: BorderSide(
                              color: searchbyDistrict == true
                                  ? Colors.indigo
                                  : Colors.black,
                              width: 0.6,
                              style: BorderStyle.solid),
                          label: Padding(
                            padding: const EdgeInsets.all(8.0),
                            child: Text(
                              'District',
                              style: TextStyle(
                                  color: searchbyDistrict == true
                                      ? Colors.white
                                      : Colors.black),
                            ),
                          ),
                          backgroundColor: searchbyDistrict == true
                              ? Colors.indigo
                              : Colors.white,
                        ),
                      ),
                    ),
                  ],
                ),
                SizedBox(
                  height: 8,
                ),
                if (searchbypin)
                  Padding(
                    padding: const EdgeInsets.all(8.0),
                    child: Form(
                      key: _formPinKey,
                      child: TextFormField(
                        cursorColor: Colors.indigo,
                        maxLength: 6,
                        decoration: InputDecoration(
                            border: OutlineInputBorder(
                                borderRadius:
                                    BorderRadius.all(Radius.circular(10.0))),
                            hintText: 'Enter your PIN'),
                        controller: pinController,
                        keyboardType: TextInputType.number,
                        maxLines: 1,
                        onChanged: (String s) {},
                        validator: (String pin) {
                          if (pin.isEmpty) {
                            return 'Enter Pin Code';
                          } else if (pin.trim().length != 6) {
                            return 'Enter Valid Pin Code';
                          } else {
                            return null;
                          }
                        },
                      ),
                    ),
                  ),
                if (searchbyDistrict)
                  Column(
                    children: [
                      Container(
                        padding: EdgeInsets.symmetric(horizontal: 10.0),
                        decoration: BoxDecoration(
                          borderRadius: BorderRadius.circular(12.0),
                          border: Border.all(
                              color: Colors.indigo,
                              style: BorderStyle.solid,
                              width: 0.80),
                        ),
                        child: DropdownButton(
                          underline: Container(),
                          onChanged: (String newValue) async {
                            print(Utility().state.length);
                            await prefs.setString("state", newValue.toString());
                            selectedstate = newValue;
                            print(newValue);
                            print(Utility().state.indexWhere((element) =>
                                element['state_id'].toString() == newValue));
                            print(Utility().state[Utility().state.indexWhere(
                                (element) =>
                                    element['state_id'].toString() ==
                                    newValue)]);
                            district.clear();
                            selectedDistrict = null;
                            district.addAll(Utility().getDistrict(
                                Utility().state[Utility().state.indexWhere(
                                    (element) =>
                                        element['state_id'].toString() ==
                                        newValue)]));
                            print(Utility().state.length);
                            print(state.length);
                            setState(() {});
                          },
                          items: state
                              .map(
                                (e) => DropdownMenuItem<String>(
                                  child: Text(
                                    e['state_name'],
                                  ),
                                  value: e['state_id'].toString(),
                                  onTap: () {
                                    print(e);
                                  },
                                ),
                              )
                              .toSet()
                              .toList(),
                          value: selectedstate,
                          hint: Text('Select State'),
                          isDense: false,
                          isExpanded: true,
                        ),
                      ),
                      Padding(
                        padding: EdgeInsets.symmetric(vertical: 10.0),
                        child: Container(
                          padding: EdgeInsets.symmetric(horizontal: 10.0),
                          decoration: BoxDecoration(
                            borderRadius: BorderRadius.circular(12.0),
                            border: Border.all(
                                color: Colors.indigo,
                                style: BorderStyle.solid,
                                width: 0.80),
                          ),
                          child: DropdownButton<String>(
                            underline: Container(),
                            onChanged: (String newValue) async {
                              await prefs.setString(
                                  "district", newValue.toString());
                              setState(() {
                                selectedDistrict = newValue;
                              });
                            },
                            items: district
                                .map(
                                  (e) => DropdownMenuItem(
                                    child: Text(
                                      e['district_name'],
                                    ),
                                    value: e['district_id'].toString(),
                                  ),
                                )
                                .toList(),
                            value: selectedDistrict,
                            hint: Text('Select District'),
                            isDense: false,
                            isExpanded: true,
                          ),
                        ),
                      ),
                    ],
                  ),
                if (searchbypin || searchbyDistrict)
                  Padding(
                    padding:
                        EdgeInsets.symmetric(horizontal: 16.0, vertical: 8),
                    child: Align(
                      alignment: Alignment.centerLeft,
                      child: Text(
                        'Filters',
                        style: TextStyle(
                          //color: Colors.black,
                          fontSize: 18,
                          fontWeight: FontWeight.bold,
                        ),
                        textAlign: TextAlign.start,
                      ),
                    ),
                  ),
                if (searchbypin || searchbyDistrict)
                  Wrap(
                    runSpacing: 2,
                    spacing: 12,
                    children: [
                      ChoiceChip(
                        label: Text(
                          '18 - 44',
                          style: TextStyle(
                              color: eighteenPlus == true
                                  ? Colors.white
                                  : Colors.black),
                        ),
                        selected: eighteenPlus,
                        selectedColor: Colors.indigo,
                        selectedShadowColor: Colors.indigo,
                        side: BorderSide(
                            color: eighteenPlus == true
                                ? Colors.indigo
                                : Colors.black,
                            width: 0.6,
                            style: BorderStyle.solid),
                        backgroundColor:
                            eighteenPlus == true ? Colors.indigo : Colors.white,
                        onSelected: (bool selected) async {
                          HapticFeedback.mediumImpact();
                          await prefs.setBool("18", selected);
                          setState(() {
                            eighteenPlus = selected;
                          });
                        },
                      ),
                      ChoiceChip(
                        label: Text(
                          '45+',
                          style: TextStyle(
                              color: fortyfivePlus == true
                                  ? Colors.white
                                  : Colors.black),
                        ),
                        selectedColor: Colors.indigo,
                        selectedShadowColor: Colors.indigo,
                        side: BorderSide(
                            color: fortyfivePlus == true
                                ? Colors.indigo
                                : Colors.black,
                            width: 0.6,
                            style: BorderStyle.solid),
                        backgroundColor: fortyfivePlus == true
                            ? Colors.indigo
                            : Colors.white,
                        selected: fortyfivePlus,
                        onSelected: (bool selected) async {
                          await prefs.setBool("45", selected);
                          HapticFeedback.mediumImpact();
                          setState(() {
                            fortyfivePlus = selected;
                          });
                        },
                      ),
                      ChoiceChip(
                        label: Text(
                          'Covishield',
                          style: TextStyle(
                              color: covishield == true
                                  ? Colors.white
                                  : Colors.black),
                        ),
                        selectedColor: Colors.indigo,
                        selectedShadowColor: Colors.indigo,
                        backgroundColor:
                            covishield == true ? Colors.indigo : Colors.white,
                        selected: covishield,
                        side: BorderSide(
                            color: covishield == true
                                ? Colors.indigo
                                : Colors.black,
                            width: 0.6,
                            style: BorderStyle.solid),
                        onSelected: (bool selected) async {
                          await prefs.setBool("covishield", selected);
                          HapticFeedback.mediumImpact();
                          setState(() {
                            covishield = selected;
                          });
                        },
                      ),
                      ChoiceChip(
                        label: Text(
                          'Covaxin',
                          style: TextStyle(
                              color: covaxin == true
                                  ? Colors.white
                                  : Colors.black),
                        ),
                        selectedColor: Colors.indigo,
                        selectedShadowColor: Colors.indigo,
                        side: BorderSide(
                            color:
                                covaxin == true ? Colors.indigo : Colors.black,
                            width: 0.6,
                            style: BorderStyle.solid),
                        backgroundColor:
                            covaxin == true ? Colors.indigo : Colors.white,
                        selected: covaxin,
                        onSelected: (bool selected) async {
                          await prefs.setBool("covaxin", selected);
                          HapticFeedback.mediumImpact();
                          setState(() {
                            covaxin = selected;
                          });
                        },
                      ),
                      ChoiceChip(
                        label: Text(
                          'Free',
                          style: TextStyle(
                              color:
                                  free == true ? Colors.white : Colors.black),
                        ),
                        selectedColor: Colors.indigo,
                        selectedShadowColor: Colors.indigo,
                        backgroundColor:
                            free == true ? Colors.indigo : Colors.white,
                        selected: free,
                        side: BorderSide(
                            color: free == true ? Colors.indigo : Colors.black,
                            width: 0.6,
                            style: BorderStyle.solid),
                        onSelected: (bool selected) async {
                          await prefs.setBool("free", selected);
                          HapticFeedback.mediumImpact();
                          setState(() {
                            free = selected;
                          });
                        },
                      ),
                      ChoiceChip(
                        label: Text(
                          'Paid',
                          style: TextStyle(
                              color:
                                  paid == true ? Colors.white : Colors.black),
                        ),
                        selectedColor: Colors.indigo,
                        selectedShadowColor: Colors.indigo,
                        backgroundColor:
                            paid == true ? Colors.indigo : Colors.white,
                        shadowColor: Colors.black,
                        side: BorderSide(
                            color: paid == true ? Colors.indigo : Colors.black,
                            width: 0.6,
                            style: BorderStyle.solid),
                        selected: paid,
                        onSelected: (bool selected) async {
                          await prefs.setBool("paid", selected);
                          HapticFeedback.mediumImpact();
                          setState(() {
                            paid = selected;
                          });
                        },
                      ),
                    ],
                  ),
                if (searchbypin || searchbyDistrict)
                  const SizedBox(
                    height: 6,
                  ),
                if (searchbypin || searchbyDistrict)
                  Padding(
                    padding:
                        EdgeInsets.symmetric(horizontal: 16.0, vertical: 8),
                    child: Align(
                      alignment: Alignment.centerLeft,
                      child: Text(
                        'Check Vaccine Availability',
                        style: TextStyle(
                          //color: Colors.black,
                          fontSize: 18,
                          fontWeight: FontWeight.bold,
                        ),
                        textAlign: TextAlign.start,
                      ),
                    ),
                  ),
                if (searchbypin || searchbyDistrict)
                  Padding(
                    padding: EdgeInsets.only(left: 10, right: 10),
                    child: Container(
                      padding:
                          EdgeInsets.symmetric(horizontal: 10.0, vertical: 6),
                      decoration: BoxDecoration(
                        borderRadius: BorderRadius.circular(12.0),
                        border: Border.all(
                            color: Colors.indigo,
                            style: BorderStyle.solid,
                            width: 0.80),
                      ),
                      child: DropdownButton(
                        underline: Container(),
                        onChanged: (newValue) async {
                          String finalValue = '60000';
                          finalValue = Utility().getTimeinString(newValue);
                          await prefs.setString(
                              "checkAvailability", finalValue);
                          setState(() {
                            selectedCheckVaccineAvailability = newValue;
                          });
                        },
                        items: Utility()
                            .checkVaccineAvailability
                            .map(
                              (e) => DropdownMenuItem(
                                child: Text(
                                  e,
                                ),
                                value: e,
                              ),
                            )
                            .toList(),
                        value: selectedCheckVaccineAvailability,
                        hint: Text('Check Vaccine Availability'),
                        isDense: false,
                        isExpanded: true,
                      ),
                    ),
                  ),
                const SizedBox(
                  height: 16,
                ),
                // if (searchbypin || searchbyDistrict)
                //   Padding(
                //     padding: const EdgeInsets.all(8.0),
                //     child: Form(
                //       key: _formWhatsAppKey,
                //       child: TextFormField(
                //         cursorColor: Colors.indigo,
                //         maxLength: 10,
                //         validator: (String number) {
                //           if (number.isEmpty) {
                //             return 'enter Email Address';
                //           } else {
                //             return null;
                //           }
                //         },
                //         decoration: InputDecoration(
                //             border: OutlineInputBorder(
                //                 borderRadius:
                //                     BorderRadius.all(Radius.circular(10.0))),
                //             hintText: 'Enter Email Address'),
                //         controller: emailController,
                //         keyboardType: TextInputType.number,
                //         maxLines: 1,
                //         onChanged: (String s) {},
                //       ),
                //     ),
                //   ),
                // const SizedBox(
                //   height: 16,
                // ),
                if (searchbypin || searchbyDistrict)
                  OutlinedButton(
                    onPressed: () async {
                      if (searchbypin) {
                        final FormState form = _formPinKey.currentState;
                        if (form.validate()) {
                          print('Form is valid');
                          await prefs.setString(
                              "pincode", pinController.text.trim());
                        } else {
                          print('Form is invalid');
                        }
                      }

                      if (searchbypin) {
                        if (selectedCheckVaccineAvailability
                                .trim()
                                .isNotEmpty &&
                            pinController.text.trim().isNotEmpty) {
                          await _getBatteryLevel(!service);
                        }
                      } else if (searchbyDistrict) {
                        if (selectedCheckVaccineAvailability
                                .trim()
                                .isNotEmpty &&
                            selectedDistrict != null &&
                            selectedstate != null) {
                          if (emailController.text.trim().isNotEmpty) {
                            await prefs.setString(
                                "email", emailController.text.trim());
                          }
                          await _getBatteryLevel(!service);
                        } else if (selectedstate == null) {
                          ScaffoldMessenger.of(context).showSnackBar(
                              SnackBar(content: Text("Please Select State.")));
                        } else if (selectedDistrict == null) {
                          ScaffoldMessenger.of(context).showSnackBar(SnackBar(
                              content: Text("Please Select District.")));
                        }
                      }
                    },
                    // style: OutlinedButton.styleFrom(
                    //     // backgroundColor: Colors.white,
                    //     elevation: 2,
                    //     shadowColor: Colors.indigo,
                    //     minimumSize:
                    //         Size(MediaQuery.of(context).size.width / 1.5, 50)),
                    child: Padding(
                      padding: const EdgeInsets.all(8.0),
                      child: Text(
                        service ? 'Stop Searching' : 'Start Searching',
                        style: TextStyle(
                          fontWeight: FontWeight.bold,
                          color: !service
                              ? Theme.of(context).buttonColor
                              : Colors.red,
                        ),
                      ),
                    ),
                  )
              ],
            ),
          ),
        ],
      ),
    );
  }

  Future<void> _getBatteryLevel(bool services) async {
    String batteryLevel;
    try {
      final String result =
          await platform.invokeMethod('startServices', <String, dynamic>{
        'services': services,
      });
      batteryLevel = 'Battery level at $result % .';
      prefs.setBool("service", services);
      setState(() {
        service = services;
      });
    } on PlatformException catch (e) {
      batteryLevel = "Failed to get battery level: '${e.message}'.";
    }
    print(batteryLevel);
  }
}
