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
import com.google.firebase.auth.FirebaseAuth;
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
 * Created by edz on 2017-07-16.
 */

public class UserAccountFragment extends Fragment {

    private TextView mUserCardsNumber;
    private TextView mUserNumberOfReceivedLikes;
    private RecyclerView mUserCardsRecyclerView;
    private RecyclerView mUserLikedCardsRecyclerView;
    private CardListAdapter mLikedCardsAdapter;
    private CardListAdapter mUserCardsAdapter;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mCardsDatabaseReference;
    private FirebaseAuth mFirebaseAuth;
    private ValueEventListener mDatabaseListener;

    private int mNumberOfReceivedLikes;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLikedCardsAdapter = new CardListAdapter(new ArrayList<Card>());
        mUserCardsAdapter = new CardListAdapter(new ArrayList<Card>());
        mNumberOfReceivedLikes = 0;

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mCardsDatabaseReference = mFirebaseDatabase.getReference().child("cards");

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user_account, container, false);

        mUserCardsNumber = (TextView) v.findViewById(R.id.user_card_number_text);
        mUserNumberOfReceivedLikes = (TextView) v.findViewById(R.id.user_liked_number_text);
        mUserCardsRecyclerView = (RecyclerView) v.findViewById(R.id.user_card_list);
        mUserLikedCardsRecyclerView = (RecyclerView) v.findViewById(R.id.user_liked_list);

        mUserCardsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        mUserCardsRecyclerView.setAdapter(mUserCardsAdapter);
        mUserLikedCardsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        mUserLikedCardsRecyclerView.setAdapter(mLikedCardsAdapter);

        return v;
    }

    private class CardListAdapter extends RecyclerView.Adapter<CardListAdapter.CardsViewHolder> {

        private List<Card> mCards;

        public CardListAdapter(List<Card> cards){
            mCards = cards;
        }

        @Override
        public CardsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_user_info_row, parent, false);

            return new CardsViewHolder(v);
        }

        public void setCards(ArrayList<Card> cards){
            mCards = cards;
            notifyDataSetChanged();
        }

        @Override
        public void onBindViewHolder(final CardsViewHolder holder, final int position) {
            holder.mAuthor.setText(mCards.get(position).getAuthorsName());

            if (mCards.get(position).getAuthorId().equals(mFirebaseAuth.getCurrentUser().getUid())){
                holder.mDeleteCard.setVisibility(View.VISIBLE);
                holder.mDeleteCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCardsDatabaseReference.child(mCards.get(position).getDbKey()).removeValue();
                    }
                });
            } else{
                holder.mDeleteCard.setVisibility(View.INVISIBLE);
            }
            Picasso.with(holder.mImage.getContext()).load(mCards.get(position).getImageUrl()).into(holder.mImage);

            holder.mImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Query queryRef = mCardsDatabaseReference.orderByChild("imageUrl").equalTo(mCards.get(holder.getPosition()).getImageUrl());
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
            private ImageView mDeleteCard;

            public CardsViewHolder(View v){
                super(v);

                mCardView = (CardView) v.findViewById(R.id.card_user_info_view);
                mAuthor = (TextView) v.findViewById(R.id.author_card_user_info_row);
                mImage = (ImageView) v.findViewById(R.id.image_card_user_info_row);
                mDeleteCard = (ImageView) v.findViewById(R.id.delete_card);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        attachDatabaseListener();
        detachDatabaseListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        attachDatabaseListener();
    }

    private void attachDatabaseListener(){
        if (mDatabaseListener == null){
            mDatabaseListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mNumberOfReceivedLikes = 0;
                ArrayList<Card> userCards = new ArrayList<Card>();
                ArrayList<Card> userLikedCards = new ArrayList<Card>();
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    HashMap<String, Object> map = (HashMap<String, Object>) singleSnapshot.getValue();
                    Card tmpCard = new Card();
                    tmpCard.setAuthorsName((String)map.get("authorsName"));
                    tmpCard.setAuthorId((String)map.get("authorId"));
                    tmpCard.setImageUrl((String)map.get("imageUrl"));
                    tmpCard.setLikes((ArrayList<String>)map.get("likes"));
                    tmpCard.setDbKey(singleSnapshot.getKey());

                    ArrayList<CardColor> mapColors = (ArrayList<CardColor>)map.get("colors");
                    tmpCard.setColorsFromHashMap(mapColors);
                    if (tmpCard.getAuthorId().equals(mFirebaseAuth.getCurrentUser().getUid())){
                        userCards.add(tmpCard);
                        mNumberOfReceivedLikes += tmpCard.getLikes().size()-1;
                    } else if(tmpCard.getLikes().contains(mFirebaseAuth.getCurrentUser().getUid())){
                        userLikedCards.add(tmpCard);
                    }
                }

                mUserCardsNumber.setText(String.format(getString(R.string.user_info_post_number_string), userCards.size()));
                mUserNumberOfReceivedLikes.setText(String.format(getString(R.string.user_info_likes_received_number_string), mNumberOfReceivedLikes));
                mLikedCardsAdapter.setCards(userLikedCards);
                mUserCardsAdapter.setCards(userCards);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
            };
            mCardsDatabaseReference.addValueEventListener(mDatabaseListener);
        }
    }

    private void detachDatabaseListener(){
        if (mDatabaseListener != null){
            mCardsDatabaseReference.removeEventListener(mDatabaseListener);
            mDatabaseListener = null;
        }
    }
}
