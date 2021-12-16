package com.erniwo.timetableconstruct.student;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.erniwo.timetableconstruct.Message;
import com.erniwo.timetableconstruct.R;
import com.erniwo.timetableconstruct.admin.AdminEditClassTimeTableActivity;
import com.erniwo.timetableconstruct.admin.AdminManageClassTimetableActivity;
import com.erniwo.timetableconstruct.admin.AdminManageListOfClassesActivity;
import com.erniwo.timetableconstruct.admin.AdminManageListOfTeachersActivity;
import com.erniwo.timetableconstruct.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StudentTimetableActivity extends AppCompatActivity implements View.OnClickListener {



    private String currentStudentName;
    private String currentStudentID;
    private String currentClassNameOfStudent;
    private String currentClassIdOfStudent;
    private TextView nameOfStudent;
    private FrameLayout frameLayoutLessonSection;
    private TextView[] mClassNumHeaders = null;
    private LinearLayout headerClassNumLl;
    private ImageView logout;
    ArrayList<String> lessonKeyList = new ArrayList<String>();

    private String TAG = "StudentTimetableActivityLog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_timetable);

        Log.d(TAG, "onCreate");

        // init elements
        headerClassNumLl = findViewById(R.id.ll_header_class_num);
        nameOfStudent = findViewById(R.id.name_of_student_header);
        frameLayoutLessonSection = (FrameLayout) findViewById(R.id.frame_layout_lesson_section);
        logout = findViewById(R.id.logout_icon);

        // onCLick actions
        logout.setOnClickListener(this);

        loadStudentName();
        loadClassOfCurrentStudent();

    } // onCreate

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG,"onStart");
        try{
            pullExistingLessonsFromDatabaseAndInitLessonsOnTimetable();
        }catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

    }

    private void pullExistingLessonsFromDatabaseAndInitLessonsOnTimetable() {

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Context context = getApplicationContext();

        TableLayout tableLayout = new TableLayout(context);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        tableLayout.setLayoutParams(lp);
        tableLayout.setStretchAllColumns(true);

        TableLayout.LayoutParams rowLp = new TableLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1.0f);
        TableRow.LayoutParams cellLp = new TableRow.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1.0f);
