package com.vuzix.ultralite.sample;

import android.util.Log;

import com.vuzix.ultralite.UltraliteSDK;

/**
 * This class demonstrates a mechanism that can be used to synchronize data being received by the
 * glasses.
 *
 * We can send a requestAcknowledgement command to the glasses after any operation, and the glasses
 * will reply when this message is processed.  This is very useful after a large block of data is
 * sent. We can insert this ack request and when we get the ack reply we know the data has made it
 * through the Bluetooth queue and we can update the phone UI to match the glasses UI.
 *
 * If we delay the phone UI, we can make the glasses UI and the phone UI update at almost the exact
 * same moment for a seamless experience.
 */
class AckWaiter {
    private boolean replied;
    private final UltraliteSDK ultralite;
    private String message;

    public AckWaiter(UltraliteSDK ultralite) {
        this.ultralite = ultralite;
    }

    /**
     * Simply call this method and the current thread will become idle until the glasses reply that this
     * message has been received. This is useful in this demo scenario since we have a worker thread
     * that is sequentially sending each screen.
     *
     * @param message A unique String to identify this wait condition
     */
    public void waitForAck(String message) {
        replied = false;
        this.message = message;
        // Request the ack and provide a callback method
        ultralite.requestAcknowledgement( () -> {
            synchronized(this) {
                // Simply set the bool and notify
                replied = true;
                this.notify();
            }
        });
        // Then wait
        synchronized(this) {
            while (!replied) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    Log.i(MainActivity.TAG, "Wait for \"" + message + "\" interrupted ", e);
                    break;
                }
            }
        }
    }
}
