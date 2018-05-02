package owl.app.limpia_publica.activities;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ganesh.intermecarabic.Arabic864;
import com.honeywell.mobility.print.LinePrinter;
import com.honeywell.mobility.print.LinePrinterException;
import com.honeywell.mobility.print.PrintProgressEvent;
import com.honeywell.mobility.print.PrintProgressListener;
import com.honeywell.mobility.print.PrinterException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import owl.app.limpia_publica.R;
import owl.app.limpia_publica.utils.DateTime;

public class PrintActivity extends AppCompatActivity {

    private static final String TAG = PrintActivity.class.getName();
    private Button mPrintBtn;
    private TextView mTextMessageTV;
    private EditText mPrinterIdET;
    private EditText mPrinterMacAddressET;
    private LinePrinter mLinePrinter = null;
    private String mJsonCmdAttributeStr = null;
    final String PRINTER_TYPE = "PR3"; // printer type here ex : pr3 , pb31...
    final String PRINTER_MAC_ADDRESS = "88:6B:0F:3E:51:CD"; // printer type here ex : pr3 , pb31...

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);
        init();
        printerConfig();

        new PrintTask().execute(PRINTER_TYPE, PRINTER_MAC_ADDRESS);

        mPrintBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentFinal = new Intent(PrintActivity.this, MainActivity.class);
                intentFinal.setFlags(intentFinal.FLAG_ACTIVITY_NEW_TASK | intentFinal.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intentFinal);
            }
        });
    }

    private void printerConfig() {
        //mPrinterIdET.setText(PRINTER_TYPE); // Set a default Printer ID.
        //mPrinterMacAddressET.setText(PRINTER_MAC_ADDRESS); // Set a default Mac Address
    }

    private void init() {
        mTextMessageTV = (TextView) findViewById(R.id.textMsg);
        //mPrinterIdET = (EditText) findViewById(R.id.editPrinterID);
        //mPrinterMacAddressET = (EditText) findViewById(R.id.editMacAddr);
        mPrintBtn = (Button) findViewById(R.id.buttonPrint);
        readAssetFiles();
    }

    private void readAssetFiles() {
        InputStream input = null;
        ByteArrayOutputStream output = null;
        AssetManager assetManager = PrintActivity.this.getAssets();
        String fileName = "printer_profiles.JSON";
        int fileIndex = 0, initialBufferSize;

        try {
            input = assetManager.open(fileName);
            initialBufferSize = 8000;
            output = new ByteArrayOutputStream(initialBufferSize);

            byte[] buf = new byte[1024];
            int len;
            while ((len = input.read(buf)) > 0) {
                output.write(buf, 0, len);
            }
            input.close();
            input = null;

            output.flush();
            output.close();
            switch (fileIndex) {
                case 0:
                    mJsonCmdAttributeStr = output.toString();
                    break;
            }
            output = null;
        } catch (Exception ex) {
            Log.v(TAG, ex.getMessage());
        } finally {
            try {
                if (input != null) {
                    input.close();
                    //input = null;
                }
                if (output != null) {
                    output.close();
                    //output = null;
                }
            } catch (IOException ex) {
                Log.v(TAG, ex.getMessage());
            }
        }
    }

    /**
     * This class demonstrates printing in a background thread and updates
     * the UI in the UI thread.
     */
    private class PrintTask extends AsyncTask<String, Integer, String> {
        private static final String PROGRESS_CANCEL_MSG = "Impresión cancelada\n";
        private static final String PROGRESS_COMPLETE_MSG = "Impresión completa\n";
        private static final String PROGRESS_END_DOC_MSG = "Fin del documento\n\n";
        private static final String PROGRESS_FINISHED_MSG = "Conexión de la impresora cerrada\n";
        private static final String PROGRESS_NONE_MSG = "Mensaje de progreso desconocido\n";
        private static final String PROGRESS_START_DOC_MSG = "Impresion en proceso...\n";


        /**
         * Runs on the UI thread before doInBackground(Params...).
         */
        @Override
        protected void onPreExecute() {
            // Clears the Progress and Status text box.
            mTextMessageTV.setText("");
            // Disables the Print button.
            mPrintBtn.setEnabled(false);

            // Shows a progress icon on the title bar to indicate
            setProgressBarIndeterminateVisibility(true);
        }

        /**
         * This method runs on a background thread. The specified parameters
         * are the parameters passed to the execute method by the caller of
         * this task. This method can call publishProgress to publish updates
         * on the UI thread.
         */
        @Override
        protected String doInBackground(String... args) {
            String sResult = null, sPrinterID = args[0], sMacAddress = args[1];

            if (!sMacAddress.contains(":") && sMacAddress.length() == 12) {
                char[] addressChars = new char[17];
                for (int i = 0, j = 0; i < 12; i += 2) {
                    sMacAddress.getChars(i, i + 2, addressChars, j);
                    j += 2;
                    if (j < 17) {
                        addressChars[j++] = ':';
                    }
                }
                sMacAddress = new String(addressChars);
            }
            String sPrinterURI = "bt://" + sMacAddress;
            LinePrinter.ExtraSettings exSettings = new LinePrinter.ExtraSettings();
            exSettings.setContext(PrintActivity.this);

            PrintProgressListener progressListener =
                    new PrintProgressListener() {
                        @Override
                        public void receivedStatus(PrintProgressEvent aEvent) {
                            publishProgress(aEvent.getMessageType());// Publishes updates on the UI thread.
                        }
                    };

            try {
                mLinePrinter = new LinePrinter(mJsonCmdAttributeStr, sPrinterID, sPrinterURI, exSettings);
                mLinePrinter.addPrintProgressListener(progressListener); //registers to listen for the print progress events.

                //A retry sequence in case the bluetooth socket is temporarily not ready
                int triesNum = 0, maxRetry = 2;
                while (triesNum < maxRetry) {
                    try {
                        mLinePrinter.connect();  // Connects to the printer
                        break;
                    } catch (LinePrinterException ex) {
                        triesNum++;
                        Thread.sleep(1000);
                    }
                }
                if (triesNum == maxRetry) //Final retry
                    mLinePrinter.connect();

                // config arabic characters for Intermec printer(must if you print arabic)...
                byte[] arabicFont = new byte[]{0x1b, 0x77, 0x46};
                mLinePrinter.write(arabicFont);

                // Check the state of the printer and abort printing if there are
                // any critical errors detected.
                int[] results = mLinePrinter.getStatus();
                if (results != null) {
                    for (int result : results) {
                        if (result == 223) {
                            // Paper out.
                            throw new Exception("Paper out");
                        } else if (result == 227) {
                            // Lid open.
                            throw new Exception("Printer lid open");
                        }
                    }
                }
                intermecPrint();
                sResult = "Number of bytes sent to printer: " + mLinePrinter.getBytesWritten();
            } catch (Exception ex) {
                if (mLinePrinter != null)
                    mLinePrinter.removePrintProgressListener(progressListener);// Stop listening for printer events.
                sResult = "Unexpected exception: " + ex.getMessage();
            } finally {
                try {
                    if (mLinePrinter != null)
                        mLinePrinter.disconnect();
                } catch (PrinterException e) {
                    e.printStackTrace();
                }
            }
            return sResult;// The result string will be passed to the onPostExecute method
        }

        /**
         * Runs on the UI thread after publishProgress is invoked. The
         * specified values are the values passed to publishProgress.
         */
        @Override
        protected void onProgressUpdate(Integer... values) {
            // Access the values array.
            int progress = values[0];

            switch (progress) {
                case PrintProgressEvent.MessageTypes.CANCEL:
                    mTextMessageTV.append(PROGRESS_CANCEL_MSG);
                    break;
                case PrintProgressEvent.MessageTypes.COMPLETE:
                    mTextMessageTV.append(PROGRESS_COMPLETE_MSG);
                    break;
                case PrintProgressEvent.MessageTypes.ENDDOC:
                    mTextMessageTV.append(PROGRESS_END_DOC_MSG);
                    break;
                case PrintProgressEvent.MessageTypes.FINISHED:
                    mTextMessageTV.append(PROGRESS_FINISHED_MSG);
                    break;
                case PrintProgressEvent.MessageTypes.STARTDOC:
                    mTextMessageTV.append(PROGRESS_START_DOC_MSG);
                    break;
                default:
                    mTextMessageTV.append(PROGRESS_NONE_MSG);
                    break;
            }
        }

        /**
         * Runs on the UI thread after doInBackground method. The specified
         * result parameter is the value returned by doInBackground.
         */
        @Override
        protected void onPostExecute(String result) {
            // Displays the result (number of bytes sent to the printer or
            // exception message) in the Progress and Status text box.
            if (result != null) {
                mTextMessageTV.append(result);
            }

            // Dismisses the progress icon on the title bar.
            setProgressBarIndeterminateVisibility(false);

            // Enables the Print button.
            mPrintBtn.setEnabled(true);
        }
    } //end of class PrintTask

    /**
     * Print text to intermec printer....
     */
    private void intermecPrint() {
        try {
            Bundle bundle = getIntent().getExtras();

            mLinePrinter.write("COSAMALOAPAN VERACRUZ");
            mLinePrinter.newLine(1);

            mLinePrinter.writeLine("Limpia Publica");
            mLinePrinter.newLine(2);

            mLinePrinter.write("Contribuyente: " + bundle.getString("contribuyente"));
            mLinePrinter.newLine(1);

            if(bundle.getBoolean("verificar")) {
                mLinePrinter.write("Comercio: " + bundle.getString("comercio"));
                mLinePrinter.newLine(1);
            }

            mLinePrinter.write("Importe: " + "$" + bundle.getString("importe") + " Pesos");
            mLinePrinter.newLine(1);

            mLinePrinter.write("Fecha: " + DateTime.getDateTime());
            mLinePrinter.newLine(1);

            mLinePrinter.write("Tipo de Cobro: " + bundle.getString("tipo"));
            mLinePrinter.newLine(2);

            mLinePrinter.write("---------------------------");
            mLinePrinter.newLine(1);

            mLinePrinter.write("---------------------------");
            mLinePrinter.newLine(1);

            mLinePrinter.write("\n");
            mLinePrinter.newLine(1);

        } catch (Exception ex) {
            Log.v(TAG, ex.getMessage());
        }
    }

    private class BadPrinterStateException extends Exception {
        static final long serialVersionUID = 1;

        BadPrinterStateException(String message) {
            super(message);
        }
    }
}
