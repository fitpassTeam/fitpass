package org.example.fitpass.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QDummyEntity is a Querydsl query type for DummyEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDummyEntity extends EntityPathBase<DummyEntity> {

    private static final long serialVersionUID = 2114115707L;

    public static final QDummyEntity dummyEntity = new QDummyEntity("dummyEntity");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QDummyEntity(String variable) {
        super(DummyEntity.class, forVariable(variable));
    }

    public QDummyEntity(Path<? extends DummyEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QDummyEntity(PathMetadata metadata) {
        super(DummyEntity.class, metadata);
    }

}

