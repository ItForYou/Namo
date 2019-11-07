package co.kr.itforone.namoint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.apache.http.util.EncodingUtils;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.function.ToLongBiFunction;

import Common.Common;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.webview) WebView webView;
    private long backPrssedTime = 0;
    final int FILECHOOSER_LOLLIPOP_REQ_CODE=1300;
    ValueCallback<Uri> filePathCallbackNormal;
    ValueCallback<Uri[]> filePathCallbackLollipop;
    Uri mCapturedImageURI;
    SharedPreferences pref_login;
    String fcmUrl= "",mb_id,mb_pwd;
    public static final String GOOGLE_ACCOUNT="google_account";
    GoogleSignInOptions gso;
    GoogleSignInClient mGoogleSignInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            ButterKnife.bind(this);

                Intent splash = new Intent(MainActivity.this,SplashActivity.class);
                startActivity(splash);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                CookieSyncManager.createInstance(this);
            }



            try{

                Intent i = getIntent();

                String tmp_gourl= i.getExtras().getString("goUrl");
                if(!tmp_gourl.isEmpty()) {
                    fcmUrl = tmp_gourl;
                }
            }
            catch (Exception e){
                Log.d("error" ,String.valueOf(e));
            }

            FirebaseApp.initializeApp(this);//firebase 등록함
            FirebaseMessaging.getInstance().subscribeToTopic("namo");
            //토큰 생성
           Common.TOKEN= FirebaseInstanceId.getInstance().getToken();
            try {
                if (Common.TOKEN.equals("") || Common.TOKEN.equals(null)) {
                    //토큰 값 재생성
                    refreshToken();
                } else {

                }
            }catch (Exception e){
                //토큰 값 재생성
                refreshToken();
            }



            WebSettings settings = webView.getSettings();
            settings.setCacheMode(WebSettings.LOAD_DEFAULT);
            settings.setJavaScriptEnabled(true);
            webView.addJavascriptInterface(new WebviewJavainterface(),"Android");
            webView.setWebViewClient(new Viewmanager(this));
            webView.setWebChromeClient(new WebchromeClient(this, this));

            pref_login = getSharedPreferences("pref_profile", MODE_PRIVATE);
            mb_id = pref_login.getString("mb_id", "");
            mb_pwd = pref_login.getString("mb_pwd", "");

        if(!fcmUrl.isEmpty())
        {
            webView.loadUrl(fcmUrl);
        }
     /*   else if(!mb_id.isEmpty() && !mb_pwd.isEmpty()){

            String url = getString(R.string.loginchk);
            String postData = "mb_id="+mb_id+"&mb_password="+mb_pwd;
            webView.postUrl(url, EncodingUtils.getBytes(postData, "BASE64"));

        }*/
        else
            webView.loadUrl(getString(R.string.index));
    }

    @Override
    public void onStart() {
        super.onStart();
        GoogleSignInAccount alreadyloggedAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (alreadyloggedAccount != null) {

        } else {
            Log.d("Tag1", "Not logged in");
        }
    }


    private void refreshToken(){
        FirebaseMessaging.getInstance().subscribeToTopic("namo");
        Common.TOKEN= FirebaseInstanceId.getInstance().getToken();
    }

    public void set_filePathCallbackLollipop(ValueCallback<Uri[]> filePathCallbackLollipop){
        this.filePathCallbackLollipop = filePathCallbackLollipop;
    }

    @Override
    protected void onResume() {
        super.onResume();
        /* cookie */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().startSync();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        /* cookie */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().stopSync();
        }
    }


    @Override
    public void onBackPressed(){
        if(webView.canGoBack()){
//            String url = webView.copyBackForwardList().getItemAtIndex(webView.copyBackForwardList().getCurrentIndex()-1).getUrl();
//            webView.loadUrl(url);
            webView.goBack();
        }else{
            long tempTime = System.currentTimeMillis();
            long intervalTime = tempTime - backPrssedTime;
            if (0 <= intervalTime && 2000 >= intervalTime){
                finish();
            }
            else
            {
                backPrssedTime = tempTime;
                Toast.makeText(getApplicationContext(), "한번 더 뒤로가기 누를시 앱이 종료됩니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 101:
                try {
                    // The Task returned from this call is always completed, no need to attach
                    // a listener.
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    webView.loadUrl(getString(R.string.register)+"sns_name=" +account.getDisplayName() +"&sns_email="+account.getEmail());
                   // onLoggedIn(account);

                } catch (ApiException e) {
                    // The ApiException status code indicates the detailed failure reason.
                    Log.w("fail", "signInResult:failed code=" + e.getStatusCode());
                }
                break;
            case WebchromeClient.FILECHOOSER_LOLLIPOP_REQ_CODE:
                Uri[] result = new Uri[0];
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (resultCode == RESULT_OK) {
                        result = (data == null) ? new Uri[]{mCapturedImageURI} : WebChromeClient.FileChooserParams.parseResult(resultCode, data);
                    }
                    filePathCallbackLollipop.onReceiveValue(result);
                }
                    break;

        }

     /*   if(requestCode==WebchromeClient.FILECHOOSER_LOLLIPOP_REQ_CODE){
            Uri[] result = new Uri[0];
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if(resultCode == RESULT_OK){
                    result = (data == null) ? new Uri[]{mCapturedImageURI} : WebChromeClient.FileChooserParams.parseResult(resultCode, data);
            }
                filePathCallbackLollipop.onReceiveValue(result);
            }
        }*/
    }

    private void onLoggedIn(GoogleSignInAccount googleSignInAccount) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.GOOGLE_ACCOUNT, googleSignInAccount);
        startActivity(intent);
        finish();
    }

    private class WebviewJavainterface {

        @JavascriptInterface
        public void login_google() {
            gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();

            mGoogleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, 101);

//            Intent i_google = new Intent(MainActivity.this,GooglesigninActivity.class);
//            startActivity(i_google);
        }

        @JavascriptInterface
        public void share(String id, String table) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "http://namoint.itforone.co.kr/bbs/board.php?bo_table="+table+"&wr_id=" + id);
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        }
        @JavascriptInterface
        public void call_app() {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=co.kr.itforone.namoint");
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        }
        @JavascriptInterface
        public void save_idinfo(String id, String pwd) {

            Response.Listener<String> responseListener = new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        boolean success = jsonResponse.getBoolean("success");
                        if (success) {
                            SharedPreferences pref = getSharedPreferences("pref_profile", MODE_PRIVATE);
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("mb_id", id);
                            editor.putString("mb_pwd", pwd);
                            editor.commit();
                        }
                        else{
                           // Toast.makeText(getApplicationContext(),"실패",Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            LoginRequest loginrequest = new LoginRequest(id, pwd, responseListener);
            RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
            queue.add(loginrequest);
        }
    }
}


