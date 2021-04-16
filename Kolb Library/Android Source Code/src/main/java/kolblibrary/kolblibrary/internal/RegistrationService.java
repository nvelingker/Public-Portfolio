/*
* @authors Tejas Priyadarshi, Christopher Seiler, Neelay Velingker
*/
package kolblibrary.kolblibrary.internal;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import kolblibrary.kolblibrary.R;
import kolblibrary.kolblibrary.internal.Networking;


public class RegistrationService extends IntentService {
    public RegistrationService() {
        super("RegistrationService");
    }
    @Override protected void onHandleIntent(Intent intent) {
        try {
	    // Attempts to obtain registration token from GCM.
            InstanceID myID = InstanceID.getInstance(getApplicationContext());
            String registrationToken = myID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE,
                    null);
            Log.d("Message", "Registration token received: " + registrationToken);
            Networking.registerPushKey(registrationToken);
        } catch(IOException ioe) {
            Log.e("Message", "Unable to obtain registration token:");
	    ioe.printStackTrace();
        }
    }
}
