/******************************************************************************
* By accessing or copying this work, you agree to comply with the following   *
* terms:                                                                      *
*                                                                             *
* Copyright (c) 2019-2024 mesibo                                              *
* https://mesibo.com                                                          *
* All rights reserved.                                                        *
*                                                                             *
* Redistribution is not permitted. Use of this software is subject to the     *
* conditions specified at https://mesibo.com . When using the source code,    *
* maintain the copyright notice, conditions, disclaimer, and  links to mesibo * 
* website, documentation and the source code repository.                      *
*                                                                             *
* Do not use the name of mesibo or its contributors to endorse products from  *
* this software without prior written permission.                             *
*                                                                             *
* This software is provided "as is" without warranties. mesibo and its        *
* contributors are not liable for any damages arising from its use.           *
*                                                                             *
* Documentation: https://docs.mesibo.com/                                     *
*                                                                             *
* Source Code Repository: https://github.com/mesibo/                          *
*******************************************************************************/

package org.mesibo.messenger.AppSettings;

import android.graphics.Typeface;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.mesibo.messenger.BuildConfig;
import org.mesibo.messenger.R;

public class AboutFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);

        View v = inflater.inflate(R.layout.about, container, false);

        TextView tx = (TextView)v.findViewById(R.id.mesibologo);

        Typeface mesiboFont = Typeface.createFromAsset(getActivity().getAssets(),  "fonts/mesibo_regular.otf");

        if(null != mesiboFont)
            tx.setTypeface(mesiboFont);

        TextView version = (TextView)v.findViewById(R.id.version);
        TextView buildDate = (TextView)v.findViewById(R.id.builddate);

        version.setText("Version: " + BuildConfig.BUILD_VERSION);
        buildDate.setText("Build Time: " + BuildConfig.BUILD_TIMESTAMP);

        return v;
    }

}
