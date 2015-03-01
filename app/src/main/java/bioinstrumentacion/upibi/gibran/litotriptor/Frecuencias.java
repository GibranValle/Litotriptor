package bioinstrumentacion.upibi.gibran.litotriptor;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/** ESTA ACTIVITY TIENE APARIENCIA DE DIALOGO (CONFIGURADA EN EL MANIFEST)
 * SI EXISTEN DISPOSITIVOS VINCULADOS, LOS ARREGLA AL PRIMER ARREGO
 * SE ENLISTAN MAS DEVICES AL HACE UN DISCOVERY CUANDO SE ELIGE UN DEVICE
 * SE REGRESA LA DIRECCION MAC A LA PARENT ACTIVITY EN EL INTENT RESULT
 */
public class Frecuencias extends Activity {
    // Debugging
    private static final String TAG = "CONTROL";
    private static final boolean D = true;

    Button actualizar, cancelar;
    String fp1, fp2, fm1, fm2;
    int pm, pM, mm, mM;
    TextView frecuenciaPortadora1, frecuenciaPortadora2;
    TextView frecuenciaModuladora1, frecuenciaModuladora2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // cargar la ventana
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.config_frecuencias);

        frecuenciaPortadora1 = (TextView) findViewById(R.id.fp_in1);
        frecuenciaPortadora2 = (TextView) findViewById(R.id.fp_in2);
        frecuenciaModuladora1 = (TextView) findViewById(R.id.fm_in1);
        frecuenciaModuladora2 = (TextView) findViewById(R.id.fm_in2);

        actualizar = (Button) findViewById(R.id.b_actualizar);
        cancelar = (Button) findViewById(R.id.b_cancelar);


        //cargar la clave en cada push, abrir el archivo en modo privado
        final SharedPreferences respaldo = getSharedPreferences("MisDatos", Context.MODE_PRIVATE);
        // cargar la clave en la variable clave, o 0000 por default (no encontrada, etc);
        fp1 = respaldo.getString("fp1","10000");
        fp2 = respaldo.getString("fp2","20000");
        fm1 = respaldo.getString("fm1","0");
        fm2 = respaldo.getString("fm2","1000");

        frecuenciaPortadora1.setText(fp1);
        frecuenciaPortadora2.setText(fp2);
        frecuenciaModuladora1.setText(fm1);
        frecuenciaModuladora2.setText(fm2);

        actualizar.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Vibrator vibrador = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);        // Vibrate for 500 milliseconds
                vibrador.vibrate(50);
                pm = Integer.parseInt(fp1);
                pM = Integer.parseInt(fp2);
                mm = Integer.parseInt(fm1);
                mM = Integer.parseInt(fm2);

                Log.d(TAG," portadora minima: "+pm +" portadora maxima: " + pM + " moduladora minima: "+mm + " moduladora maxima: " +mM);
                if(pm >= pM || mm >= mM)
                {
                    Toast.makeText(getBaseContext(),"Valores invalidos", Toast.LENGTH_LONG).show();
                }
                else if(pm <0 || mm <0 || pM > 20000 || mM > 20000)
                {
                    Toast.makeText(getBaseContext(),"Valores invalidos", Toast.LENGTH_LONG).show();
                }
                else
                {
                    fp1 = frecuenciaPortadora1.getText().toString();
                    fp2 = frecuenciaPortadora2.getText().toString();
                    fm1 = frecuenciaModuladora1.getText().toString();
                    fm2 = frecuenciaModuladora2.getText().toString();

                    SharedPreferences.Editor editor = respaldo.edit();
                    editor.putString("fp1", fp1);
                    editor.putString("fp2", fp2);
                    editor.putString("fm1", fm1);
                    editor.putString("fm2", fm2);
                    if(editor.commit())
                    {
                        Toast.makeText(getBaseContext(),"Actualizado correctamente", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
            }
        });

        cancelar.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Vibrator vibrador = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);        // Vibrate for 500 milliseconds
                vibrador.vibrate(50);
                finish();
            }
        }
        );

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
