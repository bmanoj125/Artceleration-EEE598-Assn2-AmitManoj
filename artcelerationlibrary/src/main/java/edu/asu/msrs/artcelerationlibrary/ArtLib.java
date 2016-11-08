package edu.asu.msrs.artcelerationlibrary;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.os.MemoryFile;
import android.os.Message;
import android.os.Messenger;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

import java.io.IOException;

/**
 * Created by rlikamwa on 10/2/2016.
 */

public class ArtLib {
    private TransformHandler artlistener;
    private Activity myActivity;
    public ArtLib(Activity activity){
        myActivity = activity;
        initialize();
    }
    private Messenger myMessenger;
    private boolean Bound;
    // Handler to start a connection and close a connection between library and service.
    ServiceConnection mServiceConnection = new ServiceConnection(){
    @Override
    public void onServiceConnected(ComponentName name, IBinder service){
        myMessenger = new Messenger(service);
        Bound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name){
        myMessenger = null;
        Bound = false;
    }
    };

    // Initialization Method. Binds the activity to the transform service.
    public void initialize(){
        myActivity.bindService(new Intent(myActivity, TransformService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
    }
    // This array contains the list of transforms that the library supports. Also used to populate the drop down on the UI.
    public String[] getTransformsArray(){
        String[] transforms = {"Gaussian Blur", "Neon edges", "Color Filter"};
        return transforms;
    }

    //
    public TransformTest[] getTestsArray(){
        TransformTest[] transforms = new TransformTest[3];
        transforms[0]=new TransformTest(0, new int[]{1,2,3}, new float[]{0.1f, 0.2f, 0.3f});
        transforms[1]=new TransformTest(1, new int[]{11,22,33}, new float[]{0.3f, 0.2f, 0.3f});
        transforms[2]=new TransformTest(2, new int[]{51,42,33}, new float[]{0.5f, 0.6f, 0.3f});

        return transforms;
    }

    public void registerHandler(TransformHandler artlistener){
        this.artlistener=artlistener;
    }

    // Request transform sends a request to the service to perform an image transform. It sends the relevant data to the Service.
    // It is a non blocking, so it returns true if the request was successfully made. A new request can be placed before the old one has completed.
    public boolean requestTransform(Bitmap img, int index, int[] intArgs, float[] floatArgs) {
        try {
            MemoryFile memFile = new MemoryFile("memKey", 30);
            ParcelFileDescriptor pfd = MemoryFileUtil.getParcelFileDescriptor(memFile);

            int what = TransformService.Transform_ONE;
//            int what = TransformService.Transform_TWO;

            // Sequence to send some dummy data to service. Will be replaced with sending actual data.
            Bundle dataBundle = new Bundle();
            dataBundle.putParcelable("pfd", pfd);
            Message msg = Message.obtain(null, what, 2, 3);
            msg.setData(dataBundle);
            try {
                myMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
