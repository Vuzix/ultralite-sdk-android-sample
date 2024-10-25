package com.vuzix.ultralite.sample;

import android.app.Application;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.vuzix.ultralite.LVGLImage;
import com.vuzix.ultralite.UltraliteSDK;

/**
 * This class sets up a basic connection to the Z100 glasses using the ultralite SDK
 *
 *
 */
public class MainActivity extends AppCompatActivity {

    protected static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        ImageView installedImageView = findViewById(R.id.installed);
        ImageView linkedImageView = findViewById(R.id.linked);
        TextView nameTextView = findViewById(R.id.name);
        ImageView connectedImageView = findViewById(R.id.connected);
        ImageView controlledImageView = findViewById(R.id.controlled);
        Button demoButton = findViewById(R.id.run_demo);
        Button notificationButton = findViewById(R.id.send_notification);

        // Get the instance of the SDK
        UltraliteSDK ultralite = UltraliteSDK.get(this);

        // Now we can use that instance to observe its state, and tie that to our demo app UI
        ultralite.getAvailable().observe(this, available -> {
            installedImageView.setImageResource(available ? R.drawable.ic_check_24 : R.drawable.ic_close_24);
        });

        ultralite.getLinked().observe(this, linked -> {
            linkedImageView.setImageResource(linked ? R.drawable.ic_check_24 : R.drawable.ic_close_24);
            nameTextView.setText(ultralite.getName());
        });

        ultralite.getConnected().observe(this, connected -> {
            connectedImageView.setImageResource(connected ? R.drawable.ic_check_24 : R.drawable.ic_close_24);
            demoButton.setEnabled(connected);
            notificationButton.setEnabled(connected);
        });

        ultralite.getControlledByMe().observe(this, linked -> {
            controlledImageView.setImageResource(linked ? R.drawable.ic_check_24 : R.drawable.ic_close_24);
            nameTextView.setText(ultralite.getName());
        });

        // For this example we use a ViewModel perform the logic of the test and report its state
        DemoActivityViewModel model = new ViewModelProvider(this).get(DemoActivityViewModel.class);

        // Update our "run" button based on the state of our demo
        model.running.observe(this, running -> {
            if (running) {
                demoButton.setEnabled(false);
            } else {
                demoButton.setEnabled(ultralite.isConnected());
            }
        });

        // Now set the click listeners to kick-off the two demos
        demoButton.setOnClickListener(v -> model.runDemo());
        notificationButton.setOnClickListener(v -> sendSampleNotification() );
    }

    /**
     * Sending a notification is by far the simplest mechanism to put content on the glasses.
     *
     * By default, the Android app may be listening to notifications from all system apps and
     * sending it to the glasses. But the user can control this behavior.
     *
     * We can programatically send the same notification to the glasses that does NOT need to notify
     * the rest of the phone. That's a great way to get content on the screen.
     *
     * If nothing has control, the notification shows full-screen.  But if something else has control
     * this notification may "peek" a shorter version from the top of the screen.
     *
     * When you run this demo, try hitting the "send notification" button while the app is idle, and
     * while a demo is running to see the difference.
     */
    private void sendSampleNotification() {
        UltraliteSDK ultralite = UltraliteSDK.get(this);
        ultralite.sendNotification("Ultralite SDK Sample", "Hello from a sample app!",
                loadLVGLImage(this, R.drawable.rocket, false));
    }


    /**
     * This ViewModel will hold our state during the demo.
     */
    public static class DemoActivityViewModel extends AndroidViewModel {

        private final UltraliteSDK ultralite;

        private final MutableLiveData<Boolean> running = new MutableLiveData<>();
        private boolean haveControlOfGlasses;

        public DemoActivityViewModel(@NonNull Application application) {
            super(application);
            ultralite = UltraliteSDK.get(application);
            ultralite.getControlledByMe().observeForever(controlledObserver);
        }

        @Override
        protected void onCleared() {
            ultralite.releaseControl();
            // We can delay removing the observer to allow us to be notified of losing control
            // Or we could have just set our state from here.
            new Handler(Looper.getMainLooper()).postDelayed(() ->
                    ultralite.getControlledByMe().removeObserver(controlledObserver), 500);
        }

        // We will demonstrate the glasses functionality from a single worker thread. Typically
        // an application will have other logic that drives the UI, and the Z100 output will be
        // driven by that.
        private void runDemo() {
            new Thread(() -> {
                running.postValue(true);
                // Always request control before any drawing starts
                haveControlOfGlasses = ultralite.requestControl();
                if(haveControlOfGlasses) {
                    try {
                        DemoCanvasLayout.runDemo(getApplication(), this, ultralite);
                        DemoScrollLayout.runDemo(getApplication(), this, ultralite);
                        // Always release control when finished drawing to the glasses
                        ultralite.releaseControl();
                        ultralite.sendNotification("Demo Success", "The demo is over");
                    } catch (Stop stop) {
                        ultralite.releaseControl(); // Release when aborting, too.
                        if (stop.error) {
                            ultralite.sendNotification("Demo Error", "An error occurred during the demo");
                        } else {
                            ultralite.sendNotification("Demo Control Lost", "The demo lost control of the glasses");
                        }
                    }
                    running.postValue(false);
                }
            }).start();
        }

        // This is a convenience class to pause our thread and generate a Stop exception if the
        // user wants to abort
        public void pause() throws Stop {
            pause(2000);
        }

        // This is a convenience class to pause our thread and generate a Stop exception if the
        // user wants to abort
        public void pause(long ms) throws Stop {
            SystemClock.sleep(ms);
            if (!haveControlOfGlasses) {
                // Throw Stop when we lose control
                throw new Stop(false);
            }
        }

        private final Observer<Boolean> controlledObserver = controlled -> {
            // If we lose control of the glasses we stop the demo. Other apps may continue
            // running without the glasses UI and wait for it to reconnect to begin streaming to
            // it again.
            if (!controlled) {
                haveControlOfGlasses = false;
            }
        };
    }

    /**
     * This exception is our mechanism to detect when the user wants to abort the demo
     */
    public static class Stop extends Exception {
        private final boolean error;
        public Stop(boolean error) {
            this.error = error;
        }
    }

    /**
     * This is a convenience method to get LVGL images from resources
     * @param context Application context
     * @param resource Resource ID of a bitmap
     * @param singleBit True to render as single-bit (black and white) only. This is the smallest
     *                  and fastest way to send images.
     * @return LVGLImage in 1-bit color space at the original bitmap dimensions
     */
    static LVGLImage loadLVGLImage(Context context, int resource, boolean singleBit) {
        BitmapDrawable drawable = (BitmapDrawable) ResourcesCompat.getDrawable(
                context.getResources(), resource, context.getTheme());
        int colorSpace = singleBit ? LVGLImage.CF_INDEXED_1_BIT : LVGLImage.CF_INDEXED_2_BIT ;
        return LVGLImage.fromBitmap(drawable.getBitmap(), colorSpace);
    }
}
