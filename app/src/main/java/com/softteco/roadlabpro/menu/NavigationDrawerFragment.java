package com.softteco.roadlabpro.menu;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.RAApplication;
import com.softteco.roadlabpro.adapters.NavigationMenuAdapter;
import com.softteco.roadlabpro.sqlite.model.FolderModel;
import com.softteco.roadlabpro.sqlite.model.RoadModel;
import com.softteco.roadlabpro.users.User;
import com.softteco.roadlabpro.util.DistanceUtil;

public class NavigationDrawerFragment extends Fragment {

    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    private NavigationDrawerCallbacks callbacks;

    public ActionBarDrawerToggle getDrawerToggle() {
        return drawerToggle;
    }

    private ActionBarDrawerToggle drawerToggle;

    public DrawerLayout getDrawerLayout() {
        return drawerLayout;
    }

    private View headerView;
    private DrawerLayout drawerLayout;
    private ListView drawerListView;
    private TextView header;
    private View fragmentContainerView;

    private TextView userName;
    private TextView projectName;
    private TextView roadName;
    private TextView overallDistance;

    private NavigationMenuAdapter adapter;

    private int currentSelectedPosition = ScreenItems.SCREEN_HEADER;
    private boolean fromSavedInstanceState;
    private boolean userLearnedDrawer;

    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        userLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            currentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            fromSavedInstanceState = true;
        }
        selectItem(currentSelectedPosition);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        headerView = View.inflate(getActivity(), R.layout.navigation_menu_item_header, null);
        userName = (TextView) headerView.findViewById(R.id.navigation_drawer_user_name);
        projectName = (TextView) headerView.findViewById(R.id.navigation_drawer_prject_name);
        roadName = (TextView) headerView.findViewById(R.id.navigation_drawer_road_name);
        overallDistance = (TextView) headerView.findViewById(R.id.navigation_drawer_distance);
        drawerListView = (ListView) inflater.inflate(
                R.layout.fragment_navigation_drawer, container, false);
        drawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });
        boolean isProjectsExists = RAApplication.getInstance().isProjectsExists();
        refreshNavigationMenu(isProjectsExists);
        drawerListView.setItemChecked(currentSelectedPosition, true);
        return drawerListView;
    }

    public void setCurrentUser(User user) {
        if (userName != null && user != null) {
            userName.setVisibility(View.VISIBLE);
            userName.setText(user.givenName);
        }
    }

    public void clearCurrentUserInfo() {
        if (userName != null) {
            userName.setVisibility(View.GONE);
            userName.setText("");
        }
    }

    public void setCurrentProject(FolderModel folder) {
        String projectNameStr = "";
        double distance = 0;
        double pathDistance = 0;
        if (folder != null) {
            distance = folder.getOverallDistance();
            pathDistance = folder.getPathDistance();
            projectNameStr = folder.getName();
        } else {
            projectNameStr = getString(R.string.none_text);
        }
        if (projectName != null) {
            projectName.setText(projectNameStr);
        }
        if (overallDistance != null) {
            String distanceStr = DistanceUtil.getDistanceString(getContext(), distance);
            String pathDistanceStr = pathDistance == 0 ?
                    getString(R.string.overall_path_distance_none) :
                    DistanceUtil.getDistanceString(getContext(), pathDistance);
            String overallDistanceStr = getString(R.string.overall_distance_text, distanceStr, pathDistanceStr);
            overallDistance.setText(Html.fromHtml(overallDistanceStr));
        }
    }

    public void setCurrentRoad(RoadModel road) {
        String roadNameStr = "";
        if (road != null) {
            roadNameStr = road.getName();
        } else {
            roadNameStr = getString(R.string.none_text);
        }
        if (roadName != null) {
            roadName.setText(roadNameStr);
        }
    }

    public void refreshNavigationMenu(boolean isProjectsExists) {
        if (headerView != null) {
            if (!isProjectsExists) {
                drawerListView.removeHeaderView(headerView);
            } else {
                if (drawerListView.getHeaderViewsCount() == 0) {
                    drawerListView.addHeaderView(headerView);
                }
            }
        }
        adapter = new NavigationMenuAdapter(getActivity(), NavMenuItem.getItems(!isProjectsExists));
        drawerListView.setAdapter(adapter);
        drawerListView.setItemChecked(currentSelectedPosition, true);
    }

    public boolean isDrawerOpen() {
        return drawerLayout != null && drawerLayout.isDrawerOpen(fragmentContainerView);
    }

    public void openDrawer() {
        if (drawerLayout != null) {
            drawerLayout.openDrawer(fragmentContainerView);
        }
    }

    public void closeDrawer() {
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(fragmentContainerView);
        }
    }

    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        fragmentContainerView = getActivity().findViewById(fragmentId);
        this.drawerLayout = drawerLayout;
        this.drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        drawerToggle = new ActionBarDrawerToggle(
                getActivity(),
                NavigationDrawerFragment.this.drawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }
                getActivity().supportInvalidateOptionsMenu();
                onNavigationDrawerClosed();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }
                if (!userLearnedDrawer) {
                    userLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }
                getActivity().supportInvalidateOptionsMenu();
                onNavigationDrawerOpened();
            }
        };

        if (!userLearnedDrawer && !fromSavedInstanceState) {
            this.drawerLayout.openDrawer(fragmentContainerView);
        }

        this.drawerLayout.post(new Runnable() {
            @Override
            public void run() {
                drawerToggle.syncState();
            }
        });

        this.drawerLayout.setDrawerListener(drawerToggle);
    }

    public void onNavigationDrawerOpened() {
        if (callbacks != null) {
            callbacks.onNavigationDrawerOpened();
        }
    }

    public void onNavigationDrawerClosed() {
        if (callbacks != null) {
            callbacks.onNavigationDrawerClosed();
        }
    }

    private void selectItem(int position) {
        currentSelectedPosition = position;
        boolean isProjectsExists = RAApplication.getInstance().isProjectsExists();
        NavMenuItem item = NavMenuItem.HEADER;
        if (isProjectsExists) {
            if (position == 0) {
                item = NavMenuItem.HEADER;
            } else if (position > 0){
                position--;
                if (adapter != null) {
                    item = (NavMenuItem) adapter.getItem(position);
                }
            }
        } else {
            if (adapter != null) {
                item = (NavMenuItem) adapter.getItem(position);
            }
        }
        if (drawerListView != null) {
            drawerListView.setItemChecked(currentSelectedPosition, true);
        }
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(fragmentContainerView);
        }
        if (callbacks != null) {
            callbacks.onNavigationDrawerItemSelected(item);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            callbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, currentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (drawerLayout != null && isDrawerOpen()) {
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        if (drawerToggle.onOptionsItemSelected(item)) {
//            return true;
//        }
//
//        if (item.getItemId() == R.id.action_add_new) {
//            final MainActivity activity = (MainActivity) getActivity();
//            activity.replaceFragment(LoginFragment.newInstance(), true);
//            return true;
//        }
//
        return super.onOptionsItemSelected(item);
    }

    private void showGlobalContextActionBar() {
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(getString(R.string.app_name));
    }

    public void setHomeIndicator(boolean is) {
        drawerToggle.setDrawerIndicatorEnabled(is);
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }

    public interface NavigationDrawerCallbacks {
        void onNavigationDrawerItemSelected(NavMenuItem item);
        void onNavigationDrawerOpened();
        void onNavigationDrawerClosed();
    }
}
