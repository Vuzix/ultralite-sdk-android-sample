package com.vuzix.ultralite.sample;

import android.content.Context;

import com.vuzix.ultralite.EventListener;
import com.vuzix.ultralite.Layout;
import com.vuzix.ultralite.UltraliteSDK;
import com.vuzix.ultralite.utils.scroll.TextToImageSlicer;

/**
 * The SCROLL layout gives us a mechanism to send text in any font, including mixed fonts to the
 * glasses line-by-line.
 *
 * This is more efficient for non-Latin based languages, and fonts other than the default as
 * compared to using the CANVAS or text-based layouts.
 *
 * There are two utility classes {@link DemoScrollAutoScroller} and {@link DemoScrollLiveText} that
 * are built on the native SCROLL classes and handle the two most common use cases for scrolling text.
 *
 * The native Scroll layout commands shown here can be used if those commands don't meet your needs.
 */
public class DemoScrollNative {
    final static int sliceHeight = 48; // Height of each slice of text (including inter-line padding)
    final static int fontSize = 35;    // Font size within one slice of text (smaller than the sliceHeight
    final static int lowestLineShowing = 0;
    final static int maxLinesShowing = 3;
    final static int fastScrollMilliSecs = 500;

    public static void runDemo(Context context, MainActivity.DemoActivityViewModel demoActivityViewModel, UltraliteSDK ultralite) throws MainActivity.Stop {
        AckWaiter ackWaiter = new AckWaiter(ultralite);
        ultralite.setLayout(Layout.SCROLL, 0, true, true, 0);
        UltraliteSDK.ScrollingTextView scrollingTextView = ultralite.getScrollingTextView();
        scrollingTextView.scrollLayoutConfig(sliceHeight, lowestLineShowing, maxLinesShowing, fastScrollMilliSecs, false);
        String teleprompterContents = context.getString(R.string.scroll_layout_native_text);

        // This SDK class breaks up a huge text line into multiple slices. Each slice represents a
        // single line of text. These slices be sent to the glasses which can efficiently show them
        // and animate them as we expect in a teleprompter.
        TextToImageSlicer slicer = new TextToImageSlicer(teleprompterContents, sliceHeight, fontSize);
        int i = 0;
        // First let's fill the entire screen without waiting, and without scrolling
        while (slicer.hasMoreSlices() && (i<maxLinesShowing) ) {
            // We send the line to the explicit index of the screen without scrolling the screen
            final boolean scrollFirst = false;
            final int sliceIndexNumber = maxLinesShowing - 1 - i;
            scrollingTextView.sendScrollImage(slicer.getNextSlice(), sliceIndexNumber, scrollFirst);
            // we'll wait until the glasses confirm each line has arrived, although this is not
            // necessary as the underlying queue does this. But it demonstrates this mechanism which
            // could allow us to synchronize our UI with the glasses UI
            ackWaiter.waitForAck("Send line of text as image");
            // When this wait finishes, the glasses have replied that they received the text we just sent
            i++;
        }
        // Continue slicing the rest of that same content with some pauses in between
        while (slicer.hasMoreSlices()) {
            demoActivityViewModel.pause(2000);
            // Now we will just send the bottom slice, and request that the previous bottom be
            // scrolled up one position before accepting this as the new bottom slice
            final boolean scrollFirst = true;
            final int bottomSliceIndex = 0;
            scrollingTextView.sendScrollImage(slicer.getNextSlice(), bottomSliceIndex, scrollFirst);
        }
        demoActivityViewModel.pause(2000);

        // We can then reconfigure to have a 4 second animation time
        scrollingTextView.scrollLayoutConfig(sliceHeight, lowestLineShowing, maxLinesShowing, 4000, false);
        // We can also just manually scroll the screen, which now takes 4 seconds
        scrollingTextView.scrollNow();

        // And we can use an EventListener to find out when the glasses finish the scroll animation
        final EventListener eventListener = new EventListener() {
            @Override
            public void onScrolled(boolean isScreenEmpty) {
                synchronized (this) {
                    notifyAll();
                }
            }
        };
        // That listener must be registered
        ultralite.addEventListener(eventListener);
        // Now we wait here for the glasses to tell us the 4s animation has finished
        synchronized (eventListener) {
            try {
                eventListener.wait();
            } catch (InterruptedException e) {
                throw new MainActivity.Stop(true);
            }
        }
        // And we can unregister the listener now that we're done
        ultralite.removeEventListener(eventListener);

        // Then we can manually clear an arbitrary slice. In this case, our topmost one.
        scrollingTextView.clear(maxLinesShowing - 1);
        demoActivityViewModel.pause(2000);
    }
}
