package com.comp3617.finalproject.worldofcolor;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.comp3617.finalproject.worldofcolor.data.Card;
import com.comp3617.finalproject.worldofcolor.data.CardColor;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.squareup.picasso.Picasso.with;

//import android.net.Uri;

/**
 * Created by edz on 2017-07-11.
 */

public class CardsListFragment extends Fragment {

    private final int CAMERA_PERMISSIONS = 9042;
    private final int WRITE_STORAGE_PERMISSIONS = 8123;
    private final int RC_PHOTO_PICKER = 1;
    private final int RC_CAMERA_PICKER = 2;
    private final String DEFAULT_USERNAME = "Anonymous";
    private static final int RC_SIGN_IN = 123;
    private String mCurrentPhotoPath;

    private String mUsername;
    private CardListAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private FloatingActionButton mSelectImageButton;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseStorage mFirebaseStorage;
    private DatabaseReference mCardsDatabaseReference;
    private StorageReference mImagesStorageReference;
    private ChildEventListener mChildEventListener;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    onSignedInCleanup();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(
                                            Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                                    new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                    .build(),
                            RC_SIGN_IN);
                    mAdapter.notifyDataSetChanged();
                } else{
                    onSignedInInitialize(user.getDisplayName());
                }
            }
        };

        mAdapter = new CardListAdapter(new ArrayList<Card>());

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mCardsDatabaseReference = mFirebaseDatabase.getReference().child("cards");
        mImagesStorageReference = mFirebaseStorage.getReference().child("cards_photos");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_cards_list, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.cards_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mAdapter);
        mSelectImageButton = (FloatingActionButton) v.findViewById(R.id.select_image);
        mSelectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (reqeustPermissionsForStorage()){
                    selectImage();
                }
            }
        });
        //mRecyclerView.getRecycledViewPool().setMaxRecycledViews(0, 0);

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_random_image:
                uploadRandomImages();
                return true;
            case R.id.sign_out:
                mFirebaseAuth.signOut();
                mAdapter.mCards = new ArrayList<>();
                return true;
            case R.id.user_account:
                FragmentManager fm = getActivity().getSupportFragmentManager();
                Fragment fragment = new UserAccountFragment();
                fm.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            StorageReference imageRef = mImagesStorageReference.child(selectedImageUri.getLastPathSegment());

            updatePicture(selectedImageUri, imageRef);
        } else if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getContext(), "You're signed in. Welcome!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                getActivity().finish();
            }
        } else if (requestCode == RC_CAMERA_PICKER && resultCode == RESULT_OK ) {
             Bitmap sourse = BitmapFactory.decodeFile(mCurrentPhotoPath, provideCompressionBitmapFactoryOptions());
             updatePicture(getResizedBitmap(sourse, 800, 800), mImagesStorageReference.child(new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())));

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void updatePicture(final Uri uri, final StorageReference imageRef) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Bitmap bitmap = with(getContext()).load(uri).resize(800, 800).get();
                    Palette palette = Palette.from(bitmap).generate();

                    final List<CardColor> cardColors = new ArrayList<>();
                    List<Palette.Swatch> result = new ArrayList<>(palette.getSwatches());

                    Collections.sort(result, new Comparator<Palette.Swatch>() {
                        @Override
                        public int compare(Palette.Swatch o1, Palette.Swatch o2) {
                            return Integer.compare(o2.getPopulation(), o1.getPopulation());
                        }
                    });

                    for (Palette.Swatch swatch : result) {
                        CardColor cardColorTmp = new CardColor();
                        cardColorTmp.setCode(swatch.getRgb());
                        cardColorTmp.setName(String.valueOf(swatch.getRgb()));
                        cardColorTmp.setBodyColor(swatch.getBodyTextColor());
                        cardColorTmp.setTitleColor(swatch.getTitleTextColor());
                        cardColorTmp.setPopulation(swatch.getPopulation());

                        cardColors.add(cardColorTmp);
                    }

                    final ArrayList<String> likes = new ArrayList<String>();
                    likes.add(mFirebaseAuth.getCurrentUser().getUid() == null ? "Random Image" : mFirebaseAuth.getCurrentUser().getUid());

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] testData = baos.toByteArray();
                    imageRef.putBytes(testData)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                    Card card = new Card(downloadUrl.toString(), mUsername);
                                    card.setColors(cardColors);
                                    card.setLikes(likes);
                                    card.setAuthorId(mFirebaseAuth.getCurrentUser().getUid());
                                    mCardsDatabaseReference.push().setValue(card);
                                }
                            });
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }


            @Override
            protected void onPostExecute(Void aVoid) {
            }
        }.execute();
    }

    private void updatePicture(final Bitmap bitmap, final StorageReference imageRef) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    //Bitmap image = getResizedBitmap(bitmap, 800, 800);
                    Bitmap image  = bitmap;
                    Palette palette = Palette.from(bitmap).generate();

                    final List<CardColor> cardColors = new ArrayList<>();
                    List<Palette.Swatch> result = new ArrayList<>(palette.getSwatches());

                    Collections.sort(result, new Comparator<Palette.Swatch>() {
                        @Override
                        public int compare(Palette.Swatch o1, Palette.Swatch o2) {
                            return Integer.compare(o2.getPopulation(), o1.getPopulation());
                        }
                    });

                    for (Palette.Swatch swatch : result) {
                        CardColor cardColorTmp = new CardColor();
                        cardColorTmp.setCode(swatch.getRgb());
                        cardColorTmp.setName(String.valueOf(swatch.getRgb()));
                        cardColorTmp.setBodyColor(swatch.getBodyTextColor());
                        cardColorTmp.setTitleColor(swatch.getTitleTextColor());
                        cardColorTmp.setPopulation(swatch.getPopulation());

                        cardColors.add(cardColorTmp);
                    }

                    final ArrayList<String> likes = new ArrayList<String>();
                    likes.add(mFirebaseAuth.getCurrentUser().getUid() == null ? "Random Image" : mFirebaseAuth.getCurrentUser().getUid());

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] testData = baos.toByteArray();
                    imageRef.putBytes(testData)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                    Card card = new Card(downloadUrl.toString(), mUsername);
                                    card.setColors(cardColors);
                                    card.setLikes(likes);
                                    card.setAuthorId(mFirebaseAuth.getCurrentUser().getUid());
                                    mCardsDatabaseReference.push().setValue(card);
                                }
                            });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
            }
        }.execute();
    }

    private class CardListAdapter extends RecyclerView.Adapter<CardListAdapter.CardsViewHolder> {

        private List<Card> mCards;

        public CardListAdapter(ArrayList<Card> cards) {
            mCards = cards;
        }

        @Override
        public CardsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cards_list_row, parent, false);

            return new CardsViewHolder(v);
        }

        public void updateItem(Card card) {
            int i = 0;
            for (Card c : mCards) {
                if (c.getDbKey().equals(card.getDbKey())) {
                    mCards.set(i, card);
                    return;
                }
                i++;
            }
        }

        @Override
        public void onBindViewHolder(final CardsViewHolder holder, int position) {
            holder.mAuthor.setText(mCards.get(position).getAuthorsName());
            holder.mAuthor.setTextColor(mCards.get(position).getColors().get(0).getTitleColor());
            holder.mLikesNumber.setTextColor(getResources().getColor(R.color.colorAccent));
            holder.mLike.setColorFilter(getResources().getColor(R.color.colorAccent));

            if (mCards.get(position).getAuthorId().equals(mFirebaseAuth.getCurrentUser().getUid())) {
                holder.mLike.setVisibility(View.INVISIBLE);
            } else{
                holder.mLike.setVisibility(View.VISIBLE);
            }
            if (mCards.get(position).getLikes().size() > 1) {
                holder.mLikesNumber.setText(String.valueOf(mCards.get(position).getLikes().size() - 1));
            } else {
                holder.mLikesNumber.setText("");
            }
            if (mCards.get(position).getLikes().contains(mFirebaseAuth.getCurrentUser().getUid())) {
                holder.mLike.setImageResource(R.drawable.filled_like);
                //Picasso.with(holder.mLike.getContext()).load(R.drawable.filled_like).into(holder.mImage);
            } else {
                holder.mLike.setImageResource(R.drawable.empty_like);
                // Picasso.with(holder.mLike.getContext()).load(R.drawable.empty_like).into(holder.mImage);
            }

            holder.mLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCards.get(holder.getAdapterPosition()).getLikes().contains(mFirebaseAuth.getCurrentUser().getUid())) {
                        if (mCards.get(holder.getAdapterPosition()).getLikes().size() > 1) {
                            mCards.get(holder.getAdapterPosition()).getLikes().remove(mFirebaseAuth.getCurrentUser().getUid());
                        }
                    } else {
                        mCards.get(holder.getAdapterPosition()).getLikes().add(mFirebaseAuth.getCurrentUser().getUid());
                    }
                    mCardsDatabaseReference.child(mCards.get(holder.getAdapterPosition()).getDbKey()).setValue(mCards.get(holder.getAdapterPosition()));
                }
            });

            Picasso.with(holder.mCardView.getContext()).load(mCards.get(holder.getAdapterPosition()).getImageUrl()).into(holder.mImage);

            holder.mCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Query queryRef = mCardsDatabaseReference.orderByChild("imageUrl").equalTo(mCards.get(holder.getAdapterPosition()).getImageUrl());
                    queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String key = "";
                            for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                key = childSnapshot.getKey();
                            }

                            FragmentManager fm = getFragmentManager();
                            Fragment fragment = CardInfoFragment.getInstance(key);
                            fm.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });
                }
            });
        }

        @Override
        public int getItemCount() {
            return mCards.size();
        }

        public void add(Card card) {
            mCards.add(0, card);
            notifyDataSetChanged();
        }

        class CardsViewHolder extends RecyclerView.ViewHolder {

            private CardView mCardView;
            private TextView mAuthor;
            private ImageView mImage;
            private ImageView mLike;
            private TextView mLikesNumber;

            public CardsViewHolder(View v) {
                super(v);

                mCardView = (CardView) v.findViewById(R.id.card_view);
                mAuthor = (TextView) v.findViewById(R.id.author_card_row);
                mImage = (ImageView) v.findViewById(R.id.image_card_row);
                mLike = (ImageView) v.findViewById(R.id.like_card_row);
                mLikesNumber = (TextView) v.findViewById(R.id.number_of_likes_card_row);
            }
        }
    }

    public void uploadRandomImages() {

        for (int i = 0; i < 10; i++) {

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        Random rand = new Random();
                        int value = rand.nextInt(9999);
                        String pictureName = String.valueOf(new Date().getTime() + value);

                        Bitmap image = Picasso.with(getContext()).load("https://source.unsplash.com/random").resize(800, 800).get();
                        Palette palette = Palette.from(image).generate();

                        final List<CardColor> cardColors = new ArrayList<>();
                        List<Palette.Swatch> result = new ArrayList<>(palette.getSwatches());

                        Collections.sort(result, new Comparator<Palette.Swatch>() {
                            @Override
                            public int compare(Palette.Swatch o1, Palette.Swatch o2) {
                                return Integer.compare(o2.getPopulation(), o1.getPopulation());
                            }
                        });

                        for (Palette.Swatch swatch : result) {
                            CardColor cardColorTmp = new CardColor();
                            cardColorTmp.setCode(swatch.getRgb());
                            cardColorTmp.setName(String.valueOf(swatch.getRgb()));
                            cardColorTmp.setBodyColor(swatch.getBodyTextColor());
                            cardColorTmp.setTitleColor(swatch.getTitleTextColor());
                            cardColorTmp.setPopulation(swatch.getPopulation());

                            cardColors.add(cardColorTmp);
                        }

                        final ArrayList<String> likes = new ArrayList<String>();
                        likes.add(mFirebaseAuth.getCurrentUser().getUid() == null ? "Random Image" : mFirebaseAuth.getCurrentUser().getUid());


                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] testData = baos.toByteArray();
                        mImagesStorageReference.child(pictureName).putBytes(testData)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                        Card card = new Card(downloadUrl.toString(), mUsername);
                                        card.setColors(cardColors);
                                        card.setLikes(likes);
                                        card.setAuthorId(mFirebaseAuth.getCurrentUser().getUid());
                                        mCardsDatabaseReference.push().setValue(card);
                                    }
                                });
                        Thread.sleep(5000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                }
            }.execute();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        detachDatabaseReadListener();
        mAdapter.mCards = new ArrayList<>();
    }

    private void selectImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
    }

    private void takePhoto() {
        if (requestPermissionsForCamera()){
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null){
                Uri photoURI = null;
                try{
                    File photoFile = createImageFileWith();
                    mCurrentPhotoPath = photoFile.getAbsolutePath();
                    photoURI = FileProvider.getUriForFile(getActivity().getApplicationContext(),
                            getString(R.string.file_provider_authority), photoFile);
                } catch (IOException e){
                    e.printStackTrace();
                }
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, RC_CAMERA_PICKER);
            }
        }
    }

    public File createImageFileWith() throws IOException {
        final String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        final String imageFileName = "JPEG_" + timestamp;
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "pics");
        storageDir.mkdirs();
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private static BitmapFactory.Options provideCompressionBitmapFactoryOptions() {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = false;
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        return opt;
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add Photo");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (items[which].equals("Take Photo")) {
                    takePhoto();
                } else if (items[which].equals("Choose from Gallery")) {
                    selectImageFromGallery();
                } else if (items[which].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void attachDatabaseReadListener(){
        if (mChildEventListener == null){
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Card card = dataSnapshot.getValue(Card.class);
                    card.setDbKey(dataSnapshot.getKey());
                    mAdapter.add(card);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Card card = dataSnapshot.getValue(Card.class);
                    mAdapter.updateItem(card);
                    mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {}

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            };
            mCardsDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    private void detachDatabaseReadListener(){
        if (mChildEventListener != null){
            mCardsDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    private boolean requestPermissionsForCamera(){
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[] {android.Manifest.permission.CAMERA}, CAMERA_PERMISSIONS);
        } else{
            return true;
        }

        return false;
    }

    private boolean reqeustPermissionsForStorage(){
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_STORAGE_PERMISSIONS);
        } else {
            return true;
        }

        return false;
    }

    private void onSignedInInitialize(String userName){
        mUsername = userName;
        attachDatabaseReadListener();
    }

    private void onSignedInCleanup(){
        mUsername = DEFAULT_USERNAME;
        detachDatabaseReadListener();
        mAdapter.mCards = new ArrayList<>();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    takePhoto();
                } else{
                    Toast.makeText(getContext(), "Sorry, we need camera permission to take a photo", Toast.LENGTH_LONG).show();
                }
                return;
            }
            case WRITE_STORAGE_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    selectImage();
                } else{
                    Toast.makeText(getContext(), "Sorry, we need storage permission in order to use furhter functionality", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
