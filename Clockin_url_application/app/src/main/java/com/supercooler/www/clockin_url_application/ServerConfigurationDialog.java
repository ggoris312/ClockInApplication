package com.supercooler.www.clockin_url_application;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by gio on 5/1/2017.
 */

public class ServerConfigurationDialog extends DialogFragment {

    public static ServerConfigurationDialog newInstance() {
        ServerConfigurationDialog f = new ServerConfigurationDialog();
        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_server_configuration, null);

        final EditText mIP = (EditText) v.findViewById(R.id.ip_edit_text);
        final EditText mPort = (EditText) v.findViewById(R.id.port_edit_text);
        Button mIPButton = (Button) v.findViewById(R.id.ip_button);
        mIPButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIP.getText().toString().isEmpty()){
                    SaveSharedPreference.storeStringPreference(getActivity(),
                            SaveSharedPreference.IP_STRING, mIP.getText().toString());
                    Toast.makeText(getActivity(), "IP Saved", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button mPortButtom = (Button) v.findViewById(R.id.port_button);
        mPortButtom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mPort.getText().toString().isEmpty()){
                    SaveSharedPreference.storeStringPreference(getActivity(),
                            SaveSharedPreference.PORT_STRING, mPort.getText().toString());
                    Toast.makeText(getActivity(), "Port Saved", Toast.LENGTH_SHORT).show();
                }
            }
        });


        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.server_configuration_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(),"Settings Saved", Toast.LENGTH_SHORT).show();
                    }
                })
                .create();
    }
}
