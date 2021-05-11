package dev.viztushar.vaccinateme.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SendNotificaion extends AsyncTask<Void, Void, Void> {
    String TAG = SendNotificaion.class.getSimpleName();
    private String url, jsonResult,data,token,title;
    FileOutputStream outputStream;
    Context contexxt;
    boolean newstickerpacks = false;
    private CheckAppointment.Callbacks callbacks;

    public SendNotificaion(Context context, String url,String data,String title,String token) {
        this.callbacks = callbacks;
        contexxt = context;
        this.url = url;
        this.data = data;
        this.token = token;
        this.title =title;
    }

    @Override
    protected Void doInBackground(Void... z) {
//        try {
//            Log.d(TAG, "doInBackground: " + url);
//            URL urll = new URL(url);
//            HttpURLConnection connection = (HttpURLConnection) urll.openConnection();
//            connection.setRequestMethod("POST");
//            connection.setRequestProperty("Content-Type", "application/json; utf-8");
//            connection.setRequestProperty("Accept", "application/json");
//            connection.setRequestProperty("Authorization","key=AAAA3V4WK7o:APA91bERWCbaUtmy0qg3EsuIcetK-Qv1xmqWyGl9PvfKy_Rn3x_TLYuKIcok-iHYz8xg3rs2mDHw324jz5GUxRf5D3vgd6RgeoFhXWa-TLMKAmX3dVJCvsgnzHCpM7zYAG9s-BwGnwzi");
//            connection.setDoOutput(true);
//            String jsonInputString = "{\n" +
//                    "  \"priority\":\"HIGH\",\n" +
//                    "  \"data\":{\n" +
//                    "data" + data +
//                    "  },\n" +"  \"to\":\""+token+"\"\n" + "}";
//
//            byte[] outputInBytes = jsonInputString.getBytes("UTF-8");
//            OutputStream os = connection.getOutputStream();
//            os.write(outputInBytes);
//            os.close();
//
//            jsonResult = inputStreamToString(connection.getInputStream())
//                    .toString();
//            Log.i("response", "doInBackground: " + jsonResult);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        try {
            HttpURLConnection httpcon = (HttpURLConnection) ((new URL("https://fcm.googleapis.com/fcm/send").openConnection()));
            httpcon.setDoOutput(true);
            httpcon.setRequestProperty("Content-Type", "application/json");
            httpcon.setRequestProperty("Authorization", "key=AAAA3V4WK7o:APA91bERWCbaUtmy0qg3EsuIcetK-Qv1xmqWyGl9PvfKy_Rn3x_TLYuKIcok-iHYz8xg3rs2mDHw324jz5GUxRf5D3vgd6RgeoFhXWa-TLMKAmX3dVJCvsgnzHCpM7zYAG9s-BwGnwzi");
            httpcon.setRequestMethod("POST");
            httpcon.connect();
            System.out.println("Connected!");
            String jsonInputString ="{\"notification\":{\"title\": \"My title\", \"text\": \"My text\", \"sound\": \"default\"}, \"to\": \""+token+"\"}";
            byte[] outputBytes = ( "{\n" +
                    "    \"to\": \""+token+"\",\n" +
                    "    \"notification\": {\n" +
                    "        \"title\": \"Appointment Available\",\n" +
                    "        \"body\": \""+title+"\",\n" +
                    "        \"mutable_content\": true,\n" +
                    "        \"sound\": \"default\"\n" +
                    "    },\n" +
                    "    \"priority\": \"high\",\n" +
                    "    \"data\": {\n" +
                    "        \"data\": \""+data+"\"\n" +
                    "    },\n" +
                    "    \"content_available\": true\n" +
                    "}").getBytes("UTF-8");//jsonInputString.getBytes("UTF-8");

            OutputStream os = httpcon.getOutputStream();
            os.write(outputBytes);
            os.close();

            // Reading response
            InputStream input = httpcon.getInputStream();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
                for (String line; (line = reader.readLine()) != null;) {
                    System.out.println(line);
                }
            }

            System.out.println("Http POST request sent!");
        } catch (IOException e) {
            e.printStackTrace();
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
