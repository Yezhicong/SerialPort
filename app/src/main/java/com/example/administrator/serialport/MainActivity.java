package com.example.administrator.serialport;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import weiqian.hardware.SerialPort;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements  OnClickListener{

    Spinner baudSpinner, dataSpinner, paritySpinner, stopSpinner, nameSpinner;
    EditText receiveEdit, sendEdit, timeEdit;
    Button openBut, closeBut, sendBut, stopBut, clearBut, startBut;
    TextView reText, seText, tText;
    CheckBox checkBox;
    boolean flag = false;
    boolean readflag = false;
    boolean timeflag = false;
    private ReadThread mReadThread;	//读取线程
    private SendThread mSendThread;	//发送线程
    private TimeThread mTimeThread;
    private SerialPort serialPort;
    private int reN = 0, seN = 0, num =0, timeH = 0, timeM = 0;

    private StringBuilder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        baudSpinner   = (Spinner) findViewById (R.id.baudSpinner);
        dataSpinner   = (Spinner) findViewById (R.id.dataSpinner);
        paritySpinner = (Spinner) findViewById (R.id.paritySpinner);
        stopSpinner   = (Spinner) findViewById (R.id.stopSpinner);
        nameSpinner   = (Spinner) findViewById (R.id.nameSpinner);

        receiveEdit = (EditText) findViewById (R.id.receiveText);
        sendEdit    = (EditText) findViewById (R.id.sendText);
        timeEdit    = (EditText) findViewById (R.id.timeEdit);

        openBut  = (Button) findViewById (R.id.openButton);
        closeBut = (Button) findViewById (R.id.closeButton);
        sendBut  = (Button) findViewById (R.id.sendButton);
        stopBut  = (Button) findViewById (R.id.stopButton);
        clearBut = (Button) findViewById (R.id.clearButton);
        startBut = (Button) findViewById (R.id.startButton);

        reText = (TextView) findViewById (R.id.reText);
        seText = (TextView) findViewById (R.id.seText);
        tText  = (TextView) findViewById (R.id.time_text);

        checkBox = (CheckBox) findViewById (R.id.checkBox1);

        openBut.setOnClickListener(this);
        closeBut.setOnClickListener(this);
        sendBut.setOnClickListener(this);
        stopBut.setOnClickListener(this);
        clearBut.setOnClickListener(this);
        startBut.setOnClickListener(this);

        closeBut.setEnabled(false);
        sendBut.setEnabled(false);
        stopBut.setEnabled(false);
        clearBut.setEnabled(false);
        startBut.setEnabled(false);

        serialPort = new SerialPort();
        builder = new StringBuilder();

        nameSpinner.setSelection(1);
        baudSpinner.setSelection(7);
        dataSpinner.setSelection(3);


        receiveEdit.setMovementMethod(ScrollingMovementMethod.getInstance());
        sendEdit.setMovementMethod(ScrollingMovementMethod.getInstance());
        receiveEdit.setMaxHeight(baudSpinner.getHeight() * 5 );
        sendEdit.setMaxHeight(baudSpinner.getHeight() * 3 );
        //mReadThread = new ReadThread();
        //readflag =true;
        //mReadThread.start();
    }

    //按键处理
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.openButton:
            {
                mReadThread = new ReadThread();
                mReadThread.start();
                readflag =true;
                openBut.setEnabled(false);
                closeBut.setEnabled(true);
                sendBut.setEnabled(true);
                clearBut.setEnabled(true);
                startBut.setEnabled(true);

                int baud = Integer.parseInt(baudSpinner.getSelectedItem().toString());
                int databits = Integer.parseInt(dataSpinner.getSelectedItem().toString());
                int stopbits = Integer.parseInt(stopSpinner.getSelectedItem().toString());
                String parity = paritySpinner.getSelectedItem().toString();
                serialPort.open(nameSpinner.getSelectedItem().toString(), baud, databits, parity, stopbits);

            }

            break;
            case R.id.closeButton:
            {
                receiveEdit.setText("");
                sendEdit.setText("");
                openBut.setEnabled(true);
                closeBut.setEnabled(false);
                sendBut.setEnabled(false);
                stopBut.setEnabled(false);
                startBut.setEnabled(false);
                timeflag = false;
                readflag = false;
                timeM = 0;
                timeH = 0;
                builder.delete(0, builder.length());
                serialPort.close();

                if (mReadThread != null)
                    mReadThread.interrupt();
            }
            break;
            case R.id.sendButton:
            {
                if (checkBox.isChecked()){
                    stopBut.setEnabled(true);
                    mSendThread = new SendThread();
                    flag = true;
                    mSendThread.start();
                }
                int n = sendString(sendEdit.getText().toString());
                if(n > 0) {
                    seN += n;
                    onDataSent();
                }
            }
            break;
            case R.id.stopButton:
            {
                stopBut.setEnabled(false);
                //readflag = false;
                flag = false;
                if (mSendThread != null)
                    mSendThread.interrupt();
            }
            break;
            case R.id.clearButton:
            {
                receiveEdit.setText("");
                reN=0;
                seN=0;
                timeM = 0;
                timeH = 0;
                reText.setText("R:" + reN);
                seText.setText("S:" + seN);
                tText.setText("已测:0小时0分");
                builder.delete(0, builder.length());
            }
            case R.id.startButton:
            {
                timeflag = true;
                startBut.setEnabled(false);
                mTimeThread = new TimeThread();
                mTimeThread.start();
            }
            break;
            default:
                break;
        }
    }

    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            byte[] buff = new byte[64];
            while(readflag){
                int n = 0;
                n = serialPort.read(buff, buff.length);
                if (n > 0) {
                    reN += n;
                    onDataReceived(buff);
                    num++;
                }
                Log.d("MainActivity", "return : " + n);
            }
        }
    }

    private class SendThread extends Thread {

        @Override
        public void run() {
            super.run();
            while(flag)
            {
                int n = sendString(sendEdit.getText().toString());
                if(n > 0) {
                    seN += n;
                    onDataSent();
                }
                try {
                    SendThread.sleep(Integer.parseInt(timeEdit.getText().toString()));
                } catch (NumberFormatException e) {
                    // TODO 自动生成的 catch 块
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    // TODO 自动生成的 catch 块
                    e.printStackTrace();
                }
            }
        }
    }

    protected void onDataReceived(byte[] buffer) {
        final String nbuffer = new String(buffer);
        runOnUiThread(new Runnable() {
            public void run() {
                if (reText != null && num <= 100) {
                    reText.setText("R:" + reN);
                    //receiveEdit.setText(nbuffer);
                    receiveEdit.append(nbuffer);
                    receiveEdit.append("\r\n");
                } else {
                    receiveEdit.setText(nbuffer);
                    receiveEdit.append("\r\n");
                    num = 0;
                }
            }
        });
    }

    protected void onDataSent() {
        runOnUiThread(new Runnable() {
            public void run() {
                if (seText != null) {
                    seText.setText("S:" + seN);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (mReadThread != null)
            mReadThread.interrupt();
        super.onDestroy();
    }

    private int sendString(String data) {

        return serialPort.write(data.getBytes(), data.length());
    }

    private class TimeThread extends Thread {
        @Override
        public void run() {
            super.run();
            while(timeflag) {
                try {
                    TimeThread.sleep(60000);
                    timeM++;
                    if (timeM == 60) {
                        timeH++;
                        timeM = 0;
                    }
                    onDataTime();
                } catch (NumberFormatException e) {
                    // TODO 自动生成的 catch 块
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    // TODO 自动生成的 catch 块
                    e.printStackTrace();
                }
            }
        }
    }

    protected void onDataTime() {
        runOnUiThread(new Runnable() {
            public void run() {
                if (tText != null) {
                    tText.setText("已测:" + timeH + "小时" + timeM + "分");
                }
            }
        });
    }
}
