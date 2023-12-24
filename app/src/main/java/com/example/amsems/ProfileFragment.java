package com.example.amsems;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private TextView tvAcad, tvDep, tvSec, tvProg, tvYearlvl, tvStudId, tvName;
    private ImageView profilePic;

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


        Bundle args = getArguments();
        if (args != null) {
            String studId = args.getString("studId", null); // Replace -1 with a default value if needed

            getProfilePic(studId);
        }

        btnEditpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle args = getArguments();
                if (args != null) {
                    String studId = args.getString("studId", null); // Replace -1 with a default value if needed

                    Intent intent = new Intent(getActivity(), EditPassActivity.class);
                    intent.putExtra("STUDENT_ID", studId);
                    startActivity(intent);
                }
            }
        });
        return view;
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
}