//                        rowLp.bottomMargin = 2;
        rowLp.weight = 1;
        cellLp.topMargin = 4;
        cellLp.leftMargin = 6;
        cellLp.weight = 1;

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (int r = 0; r < 9; ++r) {

                    TableRow row = new TableRow(context);
                    row.setBaselineAligned(false);

                    for (int c = 0; c < 7; ++c) {

                        Button btn = (Button) layoutInflater.inflate(R.layout.item_lesson_card, null);
                        btn.setPadding(0,0,0,0);
                        btn.setIncludeFontPadding(false);
                        String currentLessonKey = String.valueOf(r + 1) + String.valueOf(c + 1);

                        String lessonInfoToBeDisplayedOnLessonCard;
                        Map<String,String> lessonInfoToBeDisplayedOnLessonCardMap = new HashMap<>();

                        // iterate all classes' info saved in firebase database to get the current class's timetable
                        for (DataSnapshot classIdChild: snapshot.child("Classes").getChildren()) {
                            try {
                                if (getCurrentClassIdOfStudent().equals(classIdChild.getKey())) {
                                    String className = classIdChild.child("name").getValue().toString().trim();
                                    Log.d(TAG, "Current teacher Name: " + className);

                                    for (DataSnapshot timetableChild : classIdChild.child("timetable").getChildren()) {

                                        String subject = timetableChild.child("subject").getValue().toString().trim();
                                        String location = timetableChild.child("location").getValue().toString().trim();
                                        String teacherId = timetableChild.child("idnumber").getValue().toString().trim();
                                        String lessonKey = timetableChild.getKey().trim();
                                        Log.d(TAG, "Current lesson key: " + lessonKey);
                                        lessonKeyList.add(lessonKey);

                                        for (DataSnapshot teacherIdChild : snapshot.child("Teachers").getChildren()) {
                                            Log.d(TAG, "teacher id: " + teacherIdChild.getKey());
                                            if (teacherId.equals(teacherIdChild.getKey())) {
                                                String teacherName = teacherIdChild.child("name").getValue().toString().trim();
                                                lessonInfoToBeDisplayedOnLessonCard = subject + "\n\n" + location + "\n\n" + teacherName;
                                                lessonInfoToBeDisplayedOnLessonCardMap.put(lessonKey, lessonInfoToBeDisplayedOnLessonCard);
                                            }
                                        }
                                    } // timetableChild
                                } // if
                            }catch (Exception e){
                                Log.e(TAG, Log.getStackTraceString(e));
                            }
                        } // classIdChild

                        if (lessonInfoToBeDisplayedOnLessonCardMap.containsKey(currentLessonKey)) {
                            String lessonInfo = lessonInfoToBeDisplayedOnLessonCardMap.get(currentLessonKey);
                            Log.d(TAG, "textOnLessonButton"+ lessonInfo);
                            btn.setText(lessonInfo);
                            Log.d(TAG, "Button text set");
                            btn.setVisibility(VISIBLE);
                            Log.d(TAG, "Button set to VISIBLE");
                            if(row.getParent() != null) {
                                ((ViewGroup)row.getParent()).removeView(row);
                            }
                            row.addView(btn, cellLp);
                            Log.d(TAG, "Added cell to row, visible");

                        } else {

                            btn.setText(currentLessonKey);
                            btn.setVisibility(INVISIBLE);
                            Log.d(TAG, "Button set to INVISIBLE");
                            if(row.getParent() != null) {
                                ((ViewGroup)row.getParent()).removeView(row);
                            }
                            row.addView(btn, cellLp);
                            Log.d(TAG, "Added cell to row");

                        }

                    }
                    tableLayout.addView(row, rowLp);
                    Log.d(TAG, "Added row to table");
                }
                frameLayoutLessonSection.addView(tableLayout);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            } // onDataChange

        }); // currentClassTtbRef.addValueEventListener

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG,"onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,"onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.logout_icon:
                logout();
                break;
        }
    }

    public void logout() {
        FirebaseAuth.getInstance().signOut();
        Message.showMessage(getApplicationContext(),"You have logged out!");
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
    }

    private void loadStudentName() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userID = user.getUid();
        ref.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot child: snapshot.getChildren()) {
                    Log.d(TAG, "child: " + child);
                    if(userID.equals(child.getKey())) {
                        setCurrentStudentName(child.child("name").getValue().toString().trim());
                        setCurrentStudentID(child.child("idnumber").getValue().toString().trim());
                        nameOfStudent.setText(getCurrentStudentName() + "'s Timetable");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void loadClassOfCurrentStudent() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Log.d(TAG, "ref value: "+ ref.child("Classes").getKey());
        ref.child("Classes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot classIdChild: snapshot.getChildren()) {

                    Log.d(TAG, "classIdChild value: "+ classIdChild.getValue().toString());
                    try {
                        Log.d(TAG, "classIdChildStudent value: "+ classIdChild.child("student").getValue().toString());

                        for(DataSnapshot classInfoChild: classIdChild.child("student").getChildren()) {
                            Log.d(TAG, "classInfoChild value: "+ classInfoChild.getKey());
                            if (getCurrentStudentID().equals(classInfoChild.getKey())) {
                                setCurrentClassIdOfStudent(classIdChild.getKey());
                                setCurrentClassNameOfStudent(classIdChild.child("name").getValue().toString().trim());
                            }
                        }
                    }catch (Exception e){
                        Log.e(TAG, Log.getStackTraceString(e));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    public String getCurrentStudentName() {
        return currentStudentName;
    }

    public String getCurrentStudentID() {
        return currentStudentID;
    }

    public void setCurrentStudentName(String currentStudentName) {
        this.currentStudentName = currentStudentName;
    }

    public void setCurrentStudentID(String currentStudentID) {
        this.currentStudentID = currentStudentID;
    }

    public void setCurrentClassNameOfStudent(String currentClassNameOfStudent) {
        this.currentClassNameOfStudent = currentClassNameOfStudent;
    }

    public void setCurrentClassIdOfStudent(String currentClassIdOfStudent) {
        this.currentClassIdOfStudent = currentClassIdOfStudent;
    }

    public String getCurrentClassNameOfStudent() {
        return currentClassNameOfStudent;
    }

    public String getCurrentClassIdOfStudent() {
        return currentClassIdOfStudent;
    }
}

