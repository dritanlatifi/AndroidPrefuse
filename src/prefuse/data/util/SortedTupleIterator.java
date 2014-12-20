/**
 * 
 */
package prefuse.data.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import prefuse.data.Tuple;

/**
 * Iterator that provides a sorted iteration over a set of tuples.
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class SortedTupleIterator implements Iterator<Tuple> {

    private ArrayList<Tuple> m_tuples;
    private Comparator<Tuple> m_cmp;
    private Iterator<Tuple> m_iter;
    
    /**
     * Create a new SortedTupleIterator that sorts tuples in the given
     * iterator using the given comparator.
     * @param iter the source iterator of tuples
     * @param c the comparator to use for sorting
     */
    public SortedTupleIterator(Iterator<Tuple> iter, Comparator<Tuple> c) {
        this(iter, 128, c);
    }
    
    /**
     * Create a new SortedTupleIterator that sorts tuples in the given
     * iterator using the given comparator.
     * @param iter the source iterator of tuples
     * @param size the expected number of tuples in the iterator
     * @param c the comparator to use for sorting
     */
    public SortedTupleIterator(Iterator<Tuple> iter, int size, Comparator<Tuple> c) {
        m_tuples = new ArrayList<Tuple>(size);
        init(iter, c);
    }
    
    /**
     * Initialize this iterator for the given source iterator and
     * comparator.
     * @param iter the source iterator of tuples
     * @param c the comparator to use for sorting
     */
    public void init(Iterator<Tuple> iter, Comparator<Tuple> c) {
        m_tuples.clear();
        m_cmp = c;
        
        // populate tuple list
        while ( iter.hasNext() ) {
        	Tuple t = (Tuple) iter.next();
            m_tuples.add(t);
        }
        // sort tuple list
        Collections.sort(m_tuples, m_cmp);
        // create sorted iterator
        m_iter = m_tuples.iterator();
    }
    
    /**
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
        return m_iter.hasNext();
    }

    /**
     * @see java.util.Iterator#next()
     */
    public Tuple next() {
        return m_iter.next();
    }

    /**
     * Throws an UnsupportedOperationException
     * @see java.util.Iterator#remove()
     * @throws UnsupportedOperationException
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

} // end of class SortedTupleIterator