package com.vuzix.ultralite.sample;

import android.content.Context;

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
 * The lines can be scrolled upwards to create a teleprompter or a closed-caption effect.
 */
public class DemoScrollLayout {
    // Define the look of our
    final static int sliceHeight = 48; // Height of each slice of text (including inter-line padding)
    final static int fontSize = 35;    // Font size within one slice of text (smaller than the sliceHeight
    final static int lowestLineShowing = 0;
    final static int maxLinesShowing = 5;
    final static int fastScrollMilliSecs = 500;

    public static void runDemo(Context context, MainActivity.DemoActivityViewModel demoActivityViewModel, UltraliteSDK ultralite) throws MainActivity.Stop {
        AckWaiter ackWaiter = new AckWaiter(ultralite);
        ultralite.setLayout(Layout.SCROLL, 0, true);
        UltraliteSDK.ScrollingTextView scrollingTextView = ultralite.getScrollingTextView();
        scrollingTextView.scrollLayoutConfig(sliceHeight, lowestLineShowing, maxLinesShowing, fastScrollMilliSecs, false);
        String teleprompterContents = context.getString(R.string.scroll_layout_demo_text);

        // This SDK class breaks up a huge text line into multiple slices. Each slice represents a
        // single line of text. These slices be sent to the glasses which can efficiently show them
        // and animate them as we expect in a teleprompter.
        TextToImageSlicer slicer = new TextToImageSlicer(teleprompterContents, sliceHeight, fontSize);

        while (slicer.hasMoreSlices()) {
            scrollingTextView.sendScrollImage(slicer.getNextSlice(), 0, true);
            ackWaiter.waitForAck("Send line of text as image");
            // When this wait finishes, the glasses have replied that they received the text we just sent
            // This
            demoActivityViewModel.pause(2000);
        }
        demoActivityViewModel.pause(5000);
    }

}
