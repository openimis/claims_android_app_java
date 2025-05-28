package org.openimis.imisclaims.usecase;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imisclaims.Global;
import org.openimis.imisclaims.network.dto.LoginDto;
import org.openimis.imisclaims.network.dto.TokenDto;
import org.openimis.imisclaims.network.request.GetCsrfTokenGraphQLMutation;
import org.openimis.imisclaims.network.request.LoginRequest;
import org.openimis.imisclaims.tools.Log;

import java.util.concurrent.TimeUnit;

public class Login {

    @NonNull
    private final LoginRequest request;
    @NonNull
    private final Global global;
    @NonNull
    private final GetCsrfTokenGraphQLMutation getCsrfTokenGraphQLMutation;
    private static final String SHPREF_CSRF = "csrfToken";

    public Login(
            @NonNull LoginRequest request,
            @NonNull Global global,
            @NonNull GetCsrfTokenGraphQLMutation getCsrfTokenGraphQLMutation
    ) {
        this.request = request;
        this.global = global;
        this.getCsrfTokenGraphQLMutation = getCsrfTokenGraphQLMutation;
    }

    public Login() {
        this(new LoginRequest(), Global.getGlobal(), new GetCsrfTokenGraphQLMutation());
    }

    @WorkerThread
    public void execute(@NonNull String username, String password) {
        try {
            TokenDto token = request.post(new LoginDto(username.trim(), password));
            global.getJWTToken().saveTokenText(
                    token.getToken(),
                    TimeUnit.SECONDS.toMillis(token.getExpiresOn())
            );
            String csrfToken = getCsrfTokenGraphQLMutation.get();
            SharedPreferences sp = global.getDefaultSharedPreferences();
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(SHPREF_CSRF, csrfToken);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
