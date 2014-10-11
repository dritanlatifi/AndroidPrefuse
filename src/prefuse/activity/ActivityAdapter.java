package prefuse.activity;

/**
 * Adapter class for ActivityListeners. Provides empty implementations of
 * ActivityListener routines.
 * 
 * @version 1.0
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class ActivityAdapter implements ActivityListener {

    /**
     * @see prefuse.activity.ActivityListener#activityScheduled(prefuse.activity.PActivity)
     */
    public void activityScheduled(PActivity a) {
    }

    /**
     * @see prefuse.activity.ActivityListener#activityStarted(prefuse.activity.PActivity)
     */
    public void activityStarted(PActivity a) {
    }

    /**
     * @see prefuse.activity.ActivityListener#activityStepped(prefuse.activity.PActivity)
     */
    public void activityStepped(PActivity a) {
    }

    /**
     * @see prefuse.activity.ActivityListener#activityFinished(prefuse.activity.PActivity)
     */
    public void activityFinished(PActivity a) {
    }

    /**
     * @see prefuse.activity.ActivityListener#activityCancelled(prefuse.activity.PActivity)
     */
    public void activityCancelled(PActivity a) {
    }

} // end of class ActivityAdapter
