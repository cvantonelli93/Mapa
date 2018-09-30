package ve.com.edu.ucab.testmap.testmap;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(isServicesOK())
        {
            init();
        }
    }

    private void init() {
        Button btnMap = (Button) findViewById(R.id.btnMap);
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Pasando al Mapa");
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });
    }
    // Chequear la version del servidor
    public boolean isServicesOK()
    {
        Log.d(TAG, "isServicesOK: se chequeo la version de google services");
        int available = GoogleApiAvailability.getInstance().
                isGooglePlayServicesAvailable(MainActivity.this);
        if(available == ConnectionResult.SUCCESS){
            //Todo esta bien
            Log.d(TAG, "isServicesOK: google play esta trabajando");
            return true;
        }
        else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            Log.d(TAG, "isServicesOK: pero se encontrado un error el cual puede solucionarse");
            Dialog dialog = GoogleApiAvailability.getInstance().
                    getErrorDialog(MainActivity.this,available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }
        else{
            Toast.makeText(this,"No se puede conectar ",Toast.LENGTH_SHORT).show();
        }
        return false;

    }

}


