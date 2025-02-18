package co.aisaac.nes_java;

public class Controller {
    public static final int ButtonA = 0;
    public static final int ButtonB = 1;
    public static final int ButtonSelect = 2;
    public static final int ButtonStart = 3;
    public static final int ButtonUp = 4;
    public static final int ButtonDown = 5;
    public static final int ButtonLeft = 6;
    public static final int ButtonRight = 7;

    private boolean[] buttons = new boolean[8];
    private byte index;
    private byte strobe;

    public Controller() {
    }

    public static Controller NewController() {
        return new Controller();
    }

    public void SetButtons(boolean[] buttons) {
        this.buttons = buttons;
    }

    public byte Read() {
        byte value = 0;
        if (this.index < 8 && this.buttons[this.index]) {
            value = 1;
        }
        this.index++;
        if ((this.strobe & 1) == 1) {
            this.index = 0;
        }
        return value;
    }

    public void Write(byte value) {
        this.strobe = value;
        if ((this.strobe & 1) == 1) {
            this.index = 0;
        }
    }
}
