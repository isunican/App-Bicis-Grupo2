package es.unican.bicis.activities.main;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;

/**
 * Class used to adapt the functionality of an interface spinner, configuring
 * the default behavior to allow for placeholder items.
 *
 * @param <T> generic type representing the object type listed at the spinner
 */
public class HintArrayAdapter<T> extends BaseAdapter {

    /**
     * Returns the label to display for an item. This replaces
     * {@link java.util.function.Function}, which is not available on API levels below 24.
     *
     * @param <T> type of the items in the spinner
     */
    public interface LabelProvider<T> {
        String getLabel(T item);
    }

    /** Position for the placeholder item */
    private static final int HINT_POSITION = 0;

    /** Text to display at the placeholder item */
    private final String hint;

    private final Context context;
    private final List<T> items;
    private final LabelProvider<T> labelOf;

    /**
     * Constructs an adapter to handle the spinner of objects
     * @param context the application context
     * @param items list of items to be contained at the spinner
     * @param hint text to display at the placeholder item
     * @param labelOf data structure to hold the labels in relation to their corresponding object
     */
    public HintArrayAdapter(@NonNull Context context, @NonNull List<T> items, @Nullable String hint, @NonNull LabelProvider<T> labelOf) {
        this.context = context;
        this.items = items;
        this.hint = hint;
        this.labelOf = labelOf;
    }

    /**
     * Indicates whether the adapter has a placeholder item at the beginning.
     *
     * @return {@code true} if a hint was provided, {@code false} otherwise
     */
    private boolean hasHint() {
        return hint != null;
    }

    /**
     * Retrieves an item from the backing list, accounting for the placeholder offset.
     *
     * @param position adapter position of the item
     * @return the corresponding item from {@code items}
     */
    private T itemAt(int position) {
        if (hasHint()) {
            position--;
        }
        return items.get(position);
    }

    /**
     * Returns the number of rows, including the hint row when one is configured.
     *
     * @return the number of adapter rows
     * @see BaseAdapter#getCount()
     */
    @Override
    public int getCount() {
        int count = items.size();
        if (hasHint()) {
            count++;
        }
        return count;
    }

    /**
     * Returns the item at the adapter position, excluding the hint row when present.
     *
     * @param position adapter position
     * @return the corresponding item, or {@code null} for the hint row
     * @see BaseAdapter#getItem(int)
     */
    @Override
    public Object getItem(int position) {
        if (hasHint() && position == HINT_POSITION) {
            return null;
        }
        return itemAt(position);
    }

    /**
     * Uses the adapter position as the item identifier.
     *
     * @param position adapter position
     * @return the position as the item ID
     * @see BaseAdapter#getItemId(int)
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Creates or reuses the spinner's selected-item view, displaying the hint or item label.
     *
     * @param position adapter position
     * @param convertView previously created view that may be reused, or {@code null}
     * @param parent parent view
     * @return the selected-item view
     * @see BaseAdapter#getView(int, View, ViewGroup)
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return render(position, convertView, parent, android.R.layout.simple_spinner_item);
    }

    /**
     * Creates or reuses a dropdown row, displaying the hint or item label.
     *
     * @param position adapter position
     * @param convertView previously created dropdown view that may be reused, or {@code null}
     * @param parent parent view
     * @return the dropdown row view
     * @see BaseAdapter#getDropDownView(int, View, ViewGroup)
     */
    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return render(position, convertView, parent, android.R.layout.simple_spinner_dropdown_item);
    }

    /**
     * Reuses or inflates a spinner row and displays either the hint or the item's label.
     *
     * @param position adapter position to render
     * @param convertView previously rendered view that may be reused, or {@code null}
     * @param parent parent view used when inflating the row
     * @param layoutId layout resource used for the row
     * @return the rendered row view
     */
    private View render(int position, @Nullable View convertView, @NonNull ViewGroup parent, int layoutId) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(layoutId, parent, false);
        }
        TextView tv = convertView.findViewById(android.R.id.text1);
        if (hasHint() && position == HINT_POSITION) {
            tv.setText(styledHint());
        } else {
            tv.setText(labelOf.getLabel(itemAt(position)));
        }
        return convertView;
    }

    /**
     * Creates the hint text with gray color and italic styling.
     *
     * @return styled hint text
     */
    private CharSequence styledHint() {
        SpannableString styled = new SpannableString(hint);
        styled.setSpan(new StyleSpan(Typeface.ITALIC), 0, styled.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styled.setSpan(new ForegroundColorSpan(Color.GRAY), 0, styled.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return styled;
    }
}
