package app.thesystem.mobile;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.Build;
import java.util.Calendar;

public final class ReminderReceiver extends BroadcastReceiver {
    static final String CHANNEL="daily-check-in";
    static boolean enabled(Context c){return c.getSharedPreferences("reminder",Context.MODE_PRIVATE).getBoolean("enabled",false);}
    static PendingIntent pending(Context c){return PendingIntent.getBroadcast(c,1,new Intent(c,ReminderReceiver.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
    static void configure(Context c,boolean enabled){c.getSharedPreferences("reminder",Context.MODE_PRIVATE).edit().putBoolean("enabled",enabled).apply();AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);am.cancel(pending(c));if(enabled)schedule(c);}
    static void schedule(Context c){Calendar next=Calendar.getInstance();next.set(Calendar.HOUR_OF_DAY,18);next.set(Calendar.MINUTE,0);next.set(Calendar.SECOND,0);next.set(Calendar.MILLISECOND,0);if(next.getTimeInMillis()<=System.currentTimeMillis())next.add(Calendar.DAY_OF_YEAR,1);((AlarmManager)c.getSystemService(Context.ALARM_SERVICE)).setWindow(AlarmManager.RTC_WAKEUP,next.getTimeInMillis(),60*60*1000,pending(c));}
    static void channel(Context c){((NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(new NotificationChannel(CHANNEL,"Daily check-in",NotificationManager.IMPORTANCE_DEFAULT));}
    @Override public void onReceive(Context c,Intent intent){if(!enabled(c))return;schedule(c);Calendar now=Calendar.getInstance();int hour=now.get(Calendar.HOUR_OF_DAY);if(hour<8||hour>=22)return;if(Build.VERSION.SDK_INT>=33&&c.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)return;channel(c);PendingIntent open=PendingIntent.getActivity(c,2,new Intent(c,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);Notification n=new Notification.Builder(c,CHANNEL).setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle("THE SYSTEM").setContentText("Your daily check-in is ready.").setVisibility(Notification.VISIBILITY_SECRET).setAutoCancel(true).setContentIntent(open).build();((NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE)).notify(1,n);}
}
