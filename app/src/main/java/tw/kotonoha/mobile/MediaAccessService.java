package tw.kotonoha.mobile;
import android.service.notification.NotificationListenerService;
// Required by Android to authorize MediaSessionManager access. Deliberately does not read, store, or forward notification contents.
public final class MediaAccessService extends NotificationListenerService { }
