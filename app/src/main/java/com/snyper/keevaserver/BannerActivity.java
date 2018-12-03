package com.snyper.keevaserver;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.snyper.keevaserver.Common.Common;
import com.snyper.keevaserver.ViewHolder.BannerViewHolder;
import com.snyper.keevaserver.ViewHolder.FoodViewHolder;
import com.snyper.keevaserver.model.Banner;
import com.snyper.keevaserver.model.Food;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import info.hoang8f.widget.FButton;

public class BannerActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    FloatingActionButton fab;

    //Firebase
    FirebaseDatabase db;
    DatabaseReference banners;
    FirebaseStorage storage;
    StorageReference storageReference;
    FirebaseRecyclerAdapter<Banner,BannerViewHolder> adapter;
    RelativeLayout rootLayout;
    //add new banner
    MaterialEditText edtName,edtFoodId;
    FButton btnUpload,btnSelect;

    Banner newBanner;
    Uri filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner);

        //init firebase
        db=FirebaseDatabase.getInstance();
        banners=db.getReference("Banner");
        storage=FirebaseStorage.getInstance();
        storageReference= storage.getReference();

        recyclerView=(RecyclerView)findViewById(R.id.recycler_banner);
        recyclerView.setHasFixedSize(true);
        layoutManager= new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        rootLayout=(RelativeLayout)findViewById(R.id.rootLayout);

        fab=(FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showAddBanner();

            }
        });


        loadListBanner();

    }

    private void loadListBanner() {
        FirebaseRecyclerOptions<Banner> allBanner=new FirebaseRecyclerOptions.Builder<Banner>()
                .setQuery(banners,Banner.class)
                .build();

        adapter= new FirebaseRecyclerAdapter<Banner, BannerViewHolder>(allBanner) {
            @Override
            protected void onBindViewHolder(@NonNull BannerViewHolder viewHolder, int position, @NonNull Banner model) {
                viewHolder.banner_name.setText(model.getName());
                Picasso.with(getBaseContext())
                        .load(model.getImage())
                        .into(viewHolder.banner_image);
            }

            @NonNull
            @Override
            public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView= LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.banner_layout,parent,false);
                return new BannerViewHolder(itemView);
            }
        };
        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if(item.getTitle().equals(Common.UPDATE)){

            showUpdateBannerDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
        }else  if(item.getTitle().equals(Common.DELETE)){

            deleteBanner(adapter.getRef(item.getOrder()).getKey());
        }

        return super.onContextItemSelected(item);


    }

    private void deleteBanner(String key) {
       banners.child(key).removeValue();
    }

    private void showUpdateBannerDialog(final String key, final Banner item) {
        final AlertDialog.Builder alertDialog= new AlertDialog.Builder(BannerActivity.this);
        alertDialog.setTitle("Edit Banner");
        alertDialog.setMessage("Please add full information");

        LayoutInflater inflater= this.getLayoutInflater();
        View edit_banner= inflater.inflate(R.layout.add_new_banner,null);


        edtName=edit_banner.findViewById(R.id.edtFoodName);
        edtFoodId=edit_banner.findViewById(R.id.edtFoodId);

        //setting a default value for view
        edtName.setText(item.getName());
        edtFoodId.setText(item.getId());

        btnSelect=edit_banner.findViewById(R.id.btnSelect);
        btnUpload=edit_banner.findViewById(R.id.btnUpload);

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();//allows u to chose image from gallary and save url for de image

            }
        });
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                changeImage(item);

            }
        });
        alertDialog.setView(edit_banner);
        alertDialog.setIcon(R.drawable.ic_tablet_black_24dp);

        //set button
        alertDialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                //create new category
                item.setName(edtName.getText().toString());
                item.setId(edtFoodId.getText().toString());

                //make update
                Map<String,Object>update= new HashMap<>();
                update.put("id",item.getId());
                update.put("name",item.getName());
                update.put("image",item.getImage());

                banners.child(key)
                        .updateChildren(update)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Snackbar.make(rootLayout,"Updated",Snackbar.LENGTH_SHORT).show();
                                loadListBanner();
                            }
                        });

                Snackbar.make(rootLayout,"New Food"+item.getName()+"was added",Snackbar.LENGTH_SHORT).show();
                loadListBanner();
            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                loadListBanner();
            }
        });
        alertDialog.show();
    }

    private void changeImage(final Banner item) {
        if (filePath !=null){

            final ProgressDialog mDialog= new ProgressDialog(this);
            mDialog.setMessage("Uploading..");
            mDialog.show();


            String imageName= UUID.randomUUID().toString();
            final StorageReference imageFolder= storageReference.child("images/"+imageName);
            imageFolder.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mDialog.dismiss();
                    Toast.makeText(BannerActivity.this,"Sent",Toast.LENGTH_SHORT).show();
                    imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //set new category if image is been uploaded succesfully and we can get downloasd link
                            item.setImage(uri.toString());
                        }
                    });
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Toast.makeText(BannerActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    //dey may be error hr buh ignore
                    double progess=(100.0* taskSnapshot.getBytesTransferred() /taskSnapshot.getTotalByteCount() );
                    mDialog.setMessage("Uploaded"+progess+"%");

                }
            });

        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==Common.PICK_IMAGE_REQUEST && resultCode ==RESULT_OK && data !=null && data.getData() !=null)
        {
            filePath=data.getData();
            btnSelect.setText("Image Selected");
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
    private void showAddBanner() {
        final AlertDialog.Builder alertDialog= new AlertDialog.Builder(BannerActivity.this);
        alertDialog.setTitle("Add new Food");
        alertDialog.setMessage("Please add full information");

        LayoutInflater inflater= this.getLayoutInflater();
        View v= inflater.inflate(R.layout.add_new_banner,null);

        edtFoodId= v.findViewById(R.id.edtFoodId);
        edtName= v.findViewById(R.id.edtFoodName);

        btnSelect=v.findViewById(R.id.btnSelect);
        btnUpload=v.findViewById(R.id.btnUpload);

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();//allows u to chose image from gallary and save url for de image

            }
        });
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                uploadImage();

            }
        });


        alertDialog.setView(v);
        alertDialog.setIcon(R.drawable.ic_tablet_black_24dp);

        //set button for dialog
        alertDialog.setPositiveButton("CREATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog .dismiss();
                if (newBanner!=null){
                    banners.push()
                            .setValue(newBanner);
                    loadListBanner();

                }
            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                newBanner=null;
                loadListBanner();

            }
        });
        alertDialog.show();


    }

    private void uploadImage() {

        if (filePath!=null){

            final ProgressDialog mDialog= new ProgressDialog(this);
            mDialog.setMessage("Uploading..");
            mDialog.show();


            String imageName= UUID.randomUUID().toString();
            final StorageReference imageFolder= storageReference.child("images/"+imageName);
            imageFolder.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mDialog.dismiss();
                    Toast.makeText(BannerActivity.this,"Sent",Toast.LENGTH_SHORT).show();
                    imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //set new category if image is been uploaded succesfully and we can get downloasd link
                            newBanner= new Banner();
                            newBanner.setName(edtName.getText().toString());
                            newBanner.setName(edtFoodId.getText().toString());
                            newBanner.setImage(uri.toString());
                        }
                    });
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Toast.makeText(BannerActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    //dey may be error hr buh ignore
                    double progress=(100.0* taskSnapshot.getBytesTransferred() /taskSnapshot.getTotalByteCount() );
                    mDialog.setMessage("Uploaded"+progress+"%");





                }
            });

        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"), Common.PICK_IMAGE_REQUEST);

    }
}
