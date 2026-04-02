
/**
 * Represents the CAT commands for QMX-series transceivers.
 * Commands never contain a carriage return or linefeed and are terminated by a semicolon.
 *  1. Generate the command using our Enum
        For example, let's "Get" the frequency of VFO A
        String commandString = QmxCatCommand.FA.buildGetCommand();
 */
public enum QmxCatCommand {
    // CODE( <name>  <get supported> <set supported> )
    // Standard Kenwood/QMX Commands
    AG("Get/Set AF Gain (volume)", true, true),
    C2("Get/Set Signal Generator frequency", true, true),
    FA("Get/Set VFO A", true, true),
    FB("Get/Set VFO B", true, true),
    FR("Get/Set Receive VFO Mode", true, true),
    FT("Get/Set Transmit VFO Mode", true, true),
    FW("Get filter bandwidth", true, false),
    ID("Get radio ID", true, false),
    IF("Get transceiver information", true, false),
    KS("Get/Set Keyer speed", true, true),
    KY("Get/Set message for immediate sending", true, true),
    LC("Get LCD contents", true, false),
    MD("Get/Set operating mode", true, true),
    ML("Get menu list", true, false),
    MM("Get/Set/Query menu item", true, true),
    OM("Get the radio's model number", true, false),
    PC("Get power output", true, false),
    
    // QRP Labs Specific 'Q' Extended Commands
    Q0("Get/Set TCXO reference frequency", true, true),
    Q1("Get/Set Sideband", true, true),
    Q2("Get/Set VFO A frequency", true, true),
    Q3("Get/Set VOX Enable", true, true),
    Q4("Get/Set TX Rise Threshold", true, true),
    Q5("Get/Set TX Fall Threshold", true, true),
    Q6("Get/Set Cycle Min parameter", true, true),
    Q7("Get/Set Sample Min parameter", true, true),
    Q8("Get/Set Discard parameter", true, true),
    Q9("Get/Set IQ Mode", true, true),
    QA("Get/Set Japanese Band Limits mode", true, true),
    QB("Get/Set CAT timeout enable mode", true, true),
    QC("Get/Set CAT timeout", true, true),
    QJ("Get/Set TX shift threshold", true, true),
    
    // Receiver & Transceiver Control
    RC("Clear RIT mode", false, true),
    RD("Set negative RIT offset amount", false, true),
    RG("Get/Set RF Gain", true, true),
    RT("Get/Set RIT status", true, true),
    RU("Set positive RIT offset amount", false, true),
    RX("Set the radio into Receive mode immediately", false, true),
    SA("Get the AGC meter value", true, false),
    SM("Get the S-meter value", true, false),
    SP("Get/Set Split mode", true, true),
    SS("Get/Set SSB transmission source", true, true),
    SW("Get the SWR-meter value", true, false),
    TA("Transmit audio", false, true),
    TB("Retrieve decoded text from CW decoder", true, false),
    TM("Get/Set Real time clock time", true, true),
    TQ("Get/Set transmit state", true, true),
    TX("Set the radio into Transmit mode immediately", false, true),
    VN("Returns firmware version", true, false);

    private final String description;
    private final boolean supportsGet;
    private final boolean supportsSet;

    /**
     * Constructor for the CAT command enum.
     *
     * @param description A brief description of what the command does.
     * @param supportsGet Whether the command can be used to read a value from the transceiver.
     * @param supportsSet Whether the command can be used to write/trigger a value on the transceiver.
     */
    QmxCatCommand(String description, boolean supportsGet, boolean supportsSet) {
        this.description = description;
        this.supportsGet = supportsGet;
        this.supportsSet = supportsSet;
    }

    public String getDescription() {
        return description;
    }

    public boolean supportsGet() {
        return supportsGet;
    }

    public boolean supportsSet() {
        return supportsSet;
    }

    /**
     * Formats a 'Get' command for the transceiver (appends the semicolon).
     * @return Formatted command string, or throws an exception if unsupported.
     */
    public String buildGetCommand() {
        if (!supportsGet) {
            throw new UnsupportedOperationException("Command " + this.name() + " does not support GET.");
        }
        return this.name() + ";";
    }

    /**
     * Formats a 'Set' command with a parameter for the transceiver.
     * @param parameter The value to set.
     * @return Formatted command string, or throws an exception if unsupported.
     */
    public String buildSetCommand(String parameter) {
        if (!supportsSet) {
            throw new UnsupportedOperationException("Command " + this.name() + " does not support SET.");
        }
        return this.name() + parameter + ";";
    }
}