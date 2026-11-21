package com.aichat.android;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import org.json.JSONObject;
import java.util.Locale;

public class MainActivity extends Activity {
    private WebView webView;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        webView = new WebView(this);
        webView.setBackgroundColor(0xFF10151D);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        webView.addJavascriptInterface(new PhoneBridge(), "phoneAPI");
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        setContentView(webView);
        webView.loadUrl("file:///android_asset/index.html");
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    public class PhoneBridge {
        @JavascriptInterface public boolean isAndroid() { return true; }

        @JavascriptInterface public boolean openApp(String query) {
            if (query == null || query.trim().isEmpty()) return false;
            String q = query.trim();
            PackageManager pm = getPackageManager();
            try {
                Intent direct = pm.getLaunchIntentForPackage(q);
                if (direct != null) { startActivity(direct); return true; }
            } catch (Exception ignored) {}
            try {
                Intent launcher = new Intent(Intent.ACTION_MAIN);
                launcher.addCategory(Intent.CATEGORY_LAUNCHER);
                for (android.content.pm.ResolveInfo ri : pm.queryIntentActivities(launcher, 0)) {
                    CharSequence label = ri.loadLabel(pm);
                    if (label != null && label.toString().toLowerCase(Locale.ROOT).contains(q.toLowerCase(Locale.ROOT))) {
                        Intent launch = pm.getLaunchIntentForPackage(ri.activityInfo.packageName);
                        if (launch != null) { startActivity(launch); return true; }
                    }
                }
            } catch (Exception ignored) {}
            return false;
        }

        @JavascriptInterface public boolean openSettings(String section) {
            if (section == null) return false;
            Intent i;
            switch (section.toLowerCase(Locale.ROOT).trim()) {
                case "wifi": case "internet": i = new Intent(Settings.ACTION_WIFI_SETTINGS); break;
                case "bluetooth": i = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS); break;
                case "display": i = new Intent(Settings.ACTION_DISPLAY_SETTINGS); break;
                case "sound": i = new Intent(Settings.ACTION_SOUND_SETTINGS); break;
                case "location": i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS); break;
                case "apps": i = new Intent(Settings.ACTION_APPLICATION_SETTINGS); break;
                case "app-details": i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName())); break;
                default: return false;
            }
            try { startActivity(i); return true; } catch (Exception e) { return false; }
        }

        @JavascriptInterface public String getDeviceInfo() {
            JSONObject o = new JSONObject();
            try {
                o.put("Производитель", Build.MANUFACTURER);
                o.put("Модель", Build.MODEL);
                o.put("Android", Build.VERSION.RELEASE);
                o.put("API", Build.VERSION.SDK_INT);
                o.put("Устройство", Build.DEVICE);
            } catch (Exception ignored) {}
            return o.toString();
        }
    }
}
