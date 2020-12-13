package ch.retomock.docgen.example.service;

import ch.retomock.docgen.example.v1.ExampleServiceGrpc;
import ch.retomock.docgen.example.v1.ExampleServiceGrpc.ExampleServiceImplBase;
import ch.retomock.docgen.example.v1.SomeRequest;
import ch.retomock.docgen.example.v1.SomeResponse;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.security.access.prepost.PreAuthorize;

@GRpcService
public class ExampleService extends ExampleServiceImplBase {

  @Override
  public void doSomething(SomeRequest request, StreamObserver<SomeResponse> responseObserver) {
    responseObserver.onNext(SomeResponse.newBuilder().setId(1).build());
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
}
