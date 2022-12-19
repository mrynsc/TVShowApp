package com.example.tvshow.utilities;

import android.app.Application;

import com.parse.Parse;

public class ParseStarterClass extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("UBmizEhlsKPsgN73r9zj5cPhodklepRFq6YkqWql")
                .clientKey("10YcZTdgDowlqgYFk2whVa1FDznJrUG1siRs0cfR")
                .server("https://parseapi.back4app.com/")
                .build());



    }
}