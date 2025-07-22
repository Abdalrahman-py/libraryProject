package com.example.libraryproject;

import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

public class AdminPagerAdapter extends FragmentStateAdapter {

    private final List<Fragment> fragmentList = new ArrayList<>();

    public AdminPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);

        // Add your fragments in order
        fragmentList.add(new BooksFragment());       // Position 0
        fragmentList.add(new AuthorsFragment());     // Position 1
        fragmentList.add(new BorrowedBooksFragment()); // Position 2
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getItemCount() {
        return fragmentList.size();
    }

    public Fragment getFragmentAt(int position) {
        return fragmentList.get(position);
    }
}
