package org.openimis.imisclaims.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imisclaims.Global;
import org.openimis.imisclaims.network.dto.LoginDto;
import org.openimis.imisclaims.network.dto.TokenDto;
import org.openimis.imisclaims.network.request.GetCrsfTokenGraphQLMutation;
import org.openimis.imisclaims.network.request.LoginRequest;

import java.util.concurrent.TimeUnit;

public class Login {

    @NonNull
    private final LoginRequest request;
    @NonNull
    private final Global global;
    @NonNull
    private final GetCrsfTokenGraphQLMutation getCrsfTokenGraphQLMutation;

    public Login(
            @NonNull LoginRequest request,
            @NonNull Global global,
            @NonNull GetCrsfTokenGraphQLMutation getCrsfTokenGraphQLMutation
    ) {
        this.request = request;
        this.global = global;
        this.getCrsfTokenGraphQLMutation = getCrsfTokenGraphQLMutation;
    }

    public Login() {
        this(new LoginRequest(), Global.getGlobal(), new GetCrsfTokenGraphQLMutation());
    }

    @WorkerThread
    public void execute(@NonNull String username, String password) {
        try {
            TokenDto token = request.post(new LoginDto(username.trim(), password));
            global.getJWTToken().saveTokenText(
                    token.getToken(),
                    TimeUnit.SECONDS.toMillis(token.getExpiresOn())
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
