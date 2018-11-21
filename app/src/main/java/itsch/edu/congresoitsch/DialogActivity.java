package itsch.edu.congresoitsch;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by armandolopez on 20/11/18.
 */

public class DialogActivity extends DialogFragment {

    EditText txServer, txRuta;
    Button btnOk, btncancel;

    public interface NuevoListener{
        void finalizaDialog(String server, String directorio);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View vista=inflater.inflate(R.layout.activity_dialog,container);
        txServer=(EditText)vista.findViewById(R.id.tx_server);
        txRuta=(EditText)vista.findViewById(R.id.tx_ruta);
        btnOk=(Button)vista.findViewById(R.id.btn_ok);
        btncancel=(Button)vista.findViewById(R.id.btn_cancel);
        txServer.requestFocus();
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enviar();
            }
        });
        btncancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelar();
            }
        });

        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return vista;
    }

    private boolean cancelar() {
        NuevoListener actividad=(NuevoListener)getActivity();
        actividad.finalizaDialog("xx", "xx");
        this.dismiss();
        return true;
    }

    private boolean enviar() {
        NuevoListener actividad=(NuevoListener)getActivity();
        if(txServer.getText().toString().isEmpty() || txRuta.getText().toString().isEmpty()){
            actividad.finalizaDialog("xx","xx");
        }else{
            actividad.finalizaDialog(txServer.getText().toString(), txRuta.getText().toString());
        }


        this.dismiss();
        return true;
    }
}
