package com.vuzix.ultralite.sample;

import android.content.Context;
import android.graphics.Point;
import android.util.Log;


import com.vuzix.ultralite.Anchor;
import com.vuzix.ultralite.LVGLImage;
import com.vuzix.ultralite.Layout;
import com.vuzix.ultralite.TextAlignment;
import com.vuzix.ultralite.TextWrapMode;
import com.vuzix.ultralite.UltraliteColor;
import com.vuzix.ultralite.UltraliteSDK;

/**
 * This demonstrates the use of a CANVAS layout
 *
 * The Canvas layout allows up to 7 text fields and 3 small images to be created in the foreground,
 * and allows the full background to be painted with text and images.
 *
 * Objects in the foreground cover the background and can be shown/hidden and moved.  The background
 * can only be redrawn to new content and cleared.
 */
public class DemoCanvasLayout {

    public static void runDemo(Context context, MainActivity.DemoActivityViewModel demoActivityViewModel, UltraliteSDK ultralite) throws MainActivity.Stop {
        demoTextFields(context, demoActivityViewModel, ultralite);
        demoImages(context, demoActivityViewModel, ultralite);
        demoBackgroundDrawing(context, demoActivityViewModel, ultralite);
    }

    private static void demoTextFields(Context context, MainActivity.DemoActivityViewModel demoActivityViewModel, UltraliteSDK ultralite) throws MainActivity.Stop {
        // Note, the caller already has requested control, and is observing the state of the glasses
        ultralite.setLayout(Layout.CANVAS, 0, true);

        int textId = ultralite.getCanvas().createText("This is a canvas with a text field.", TextAlignment.AUTO, UltraliteColor.WHITE, Anchor.CENTER, 0, 0, 640, -1, TextWrapMode.WRAP, true);
        if (textId == -1) {
            throw new MainActivity.Stop(true);
        }
        // In the canvas layout, we always call commit to ensure the state of the glasses matches
        // all the previous commands we have sent. We can change multiple elements before calling
        // commit() a single time. Certain changes may take effect without the commit(), but calling
        // this guarantees the state of the glasses will match.

        // Please note this simple example is not requesting an acknowledgement from the glasses
        // which would be critical for synchronizing a phone display to the glasses display.
        ultralite.getCanvas().commit();
        demoActivityViewModel.pause(5000);

        ultralite.getCanvas().updateText(textId, "The text can be changed.");
        ultralite.getCanvas().commit();
        demoActivityViewModel.pause();

        ultralite.getCanvas().updateText(textId, "The text can be moved.");
        ultralite.getCanvas().moveText(textId, Anchor.TOP_CENTER, 0, 0);
        ultralite.getCanvas().commit();
        demoActivityViewModel.pause();

        ultralite.getCanvas().updateText(textId, "The text can be made invisible...");
        ultralite.getCanvas().commit();
        demoActivityViewModel.pause();

        ultralite.getCanvas().setTextVisible(textId, false);
        ultralite.getCanvas().commit();
        demoActivityViewModel.pause();

        ultralite.getCanvas().updateText(textId, "and visible again...");
        ultralite.getCanvas().setTextVisible(textId, true);
        ultralite.getCanvas().commit();
        demoActivityViewModel.pause();

        ultralite.getCanvas().updateText(textId, "If requested, the text can wrap if it grows too large to show on a single line.");
        ultralite.getCanvas().commit();
        demoActivityViewModel.pause(5000);

        // When we're done, this frees the object and removes it from the screen
        ultralite.getCanvas().removeText(textId);
        ultralite.getCanvas().commit();
    }

