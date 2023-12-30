package com.example.amsems;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.Fragment;

import com.github.drjacky.imagepicker.ImagePicker;
import com.google.android.material.navigation.NavigationView;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment implements NavigationView.OnNavigationItemSelectedListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String studid;
    private TextView tvAcad, tvDep, tvSec, tvProg, tvYearlvl, tvStudId, tvName;
    private ImageView profilePic;
    private Uri uri;
    SharedPreferences sharedPreferences;
    Intent intent;
    Dialog dialog;
    private final ActivityResultLauncher<Intent> imagePickerLauncher=
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),(ActivityResult result)->{
                if(result.getResultCode()==RESULT_OK){
                    Uri uri=result.getData().getData();
                    byte[] imageByte = uriToByteArray(getActivity(), uri);
                    updateProfilePic(studid,imageByte);
                    Toast.makeText(getActivity(), "Successfully Change Profile", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }else if(result.getResultCode()==ImagePicker.RESULT_ERROR){
                    Toast.makeText(getActivity(), "No Image Selected", Toast.LENGTH_SHORT).show();
                }
            });

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        tvAcad = view.findViewById(R.id.tvAcad);
        tvDep = view.findViewById(R.id.tvDep);
        tvSec = view.findViewById(R.id.tvSec);
        tvProg = view.findViewById(R.id.tvProgram);
        tvYearlvl = view.findViewById(R.id.tvYearlvl);
        profilePic = view.findViewById(R.id.profilePic);
        tvStudId = view.findViewById(R.id.tvStudID);
        tvName = view.findViewById(R.id.tvName);
        ImageButton btnEditpass = view.findViewById(R.id.btnEditPass);

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomDialog();
            }
        });

        sharedPreferences = getActivity().getSharedPreferences("stud_info", MODE_PRIVATE);
        String studId = sharedPreferences.getString("studentID", null);
        studid = studId;
        getProfilePic(studId);

        intent = new Intent(getActivity(), EditPassActivity.class);
        //Bundle args = getArguments();
        //if (args != null) {
        //    String studId = args.getString("studId", null); // Replace -1 with a default value if needed
        //    studid = studId;
        //    getProfilePic(studId);
        //}

        btnEditpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(intent);
            }
        });
        return view;
    }
    public void showBottomDialog(){
        dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.profile_bottom_sheet);

        Button btnChangeProf = dialog.findViewById(R.id.btnChangePic);
        Button btnRemoveProf = dialog.findViewById(R.id.btnRemoveProf);
        btnChangeProf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                com.github.dhaval2404.imagepicker.ImagePicker.Companion.with(getActivity())
                        .crop()
                        .maxResultSize(512,512)
                        .createIntent((Function1)(new Function1(){
                            public Object invoke(Object var1){
                                this.invoke((Intent)var1);
                                return Unit.INSTANCE;
                            }

                            public final void invoke(@NotNull Intent it){
                                Intrinsics.checkNotNullParameter(it,"it");
                                imagePickerLauncher.launch(it);
                            }
                        }));
            }
        });
        btnRemoveProf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeProfilePic(studid, getActivity());
                profilePic.setImageResource(R.drawable.man);
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }
    public void updateProfilePic(String studentId, byte[] newProfilePic) {
        try {
            Connection connection = SQL_Connection.connectionClass();

            // Use a PreparedStatement to avoid SQL injection vulnerabilities
            String updateQuery = "UPDATE tbl_student_accounts SET Profile_pic = ? WHERE ID = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                // Set the new profile picture and student ID as parameters
                preparedStatement.setBytes(1, newProfilePic);
                preparedStatement.setString(2, studentId);

                // Execute the update query
                int rowsAffected = preparedStatement.executeUpdate();

                if (rowsAffected > 0) {
                    Toast.makeText(getActivity(), "Profile picture updated successfully.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Failed to update profile picture. Student not found.", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static byte[] uriToByteArray(Context context, Uri uri) {
        try {
            ContentResolver contentResolver = context.getContentResolver();

            // Open an input stream from the content resolver
            InputStream inputStream = contentResolver.openInputStream(uri);

            // Convert the input stream to a byte array
            byte[] bytes = getBytesFromInputStream(inputStream);

            // Close the input stream
            if (inputStream != null) {
                inputStream.close();
            }

            return bytes;
        } catch (IOException e) {
            Log.e("ImageUtils", "Error converting URI to byte array: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    private static byte[] getBytesFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;

        while ((len = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, len);
        }

        return byteArrayOutputStream.toByteArray();
    }
    public void removeProfilePic(String studentId, Context context) {
        try {
            Connection connection = SQL_Connection.connectionClass();

            // Use a PreparedStatement to avoid SQL injection vulnerabilities
            String updateQuery = "UPDATE tbl_student_accounts SET Profile_pic = ? WHERE ID = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                // Set the default profile picture from a drawable resource
                byte[] defaultImageData = getDefaultImageData(context);
                preparedStatement.setBytes(1, defaultImageData);
                preparedStatement.setString(2, studentId);

                // Execute the update query
                int rowsAffected = preparedStatement.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("Profile picture reset to default successfully.");
                } else {
                    System.out.println("Failed to reset profile picture. Student not found.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to convert a drawable to a byte array
    private byte[] getDefaultImageData(Context context) {
        Drawable defaultDrawable = ContextCompat.getDrawable(context, R.drawable.man);

        if (defaultDrawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) defaultDrawable).getBitmap();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            return outputStream.toByteArray();
        } else {
            // Handle other drawable types if needed
            return null;
        }
    }
    @SuppressLint("SetTextI18n")
    private void getProfilePic(String id) {
        byte[] imageData = null;
        String name, acad, dep, sec, prog, yearlvl, studid;
        Context context = getActivity();
        try {
            Connection connection = SQL_Connection.connectionClass();

            String query = "SELECT ID , Profile_pic,UPPER(Firstname + ' ' + Middlename + ' ' + Lastname) Name, Academic_Level_Description, d.Description Dep, se.Description Sec, p.Description Prog, y.Description Ylvl FROM tbl_student_accounts sa LEFT JOIN tbl_Departments d ON sa.Department = d.Department_ID LEFT JOIN tbl_academic_level al ON d.AcadLevel_ID = al.Academic_Level_ID LEFT JOIN tbl_Section se ON sa.Section = se.Section_ID LEFT JOIN tbl_program p ON sa.Program = p.Program_ID LEFT JOIN tbl_year_level y ON sa.Year_Level = y.Level_ID WHERE ID=?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, id);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        imageData = resultSet.getBytes("Profile_pic");
                        name = resultSet.getString("Name");
                        acad = resultSet.getString("Academic_Level_Description");
                        dep = resultSet.getString("Dep");
                        sec = resultSet.getString("Sec");
                        prog = resultSet.getString("Prog");
                        yearlvl = resultSet.getString("Ylvl");
                        studid = resultSet.getString("ID");

                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);

                        if (bitmap != null) {
                            // Get the circular background drawable
                            Drawable circularDrawable = ContextCompat.getDrawable(context, R.drawable.circle_profile);

                            // Combine the circular background with the profile picture
                            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                            roundedBitmapDrawable.setCircular(true);

                            // Set the combined drawable to the ImageButton
                            profilePic.setImageDrawable(roundedBitmapDrawable);
                        } else {
                            // Handle the case where the Bitmap is null (no image data)
                            profilePic.setImageResource(R.mipmap.ic_profile);
                        }

                        tvName.setText(name);
                        tvDep.setText("Department: "+dep);
                        tvAcad.setText("Academic Level: "+acad);
                        tvSec.setText("Section: "+sec);
                        tvYearlvl.setText("Year Level: "+yearlvl);
                        tvProg.setText("Program: "+prog);
                        tvStudId.setText("Student ID: "+studid);

                    }
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "SQL Exception: " + e.getMessage());
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return true;
    }
}