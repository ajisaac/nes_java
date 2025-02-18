package co.aisaac.nes_java;

public class Controller {
    // Original Golang constants using iota - values: 0, 1, 2, 3, 4, 5, 6, 7
    public static final int ButtonA = 0;
    public static final int ButtonB = 1;
    public static final int ButtonSelect = 2;
    public static final int ButtonStart = 3;
    public static final int ButtonUp = 4;
    public static final int ButtonDown = 5;
    public static final int ButtonLeft = 6;
    public static final int ButtonRight = 7;

    // Fields corresponding to [8]bool buttons, index byte, strobe byte in Golang
    private boolean[] buttons = new boolean[8];
    private byte index;
    private byte strobe;

    // Constructor corresponding to NewController in Golang; returns an empty Controller instance
    public Controller() {
        // Default initialization; fields are automatically set to 0/false as in Go
    }

    // Equivalent of Golang NewController() function
    public static Controller NewController() {
        return new Controller();
    }

    // Sets the controller buttons as provided. Mirrors: func (c *Controller) SetButtons(buttons [8]bool)
    public void SetButtons(boolean[] buttons) {
        this.buttons = buttons;
    }

    // Reads the current button state as a byte. Mirrors: func (c *Controller) Read() byte
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

    // Writes a value to the controller, setting the strobe and possibly resetting the index.
    // Mirrors: func (c *Controller) Write(value byte)
    public void Write(byte value) {
        this.strobe = value;
        if ((this.strobe & 1) == 1) {
            this.index = 0;
        }
    }
}
