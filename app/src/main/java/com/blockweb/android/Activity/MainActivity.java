package com.blockweb.android.Activity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.blockweb.android.Helper.BlockWebHelper;
import com.blockweb.android.Helper.RangeTimePickerDialog;
import com.blockweb.android.R;

import org.jetbrains.annotations.Contract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    AlertDialog alertDialog;
    ImageButton closeBtn;
    TextView fromTime, toTime;
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton addBtn = findViewById(R.id.add);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isAccessibilityEnabled() == 1)
                {
                    if (alertDialog == null)
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        View dialogView = getLayoutInflater().inflate(R.layout.add_timing_dialog,null,true);

                        TextView title = dialogView.findViewById(R.id.title);
                        title.setText("Add Timing");
                        Button saveBtn = dialogView.findViewById(R.id.save);
                        closeBtn = dialogView.findViewById(R.id.close);
                        fromTime = dialogView.findViewById(R.id.from_time);
                        toTime = dialogView.findViewById(R.id.to_time);

                        fromTime.setText("11:00 AM");
                        toTime.setText("08:00 PM");

                        alertDialog = builder.create();
                        alertDialog.setView(dialogView);
                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.show();

                        fromTime.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                setTime("from");
                            }
                        });

                        toTime.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                setTime("to");
                            }
                        });

                        closeBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                alertDialog.dismiss();
                            }
                        });

                        saveBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String from = fromTime.getText().toString().toLowerCase();
                                String to = toTime.getText().toString().toLowerCase();

                                if (!from.equals("") && !to.equals(""))
                                {
                                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                                    sharedPreferences.edit().putString("from",from).putString("to",to).putInt("mode",1).
                                            putString("list","facebook.com,instagram.com,twitter.com,reddit.com,9gag.com").
                                            putString("display_list","facebook.com, instagram.com, twitter.com, reddit.com, 9gag.com").commit();
                                    alertDialog.dismiss();
                                    Intent intent = new Intent(MainActivity.this, SchedulePage.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });
                    }
                    else
                        alertDialog.show();
                }
                else
                {
                    showPermissionDialog();
                }
            }
        });
    }

    private void showPermissionDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.permi_dialog,null,true);

        Button saveBtn = dialogView.findViewById(R.id.save);
        ImageButton closeB = dialogView.findViewById(R.id.close);

        AlertDialog permDialog = builder.create();
        permDialog.setView(dialogView);
        permDialog.setCanceledOnTouchOutside(true);
        permDialog.show();

        closeB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                permDialog.dismiss();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                permDialog.dismiss();
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
            }
        });
    }

    private void setTime(String to)
    {
        SimpleDateFormat fo = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat format = new SimpleDateFormat("hh:mm a");

        final Calendar calendar = Calendar.getInstance();
        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int minute = calendar.get(Calendar.MINUTE);

        RangeTimePickerDialog timePickerDialog = new RangeTimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String hour = "", xx = "", minutes = "", parsed = "";

                if (hourOfDay>12)
                {
                    hourOfDay = hourOfDay - 12;
                    xx = "PM";
                }
                else if (hourOfDay < 12)
                {
                    if (hourOfDay == 0)
                        hourOfDay = 12;
                    xx = "AM";
                }

                else if (hourOfDay == 12)
                {
                    xx = "PM";
                }

                if (hourOfDay == 0)
                    hour = "12";
                else if (hourOfDay<10)
                    hour = "0"+hourOfDay;
                else
                    hour = String.valueOf(hourOfDay);

                if (minute<10)
                    minutes = "0"+minute;
                else
                    minutes = String.valueOf(minute);

                try
                {
                    Date otDate = format.parse(hour+":"+minutes+" "+xx);
                    parsed = fo.format(otDate);
                }
                catch (ParseException e)
                {
                    e.printStackTrace();
                }

                if (to.equals("to"))
                {
                    toTime.setText(hour+":"+minutes+" "+xx);
                }
                else if (to.equals("from"))
                {
                    fromTime.setText(hour+":"+minutes+" "+xx);
                }
            }
        }, hour, minute, false);
        timePickerDialog.show();
    }

    public void onBackPressed() {
        try {
            if (doubleBackToExitPressedOnce) {
                finish();
            }
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, getResources().getString(R.string.backPress_msg), Toast.LENGTH_SHORT).show();

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int isAccessibilityEnabled() {
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(this.getContentResolver(),android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
        }

        return accessibilityEnabled;
    }
}