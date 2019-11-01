package co.kr.itforone.namoint;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import Common.Common;

class Viewmanager extends WebViewClient {
    MainActivity mainActivity;
    Viewmanager(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }
    Viewmanager(){}

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);
        return true;
    }

    @Override
    public void onPageFinished(WebView view, String url) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().sync();
        } else {
            CookieManager.getInstance().flush();
        }
        super.onPageFinished(view, url);
        if(!Common.TOKEN.isEmpty()){
            view.loadUrl("javascript:setToken('"+Common.TOKEN+"')");
        }
    }
}

