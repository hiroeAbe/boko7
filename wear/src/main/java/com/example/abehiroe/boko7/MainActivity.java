package com.example.abehiroe.boko7;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends Activity implements SensorEventListener{
    private final String TAG = MainActivity.class.getName();
    private final float GAIN = 0.9f;
    private final String[] SEND_MESSAGES = {"/Action/NONE", "/Action/PUNCH", "/Action/UPPER", "/Action/HOOK","/Action/don1","/Action/don2","/Action/don3","/Action/ど","/Action/れ","/Action/み","/Action/ふぁ","/Action/そ"};
    //private final String[] SEND_MESSAGES2 = {"/Action/NONE","/Action/ど","/Action/れ","/Action/み","/Action/ふぁ","/Action/そ"};

    private TextView mTextView;
    private SensorManager mSensorManager;
    private GoogleApiClient mGoogleApiClient;
    private String mNode;
    private float x,y,z,x2,y2,z2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Log.d(TAG, "onConnected");

//                        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                            @Override
                            public void onResult(NodeApi.GetConnectedNodesResult nodes) {
                                //Nodeは１個に限定
                                if (nodes.getNodes().size() > 0) {
                                    mNode = nodes.getNodes().get(0).getId();
                                }
                            }
                        });
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.d(TAG, "onConnectionSuspended");

                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.d(TAG, "onConnectionFailed : " + connectionResult.toString());
                    }
                })
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            x = (x * GAIN + event.values[0] * (1 - GAIN));
            y = (y * GAIN + event.values[1] * (1 - GAIN));
            z = (z * GAIN + event.values[2] * (1 - GAIN));
            x2 = (x * GAIN + event.values[0] * (1 - GAIN));
            y2 = (y * GAIN + event.values[1] * (1 - GAIN));
            z2 = (z * GAIN + event.values[2] * (1 - GAIN));


            if (mTextView != null) mTextView.setText(String.format("X : %f\nY : %f\nZ : %f\nX2 : %f\nY2 : %f\nZ2 : %f\n",x, y, z,x2,y2,z2));

            int motion,motion2;
            motion = detectMotion(x, y, z,x2,y2,z2);
            //motion2 = detectMotion2(x,y,z,x2,y2,z2);
            if (motion > 0 && mNode != null ) {
//                MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient, mNode, SEND_MESSAGES[motion], null).await();
//                if (!result.getStatus().isSuccess()) {
//                    Log.d(TAG, "ERROR : failed to send Message" + result.getStatus());
//                }
                Wearable.MessageApi.sendMessage(mGoogleApiClient, mNode, SEND_MESSAGES[motion], null).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(MessageApi.SendMessageResult result) {
                        if (!result.getStatus().isSuccess()) {
                            Log.d(TAG, "ERROR : failed to send Message" + result.getStatus());
                        }
                    }
                });
              /*  Wearable.MessageApi.sendMessage(mGoogleApiClient, mNode, SEND_MESSAGES2[motion2], null).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(MessageApi.SendMessageResult result) {
                        if (!result.getStatus().isSuccess()) {
                            Log.d(TAG, "ERROR : failed to send Message" + result.getStatus());
                        }
                    }
                });*/
            }

        }
    }

    /**
     * 超適当な判定
     *
     */
    float ox,oy,oz;
    int delay;
    private int detectMotion(float x, float y, float z,float x2, float y2, float z2) {
        int diffX = (int)((x - ox)*10);
        int diffY = (int)((y - oy)*10);
        int diffZ = (int)((z - oz)*10);
        int diffX2 = (int)(x2*10);
        int diffY2 = (int)(y2*10);
        int diffZ2 = (int)(z2*10);
        int motion = 0;

        Log.d(TAG, "s:" + diffX + "/" + diffY + "/" + diffZ + " - " + (int)x + "/" + (int)y + "/" + (int)z);
        //ぼこぼこ
        if (Math.abs(diffZ) > 20) {
            Log.d(TAG, "upper!");
            motion = 2;
            delay = 4;
        } else if (Math.abs(diffY) > 20) {
            Log.d(TAG, "hook!");
            motion = 3;
            delay = 4;
        } else if (Math.abs(diffX) > 20) {
            if (delay == 0) {
                Log.d(TAG, "punch!");
                motion = 1;
            }
        }
        //きゅんきゅん
        if(Math.abs(diffX)> 10 && Math.abs(diffX) <= 20){
            if (delay == 0) {
                Log.d(TAG, "don1!");
                motion = 4;
            }
        }else if(Math.abs(diffX)>20 && Math.abs(diffX) <= 30){
            if (delay == 0) {
                Log.d(TAG, "don2!");
                motion = 5;
            }
        }else if(Math.abs(diffX)>30 && Math.abs(diffX) <= 50){
            if (delay == 0) {
                Log.d(TAG, "don3!");
                motion = 6;
            }
        }
        //楽器
        if ( (diffY2 <= -20)&& Math.abs(diffX)>10) {
            Log.d(TAG, "ど!");
            motion = 7;
            //delay = 4;
        } else if (diffY2 > 20 && Math.abs(diffX)>10) {
            Log.d(TAG, "れ!");
            motion = 8;
            //delay = 4;
        }else if (diffX2 <= 20&& Math.abs(diffZ)>10) {
            Log.d(TAG, "み!");
            motion = 9;
           // delay = 4;
        } else if (diffX2 > 20&& Math.abs(diffZ)>10) {
            Log.d(TAG, "ふぁ!");
            motion = 10;
            //delay = 4;
        }else if (Math.abs(diffY)>30) {
            Log.d(TAG, "そ!");
            motion = 11;
           // delay = 4;
        }

        if (delay > 0) delay--;
        ox = x;
        oy = y;
        oz = z;
        return motion;
    }
    /*private int detectMotion2(float x, float y, float z,float x2, float y2, float z2) {
        int diffX = (int)((x - ox)*10);
        int diffY = (int)((y - oy)*10);
        int diffZ = (int)((z - oz)*10);
        int diffX2 = (int)(x2*10);
        int diffY2 = (int)(y2*10);
        int diffZ2 = (int)(z2*10);
        int motion = 0;

        Log.d(TAG, "s:" + diffX + "/" + diffY + "/" + diffZ + " - " + (int)x + "/" + (int)y + "/" + (int)z);


        //楽器
        if ( (diffY2 <= -20)&& Math.abs(diffX)>10) {
            Log.d(TAG, "ど!");
            motion = 1;
            delay = 4;
        } else if (diffY2 > 20 && Math.abs(diffX)>10) {
            Log.d(TAG, "れ!");
            motion = 2;
            delay = 4;
        }else if (diffX2 <= 20&& Math.abs(diffZ)>10) {
            Log.d(TAG, "み!");
            motion = 3;
            delay = 4;
        } else if (diffX2 > 20&& Math.abs(diffZ)>10) {
            Log.d(TAG, "ふぁ!");
            motion = 4;
            delay = 4;
        }else if (Math.abs(diffY)>10) {
            Log.d(TAG, "そ!");
            motion = 5;
            delay = 4;
        }

        if (delay > 0) delay--;
        ox = x;
        oy = y;
        oz = z;
        return motion;
    }*/

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}