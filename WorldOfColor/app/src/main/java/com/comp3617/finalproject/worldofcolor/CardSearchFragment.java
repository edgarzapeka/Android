package com.comp3617.finalproject.worldofcolor;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by edz on 2017-07-15.
 */

public class CardSearchFragment extends Fragment {

    private static final String COLOR_CODE = "color_code";

    private String mCode;
    private List<Card> mCards;

    private CardListAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mCardsDatabaseReference;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getArguments();
        mCode = bundle.getString(COLOR_CODE);
        mCards = new ArrayList<Card>();
        mAdapter = new CardListAdapter(mCards);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mCardsDatabaseReference = mFirebaseDatabase.getReference().child("cards");

        mCardsDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Card> allCards = new ArrayList<Card>();
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    HashMap<String, Object> map = (HashMap<String, Object>) singleSnapshot.getValue();
                    Card tmpCard = new Card();
                    tmpCard.setAuthorsName((String)map.get("authorsName"));
                    tmpCard.setImageUrl((String)map.get("imageUrl"));
                    tmpCard.setLikes((ArrayList<String>)map.get("likes"));

                    ArrayList<CardColor> mapColors = (ArrayList<CardColor>)map.get("colors");
                    tmpCard.setColorsFromHashMap(mapColors);
                    allCards.add(tmpCard);
                }

                mAdapter.setCards(findMatchingPhotos(allCards));
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_card_search, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.cards_list_search);
        //mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        mRecyclerView.setAdapter(mAdapter);

        return v;
    }

    public static CardSearchFragment getInstance(String colorName){
        Bundle bundle = new Bundle();
        bundle.putString(COLOR_CODE, colorName);

        CardSearchFragment fragment = new CardSearchFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    public ArrayList<Card> findMatchingPhotos(ArrayList<Card> cards){
        ArrayList<Card> result = new ArrayList<>();
        for(Card card : cards){
            for(CardColor color : card.getColors()){
                if (color.getName().equals(mCode)){
                    result.add(card);
                }
            }
        }

        return result;
    }


    private class CardListAdapter extends RecyclerView.Adapter<CardListAdapter.CardsViewHolder> {

        private List<Card> mCards;

        public CardListAdapter(List<Card> cards){
            mCards = cards;
        }

        @Override
        public CardsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_search_item_row, parent, false);

            return new CardsViewHolder(v);
        }

        public void setCards(ArrayList<Card> cards){
            mCards = cards;
        }

        @Override
        public void onBindViewHolder(CardsViewHolder holder, final int position) {
            holder.mAuthor.setText(mCards.get(position).getAuthorsName());

            Picasso.with(holder.mImage.getContext()).load(mCards.get(position).getImageUrl()).into(holder.mImage);

            holder.mCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Query queryRef = mCardsDatabaseReference.orderByChild("imageUrl").equalTo(mCards.get(position).getImageUrl());
                    queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String key = "";
                            for(DataSnapshot childSnapshot : dataSnapshot.getChildren()){
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

        public void add(Card card){
            mCards.add(card);
            notifyDataSetChanged();
        }

        class CardsViewHolder extends RecyclerView.ViewHolder{

            private CardView mCardView;
            private TextView mAuthor;
            private ImageView mImage;

            public CardsViewHolder(View v){
                super(v);

                mCardView = (CardView) v.findViewById(R.id.card_search_view);
                mAuthor = (TextView) v.findViewById(R.id.author_card_search_row);
                mImage = (ImageView) v.findViewById(R.id.image_card_search_row);
            }
        }
    }
}
