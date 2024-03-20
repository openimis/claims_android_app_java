package org.openimis.imisclaims.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imisclaims.Global;
import org.openimis.imisclaims.network.dto.LoginDto;
import org.openimis.imisclaims.network.dto.TokenDto;
import org.openimis.imisclaims.network.request.LoginRequest;
import org.openimis.imisclaims.repository.LoginRepository;

import java.util.Date;

public class Login {

    @NonNull
    private final LoginRequest request;
    @NonNull
    private final LoginRepository repository;


    public Login() {
        this(
                new LoginRequest(),
                Global.getGlobal().getLoginRepository()
        );
    }

    public Login(
            @NonNull LoginRequest request,
            @NonNull LoginRepository repository
    ) {
        this.request = request;
        this.repository = repository;
    }

    @WorkerThread
    public void execute(@NonNull String username, String password) {
        try {
            TokenDto token = request.post(new LoginDto(username.trim(), password));
            repository.saveToken(token.getToken(), new Date(token.getExpiresOn()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
