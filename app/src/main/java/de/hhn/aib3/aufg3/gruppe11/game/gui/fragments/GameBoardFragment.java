package de.hhn.aib3.aufg3.gruppe11.game.gui.fragments;


import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.net.HttpURLConnection;
import java.util.Arrays;

import de.hhn.aib3.aufg3.gruppe11.R;
import de.hhn.aib3.aufg3.gruppe11.game.elements.placement.BattlePreset;
import de.hhn.aib3.aufg3.gruppe11.game.elements.game.Cell;
import de.hhn.aib3.aufg3.gruppe11.game.elements.placement.EmptyPreset;
import de.hhn.aib3.aufg3.gruppe11.game.elements.placement.Preset;
import de.hhn.aib3.aufg3.gruppe11.game.enums.Orientation;
import de.hhn.aib3.aufg3.gruppe11.game.enums.ShipType;
import de.hhn.aib3.aufg3.gruppe11.game.enums.GEvent;
import de.hhn.aib3.aufg3.gruppe11.game.enums.NextMove;
import de.hhn.aib3.aufg3.gruppe11.game.gui.CustomShadowBuilder;
import de.hhn.aib3.aufg3.gruppe11.game.gui.elements.Marker;
import de.hhn.aib3.aufg3.gruppe11.game.gui.elements.SelectionField;
import de.hhn.aib3.aufg3.gruppe11.game.gui.elements.ShipField;
import de.hhn.aib3.aufg3.gruppe11.game.elements.game.Game;
import de.hhn.aib3.aufg3.gruppe11.game.elements.game.GameEvent;
import de.hhn.aib3.aufg3.gruppe11.rest.Event;
import de.hhn.aib3.aufg3.gruppe11.rest.GeneralEvent;
import de.hhn.aib3.aufg3.gruppe11.rest.OfflineEvent;
import de.hhn.aib3.aufg3.gruppe11.rest.RestEvent;
import de.hhn.aib3.aufg3.gruppe11.rest.RestService;
import de.hhn.aib3.aufg3.gruppe11.utility.Utility;

import static android.R.attr.width;

/**
 * Game board for each player
 */
public class GameBoardFragment extends Fragment {

    private static final int GAME_BOARD_SIZE = 10;
    private TableLayout gameGrid = null;

    private RestService restService = null;
    private Game game = null;
    private LinearLayout linearLayout = null;
    private boolean shipPreset = false;
    private boolean initiator = false;
    private boolean initiatorField = false;

    private boolean init = false;
    private boolean selected = false;

    private NextMove move = NextMove.NONE;

    //TODO: Offline Impl
    private boolean offlineMode = false;
    private boolean test = false;


    private static final String DEBUGLOG_TAG = "DEBUGLOG-GBF";

    @Override
    public void onAttach(Context context) {
        Log.d(DEBUGLOG_TAG, "GameBoardFragment attach");
        EventBus.getDefault().register(this);
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        Log.d(DEBUGLOG_TAG, "GameBoardFragment detach");
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        init = true;
        selected = true;
        Log.d(DEBUGLOG_TAG, "GameBoardFragment started");
    }

    @Override
    public void onStop() {
        super.onStop();
        selected = false;
        Log.d(DEBUGLOG_TAG, "GameBoardFragment stopped");
    }

    @Override
    public void onDestroy() {
        Log.d(DEBUGLOG_TAG, "GameBoardFragment destroyed");
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View myFragmentView = inflater.inflate(R.layout.fragment_game_board, container, false);

        shipPreset = false;

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            shipPreset = bundle.getBoolean(getString(R.string.extra_shipPreset), false);
            initiator = bundle.getBoolean(getString(R.string.extra_initiator), false);
            initiatorField = bundle.getBoolean(getString(R.string.extra_initiatorField), false);
            restService = (RestService) bundle.getSerializable(getString(R.string.extra_restService));
            game = (Game) bundle.getSerializable(getString(R.string.extra_game));
            //TODO: Offline Impl
            offlineMode = bundle.getBoolean(getString(R.string.extra_offline), false);
            test = bundle.getBoolean("test", false);
        } else {
            Log.d(DEBUGLOG_TAG, "Bundle null");
        }

        linearLayout = myFragmentView.findViewById(R.id.fragment_game_board_table_wrapper);

        if (shipPreset) {
            Log.d(DEBUGLOG_TAG, "Preset Ships");
            Preset battlePreset = new BattlePreset();
            buildGameBoard(battlePreset.toByteArray());
        }

        else if(offlineMode){
            if(test){
                Log.d("TEST--", "test is true");
                offlineUpdateCurrentGameBoard(game.getBoardP1());
            }else{
                Log.d("TEST--", "test is false");
                offlineUpdateCurrentGameBoard(game.getBoardP2());
            }
            //TODO: Here lies the problem
//            Preset emptyPreset = new EmptyPreset();
//            buildGameBoard(emptyPreset.toByteArray());
        }

