package com.snyper.keevaserver.Service;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.snyper.keevaserver.Common.Common;
import com.snyper.keevaserver.model.Token;

/**
 * Created by stephen snyper on 11/28/2018.
 */

public class MyFirebaseIdServiceShipper extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        sendTokenToServer(refreshedToken);
    }

    private void sendTokenToServer(String refreshedToken) {

        if (Common.currentShipper!=null) {
            FirebaseDatabase db = FirebaseDatabase.getInstance();
            DatabaseReference tokens = db.getReference("Tokens");
            Token data = new Token(refreshedToken, true);//
            tokens.child(Common.currentUser.getPhone()).setValue(data);
        }
    }
}
