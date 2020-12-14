package ch.retomock.docgen.example.repository;

import static ch.retomock.docgen.example.generated.Tables.EXAMPLE;

import ch.retomock.docgen.example.generated.Tables;
import ch.retomock.docgen.example.generated.tables.records.ExampleRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ExampleRepository {

  private final DSLContext dsl;

  public ExampleRecord getExampleById(int id) {
    return dsl.selectFrom(EXAMPLE)
        .where(EXAMPLE.ID.eq(id))
        .fetchAny();
  }
}
