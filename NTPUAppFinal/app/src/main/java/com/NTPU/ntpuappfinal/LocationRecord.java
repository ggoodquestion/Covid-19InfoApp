package com.NTPU.ntpuappfinal;

import android.app.AlarmManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class LocationRecord extends Service {

    public LocationBinder binder;

    public LocationRecord() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        return super.onStartCommand(intent, flags, startId);
        //AlarmManager alarmManager;
        //alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);

        //return 0;
    }

    class LocationBinder extends Binder{
        public void startRecord(){

        }
    }
}