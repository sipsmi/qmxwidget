/* 
 * This class implements a circular string buffer that stores characters in a fixed-size 
 * array. It allows for adding new strings while maintaining a maximum capacity, effectively 
 * overwriting the oldest data when necessary. The class provides functionality to clear the 
 * buffer, obtain its current contents as a string, and check its size and capacity.
 */

/* 
 * Key improvement opportunities: 
 * 1. The method to add a string could use more comprehensive error handling for input validation 
 *    beyond null or empty checks. 
 * 2. The current implementation might be susceptible to buffer overflow if not managed properly.
 * 3. There are no checks for concurrent modifications to the buffer, which could lead to inconsistent states.
 */

public class CircularStringBuffer {
    private final char[] buffer;  // The underlying array to store characters.
    private int head;              // Points to the start of the content.
    private int tail;              // Points to the end of the content.
    private int size;              // Current number of characters in the buffer.
    private final int capacity;    // Maximum capacity of the buffer.

    // Default constructor initializes the buffer with a capacity of 255 characters.
    public CircularStringBuffer() {
        this(255);
    }

    // Constructor to initialize the buffer with a specified capacity.
    public CircularStringBuffer(int capacity) {
        this.capacity = capacity;                // Set the maximum capacity.
        this.buffer = new char[capacity];       // Initialize the character array.
        this.head = 0;                          // Set head index to zero.
        this.tail = 0;                          // Set tail index to zero.
        this.size = 0;                          // Set initial size to zero.
    }

    /** 
     * Adds a new string to the end of the buffer.
     * If the string is longer than the capacity, only the last 'capacity' characters are added.
     * 
     * @param str The string to add; if null or empty, no action is taken.
     */
    public void add(String str) {
        if (str == null || str.isEmpty()) {  // Check for null or empty string.
            return;                           // No action taken if the input is invalid.
        }

        // If the string exceeds the buffer's capacity, truncate it to fit.
        if (str.length() > capacity) {       
            str = str.substring(str.length() - capacity); // Take the last 'capacity' characters.
        }

        // Add each character of the string to the buffer.
        for (char c : str.toCharArray()) {
            addChar(c);                       // Add each character individually.
        }
    }

    // Adds a single character to the buffer.
    private void addChar(char c) {
        // Check if the buffer is full and overwrite the oldest character.
        if (size == capacity) {
            head = (head + 1) % capacity;   // Increment head index circularly.
            size--;                          // Decrease the size of the buffer.
        }

        buffer[tail] = c;                    // Store the new character.
        tail = (tail + 1) % capacity;       // Increment tail index circularly.
        size++;                              // Increase the size of the buffer.
    }

    /** 
     * Returns the current content of the buffer as a String.
     * If the buffer is empty, an empty string is returned.
     * 
     * @return The current content of the buffer as a String.
     */
    @Override
    public String toString() {
        if (size == 0) {                     // Check if the buffer is empty.
            return "";                       // Return an empty string if it is.
        }

        char[] result = new char[size];      // Create a new char array to hold result.
        if (head < tail) {                   // Check if there's no wrap-around in the buffer.
            System.arraycopy(buffer, head, result, 0, size); // Copy contiguous data.
        } else {
            int firstPart = capacity - head;  // Calculate how many characters to copy.
            System.arraycopy(buffer, head, result, 0, firstPart); // Copy the first part.
            System.arraycopy(buffer, 0, result, firstPart, tail); // Copy the remaining part.
        }

        return new String(result);            // Convert char array back to String and return.
    }

    /** 
     * Returns the current number of characters in the buffer.
     * 
     * @return The current number of characters in the buffer.
     */
    public int size() {
        return size;                           // Return the size of the buffer.
    }

    /** 
     * Returns the maximum capacity of the buffer.
     * 
     * @return The maximum capacity of the buffer.
     */
    public int capacity() {
        return capacity;                       // Return the set capacity of the buffer.
    }

    /** 
     * Clears the buffer by resetting head, tail, and size.
     */
    public void clear() {
        head = 0;                             // Reset head index to zero.
        tail = 0;                             // Reset tail index to zero.
        size = 0;                             // Reset current size to zero.
    }
}