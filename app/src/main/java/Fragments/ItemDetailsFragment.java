package Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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
import com.projectfirebase.soen341.root.Helper;
import com.projectfirebase.soen341.root.ItemDescription;
import com.projectfirebase.soen341.root.R;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.projectfirebase.soen341.root.Helper.setImage;

public class ItemDetailsFragment extends Fragment {
    public static String itemIDToDisplay;
    private String UserId;
    public ItemDescription itemToDisplay;

    private DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference itemsRef = rootRef.child("Items");
    private DatabaseReference currentUserRef;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private TextView name_tv, price_tv, description_tv, seller_name_tv, seller_email_tv;
    private ImageView item_iv, seller_iv;
    private ToggleButton favorite_tb;

    String sellerName, sellerEmail, sellerPhotoURL;

    public ItemDetailsFragment() {
        // Required empty public constructor
    }

    public static ItemDetailsFragment newInstance() {
        return new ItemDetailsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_item_details, container, false);

        //Get the views in case we ever want to do anything with them
        name_tv = view.findViewById(R.id.item_name);
        price_tv = view.findViewById(R.id.item_price);
        description_tv = view.findViewById(R.id.item_description);
        item_iv = view.findViewById(R.id.item_photo);
        favorite_tb = view.findViewById(R.id.favorite);
        seller_name_tv = view.findViewById(R.id.seller_name);
        seller_email_tv = view.findViewById(R.id.seller_email);
        seller_iv = view.findViewById(R.id.seller_photo);

        if (user != null) {
            setFavoriteButtonListener();

        } else {
            favorite_tb.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        //make the id string final to make it accessible in the onDataChange listener
        this.populateItem();
    }

    public void populateItem() {
        DatabaseReference item = itemsRef.child(itemIDToDisplay);
        itemToDisplay = new ItemDescription();

        item.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Object> itemsInDB = (HashMap<String, Object>) dataSnapshot.getValue();

                //get the data for the item to display
                UserId = (String) itemsInDB.get("OwnerID");
                String name = (String) itemsInDB.get("Name");
                String description = (String) itemsInDB.get("Description");
                String url = (String) itemsInDB.get("ImageURL");
                Double price = ((Number) itemsInDB.get("Price")).doubleValue();
                int category = ((Number) itemsInDB.get("Category")).intValue();
                int subCategory = ((Number) itemsInDB.get("SubCategory")).intValue();

                //set it
                itemToDisplay = new ItemDescription(itemIDToDisplay, UserId, name, price, url, description, category, subCategory);

                setDisplayViews();
                setSellerDetails();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void setDisplayViews() {
        name_tv.setText(this.itemToDisplay.getName());

        Locale kenyan = new Locale("sw", "KE");
        NumberFormat kenyanFormat  = NumberFormat.getCurrencyInstance(kenyan);
        String price = kenyanFormat.format(this.itemToDisplay.getPrice());
        price_tv.setText(price);

        if (Helper.isNullOrEmpty(this.itemToDisplay.getDescription())) {
            String NO_DESC = "No additional information available";
            description_tv.setText(NO_DESC);
        } else {
            description_tv.setText(this.itemToDisplay.getDescription());
        }

        setImage(getActivity(), itemToDisplay.getImageURL(), item_iv);
    }

    public static void setItemIDToDisplay(String id) {
        ItemDetailsFragment.itemIDToDisplay = id;
    }

    private void setSellerDetails() {
        DatabaseReference sellerRef = rootRef.child("Users").child(UserId);

        sellerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                sellerPhotoURL = dataSnapshot.child("ImageURL").getValue(String.class);
                if (!Helper.isNullOrEmpty(sellerPhotoURL)) {
                    setImage(getActivity(), dataSnapshot.child("ImageURL").getValue(String.class), seller_iv);
                }
                sellerName = dataSnapshot.child("firstName").getValue(String.class) + " " + dataSnapshot.child("lastName").getValue(String.class);
                sellerEmail = dataSnapshot.child("email").getValue(String.class);
                seller_name_tv.setText(sellerName);
                seller_email_tv.setText(sellerEmail);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


    private void setFavoriteButtonListener() {
        currentUserRef = rootRef.child("Users").child(user.getUid());
        DatabaseReference favRef = currentUserRef.child("Favorites");

        favRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(itemIDToDisplay))
                    setFavToggle(true);
                else
                    setFavToggle(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void setFavToggle(final boolean isFavorite) {
        rootRef = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            currentUserRef = rootRef.child("Users").child(user.getUid());

            if (isFavorite) {
                favorite_tb.setChecked(true);
                favorite_tb.setBackgroundResource(R.drawable.ic_star_yellow_24dp);
            } else {
                favorite_tb.setChecked(false);
                favorite_tb.setBackgroundResource(R.drawable.ic_star_border_yellow_24dp);
            }

            if (user != null) {
                favorite_tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            //usersRef.child(user.getUid()).child("Favorites").setValue(favString + ";" + holder.id.toString());
                            currentUserRef.child("Favorites").child(itemIDToDisplay).setValue(true);
                            favorite_tb.setBackgroundResource(R.drawable.ic_star_yellow_24dp);
                        } else {
                        /*newFavList.remove(newFavList.indexOf(holder.id.toString()));
                        usersRef.child(user.getUid()).child("Favorites").setValue(android.text.TextUtils.join(";", newFavList));*/
                            currentUserRef.child("Favorites").child(itemIDToDisplay).removeValue();

                            favorite_tb.setBackgroundResource(R.drawable.ic_star_border_yellow_24dp);
                        }
                    }
                });
            }
        }
    }
}
