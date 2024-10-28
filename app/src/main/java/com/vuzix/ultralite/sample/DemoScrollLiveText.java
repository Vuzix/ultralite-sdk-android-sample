package com.vuzix.ultralite.sample;

import android.content.Context;

import com.vuzix.ultralite.Layout;
import com.vuzix.ultralite.UltraliteSDK;
import com.vuzix.ultralite.utils.scroll.LiveText;

/**
 * This class demonstrates the LiveText feature which is used when we want to send text in realtime
 * to the glasses, but do not know beforehand what the text will be.  The text can be moved and
 * replaced. This creates an effect similar to closed captioning.
 */
public class DemoScrollLiveText {
    // Each line of the fullString will be sent for display at at the specified interval. The
    // lines are appended together as time goes on. This simulates data coming back from a speech
    // recognizer in chunks. The display will take those chunks and break it into screen lines
    // and send it appropriately.
    private static void chunkStringsToEngine(MainActivity.DemoActivityViewModel demoActivityViewModel, LiveText liveTextSender, int intervalMs, String[] fullStrings) throws MainActivity.Stop {
        String fullTextToSend = "";
        for (String eachLine : fullStrings) {
            // We append lines together to simulate the results of a speech engine. It will give us a partial
            // result, then update that over and over again, growing and changing the text as it goes.
            // As long as we use one LiveText class, it will manage this properly. So, for the demo, we
            // just send a block of text (with no correlation to screen lines) and let the LiveText break
            // it into lines and show what it needs to.
            fullTextToSend += eachLine + " ";
            liveTextSender.sendText(fullTextToSend);
            // We pause as we parse the text array to simulate the speech engine giving us data over time
            demoActivityViewModel.pause(intervalMs);
        }
    }

    public static void runDemo(Context context, MainActivity.DemoActivityViewModel demoActivityViewModel, UltraliteSDK ultralite) throws MainActivity.Stop  {
        final int sliceHeightInPixels = 48;    // The lines will be 48 pixels high, so each line is 1/10th the screen height. This affects the
                                               // ranges for all other values below since this configuration now has a maximum of 10 lines.
        final int sliceWidthInPixels = UltraliteSDK.Canvas.WIDTH; // Use the full width
        final int startingScreenLocation = 1;  // The lines will appear at line 0, the lowest point on the screen.  (Since above we
                                               // configured a total of 10 lines on the screen, this can be (0-9) and we're choosing 1.
        final int numberLinesShowing = 3;      // Number of full lines when the text pauses. A fourth line shows during the transition.
                                               // (Since each line is set to be 48 pixels high above, we can have a max of 10 lines on
                                               // the screen, 1 up from the bottom, we can choose between 1 and 9, and we choose 3).
        ultralite.setLayout(Layout.SCROLL, 0, true, true, 0);
        LiveText liveTextSender = new LiveText(ultralite, sliceHeightInPixels, sliceWidthInPixels, startingScreenLocation, numberLinesShowing, null);
        // Often the LiveText is used with a speech recognition engine that gives us results. We will
        // simulate that by sending some arrays.
        String[] text = context.getResources().getStringArray(R.array.live_text_demo_text_1);
        chunkStringsToEngine(demoActivityViewModel, liveTextSender, 2000, text);
        demoActivityViewModel.pause(1000);
        text = context.getResources().getStringArray(R.array.live_text_demo_text_2);
        chunkStringsToEngine(demoActivityViewModel, liveTextSender, 2000, text);
        demoActivityViewModel.pause(1000);
    }
}
