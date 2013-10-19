package com.lisa.lisa;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class HelpActivity extends Activity {
    
    TextView txtHelp;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        txtHelp = (TextView) findViewById(R.id.txtHelp);
        txtHelp.setText("Informations are on http://www.lisa-project.net/");
    }
}
