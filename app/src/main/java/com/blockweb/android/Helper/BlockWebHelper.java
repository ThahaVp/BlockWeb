package com.blockweb.android.Helper;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.Browser;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.blockweb.android.Activity.AccessDenied;
import com.blockweb.android.Activity.MainActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class BlockWebHelper extends AccessibilityService {
    SharedPreferences sharedPreferences;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        String from = sharedPreferences.getString("from","");
        String to = sharedPreferences.getString("to","");
        SimpleDateFormat format = new SimpleDateFormat("h:m a");

        boolean timeOk = false;
        try
        {
            String currentTime = format.format(new Date());
            long cuTime = format.parse(currentTime).getTime();
            long frT = format.parse(from).getTime();
            long toT = format.parse(to).getTime();

            if (cuTime >= frT && cuTime < toT)
                timeOk = true;

        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (timeOk)
        {
            AccessibilityNodeInfo parentNodeInfo = event.getSource();
            if (parentNodeInfo == null)
                return;

            List<AccessibilityNodeInfo> nodes = parentNodeInfo.findAccessibilityNodeInfosByViewId("com.android.chrome:id/url_bar");
            if (nodes == null || nodes.size() <= 0)
                return;

            AccessibilityNodeInfo addressBarNodeInfo = nodes.get(0);
            String url = "";
            if (addressBarNodeInfo.getText() != null)
                url = addressBarNodeInfo.getText().toString();

            if (url.contains("."))
            {
                if (sharedPreferences == null)
                    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

                int mode = sharedPreferences.getInt("mode",0);
                if (mode > 0)
                {
                    String urlSplit[] = url.split("\\.");
                    if (urlSplit.length == 3)
                        url = urlSplit[1] + "." + urlSplit[2];

                    if (url.contains("/"))
                    {
                        String otherSplit[] = url.split("/");
                        url = otherSplit[0];
                    }

                    String temp = sharedPreferences.getString("list","");
                    List<String> list = new ArrayList<>();
                    if (!temp.equals(""))
                    {
                        if (temp.contains(","))
                            list.addAll(Arrays.asList(temp.split(",")));
                        else
                            list.add(temp);
                    }


                    if (mode == 2) // whiteList
                    {
                        list.add("my-store.in");
                        if (!list.contains(url))
                            redirect();
                    }
                    else if (mode == 1) // blackList
                    {
                        if (list.contains(url))
                            redirect();
                    }
                }
            }
        }
    }

    private void redirect()
    {
        try
        {
            Intent intent = new Intent(getApplicationContext(), AccessDenied.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            getApplicationContext().startActivity(intent);
        }
        catch (Exception e)
        {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.my-store.in/access-denied"));
                intent.setPackage("com.android.chrome");
                intent.putExtra(Browser.EXTRA_APPLICATION_ID, "com.android.chrome");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
            catch(ActivityNotFoundException v) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.my-store.in/access-denied"));
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
            }
        }
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        AccessibilityServiceInfo info = getServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
        info.packageNames = new String[]{"com.android.chrome"};
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_VISUAL;
        info.notificationTimeout = 300;
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS |
                AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        this.setServiceInfo(info);

    }
}
