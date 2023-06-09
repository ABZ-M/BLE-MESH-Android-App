package no.nordicsemi.android.mesh;

import android.os.Parcel;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.room.Ignore;

/**
 * Class definition for allocating group range for provisioners.
 */
@SuppressWarnings({"unused"})
public class AllocatedSceneRange extends Range {

    @Expose
    private int firstScene;

    @Expose
    private int lastScene;

    @Override
    public final int getLowerBound() {
        return lowerBound;
    }

    @Override
    public final int getUpperBound() {
        return upperBound;
    }

    public List<Range> minus(final Range left, final Range right) {
        return null;
    }

    /**
     * Constructs {@link AllocatedSceneRange} for provisioner
     *
     * @param firstScene high address of group range
     * @param lastScene  low address of group range
     */
    public AllocatedSceneRange(final int firstScene, final int lastScene) {
        lowerBound = 0x0001;
        upperBound = 0xFFFF;
        if (firstScene < lowerBound || firstScene > upperBound)
            throw new IllegalArgumentException("firstScene value must range from 0x0000 to 0xFFFF");

        if (lastScene < lowerBound || lastScene > upperBound)
            throw new IllegalArgumentException("lastScene value must range from 0x0000 to 0xFFFF");

        /*if (firstScene > lastScene)
            throw new IllegalArgumentException("firstScene value must be lower than the lastScene value");*/

        this.firstScene = firstScene;
        this.lastScene = lastScene;
    }

    @Ignore
    AllocatedSceneRange() {
    }

    protected AllocatedSceneRange(Parcel in) {
        lowerBound = in.readInt();
        upperBound = in.readInt();
        firstScene = in.readInt();
        lastScene = in.readInt();
    }

    public static final Creator<AllocatedSceneRange> CREATOR = new Creator<AllocatedSceneRange>() {
        @Override
        public AllocatedSceneRange createFromParcel(Parcel in) {
            return new AllocatedSceneRange(in);
        }

        @Override
        public AllocatedSceneRange[] newArray(int size) {
            return new AllocatedSceneRange[size];
        }
    };

    /**
     * Returns the low address of the allocated group address
     *
     * @return low address
     */
    public int getLastScene() {
        return lastScene;
    }

    /**
     * Sets the low address of the allocated group address
     *
     * @param lastScene of the group range
     */
    public void setLastScene(final int lastScene) {
        this.lastScene = lastScene;
    }

    /**
     * Returns the high address of the allocated group range
     *
     * @return firstScene of the group range
     */
    public int getFirstScene() {
        return firstScene;
    }

    /**
     * Sets the high address of the group address
     *
     * @param firstScene of the group range
     */
    public void setFirstScene(final int firstScene) {
        this.firstScene = firstScene;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(lowerBound);
        dest.writeInt(upperBound);
        dest.writeInt(firstScene);
        dest.writeInt(lastScene);
    }

    @Override
    public int range() {
        return lastScene - firstScene;
    }

    @Override
    public boolean overlaps(@NonNull final Range otherRange) {
        if (otherRange instanceof AllocatedSceneRange) {
            final AllocatedSceneRange otherSceneRange = (AllocatedSceneRange) otherRange;
            return overlaps(firstScene, lastScene, otherSceneRange.getFirstScene(), otherSceneRange.getLastScene());
        }
        return false;
    }

    /**
     * Subtracts a range from a list of ranges
     *
     * @param ranges ranges to be subtracted
     * @param other  {@link AllocatedSceneRange} range
     * @return a resulting {@link AllocatedSceneRange} or null otherwise
     */
    @NonNull
    public static List<AllocatedSceneRange> minus(@NonNull final List<AllocatedSceneRange> ranges, @NonNull final AllocatedSceneRange other) {
        List<AllocatedSceneRange> results = new ArrayList<>();
        for (AllocatedSceneRange range : ranges) {
            results.addAll(range.minus(other));
            results = mergeSceneRanges(results);
        }
        /*ranges.clear();
        ranges.addAll(results);*/
        return results;
    }

    /**
     * Deducts a range from another
     *
     * @param other right {@link AllocatedSceneRange}
     * @return a resulting {@link AllocatedSceneRange} or null otherwise
     */
    private List<AllocatedSceneRange> minus(final AllocatedSceneRange other) {
        final List<AllocatedSceneRange> results = new ArrayList<>();
        // Left:   |------------|                    |-----------|                 |---------|
        //                  -                              -                            -
        // Right:      |-----------------|   or                     |---|   or        |----|
        //                  =                              =                            =
        // Result: |---|                             |-----------|                 |--|
        if (other.firstScene > firstScene) {
            final AllocatedSceneRange leftSlice = new AllocatedSceneRange(firstScene, (Math.min(lastScene, other.firstScene - 1)));
            results.add(leftSlice);
        }

        // Left:                |----------|             |-----------|                     |--------|
        //                         -                          -                             -
        // Right:      |----------------|           or       |----|          or     |---|
        //                         =                          =                             =
        // Result:                      |--|                      |--|                     |--------|
        if (other.lastScene < lastScene) {
            final AllocatedSceneRange rightSlice = new AllocatedSceneRange(Math.max(other.lastScene + 1, firstScene), lastScene);
            results.add(rightSlice);
        }
        return results;
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(final Object o) {
        final AllocatedSceneRange range = (AllocatedSceneRange) o;
        return firstScene == range.firstScene && lastScene == range.lastScene;
    }

    @Override
    public int hashCode() {
        int result = firstScene;
        result = 31 * result + lastScene;
        return result;
    }
}
