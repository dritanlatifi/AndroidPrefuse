package prefuse.data.event;

//import javax.swing.event.TableModelEvent;

/**
 * Constants used within prefuse data structure modification notifications.
 * 
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public interface EventConstants {

//    /** Indicates a data insert operation. */
//    public static final int INSERT = TableModelEvent.INSERT;
//    /** Indicates a data update operation. */
//    public static final int UPDATE = TableModelEvent.UPDATE;
//    /** Indicates a data delete operation. */
//    public static final int DELETE = TableModelEvent.DELETE;
//    /** Indicates an operation that affects all columns of a table. */
//    public static final int ALL_COLUMNS = TableModelEvent.ALL_COLUMNS;

    /** Indicates a data insert operation. */
    public static final int INSERT = 1;
    /** Indicates a data update operation. */
    public static final int UPDATE = 0;
    /** Indicates a data delete operation. */
    public static final int DELETE = -1;
    /** Indicates an operation that affects all columns of a table. */
    public static final int ALL_COLUMNS = -1;    
    
} // end of interface EventConstants
