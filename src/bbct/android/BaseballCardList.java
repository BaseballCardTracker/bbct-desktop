/*
 * This file is part of BBCT for Android.
 *
 * Copyright 2012 codeguru <codeguru@users.sourceforge.net>
 *
 * BBCT for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BBCT for Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bbct.android;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import bbct.common.data.BaseballCard;

/**
 * TODO: Add column headers
 *
 * TODO: Make list fancier
 * 
 * TODO: Maintain previous filter when Activity is restarted
 *
 * @author codeguru <codeguru@users.sourceforge.net>
 */
public class BaseballCardList extends ListActivity {

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.card_list);

        this.sqlHelper = new BaseballCardSQLHelper(this);
        this.sqlHelper.unfilterCursor();

        Cursor cursor = this.sqlHelper.getCursor();
        this.startManagingCursor(cursor);
        this.adapter = new SimpleCursorAdapter(this, R.layout.row, cursor, ROW_PROJECTION, ROW_TEXT_VIEWS);
        this.setListAdapter(this.adapter);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        this.sqlHelper.close();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent i = new Intent(BaseballCardList.this, BaseballCardDetails.class);
        BaseballCard card = BaseballCardList.this.sqlHelper.getBaseballCardFromCursor();

        i.putExtra(AndroidConstants.BASEBALL_CARD_EXTRA, card);
        BaseballCardList.this.startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.option, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_menu:
                this.startActivity(new Intent(this, BaseballCardDetails.class));
                return true;

            case R.id.filter_menu:
                this.startActivityForResult(new Intent(this, FilterOptions.class), AndroidConstants.FILTER_OPTIONS_REQUEST);
                return true;

            case R.id.about_menu:
                this.startActivity(new Intent(this, About.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case AndroidConstants.FILTER_OPTIONS_REQUEST:
                if (resultCode == RESULT_OK) {
                    int filterRequest = data.getIntExtra(AndroidConstants.FILTER_REQUEST_EXTRA, AndroidConstants.DEFAULT_INT_EXTRA);

                    switch (filterRequest) {
                        case AndroidConstants.YEAR_FILTER_REQUEST:
                            int year = data.getIntExtra(AndroidConstants.YEAR_EXTRA, AndroidConstants.DEFAULT_INT_EXTRA);
                            this.sqlHelper.filterCursorByYear(year);
                            break;

                        case AndroidConstants.NUMBER_FILTER_REQUEST:
                            int number = data.getIntExtra(AndroidConstants.NUMBER_EXTRA, AndroidConstants.DEFAULT_INT_EXTRA);
                            this.sqlHelper.filterCursorByNumber(number);
                            break;

                        case AndroidConstants.YEAR_AND_NUMBER_FILTER_REQUEST:
                            year = data.getIntExtra(AndroidConstants.YEAR_EXTRA, AndroidConstants.DEFAULT_INT_EXTRA);
                            number = data.getIntExtra(AndroidConstants.NUMBER_EXTRA, AndroidConstants.DEFAULT_INT_EXTRA);
                            this.sqlHelper.filterCursorByYearAndNumber(year, number);
                            break;

                        case AndroidConstants.PLAYER_NAME_FILTER_REQUEST:
                            Log.d(TAG, "Filtering by player name.");
                            String playerName = data.getStringExtra(AndroidConstants.PLAYER_NAME_EXTRA);
                            Log.d(TAG, "playerName=" + playerName);
                            this.sqlHelper.filterCursorByPlayerName(playerName);
                            break;

                        default:
                            Log.e(TAG, "Invalid filter request code.");
                            // TODO: Throw an exception?
                            break;
                    }

                    Cursor cursor = this.sqlHelper.getCursor();
                    if (cursor.getCount() == 0) {
                        String msg = this.getString(R.string.no_cards_message);
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    } else {
                        BaseballCardList.this.adapter.swapCursor(cursor);
                    }
                    // TODO: Toggle Filter Cards menu button to "Turn Off Filter" or something like that
                }
                break;
        }
    }
    private static final String[] ROW_PROJECTION = {
        BaseballCardSQLHelper.BRAND_COL_NAME, BaseballCardSQLHelper.YEAR_COL_NAME,
        BaseballCardSQLHelper.NUMBER_COL_NAME, BaseballCardSQLHelper.PLAYER_NAME_COL_NAME
    };
    private static final int[] ROW_TEXT_VIEWS = {
        R.id.brand_row, R.id.year_row, R.id.number_row, R.id.player_name_row
    };
    private static final String TAG = BaseballCardList.class.getName();
    private BaseballCardSQLHelper sqlHelper = null;
    private CursorAdapter adapter = null;
}
