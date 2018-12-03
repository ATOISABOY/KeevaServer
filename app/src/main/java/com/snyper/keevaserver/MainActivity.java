package com.snyper.keevaserver;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.felipecsl.gifimageview.library.GifImageView;
import com.nex3z.togglebuttongroup.SingleSelectToggleGroup;
import com.nex3z.togglebuttongroup.button.LabelToggle;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    Button btnSignIn;
    TextView txtSlogan;
 private GifImageView gifImageView;

 public boolean choiceManagerIschecked=false;
    public boolean keevaCarrierIschecked=false;
    public boolean CustomIschecked=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnSignIn=(Button)findViewById(R.id.btnSignIn);

        gifImageView=(GifImageView)findViewById(R.id.gif);

       final LabelToggle choiceManager=(LabelToggle)findViewById(R.id.choice_Manager);
        final LabelToggle choiceKeevaCarrier=(LabelToggle)findViewById(R.id.choice_KeevaCarrier);
        final LabelToggle choiceCustom=(LabelToggle)findViewById(R.id.choice_Custom);

        SingleSelectToggleGroup singleSelectToggleGroup=(findViewById(R.id.loginChoice));
        singleSelectToggleGroup.setOnCheckedChangeListener(new SingleSelectToggleGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SingleSelectToggleGroup group, int checkedId) {

                if (checkedId==choiceManager.getId()){
                    choiceManagerIschecked=true;
                    keevaCarrierIschecked=false;
                    CustomIschecked=false;

                }
                if (checkedId==choiceKeevaCarrier.getId()){
                    keevaCarrierIschecked=true;
                    choiceManagerIschecked=false;
                    CustomIschecked=false;

                }
                if (checkedId==choiceCustom.getId()){
                    CustomIschecked=true;
                    keevaCarrierIschecked=false;
                    choiceManagerIschecked=false;
                }


            }
        });
        try
        {
            InputStream inputStream=getAssets().open("finalkeeva.gif");
            byte[] bytes= IOUtils.toByteArray(inputStream);
            gifImageView.setBytes(bytes);
            gifImageView.startAnimation();

        }catch (IOException ex)
        {

        }

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (choiceManagerIschecked){
                    Intent signIn= new Intent(MainActivity.this,SignIn.class);
                    startActivity(signIn);
                }
                if (CustomIschecked){
                    Intent signIn= new Intent(MainActivity.this,CustomDeliveryLogin.class);
                    startActivity(signIn);

                }


            }
        });


    }
}
