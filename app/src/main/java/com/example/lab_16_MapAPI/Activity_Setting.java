package com.example.lab_16_MapAPI;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;
import com.jaredrummler.android.colorpicker.ColorShape;

public class Activity_Setting extends AppCompatActivity implements ColorPickerDialogListener {

    boolean coastline;
    boolean river;
    boolean railroad;
    boolean road;

    CheckBox chCoastline;
    CheckBox chRiver;
    CheckBox chRailroad;
    CheckBox chRoad;

    Button btnColorCoastline;
    Button btnColorRiver;
    Button btnColorRailroad;
    Button btnColorRoad;

    EditText edAPI;
    EditText edLifetimeTile;

    Intent i;

    final int coastlineID = 0;
    final int riverID = 1;
    final int railroadID = 2;
    final int roadID = 3;

    int[] colors;

    public static DB database;

    int lifetimeTile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        chCoastline = findViewById(R.id.chCoastline);
        chRiver = findViewById(R.id.chRiver);
        chRailroad = findViewById(R.id.chRailroad);
        chRoad = findViewById(R.id.chRoad);

        btnColorCoastline = findViewById(R.id.btnColorCoastline);
        btnColorRiver = findViewById(R.id.btnColorRiver);
        btnColorRailroad = findViewById(R.id.btnColorRailroad);
        btnColorRoad = findViewById(R.id.btnColorRoad);

        edAPI = findViewById(R.id.edAPI);
        edLifetimeTile = findViewById(R.id.edLifetimeTile);

        edAPI.setText((String) database.GetSetting("http"), TextView.BufferType.EDITABLE);

        i = getIntent();

        chCoastline.setChecked(i.getBooleanExtra("coastline", coastline));
        chRiver.setChecked(i.getBooleanExtra("river", river));
        chRailroad.setChecked(i.getBooleanExtra("railroad", railroad));
        chRoad.setChecked(i.getBooleanExtra("road", road));
        lifetimeTile = i.getIntExtra("lifetimeTile", 1000);
        colors = i.getIntArrayExtra("colors");

        edLifetimeTile.setText(String.valueOf(lifetimeTile), TextView.BufferType.EDITABLE);

        btnColorCoastline.setBackgroundColor(colors[0]);
        btnColorRiver.setBackgroundColor(colors[1]);
        btnColorRailroad.setBackgroundColor(colors[2]);
        btnColorRoad.setBackgroundColor(colors[3]);
    }

    public void OnSave_Click(View v)
    {
        APIHelper testAPI = new APIHelper(this)
        {
            @Override
            public void on_ready(String res)
            {
                i.putExtra("coastline", chCoastline.isChecked());
                i.putExtra("river", chRiver.isChecked());
                i.putExtra("railroad", chRailroad.isChecked());
                i.putExtra("road", chRoad.isChecked());
                i.putExtra("colors", colors);
                i.putExtra("lifetimeTile", lifetimeTile);

                database.UpdateFieldInSetting("http", edAPI.getText().toString());
                database.UpdateFieldInSetting("colorCoastlines", colors[0]);
                database.UpdateFieldInSetting("colorRivers", colors[1]);
                database.UpdateFieldInSetting("colorRailroads", colors[2]);
                database.UpdateFieldInSetting("colorRoads", colors[3]);
                database.UpdateFieldInSetting("lifetimeTile", lifetimeTile);

                setResult(RESULT_OK, i);
                finish();
            }

            @Override
            public void on_fail() {
                Activity_Setting.this.runOnUiThread(() ->
                {
                    Toast.makeText(Activity_Setting.this, "Введенный API не отвечает", Toast.LENGTH_SHORT).show();
                });
            }
        };

        testAPI.send(edAPI.getText().toString() + "raster");
    }

    public void OnCancel_Click(View v)
    {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void createColorPickerDialog(int id) {
        ColorPickerDialog.newBuilder()
                .setDialogType(ColorPickerDialog.TYPE_PRESETS)
                .setAllowCustom(true)
                .setAllowPresets(true)
                .setColorShape(ColorShape.CIRCLE)
                .setDialogId(id)
                .show(this);
    }

    public void onColorButton_Click(View view)
    {
        switch (view.getId()) {
            case R.id.btnColorCoastline:
                createColorPickerDialog(coastlineID);
                break;
            case R.id.btnColorRiver:
                createColorPickerDialog(riverID);
                break;
            case R.id.btnColorRailroad:
                createColorPickerDialog(railroadID);
                break;
            case R.id.btnColorRoad:
                createColorPickerDialog(roadID);
                break;
        }
    }

    @Override
    public void onColorSelected(int dialogId, int color)
    {
        switch (dialogId)
        {
            case coastlineID:
                btnColorCoastline.setBackgroundColor(color);
                colors[0] = color;
                break;
            case riverID:
                btnColorRiver.setBackgroundColor(color);
                colors[1] = color;
                break;
            case railroadID:
                btnColorRailroad.setBackgroundColor(color);
                colors[2] = color;
                break;
            case roadID:
                btnColorRoad.setBackgroundColor(color);
                colors[3] = color;
                break;
        }
    }

    @Override
    public void onDialogDismissed(int dialogId)
    {
        Toast.makeText(this, R.string.strDismissColor, Toast.LENGTH_SHORT).show();
    }
}
