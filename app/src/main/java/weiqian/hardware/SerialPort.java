package weiqian.hardware;


public class SerialPort {
    
    private int mFd;
    
    public SerialPort() {
        mFd = -1;
    }
    
    public void open(String path, int baud, int databits, String parity, int stopbits) {
        mFd = HardwareControl.OpenSerialPort(path, baud, databits, parity, stopbits);
    }
    
    public void close() {
        HardwareControl.CloseSerialPort(mFd);
    }
    
    public int read(byte[] buff, int count) {
        return HardwareControl.ReadSerialPort(mFd, buff, count);
    }
    
    public int write(byte[] buff, int count) {
        return HardwareControl.WriteSerialPort(mFd, buff, count);
    }
 
}
