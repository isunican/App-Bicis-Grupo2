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
import es.unican.bicis.model.Country;

public class CountriesArrayAdapter extends BaseAdapter {

    private static final int HINT_POSITION = 0;
    private final Context context;
    private final List<Country> countries;
    private final String hint;

    public CountriesArrayAdapter(@NonNull Context context, @NonNull List<Country> countries, @Nullable String hint) {
        this.context = context;
        this.countries = countries;
        this.hint = hint;
    }
    
    private boolean hasHint() {
        return hint != null;
    }

    private Country countryAt(int position) {
        if (hasHint()) {
            position--;
        }
        return countries.get(position);
    }

    @Override
    public int getCount() {
        int count = countries.size();
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
        return countryAt(position);
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
            tv.setText(countryAt(position).getName());
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
