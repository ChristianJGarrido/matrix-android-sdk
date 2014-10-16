package org.matrix.matrixandroidsdk;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import org.matrix.androidsdk.MXApiClient;
import org.matrix.androidsdk.api.response.MatrixError;
import org.matrix.androidsdk.api.response.PublicRoom;
import org.matrix.androidsdk.data.RoomState;
import org.matrix.androidsdk.rest.client.EventsApiClient;
import org.matrix.matrixandroidsdk.adapters.RoomsAdapter;

import java.util.List;


/**
 * Displays a list of available public rooms.
 */
public class PublicRoomsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_rooms);

        final GridView publicRoomsGridView = (GridView) findViewById(R.id.gridView_publicRoomList);
        final RoomsAdapter adapter = new RoomsAdapter(this, R.layout.adapter_item_public_rooms);
        adapter.setAlternatingColours(0xFFFFFFFF, 0xFFEEEEEE);
        publicRoomsGridView.setAdapter(adapter);
        Matrix.getInstance(getApplicationContext()).getDefaultSession().getEventsApiClient().loadPublicRooms(new EventsApiClient.ApiCallback<List<PublicRoom>>() {
            @Override
            public void onSuccess(List<PublicRoom> publicRooms) {
                for (PublicRoom publicRoom : publicRooms) {
                    adapter.add(publicRoom);
                }
                adapter.sortRooms();
            }

            @Override
            public void onNetworkError(Exception e) {

            }

            @Override
            public void onMatrixError(MatrixError e) {

            }

            @Override
            public void onUnexpectedError(Exception e) {

            }
        });

        publicRoomsGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                RoomState room = adapter.getItem(i);
                String roomId = room.roomId;
                goToRoom(roomId);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.public_rooms, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        else if (id == R.id.action_my_rooms) {
            goToHomepage();
            return true;
        }
        else if (id == R.id.action_logout) {
            CommonActivityUtils.logout(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void goToLoginPage() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    private void goToHomepage() {
        startActivity(new Intent(this, HomeActivity.class));
    }

    private void goToRoom(String roomId) {
        Intent intent = new Intent(this, RoomActivity.class);
        intent.putExtra(RoomActivity.EXTRA_ROOM_ID, roomId);
        startActivity(intent);
    }
}