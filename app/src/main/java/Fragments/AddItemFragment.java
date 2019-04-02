package Fragments;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.projectfirebase.soen341.root.Helper;
import com.projectfirebase.soen341.root.ItemDescription;
import com.projectfirebase.soen341.root.R;

import java.util.Objects;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;
import static com.projectfirebase.soen341.root.Helper.getCategoryArrayFromSnapshot;

public class AddItemFragment extends Fragment {
    View view;

    private DatabaseReference databaseItems;
    private DatabaseReference categoriesRef;
    private DatabaseReference subCategoryRef;
    private StorageReference mStorage;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private EditText itemNameET;
    private EditText itemPriceET;
    private EditText itemDescriptionET;

    private Spinner categorySpinner;
    private String[] categoryOptions;
    private int selectedCategory;
    private boolean justCreatedFlagC;


    private Spinner subCategorySpinner;
    private String[] subCategoryOptions;
    private int selectedSubCategory;
    private boolean justCreatedFlagSC;

    private boolean isItemPostable;
    private String imageURL;
    private final String uniqueItemID = UUID.randomUUID().toString();

    private ProgressDialog mProgressDialog;

    private static final int CAMERA_REQUEST_CODE = 111;
    private static final int GALLERY_INTENT = 2;

    public AddItemFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static AddItemFragment newInstance() {
        return new AddItemFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_add_listing_item, container, false);

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        databaseItems = rootRef.child("Items");
        categoriesRef = rootRef.child("Categories");

        mStorage = FirebaseStorage.getInstance().getReference();

        itemNameET = view.findViewById(R.id.item_name);
        itemPriceET = view.findViewById(R.id.item_price);
        itemDescriptionET = view.findViewById(R.id.item_description);

        LinearLayout selectImageCameraLayout = view.findViewById(R.id.select_image_photo);
        LinearLayout selectImageGalleryLayout = view.findViewById(R.id.select_image_gallery);

        Button postItemButton = view.findViewById(R.id.post_item_button);
        mProgressDialog = new ProgressDialog(getContext());

        categorySpinner = view.findViewById(R.id.add_category);
        subCategorySpinner = view.findViewById(R.id.add_subCategory);

        //DATABASE-DEPENDANT LISTENERS
        categoriesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                categoryOptions = Helper.getCategoryArrayFromSnapshot(dataSnapshot, "categoryOptions.");

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, new String[] {"Electronics","Vehicles","Clothing","Stationery"});

                categorySpinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        justCreatedFlagC = true;
        selectedCategory = -1;
        categorySpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int category, long id) {
                        if(justCreatedFlagC){
                            justCreatedFlagC = false;
                        } else {
                            selectedCategory = category - 1;

                            subCategoryRef = categoriesRef.child(Integer.toString(selectedCategory)).child("SubCategories");
                            subCategoryRef.addValueEventListener(new ValueEventListener() {
                                @SuppressLint("NewApi")
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(selectedCategory >= 0) {
                                        subCategoryOptions = getCategoryArrayFromSnapshot(Objects.requireNonNull(dataSnapshot), "subCategoryOptions.");

                                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, new String[] {"Nissan","Phone","Laptop","Suit","Trouser","Bus","Radio"});
                                        subCategorySpinner.setAdapter(adapter);
                                    } else {
                                        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(Objects.requireNonNull(getContext()), R.array.categoryOptions, android.R.layout.simple_spinner_item);
                                        subCategorySpinner.setAdapter(adapter);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });

                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                }
        );

        justCreatedFlagSC = true;
        selectedSubCategory = -1;
        subCategorySpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int subCategory, long l) {
                        if(justCreatedFlagSC){
                            justCreatedFlagSC = false;
                        } else {
                            selectedSubCategory = subCategory - 1;
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                }
        );

        //CLICK LISTENERS
        selectImageCameraLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Set camera click listener
            }
        });

        selectImageGalleryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_INTENT);
            }
        });

        postItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                isItemPostable = false;
                                dialog.cancel();
                            }
                        });

                if (Helper.isEmpty(itemNameET)) {
                    builder1.setMessage("Please enter a name for your item!");
                    AlertDialog alert11 = builder1.create();
                    alert11.show();

                } else if (Helper.isEmpty(itemPriceET)) {
                    builder1.setMessage("Please enter a price for your item!");
                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                } else if (Helper.isEmpty(itemDescriptionET)) {
                    builder1.setMessage("Please enter a description for your item!");
                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                } else if(selectedCategory < 0){
                    builder1.setMessage("Please choose a category!");
                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                } else if (selectedSubCategory < 0){
                    builder1.setMessage("Please choose a sub-category!");
                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                } else if(Helper.isNullOrEmpty(imageURL)){
                    builder1.setMessage("Please upload an image!");
                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                } else{
                    isItemPostable = true;
                }

                if (isItemPostable) {
                    String itemName = itemNameET.getText().toString();
                    double itemPrice = Double.parseDouble(itemPriceET.getText().toString());
                    itemPrice = Math.round(itemPrice * 100)/100.0;
                    String itemDescription = itemDescriptionET.getText().toString();

                    ItemDescription listingItem = new ItemDescription(uniqueItemID, user.getUid(), itemName, itemPrice, imageURL, itemDescription, selectedCategory, selectedSubCategory);

                    databaseItems.child(uniqueItemID).setValue(listingItem);

                    Toast.makeText(getContext(), "Item Posted", Toast.LENGTH_LONG).show();
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_INTENT && resultCode == RESULT_OK) {
            uploadImageToFirebase(data);

        }  //Camera logic

    }

    public void uploadImageToFirebase(Intent data) {
        mProgressDialog.setMessage("Uploading ...");
        mProgressDialog.show();


        Uri uri = data.getData();
        StorageReference filePath = mStorage.child("ItemPictures").child(uniqueItemID + "---" + uri.getLastPathSegment());

        filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getContext(), "Photo Upload Done", Toast.LENGTH_LONG).show();
                mProgressDialog.dismiss();
                imageURL =mStorage.getDownloadUrl().toString();
            }
        });
    }
}
