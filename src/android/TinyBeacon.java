package info.kyorohiro.tinybeacon;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TinyBeacon {

    private Map<TinyAdStructure, TinyAdStructureEx> mFouncediBeacon = new LinkedHashMap();
    private ScanCallback mCurrentScanCallback = null;
    private BluetoothManager mBluetoothManager = null;
    private BluetoothLeScanner mScanner = null;

    public void startLescan(Context context) {
        if (mCurrentScanCallback != null) {
            return;
        }
        mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = mBluetoothManager.getAdapter();
        mScanner = adapter.getBluetoothLeScanner();
        //
        MyCallback callback = new MyCallback(this);
        mScanner.startScan(callback);
        mCurrentScanCallback = callback;

    }

    public void stopLescan() {
        if(mCurrentScanCallback != null) {
            mScanner.stopScan(mCurrentScanCallback);
            mCurrentScanCallback = null;
        }
    }

    public Map<TinyAdStructure, TinyAdStructureEx> getFoundedBeeacon() {
        return mFouncediBeacon;
    }

    public String getFoundedBeeaconAsJSONText() throws JSONException {
        JSONObject ret = new JSONObject();
        List<JSONObject> t = new LinkedList<JSONObject>();
        for(TinyAdStructure s: mFouncediBeacon.keySet()) {
            TinyAdStructureEx e = mFouncediBeacon.get(s);
            t.add(e.toJsonString(s));
        }
        ret.put("founded", new JSONArray(t));
        return ret.toString();
    }

    public void clearFoundedBeeacon() throws JSONException {
        mFouncediBeacon.clear();
    }

    //
    //
    //
    static class TinyAdStructureEx {
        int rssi;
        long time;
        TinyAdStructureEx(int _rssi, long _time) {
            this.rssi = _rssi;
            this.time = _time;
        }

        JSONObject toJsonString(TinyAdStructure ad) throws JSONException {
            JSONObject ret = new JSONObject();
            ret.put("uuid", ad.getUUIDHexStringAsiBeacon());
            ret.put("major", ad.getMajorAsiBeacon());
            ret.put("minor", ad.getMinorAsiBeacon());
            ret.put("calrssi", ad.getCalibratedRSSIAsiBeacon());
            ret.put("rssi", rssi);
            ret.put("time", time);
            return ret;
        }

    }

    //
    //
    //
    static class MyCallback extends ScanCallback {
        TinyBeacon mParent = null;

        MyCallback(TinyBeacon parent) {
            mParent = parent;
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
//            android.util.Log.v("beacon", "###SA## scanResult type:" + callbackType + " ,result: " + result.toString());
            long t = System.currentTimeMillis();
            List<TinyAdStructure> ad = TinyAdStructure.parse(result.getScanRecord().getBytes());
            for(TinyAdStructure a : ad){
                if(a.isiBeacon()) {
                    android.util.Log.v("KY", "uuid:" + a.getUUIDAsiBeacon() + ", major:" + a.getMajorAsiBeacon() + ", minor:" + a.getMinorAsiBeacon() + ",crssi:" + a.getCalibratedRSSIAsiBeacon());
                    if(false == mParent.mFouncediBeacon.containsKey(a)) {
                        mParent.mFouncediBeacon.put(a,new TinyAdStructureEx(result.getRssi(), t));
                    } else {
                        mParent.mFouncediBeacon.get(a).rssi = result.getRssi();
                        mParent.mFouncediBeacon.get(a).time = t;
                    }
                }
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            StringBuilder builder = new StringBuilder();
            for (ScanResult r : results) {
                builder.append("re " + r + ",b:" + r.getScanRecord().getBytes()+", rssi:"+r.getRssi()+"\n");
            }
            android.util.Log.v("beacon", "###S## batchScanResult type:" + builder.toString());
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            android.util.Log.v("beacon", "###S## scanFailed errorCode:" + errorCode);
        }
    }
}