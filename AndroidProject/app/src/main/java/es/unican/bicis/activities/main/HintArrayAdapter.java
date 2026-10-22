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
import java.util.function.Function;

/**
 * Class used to adapt the functionality of an interface spinner, configuring
 * the default behavior to allow for placeholder items.
 *
 * @param <T> generic type representing the object type listed at the spinner
 */
public class HintArrayAdapter<T> extends BaseAdapter {

    /** Position for the placeholder item */
    private static final int HINT_POSITION = 0;

    /** Text to display at the placeholder item */
    private final String hint;

    private final Context context;
    private final List<T> items;
    private final Function<T, String> labelOf;

    /**
     * Constructs an adapter to handle the spinner of objects
     * @param context the application context
     * @param items list of items to be contained at the spinner
     * @param hint text to display at the placeholder item
     * @param labelOf data structure to hold the labels in relation to their corresponding object
     */
    public HintArrayAdapter(@NonNull Context context, @NonNull List<T> items, @Nullable String hint, @NonNull Function<T, String> labelOf) {
        this.context = context;
        this.items = items;
        this.hint = hint;
        this.labelOf = labelOf;
    }

    private boolean hasHint() {
        return hint != null;
    }

    private T itemAt(int position) {
        if (hasHint()) {
            position--;
        }
        return items.get(position);
    }

    @Override
    public int getCount() {
        int count = items.size();
        if (hasHint()) {
            count++;
        }
        return count;
    }

    @Override
    public Object getItem(int position) {
        if (hasHint() && position == HINT_POSITION) {
            return null;
        }
        return itemAt(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return render(position, convertView, parent, android.R.layout.simple_spinner_item);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return render(position, convertView, parent, android.R.layout.simple_spinner_dropdown_item);
    }

    private View render(int position, @Nullable View convertView, @NonNull ViewGroup parent, int layoutId) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(layoutId, parent, false);
        }
        TextView tv = convertView.findViewById(android.R.id.text1);
        if (hasHint() && position == HINT_POSITION) {
            tv.setText(styledHint());
        } else {
            tv.setText(labelOf.apply(itemAt(position)));
        }
        return convertView;
    }

    private CharSequence styledHint() {
        SpannableString styled = new SpannableString(hint);
        styled.setSpan(new StyleSpan(Typeface.ITALIC), 0, styled.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styled.setSpan(new ForegroundColorSpan(Color.GRAY), 0, styled.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return styled;
    }
}
