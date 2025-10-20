package com.example.core;

import android.content.Intent;

public final class ProfileStarter {
    public static final String EXTRA_KIND = "PROFILE_KIND"; // "COMPANY" ou "WORKER"
    public static final String EXTRA_ID   = "PROFILE_ID";   // companyId ou workerId

    private ProfileStarter() {}

    public static Intent company(android.content.Context ctx, int companyId) {
        Intent i = new Intent(ctx, Profile.class);
        i.putExtra(EXTRA_KIND, "COMPANY");
        i.putExtra(EXTRA_ID, companyId);
        return i;
    }
    public static Intent worker(android.content.Context ctx, int workerId) {
        Intent i = new Intent(ctx, Profile.class);
        i.putExtra(EXTRA_KIND, "WORKER");
        i.putExtra(EXTRA_ID, workerId);
        return i;
    }
}
