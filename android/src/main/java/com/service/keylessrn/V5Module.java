package com.service.keylessrn;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.service.keylessrn.model.LoginResponseModel;
import com.service.keylessrn.utility.Constants;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

public class V5Module extends ReactContextBaseJavaModule {

    ReactContext reactContext;
    V5AidlInterface v5AidlInterface;
    public V5Module(@Nonnull ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        initService();
    }

    @NonNull
    @Override
    public String getName() {
        return Constants.APP_NAME;
    }

    public void initService() {
        if(v5AidlInterface != null){
            return;
        }
        CountDownLatch latch = new CountDownLatch(1);
        ServiceConnection serviceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                Log.i(Constants.TAG, "Service Is Connected");
                v5AidlInterface = V5AidlInterface.Stub.asInterface(service);
                latch.countDown();
            }

            public void onServiceDisconnected(ComponentName className) {
                Log.i(Constants.TAG, "Service Is Disconnected");
                v5AidlInterface = null;
            }
        };

        Intent intent = new Intent();
        intent.setComponent(new ComponentName(Constants.SERVICE_APP_PACKAGE, Constants.SERVICE_APP_CLASS));
        this.reactContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @ReactMethod
    public void login(String email, String password, final Promise promise) {
        initService();
        Bundle bundle = new Bundle();
        bundle.putString("email", email);
        bundle.putString("password", password);
        if (v5AidlInterface != null) {
            try {
                v5AidlInterface.login(bundle, new ResponseCallback.Stub() {
                    @Override
                    public void onResponse(LoginResponseModel response) {
                        Log.i(Constants.TAG, "Received Response");
                        WritableMap map = new WritableNativeMap();

                        boolean success = response.isSuccess();
                        map.putBoolean("success", response.isSuccess());
                        if (success) {
                            map.putString("access_token", response.getAccess_token());
                            map.putString("id_token", response.getId_token());
                            map.putString("refresh_token", response.getRefresh_token());
                        } else {
                            map.putString("message", response.getMessage());
                        }
                        if (success) {
                            promise.resolve(map);
                        } else {
                            promise.reject("200", response.getMessage(), map);
                        }
                    }
                });
            } catch (Exception e) {
                promise.reject("500", e.getMessage());
            }
        } else {
            promise.reject("500", "Unable to establish connection");
        }
    }
}
