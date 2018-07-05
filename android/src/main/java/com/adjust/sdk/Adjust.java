//
//  Adjust.java
//  Adjust SDK
//
//  Created by Abdullah Obaied on 2016-10-19.
//  Copyright (c) 2018 adjust GmbH. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adjust.nativemodule;

import android.util.Log;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;

import android.net.Uri;
import javax.annotation.Nullable;

import com.facebook.react.bridge.*;
import com.facebook.react.modules.core.*;

import com.adjust.sdk.*;

public class Adjust extends ReactContextBaseJavaModule implements LifecycleEventListener,
                OnAttributionChangedListener,
                OnEventTrackingSucceededListener,
                OnEventTrackingFailedListener,
                OnSessionTrackingSucceededListener,
                OnSessionTrackingFailedListener,
                OnDeeplinkResponseListener {
    private boolean attributionCallback;
    private boolean eventTrackingSucceededCallback;
    private boolean eventTrackingFailedCallback;
    private boolean sessionTrackingSucceededCallback;
    private boolean sessionTrackingFailedCallback;
    private boolean deferredDeeplinkCallback;
    private boolean shouldLaunchDeeplink = true;
    private static String TAG = "AdjustBridge";

    public Adjust(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "Adjust";
    }

    @Override
    public void initialize() {
        getReactApplicationContext().addLifecycleEventListener(this);
    }

    @Override
    public void onHostPause() {
        com.adjust.sdk.Adjust.onPause();
    }

    @Override
    public void onHostResume() {
        com.adjust.sdk.Adjust.onResume();
    }

    @Override
    public void onHostDestroy() {}

    @Override
    public void onAttributionChanged(AdjustAttribution attribution) {
        sendEvent(getReactApplicationContext(), "adjust_attribution", AdjustUtil.attributionToMap(attribution));
    }

    @Override
    public void onFinishedEventTrackingSucceeded(AdjustEventSuccess event) {
        sendEvent(getReactApplicationContext(), "adjust_eventTrackingSucceeded", AdjustUtil.eventSuccessToMap(event));
    }

    @Override
    public void onFinishedEventTrackingFailed(AdjustEventFailure event) {
        sendEvent(getReactApplicationContext(), "adjust_eventTrackingFailed", AdjustUtil.eventFailureToMap(event));
    }

    @Override
    public void onFinishedSessionTrackingSucceeded(AdjustSessionSuccess session) {
        sendEvent(getReactApplicationContext(), "adjust_sessionTrackingSucceeded", AdjustUtil.sessionSuccessToMap(session));
    }

    @Override
    public void onFinishedSessionTrackingFailed(AdjustSessionFailure session) {
        sendEvent(getReactApplicationContext(), "adjust_sessionTrackingFailed", AdjustUtil.sessionFailureToMap(session));
    }

    @Override
    public boolean launchReceivedDeeplink(Uri uri) {
        sendEvent(getReactApplicationContext(), "adjust_deferredDeeplink", AdjustUtil.deferredDeeplinkToMap(uri));
        return this.shouldLaunchDeeplink;
    }

    @ReactMethod
    public void create(ReadableMap mapConfig) {
        String appToken = null;
        String environment = null;
        String logLevel = null;
        String sdkPrefix = null;
        String userAgent = null;
        String processName = null;
        String defaultTracker = null;
        long secretId  = 0L;
        long info1 = 0L;
        long info2 = 0L;
        long info3 = 0L;
        long info4 = 0L;
        double delayStart = 0.0;
        boolean isDeviceKnown = false;
        boolean sendInBackground = false;
        boolean isLogLevelSuppress = false;
        boolean shouldLaunchDeeplink = false;
        boolean eventBufferingEnabled = false;
        boolean readMobileEquipmentIdentity = false;

        // Suppress log level
        if (checkKey(mapConfig, "logLevel")) {
            logLevel = mapConfig.getString("logLevel");
            if (logLevel.equals("SUPPRESS")) {
                isLogLevelSuppress = true;
            }
        }

        // App token
        if (checkKey(mapConfig, "appToken")) {
            appToken = mapConfig.getString("appToken");
        }

        // Environment
        if (checkKey(mapConfig, "environment")) {
            environment = mapConfig.getString("environment");
        }

        final AdjustConfig adjustConfig = new AdjustConfig(getReactApplicationContext(), appToken, environment, isLogLevelSuppress);
        if (!adjustConfig.isValid()) {
            return;
        }

        // Log level
        if (checkKey(mapConfig, "logLevel")) {
            logLevel = mapConfig.getString("logLevel");
            if (logLevel.equals("VERBOSE")) {
                adjustConfig.setLogLevel(LogLevel.VERBOSE);
            } else if (logLevel.equals("DEBUG")) {
                adjustConfig.setLogLevel(LogLevel.DEBUG);
            } else if (logLevel.equals("INFO")) {
                adjustConfig.setLogLevel(LogLevel.INFO);
            } else if (logLevel.equals("WARN")) {
                adjustConfig.setLogLevel(LogLevel.WARN);
            } else if (logLevel.equals("ERROR")) {
                adjustConfig.setLogLevel(LogLevel.ERROR);
            } else if (logLevel.equals("ASSERT")) {
                adjustConfig.setLogLevel(LogLevel.ASSERT);
            } else if (logLevel.equals("SUPPRESS")) {
                adjustConfig.setLogLevel(LogLevel.SUPRESS);
            } else {
                adjustConfig.setLogLevel(LogLevel.INFO);
            }
        }

        // Event buffering
        if (checkKey(mapConfig, "eventBufferingEnabled")) {
            eventBufferingEnabled = mapConfig.getBoolean("eventBufferingEnabled");
            adjustConfig.setEventBufferingEnabled(eventBufferingEnabled);
        }

        // SDK prefix
        if (checkKey(mapConfig, "sdkPrefix")) {
            sdkPrefix = mapConfig.getString("sdkPrefix");
            adjustConfig.setSdkPrefix(sdkPrefix);
        }

        // Main process name
        if (checkKey(mapConfig, "processName")) {
            processName = mapConfig.getString("processName");
            adjustConfig.setProcessName(processName);
        }

        // Default tracker
        if (checkKey(mapConfig, "defaultTracker")) {
            defaultTracker = mapConfig.getString("defaultTracker");
            adjustConfig.setDefaultTracker(defaultTracker);
        }

        // User agent
        if (checkKey(mapConfig, "userAgent")) {
            userAgent = mapConfig.getString("userAgent");
            adjustConfig.setUserAgent(userAgent);
        }

        // App secret
        if (checkKey(mapConfig, "secretId")
                && checkKey(mapConfig, "info1")
                && checkKey(mapConfig, "info2")
                && checkKey(mapConfig, "info3")
                && checkKey(mapConfig, "info4")) {
            try {
                secretId = Long.parseLong(mapConfig.getString("secretId"), 10);
                info1 = Long.parseLong(mapConfig.getString("info1"), 10);
                info2 = Long.parseLong(mapConfig.getString("info2"), 10);
                info3 = Long.parseLong(mapConfig.getString("info3"), 10);
                info4 = Long.parseLong(mapConfig.getString("info4"), 10);
                adjustConfig.setAppSecret(secretId, info1, info2, info3, info4);
            } catch(NumberFormatException ignore) { }
        }

        // Background tracking
        if (checkKey(mapConfig, "sendInBackground")) {
            sendInBackground = mapConfig.getBoolean("sendInBackground");
            adjustConfig.setSendInBackground(sendInBackground);
        }

        // Set device Known
        if (checkKey(mapConfig, "isDeviceKnown")) {
            isDeviceKnown = mapConfig.getBoolean("isDeviceKnown");
            adjustConfig.setDeviceKnown(isDeviceKnown);
        }

        // Set read mobile equipment id
        if (checkKey(mapConfig, "readMobileEquipmentIdentity")) {
            readMobileEquipmentIdentity = mapConfig.getBoolean("readMobileEquipmentIdentity");
            adjustConfig.setReadMobileEquipmentIdentity(readMobileEquipmentIdentity);
        }

        // Launching deferred deep link
        if (checkKey(mapConfig, "shouldLaunchDeeplink")) {
            shouldLaunchDeeplink = mapConfig.getBoolean("shouldLaunchDeeplink");
            this.shouldLaunchDeeplink = shouldLaunchDeeplink;
        }

        // Delayed start
        if (checkKey(mapConfig, "delayStart")) {
            delayStart = mapConfig.getDouble("delayStart");
            adjustConfig.setDelayStart(delayStart);
        }

        // Attribution callback
        if (attributionCallback) {
            adjustConfig.setOnAttributionChangedListener(this);
        }

        // Event tracking succeeded callback
        if (eventTrackingSucceededCallback) {
            adjustConfig.setOnEventTrackingSucceededListener(this);
        }

        // Event tracking failed callback
        if (eventTrackingFailedCallback) {
            adjustConfig.setOnEventTrackingFailedListener(this);
        }

        // Session tracking succeeded callback
        if (sessionTrackingSucceededCallback) {
            adjustConfig.setOnSessionTrackingSucceededListener(this);
        }

        // Session tracking failed callback
        if (sessionTrackingFailedCallback) {
            adjustConfig.setOnSessionTrackingFailedListener(this);
        }

        // Deferred deeplink callback listener
        if (deferredDeeplinkCallback) {
            adjustConfig.setOnDeeplinkResponseListener(this);
        }

        com.adjust.sdk.Adjust.onCreate(adjustConfig);
        com.adjust.sdk.Adjust.onResume();
    }

    @ReactMethod
    public void trackEvent(ReadableMap mapEvent) {
        double revenue = -1.0;
        String eventToken = null;
        String currency = null;
        String transactionId = null;
        Map<String, Object> callbackParameters = null;
        Map<String, Object> partnerParameters = null;

        // Event token
        if (checkKey(mapEvent, "eventToken")) {
            eventToken = mapEvent.getString("eventToken");
        }

        final AdjustEvent event = new AdjustEvent(eventToken);
        if (!event.isValid()) {
            return;
        }

        // Revenue
        if (checkKey(mapEvent, "revenue")) {
            try {
                revenue = Double.parseDouble(mapEvent.getString("revenue"));
                if (checkKey(mapEvent, "currency")) {
                    currency = mapEvent.getString("currency");
                    event.setRevenue(revenue, currency);
                }
            } catch(NumberFormatException ignore) { }
        }

        // Callback parameters
        if (checkKey(mapEvent, "callbackParameters")) {
            callbackParameters = AdjustUtil.toMap(mapEvent.getMap("callbackParameters"));
            if (null != callbackParameters) {
                for (Map.Entry<String, Object> entry : callbackParameters.entrySet()) {
                    event.addCallbackParameter(entry.getKey(), entry.getValue().toString());
                }
            }
        }

        // Partner parameters
        if (checkKey(mapEvent, "partnerParameters")) {
            partnerParameters = AdjustUtil.toMap(mapEvent.getMap("partnerParameters"));
            if (null != partnerParameters) {
                for (Map.Entry<String, Object> entry : partnerParameters.entrySet()) {
                    event.addPartnerParameter(entry.getKey(), entry.getValue().toString());
                }
            }
        }

        // Revenue deduplication
        if (checkKey(mapEvent, "transactionId")) {
            transactionId = mapEvent.getString("transactionId");
            if (null != transactionId) {
                event.setOrderId(transactionId);
            }
        }

        com.adjust.sdk.Adjust.trackEvent(event);
    }

    @ReactMethod
    public void setEnabled(Boolean enabled) {
        com.adjust.sdk.Adjust.setEnabled(enabled);
    }

    @ReactMethod
    public void isEnabled(Callback callback) {
        callback.invoke(com.adjust.sdk.Adjust.isEnabled());
    }

    @ReactMethod
    public void setReferrer(String referrer) {
        com.adjust.sdk.Adjust.setReferrer(referrer, getReactApplicationContext());
    }

    @ReactMethod
    public void setOfflineMode(Boolean enabled) {
        com.adjust.sdk.Adjust.setOfflineMode(enabled);
    }

    @ReactMethod
    public void setPushToken(String token) {
        com.adjust.sdk.Adjust.setPushToken(token, getReactApplicationContext());
    }

    @ReactMethod
    public void appWillOpenUrl(String strUri) {
        final Uri uri = Uri.parse(strUri);
        com.adjust.sdk.Adjust.appWillOpenUrl(uri, getReactApplicationContext());
    }

    @ReactMethod
    public void sendFirstPackages() {
        com.adjust.sdk.Adjust.sendFirstPackages();
    }

    @ReactMethod
    public void addSessionCallbackParameter(String key, String value) {
        com.adjust.sdk.Adjust.addSessionCallbackParameter(key, value);
    }

    @ReactMethod
    public void addSessionPartnerParameter(String key, String value) {
        com.adjust.sdk.Adjust.addSessionPartnerParameter(key, value);
    }

    @ReactMethod
    public void removeSessionCallbackParameter(String key) {
        com.adjust.sdk.Adjust.removeSessionCallbackParameter(key);
    }

    @ReactMethod
    public void removeSessionPartnerParameter(String key) {
        com.adjust.sdk.Adjust.removeSessionPartnerParameter(key);
    }

    @ReactMethod
    public void resetSessionCallbackParameters() {
        com.adjust.sdk.Adjust.resetSessionCallbackParameters();
    }

    @ReactMethod
    public void resetSessionPartnerParameters() {
        com.adjust.sdk.Adjust.resetSessionPartnerParameters();
    }

    @ReactMethod
    public void gdprForgetMe() {
        com.adjust.sdk.Adjust.gdprForgetMe(getReactApplicationContext());
    }

    @ReactMethod
    public void getIdfa(Callback callback) {
        callback.invoke("");
    }

    @ReactMethod
    public void getGoogleAdId(final Callback callback) {
        com.adjust.sdk.Adjust.getGoogleAdId(getReactApplicationContext(), new com.adjust.sdk.OnDeviceIdsRead() {
            @Override
            public void onGoogleAdIdRead(String googleAdId) {
                callback.invoke(googleAdId);
            }
        });
    }

    @ReactMethod
    public void getAdid(Callback callback) {
        callback.invoke(com.adjust.sdk.Adjust.getAdid());
    }

    @ReactMethod
    public void getAmazonAdId(Callback callback) {
        callback.invoke(com.adjust.sdk.Adjust.getAmazonAdId(getReactApplicationContext()));
    }

    @ReactMethod
    public void getAttribution(Callback callback) {
        callback.invoke(AdjustUtil.attributionToMap(com.adjust.sdk.Adjust.getAttribution()));
    }

    @ReactMethod
    public void setAttributionCallbackListener() {
        this.attributionCallback = true;
    }

    @ReactMethod
    public void setEventTrackingSucceededCallbackListener() {
        this.eventTrackingSucceededCallback = true;
    }

    @ReactMethod
    public void setEventTrackingFailedCallbackListener() {
        this.eventTrackingFailedCallback = true;
    }

    @ReactMethod
    public void setSessionTrackingSucceededCallbackListener() {
        this.sessionTrackingSucceededCallback = true;
    }

    @ReactMethod
    public void setSessionTrackingFailedCallbackListener() {
        this.sessionTrackingFailedCallback = true;
    }

    @ReactMethod
    public void setDeferredDeeplinkCallbackListener() {
        this.deferredDeeplinkCallback = true;
    }

    @ReactMethod
    public void teardown() {
        this.attributionCallback = false;
        this.eventTrackingSucceededCallback = false;
        this.eventTrackingFailedCallback = false;
        this.sessionTrackingSucceededCallback = false;
        this.sessionTrackingFailedCallback = false;
        this.deferredDeeplinkCallback = false;
    }

    @ReactMethod
    public void setTestOptions(ReadableMap map) {
        final AdjustTestOptions testOptions = new AdjustTestOptions();
        if (checkKey(map, "hasContext")) {
            boolean value = map.getBoolean("hasContext");
            if (value) {
                testOptions.context = getReactApplicationContext();
            }
        }
        if (checkKey(map, "baseUrl")) {
            String value = map.getString("baseUrl");
            testOptions.baseUrl = value;
        }
        if (checkKey(map, "gdprUrl")) {
            String value = map.getString("gdprUrl");
            testOptions.gdprUrl = value;
        }
        if (checkKey(map, "basePath")) {
            String value = map.getString("basePath");
            testOptions.basePath = value;
        }
        if (checkKey(map, "gdprPath")) {
            String value = map.getString("gdprPath");
            testOptions.gdprPath = value;
        }
        if (checkKey(map, "useTestConnectionOptions")) {
            boolean value = map.getBoolean("useTestConnectionOptions");
            testOptions.useTestConnectionOptions = value;
        }
        if (checkKey(map, "timerIntervalInMilliseconds")) {
            try {
                Long value = Long.parseLong(map.getString("timerIntervalInMilliseconds"));
                testOptions.timerIntervalInMilliseconds = value;
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                Log.d(TAG, "Can't format number");
            }
        }
        if (checkKey(map, "timerStartInMilliseconds")) {
            try {
                Long value = Long.parseLong(map.getString("timerStartInMilliseconds"));
                testOptions.timerStartInMilliseconds = value;
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                Log.d(TAG, "Can't format number");
            }
        }
        if (checkKey(map, "sessionIntervalInMilliseconds")) {
            try {
                Long value = Long.parseLong(map.getString("sessionIntervalInMilliseconds"));
                testOptions.sessionIntervalInMilliseconds = value;
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                Log.d(TAG, "Can't format number");
            }
        }
        if (checkKey(map, "subsessionIntervalInMilliseconds")) {
            try {
                Long value = Long.parseLong(map.getString("subsessionIntervalInMilliseconds"));
                testOptions.subsessionIntervalInMilliseconds = value;
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                Log.d(TAG, "Can't format number");
            }
        }
        if (checkKey(map, "noBackoffWait")) {
            boolean value = map.getBoolean("noBackoffWait");
            testOptions.noBackoffWait = value;
        }
        if (checkKey(map, "teardown")) {
            boolean value = map.getBoolean("teardown");
            testOptions.teardown = value;
        }

        com.adjust.sdk.Adjust.setTestOptions(testOptions);
    }

    @ReactMethod
    public void onResume() {
        com.adjust.sdk.Adjust.onResume();
    }

    @ReactMethod
    public void onPause() {
        com.adjust.sdk.Adjust.onPause();
    }

    private void sendEvent(ReactContext reactContext, String eventName, @Nullable WritableMap params) {
        reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit(eventName, params);
    }

    private boolean checkKey(ReadableMap config, String key) {
        if (config == null) {
            return false;
        }

        return config.hasKey(key) && !config.isNull(key);
    }
}
