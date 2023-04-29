package com.service.keylessrn;
import com.service.keylessrn.LoginResponseModel;

interface ResponseCallback {
    void onResponse(in LoginResponseModel response);
}