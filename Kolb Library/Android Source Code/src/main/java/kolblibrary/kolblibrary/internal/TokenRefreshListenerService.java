/*
* @authors Tejas Priyadarshi, Christopher Seiler, Neelay Velingker
*/
package kolblibrary.kolblibrary.internal;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

import kolblibrary.kolblibrary.internal.RegistrationService;



public class TokenRefreshListenerService extends InstanceIDListenerService {
    @Override public void onTokenRefresh() {
        Intent i = new Intent(this, RegistrationService.class);
        startService(i);
    }
}