        else {
            updateCurrentGameBoard(game);
        }

        return myFragmentView;
    }

    /**
     * ClickListener for ShipFields
     */
    private final class ShipFieldClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            rotateShip((ShipField) view);
        }
    }

    /**
     * LongClickListener for ShipFields
     */
    private final class ShipFieldLongClickListener implements View.OnLongClickListener {
        @Override
        public boolean onLongClick(View view) {
            initiateDrag((ShipField) view);
            return true;
        }
    }


    private class MyDragListener implements View.OnDragListener {

        @Override
        public boolean onDrag(View v, DragEvent event) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    // do nothing
                    break;

                case DragEvent.ACTION_DRAG_ENTERED:
                    break;

                case DragEvent.ACTION_DRAG_EXITED:
                    break;

                case DragEvent.ACTION_DROP:
                    FrameLayout container = (FrameLayout) v;

                    Cell dropField = null;

                    for (int i = 0; i < GAME_BOARD_SIZE; i++) {
                        TableRow row = (TableRow) gameGrid.getChildAt(i);
                        for (int j = 0; j < GAME_BOARD_SIZE; j++) {
                            FrameLayout frameLayout = (FrameLayout) row.getChildAt(j);
                            if (container.getId() == frameLayout.getId()) {
                                dropField = new Cell(i, j);
                            }
                        }
                    }

                    if (dropField != null) {
                        finalizeDrag(dropField, (ShipField) event.getLocalState());
                    }
                    break;

                case DragEvent.ACTION_DRAG_ENDED:
                    break;

                default:
                    break;
            }
            return true;
        }
    }

    /**
     * Turns Ship (All {@link ShipField} of same type) by 90 degrees provided that new position is valid.
     *
     * @param selectedShip {@link ShipField} in original position
     */
    private void rotateShip(@NonNull ShipField selectedShip) {
        ShipField newShip;
        if (selectedShip.getOrientation() == Orientation.HORIZONTALLY) {
            newShip = new ShipField(getActivity(), null, selectedShip.getShipType(), Orientation.VERTICALLY, selectedShip.getPosition());
        } else {
            newShip = new ShipField(getActivity(), null, selectedShip.getShipType(), Orientation.HORIZONTALLY, selectedShip.getPosition());
        }

        Cell headCell = null;
        boolean firstInstance = true;
        //find head cell
        if (gameGrid != null) {
            for (int i = 0; i < GAME_BOARD_SIZE; i++) {
                TableRow row = (TableRow) gameGrid.getChildAt(i);
                for (int j = 0; j < GAME_BOARD_SIZE; j++) {
                    FrameLayout frameLayout = (FrameLayout) row.getChildAt(j);
                    for (int k = 0; k < frameLayout.getChildCount(); k++) {
                        if (frameLayout.getChildAt(k) instanceof ShipField) {
                            ShipField shipField = (ShipField) frameLayout.getChildAt(k);
                            if ((shipField.getShipType() == selectedShip.getShipType()) && firstInstance) {
                                headCell = new Cell(i, j);
                                firstInstance = false;
                            }
                        }
                    }
                }
            }
        }

        if (headCell != null) {
            if (isValid(newShip, headCell)) {
                if (gameGrid != null) {

                    //Remove
                    for (int i = 0; i < GAME_BOARD_SIZE; i++) {
                        TableRow row = (TableRow) gameGrid.getChildAt(i);
                        for (int j = 0; j < GAME_BOARD_SIZE; j++) {
                            FrameLayout frameLayout = (FrameLayout) row.getChildAt(j);
                            for (int k = 0; k < frameLayout.getChildCount(); k++) {
                                if (frameLayout.getChildAt(k) instanceof ShipField) {
                                    ShipField tempShip = (ShipField) frameLayout.getChildAt(k);
                                    if (tempShip.getShipType() == newShip.getShipType()) {
                                        ViewGroup owner = (ViewGroup) tempShip.getParent();
                                        owner.removeView(tempShip);
                                        Log.d(DEBUGLOG_TAG, "ShipField Element Removed from Placement Field");
                                    }
                                }
                            }
                        }
                    }

                    //Replace
                    for (int i = 0; i < newShip.getShipType().getLength(); i++) {
                        if (newShip.getOrientation() == Orientation.HORIZONTALLY) {
                            TableRow row = (TableRow) gameGrid.getChildAt(headCell.getRow());
                            FrameLayout frameLayout = (FrameLayout) row.getChildAt(headCell.getColumn() + i);
                            Cell currentPos = new Cell(headCell.getRow(), headCell.getColumn() + i);
                            ShipField shipField = new ShipField(getActivity(), null, newShip.getShipType(), newShip.getOrientation(), currentPos);
                            shipField.setOnLongClickListener(new ShipFieldLongClickListener());
                            shipField.setOnClickListener(new ShipFieldClickListener());
                            frameLayout.addView(shipField);
                        } else {
                            TableRow row = (TableRow) gameGrid.getChildAt(headCell.getRow() + i);
                            FrameLayout frameLayout = (FrameLayout) row.getChildAt(headCell.getColumn());
                            Cell currentPos = new Cell(headCell.getRow() + i, headCell.getColumn());
                            ShipField shipField = new ShipField(getActivity(), null, newShip.getShipType(), newShip.getOrientation(), currentPos);
                            shipField.setOnLongClickListener(new ShipFieldLongClickListener());
                            shipField.setOnClickListener(new ShipFieldClickListener());
                            frameLayout.addView(shipField);
                        }
                    }
                }

            }
        }
    }


    /**
     * Starts drag and creates a CustomShadowBuilder for selectedShip
     *
     * @param selectedShip any {@link ShipField} with a certain {@link ShipType} which constitutes the ship that is supposed to be dragged
     *                     {@link ShipField} objects which hold the same {@link ShipType} will be dragged
     *                     {@link ShipField} objects which hold the same {@link ShipType} will become invisible
     */
    private void initiateDrag(@NonNull ShipField selectedShip) {
        if (shipPreset) {
            ClipData data = ClipData.newPlainText("", "");
            CustomShadowBuilder customShadowBuilder = new CustomShadowBuilder(selectedShip);
            selectedShip.startDrag(data, customShadowBuilder, selectedShip, 0);

            ShipType currentState = selectedShip.getShipType();

            if (gameGrid != null) {
                for (int i = 0; i < GAME_BOARD_SIZE; i++) {
                    TableRow row = (TableRow) gameGrid.getChildAt(i);
                    for (int j = 0; j < GAME_BOARD_SIZE; j++) {
                        FrameLayout frameLayout = (FrameLayout) row.getChildAt(j);
                        if (frameLayout.getChildCount() != 0) {
                            ShipField shipField = (ShipField) frameLayout.getChildAt(0);
                            if (currentState == shipField.getShipType()) {
                                shipField.setVisibility(View.INVISIBLE);
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * Redraws the ship (All {@link ShipField} of same type) at new positions provided that
     * all new positions of  {@link ShipField} are valid.
     * Otherwise make {@link ShipField}, that were previously set invisible, visible
     *
     * @param dropField   {@link Cell} inside {@link #gameGrid} in which ShipField was dropped
     * @param draggedShip {@link ShipField} which was dragged by client
     */
    private void finalizeDrag(@NonNull Cell dropField, @NonNull ShipField draggedShip) {
        if (isValid(draggedShip, dropField)) {
            Log.d(DEBUGLOG_TAG, "shadow valid");

            ShipType currentState = draggedShip.getShipType();

            if (gameGrid != null) {
                for (int i = 0; i < GAME_BOARD_SIZE; i++) {
                    TableRow row = (TableRow) gameGrid.getChildAt(i);
                    for (int j = 0; j < GAME_BOARD_SIZE; j++) {
                        FrameLayout frameLayout = (FrameLayout) row.getChildAt(j);
                        if (frameLayout.getChildCount() != 0) {
                            ShipField shipField = (ShipField) frameLayout.getChildAt(0);
                            if (currentState == shipField.getShipType()) {
                                ViewGroup owner = (ViewGroup) shipField.getParent();
                                owner.removeView(shipField);
                                Log.d(DEBUGLOG_TAG, "View removed");
                            }
                        }
                    }
                }
            }

            for (int i = 0; i < draggedShip.getShipType().getLength(); i++) {
                if (draggedShip.getOrientation() == Orientation.HORIZONTALLY) {
                    TableRow row = (TableRow) gameGrid.getChildAt(dropField.getRow());
                    FrameLayout frameLayout = (FrameLayout) row.getChildAt(dropField.getColumn() + i);
                    Cell currentPos = new Cell(dropField.getRow(), dropField.getColumn() + i);
                    ShipField shipField = new ShipField(getActivity(), null, draggedShip.getShipType(), draggedShip.getOrientation(), currentPos);
                    shipField.setOnLongClickListener(new ShipFieldLongClickListener());
                    shipField.setOnClickListener(new ShipFieldClickListener());
                    frameLayout.addView(shipField);
                } else {
                    TableRow row = (TableRow) gameGrid.getChildAt(dropField.getRow() + i);
                    FrameLayout frameLayout = (FrameLayout) row.getChildAt(dropField.getColumn());
                    Cell currentPos = new Cell(dropField.getRow() + i, dropField.getColumn());
                    ShipField shipField = new ShipField(getActivity(), null, draggedShip.getShipType(), draggedShip.getOrientation(), currentPos);
                    shipField.setOnLongClickListener(new ShipFieldLongClickListener());
                    shipField.setOnClickListener(new ShipFieldClickListener());
                    frameLayout.addView(shipField);
                }
            }

        } else {
            Log.d(DEBUGLOG_TAG, "shadow invalid");
            ShipType currentState = draggedShip.getShipType();

            if (gameGrid != null) {
                for (int i = 0; i < GAME_BOARD_SIZE; i++) {
                    TableRow row = (TableRow) gameGrid.getChildAt(i);
                    for (int j = 0; j < GAME_BOARD_SIZE; j++) {
                        FrameLayout frameLayout = (FrameLayout) row.getChildAt(j);
                        if (frameLayout.getChildCount() != 0) {
                            ShipField shipField = (ShipField) frameLayout.getChildAt(0);
                            if (currentState == shipField.getShipType()) {
                                shipField.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * Deletes current game grid and creates new game grid according to grid param.
     *
     * @param gameBoard Length must equal GAME_BOARD_SIZE squared
     *                  Valid byte values:
     *                  10 - Water
     *                  11 - Water hit
     *                  20 - Ship of length 2
     *                  21 - Ship of length 2 which was hit
     *                  30 - Ship of length 3
     *                  31 - Ship of length 3 which was hit
     *                  40 - Ship of length 4
     *                  41 - Ship of length 4 which was hit
     *                  50 - Ship of length 5
     *                  51 - Ship of length 5 which was hit
     */
    private void buildGameBoard(byte[] gameBoard) {

        Log.d(DEBUGLOG_TAG, "remove View");
        linearLayout.removeAllViews();

        gameGrid = new TableLayout(getActivity());
        gameGrid.setBackgroundColor(Color.GREEN);
        gameGrid.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, width));

        for (int i = 0; i < GAME_BOARD_SIZE; i++) {
            TableRow row = new TableRow(getActivity());
            for (int j = 0; j < GAME_BOARD_SIZE; j++) {

                //battlefield
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                final FrameLayout fragment = new FrameLayout(getActivity());
                if (!shipPreset) {
                    if ((initiator && !initiatorField) || (!initiator && initiatorField) || offlineMode) {
                        fragment.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Log.d(DEBUGLOG_TAG, "Clicked fragment at: ");
                                if (gameGrid != null) {
                                    for (int i = 0; i < GAME_BOARD_SIZE; i++) {
                                        TableRow row = (TableRow) gameGrid.getChildAt(i);
                                        for (int j = 0; j < GAME_BOARD_SIZE; j++) {
                                            FrameLayout frameLayout = (FrameLayout) row.getChildAt(j);
                                            if (frameLayout.getChildCount() != 0) {
                                                for (int k = 0; k < frameLayout.getChildCount(); k++) {
                                                    if (frameLayout.getChildAt(k) instanceof SelectionField) {
                                                        frameLayout.removeViewAt(k);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                fragment.addView(new SelectionField(getActivity(), null));
                            }
                        });
                    }
                }
                fragment.setId(View.generateViewId());
                //water
                if ((i % 2 == 0 && j % 2 == 0) || (i % 2 != 0 && j % 2 != 0)) {
                    fragment.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorWater1));
                } else {
                    fragment.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorWater2));
                }
                row.addView(fragment, displayMetrics.widthPixels / GAME_BOARD_SIZE, displayMetrics.widthPixels / GAME_BOARD_SIZE);

                fragment.setOnDragListener(new MyDragListener());

                //ships / hits
                ShipField shipField = null;
                switch (gameBoard[i * 10 + j]) {

                    case Utility.WATER:
                        //do nothing
                        break;

                    case Utility.MISS:
                        fragment.addView(new Marker(getActivity(), Color.BLACK));
                        break;

                    case Utility.SHIP_2:
                        if(shipPreset){
                            shipField = new ShipField(getActivity(), null, ShipType.BOAT2, Utility.getShipOrientation(gameBoard, Utility.SHIP_2), new Cell(i, j));
                            fragment.addView(shipField);
                        }else{
                            fragment.addView(new ShipField(getActivity(), null, ShipType.BOAT2));
                        }
                        break;

                    case Utility.SHIP_2_HIT:
                        fragment.addView(new ShipField(getActivity(), null, ShipType.BOAT2));
                        fragment.addView(new Marker(getActivity(), Color.RED));
                        break;

                    case Utility.SHIP_3:
                        if(shipPreset){
                            shipField = new ShipField(getActivity(), null, ShipType.BOAT3, Utility.getShipOrientation(gameBoard, Utility.SHIP_3), new Cell(i, j));
                            fragment.addView(shipField);
                        }else{
                            fragment.addView(new ShipField(getActivity(), null, ShipType.BOAT3));
                        }
                        break;

                    case Utility.SHIP_3_HIT:
                        fragment.addView(new ShipField(getActivity(), null, ShipType.BOAT3));
                        fragment.addView(new Marker(getActivity(), Color.RED));
                        break;

                    case Utility.SHIP_4:
                        if(shipPreset){
                            shipField = new ShipField(getActivity(), null, ShipType.BOAT4, Utility.getShipOrientation(gameBoard, Utility.SHIP_4), new Cell(i, j));
                            fragment.addView(shipField);
                        }else{
                            fragment.addView(new ShipField(getActivity(), null, ShipType.BOAT4));
                        }
                        break;

                    case Utility.SHIP_4_HIT:
                        fragment.addView(new ShipField(getActivity(), null, ShipType.BOAT4));
                        fragment.addView(new Marker(getActivity(), Color.RED));
                        break;

                    case Utility.SHIP_5:
                        if(shipPreset){
                            shipField = new ShipField(getActivity(), null, ShipType.BOAT5, Utility.getShipOrientation(gameBoard, Utility.SHIP_5), new Cell(i, j));
                            fragment.addView(shipField);
                        }else{
                            fragment.addView(new ShipField(getActivity(), null, ShipType.BOAT5));
                        }
                        break;

                    case Utility.SHIP_5_HIT:
                        fragment.addView(new ShipField(getActivity(), null, ShipType.BOAT5));
                        fragment.addView(new Marker(getActivity(), Color.RED));
                        break;
                }

                if (shipPreset && shipField != null) {
                    shipField.setOnLongClickListener(new ShipFieldLongClickListener());
                    shipField.setOnClickListener(new ShipFieldClickListener());
                }

            }
            gameGrid.addView(row);
        }
        linearLayout.addView(gameGrid);
    }

    /**
     * Conversion of {@code gameGrid} to byte array
     *
     * @return byte array of length GAME_BOARD_SIZE squared
     */
    public byte[] getGameGrid() {
        byte[] byteGrid = new byte[GAME_BOARD_SIZE * GAME_BOARD_SIZE];

        for (int i = 0; i < GAME_BOARD_SIZE; i++) {
            TableRow row = (TableRow) gameGrid.getChildAt(i);
            for (int j = 0; j < GAME_BOARD_SIZE; j++) {

                int listIndex = i * 10 + j;
                FrameLayout frameLayout = (FrameLayout) row.getChildAt(j);

                ShipField shipField = null;
                Marker marker = null;

                for (int k = 0; k < frameLayout.getChildCount(); k++) {

                    if (frameLayout.getChildAt(k) instanceof ShipField) {
                        Log.d(DEBUGLOG_TAG, "child instance of ship field");
                        shipField = (ShipField) frameLayout.getChildAt(k);
                    } else if (frameLayout.getChildAt(k) instanceof Marker) {
                        Log.d(DEBUGLOG_TAG, "child instance of marker");
                        Log.d(DEBUGLOG_TAG, "");
                        marker = (Marker) frameLayout.getChildAt(k);
                    }
                }

                if (shipField != null) {

                    switch (shipField.getShipType()) {

                        case BOAT2:
                            if (marker != null) {
                                byteGrid[listIndex] = Utility.SHIP_2_HIT;
                            } else {
                                byteGrid[listIndex] = Utility.SHIP_2;
                            }
                            break;

                        case BOAT3:
                            if (marker != null) {
                                byteGrid[listIndex] = Utility.SHIP_3_HIT;
                            } else {
                                byteGrid[listIndex] = Utility.SHIP_3;
                            }
                            break;

                        case BOAT4:
                            if (marker != null) {
                                byteGrid[listIndex] = Utility.SHIP_4_HIT;
                            } else {
                                byteGrid[listIndex] = Utility.SHIP_4;
                            }
                            break;

                        case BOAT5:
                            if (marker != null) {
                                byteGrid[listIndex] = Utility.SHIP_5_HIT;
                            } else {
                                byteGrid[listIndex] = Utility.SHIP_5;
                            }
                            break;
                    }
                } else {
                    if (marker != null) {
                        byteGrid[listIndex] = Utility.MISS;
                    } else {
                        byteGrid[listIndex] = Utility.WATER;
                    }
                }

            }
        }
        return byteGrid;
    }

    /**
     * Updates selected game board
     *
     * @param game instance of current game
     */
    public void updateCurrentGameBoard(Game game) {
        if (initiatorField) {
            Log.d("TEST--","Initiator Field TRUE");
            buildGameBoard(game.getBoardP1());
        } else {
            Log.d("TEST--","Initiaror Field FALSE");
            buildGameBoard(game.getBoardP2());
        }
    }

    public void offlineUpdateCurrentGameBoard(byte[] board){
        buildGameBoard(board);
    }

    /**
     * Determines whether the position of a ship conflicts with another ship or is out of the
     * bounds of the {@link #gameGrid} when turned or dragged
     *
     * @param shipField any {@link ShipField} with a certain {@link ShipType} which constitutes the ship that's supposed to be validated
     * @param dropField {@link Cell} inside {@link #gameGrid} in which ShipField was dropped
     * @return true -> legal position
     * position of ship not in conflict and inside bounds of {@link #gameGrid}
     * false -> illegal position
     * position in conflict with another ship or out of bounds of {@link #gameGrid}
     */
    private boolean isValid(ShipField shipField, Cell dropField) {

        Log.d(DEBUGLOG_TAG, "called is valid");

        if (gameGrid != null) {
            Orientation orientation = shipField.getOrientation();
            int shipLength = shipField.getShipType().getLength();

            Log.d(DEBUGLOG_TAG, "Drop Field Pos: row: " + dropField.getRow() + " column: " + dropField.getColumn());
            Log.d(DEBUGLOG_TAG, "Ship length: " + shipLength);

            int max = GAME_BOARD_SIZE - 1;

            //outside boundaries
            if (orientation == Orientation.HORIZONTALLY) {
                Log.d(DEBUGLOG_TAG, "Horizontally");
                if (dropField.getColumn() + (shipLength - 1) > max) {
                    Toast.makeText(getActivity(), R.string.out_of_battlefield, Toast.LENGTH_SHORT).show();
                    return false;
                }

                for (int i = 0; i < shipLength; i++) {
                    TableRow row = (TableRow) gameGrid.getChildAt(dropField.getRow());
                    FrameLayout frameLayout = (FrameLayout) row.getChildAt(dropField.getColumn() + i);
                    if (frameLayout.getChildCount() != 0) {
                        ShipField sf = (ShipField) frameLayout.getChildAt(0);
                        if (shipField.getShipType() != sf.getShipType()) {
                            Toast.makeText(getActivity(), R.string.no_ship_stacking, Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    }
                }
            } else {
                Log.d(DEBUGLOG_TAG, "Vertically");
                if (dropField.getRow() + (shipLength - 1) > max) {
                    Toast.makeText(getActivity(), R.string.out_of_battlefield, Toast.LENGTH_SHORT).show();
                    return false;
                }
                for (int i = 0; i < shipLength; i++) {
                    TableRow row = (TableRow) gameGrid.getChildAt(dropField.getRow() + i);
                    FrameLayout frameLayout = (FrameLayout) row.getChildAt(dropField.getColumn());
                    if (frameLayout.getChildCount() != 0) {
                        ShipField sf = (ShipField) frameLayout.getChildAt(0);
                        if (shipField.getShipType() != sf.getShipType() ||
                                frameLayout.getChildCount() != 1) {
                            Toast.makeText(getActivity(), R.string.no_ship_stacking, Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    }
                }
            }
            Log.d(DEBUGLOG_TAG, "no conflict");
            return true;
        }
        Log.d(DEBUGLOG_TAG, "gameGrid null");
        return false;
    }

    @Subscribe
    public void onEvent(GeneralEvent gameEvent) {

        Log.d(DEBUGLOG_TAG, "General Event triggered");

        RestEvent restEvent = null;
        OfflineEvent offlineEvent = null;
        Event event = gameEvent.getEvent();
        String move2 = null;
        byte[] correctBoard = new byte[0];

        Game game = gameEvent.getGame();
        int responseCode = -1;

        if(gameEvent instanceof RestEvent){
            restEvent = (RestEvent) gameEvent;
            responseCode = restEvent.getResponseCode();
        }else if(gameEvent instanceof OfflineEvent){
            offlineEvent = (OfflineEvent) gameEvent;
            move2 = offlineEvent.getMove();
            correctBoard = offlineEvent.getBoard();
        }

        switch (event) {
            case STATE:
                if (!shipPreset) {
                    if(restEvent != null) {
                        if (restEvent.isWsConnected()) {
                            Log.d(DEBUGLOG_TAG, "NOT OFFLINE MODES");
                            if (responseCode == HttpURLConnection.HTTP_OK) {
                                if (!move.toString().equals(game.getNextMove())) {
                                    Log.d(DEBUGLOG_TAG, "Current Move: " + move.toString());
                                    Log.d(DEBUGLOG_TAG, "Next Move: " + game.getNextMove());
                                    updateCurrentGameBoard(game);

                                    TextView clientName = getActivity().findViewById(R.id.fragment_game_client_text_view);
                                    TextView opponentName = getActivity().findViewById(R.id.fragment_game_opponent_text_view);
                                    FloatingActionButton firingButton = getActivity().findViewById(R.id.fragment_game_save_button);

                                    if (game.getNextMove().equals(NextMove.INITIATOR.toString())) {
                                        if (!init && !shipPreset && selected) {
                                            Utility.play(R.raw.water, getActivity());
                                        }
                                        move = NextMove.INITIATOR;
                                        if (initiator) {
                                            clientName.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
                                            opponentName.setTextColor(Color.BLACK);
                                            firingButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorAccent)));
                                            firingButton.setClickable(true);
                                        } else {
                                            clientName.setTextColor(Color.BLACK);
                                            opponentName.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
                                            firingButton.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                                            firingButton.setClickable(false);
                                        }
                                    } else if (game.getNextMove().equals(NextMove.P2.toString())) {
                                        if (!init && !shipPreset && selected) {
                                            Utility.play(R.raw.water, getActivity());
                                        }
                                        move = NextMove.P2;
                                        if (!initiator) {
                                            clientName.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
                                            opponentName.setTextColor(Color.BLACK);
                                            firingButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorAccent)));
                                            firingButton.setClickable(true);
                                        } else {
                                            clientName.setTextColor(Color.BLACK);
                                            opponentName.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
                                            firingButton.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                                            firingButton.setClickable(false);
                                        }
                                    } else {
                                        move = NextMove.NONE;
                                        if (game.getWinner() != null) {
                                            String msg;
                                            if ((initiator && game.getWinner().equals(game.getInitiator()))
                                                    || (!initiator && game.getWinner().equals(game.getPlayer2()))) {
                                                msg = getString(R.string.winner);
                                                Utility.play(R.raw.win, getActivity());
                                            } else {
                                                msg = getString(R.string.Loser);
                                                Utility.play(R.raw.losing, getActivity());
                                            }
                                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                                            alertDialog.setTitle(msg);
                                            alertDialog.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    EventBus.getDefault().unregister(this);
                                                    getActivity().finish();
                                                }
                                            });
                                            alertDialog.show();
                                        }
                                    }
                                } else {
                                    if (!Arrays.equals(this.game.getBoardP1(), game.getBoardP1()) || !Arrays.equals(this.game.getBoardP2(), game.getBoardP2())) {
                                        updateCurrentGameBoard(game);
                                        if (!init && selected) {
                                            Utility.detectHit(this.game, game, getActivity());
                                        }
                                    }
                                }
                                init = false;
                                this.game = game;
                            } else if (responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
                                Toast.makeText(getActivity(), R.string.jwt_token_expired, Toast.LENGTH_LONG).show();
                                Utility.backToLogin(getActivity());
                            } else {
                                Toast.makeText(getActivity(), R.string.failed_to_update_game, Toast.LENGTH_LONG).show();
                                Utility.backToLogin(getActivity());
                            }
                        }

                    } else if(offlineMode){

                        Log.d(DEBUGLOG_TAG, "OFFLINE MODE");
                        offlineUpdateCurrentGameBoard(correctBoard);

//                        if(move2.equals(Utility.INITIATOR_TAB_TAG)){
//                            Log.d("TEST--", "ACHTUNG INITIATOR");
//                            offlineUpdateCurrentGameBoard(game.getBoardP1());
//                        }else if(move2.equals(Utility.PLAYER2_TAB_TAG)){
//                            Log.d("TEST--", "ACHTUNG PLAYER2");
//                            offlineUpdateCurrentGameBoard(game.getBoardP2());
//                        }



//                        this.game = game;
//                        updateCurrentGameBoard(game);

//                        this.game = game;

//                        if (!move.toString().equals(game.getNextMove())) {
//                            Log.d(DEBUGLOG_TAG, "Current Move: " + move.toString());
//                            Log.d(DEBUGLOG_TAG, "Next Move: " + game.getNextMove());
////                            updateCurrentGameBoard(game);
//
//                            TextView clientName = getActivity().findViewById(R.id.fragment_game_client_text_view);
//                            TextView opponentName = getActivity().findViewById(R.id.fragment_game_opponent_text_view);
//
//                            if (game.getNextMove().equals(NextMove.INITIATOR.toString())) {
//                                if (!init && !shipPreset && selected) {
//                                    Utility.play(R.raw.water, getActivity());
//                                }
//                                move = NextMove.INITIATOR;
//                                if (initiator) {
//                                    clientName.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
//                                    opponentName.setTextColor(Color.BLACK);
//                                } else {
//                                    clientName.setTextColor(Color.BLACK);
//                                    opponentName.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
//                                }
//                            } else if (game.getNextMove().equals(NextMove.P2.toString())) {
//                                if (!init && !shipPreset && selected) {
//                                    Utility.play(R.raw.water, getActivity());
//                                }
//                                move = NextMove.P2;
//                                if (!initiator) {
//                                    clientName.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
//                                    opponentName.setTextColor(Color.BLACK);
//                                } else {
//                                    clientName.setTextColor(Color.BLACK);
//                                    opponentName.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
//                                }
//                            } else {
//                                move = NextMove.NONE;
//                                if (game.getWinner() != null) {
//                                    String msg;
//                                    if ((initiator && game.getWinner().equals(game.getInitiator()))
//                                            || (!initiator && game.getWinner().equals(game.getPlayer2()))) {
//                                        msg = getString(R.string.winner);
//                                        Utility.play(R.raw.win, getActivity());
//                                    } else {
//                                        msg = getString(R.string.Loser);
//                                        Utility.play(R.raw.losing, getActivity());
//                                    }
//                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
//                                    alertDialog.setTitle(msg);
//                                    alertDialog.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialog, int which) {
//                                            EventBus.getDefault().unregister(this);
//                                            getActivity().finish();
//                                        }
//                                    });
//                                    alertDialog.show();
//                                }
//                            }
//                        }

//                        else {
//                            Log.d("TEST--", "SO FAR");
//                            if(Arrays.equals(this.game.getBoardP1(), game.getBoardP1()) || Arrays.equals(this.game.getBoardP2(), game.getBoardP2())){
//                                Log.d("TEST--", "SO FAR ARRAYS EQUAL");
//                                if(this.game.getBoardP1() == null){
//                                    Log.d("TEST--", "Cause They are both null");
//                                }
//                            }
//                            if (!Arrays.equals(this.game.getBoardP1(), game.getBoardP1()) || !Arrays.equals(this.game.getBoardP2(), game.getBoardP2())) {
//                                Log.d("TEST--", "HURA THIS WORKS SO FAR");
//                                updateCurrentGameBoard(game);
//                                Utility.detectHit(this.game, game, getActivity());
////                                if (!init && selected) {
////                                if ((!init && selected) || offlineMode) {
////                                    Utility.detectHit(this.game, game, getActivity());
////                                }
//                            }
//                        }
//                        init = false;
//                        this.game = game;




                    } else {
                        Toast.makeText(getActivity(), R.string.connection_failed, Toast.LENGTH_SHORT).show();
                        Utility.backToLogin(getActivity());
                    }
                }
                break;

            case SHOOT:
                if (restEvent.isWsConnected()) {
                    if (responseCode == HttpURLConnection.HTTP_CONFLICT) {
                        Toast.makeText(getActivity(), R.string.fire_on_already_hit_position, Toast.LENGTH_SHORT).show();
                    } else if (responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
                        Toast.makeText(getActivity(), R.string.other_players_turn, Toast.LENGTH_SHORT).show();
                    }
                } else if(offlineMode){

                } else {
                    Toast.makeText(getActivity(), R.string.connection_failed, Toast.LENGTH_LONG).show();
                    Utility.backToLogin(getActivity());
                }
                break;
        }
    }

    @Subscribe
    public void onEvent(GameEvent gameEvent) {
        GEvent gEvent = gameEvent.getgEvent();

        switch (gEvent) {
            case SHOOT:
                if (game != null && gameGrid != null) {
                    for (int i = 0; i < GAME_BOARD_SIZE; i++) {
                        TableRow row = (TableRow) gameGrid.getChildAt(i);
                        for (int j = 0; j < GAME_BOARD_SIZE; j++) {
                            FrameLayout frameLayout = (FrameLayout) row.getChildAt(j);
                            if (frameLayout.getChildCount() != 0) {
                                for (int k = 0; k < frameLayout.getChildCount(); k++) {
                                    if (frameLayout.getChildAt(k) instanceof SelectionField) {
                                        restService.fireShot(game.getId(), i + 1, j + 1);
                                    }
                                }
                            }
                        }
                    }
                }
                break;
        }
    }

    //TODO: Offline Mode
    public Cell getSelectedField(){
        for (int i = 0; i < GAME_BOARD_SIZE; i++) {
            TableRow row = (TableRow) gameGrid.getChildAt(i);
            for (int j = 0; j < GAME_BOARD_SIZE; j++) {
                FrameLayout frameLayout = (FrameLayout) row.getChildAt(j);
                if (frameLayout.getChildCount() != 0) {
                    for (int k = 0; k < frameLayout.getChildCount(); k++) {
                        if (frameLayout.getChildAt(k) instanceof SelectionField) {
                            return new Cell(i + 1, j + 1);
                        }
                    }
                }
            }
        }
        return null;
    }


}