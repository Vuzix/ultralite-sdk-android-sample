package com.vuzix.ultralite.sample;

import android.content.Context;

import com.vuzix.ultralite.Layout;
import com.vuzix.ultralite.UltraliteSDK;
import com.vuzix.ultralite.utils.scroll.AutoScroller;

/**
 * This class demonstrates the AutoScroller feature which is used when we know up-front what the full
 * text will be, and want it to scroll at a continuous rate. This creates an effect similar to a
 * teleprompter or movie credits.
 */
public class DemoScrollAutoScroller {
    static boolean scrollFinished;

    public static void runDemo(Context context, MainActivity.DemoActivityViewModel demoActivityViewModel, UltraliteSDK ultralite) throws MainActivity.Stop {
        final int sliceHeightInPixels = 48;    // The lines will be 48 pixels high, so each line is 1/10th the screen height. This affects the
                                               // ranges for all other values below since this configuration now has a maximum of 10 lines.
        final int numberLinesShowing = 4;      // Number of full lines when the text pauses. A fifth line shows during the transition.
                                               // (Since each line is set to be 48 pixels high above, we can have a max of 10 lines, and we
                                               // choose 4)
        final int startingScreenLocation = 0;  // The 4 lines (set above) will appear at line 0, the lowest point on the screen.  (Since above we
                                               // configured a total of 10 lines on the screen, and 4 lines to be shown, we can move those 4 lines
                                               // anywhere from starting position 0-6, and we're choosing 0),
        final int scrollSpeedInMs = 1500;      // A new line appears every 1.5 sec

        // Using AutoScroller requires we have control (obtained by the MainActivity in this demo)
        // and that we set the SCROLL layout.
        ultralite.setLayout(Layout.SCROLL, 0, true, true, 0);
        // Load the full text we want to show
        String teleprompterContents = context.getString(R.string.scroll_layout_demo_text);
        // Provide the text to show and text layout parameters to the AutoScroller
        AutoScroller autoScroller = new AutoScroller(ultralite, teleprompterContents, sliceHeightInPixels,
                                                     startingScreenLocation, numberLinesShowing,
                                                     UltraliteSDK.Canvas.WIDTH, null);
        // The duration should be set first, and can be changed at any point, even while running.
        autoScroller.setDuration(scrollSpeedInMs);
        // Set a callback so when the AutoScroller finishes we unlock this thread
        scrollFinished = false;
        Object doneMutex = new Object();
        autoScroller.setCallback( (complete) -> {
            synchronized (doneMutex) {
                scrollFinished = true;
                doneMutex.notifyAll();
            }
        });
        autoScroller.start();
        // Now that it's running we can call pause() and resume() and dynamically change the speed
        // with additional calls to setDuration. We could also cancel it fully by calling stop().
        // But this demo will just let the text play to the end.

        // We pause the execution of this thread until the AutoScroller notifies us it is done.
        while(!scrollFinished) {
            synchronized(doneMutex) {
                try {
                    doneMutex.wait();
                } catch (Exception e) {
                    throw new MainActivity.Stop(true);
                }
            }
        }
    }
}
