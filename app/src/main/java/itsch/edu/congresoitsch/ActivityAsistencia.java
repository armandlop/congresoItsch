package itsch.edu.congresoitsch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import conexion.DatosConexion;
import cz.msebera.android.httpclient.Header;

public class ActivityAsistencia extends AppCompatActivity {

    private final int IDRESULT=100;
    Button btnAsis;
    ProgressBar pBar;
    DatosConexion dc;
    int idAsis;

    SharedPreferences sp;
    String miIp;
    String miRuta;
    String servAsistencia;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asistencia);
        dc=new DatosConexion();
        btnAsis=findViewById(R.id.asis_btn);
        pBar=findViewById(R.id.asis_progres_bar);
        pBar.setVisibility(View.INVISIBLE);
        validarPreferencias();
        btnAsis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pBar.setVisibility(View.VISIBLE);
                capturarAsis();
            }
        });
    }

    private void validarPreferencias() {
        sp=getSharedPreferences("ITSCHCongreso", Context.MODE_PRIVATE);
        int result, currentVersionCode = BuildConfig.VERSION_CODE;
        int lastVersionCode = sp.getInt("FIRSTTIMERUN", -1);
        if (lastVersionCode == -1){
            alerta("Se cargaron los datos pre-configurados, asegurate de corregirlos");
            sp=getSharedPreferences("ITSCHCongreso", Context.MODE_PRIVATE);
            SharedPreferences.Editor edit=sp.edit();
            edit.putInt("FIRSTTIMERUN", 1);
            edit.putString("ip",dc.ipTemp);
            edit.putString("ruta",dc.rutaTemp);
            miIp=dc.ipTemp;
            miRuta=dc.rutaTemp;
            edit.commit();
        }else{
            sp=getSharedPreferences("ITSCHCongreso", Context.MODE_PRIVATE);
            miIp=sp.getString("ip","");
            miRuta=sp.getString("ruta","");
            Toast.makeText(this, miIp+" - "+miRuta, Toast.LENGTH_SHORT).show();
        }
        servAsistencia=miIp+miRuta+dc.urlRegistraAsis;
    }


    private void capturarAsis() {
        //Abrir la camara para capturar el qr
        Intent intent=new Intent(ActivityAsistencia.this,ScannerActivity.class);
        try {
            startActivityForResult(intent,IDRESULT);
        }catch (Exception e){
            Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
        }
        //esperar en el metodo onResult el codigo qr
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==IDRESULT) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    String dato=data.getStringExtra("datosResul");
                    registraAsistencia(dato);
                }
            }
        }
    }

    private void registraAsistencia(String  dato) {
        AsyncHttpClient cliente =new AsyncHttpClient();
        RequestParams rp=new RequestParams();
        rp.add("qr",dato);
        cliente.post(servAsistencia, rp, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    String resp = new String(responseBody);
                    Toast.makeText(ActivityAsistencia.this,
                            resp, Toast.LENGTH_LONG).show();
                    if(resp.equals("OK")){
                        alerta("Asistencia registrada");
                    }else{
                        alerta("No se pudo realizar la asistencia");
                    }
                }catch (Exception e){
                    alerta("No se pudo realizar la asistencia");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                alerta("Error al conectar con el servidor");
            }
        });
    }

    private void alerta(String mns) {
        AlertDialog.Builder alerta=new AlertDialog.Builder(this);
        alerta.setMessage(mns);
        alerta.setPositiveButton("OK",null);
        alerta.create();
        alerta.show();
        pBar.setVisibility(View.INVISIBLE);
    }
}
