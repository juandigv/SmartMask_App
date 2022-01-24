package com.covid.smartmask.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.covid.smartmask.R;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DialogActivities extends AppCompatDialogFragment {
    private TimePicker time_start;
    private TimePicker time_end;
    private DialogActivitiesListener listener;
    private int startHour;
    private int startMinute;
    private int endHour;
    private int endMinute;

    public DialogActivities(int startHour, int startMinute, int endHour, int endMinute) {
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.endHour = endHour;
        this.endMinute = endMinute;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (DialogActivitiesListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString());
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_activities, null);
        builder.setView(view)
                .setTitle("Actividades")
                .setCancelable(false)
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.saveActivityHours(time_start,time_end);
                    }
                });

        time_start = view.findViewById(R.id.time_start);
        time_end = view.findViewById(R.id.time_end);
        time_start.setIs24HourView(true);
        time_end.setIs24HourView(true);
        if(startHour != -1){
            time_start.setHour(startHour);
        }
        if(startMinute != -1){
            time_start.setMinute(startMinute);
        }
        if(endHour != -1){
            time_end.setHour(endHour);
        }
        if(endMinute != -1){
            time_end.setMinute(endMinute);
        }

        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    if (i == KeyEvent.KEYCODE_BACK) {
                        return true;
                    }
                }
                return false;
            }
        });
        return builder.create();
    }

    public interface DialogActivitiesListener{
        void saveActivityHours(TimePicker time_start, TimePicker time_end);
    }


}
