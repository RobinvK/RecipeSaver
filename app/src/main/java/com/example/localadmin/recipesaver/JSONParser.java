package com.example.localadmin.recipesaver;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

public class JSONParser {

    String charset = "UTF-8";
    HttpURLConnection conn;
    DataOutputStream wr;
    StringBuilder result = new StringBuilder();
    URL urlObj;
    JSONObject jObj = null;
    StringBuilder sbParams;
    String paramsString;

    public JSONObject makeHttpRequest(String url, String method,
                                      HashMap<String, String> params) {

        result.setLength(0);
        sbParams = new StringBuilder();
        int i = 0;
        for (String key : params.keySet()) {
            try {
                if (i != 0){
                    sbParams.append("&");
                }
                sbParams.append(key).append("=")
                        .append(URLEncoder.encode(params.get(key), charset));

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            i++;
        }

        if (method.equals("POST") || method.equals("POSTIMAGE")) {
            // request method is POST
            try {
                urlObj = new URL(url);

                conn = (HttpURLConnection) urlObj.openConnection();

                conn.setDoOutput(true);

                conn.setRequestMethod("POST");


                if(method.equals("POST")){
                    conn.setRequestProperty("Accept-Charset", charset);
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(15000);
                    conn.connect();

                    paramsString = sbParams.toString();

                    wr = new DataOutputStream(conn.getOutputStream());
                    wr.writeBytes(paramsString);
                }
                else if(method.equals("POSTIMAGE")){//upload image
                    String boundary = "*****";
                    String lineEnd = "\r\n";
                    String twoHyphens = "--";
                    int bytesRead, bytesAvailable, bufferSize;
                    byte[] buffer;
                    int maxBufferSize = 1 * 1024 * 1024;
                    int serverResponseCode = 0;

                    if(params.get("selectedImagePath")!=null && params.get("selectedImagePath")!="" && params.get("recipeID")!=null && params.get("recipeID")!="") {
                        String selectedImagePath = params.get("selectedImagePath");

                        conn.setDoInput(true); // Allow Inputs
                        conn.setRequestProperty("Connection", "Keep-Alive");
                        conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                        conn.setRequestProperty("uploaded_file", selectedImagePath);
                        conn.connect();
                        wr = new DataOutputStream(conn.getOutputStream());
                        wr.writeBytes(twoHyphens + boundary + lineEnd);


                        wr.writeBytes("Content-Disposition: form-data; name=\"recipeID\"" + lineEnd);
                        wr.writeBytes(lineEnd);
                        wr.writeBytes(params.get("recipeID"));
                        wr.writeBytes(lineEnd);
                        wr.writeBytes(twoHyphens + boundary + lineEnd);

                        wr.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                                + selectedImagePath + "\"" + lineEnd);

                        wr.writeBytes(lineEnd);

                        FileInputStream fileInputStream = new FileInputStream(new File(selectedImagePath));
                        bytesAvailable = fileInputStream.available();

                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        buffer = new byte[bufferSize];

                        // read file and write it into form...
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                        while (bytesRead > 0) {

                            wr.write(buffer, 0, bufferSize);
                            bytesAvailable = fileInputStream.available();
                            bufferSize = Math.min(bytesAvailable, maxBufferSize);
                            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                        }
                        wr.writeBytes(lineEnd);
                        wr.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                        serverResponseCode = conn.getResponseCode();
                        if (serverResponseCode == 200) {
                            Log.d("RRROBIN RECIPEDATA", "File Upload Complete.");
                        } else {
                            //TODO
                        }
                        fileInputStream.close();
                    }
                    else{
                        //TODO: no path...
                    }
                }

                wr.flush();
                wr.close();


            } catch (IOException e) {
                e.printStackTrace();
            }
            catch (Exception e) {//for image upload exceptions
                Log.d("RRROBIN ERROR", "Got Exception : see logcat  e " + e);
                e.printStackTrace();

                Log.d("RRROBIN ERROR", " Upload file to server Exception Exception : " + e.getMessage(), e);
            }
        }
        else if(method.equals("GET")){
            // request method is GET

            if (sbParams.length() != 0) {
                url += "?" + sbParams.toString();
            }

            try {
                urlObj = new URL(url);

                conn = (HttpURLConnection) urlObj.openConnection();

                conn.setDoOutput(false);

                conn.setRequestMethod("GET");

                conn.setRequestProperty("Accept-Charset", charset);

                conn.setConnectTimeout(15000);

                conn.connect();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        try {
            //Receive the response from the server
            InputStream in = new BufferedInputStream(conn.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            Log.d("RRROBIN RECIPEDATA",  " JSON Parser result: " + result.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }

        conn.disconnect();

        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(result.toString());
        } catch (JSONException e) {
            Log.e("RRROBIN RECIPEDATA",  " JSON Parser Error parsing data " + e.toString());
        }

        // return JSON Object
        return jObj;
    }
}