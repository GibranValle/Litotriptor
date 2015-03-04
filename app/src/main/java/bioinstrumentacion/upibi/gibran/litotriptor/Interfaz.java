package bioinstrumentacion.upibi.gibran.litotriptor;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


public class Interfaz extends Activity implements SeekBar.OnSeekBarChangeListener,View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    /*//////////////////////// CONSTANTES PARA BLUETOOTH//////////////////////////////////////////*/
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    // Name of the connected device
    private String connectedDeviceName = null;
    // String buffer for outgoing messages
    private StringBuffer outStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter BTadaptador = null;
    // Member object for the chat services
    private BluetoothManager BTservice = null;
    private static final boolean D = true;
    /*//////////////////////// CONSTANTES PARA BLUETOOTH//////////////////////////////////////////*/
    int pm, pM, mm, mM, fss;
    int valorBarra1, valorBarra2;
    TextView estado, consola;
    TextView frecuenciaPortadora, frecuenciaModuladora, texto1, texto2;
    Button botonEnviar1, botonEnviar2, botonParo;
    SeekBar barrita, barrita2;
    Intent i;
    ObjectAnimator animador;
    String incremento;
    // Debuggin
    String TAG = "Interfaz";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, " onCreate ");
        setContentView(R.layout.activity_interfaz);

        //cargar recurso xml
        consola = (TextView) findViewById(R.id.consola);
        estado = (TextView) findViewById(R.id.mensaje_estado);

        frecuenciaPortadora = (TextView) findViewById(R.id.progreso);
        frecuenciaModuladora = (TextView) findViewById(R.id.progreso2);

        texto1 = (TextView) findViewById(R.id.textoProgreso);
        texto2 = (TextView) findViewById(R.id.textoProgreso2);

        botonEnviar1 = (Button) findViewById(R.id.b_enviar1);
        botonEnviar2 = (Button) findViewById(R.id.b_enviar2);
        botonParo = (Button) findViewById(R.id.b_paro);

        barrita = (SeekBar) findViewById(R.id.velocidad);
        barrita2 = (SeekBar) findViewById(R.id.velocidad2);

        // asignar event listener
        botonEnviar1.setOnClickListener(this);
        botonEnviar2.setOnClickListener(this);
        botonParo.setOnClickListener(this);

        barrita.setOnSeekBarChangeListener(this);
        barrita2.setOnSeekBarChangeListener(this);

        //cargar la clave en cada push, abrir el archivo en modo privado
        final SharedPreferences respaldo = getSharedPreferences("MisDatos", Context.MODE_PRIVATE);
        // cargar la clave en la variable clave, o 0000 por default (no encontrada, etc);
        pm = Integer.parseInt(respaldo.getString("fp1","10000"));
        pM = Integer.parseInt(respaldo.getString("fp2","20000"));
        mm = Integer.parseInt(respaldo.getString("fm1","0"));
        mM = Integer.parseInt(respaldo.getString("fm2","1000"));
        fss = Integer.parseInt(respaldo.getString("fss","1"));
        incremento = respaldo.getString("incremento","0");

        // asignar valor inicial
        barrita.setProgress(50);
        barrita2.setProgress(50);

        //////////////////*BLUETOOH ////////////////*/////////////////*/////////////////*/////////////////*/
        // Obtener el adaptador y comprobar soporte de BT
        BTadaptador = BluetoothAdapter.getDefaultAdapter();
        if (BTadaptador == null) {
            Log.e(TAG, "NO SOPORTA BT");
            finish();
        }
        //////////////////*BLUETOOH ////////////////*/////////////////*/////////////////*/////////////////*/

    }

    protected void onResume() {
        super.onResume();
        Log.d(TAG, "RESUMIENDO");
        //////////////////*BLUETOOH ////////////////*/////////////////*/////////////////*/////////////////*/
        if (!BTadaptador.isEnabled())//habilitar si no lo esta
        {
            BTadaptador.enable();
        }
        if (BTservice != null)  //si ya se configuró el servicio de BT
        {
            //iniciar si no se ha iniciado
            if (BTservice.getState() == BluetoothManager.STATE_NONE) {
                BTservice.start();
            }
        }
        // configurar el servicio de BT
        if (BTservice == null) configurar();
        //////////////////*BLUETOOH ////////////////*/////////////////*/////////////////*/////////////////*/

        //cargar la clave en cada push, abrir el archivo en modo privado
        final SharedPreferences respaldo = getSharedPreferences("MisDatos", Context.MODE_PRIVATE);
        // cargar la clave en la variable clave, o 0000 por default (no encontrada, etc);
        pm = Integer.parseInt(respaldo.getString("fp1","10000"));
        pM = Integer.parseInt(respaldo.getString("fp2","20000"));
        mm = Integer.parseInt(respaldo.getString("fm1","0"));
        mM = Integer.parseInt(respaldo.getString("fm2","1000"));
        fss = Integer.parseInt(respaldo.getString("fss","1"));
        incremento = respaldo.getString("incremento","0");

        // ENVIAR LA CONFIGURACIÓN DE INCREMENTO
        enviarMensaje("I" + incremento+"\n"); // INCREMENTO ?
        enviarMensaje("F" + fss+"\n"); // PASO DE INCREMENTO
        enviarMensaje("A" + mM); // PASO DE INCREMENTO
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "DETENIENDO");
        enviarMensaje("F");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Destruyendo");
        enviarMensaje("S0");
        if (BTadaptador.isEnabled())//habilitar si no lo esta
        {
            BTadaptador.disable();
        }
        if (BTservice != null)  //si ya se configuró el servicio de BT
        {
            //iniciar si no se ha iniciado
            if (BTservice.getState() == BluetoothManager.STATE_CONNECTED) {
                BTservice.stop();
            }
        }
    }

    public void animar(Button boton, int ColorBase, int ColorPush)
    {
        //animacion
        if (animador != null)
        {
            animador.cancel();
        }
        // colores default previos 0xe62b2a71, 0xe6000394, 0xe62b2a71
        animador = ObjectAnimator.ofInt(boton, "backgroundColor", ColorBase, ColorPush, ColorBase);
        animador.setDuration(100);
        animador.setEvaluator(new ArgbEvaluator());
        animador.start();
    }

    @Override
    public void onClick(View v) {
        Vibrator vibrador = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);        // Vibrate for 500 milliseconds
        vibrador.vibrate(100);
        if(v.getId() == R.id.b_enviar1) // FRECUENCIA PORTADORA
        {
            animar(botonEnviar1, 0xe62b2a71, 0xe6000394);
            Log.d(TAG, "P"+valorBarra1);
            enviarMensaje("P" + valorBarra1); // OPCION SIN RELLENAR
        }

        else if(v.getId() == R.id.b_enviar2) // FRECUENCIA MODULADORA
        {
            animar(botonEnviar2,0xe62b2a71, 0xe6000394);
            Log.d(TAG, "M"+valorBarra2);
            enviarMensaje("M" + valorBarra2); // OPCION SIN RELLENAR
        }

        else if(v.getId() == R.id.b_paro)   // PARO DE EMERGENCIA
        {
            // 0X8cff0000, 0xd2700000, 0X8cff0000
            animar(botonParo,0X8cff0000, 0xd2700000);
            Log.d(TAG,"S0" );
            enviarMensaje("S0");
        }
    }
    //************************************** SWITCH *********************************************//
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Vibrator vibrador = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);        // Vibrate for 500 milliseconds
        vibrador.vibrate(50);
    }

    //************************************** Seekbar *********************************************//
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        if(seekBar.getId()==R.id.velocidad) // primer seekbar
        {
            valorBarra1 = (pM - pm)*progress/100 + pm ;
            frecuenciaPortadora.setText(valorBarra1+ "Hz");

        }
        if(seekBar.getId()==R.id.velocidad2) // primer seekbar
        {
            valorBarra2 = (mM - mm)*progress/100 + mm ;
            frecuenciaModuladora.setText(valorBarra2 + "Hz");
        }
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
    }
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
    //************************************** Seekbar *********************************************//


    /* ///////////////////////////////MENU/////////////////////////////////////////////////////// */
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.control, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "BUTON PUSHADO");
        int id = item.getItemId();
        switch (id) {
            case R.id.buscar:
                Log.d(TAG, "ABRIR FRAGMENT");
                i = new Intent(this, DeviceList.class);
                startActivityForResult(i, REQUEST_CONNECT_DEVICE_SECURE);
                break;

            case R.id.visible: //hacer BT visible
                Log.d(TAG, "HACER VISIBLIE EL BT");
                hacerVisible();
                break;

            case R.id.config:
                Log.d(TAG, "CONFIGURAR FRECUENCIAS");
                startActivity(new Intent(this,Frecuencias.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    /* ///////////////////////////////MENU/////////////////////////////////////////////////////// */

    /* ///////////////////////////////METODOS BLUETOOTH/////////////////////////////////////////////// */
    private void hacerVisible() {
        if (D) Log.d(TAG, "ensure discoverable");
        if (BTadaptador.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
        {
            // elegir el intent para hacer visible
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            // 300 segundos de hacerlo visible
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            // lanzar el intent
            startActivity(discoverableIntent);
        }
    }

    private void configurar() {
        Log.d(TAG, "setupChat()");
        // Initialize the BluetoothChatService to perform bluetooth connections
        BTservice = new BluetoothManager(this, mHandler);
        // Initialize the buffer for outgoing messages
        outStringBuffer = new StringBuffer("");
    }

    private void enviarMensaje(String mensaje) //recibeel mensaje enviar de tipo string
    {
        //checar la conexion antes de enviar
        if (BTservice.getState() != BluetoothManager.STATE_CONNECTED) {
            Toast.makeText(this, "NO CONECTADO", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "NO CONECTADO");
            return;
        }
        //comprobar que haya algo para enviar
        if (mensaje.length() > 0) {
            //convertir a bytes para enviar por serial
            byte[] send = mensaje.getBytes();
            BTservice.write(send);
            Log.d(TAG, "ENVIANDO MENSAJE: " + mensaje);
        }
    }

    private void conectarDevice(Intent data, boolean secure) {
        // RECUPERAR LA DIRECCIÓN MAC
        String address = data.getExtras().getString(DeviceList.EXTRA_DEVICE_ADDRESS);
        String nombre = data.getExtras().getString(DeviceList.EXTRA_DEVICE_NAME);
        // Recupera el objeto BluetoothDevice
        BluetoothDevice device = BTadaptador.getRemoteDevice(address);
        // Intentar conectar el device
        BTservice.connect(device, secure);
        Log.d(TAG, "CONECTANDO A DEVICE.... " + device);
        Log.d(TAG, "CONECTANDO A DEVICE.... " + nombre);
        Toast.makeText(this, "Conectando a " + nombre, Toast.LENGTH_LONG).show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    conectarDevice(data, true);
                    Log.d(TAG, "CONEXION SEGURA, DISPOSITIVO");
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    conectarDevice(data, false);
                    Log.d(TAG, "CONEXION SEGURA, INSEGURA");
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    configurar();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.e(TAG, "ERROR DE CONEXION");
                    Toast.makeText(this, "ERROR DE CONEXION", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // APLICAR LOS CAMBIOS DE COLOR EN LA INTERFAZ CUANDO DETECTA UN CAMBIO EN ESTADO
                case MESSAGE_STATE_CHANGE:
                    if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothManager.STATE_CONNECTED:
                            estado.setText(R.string.bt_CT);
                            estado.setBackgroundColor(0x4300ff00);
                            Log.d(TAG, " BT CONECTADO");
                            enviarMensaje("O"+"\n");
                            // ENVIAR LA CONFIGURACIÓN DE INCREMENTO
                            enviarMensaje("I" + incremento+"\n"); // INCREMENTO ?
                            enviarMensaje("F" + fss+"\n"); // PASO DE INCREMENTO
                            enviarMensaje("A" + mM); // PASO DE INCREMENTO
                            break;
                        case BluetoothManager.STATE_CONNECTING:
                            estado.setText(R.string.bt_CTING);
                            estado.setBackgroundColor(0x430000ff);
                            Log.d(TAG, " BT CONECTANDO");
                            break;
                        case BluetoothManager.STATE_LISTEN:
                            estado.setText(R.string.bt_DC);
                            estado.setBackgroundColor(0x43ff0000);
                            Log.d(TAG, " BT DESCONECTADO");
                            break;
                        case BluetoothManager.STATE_NONE:
                            estado.setText(R.string.bt_DC);
                            estado.setBackgroundColor(0x43ff0000);
                            Log.d(TAG, " BT DESCONECTADO");
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    String writeMessage = new String(writeBuf);
                    break;
                case MESSAGE_READ:
                    final String readMessage = (String) msg.obj;
                    Log.e(TAG, "armado2: " + readMessage);
                    consola.setText(readMessage);
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    connectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Conectado a " + connectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
}/* ///////////////////////////////////////////// FIN MAIN //////////////////////////////////////*/