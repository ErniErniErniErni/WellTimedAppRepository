package com.erniwo.timetableconstruct.admin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.erniwo.timetableconstruct.Message;
import com.erniwo.timetableconstruct.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AdminAddNewClassActivity extends AppCompatActivity implements View.OnClickListener {

    AutoCompleteTextView selectGradeTextView;
    EditText classNumEditText;
    TextView selectStudentsArrowDown;
    boolean[] selectedStudent;
    ArrayList<Integer> studentList = new ArrayList<>(); // off
    ArrayList<String> studentListList = new ArrayList<>();
    static String[] studentArray = new String[1000];
    private Button addNewClassButton;

    private String TAG = "AdminAddNewClassActivityLog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_new_class);

        Log.d(TAG, "onCreate");

        // init elements
        selectGradeTextView = (AutoCompleteTextView) findViewById(R.id.select_edit_text_grade_num);
        classNumEditText = findViewById(R.id.select_edit_text_class_num);
        selectStudentsArrowDown = findViewById(R.id.select_student_num);
        addNewClassButton = findViewById(R.id.add_new_class_button);

        // onClick action
        selectStudentsArrowDown.setOnClickListener(this);
        addNewClassButton.setOnClickListener(this);

        // adapt array to select Grade
        String[] listOfGradeNum = getResources().getStringArray(R.array.select_grades);
        ArrayAdapter arrayAdapterGrade = new ArrayAdapter(getApplicationContext(),R.layout.dropdown_item, listOfGradeNum);
        selectGradeTextView.setAdapter(arrayAdapterGrade);

        selectedStudent = new boolean[studentArray.length];

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_new_class_button:
                addNewClass();
                break;
            case R.id.select_student_num:
                multipleCheckStudents();
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG,"onStart");
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
    public void addNewClass() {

        String txt_GradeNum = selectGradeTextView.getText().toString().trim();
        String txt_ClassNum = classNumEditText.getText().toString().trim();
        String className = txt_GradeNum + " Class " + txt_ClassNum;

        if(txt_GradeNum.isEmpty()) {
            selectGradeTextView.setError("Please select a Grade!");
            selectGradeTextView.requestFocus();
            return;
        }

        if(txt_ClassNum.isEmpty()) {
             classNumEditText.setError("Please enter Class Number!");
             classNumEditText.requestFocus();
             return;
        }

        String gradeNumLastTwoChar = txt_GradeNum.length() > 2 ? txt_GradeNum.substring(txt_GradeNum.length() - 2) : txt_GradeNum;
        String gradeNumInTwoChars;
        if(gradeNumLastTwoChar.trim().length() == 2) {
            gradeNumInTwoChars = gradeNumLastTwoChar.trim();
        }else {
            gradeNumInTwoChars = "0" + gradeNumLastTwoChar.trim();
        }

        String classNumLastTwoChar = txt_ClassNum.length() > 2 ? txt_ClassNum.substring(txt_ClassNum.length() - 2) : txt_ClassNum;
        String classNumInTwoChars;
        if(classNumLastTwoChar.trim().length() == 2) {
            classNumInTwoChars = classNumLastTwoChar.trim();
        }else {
            classNumInTwoChars = "0" + classNumLastTwoChar.trim();
        }

        String txt_ClassID = "C" + gradeNumInTwoChars + classNumInTwoChars;
        Log.i(TAG, txt_ClassID);

        FirebaseDatabase.getInstance().getReference().child("Classes").child(txt_ClassID)
                .child("name").setValue(className);

        String stringListOfStudentID = selectStudentsArrowDown.getText().toString();
        List<String> stringListOfStudentIDList = Arrays.asList(stringListOfStudentID.split(","));

        // save to firebase realtime database
        for (String id: stringListOfStudentIDList ){
            Log.i(TAG, id);
            FirebaseDatabase.getInstance().getReference().child("Classes").child(txt_ClassID)
                    .child("student").child(id).setValue("");
        }

        Message.showMessage(getApplicationContext(),"Added new class successfully!");
        Intent intent = new Intent(AdminAddNewClassActivity.this, AdminManageListOfClassesActivity.class);
        startActivity(intent);
    }


    public void multipleCheckStudents() {
        // show dialog to check multiple students to the text bar
        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.activity_listview, studentListList);
        DatabaseReference studentInfoRef = FirebaseDatabase.getInstance().getReference()
                .child("Students");
        studentInfoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot child : snapshot.getChildren()) {
                    try {
                        studentListList.clear();
                        String id = child.getKey().trim();
                        studentListList.add(id);
                    }catch (NullPointerException e) {
                        Log.e(TAG, "idnumber Null");
                    }
                }
                adapter.notifyDataSetChanged();

                studentArray = new String[studentListList.size()];

                for (int q = 0; q < studentListList.size(); q++) {
                    studentArray[q] = studentListList.get(q);
                }
                if (studentArray == null) {
                    Log.d(TAG, "Array Null!");
                    return;
                }

                int studentArrayLength = studentArray.length;
                selectedStudent = new boolean[studentArrayLength];

                //Init alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        AdminAddNewClassActivity.this
                );
                //Set title
                builder.setTitle("Please Select from the Following Registered Student IDs:");
                //Set dialog non cancelable
                builder.setCancelable(false);

                builder.setMultiChoiceItems(studentArray, selectedStudent, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        //Check condition
                        if (isChecked) {
                            //When checkbox selected
                            //Add position in student list
                            studentList.add(which);
                            //Sort student list
                            Collections.sort(studentList);
                        } else {
                            //When checkbox unselected
                            //Remove position from list
                            studentList.remove(which);
                        }
                    }
                });
                builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Init string builder
                        StringBuilder stringBuilder = new StringBuilder();
                        //Use for loop
                        for (int j = 0; j < studentList.size(); j++) {
                            //Concat array value
                            stringBuilder.append(studentArray[studentList.get(j)]);
                            //Check condition
                            if (j != studentList.size() - 1) {
                                //When j value not equal to student list size -1
                                //Add comma
                                stringBuilder.append(",");
                            }
                        }
                        //Set text on text view
                        selectStudentsArrowDown.setText(stringBuilder.toString());
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Dismiss Dialog
                        dialog.dismiss();
                    }
                });

                builder.setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Use for loop
                        for (int i = 0; i < selectedStudent.length; i++) {
                            //Remove all selected
                            selectedStudent[i] = false;
                            //Clear student list
                            studentList.clear();
                            //Clear text view value
                            selectStudentsArrowDown.setText("");
                        }
                    }
                });
                //Show dialog
                builder.show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
    }

}// class