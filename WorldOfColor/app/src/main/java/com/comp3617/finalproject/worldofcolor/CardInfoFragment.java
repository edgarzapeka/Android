package com.comp3617.finalproject.worldofcolor;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.comp3617.finalproject.worldofcolor.data.Card;
import com.comp3617.finalproject.worldofcolor.data.CardColor;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by edz on 2017-07-12.
 */

public class CardInfoFragment extends Fragment {

    private final static String CARD_REF = "cardinfofragmentconstant";

    private ImageView mImageView;
    private RecyclerView mRecyclerView;
    private ColorListAdapter mColorListAdapter;
    private LinearLayout mCardInfoLayout;

    private Card mCard = new Card();
    private Bitmap mBitmap;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mCardsDatabaseReference;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mCardsDatabaseReference = mFirebaseDatabase.getReference("cards");

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_card_info, container, false);
        Bundle bundle = this.getArguments();

        mCardInfoLayout = (LinearLayout) v.findViewById(R.id.card_info_fragment_layout);
        Query oneRef = mCardsDatabaseReference.child(bundle.getString(CARD_REF));
        oneRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, Object> map = (HashMap<String, Object>) dataSnapshot.getValue();
                mCard.setAuthorsName((String)map.get("authorsName"));
                mCard.setImageUrl((String)map.get("imageUrl"));

                ArrayList<CardColor> mapColors = (ArrayList<CardColor>)map.get("colors");
                mCard.setColorsFromHashMap(mapColors);
                mCardInfoLayout.setBackgroundColor(mCard.getColors().get(0).getBodyColor());

                mColorListAdapter = new ColorListAdapter(mCard.getColors());
                mColorListAdapter.setTotalNumberOfPixelsDetected(mCard.getTotalPopulation());
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                mRecyclerView.setAdapter(mColorListAdapter);

                Picasso.with(getContext()).load(mCard.getImageUrl()).into(mImageView);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        mImageView = (ImageView) v.findViewById(R.id.card_image_info);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.dominant_colors_list_info);

        return v;
    }

    public static CardInfoFragment getInstance(String ref){
        Bundle bundle = new Bundle();
        bundle.putString(CARD_REF, ref);

        CardInfoFragment fragment = new CardInfoFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    private class ColorListAdapter extends RecyclerView.Adapter<ColorListAdapter.CardsViewHolder> {

        private List<CardColor> mCardsColors;
        private int mTotalNumberOfPixelsDetected;

        public ColorListAdapter(List<CardColor> cardsColors){
            mCardsColors = cardsColors;
        }

        @Override
        public CardsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.color_info_row, parent, false);

            return new CardsViewHolder(v);
        }

        @Override
        public void onBindViewHolder(CardsViewHolder holder, final int position) {
            Double num = (double) mCardsColors.get(position).getPopulation() * 100 / mTotalNumberOfPixelsDetected;
            holder.mColorPercentage.setText("Percentage " + new DecimalFormat("##.#").format(num) + "%");
            holder.mColorPercentage.setTextColor(mCardsColors.get(position).getBodyColor());
            holder.mColorName.setTextColor(mCardsColors.get(position).getTitleColor());
            holder.mCardRow.setBackgroundColor(mCardsColors.get(position).getCode());
            int green = Color.green(mCardsColors.get(position).getCode());
            int red = Color.red(mCardsColors.get(position).getCode());
            int blue = Color.blue(mCardsColors.get(position).getCode());
            holder.mColorName.setText(String.format(getString(R.string.rgb_format), red, green, blue));

            holder.mCardRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager fm = getFragmentManager();
                    Fragment fragment = CardSearchFragment.getInstance(mCardsColors.get(position).getName());
                    fm.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mCardsColors.size();
        }

        class CardsViewHolder extends RecyclerView.ViewHolder{

            private TextView mColorName;
            private TextView mColorPercentage;
            private FrameLayout mCardRow;

            public CardsViewHolder(View v){
                super(v);

                mColorName = (TextView) v.findViewById(R.id.color_name);
                mCardRow = (FrameLayout) v.findViewById(R.id.color_info_row);
                mColorPercentage = (TextView) v.findViewById(R.id.color_percentage);
            }
        }

        public void setTotalNumberOfPixelsDetected(int number){
            mTotalNumberOfPixelsDetected = number;
        }
    }
}
