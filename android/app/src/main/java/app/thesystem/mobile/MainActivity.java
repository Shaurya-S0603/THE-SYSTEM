package app.thesystem.mobile;

import android.Manifest;
import android.app.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.*;
import android.view.*;
import android.webkit.*;
import androidx.webkit.*;
import java.io.ByteArrayInputStream;
import java.util.*;
import org.json.*;

public final class MainActivity extends Activity {
    private static final String ORIGIN="https://appassets.androidplatform.net";
    private WebView web;
    private Vault vault;
    @Override public void onCreate(Bundle state){super.onCreate(state);getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);vault=new Vault(this);web=new WebView(this);web.setBackgroundColor(Color.rgb(10,11,16));setContentView(web);
        web.setOnApplyWindowInsetsListener((v,insets)->{android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout()|WindowInsets.Type.ime());v.setPadding(bars.left,bars.top,bars.right,bars.bottom);return WindowInsets.CONSUMED;});
        WebSettings settings=web.getSettings();settings.setJavaScriptEnabled(true);settings.setDomStorageEnabled(false);settings.setAllowFileAccess(false);settings.setAllowContentAccess(false);settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);settings.setJavaScriptCanOpenWindowsAutomatically(false);settings.setSupportMultipleWindows(false);settings.setMediaPlaybackRequiresUserGesture(true);settings.setTextZoom(Math.round(100*getResources().getConfiguration().fontScale));
        CookieManager.getInstance().setAcceptCookie(false);WebView.setWebContentsDebuggingEnabled(false);
        WebViewAssetLoader loader=new WebViewAssetLoader.Builder().addPathHandler("/assets/",new WebViewAssetLoader.AssetsPathHandler(this)).build();
        Set<String> paths=new HashSet<>(Arrays.asList("/assets/index.html","/assets/engine.js","/assets/app.js","/assets/app.css"));
        web.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest request){return !trusted(request.getUrl().toString());}
            @Override public WebResourceResponse shouldInterceptRequest(WebView view,WebResourceRequest request){if(!trusted(request.getUrl().toString())||!paths.contains(request.getUrl().getPath())||!"GET".equals(request.getMethod()))return new WebResourceResponse("text/plain","UTF-8",403,"Blocked",Collections.emptyMap(),new ByteArrayInputStream(new byte[0]));return loader.shouldInterceptRequest(request.getUrl());}
            @Override public void onReceivedSslError(WebView view,android.webkit.SslErrorHandler handler,android.net.http.SslError error){handler.cancel();}
        });
        if(!WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)){new AlertDialog.Builder(this).setTitle("WebView update needed").setMessage("Update Android System WebView to open THE SYSTEM safely.").setPositiveButton("Close",(d,w)->finish()).setCancelable(false).show();return;}
        WebViewCompat.addWebMessageListener(web,"SystemNative",Collections.singleton(ORIGIN),(view,message,sourceOrigin,isMainFrame,reply)->{
            if(!isMainFrame||!ORIGIN.equals(sourceOrigin.toString()))return;
            JSONObject result=new JSONObject();
            try{
                String raw=message.getData();if(raw==null||raw.length()>270000)throw new IllegalArgumentException("Request too large");JSONObject request=new JSONObject(raw);StateSchema.exact(request,"id","action","payload");StateSchema.number(request.get("id"),1,1000000000);result.put("id",request.getInt("id"));Object value=JSONObject.NULL;
                switch(request.getString("action")){
                case "load":if(!request.isNull("payload"))throw new IllegalArgumentException();String saved=vault.read();if(saved!=null){JSONObject data=new JSONObject(saved);StateSchema.validate(data);value=data;}break;
                case "save":JSONObject data=request.getJSONObject("payload");StateSchema.validate(data);vault.write(data.toString());value=true;break;
                case "erase":if(!request.isNull("payload"))throw new IllegalArgumentException();ReminderReceiver.configure(this,false);((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancelAll();vault.erase();value=true;break;
                case "reminder":StateSchema.choice(request.get("payload"),"off","18:00");ReminderReceiver.configure(this,"18:00".equals(request.getString("payload")));value=true;break;
                case "permission":if(!request.isNull("payload"))throw new IllegalArgumentException();if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},7);value="requested";}else value="granted";break;
                default:throw new IllegalArgumentException("Unknown command");
                }
                result.put("ok",true);result.put("value",value);
            }catch(Exception e){try{result.put("ok",false);result.put("error",e instanceof IllegalArgumentException?StateSchema.METADATA:"Local data operation failed. No content was logged.");}catch(JSONException ignored){/* no payload logging */}}
            reply.postMessage(result.toString());
        });
        ReminderReceiver.channel(this);if(ReminderReceiver.enabled(this))ReminderReceiver.schedule(this);web.loadUrl(ORIGIN+"/assets/index.html");
    }
    static boolean trusted(String url){android.net.Uri uri=android.net.Uri.parse(url);return "https".equals(uri.getScheme())&&"appassets.androidplatform.net".equals(uri.getHost())&&uri.getPort()==-1&&uri.getUserInfo()==null&&uri.getPath()!=null&&uri.getPath().startsWith("/assets/");}
    @Override protected void onPause(){if(web!=null){web.evaluateJavascript("document.dispatchEvent(new Event('systemPause'))",null);web.onPause();}super.onPause();}
    @Override protected void onResume(){super.onResume();if(web!=null)web.onResume();}
    @Override protected void onDestroy(){if(web!=null){WebViewCompat.removeWebMessageListener(web,"SystemNative");web.destroy();}super.onDestroy();}
}
