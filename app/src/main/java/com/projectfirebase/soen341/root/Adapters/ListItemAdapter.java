package com.projectfirebase.soen341.root.Adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.projectfirebase.soen341.root.Listing;
import com.projectfirebase.soen341.root.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import static com.projectfirebase.soen341.root.Helper.setImage;

public class ListItemAdapter extends RecyclerView.Adapter<ListItemAdapter.ViewHolder> {
    private List<Listing> itemList;

    private DatabaseReference rootRef;
    private DatabaseReference currentUserRef;
    private DatabaseReference favRef;

    private FirebaseUser user;

    // View holder is what holds the views
    public class ViewHolder extends RecyclerView.ViewHolder {
        public View view;
        public TextView name_tv;
        public TextView price_tv;
        public ImageView image_iv;
        public ToggleButton fav;
        public String id;

        public ViewHolder(View v) {
            super(v);
            name_tv = (TextView) v.findViewById(R.id.list_item_name);
            price_tv = (TextView) v.findViewById(R.id.list_item_price);
            image_iv = (ImageView) v.findViewById(R.id.list_item_photo);
            fav = (ToggleButton) v.findViewById(R.id.list_item_fav);
            view = v;
        }
    }

    public ListItemAdapter(List<Listing> itemList) {
        this.itemList = itemList;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_row, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        rootRef = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();

        Listing listItem = itemList.get(position);

        holder.id = listItem.getID();
        holder.name_tv.setText(listItem.getName());

        Locale kenyan = new Locale("sw", "KE");
        NumberFormat kenyanFormat = NumberFormat.getCurrencyInstance(kenyan);
        String price = kenyanFormat.format(listItem.getPrice());
        holder.price_tv.setText(price);

        holder.view.setTag(listItem.getID());

        String imgUrl = listItem.getImageURL();
        setImage(holder.view, imgUrl, holder.image_iv);

        if (user != null) {
            currentUserRef = rootRef.child("Users").child(user.getUid());
            favRef = currentUserRef.child("Favorites");

            final ViewHolder currentHolder = holder;
            favRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(currentHolder.id))
                        setFavToggle(currentHolder, true);
                    else
                        setFavToggle(currentHolder, false);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        } else {
            holder.fav.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public Listing getListItem(int pos) {
        return itemList.get(pos);
    }

    public void setFavToggle(final ViewHolder holder, final boolean isFavorite) {
        rootRef = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            currentUserRef = rootRef.child("Users").child(user.getUid());

            if (isFavorite) {
                holder.fav.setChecked(true);
                holder.fav.setBackgroundResource(R.drawable.ic_star_yellow_24dp);
            } else {
                holder.fav.setChecked(false);
                holder.fav.setBackgroundResource(R.drawable.ic_star_border_yellow_24dp);
            }

            if (user != null) {
                holder.fav.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            //usersRef.child(user.getUid()).child("Favorites").setValue(favString + ";" + holder.id.toString());
                            currentUserRef.child("Favorites").child(holder.id).setValue(true);
                            holder.fav.setBackgroundResource(R.drawable.ic_star_yellow_24dp);
                        } else {
                        /*newFavList.remove(newFavList.indexOf(holder.id.toString()));
                        usersRef.child(user.getUid()).child("Favorites").setValue(android.text.TextUtils.join(";", newFavList));*/
                            currentUserRef.child("Favorites").child(holder.id).removeValue();

                            holder.fav.setBackgroundResource(R.drawable.ic_star_border_yellow_24dp);
                        }
                    }
                });
            }
        }
    }
}
