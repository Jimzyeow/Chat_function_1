package com.example.chatfunction;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class TabsAccessorAdapter extends FragmentStatePagerAdapter
{
    public TabsAccessorAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }
/*
    public TabsAccessorAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }
 */



    @NonNull
    @Override
    public Fragment getItem(int i) {

        switch(i)
        {
            case 0:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;

            case 1:
                GroupsFragment groupsFragment = new GroupsFragment();
                return groupsFragment;

            case 2:
                ContactsFragment contactsFragment = new ContactsFragment();
                return contactsFragment;

            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 3;
    }

    public CharSequence getPageTitle(int position)
    {
        switch(position)
        {
            case 0:
                return "Chats";

            case 1:
                return "Groups";

            case 2:
                return "Contacts";

            default:
                return null;
        }

    }
}
