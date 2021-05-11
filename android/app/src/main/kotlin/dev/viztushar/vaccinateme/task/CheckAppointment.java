package dev.viztushar.vaccinateme.task;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class CheckAppointment extends AsyncTask<Void, Void, Void> {

    String TAG = CheckAppointment.class.getSimpleName();
    private String url, jsonResult;
    FileOutputStream outputStream;
    Context contexxt;
    boolean newstickerpacks = false;
    private Callbacks callbacks;
    SharedPreferences sharedpreferences;
    boolean eighteenPlus = false;
    boolean fortyfivePlus = false;
    boolean covishield = false;
    boolean covaxin = false;
    boolean free = false;
    boolean paid = false;
    String token;
    PowerManager.WakeLock wakeLock;
    WifiManager.WifiLock wifiLock;

    public CheckAppointment(Context context, Callbacks callbacks, String url, SharedPreferences sharedpreferences,
                            boolean eighteenPlus,
                            boolean fortyfivePlus,
                            boolean covishield,
                            boolean covaxin,
                            boolean free,
                            boolean paid,String token) {
        this.callbacks = callbacks;
        contexxt = context;
        this.url = url;
        this.sharedpreferences = sharedpreferences;

        this.eighteenPlus = eighteenPlus;
        this.fortyfivePlus = fortyfivePlus;
        this.covishield = covishield;
        this.covaxin = covaxin;
        this.free = free;
        this.paid = paid;
        this.token = token;
    }

    @SuppressLint("WrongThread")
    @Override
    protected Void doInBackground(Void... z) {
        PowerManager p = (PowerManager) contexxt.getSystemService(Context.POWER_SERVICE);
        assert p != null;
        wakeLock = p.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EndlessService::lock");
        wakeLock.acquire();
        try {

            URL urll = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urll.openConnection();

            jsonResult = inputStreamToString(connection.getInputStream())
                    .toString();
            //Log.i("response", "doInBackground: " + jsonResult);

        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            String painOrFree = "free";
            String eighteenPlusOrfortyfivePlus = "18";
            String covishieldOrcovaxin = "covishield";
            if (free && !paid) {
                painOrFree = "free";
            } else if (!free && paid) {
                painOrFree = "paid";
            } else {
                painOrFree = "both";
            }

            if (covishield && covaxin) {
                covishieldOrcovaxin = "both";
            } else if (!covishield && !covaxin) {
                covishieldOrcovaxin = "both";
            } else if (covishield) {
                covishieldOrcovaxin = "covishield";
            } else if (covaxin) {
                covishieldOrcovaxin = "covaxin";
            }

            if (eighteenPlus && !fortyfivePlus) {
                eighteenPlusOrfortyfivePlus = "18";
            } else if (!eighteenPlus && fortyfivePlus) {
                eighteenPlusOrfortyfivePlus = "45";
            } else {
                eighteenPlusOrfortyfivePlus = "both";
            }


            Log.d(TAG, "doInBackground: " + painOrFree);
            Log.d(TAG, "doInBackground: " + eighteenPlusOrfortyfivePlus);
            Log.d(TAG, "doInBackground: " + covishieldOrcovaxin);

            ArrayList sendArray = new ArrayList();
            JSONObject obj = new JSONObject(jsonResult);
            JSONArray array = obj.getJSONArray("centers");
            Log.d(TAG, "doInBackground: " + array.length());
            StringBuilder sb = new StringBuilder();
            String title = "";
            for (int i = 0; i < array.length(); i++) {
                JSONObject sessionsObj = (JSONObject) array.get(i);
                Log.d(TAG, "doInBackground: " + sessionsObj.getString("fee_type"));
                JSONArray sessions = sessionsObj.getJSONArray("sessions");

                if (sessions.length() == 1) {
                    JSONObject sessionsObjData = (JSONObject) sessions.get(0);
                    Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("session_id"));
                    Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("min_age_limit"));
                    Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getInt("min_age_limit"));
                    Log.d(TAG, "doInBackground: available_capacity " + (i + 1) + " " + sessionsObjData.getInt("available_capacity"));
                    if (sessionsObjData.getInt("available_capacity") != 0) {

                        if ((sessionsObj.getString("fee_type").compareTo("Free") == 0 && painOrFree.contains("free"))) {


                            if (sessionsObjData.getString("min_age_limit").contains("18") &&  eighteenPlusOrfortyfivePlus.contains("18")) {
                                if(eighteenPlusOrfortyfivePlus.contains("18")) {
                                    Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getInt("min_age_limit"));
                                    if (sessionsObjData.getString("vaccine").toString().compareTo("COVISHIELD") == 0 && covishieldOrcovaxin.contains("covishield")) {
                                        Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                        title = sessionsObj.getString("name") + " available capacity" + sessionsObjData.getInt("available_capacity");
                                        sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                    } else if (sessionsObjData.getString("vaccine").toString().compareTo("COVAXIN") == 0 && covishieldOrcovaxin.contains("covaxin")) {
                                        Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                        title = sessionsObj.getString("name") + " available capacity" + sessionsObjData.getInt("available_capacity");
                                        sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                    } else if (covishieldOrcovaxin.contains("both")) {
                                        Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                        title = sessionsObj.getString("name") + " available capacity" + sessionsObjData.getInt("available_capacity");
                                        sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                    }
                                }

                            } else if (sessionsObjData.getString("min_age_limit").contains("45") && eighteenPlusOrfortyfivePlus.contains("45")) {
                                if(eighteenPlusOrfortyfivePlus.contains("45")) {
                                    Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getInt("min_age_limit"));
                                    if (sessionsObjData.getString("vaccine").toString().compareTo("COVISHIELD") == 0 && covishieldOrcovaxin.contains("covishield")) {
                                        Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                        title = sessionsObj.getString("name") + " available capacity" + sessionsObjData.getInt("available_capacity");
                                        sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                    } else if (sessionsObjData.getString("vaccine").toString().compareTo("COVAXIN") == 0 && covishieldOrcovaxin.contains("covaxin")) {
                                        Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                        title = sessionsObj.getString("name") + " available capacity" + sessionsObjData.getInt("available_capacity");
                                        sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                    } else if (covishieldOrcovaxin.contains("both")) {
                                        Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                        title = sessionsObj.getString("name") + " available capacity" + sessionsObjData.getInt("available_capacity");
                                        sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                    }
                                }

                            } else if (eighteenPlusOrfortyfivePlus.contains("both")) {
                                //Log.d(TAG, "doInBackground: " + sessionsObjData.getString("vaccine"));
                                if (sessionsObjData.getString("vaccine").toString().compareTo("COVISHIELD") == 0) {
                                   
                                    Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                    title = sessionsObj.getString("name") + " available capacity" + sessionsObjData.getInt("available_capacity");
                                    //sb.append("\nname:- ").append(sessionsObjData.getString("name")).append("\naddress:- ").append(sessionsObjData.getString("address")).append("\nage:- ").append(sessionsObjData.getString("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                    sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                    Log.d(TAG, "doInBackground: sb " + sessionsObj.getString("name") );
                                    Log.d(TAG, "doInBackground: sb " + sessionsObj.getString("address"));
                                    Log.d(TAG, "doInBackground: sb " + String.valueOf(sessionsObjData.getInt("min_age_limit")));
                                    Log.d(TAG, "doInBackground: sb " + sessionsObjData.getString("vaccine"));
                                } else if (sessionsObjData.getString("vaccine").toString().compareTo("COVAXIN") == 0 && covishieldOrcovaxin.contains("covaxin")) {
                                    Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                    title = sessionsObj.getString("name") + " available capacity" + sessionsObjData.getInt("available_capacity");
                                    sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                } else if (covishieldOrcovaxin.contains("both")) {
                                    Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                    title = sessionsObj.getString("name") + " available capacity" + sessionsObjData.getInt("available_capacity");
                                    sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                }
                            }
                        }


                        else if ((sessionsObj.getString("fee_type").compareTo("Paid") == 0 && painOrFree.contains("paid"))) {


                            if (sessionsObjData.getString("min_age_limit").contains("18") &&  eighteenPlusOrfortyfivePlus.contains("18")) {
                                if(eighteenPlusOrfortyfivePlus.contains("18")) {
                                    Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getInt("min_age_limit"));
                                    title = sessionsObj.getString("name") + " available capacity" + sessionsObjData.getInt("available_capacity");
                                    if (sessionsObjData.getString("vaccine").toString().compareTo("COVISHIELD") == 0 && covishieldOrcovaxin.contains("covishield")) {
                                        Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                       sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                    } else if (sessionsObjData.getString("vaccine").toString().compareTo("COVAXIN") == 0 && covishieldOrcovaxin.contains("covaxin")) {
                                        Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                       sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                    } else if (covishieldOrcovaxin.contains("both")) {
                                        Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                       sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                    }
                                }

                            } else if (sessionsObjData.getString("min_age_limit").contains("45") && eighteenPlusOrfortyfivePlus.contains("45")) {
                                if(eighteenPlusOrfortyfivePlus.contains("45")) {
                                    title = sessionsObj.getString("name") + " available capacity" + sessionsObjData.getInt("available_capacity");
                                    Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getInt("min_age_limit"));
                                    if (sessionsObjData.getString("vaccine").toString().compareTo("COVISHIELD") == 0 && covishieldOrcovaxin.contains("covishield")) {
                                        Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                       sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                    } else if (sessionsObjData.getString("vaccine").toString().compareTo("COVAXIN") == 0 && covishieldOrcovaxin.contains("covaxin")) {
                                        Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                       sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                    } else if (covishieldOrcovaxin.contains("both")) {
                                        Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                       sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                    }
                                }

                            } else if (eighteenPlusOrfortyfivePlus.contains("both")) {
                                title = sessionsObj.getString("name") + " available capacity" + sessionsObjData.getInt("available_capacity");
                                Log.d(TAG, "doInBackground: " + sessionsObjData.getString("vaccine"));
                                if (sessionsObjData.getString("vaccine").toString().compareTo("COVISHIELD") == 0 && covishieldOrcovaxin.contains("covishield")) {
                                    Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                   sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                } else if (sessionsObjData.getString("vaccine").toString().compareTo("COVAXIN") == 0 && covishieldOrcovaxin.contains("covaxin")) {
                                    Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                   sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                } else if (covishieldOrcovaxin.contains("both")) {
                                    Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                   sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                }
                            }
                        }

                        else if (painOrFree.contains("both")) {


                            if (sessionsObjData.getString("min_age_limit").contains("18") &&  eighteenPlusOrfortyfivePlus.contains("18")) {
                                if(eighteenPlusOrfortyfivePlus.contains("18")) {
                                    title = sessionsObj.getString("name") + " available capacity" + sessionsObjData.getInt("available_capacity");
                                    Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getInt("min_age_limit"));
                                    if (sessionsObjData.getString("vaccine").toString().compareTo("COVISHIELD") == 0 && covishieldOrcovaxin.contains("covishield")) {
                                        Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                       sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                    } else if (sessionsObjData.getString("vaccine").toString().compareTo("COVAXIN") == 0 && covishieldOrcovaxin.contains("covaxin")) {
                                        Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                       sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                    } else if (covishieldOrcovaxin.contains("both")) {
                                        Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                       sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                    }
                                }

                            } else if (sessionsObjData.getString("min_age_limit").contains("45") && eighteenPlusOrfortyfivePlus.contains("45")) {
                                if(eighteenPlusOrfortyfivePlus.contains("45")) {
                                    title = sessionsObj.getString("name") + " available capacity" + sessionsObjData.getInt("available_capacity");
                                    Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getInt("min_age_limit"));
                                    if (sessionsObjData.getString("vaccine").toString().compareTo("COVISHIELD") == 0 && covishieldOrcovaxin.contains("covishield")) {
                                        Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                       sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                    } else if (sessionsObjData.getString("vaccine").toString().compareTo("COVAXIN") == 0 && covishieldOrcovaxin.contains("covaxin")) {
                                        Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                       sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                    } else if (covishieldOrcovaxin.contains("both")) {
                                        Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                       sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                    }
                                }

                            } else if (eighteenPlusOrfortyfivePlus.contains("both")) {
                                title = sessionsObj.getString("name") + " available capacity" + sessionsObjData.getInt("available_capacity");
                                Log.d(TAG, "doInBackground: " + sessionsObjData.getString("vaccine"));
                                if (sessionsObjData.getString("vaccine").toString().compareTo("COVISHIELD") == 0 && covishieldOrcovaxin.contains("covishield")) {
                                    Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                   sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                } else if (sessionsObjData.getString("vaccine").toString().compareTo("COVAXIN") == 0 && covishieldOrcovaxin.contains("covaxin")) {
                                    Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                   sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                } else if (covishieldOrcovaxin.contains("both")) {
                                    Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                   sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                }
                            }
                        }

                    }
                } else {
                    for (int j = 0; j < sessions.length(); j++) {

                        JSONObject sessionsObjData = (JSONObject) sessions.get(i);
                        if (sessionsObjData.getInt("available_capacity") != 0) {
                            title = sessionsObj.getString("name") + " available capacity" + sessionsObjData.getInt("available_capacity");
                            if ((sessionsObj.getString("fee_type").compareTo("Free") == 0 && painOrFree.contains("free"))) {


                                if (sessionsObjData.getString("min_age_limit").contains("18") &&  eighteenPlusOrfortyfivePlus.contains("18")) {
                                    if(eighteenPlusOrfortyfivePlus.contains("18")) {
                                        Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getInt("min_age_limit"));
                                        if (sessionsObjData.getString("vaccine").toString().compareTo("COVISHIELD") == 0 && covishieldOrcovaxin.contains("covishield")) {
                                            Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                           sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                        } else if (sessionsObjData.getString("vaccine").toString().compareTo("COVAXIN") == 0 && covishieldOrcovaxin.contains("covaxin")) {
                                            Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                           sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                        } else if (covishieldOrcovaxin.contains("both")) {
                                            Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                           sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                        }
                                    }

                                } else if (sessionsObjData.getString("min_age_limit").contains("45") && eighteenPlusOrfortyfivePlus.contains("45")) {
                                    if(eighteenPlusOrfortyfivePlus.contains("45")) {
                                        Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getInt("min_age_limit"));
                                        if (sessionsObjData.getString("vaccine").toString().compareTo("COVISHIELD") == 0 && covishieldOrcovaxin.contains("covishield")) {
                                            Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                           sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                        } else if (sessionsObjData.getString("vaccine").toString().compareTo("COVAXIN") == 0 && covishieldOrcovaxin.contains("covaxin")) {
                                            Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                           sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                        } else if (covishieldOrcovaxin.contains("both")) {
                                            Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                           sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                        }
                                    }

                                } else if (eighteenPlusOrfortyfivePlus.contains("both")) {
                                    Log.d(TAG, "doInBackground: " + sessionsObjData.getString("vaccine"));
                                    if (sessionsObjData.getString("vaccine").toString().compareTo("COVISHIELD") == 0 && covishieldOrcovaxin.contains("covishield")) {
                                        Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                       sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                    } else if (sessionsObjData.getString("vaccine").toString().compareTo("COVAXIN") == 0 && covishieldOrcovaxin.contains("covaxin")) {
                                        Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                       sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                    } else if (covishieldOrcovaxin.contains("both")) {
                                        Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                       sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                    }
                                }
                            }


                            else if ((sessionsObj.getString("fee_type").compareTo("Paid") == 0 && painOrFree.contains("paid"))) {


                                if (sessionsObjData.getString("min_age_limit").contains("18") &&  eighteenPlusOrfortyfivePlus.contains("18")) {
                                    if(eighteenPlusOrfortyfivePlus.contains("18")) {
                                        Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getInt("min_age_limit"));
                                        if (sessionsObjData.getString("vaccine").toString().compareTo("COVISHIELD") == 0 && covishieldOrcovaxin.contains("covishield")) {
                                            Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                           sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                        } else if (sessionsObjData.getString("vaccine").toString().compareTo("COVAXIN") == 0 && covishieldOrcovaxin.contains("covaxin")) {
                                            Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                           sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                        } else if (covishieldOrcovaxin.contains("both")) {
                                            Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                           sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                        }
                                    }

                                } else if (sessionsObjData.getString("min_age_limit").contains("45") && eighteenPlusOrfortyfivePlus.contains("45")) {
                                    if(eighteenPlusOrfortyfivePlus.contains("45")) {
                                        Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getInt("min_age_limit"));
                                        if (sessionsObjData.getString("vaccine").toString().compareTo("COVISHIELD") == 0 && covishieldOrcovaxin.contains("covishield")) {
                                            Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                           sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                        } else if (sessionsObjData.getString("vaccine").toString().compareTo("COVAXIN") == 0 && covishieldOrcovaxin.contains("covaxin")) {
                                            Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                           sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                        } else if (covishieldOrcovaxin.contains("both")) {
                                            Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                           sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                        }
                                    }

                                } else if (eighteenPlusOrfortyfivePlus.contains("both")) {
                                    Log.d(TAG, "doInBackground: " + sessionsObjData.getString("vaccine"));
                                    if (sessionsObjData.getString("vaccine").toString().compareTo("COVISHIELD") == 0 && covishieldOrcovaxin.contains("covishield")) {
                                        Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                       sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                    } else if (sessionsObjData.getString("vaccine").toString().compareTo("COVAXIN") == 0 && covishieldOrcovaxin.contains("covaxin")) {
                                        Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                       sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                    } else if (covishieldOrcovaxin.contains("both")) {
                                        Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                       sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                    }
                                }
                            }

                            else if (painOrFree.contains("both")) {


                                if (sessionsObjData.getString("min_age_limit").contains("18") &&  eighteenPlusOrfortyfivePlus.contains("18")) {
                                    if(eighteenPlusOrfortyfivePlus.contains("18")) {
                                        Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getInt("min_age_limit"));
                                        if (sessionsObjData.getString("vaccine").toString().compareTo("COVISHIELD") == 0 && covishieldOrcovaxin.contains("covishield")) {
                                            Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                           sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                        } else if (sessionsObjData.getString("vaccine").toString().compareTo("COVAXIN") == 0 && covishieldOrcovaxin.contains("covaxin")) {
                                            Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                           sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                        } else if (covishieldOrcovaxin.contains("both")) {
                                            Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                           sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                        }
                                    }

                                } else if (sessionsObjData.getString("min_age_limit").contains("45") && eighteenPlusOrfortyfivePlus.contains("45")) {
                                    if(eighteenPlusOrfortyfivePlus.contains("45")) {
                                        Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getInt("min_age_limit"));
                                        if (sessionsObjData.getString("vaccine").toString().compareTo("COVISHIELD") == 0 && covishieldOrcovaxin.contains("covishield")) {
                                            Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                           sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                        } else if (sessionsObjData.getString("vaccine").toString().compareTo("COVAXIN") == 0 && covishieldOrcovaxin.contains("covaxin")) {
                                            Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                           sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                        } else if (covishieldOrcovaxin.contains("both")) {
                                            Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                           sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                        }
                                    }

                                } else if (eighteenPlusOrfortyfivePlus.contains("both")) {
                                    Log.d(TAG, "doInBackground: " + sessionsObjData.getString("vaccine"));
                                    if (sessionsObjData.getString("vaccine").toString().compareTo("COVISHIELD") == 0 && covishieldOrcovaxin.contains("covishield")) {
                                        Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                       sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                    } else if (sessionsObjData.getString("vaccine").toString().compareTo("COVAXIN") == 0 && covishieldOrcovaxin.contains("covaxin")) {
                                        Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                       sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                    } else if (covishieldOrcovaxin.contains("both")) {
                                        Log.d(TAG, "doInBackground: " + (i + 1) + " " + sessionsObjData.getString("vaccine"));
                                       sb.append("\nname:- ").append(sessionsObj.getString("name")).append("\naddress:- ").append(sessionsObj.getString("address")).append("\nage:- ").append(sessionsObjData.getInt("min_age_limit")).append("\nvaccine:- ").append(sessionsObjData.getString("vaccine")).append("\n\n");
                                    }
                                }
                            }

                        }
                    }
                }



            }
            if(sb.length() != 0){
                Log.d(TAG, "doInBackground: string data " + sb);
                new SendNotificaion(contexxt,"https://fcm.googleapis.com/fcm/send",sb.toString(),title,token).execute();
            }

        } catch (Exception ex) {
            //Do nothing because something is wrong! Oh well this feature just wont work on whatever device.
        }

        return null;
    }

    private boolean fileExistance(String fname) {
        File file = contexxt.getFileStreamPath(fname);
        return file.exists();
    }

    private void writeWallFile() {
        try {
            outputStream = contexxt.openFileOutput("sticker_packs", Context.MODE_PRIVATE);
            outputStream.write(jsonResult.getBytes());
            outputStream.close();
        } catch (Exception ex) {
            //Do nothing because something is wrong! Oh well this feature just wont work on whatever device.
        }
    }

    private StringBuilder inputStreamToString(InputStream is) {
        String rLine = "";
        StringBuilder answer = new StringBuilder();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));

        try {
            while ((rLine = rd.readLine()) != null) {
                answer.append(rLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return answer;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (callbacks != null)
            callbacks.onListLoaded(jsonResult, newstickerpacks);
    }

    public interface Callbacks {
        void onListLoaded(String jsonResult, boolean jsonSwitch);
    }



}
