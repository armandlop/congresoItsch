package itsch.edu.congresoitsch;

import android.app.Activity;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

/**
 * Created by armandolopez on 05/11/18.
 */

public class ScannerActivity extends AppCompatActivity implements ZBarScannerView.ResultHandler {

    private ZBarScannerView mScannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZBarScannerView(this);    // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view
    }
    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }
    @Override
    public void handleResult(Result result) {
        // Do something with the result here
        //Log.v(TAG, rawResult.getContents()); // Prints scan results
        //Log.v(TAG, rawResult.getBarcodeFormat().getName()); // Prints the scan format (qrcode, pdf417 etc.)

        //Toast.makeText(this, result.getContents().toString(), Toast.LENGTH_SHORT).show();
        // If you would like to resume scanning, call this method below:
        //mScannerView.resumeCameraPreview(this);

        //escanear el codigo, y obtener el resultado
        final String resulCode=result.getContents();
        //dar formato
        final String format=result.getBarcodeFormat().getName();
        //Concatenar
        final String mnsj=resulCode+"";
        //poner sonido
        try{

            // #1.- Leer codigo Qr del comprobante
            Uri notificacion= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone tono=RingtoneManager.getRingtone(getApplicationContext(),notificacion);
            tono.play();
            Intent resultData=new Intent();
            resultData.putExtra("datosResul",mnsj);
            setResult(Activity.RESULT_OK,resultData);
            finish();
        }catch (Exception e){
            Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
        }


        /*
        try {
                    // algún código que procese lo que querramos
                    Intent resultData = new Intent();
                    resultData.putExtra("Test", "Lo he aprendido en <a class="vglnk" href="http://www.devtroce.com" rel="nofollow"><span>www</span><span>.</span><span>devtroce</span><span>.</span><span>com</span></a>");
                    setResult(Activity.RESULT_OK, resultData);
                    finish();
                } catch (Exception e) {
                    MensajeBox(e.getMessage());
                }
         */
    }


}
