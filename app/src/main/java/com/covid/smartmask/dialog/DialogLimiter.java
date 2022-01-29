package com.covid.smartmask.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.covid.smartmask.R;

public class DialogLimiter extends AppCompatDialogFragment {
    private EditText edit_limitCO2;
    private EditText edit_limitTVOC;
    private DialogLimiterListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (DialogLimiterListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString());
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_limiter, null);
        builder.setView(view)
                .setTitle("Limites Aceptables [CO2, TVOC]")
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int limit_co2;
                        int limit_tvoc;
                        try{
                            limit_co2 = Integer.parseInt(edit_limitCO2.getText().toString());
                        }catch (NumberFormatException e){
                            limit_co2 = 0;
                        }
                        try{
                            limit_tvoc = Integer.parseInt(edit_limitTVOC.getText().toString());
                        }catch (NumberFormatException e){
                            limit_tvoc = 0;
                        }
                        listener.saveLimits(limit_co2, limit_tvoc);
                    }
                });

        edit_limitCO2 = view.findViewById(R.id.edit_limitCO2);
        edit_limitTVOC = view.findViewById(R.id.edit_limitTVOC);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        edit_limitCO2.setText(settings.getInt("limit_CO2", 6500) + "");
        edit_limitTVOC.setText(settings.getInt("limit_TVOC", 800) + "");
        Log.d("SharedPreferences","CO2 :" + settings.getInt("limit_CO2", 6500) + "");
        Log.d("SharedPreferences","TVOC :" + settings.getInt("limit_TVOC", 800) + "");
        return builder.create();
    }

    public interface DialogLimiterListener {
        void saveLimits(int limit_co2, int limit_tvoc);
    }
}
