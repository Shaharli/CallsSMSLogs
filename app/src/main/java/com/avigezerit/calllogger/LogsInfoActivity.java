package com.avigezerit.calllogger;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class LogsInfoActivity extends AppCompatActivity {

    LogsFragment logsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs_info);

        //fragment add : logs list
        logsFragment = LogsFragment.newInstance(this);
        getFragmentManager().beginTransaction().add(R.id.ContainerFlowLL, logsFragment).commit();

        Button saveToFileBtn = (Button) findViewById(R.id.saveBtn);
        saveToFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                logsFragment.savingBtnClicked();

            }
        });

        Button changeContactBtn = (Button) findViewById(R.id.changeContactBtn);
        changeContactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent intent = new Intent(LogsInfoActivity.this, WelcomeActivity.class);
                intent.putExtra("choose_contact_again", "true");
                startActivity(intent);

            }
        });



    }

}
