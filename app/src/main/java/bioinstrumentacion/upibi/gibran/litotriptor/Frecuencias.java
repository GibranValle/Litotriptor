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
import android.widget.CompoundButton;
import android.widget.Switch;
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
    private int pm, pM, mm, mM,fss;

    Button actualizar, cancelar;
    String fp1, fp2, fm1, fm2,fs,incremento;
    TextView frecuenciaPortadora1, frecuenciaPortadora2;
    TextView frecuenciaModuladora1, frecuenciaModuladora2;
    TextView frecuenciaPaso;
    Switch incrementos;
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
        frecuenciaPaso = (TextView) findViewById(R.id.f_paso_in);
        actualizar = (Button) findViewById(R.id.b_actualizar);
        cancelar = (Button) findViewById(R.id.b_cancelar);
        incrementos = (Switch) findViewById(R.id.switch1);


        //cargar la clave en cada push, abrir el archivo en modo privado
        final SharedPreferences respaldo = getSharedPreferences("MisDatos", Context.MODE_PRIVATE);
        // cargar la clave en la variable clave, o 0000 por default (no encontrada, etc);
        fp1 = respaldo.getString("fp1","10000");
        fp2 = respaldo.getString("fp2","20000");
        fm1 = respaldo.getString("fm1","0");
        fm2 = respaldo.getString("fm2","1000");
        fs = respaldo.getString("fss","1");
        incremento = respaldo.getString("incremento","0");

        if(incremento.equals("0"))
        {
            incrementos.setChecked(false);
        }
        else if(incremento.equals("1"))
        {
            incrementos.setChecked(true);
        }

        frecuenciaPortadora1.setText(fp1);
        frecuenciaPortadora2.setText(fp2);
        frecuenciaModuladora1.setText(fm1);
        frecuenciaModuladora2.setText(fm2);
        frecuenciaPaso.setText(fs);

        incrementos.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Vibrator vibrador = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);        // Vibrate for 500 milliseconds
                vibrador.vibrate(100);
                if(isChecked)
                {
                    Toast.makeText(getBaseContext(),"Incremento de frecuencia automatico activado", Toast.LENGTH_SHORT).show();
                    incremento = "1";
                }
                else
                {
                    Toast.makeText(getBaseContext(),"Incremento de frecuencia automatico desactivado", Toast.LENGTH_SHORT).show();
                    incremento = "0";
                }
            }
        });

        actualizar.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Vibrator vibrador = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);        // Vibrate for 500 milliseconds
                vibrador.vibrate(100);
                pm = Integer.parseInt(fp1);
                pM = Integer.parseInt(fp2);
                mm = Integer.parseInt(fm1);
                mM = Integer.parseInt(fm2);
                fss = Integer.parseInt(fs);

                Log.d(TAG," portadora minima: "+pm +" portadora maxima: " + pM + " moduladora minima: "+mm + " moduladora maxima: " +mM);
                if(pm >= pM)
                {
                    Toast.makeText(getBaseContext(),"Error: Fportadora min > Fportadora max", Toast.LENGTH_LONG).show();
                }
                if(mm >= mM)
                {
                    Toast.makeText(getBaseContext(),"Error: Fmoduladora min > Fmoduladora max", Toast.LENGTH_LONG).show();
                }
                if(pm < 0)
                {
                    Toast.makeText(getBaseContext(),"Error: Fportadora debe ser > 0", Toast.LENGTH_LONG).show();
                }
                if(mm <0)
                {
                    Toast.makeText(getBaseContext(),"Error: Fmoduladora debe ser > 0", Toast.LENGTH_LONG).show();
                }
                if(pM > 40000)
                {
                    Toast.makeText(getBaseContext(),"Error: Fmoduladora excesiva ", Toast.LENGTH_LONG).show();
                }
                if(mM > 40000)
                {
                    Toast.makeText(getBaseContext(),"Error: Fportadora excesiva ", Toast.LENGTH_LONG).show();
                }
                if(fss < 0 )
                {
                    Toast.makeText(getBaseContext(),"Error: el incremento debe ser positivo > ", Toast.LENGTH_LONG).show();
                }

                if(fss > 10000 )
                {
                    Toast.makeText(getBaseContext(),"Error: paso de frecuencia excesivo > ", Toast.LENGTH_LONG).show();
                }
                else
                {
                    fp1 = frecuenciaPortadora1.getText().toString();
                    fp2 = frecuenciaPortadora2.getText().toString();
                    fm1 = frecuenciaModuladora1.getText().toString();
                    fm2 = frecuenciaModuladora2.getText().toString();
                    fs = frecuenciaPaso.getText().toString();

                    SharedPreferences.Editor editor = respaldo.edit();
                    editor.putString("fp1", fp1);
                    editor.putString("fp2", fp2);
                    editor.putString("fm1", fm1);
                    editor.putString("fm2", fm2);
                    editor.putString("fss", fs);
                    editor.putString("incremento", incremento);
                    if(editor.commit())
                    {
                        Toast.makeText(getBaseContext(),"Actualizado correctamente", Toast.LENGTH_SHORT).show();
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
                vibrador.vibrate(100);
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
