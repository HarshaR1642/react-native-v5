package com.service.keylessrn;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

import javax.annotation.Nonnull;

public class V5Module extends ReactContextBaseJavaModule {

    ReactContext reactContext;
    V5AidlInterface v5AidlInterface;
    ServiceConnection serviceConnection;

    public V5Module(@Nonnull ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        initService();
    }

    @NonNull
    @Override
    public String getName() {
        return "V5Module";
    }

    public void initService() {
        serviceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                v5AidlInterface = V5AidlInterface.Stub.asInterface(service);
            }

            public void onServiceDisconnected(ComponentName className) {
                Log.e("KeylessRN", "Service has unexpectedly disconnected");
                v5AidlInterface = null;
            }
        };

        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.service.keylessrn", "com.service.keylessrn.AidlService"));
        this.reactContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @ReactMethod
    public void login(String email, String password, final Promise promise){
        Bundle bundle = new Bundle();
        bundle.putString("email", email);
        bundle.putString("password", password);
        if(v5AidlInterface != null){
            try {
                v5AidlInterface.login(bundle, new ResponseCallback.Stub(){
                    @Override
                    public void onResponse(LoginResponseModel response){
                        WritableMap map = new WritableNativeMap();

                        boolean success = response.isSuccess();
                        map.putBoolean("success", response.isSuccess());
                        if(success) {
                            map.putString("access_token", response.getAccess_token());
                            map.putString("id_token", response.getId_token());
                            map.putString("refresh_token", response.getRefresh_token());
                        }else{
                            map.putString("message", response.getMessage());
                        }
                        if(success) {
                            promise.resolve(map);
                        }else{
                            promise.reject("200", response.getMessage(), map);
                        }
                    }
                });
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
