public class CircularStringBuffer {
    private final char[] buffer;
    private int head;  // Points to the start of the content
    private int tail;  // Points to the end of the content
    private int size;  // Current number of characters in the buffer
    private final int capacity;
    
    public CircularStringBuffer() {
        this(255);  // Default capacity of 255 characters
    }
    
    public CircularStringBuffer(int capacity) {
        this.capacity = capacity;
        this.buffer = new char[capacity];
        this.head = 0;
        this.tail = 0;
        this.size = 0;
    }
    
    /**
     * Adds a new string to the end of the buffer
     * @param str The string to add
     */
    public void add(String str) {
        if (str == null || str.isEmpty()) {
            return;
        }
        
        // If the string is longer than capacity, only take the last 'capacity' characters
        if (str.length() > capacity) {
            str = str.substring(str.length() - capacity);
        }
        
        // Add each character to the buffer
        for (char c : str.toCharArray()) {
            addChar(c);
        }
    }
    
    private void addChar(char c) {
        if (size == capacity) {
            // Buffer is full, remove oldest character
            head = (head + 1) % capacity;
            size--;
        }
        
        buffer[tail] = c;
        tail = (tail + 1) % capacity;
        size++;
    }
    
    /**
     * @return The current content of the buffer as a String
     */
    @Override
    public String toString() {
        if (size == 0) {
            return "";
        }
        
        char[] result = new char[size];
        if (head < tail) {
            System.arraycopy(buffer, head, result, 0, size);
        } else {
            int firstPart = capacity - head;
            System.arraycopy(buffer, head, result, 0, firstPart);
            System.arraycopy(buffer, 0, result, firstPart, tail);
        }
        
        return new String(result);
    }
    
    /**
     * @return The current number of characters in the buffer
     */
    public int size() {
        return size;
    }
    
    /**
     * @return The maximum capacity of the buffer
     */
    public int capacity() {
        return capacity;
    }
    
    /**
     * Clears the buffer
     */
    public void clear() {
        head = 0;
        tail = 0;
        size = 0;
    }
}