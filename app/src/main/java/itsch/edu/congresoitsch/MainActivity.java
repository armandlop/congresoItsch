package itsch.edu.congresoitsch;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;

import java.util.StringTokenizer;

import conexion.DatosConexion;
import cz.msebera.android.httpclient.Header;
import modelos.Participante;

public class MainActivity extends AppCompatActivity implements DialogActivity.NuevoListener {


    private final int IDRESULT=100;
    Button btnQr, btnGaf;
    TextView tvClave, tvNombre,tvApellido;
    ProgressBar pBar;
    ImageView img;
    DatosConexion dc;
    SharedPreferences sp;
    String miIp;
    String miRuta;
    String servRegistro, servGaffete, servAsistencia;
    String clavePar;

    /*
    1.- Leer codigo Qr del comprobante
    2.- Obtener el numero de recibo, clave y hash, recibidos en el qr
    3.- Enviar al Server la peticion para buscar el registro,
    4.-    // Server-> Con la clave generar un hash
    5.-     Server-> Buscar el registro donde coincida numero de recibo, clave y hash
    6.-     Server-> Si el participante esta registrado, retorna: clave-nombre-apellidos
    7.- Recibe clave-nombre-apellidos
    8.- Leer el codigo Qr del gaffete
    9.- Integrar los parametros para enviar al server el Qr, clave.
    10      Server-> Recibe los parametros, y valida, si ya existe un registro con la misma clave, debera retornar que el alumno ya
            realizo su registro
     */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.mipmap.ic_icono);

        dc=new DatosConexion();


        validarPreferencias();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mens="IP= "+miIp+"\nRegistro: "+miRuta;
                AlertDialog.Builder alerta=new AlertDialog.Builder(MainActivity.this);
                alerta.setTitle("Datos de Conexion");
                alerta.setMessage(mens);
                alerta.setPositiveButton("Ok",null);
                alerta.setNegativeButton("Modificar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        modificarSharedPreferences();
                    }
                });
                alerta.create();
                alerta.show();
               // Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
            }
        });

        cargarControles();
        cargarEventos();
        pBar.setVisibility(View.INVISIBLE);
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
        servRegistro=miIp+miRuta+dc.urlQueryRegistro;
        servGaffete=miIp+miRuta+dc.urlSincronizaGaffete;
        servAsistencia=miIp+miRuta+dc.urlRegistraAsis;
    }

    private void alerta(String mns) {
        AlertDialog.Builder aler=new AlertDialog.Builder(this);
        aler.setMessage(mns);
        aler.setNeutralButton("Ok", null);
        aler.create();
        aler.show();
    }

    private void modificarSharedPreferences() {
        //Llamar al Dialogo personal
        DialogActivity da=new DialogActivity();
        da.show(getFragmentManager(),"miDialog");

        android.app.Fragment frag=getFragmentManager().findFragmentByTag("miDialog");
        if(frag!=null){
            getFragmentManager().beginTransaction().remove(frag).commit();
        }

        /*
        DialogoPersonal dp=new DialogoPersonal();
        dp.show(getFragmentManager(),"miDialog");
        android.app.Fragment frag=getFragmentManager().findFragmentByTag("miDialog");
        if (frag!=null){
            getFragmentManager().beginTransaction().remove(frag).commit();
        }
         */
    }

    //Creando metodos
    private void cargarEventos() {
        btnQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrir();
            }
        });
        btnGaf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrir();
                pBar.setVisibility(View.VISIBLE);
            }
        });
    }

    private void sincroniza(String qr) {
        // # 9.- Integrar los parametros para enviar al server el Qr, clave.
        if(qr!=null && clavePar!=null){
            AsyncHttpClient cliente=new AsyncHttpClient();
            RequestParams rp=new RequestParams();
            rp.add("clave",clavePar);
            rp.add("qr",qr);
            cliente.post(servGaffete,rp, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    //Se guardaron los datos
                    //Cargar imagen de exito
                    String resp=new String(responseBody);
                    Toast.makeText(MainActivity.this, resp, Toast.LENGTH_SHORT).show();
                    if(resp.equals("OK")){
                        limpiar("Registro Exitoso");
                    }else{
                        limpiar("ERROR al registrar");
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    limpiar("Error al conectar con el Servidor");
                }
            });
        }else{
            limpiar("Error al leer el Qr");

        }
    }

    private void limpiar(String mns) {
        AlertDialog.Builder alerta=new AlertDialog.Builder(this);
        alerta.setMessage(mns);
        alerta.setPositiveButton("OK",null);
        alerta.create();
        alerta.show();
        tvClave.setText("");
        tvNombre.setText("");
        tvApellido.setText("");
        pBar.setVisibility(View.INVISIBLE);
    }

    private void cargarControles() {
        btnQr=findViewById(R.id.btn_qr);
        btnGaf=findViewById(R.id.btn_gaffete);
        tvClave=findViewById(R.id.tv_clave);
        tvNombre=findViewById(R.id.tv_nom);
        tvApellido=findViewById(R.id.tv_ape);
        pBar=findViewById(R.id.progresbar);
        img=findViewById(R.id.imgview);
    }

    private void cargarDatos(String dato, String clave, String hash) {
        btnGaf.setEnabled(true);
        clavePar=null;
        // #3.- Enviar al Server la peticion para buscar el registro,
        //clave="LOHA811216HMNPRR08";
        //clave="18030125";
        //hash="sistemas";
       //Toast.makeText(this, dato+" - "+clave+" - "+hash, Toast.LENGTH_SHORT).show();
        //Iniciar conexion para obtener los datos del alumno
        AsyncHttpClient cliente=new AsyncHttpClient();
        RequestParams rp=new RequestParams();
        rp.add("tipo",dato);
        rp.add("clave",clave);
        rp.add("hash",hash);
        //Toast.makeText(this, servRegistro, Toast.LENGTH_LONG).show();
        cliente.post("www.itsch.edu.mx:8081/servicios/query_registro.php", rp,new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                //Toast.makeText(MainActivity.this, servRegistro, Toast.LENGTH_LONG).show();
                try{
                    String resp=new String(responseBody);
                   // Toast.makeText(MainActivity.this, "Dato: "+resp, Toast.LENGTH_LONG).show();
                    if(resp!=null){


                        // #7.- Recibe clave-nombre-apellidos

                        JSONArray jArray=new JSONArray(resp);
                        //Recibir los datos del JsonArray en un objeto
                        Participante par=new Participante();

                        par.setClave(jArray.getJSONObject(0).getString("control"));
                        par.setNombre(jArray.getJSONObject(0).getString("nombre"));
                        par.setApellidos(jArray.getJSONObject(0).getString("apellidos"));
                        tvClave.setText(par.getClave());
                        tvNombre.setText(par.getNombre());
                        tvApellido.setText(par.getApellidos());
                        clavePar=par.getClave();
                        if(par.getClave().equals("0000")){
                            limpiar("Error: El participante No existe");
                        }
                    }else{
                        limpiar("Error en el QR");
                    }
                }catch(Exception e){
                    limpiar("Error ");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                //limpiar("Error al conectar con el servidor "+new String(responseBody));

                try {
                    Toast.makeText(MainActivity.this, "Error-> "+new String(responseBody), Toast.LENGTH_SHORT).show();
                }catch (Exception e){
                    Toast.makeText(MainActivity.this, "E---"+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void abrir() {
        // #1.- Abrir camara para leer codigo Qr.

        // #8.- Leer el codigo Qr del gaffete
        Intent intent=new Intent(MainActivity.this,ScannerActivity.class);
        try {
            startActivityForResult(intent,IDRESULT);
        }catch (Exception e){
            Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode==IDRESULT){

            if(resultCode==RESULT_OK){
                if(data!=null){
                    // #1.- Tratar los datos del resultado del QR

                    String datos=data.getStringExtra("datosResul");
                    //Toast.makeText(this,datos, Toast.LENGTH_LONG).show();
                    descomponerQr(datos);// Metodo que recibe el resultado y genra los tokens



                    /*
                    # 8.- Leer el codigo Qr del gaffete
                    Preparar los datos para el GET

                     */
                }
            }
        }
    }

    private void descomponerQr(String datos) {
        StringTokenizer st=new StringTokenizer(datos,"|");
        String dato=st.nextToken();

        // Validar que codigo se lee, si el del comprobante o el del gaffete
        if(dato.equals("externo") || dato.equals("alumno")){
            // #2.- Obtener el numero de recibo, clave y hash, recibidos en el qr
            //int recibo = Integer.parseInt(st.nextToken());
            String clave = st.nextToken();
            String hash = st.nextToken();
            //tvClave.setText(clave);
           // tvNombre.setText(hash);
            cargarDatos(dato,clave, hash);
        }else{
            sincroniza(dato);
        }

    }


    //Codigos del menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_asis) {
            Intent intent=new Intent(MainActivity.this, ActivityAsistencia.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finalizaDialog(String server, String directorio) {
        if(!server.equals("xx") && !directorio.equals("xx")) {
            cambiarPreferencias(server,directorio);
            //tvN.setText(tx + "  -  " + tx2);
        }
    }

    private void cambiarPreferencias(String server, String directorio) {
        sp=getSharedPreferences("ITSCHCongreso", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit=sp.edit();
        edit.putInt("FIRSTTIMERUN", 1);
        edit.putString("ip",server);
        edit.putString("ruta",directorio);
        miIp=server;
        miRuta=directorio;
        edit.commit();
        alerta("Se actualizaron los datos de conexion");
        servRegistro=miIp+miRuta+dc.urlQueryRegistro;
        servGaffete=miIp+miRuta+dc.urlSincronizaGaffete;
        servAsistencia=miIp+miRuta+dc.urlRegistraAsis;
    }
}
