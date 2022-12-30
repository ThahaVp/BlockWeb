package com.blockweb.android.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.blockweb.android.Helper.RangeTimePickerDialog;
import com.blockweb.android.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SchedulePage extends AppCompatActivity {

    AlertDialog editDialog, deleteDialog;
    Button saveBtn;
    private boolean doubleBackToExitPressedOnce = false;
    TextView fromTime, toTime, webList;
    String displayString = "", listString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_page);

        Switch switchBtn = findViewById(R.id.s);
        TextView detailsText = findViewById(R.id.details);
        TextView whiteTextView = findViewById(R.id.white_text);
        TextView blackTextView = findViewById(R.id.black_text);
        TextView edit = findViewById(R.id.edit);
        TextView delete = findViewById(R.id.delete);
        EditText editText = findViewById(R.id.editText);
        TextView editList = findViewById(R.id.edit_list);
        TextView addBtn = findViewById(R.id.add);
        TextView fromField = findViewById(R.id.from);
        webList = findViewById(R.id.web_list);
        TextView toField = findViewById(R.id.to);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String fromV = sharedPreferences.getString("from","").toUpperCase();
        String toV = sharedPreferences.getString("to","").toUpperCase();
       int modeV = sharedPreferences.getInt("mode",0);

       if (fromV.equals("") || toV.equals("") || modeV == 0)
           toHome();

       fromField.setText(fromV);
       toField.setText(toV);

        displayString = sharedPreferences.getString("display_list","");
        listString = sharedPreferences.getString("list","");
        webList.setText(displayString);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newWeb = editText.getText().toString().trim().toLowerCase();
                editText.setText("");
                listString += ","+newWeb;
                displayString += ", "+newWeb;
                webList.setText(displayString);
                sharedPreferences.edit().putString("list", listString).putString("display_list", displayString).commit();
            }
        });

        editList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SchedulePage.this,"Couldn't complete",Toast.LENGTH_SHORT).show();
            }
        });


        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editDialog == null)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SchedulePage.this);
                    View dialogView = getLayoutInflater().inflate(R.layout.add_timing_dialog,null,true);

                    TextView title = dialogView.findViewById(R.id.title);
                    title.setText("Edit Timing");
                    saveBtn = dialogView.findViewById(R.id.save);
                    ImageButton closeBtn = dialogView.findViewById(R.id.close);
                    fromTime = dialogView.findViewById(R.id.from_time);
                    toTime = dialogView.findViewById(R.id.to_time);

                    fromTime.setText(fromV);
                    toTime.setText(toV);

                    editDialog = builder.create();
                    editDialog.setView(dialogView);
                    editDialog.setCanceledOnTouchOutside(false);
                    editDialog.show();

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
                            editDialog.dismiss();
                        }
                    });

                    saveBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String from = fromTime.getText().toString();
                            String to = toTime.getText().toString();
                            if (!from.equals("") && !to.equals(""))
                            {
                                sharedPreferences.edit().putString("from",from.toLowerCase()).putString("to",to.toLowerCase()).commit();
                                fromField.setText(from);
                                toField.setText(to);
                            }
                            editDialog.dismiss();
                        }
                    });
                }
                else
                {
                    editDialog.show();
                    fromTime.setText(fromField.getText().toString());
                    toTime.setText(toField.getText().toString());
                }

            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (deleteDialog == null)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SchedulePage.this);
                    View dialogView = getLayoutInflater().inflate(R.layout.delete_dialog,null,true);

                    Button deleteBtn = dialogView.findViewById(R.id.delete);
                    ImageButton closeBtn = dialogView.findViewById(R.id.close);

                    deleteDialog = builder.create();
                    deleteDialog.setView(dialogView);
                    deleteDialog.setCanceledOnTouchOutside(false);
                    deleteDialog.show();

                    closeBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            editDialog.dismiss();
                        }
                    });

                    deleteBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            sharedPreferences.edit().remove("mode").remove("from").remove("to").
                                    remove("display_list").remove("list").commit();
                            deleteDialog.dismiss();
                            toHome();
                        }
                    });
                }
                else
                {
                    editDialog.show();
                    // set time to schedule's time
                }

            }
        });

        String blackListText = "In blacklist mode,<b> the websites in the list are blocked </b> but all other websites are allowed.";
        String whiteListText = "In whitelist mode,<b> the websites in the list are allowed </b> but all other websites are blocked.";

        if (modeV == 1)
        {
            switchBtn.setChecked(true);
            detailsText.setText(Html.fromHtml(blackListText));
        }
        else if (modeV == 2)
        {
            switchBtn.setChecked(false);
            detailsText.setText(Html.fromHtml(whiteListText));
        }


        switchBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                {
                    detailsText.setText(Html.fromHtml(blackListText));
                    blackTextView.setTextColor(ContextCompat.getColor(SchedulePage.this, R.color.black));
                    whiteTextView.setTextColor(ContextCompat.getColor(SchedulePage.this, R.color.grey));
                    sharedPreferences.edit().putInt("mode",1).commit();
                }
                else
                {
                    detailsText.setText(Html.fromHtml(whiteListText));
                    blackTextView.setTextColor(ContextCompat.getColor(SchedulePage.this, R.color.grey));
                    whiteTextView.setTextColor(ContextCompat.getColor(SchedulePage.this, R.color.black));
                    sharedPreferences.edit().putInt("mode",2).commit();
                }
            }
        });

    }

    private void toHome()
    {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
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

    @Override
    protected void onResume() {
        super.onResume();
        if (isAccessibilityEnabled() == 0)
            showPermissionDialog();
    }

    private void showPermissionDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(SchedulePage.this);
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

    public int isAccessibilityEnabled() {
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(this.getContentResolver(),android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
        }

        return accessibilityEnabled;
    }
}