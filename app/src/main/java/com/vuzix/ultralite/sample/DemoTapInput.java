package com.vuzix.ultralite.sample;

import android.content.Context;

import com.vuzix.ultralite.EventListener;
import com.vuzix.ultralite.LVGLImage;
import com.vuzix.ultralite.Layout;
import com.vuzix.ultralite.UltraliteSDK;
import com.vuzix.ultralite.utils.scroll.TextToImageSlicer;

/**
 * This class demonstrates using tap input from the glasses.
 */
public class DemoTapInput {
    final static int sliceHeight = 60; // Height of each slice of text (including inter-line padding)
    final static int fontSize = 48;    // Font size within one slice of text (smaller than the sliceHeight
    final static int lowestLineShowing = 3;
    final static int maxLinesShowing = 2;
    final static int fastScrollMilliSecs = 500;

    public static void runDemo(Context context, MainActivity.DemoActivityViewModel demoActivityViewModel, UltraliteSDK ultralite) throws MainActivity.Stop {
        final int SCREEN_TIMEOUT_SECS = 15;
        final boolean HIDE_STATUS_BAR = false;
        final int maxTaps = 2;
        boolean animateTaps = true;
        // Taps work in all layouts, and we'll use the scroll layout since it is convenient.
        // We specify the tap behavior when calling setLayout.
        ultralite.setLayout(Layout.SCROLL, SCREEN_TIMEOUT_SECS, HIDE_STATUS_BAR, animateTaps, maxTaps);
        UltraliteSDK.ScrollingTextView scrollingTextView = ultralite.getScrollingTextView();
        scrollingTextView.scrollLayoutConfig(sliceHeight, lowestLineShowing, maxLinesShowing, fastScrollMilliSecs, false);

        // Create several images of text we can choose between to show the status
        LVGLImage tapOnce = new TextToImageSlicer(context.getString(R.string.tap_once), sliceHeight, fontSize).getSliceAt(0);
        LVGLImage tapTwice = new TextToImageSlicer(context.getString(R.string.tap_twice), sliceHeight, fontSize).getSliceAt(0);
        LVGLImage tappedOnce = new TextToImageSlicer(context.getString(R.string.tapped_1), sliceHeight, fontSize).getSliceAt(0);
        LVGLImage tappedTwice = new TextToImageSlicer(context.getString(R.string.tapped_2), sliceHeight, fontSize).getSliceAt(0);

        // We need to add an event listener if we want to know when the taps occur
        TapListener tapListener = new TapListener();
        ultralite.addEventListener(tapListener);

        // Show the instructions on the glasses
        scrollingTextView.sendScrollImage(tapOnce, lowestLineShowing, false);
        int numTaps;
        do {
            numTaps = tapListener.waitForTaps();
            if(numTaps == 1) {
                // Got one tap, indicate we got it
                scrollingTextView.sendScrollImage(tappedOnce, lowestLineShowing, true);
                // Now configures to allow double-taps, and instruct the user to tap twice
                scrollingTextView.sendScrollImage(tapTwice, lowestLineShowing, true);
            }
        } while (numTaps != 2);
        // We got 2 taps, indicate this on the glasses
        scrollingTextView.sendScrollImage(tappedTwice, lowestLineShowing, true);
        scrollingTextView.scrollNow();
        demoActivityViewModel.pause(2000);
        // Unregister this so our listener stops being called
        ultralite.removeEventListener(tapListener);
    }

    // The event listener has many indications, this one is occurs when the frames are touched
    static class TapListener implements EventListener {
        private volatile int userTapCount;

        @Override
        public void onTap(int tapCount) {
            synchronized (this) {
                userTapCount = tapCount;
                this.notifyAll();
            }
        }

        // This method blocks the calling thread until the taps are received
        public synchronized int waitForTaps() throws MainActivity.Stop {
            userTapCount = 0;
            try {
                this.wait();   // Block the thread here
            } catch (InterruptedException e) {
                throw new MainActivity.Stop(true);
            }
            return userTapCount;
        }
    };
}
