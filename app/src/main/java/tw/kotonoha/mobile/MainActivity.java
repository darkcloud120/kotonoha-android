package tw.kotonoha.mobile;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.ViewGroup;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.graphics.drawable.GradientDrawable;
import android.view.WindowManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.ValueCallback;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONObject;
import java.util.List;

public final class MainActivity extends Activity {
    private static final String SITE = "https://darkcloud120.github.io/kotonoha/";
    private static final String HOST = "darkcloud120.github.io";
    private static final String MUSIC = "com.google.android.apps.youtube.music";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private WebView web;
    private TextView status; // detached diagnostic text, visible in menu
    private Button permission;
    private boolean running = false, ready = false;
    private ValueCallback<Uri[]> fileCallback;
    private final int PICK_FILE = 51;

    private boolean trusted(String url) {
        if (url == null) return false;
        Uri u = Uri.parse(url);
        String path = u.getPath();
        return "https".equals(u.getScheme()) && HOST.equalsIgnoreCase(u.getHost())
                && (u.getPort() == -1 || u.getPort() == 443) && u.getUserInfo() == null
                && ("/kotonoha/".equals(path) || "/kotonoha/index.html".equals(path) || "/kotonoha".equals(path));
    }
    private Button button(String label, Runnable action) {
        Button b = new Button(this); b.setText(label); b.setAllCaps(false);
        b.setTextSize(12); b.setOnClickListener(v -> action.run());
        return b;
    }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(16,19,28));
        getWindow().setNavigationBarColor(Color.rgb(16,19,28));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.rgb(16,19,28));
        status = new TextView(this);
        status.setText("等待授權");
        permission = button("⋮", () -> showMenu());
        permission.setContentDescription("手機同步選單");
        permission.setTextSize(23); permission.setPadding(0,0,0,0);
        permission.setMinWidth(0); permission.setMinimumWidth(0);
        permission.setMinHeight(0); permission.setMinimumHeight(0);
        permission.setTextColor(Color.rgb(219,207,255));
        GradientDrawable menuBg = new GradientDrawable();
        menuBg.setColor(Color.rgb(36,34,51)); menuBg.setCornerRadius(dp(12));
        permission.setBackground(menuBg);
        web = new WebView(this);
        web.setBackgroundColor(Color.rgb(16,19,28));
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setAllowFileAccess(false);
        web.getSettings().setAllowContentAccess(false);
        web.getSettings().setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        web.getSettings().setSupportMultipleWindows(false);
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                if (!req.isForMainFrame()) return false;
                String url = req.getUrl().toString();
                if (trusted(url)) return false;
                String scheme = req.getUrl().getScheme();
                if ("https".equals(scheme) || "http".equals(scheme)) {
                    try { startActivity(new Intent(Intent.ACTION_VIEW, req.getUrl())); } catch (Exception ignored) {}
                }
                return true;
            }
            @Override public void onPageStarted(WebView v, String url, android.graphics.Bitmap icon) { ready=false; }
            @Override public void onPageFinished(WebView v, String url) {
                ready=trusted(url);
                if (ready) decorate();
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> cb, FileChooserParams params) {
                if (!trusted(v.getUrl())) return false;
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback=cb;
                try { startActivityForResult(params.createIntent(), PICK_FILE); return true; }
                catch (Exception ex) { fileCallback=null; return false; }
            }
        });
        root.addView(web,new FrameLayout.LayoutParams(-1,-1));
        FrameLayout.LayoutParams mp=new FrameLayout.LayoutParams(dp(38),dp(38),Gravity.TOP|Gravity.END);
        mp.setMargins(0,dp(9),dp(9),0);root.addView(permission,mp);
        setContentView(root);
        web.loadUrl(SITE);
        if(!granted()) new android.app.AlertDialog.Builder(this).setTitle("啟用音樂同步").setMessage("授予通知存取權限，才能讀取 YouTube Music 的播放資訊。也可稍後從右上角選單授權。").setPositiveButton("授權",(d,w)->startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))).setNegativeButton("稍後",null).show();
    }
    private int dp(int value){return Math.round(value*getResources().getDisplayMetrics().density);}
    private void showMenu(){
        PopupMenu menu=new PopupMenu(this,permission);
        menu.getMenu().add(0,1,0,granted()?"同步權限：已授權":"授權同步");
        menu.getMenu().add(0,2,1,"開啟 YouTube Music");
        menu.getMenu().add(0,3,2,"重新整理歌詞網站");
        menu.getMenu().add(0,4,3,"同步狀態");
        menu.setOnMenuItemClickListener(item->{switch(item.getItemId()){
            case 1:startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));return true;
            case 2:openMusic();return true;
            case 3:ready=false;web.loadUrl(SITE);return true;
            case 4:new android.app.AlertDialog.Builder(this).setTitle("同步狀態").setMessage(status.getText()).setPositiveButton("關閉",null).show();return true;
            default:return false;}});menu.show();
    }
    private void decorate() {
        // Reuse the existing v0.4 page; no privileged JS bridge or new server.
        web.evaluateJavascript("(()=>{if(location.origin!=="+JSONObject.quote("https://"+HOST)+"||!['/kotonoha/','/kotonoha/index.html','/kotonoha'].includes(location.pathname))return;"
            +"if(document.getElementById('android-style'))return;const s=document.createElement('style');s.id='android-style';s.textContent="
            +JSONObject.quote("header{padding-right:56px!important} body.focus-mode main{padding-top:48px} body.focus-mode .player{height:calc(100dvh - 56px)} @media(max-width:720px){aside>.panel:nth-child(2){order:2} header{padding-top:10px} }")
            +";document.head.append(s);const h=document.querySelector('aside>.panel h2');if(h)h.textContent='手機自動同步';const brand=document.querySelector('.brand small');if(brand)brand.textContent='KOTONOHA / Android 1.0 RC1';})()",null);
    }
    private void openMusic() {
        Intent intent=getPackageManager().getLaunchIntentForPackage(MUSIC);
        if(intent!=null) startActivity(intent);
        else try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://music.youtube.com/"))); } catch(Exception ignored) {}
    }
    private boolean granted() {
        return getSystemService(NotificationManager.class).isNotificationListenerAccessGranted(new ComponentName(this,MediaAccessService.class));
    }
    private static String str(MediaMetadata m,String key) {String s=m.getString(key); return s==null?"":s.trim();}
    private static boolean playing(PlaybackState s) {return s!=null && s.getState()==PlaybackState.STATE_PLAYING;}
    private MediaController choose(List<MediaController> list) {
        MediaController fallback=null;
        for(MediaController c:list) {
            if(!MUSIC.equals(c.getPackageName()) || c.getMetadata()==null || c.getPlaybackState()==null)continue;
            if(playing(c.getPlaybackState()))return c;
            if(fallback==null)fallback=c;
        }
        return fallback;
    }
    private void poll() {
        if(!granted()){permission.setText("⋮");status.setText("尚未授權：點上方授權同步，允許通知存取。");return;}
        permission.setText("⋮");
        try {
            MediaSessionManager manager=getSystemService(MediaSessionManager.class);
            MediaController c=choose(manager.getActiveSessions(new ComponentName(this,MediaAccessService.class)));
            if(c==null){status.setText("等待 YouTube Music 播放歌曲…");return;}
            MediaMetadata m=c.getMetadata(); PlaybackState s=c.getPlaybackState();
            if(m==null||s==null)return;
            String title=str(m,MediaMetadata.METADATA_KEY_TITLE);
            if(title.isEmpty())title=str(m,MediaMetadata.METADATA_KEY_DISPLAY_TITLE);
            String artist=str(m,MediaMetadata.METADATA_KEY_ARTIST);
            if(artist.isEmpty())artist=str(m,MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE);
            if(artist.isEmpty())artist=str(m,MediaMetadata.METADATA_KEY_ALBUM_ARTIST);
            if(title.isEmpty()){status.setText("YT Music 尚未提供歌名");return;}
            title=title.substring(0,Math.min(500,title.length()));artist=artist.substring(0,Math.min(500,artist.length()));
            long duration=Math.max(0,m.getLong(MediaMetadata.METADATA_KEY_DURATION));
            long raw=s.getPosition(); boolean isPlaying=playing(s);
            double speed=s.getPlaybackSpeed(); if(!Double.isFinite(speed)||speed<=0)speed=1;
            double position=Math.max(0,raw);
            if(isPlaying && raw>=0 && s.getLastPositionUpdateTime()>0) position+=Math.max(0,SystemClock.elapsedRealtime()-s.getLastPositionUpdateTime())*speed;
            if(duration>0)position=Math.min(position,duration);
            status.setText((isPlaying?"播放中 · ":"暫停 / 等待 · ")+title+(raw<0?"（系統未提供進度）":""));
            if(!ready||!trusted(web.getUrl()))return;
            JSONObject payload=new JSONObject();payload.put("source","android");payload.put("title",title);payload.put("artist",artist);
            payload.put("videoId",str(m,MediaMetadata.METADATA_KEY_MEDIA_ID));
            payload.put("position",position/1000.0);payload.put("duration",duration/1000.0);
            payload.put("playing",isPlaying && raw>=0);payload.put("speed",speed);
            payload.put("observedAt",System.currentTimeMillis());payload.put("ad",false);
            JSONObject msg=new JSONObject();msg.put("type","kotonoha-extension-v03");msg.put("state",payload);
            String js="if(location.origin==="+JSONObject.quote("https://"+HOST)+"&&['/kotonoha/','/kotonoha/index.html','/kotonoha'].includes(location.pathname))window.postMessage(JSON.parse("+JSONObject.quote(msg.toString())+"),location.origin);";
            web.evaluateJavascript(js,null);
        } catch(SecurityException e){status.setText("通知存取尚未生效，請重新授權後返回。");}
          catch(Exception e){status.setText("同步暫時失敗："+e.getClass().getSimpleName());}
    }
    private final Runnable ticker=new Runnable(){@Override public void run(){if(!running)return;poll();handler.postDelayed(this,700);}};
    @Override protected void onResume(){super.onResume();running=true;web.onResume();handler.removeCallbacks(ticker);handler.post(ticker);}
    @Override protected void onPause(){running=false;handler.removeCallbacks(ticker);web.onPause();super.onPause();}
    @Override protected void onDestroy(){handler.removeCallbacks(ticker);if(fileCallback!=null)fileCallback.onReceiveValue(null);web.destroy();super.onDestroy();}
    @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request==PICK_FILE&&fileCallback!=null){fileCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(result,data));fileCallback=null;}}
}
