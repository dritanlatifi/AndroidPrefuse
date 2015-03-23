package prefuse.data.query;

import java.util.ArrayList;



import prefuse.util.collections.CopyOnWriteArrayList;
import swing.javax.swing.MutableComboBoxModel;
import swing.javax.swing.DefaultListSelectionModel;
import swing.javax.swing.event.ListDataEvent;
import swing.javax.swing.event.ListDataListener;

/**
 * List data model supporting both data modeling and selection management.
 * Though generally useful, this has been designed particularly to support
 * dynamic queries.
 * 
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class ListModel extends DefaultListSelectionModel implements MutableComboBoxModel
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<Object> m_items = new ArrayList<Object>();
    private CopyOnWriteArrayList<ListDataListener> m_lstnrs = new CopyOnWriteArrayList<ListDataListener>();
    
    /**
     * Create an empty ListModel.
     */
    public ListModel() {
        // do nothing
    }
    
    /**
     * Create a ListModel with the provided items.
     * @param items the items for the data model.
     */
    public ListModel(final Object[] items) {
        for ( int i=0; i<items.length; ++i )
            m_items.add(items[i]);
    }
    
    // --------------------------------------------------------------------
    
    /**
     * Indicates if the ListModel currently has multiple selections.
     * @return true if there are multiple selections, false otherwise
     */
    private boolean isMultipleSelection() {
        return getMaxSelectionIndex()-getMinSelectionIndex() > 0;
    }
    
    /**
     * @see swing.javax.swing.ComboBoxModel#getSelectedItem()
     */
    public Object getSelectedItem() {
        int idx = getMinSelectionIndex();
        return ( idx == -1 ? null : m_items.get(idx) );
    }
    
    /**
     * @see swing.javax.swing.ComboBoxModel#setSelectedItem(java.lang.Object)
     */
    public void setSelectedItem(Object item) {
        int idx = m_items.indexOf(item);
        if ( idx < 0 ) return;
        
        if ( !isMultipleSelection() && idx == getMinSelectionIndex() )
            return;
        
        super.setSelectionInterval(idx,idx);
        fireDataEvent(this,ListDataEvent.CONTENTS_CHANGED,-1,-1);
    }
    
    /**
     * @see swing.javax.swing.ListModel#getSize()
     */
    public int getSize() {
        return m_items.size();
    }
    
    /**
     * @see swing.javax.swing.ListModel#getElementAt(int)
     */
    public Object getElementAt(int idx) {
        return m_items.get(idx);
    }
    
    /**
     * @see swing.javax.swing.MutableComboBoxModel#addElement(java.lang.Object)
     */
    public void addElement(Object item) {
        m_items.add(item);
        int sz = m_items.size()-1;
        fireDataEvent(this,ListDataEvent.INTERVAL_ADDED,sz,sz);
        if ( sz >= 0 && isSelectionEmpty() && item != null )
            setSelectedItem(item);
    }
    
    /**
     * @see swing.javax.swing.MutableComboBoxModel#insertElementAt(java.lang.Object, int)
     */
    public void insertElementAt(Object item, int idx) {
        m_items.add(idx, item);
        fireDataEvent(this,ListDataEvent.INTERVAL_ADDED,idx,idx);
    }
    
    /**
     * @see swing.javax.swing.MutableComboBoxModel#removeElement(java.lang.Object)
     */
    public void removeElement(Object item) {
        int idx = m_items.indexOf(item);
        if ( idx >= 0 )
            removeElementAt(idx);
    }
    
    /**
     * @see swing.javax.swing.MutableComboBoxModel#removeElementAt(int)
     */
    public void removeElementAt(int idx) {
        if ( !isMultipleSelection() && idx == getMinSelectionIndex() ) {
            int sidx = ( idx==0 ? getSize()==1 ? -1 : idx+1 : idx-1 );
            Object sel = ( sidx == -1 ? null : m_items.get(sidx) );
            setSelectedItem(sel);
        }
    
        m_items.remove(idx);
        fireDataEvent(this,ListDataEvent.INTERVAL_REMOVED,idx,idx);
    }
    
    // --------------------------------------------------------------------
    // List Data Listeners
    
    /**
     * @see swing.javax.swing.ListModel#addListDataListener(swing.javax.swing.event.ListDataListener)
     */
    public void addListDataListener(ListDataListener l) {
        if ( !m_lstnrs.contains(l) )
            m_lstnrs.add(l);
    }
    
    /**
     * @see swing.javax.swing.ListModel#removeListDataListener(swing.javax.swing.event.ListDataListener)
     */
    public void removeListDataListener(ListDataListener l) {
        m_lstnrs.remove(l);
    }
    
    /**
     * Fires a change notification in response to changes in the ListModel.
     */
    protected void fireDataEvent(Object src, int type, int idx0, int idx1) {
        Object[] lstnrs = m_lstnrs.getArray();
        if ( lstnrs.length > 0 ) {
            ListDataEvent e = new ListDataEvent(src, type, idx0, idx1);
            for ( int i=0; i<lstnrs.length; ++i ) {
                ((ListDataListener)lstnrs[i]).contentsChanged(e);
            }
        }
    }

} // end of class ListModel
