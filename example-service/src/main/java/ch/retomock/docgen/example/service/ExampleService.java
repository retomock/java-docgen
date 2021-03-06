package ch.retomock.docgen.example.service;

import ch.retomock.docgen.example.repository.ExampleRepository;
import ch.retomock.docgen.example.v1.ExampleServiceGrpc;
import ch.retomock.docgen.example.v1.ExampleServiceGrpc.ExampleServiceImplBase;
import ch.retomock.docgen.example.v1.SomeRequest;
import ch.retomock.docgen.example.v1.SomeResponse;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;

@GRpcService
@RequiredArgsConstructor
public class ExampleService extends ExampleServiceImplBase {

  private final ExampleRepository repository;

  @Override
  public void doSomething(SomeRequest request, StreamObserver<SomeResponse> responseObserver) {
    var builder = SomeResponse.newBuilder().setId(1);
    if (hasPermission("view:data")) {
      builder.setData("some data");
    }
    responseObserver.onNext(builder.build());
    responseObserver.onCompleted();
  }

  @PreAuthorize("hasAuthority('some-permission')")
  @Override
  public void doSomethingElse(SomeRequest request, StreamObserver<SomeResponse> responseObserver) {

    // Some dummy RPC service call
    var response = ExampleServiceGrpc.newBlockingStub(ManagedChannelBuilder.forTarget("localhost").build())
        .doSomething(request);

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void doSomethingWithDatabase(SomeRequest request, StreamObserver<SomeResponse> responseObserver) {
    var example = repository.getExampleById(request.getId());
    responseObserver.onNext(SomeResponse.newBuilder().setId(example.getId()).build());
    responseObserver.onCompleted();
  }

  private static boolean hasPermission(String permission) {
    return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
        .anyMatch(authority -> authority.getAuthority().equals(permission));
  }
}
