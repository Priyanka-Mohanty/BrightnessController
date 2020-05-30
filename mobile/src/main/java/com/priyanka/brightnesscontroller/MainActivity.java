package com.priyanka.brightnesscontroller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.System;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    //Write setting Permission
    private static final int WRITE_SETTINGS_REQUEST_CODE = 121;
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    //a variable to store the system brightness
    private int brightness;
    //the content resolver used as a handle to the system's settings
    private ContentResolver cResolver;
    //a window object, that will store a reference to the current window
    private Window window;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = MainActivity.this;
        //checking write setting permission
        try {
            showRequestPermissionWriteSettings();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Log.i("Final Block Executed", "FINAL BLOCK EXECUTED");
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            setContentView(R.layout.activity_main);


            Button buttonRate = (Button) findViewById(R.id.btn_rate);
            Button buttonShare = (Button) findViewById(R.id.btn_share);
            Button buttonFeedBack = (Button) findViewById(R.id.btn_feedback);


            //get the seek bar from main.xml file
            SeekBar brightBar = (SeekBar) findViewById(R.id.seek_brightbar);


            //get the content resolver
            cResolver = getContentResolver();

            //get the current window
            window = getWindow();

            //seek bar settings//
            //sets the range between 0 and 255
            brightBar.setMax(255);
            //set the seek bar progress to 1
            brightBar.setKeyProgressIncrement(1);
            try {
                //get the current system brightness
                brightness = Settings.System.getInt(cResolver, Settings.System.SCREEN_BRIGHTNESS);

                //set the system brightness value, and if active, disable auto mode.
                Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
                //Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);

            } catch (Settings.SettingNotFoundException e) {
                //throw an error case it couldn't be retrieved
                Log.e("Error", "Cannot access system brightness");
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            //sets the progress of the seek bar based on the system's brightness
            brightBar.setProgress(brightness);


            //register OnSeekBarChangeListener, so it can actually change values
            brightBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    //set the system brightness using the brightness variable value
                    System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, brightness);

                    //preview brightness changes at this window
                    //get the current window attributes
                    WindowManager.LayoutParams layoutpars = window.getAttributes();
                    //set the brightness of this window
                    layoutpars.screenBrightness = brightness / (float) 255;

                    //apply attribute changes to this window
                    window.setAttributes(layoutpars);
                    window.setDimAmount((float) 0.0);
                    window.setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                            WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    //sets the minimal brightness level
                    //if seek bar is 20 or any value below
                    if (progress <= 20) {
                        //set the brightness to 20
                        brightness = 0;
                    } else //brightness is greater than 20
                    {
                        //sets brightness variable based on the progress bar
                        brightness = progress;
                    }
                }
            });


            //-------------------------------------------------functionality for buttons
            buttonRate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
                    Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                    // To count with Play market backstack, After pressing back button,
                    // to taken back to our application, we need to add following flags to intent.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    }
                    try {
                        startActivity(goToMarket);
                    } catch (ActivityNotFoundException e) {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        Log.i("Final Block Executed", "FINAL BLOCK EXECUTED");
                    }
                }
            });
            buttonFeedBack.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("LongLogTag")
                @Override
                public void onClick(View v) {
                    String email = "appfeedbackpriyanka@gmail.com";
                    String Subject = "User Feedback : Brightness Controller Application";
                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
                    emailIntent.setData(Uri.parse("mailto:"));
                    emailIntent.setType("text/plain");
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, Subject);
                    emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                    try {
                        startActivity(Intent.createChooser(emailIntent, "Send mail"));
                        finish();
                        Log.i("Finished sending email...", "");
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(MainActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
                    } finally {
                        Log.i("Final Block Executed", "FINAL BLOCK EXECUTED");
                    }
                }
            });

            buttonShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String shareBody = "https://play.google.com/store/apps/details?id=" + context.getPackageName();
                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, context.getResources().getString(R.string.app_name) +
                            "\n\n(Open it in Google Play Store to Download the Application)" + context.getResources().getString(R.string.app_name));
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                            context.getResources().getString(R.string.app_name) + ":\n\n(Open it in Google Play Store to Download the Application)\n"
                                    + shareBody);
                    try {
                        startActivity(Intent.createChooser(sharingIntent, "Share via"));
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "There is no app installed for sharing this application.", Toast.LENGTH_SHORT).show();
                    } finally {
                        Log.i("Final Block Executed", "FINAL BLOCK EXECUTED");
                    }

                }
            });

        }//---------------------try close
        catch (Exception e) {
            e.printStackTrace();
        } finally {
            Log.i("Final Block Executed", "FINAL BLOCK EXECUTED");
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    //system settings permission
    @TargetApi(Build.VERSION_CODES.M)
    private void showRequestPermissionWriteSettings() {
        try {
            // for Settings.ACTION_MANAGE_WRITE_SETTINGS: Settings.System.canWrite
            // CommonsWare's blog post:https://commonsware.com/blog/2015/08/17/random-musings-android-6p0-sdk.html
            boolean hasSelfPermission;
            //boolean hasSelfPermission = Settings.System.canWrite(this);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                hasSelfPermission = Settings.System.canWrite(this);
            } else {
                hasSelfPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_GRANTED;
            }

            if (hasSelfPermission) {
         /*  new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle("Permission Brightness Write Settings")
                    .setMessage("\r\n" + "granted")
                    .setPositiveButton(android.R.string.ok, null)
                    .show();*/
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, WRITE_SETTINGS_REQUEST_CODE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Log.i("Final Block Executed", "FINAL BLOCK EXECUTED");
        }

    }


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == WRITE_SETTINGS_REQUEST_CODE) {

                final boolean hasSelfPermission = Settings.System.canWrite(this);
                new AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setTitle(context.getResources().getString(R.string.text_brightness_access))
                  /*  .setMessage((hasSelfPermission ? "granted" : "not granted") + "\r\n\r\n" +
                        context.getResources().getString(R.string.text_request))*/
                        .setMessage((hasSelfPermission ? "Granted" : "Not Granted") + "\r\n\r\n" +
                                context.getResources().getString(R.string.text_request))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (!hasSelfPermission) {
                                    //onBackPressed();
                                    showRequestPermissionWriteSettings();
                                }
                            }
                        })
                        .show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Log.i("Final Block Executed", "FINAL BLOCK EXECUTED");
        }

    }
}