    private static void demoImages(Context context, MainActivity.DemoActivityViewModel demoActivityViewModel, UltraliteSDK ultralite) throws MainActivity.Stop {
        int textId = ultralite.getCanvas().createText("You can create image objects.", TextAlignment.AUTO, UltraliteColor.WHITE, Anchor.TOP_CENTER, 0, 0, 640, 100, TextWrapMode.WRAP, true);
        if (textId == -1) {
            throw new MainActivity.Stop(true);
        }
        final boolean useSingleBit = true; // We can use single-bit images to reduce transfer time
        LVGLImage rocket = MainActivity.loadLVGLImage(context, R.drawable.rocket, useSingleBit);
        int imageId = ultralite.getCanvas().createImage(rocket, Anchor.CENTER);
        if (imageId == -1) {
            throw new MainActivity.Stop(true);
        }
        ultralite.getCanvas().commit();
        demoActivityViewModel.pause(5000);

        ultralite.getCanvas().updateText(textId, "You can change the image.");
        ultralite.getCanvas().updateImage(imageId, MainActivity.loadLVGLImage(context, R.drawable.poop, useSingleBit));
        ultralite.getCanvas().commit();
        demoActivityViewModel.pause();

        ultralite.getCanvas().updateText(textId, "You can move the image.");
        ultralite.getCanvas().moveImage(imageId, 100, 100);
        ultralite.getCanvas().commit();
        demoActivityViewModel.pause();

        ultralite.getCanvas().updateText(textId, "You can hide an image.");
        ultralite.getCanvas().setImageVisible(imageId, false);
        ultralite.getCanvas().commit();
        demoActivityViewModel.pause();

        ultralite.getCanvas().updateText(textId, "You can show an image.");
        ultralite.getCanvas().setImageVisible(imageId, true);
        ultralite.getCanvas().commit();
        demoActivityViewModel.pause();

        ultralite.getCanvas().removeImage(imageId);
        ultralite.getCanvas().updateText(textId, "Animations are possible too.");

        LVGLImage happy = MainActivity.loadLVGLImage(context, R.drawable.happy, useSingleBit);
        LVGLImage wink = MainActivity.loadLVGLImage(context, R.drawable.wink, useSingleBit);
        int animationId = ultralite.getCanvas().createAnimation(new LVGLImage[]{happy, wink}, Anchor.CENTER, 1000);
        if (animationId == -1) {
            throw new MainActivity.Stop(true);
        }
        ultralite.getCanvas().commit();
        demoActivityViewModel.pause(5000);

        ultralite.getCanvas().updateText(textId, "You can move animations.");
        ultralite.getCanvas().moveAnimation(animationId, 400, 300);
        ultralite.getCanvas().commit();
        demoActivityViewModel.pause();

        ultralite.getCanvas().updateText(textId, "You can hide animations.");
        ultralite.getCanvas().setAnimationVisible(animationId, false);
        ultralite.getCanvas().commit();
        demoActivityViewModel.pause();

        ultralite.getCanvas().updateText(textId, "You can show animations.");
        ultralite.getCanvas().setAnimationVisible(animationId, true);
        ultralite.getCanvas().commit();
        demoActivityViewModel.pause();

        ultralite.getCanvas().removeAnimation(animationId);
        ultralite.getCanvas().removeText(textId);
        ultralite.getCanvas().commit();
    }

    private static void demoBackgroundDrawing(Context context, MainActivity.DemoActivityViewModel demoActivityViewModel, UltraliteSDK ultralite) throws MainActivity.Stop {
        int textId = ultralite.getCanvas().createText("You can create image objects.", TextAlignment.AUTO, UltraliteColor.WHITE, Anchor.TOP_CENTER, 0, 0, 640, 100, TextWrapMode.WRAP, true);
        final boolean useSingleBit = false;  // The background does not allow single-bit images
        if (textId == -1) {
            throw new MainActivity.Stop(true);
        }
        ultralite.getCanvas().updateText(textId, "You can repeat an image across the background layer with a single command.");
        Point[] coordinates = {
                new Point(0, 100),
                new Point(100, 150),
                new Point(200, 200),
                new Point(300, 250),
                new Point(400, 300),
                new Point(500, 350)
        };
        ultralite.getCanvas().drawBackground(MainActivity.loadLVGLImage(context, R.drawable.rocket, useSingleBit), coordinates);
        ultralite.getCanvas().commit();
        demoActivityViewModel.pause(5000);

        ultralite.getCanvas().updateText(textId, "You can clear areas of the background layer.");
        ultralite.getCanvas().clearBackgroundRect(100, 100, 440, 280, UltraliteColor.WHITE);
        ultralite.getCanvas().commit();
        demoActivityViewModel.pause();

        ultralite.getCanvas().clearBackgroundRect(100, 100, 440, 280);
        ultralite.getCanvas().commit();
        demoActivityViewModel.pause();

        ultralite.getCanvas().clearBackground();
        ultralite.getCanvas().updateText(textId, "Finally, we're going to send a full screen image to the glasses.");
        ultralite.getCanvas().commit();
        demoActivityViewModel.pause(4000);

        ultralite.getCanvas().removeText(textId);

        LVGLImage bigImage = MainActivity.loadLVGLImage(context, R.drawable.ultralite_large_ori, useSingleBit);
        ultralite.getCanvas().drawBackground(bigImage, 0, 0);

        // This form of commit executes a callback when the glasses have received the commit. Since
        // we just sent a large image, this allows us to synchronize the phone UI to the glasses.
        ultralite.getCanvas().commit(() -> Log.d("MainActivity", "full screen image commit is done!"));
        demoActivityViewModel.pause(5000);
    }
}
