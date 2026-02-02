package org.openimis.imisclaims.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imisclaims.GetControlsQuery;
import org.openimis.imisclaims.GetProgramsQuery;
import org.openimis.imisclaims.domain.entity.Control;
import org.openimis.imisclaims.domain.entity.Program;
import org.openimis.imisclaims.network.request.GetProgramsGraphQLRequest;
import org.openimis.imisclaims.network.util.Mapper;

import java.util.List;
import java.util.Objects;

public class FetchPrograms {

    @NonNull
    private final GetProgramsGraphQLRequest request;

    public FetchPrograms() {
        this(new GetProgramsGraphQLRequest());
    }

    public FetchPrograms(
            @NonNull GetProgramsGraphQLRequest request
    ) {
        this.request = request;
    }

    @WorkerThread
    @NonNull
    public List<Program> execute() throws Exception {
        return Mapper.map(request.get(), dto -> {
            GetProgramsQuery.Node node = Objects.requireNonNull(dto.node());
            return new Program(
                    /* idProgram = */ node.idProgram(),
                    /* code = */ node.code(),
                    /* nameProgram = */ node.nameProgram()
            );
        });
    }


}