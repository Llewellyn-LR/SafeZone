package com.safezone.app.service;

import android.content.Context;
import android.util.Log;

import com.safezone.app.model.PoliceStation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AssetStationService implements StationService {
    private static final String TAG = "AssetStationService";
    private final Context context;

    public AssetStationService(Context context) { this.context = context.getApplicationContext(); }

    @Override
    public List<PoliceStation> getStations() {
        List<PoliceStation> out = new ArrayList<>();
        try (InputStream in = context.getAssets().open("stations.json")) {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int n;
            while ((n = in.read(chunk)) > 0) buf.write(chunk, 0, n);
            JSONArray arr = new JSONArray(buf.toString("UTF-8"));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                PoliceStation s = new PoliceStation(
                        o.optString("name"), o.optString("address"), o.optString("phone"),
                        o.optDouble("latitude", 0), o.optDouble("longitude", 0),
                        o.optString("sourceUrl"), o.optString("verifiedOn"));
                if (s.isVerified()) out.add(s);
                else Log.w(TAG, "Skipped unverified station entry #" + i);
            }
        } catch (Exception e) {
            Log.w(TAG, "Couldn't read stations.json", e);
        }
        return out;
    }
}
